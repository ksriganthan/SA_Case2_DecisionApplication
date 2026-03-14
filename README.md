# SA Case 2 – Decision Application

Eine regelbasierte **Versandentscheidungs-Anwendung** entwickelt an der FHNW im Rahmen des Software Architecture-Moduls (Case 2).  
Die Anwendung nimmt Sendungsdaten (Gewicht & Zielland) entgegen, wertet sie gegen eine **Drools-Entscheidungstabelle** aus und liefert automatisch eine Versandempfehlung zurück.

---

## Inhaltsverzeichnis

1. [Überblick & Architektur](#1-überblick--architektur)
2. [Technologie-Stack](#2-technologie-stack)
3. [Projektstruktur](#3-projektstruktur)
4. [Datenmodell](#4-datenmodell)
5. [Entscheidungsregeln (Drools Decision Table)](#5-entscheidungsregeln-drools-decision-table)
6. [Schichten der Anwendung](#6-schichten-der-anwendung)
   - [6.1 REST API – `DecisionAPI`](#61-rest-api--decisionapi)
   - [6.2 Service – `DecisionService`](#62-service--decisionservice)
   - [6.3 Rule Engine – `RuleEngineLauncher`](#63-rule-engine--ruleenginelauncher)
7. [Ablauf eines Entscheidungs-Requests](#7-ablauf-eines-entscheidungs-requests)
8. [API-Dokumentation](#8-api-dokumentation)
9. [Konfiguration](#9-konfiguration)
10. [Anwendung starten](#10-anwendung-starten)

---

## 1. Überblick & Architektur

Die Anwendung implementiert ein **regelbasiertes Entscheidungssystem** für die automatische Versandsteuerung.  
Gegeben eine Sendung mit einem bestimmten **Gewicht** und einem **Zielland**, entscheidet das System:

- Soll die Sendung **automatisch** oder **manuell** bearbeitet werden?
- Welche **Versandmethode** soll verwendet werden (`SPECIAL`, `NORMAL`, `AIR`)?
- Welcher **Carrier** (Transportdienstleister) ist zuständig?
- Welche **Regel-ID** hat die angewandte Entscheidungsregel?

```
┌──────────────┐     POST /decision/make     ┌─────────────────┐
│   Client /   │ ─────────────────────────▶  │   DecisionAPI   │  (REST-Controller)
│   Camunda    │ ◀───────────────────────── │  (RestAPI/)     │
└──────────────┘     Decision (JSON)         └────────┬────────┘
                                                       │
                                                       ▼
                                             ┌─────────────────┐
                                             │ DecisionService │  (Validierung)
                                             │  (Service/)     │
                                             └────────┬────────┘
                                                       │
                                                       ▼
                                             ┌─────────────────────┐
                                             │ RuleEngineLauncher  │  (Drools KIE)
                                             │  (RuleEngine/)      │
                                             └────────┬────────────┘
                                                       │
                                                       ▼
                                             ┌─────────────────────┐
                                             │  ShippingRules      │  (Excel-Regelwerk)
                                             │  .drl.xls           │
                                             └─────────────────────┘
```

---

## 2. Technologie-Stack

| Technologie              | Version        | Verwendungszweck                                                     |
|--------------------------|----------------|----------------------------------------------------------------------|
| **Java**                 | 17+            | Programmiersprache                                                   |
| **Spring Boot**          | 4.0.3          | Anwendungsframework, REST-Server (Port 8081)                         |
| **Drools / KIE**         | 8.32.0.Final   | Regel-Engine zur Entscheidungsauswertung (`kie-ci`)                  |
| **Drools Decision Table**| 8.32.0.Final   | Excel-basiertes Regelwerk (`.drl.xls`, `drools-decisiontables`)      |
| **MVEL2**                | 2.5.2.Final    | MVEL-Override: Kompatibilitäts-Fix für JDK 17+ (entfernter Compiler) |
| **Camunda External Task**| 1.3.1          | Dependency für geplante BPMN-Integration (aktuell nicht aktiv genutzt)|
| **Jersey (JAX-RS)**      | 2.31           | REST-Client-Bibliothek (Dependency, für externe Aufrufe vorgesehen)  |
| **Jackson Databind**     | 2.10.0         | JSON-Serialisierung / -Deserialisierung                              |
| **Maven**                | –              | Build-Tool & Dependency-Management                                   |

---

## 3. Projektstruktur

```
SA_Case2_DecisionApplication/
├── pom.xml                                  # Maven-Konfiguration & Dependencies
├── src/
│   └── main/
│       ├── java/com/fhnw/sa_case2_decisionapplication/
│       │   ├── SaCase2DecisionApplication.java   # Spring Boot Einstiegspunkt
│       │   ├── Data/
│       │   │   ├── DecisionArgs.java             # Eingabe- & Ausgabedaten (Fact)
│       │   │   └── Decision.java                 # Rückgabe-DTO
│       │   ├── RestAPI/
│       │   │   └── DecisionAPI.java              # REST-Controller
│       │   ├── Service/
│       │   │   └── DecisionService.java          # Geschäftslogik & Validierung
│       │   └── RuleEngine/
│       │       └── RuleEngineLauncher.java       # Drools KIE Integration
│       └── resources/
│           ├── application.properties            # App-Konfiguration
│           └── rules/
│               └── ShippingRules.drl.xls         # Entscheidungstabelle (Excel)
└── target/                                  # Kompilierte Klassen (generiert)
```

---

## 4. Datenmodell

### `DecisionArgs` – Eingabe & interner Zustand

Das Hauptobjekt, das durch die Regel-Engine verarbeitet wird. Es enthält **Eingabefelder** (werden vor der Verarbeitung gesetzt) und **Ausgabefelder** (werden durch die Regeln befüllt).

| Feld                 | Typ                  | Richtung  | Beschreibung                     |
|----------------------|----------------------|-----------|----------------------------------|
| `weight`             | `Integer`            | Eingabe   | Gewicht der Sendung in Kilogramm |
| `destinationCountry` | `DestinationCountry` | Eingabe   | Zielland der Sendung             |
| `decisionType`       | `DecisionType`       | Ausgabe   | `AUTOMATIC` oder `MANUAL`        |
| `shippingMethod`     | `ShippingMethod`     | Ausgabe   | `SPECIAL`, `NORMAL` oder `AIR`   |
| `carrier`            | `String`             | Ausgabe   | Name des zugewiesenen Carriers   |
| `ruleId`             | `Integer`            | Ausgabe   | ID der angewendeten Regel        |

**Enumerationen:**

```
DestinationCountry:  ARG | JAP | DE | CH | RUS
DecisionType:        AUTOMATIC | MANUAL
ShippingMethod:      SPECIAL | NORMAL | AIR
```

### `Decision` – Rückgabe-DTO

Enthält nur die Ausgabefelder aus `DecisionArgs` und wird als JSON-Antwort zurückgegeben:

```json
{
  "decisionType": "AUTOMATIC",
  "shippingMethod": "AIR",
  "carrier": "DHL",
  "ruleId": 3
}
```

---

## 5. Entscheidungsregeln (Drools Decision Table)

Die Regeln sind in einer **Excel-Datei** (`ShippingRules.drl.xls`) als Decision Table definiert.  
Drools konvertiert diese Tabelle zur Laufzeit in DRL-Regeln (Drools Rule Language).

**Regelstruktur:**  
Jede Zeile in der Excel-Tabelle entspricht einer Regel mit folgenden Spalten:

| Spalte            | Bedeutung                                   |
|-------------------|---------------------------------------------|
| Regelname         | Eindeutiger Name der Regel (z. B. `DE`)     |
| `destinationCountry` | Bedingung: Zielland                      |
| `weight`          | Bedingung: Gewichtsbereich (z. B. `<60`)    |
| `decisionType`    | Aktion: Setze `AUTOMATIC` oder `MANUAL`     |
| `shippingMethod`  | Aktion: Setze Versandmethode                |
| `carrier`         | Aktion: Setze Carrier-Name                  |
| `ruleId`          | Aktion: Setze die Regel-ID                  |

**Bekannte Regeln (aus den Fehlermeldungen ablesbar):**

| Regel-Name    | Zielland | Gewicht      | Beschreibung                   |
|---------------|----------|--------------|--------------------------------|
| `ARG <60`     | ARG      | < 60         | Argentinien, leichte Sendung   |
| `ARG 60-500`  | ARG      | 60 – 500     | Argentinien, mittlere Sendung  |
| `JAP <=200`   | JAP      | ≤ 200        | Japan bis 200g                 |
| `DE`          | DE       | –            | Deutschland                    |
| `CH`          | CH       | –            | Schweiz                        |

---

## 6. Schichten der Anwendung

### 6.1 REST API – `DecisionAPI`

**Pfad:** `com.fhnw.sa_case2_decisionapplication.RestAPI.DecisionAPI`

Der REST-Controller stellt einen einzigen Endpunkt bereit und delegiert die Verarbeitung an den `DecisionService`.  
Er behandelt Fehler und gibt entsprechende HTTP-Statuscodes zurück:

- **200 OK** – Entscheidung erfolgreich getroffen
- **400 Bad Request** – Validierungsfehler (fehlendes Zielland oder ungültiges Gewicht)
- **500 Internal Server Error** – Fehler in der Regel-Engine

```java
@RestController
@RequestMapping("/decision")
public class DecisionAPI {

    @PostMapping(value = "/make", produces = "application/json")
    public ResponseEntity<?> makeDecision(@RequestBody DecisionArgs decisionArgs) { ... }
}
```

---

### 6.2 Service – `DecisionService`

**Pfad:** `com.fhnw.sa_case2_decisionapplication.Service.DecisionService`

Enthält die **Validierungslogik** vor der Regelauswertung:

1. Prüft, ob `destinationCountry` gesetzt ist → sonst `IllegalArgumentException`
2. Prüft, ob `weight > 0` ist → sonst `IllegalArgumentException`
3. Übergibt `DecisionArgs` an den `RuleEngineLauncher`
4. Mappt das Ergebnis in ein `Decision`-Objekt und gibt es zurück

```java
@Service
public class DecisionService {
    public Decision validateConsignment(DecisionArgs decisionArgs) {
        // 1. Validierung
        // 2. Regelauswertung via RuleEngineLauncher
        // 3. Mapping zu Decision-DTO
    }
}
```

---

### 6.3 Rule Engine – `RuleEngineLauncher`

**Pfad:** `com.fhnw.sa_case2_decisionapplication.RuleEngine.RuleEngineLauncher`

Verwaltet den **gesamten Drools-Lebenszyklus** pro Aufruf:

```
Schritt 1: System.setProperty("drools.dialect.mvel.strict", "false")
           → MVEL-Strict-Mode deaktivieren (Kompatibilitäts-Fix für JDK 17+)

Schritt 2: KieServices.Factory.get()
           → Zugriff auf den KIE-Dienst

Schritt 3: ResourceFactory.newClassPathResource("rules/ShippingRules.drl.xls", getClass())
           → Excel-Regeldatei aus dem Classpath laden

Schritt 4: kieFileSystem.write(dt) → KieBuilder.buildAll()
           → Excel-Tabelle in DRL-Regeln kompilieren
           → Bei Fehler: RuntimeException werfen

Schritt 5: kieContainer.newKieSession()
           → Neue Regel-Session erstellen

Schritt 6: decisionArgs.setDecisionType(MANUAL)  ← Standardwert: MANUELL
           kieSession.insert(decisionArgs)         ← Fact einfügen
           kieSession.fireAllRules()               ← Alle passenden Regeln auswerten

Schritt 7: kieSession.dispose()
           → Session sauber beenden

Schritt 8: Ergebnis zurückgeben
           → Falls Regeln gefeuert: DecisionType evtl. auf AUTOMATIC gesetzt
           → Falls keine Regel: bleibt MANUAL
```

**Wichtiger Hinweis:**  
Der `decisionType` wird **vor** der Regelauswertung auf `MANUAL` gesetzt. Nur wenn eine Regel explizit `AUTOMATIC` setzt, ändert sich der Wert. So ist sichergestellt, dass bei fehlender Regelabdeckung immer eine manuelle Prüfung angefordert wird.

---

## 7. Ablauf eines Entscheidungs-Requests

```
Client sendet POST /decision/make
         │
         ▼
DecisionAPI.makeDecision(DecisionArgs)
         │
         ▼
DecisionService.validateConsignment(DecisionArgs)
    ├─ destinationCountry == null? → 400 Bad Request
    ├─ weight == null || weight <= 0? → 400 Bad Request
    └─ Validierung OK
         │
         ▼
RuleEngineLauncher.makeDecision(DecisionArgs)
    ├─ drools.dialect.mvel.strict = false  (JDK-17-Kompatibilität)
    ├─ Excel-Datei laden & kompilieren
    ├─ KIE-Session erstellen
    ├─ decisionType = MANUAL  (Standardwert)
    ├─ Fact in Session einfügen
    ├─ Regeln auswerten (fireAllRules)
    │    ├─ Regel trifft zu → setzt decisionType, shippingMethod, carrier, ruleId
    │    └─ Keine Regel → decisionType bleibt MANUAL
    └─ Session beenden
         │
         ▼
DecisionService: Ergebnis in Decision-DTO mappen
         │
         ▼
DecisionAPI: ResponseEntity.ok(decision) → 200 OK
         │
         ▼
Client erhält JSON-Antwort
```

---

## 8. API-Dokumentation

### POST `/decision/make`

Trifft eine Versandentscheidung anhand von Gewicht und Zielland.

**Request-Header:**
```
Content-Type: application/json
```

**Request-Body:**
```json
{
  "weight": 100,
  "destinationCountry": "ARG"
}
```

| Feld                 | Typ     | Pflicht | Beschreibung                                                 |
|----------------------|---------|-------|--------------------------------------------------------------|
| `weight`             | Integer |  Ja | Gewicht in Gramm (muss > 0 sein)                             |
| `destinationCountry` | String  |  Ja | Zielland: `ARG`, `JAP`, `DE`, `CH`, `RUS`                   |

**Antwort (200 OK):**
```json
{
  "decisionType": "AUTOMATIC",
  "shippingMethod": "AIR",
  "carrier": "DHL",
  "ruleId": 2
}
```

**Fehlerfälle:**

| HTTP-Code | Ursache                                     |
|-----------|---------------------------------------------|
| `400`     | Zielland fehlt oder Gewicht ist ≤ 0         |
| `500`     | Fehler beim Laden/Kompilieren der Regeln    |

**Beispiel mit curl:**
```bash
curl -X POST http://localhost:8081/decision/make \
     -H "Content-Type: application/json" \
     -d '{"weight": 100, "destinationCountry": "ARG"}'
```

---

## 9. Konfiguration

Die Konfiguration befindet sich in `src/main/resources/application.properties`:

```properties
spring.application.name=SA_Case2_DecisionApplication
server.port=8081
```

| Eigenschaft                   | Wert                          | Beschreibung                    |
|-------------------------------|-------------------------------|---------------------------------|
| `spring.application.name`     | SA_Case2_DecisionApplication  | Anwendungsname                  |
| `server.port`                 | 8081                          | HTTP-Port des REST-Servers      |

---

## 10. Anwendung starten

### Voraussetzungen

- **Java 17** oder höher installiert
- **Maven 3.x** installiert (oder `mvnw` Wrapper verwenden)

### Mit Maven starten

```bash
# Im Projektverzeichnis:
./mvnw spring-boot:run
```

### Als JAR starten

```bash
./mvnw clean package
java -jar target/SA_Case2_DecisionApplication-0.0.1-SNAPSHOT.jar
```

### Mit IntelliJ IDEA starten

1. `SaCase2DecisionApplication.java` öffnen
2. Grünen Play-Button ▶ neben der `main`-Methode klicken
3. Die Anwendung startet auf **http://localhost:8081**

---

## 11. Tests

Im Projekt ist ein einfacher Spring-Boot-Kontexttest enthalten:

| Testklasse                          | Test            | Beschreibung                                  |
|-------------------------------------|-----------------|-----------------------------------------------|
| `SaCase2DecisionApplicationTests`   | `contextLoads`  | Prüft, ob der Spring-Anwendungskontext korrekt startet |

Tests ausführen:

```bash
./mvnw test
```

---

*Dokumentation erstellt für SA Case 2 – FHNW Software Architecture*

