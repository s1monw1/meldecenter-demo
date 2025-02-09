# Lexware Meldecenter Demo Application simon.wirtz

## Aufgabenstellung

Erstellt werden soll eine Kotlin-Applikation auf Basis von Spring Boot 3 und PostgreSQL

Gegeben ist folgendes Szenario: Das Meldecenter nimmt Meldungen verschiedener Typen von unterschiedlichen Clients und Mandanten an, verarbeitet diese und sendet sie in aufbereiteter Form je nach Typ an eine behördliche Gegenstelle.

Ziel der Aufgabe: Schreibe eine Demo-Anwendung mit einem Endpunkt zum Einliefern von Meldungen unterschiedlichen Typs und Inhalts, welche Kontaktdaten inklusive einer Telefonnummer enthalten. Bitte beschränke dich auf maximal zwei Meldungstypen.
Bei der Verarbeitung der Meldungen werden die Telefonnummern auf ein einheitliches Nummernformat vereinheitlicht, bevor sie an die Gegenstelle versandt werden.
Um die Anzahl an Requests zu den Gegenstellen zu minimieren, werden die verarbeiteten Meldungen regelmäßig, nach Typ gruppiert und in aggregierten Batches an die entsprechende fiktive Gegenstelle versendet. Stelle sicher, dass Meldungen nur einmal an die Gegenstelle übermittelt werden können.
Zudem können die einliefernden Clients Informationen über den Verarbeitungs- und Versand-Zustand erhalten.

Eine UI wird für die Aufgabe nicht benötigt.

Setze die Aufgabe in einer Qualität um, die du persönlich für sinnvoll erachtest - wir machen dazu keine Vorgaben.

## Aufbau der Anwendung

### Tech Stack
- Kotlin
- Spring Boot
- PostgreSQL
- [Flyway](https://github.com/flyway/flyway)
- Google [libphonenumber](https://github.com/google/libphonenumber?tab=readme-ov-file)
- [Testcontainers](https://testcontainers.com/)
- JUnit
- Docker

### Anwendung starten
Die Applikation wird mittels Docker-Compose gestartet und verbindet sich damit zu einer lokalen Postgres-Instanz:

Ausführen im Root-Verzeichnis des Projektes.
```
docker-compose -p lexware-demo-meldecenter up
```

### API Documentation

Die REST-Endpoints werden rudimentär mittels org.springdoc via Swagger dokumentiert, die Dokumentation ist über [{URL}/swagger-ui/index.html]({URL}/swagger-ui/index.html)
aufrufbar.

#### API in MeldecenterController

Der `MeldecenterController` stellt die Schnittstelle zu den einliefernden Clients dar und verfügt über zwei Endpunkte:
1. `report(report: ReportDTO)`
2. `getReportStatus(id: Int): RequestedReportStatus`

Hiermit werden die beiden Anforderungen zum Einliefern von Reports (1) und Abfragen des Bearbeitungsstatus (2) bedient.

**Beispiel Request für `report`**

```
POST /api/meldecenter HTTP/1.1
Content-Type: application/json

{
  "type": "SV_RV",
  "data": {
    "rvNumber": "123ab",
    "date": "2024-11-03",
    "amount": "12345"
  },
  "contact": {
    "name": "Simon",
    "surName": "Wirtz",
    "address": "Main Street 1a, 50944 Cologne",
    "phoneNumber": "01738888888"
  }
}
```

**Beispiel Request für `getReportStatus`**

```
GET /api/meldecenter/19 HTTP/1.1
Content-Type: application/json

```

### Services

1. **ReportsService**

Der `ReportsService` stellt interne Schnittstellen zum Arbeiten mit persistieren Reports dar und erlaubt das Speichern, Updaten und Abfragen dieser.
Dieser Service nutzt das `ReportsRepository` zur Interaktion mit der angebundenen Postgres Datenbank.

2. **ReportsResolver und DeliveredReportResolver**

Da die Anwendung mit Reports generischer Natur arbeitet und möglichst erweiterbar sein soll, benötigt es generische Services zum Arbeiten mit Reports, die zur Laufzeit teilweise unbekannten Typs sind.
Der `ReportsResolver` stellt Funktionen bereit um von Clients gelieferte Reports in einen bekannten Report-Typen zu konvertieren. In der vorliegenden Implementierung existieren zwei Report-Typen: `KVReport` (Krankenversicherung) und `RVReport` (Rentenversicherung). Des weiteren konvertiert
der Service entities aus der Datenbank in ebendiese konkreten Report-Typen, da auch die Datenbank eine generische Tabelle `reports` nutzt und die Laufzeittypen dort nicht direkt hinterlegt sind.
Das Resolving geschieht im `DeliveredReportResolver`, der zudem die Anforderung erfüllt, dass gelieferte Telefonnummern in ein einheitliches Format gebracht werden.

3. **PhoneNumberUnifier**

Der `PhoneNumberUnifier` nutzt die Google library `libphonenumber` um Telefonnummern zu verifizieren und in das einheitliche E164 Format zu wandeln.

4. **ReportEndpoint**

Der `ReportEndpoint` stellt die Schnittstelle zur Außenwelt dar. Es sind zwei simple Gegenstellen implementiert, die jeweils eine Liste von Reports zur Verarbeitung erwarten. Der `KVReportService` ist 
konfigurierbar dahingehend, dass gewisse zufällige Fehlerfälle simuliert werden können. Der `RVReportService` auf der anderen Seite verarbeitet Reports mit ausschließlich positivem Verarbeitungsausgang. In
der Realität würden Implementierungen des `ReportEndpoint` Integrationen zu APIs der externen Gegenstellen darstellen, in denen die Umwandlung der internen in die externe Datendarstellung erfolgt. Mehr dazu im Abschnitt zu möglichen [Erweiterungen](#erweiterungen).

5. **ReportsDispatcher**

Der `ReportsDispatcher` ist ein über Spring geschedulter Service, der regelmäßig nach ungesendeten Reports schaut und diese an die passenden `ReportEndpoint`s vermittelt. Um die Last auf dem externen System im Rahmen zu halten werden beispielhaft lediglich 3 Reports pro Typ gleichzeitig 
bearbeitet. Der Service ist verantwortlich für das Updaten der Entities in der Datenbank mit dem aktuell passenden `ReportStatus` (AWAITING_SEND,IN_TRANSIT, SENT, FAILED) und stellt damit sicher, dass Reports nur einmalig gesendet werden können.

## Erweiterungen

1. **HTTP Kommunikation mit angebundenen Services**

In der aktuellen Implementierung wurde auf eine realistische Kommunikation zu den Reportingsystemen verzichtet um den Scope der Aufgabe zu beschränken. Anstatt über HTTP an tatsächliche Server zu kommunizieren, wurden Implementierungen verwendet, die teilweise zufallsbasiert Reports fehlschlagen bzw. gelingen lassen. 

2. **Production-ready Scheduling/Batching Ansatz**

Der `ReportsDispatcher` wird über Spring's `@Scheduled` Annotation als ein Scheduled Task registriert, um den regelmäßigen Batch-Versand zu ermöglichen. Der Ansatz skaliert nicht ausreichend gut und müsste in einer produktiven Anwendung mit einer besseren Lösung ersetzt werden. Das Hauptproblem wäre, dass in einer Architektur
mit mehreren Service-Instanzen dafür zu sorgen ist, dass nur eine Instanz das Batching/Reporting erledigt. Es ist denkbar, das Scheduling extern vom eigentlichen Service zu handhaben um damit eine spezifische Backend-Instanz (vom Loadbalancer gewählt) zu triggern.  

3. **Event-basierte Kommunikation zur Abarbeitung von Reports**

Möglicherweise wäre denkbar einen Event-Driven-Design Ansatz zu nutzen, um die Anwendung skalierbarer und flexibler zu gestalten. Denkbar sind Technologien wie Kafka oder RabbitMQ. 

4. **Tracking der Status-Änderungen zur besseren Nachverfolgung**

Als Teilaspekt des Reportings an die externen Systeme werden Reports auf Ebene der Datenbank in Bezug auf die vorhanden `ReportStatus` verändert. Zum Beispiel wird hierbei ein Report, der im `AWAITING_SEND` Status ist, zunächst in den Status `IN_TRANSIT`, und am Ende auf `SENT` oder `FAILED` gesetzt. Es ist aktuell nicht langfristig nachvollziehbar, wann
welcher Status weshalb verändert wurde. Aus diesem Grund wäre es sinnvoll eine zusätzliche (Audit-)Tabelle für jegliche Status-Änderung zu implementieren.

5. **Fault-Tolerance und Resilience: Error Handling, Circuit Breakers, Retries**

Im Zusammenhang mit (1) und der sinnvollen Erweiterung auf HTTP-basierte Kommunikation zu den externen Reporting-Systemen wäre es wünschenswert auf mögliche Fehler dahingehend zu reagieren, dass ein fehlgeschlagenes Reporting mittels Retries verbessert wird. Auf gewisse Fehler kann unmittelbar nach Fehlschlag reagiert werden. So wäre es 
denkbar kurzzeitig nicht-antwortende APIs bereits umgehend erneut anzufragen. In anderen Fällen wäre ein Retry im nächsten Scheduling-Zyklus des `ReportsDispatcher` erfolgsversprechender. Hierzu wäre denkbar, die reports Datenbank-Tabelle um einen Failure-Counter zu erweitern und Reports, die im Status `FAILED` sind, erneut zu versenden.
Circuit Breaker können genutzt werden, um den Status der externen Systeme zu überwachen und einzubeziehen und das System insgesamt Fehler-toleranter zu gestalten. 
 
6. **Authentication**

In einem produktiven Umfeld müsste der Service über Auth-Mechanismen abgesichert werden, sodass die APIs nur für authentifizierte Nutzer zugänglich sind. 
