# HabitHub

Eine native Android-App zum Tracking täglicher Gewohnheiten. Sie hilft Userinnen und Usern dabei, Routinen aufzubauen, ihren Fortschritt im Blick zu behalten und durch dezentes Feedback und Sensor-Interaktionen motiviert zu bleiben.

## Team

| Mitglied | Zuständigkeit |
|---|---|
| Anja | Datenebene (Entities, Room-DAOs, Repository, Persistenz) |
| Stefan | Anwendungslogik (ViewModels, Sensor-Integration, Screen-Verknüpfung) |
| Sonja | UI/UX (Theme, Navigation, Screens, visueller Feinschliff) |

## Was die App kann

- **Habits anlegen** mit Name, Beschreibung, Emoji, Farbe und der Auswahl der Wochentage, an denen sie absolviert werden sollen.
- **Habits abhaken** per Tap oder durch Swipe-Geste nach rechts auf der Habit-Karte.
- **Habits löschen** per Long-Press oder Swipe-Geste nach links, jeweils mit Bestätigungsdialog.
- **Fortschritt verfolgen** über eine Tages-Fortschrittsanzeige, eine pro-Habit-Streak, die längste je erreichte Streak sowie Wochen- und 30-Tage-Übersichten.
- **Statistiken einsehen** auf einem eigenen Screen, inklusive Completion-Rate pro Habit und Gesamtfortschritt.
- **Motivation bekommen**: Bei Schütteln des Geräts erscheint ein zufällig gewählter Motivationsspruch als Snackbar.
- **Schritte zählen** über den im Gerät verbauten Schrittsensor — die aktuelle Schrittzahl wird direkt am Home-Screen angezeigt.
- **Theme wechseln** zwischen Light- und Dark-Mode per einfachem Tap; die Auswahl wird über App-Starts hinweg gespeichert.

## Wie die Projekt-Anforderungen erfüllt sind

Die Vorgabe verlangt, dass mindestens zwei der fünf Themenbereiche abgedeckt werden. HabitHub deckt **drei** davon ab:

### 1. Data Centricity (Datenbank)
Eine lokale Room-Datenbank ist das Rückgrat der App.
- Zwei Entities (`Habit`, `HabitCompletion`) mit jeweils eigenen DAOs.
- Ein `HabitRepository` als Vermittler zwischen ViewModels und Datenbank.
- Sämtliche Habit-Daten — inklusive Abhak-Historie — überstehen App-Neustarts.
- Eine zusätzliche `DataStore`-Preferences-Schicht speichert die Theme-Auswahl persistent.

### 2. Sensoren
- **Schrittzähler** (`Sensor.TYPE_STEP_COUNTER`): zeigt die seit App-Start gemachten Schritte am Home-Screen an. Benötigt die Runtime-Permission `ACTIVITY_RECOGNITION`.
- **Beschleunigungssensor / Shake-Detection**: Schütteln des Geräts triggert einen zufälligen Motivationsspruch in einer Snackbar.

### 3. Special Gestures
- **Swipe nach rechts** auf einer Habit-Karte → Habit für heute als erledigt markieren.
- **Swipe nach links** auf einer Habit-Karte → Habit löschen (mit Bestätigung).
- **Long-Press** auf einer Habit-Karte → direkter Sprung zum Edit-Screen.

## Architektur

Die App folgt einer klassischen **MVVM**-Struktur mit Jetpack Compose als UI-Ebene.

```
ui/                  ← Composable Screens und ViewModels (View + ViewModel)
 ├── screen/         ← HomeScreen, AddHabitScreen, EditHabitScreen, DetailScreen, StatsScreen
 ├── viewmodel/      ← HabitViewModel, ThemeViewModel (+ Factories)
 ├── navigation/     ← Screen-Routen
 └── theme/          ← Material-3-Theme, Farben, Typografie

data/                ← Modell-Ebene
 ├── model/          ← Habit, HabitCompletion Entities
 ├── database/       ← Room-Datenbank + DAOs
 ├── repository/     ← HabitRepository
 └── preferences/    ← ThemePreference (DataStore)

sensor/              ← ShakeDetector, StepCounterSensor
MainActivity.kt      ← Hostet den NavHost, Sensor-Lifecycle, Theme-Verkabelung
HabitHubApplication.kt ← Stellt die Singleton-Instanz der Datenbank bereit
```

Daten fließen von der Datenbank aufwärts als `Flow<...>`, werden in den ViewModels zu `StateFlow` konvertiert und vom Composable per `collectAsState()` beobachtet. Schreibvorgänge laufen den umgekehrten Weg: UI-Event → ViewModel-Funktion → Repository → DAO → Datenbank.

## Tech Stack

- **Sprache:** Kotlin
- **UI:** Jetpack Compose, Material 3, Material Icons Extended
- **Architektur:** MVVM (ViewModel + StateFlow), unidirektionaler Datenfluss
- **Persistenz:** Room (Datenbank, DAOs, Entities) und Jetpack DataStore (Preferences)
- **Navigation:** Navigation Compose
- **Lifecycle:** Lifecycle ViewModel Compose, viewModelScope, Activity Result API (Runtime-Permissions)
- **Sensoren:** Android `SensorManager` (`TYPE_STEP_COUNTER`, `TYPE_ACCELEROMETER`)
- **Min SDK:** 24 / **Target SDK:** 36

## Projekt-Setup

Voraussetzungen:
- Android Studio (aktuelle Stable-Version)
- JDK 11 oder höher
- Android-Gerät oder Emulator (der Schrittzähler funktioniert nur auf Geräten mit physischem Step-Sensor)

Bauen und starten:
1. Repository klonen: `git clone https://github.com/HabitHubAAU/App.git`
2. Projekt in Android Studio öffnen
3. Gradle-Sync abwarten
4. Gerät anschließen oder Emulator starten
5. **Run** → **Run 'app'**

## Berechtigungen

| Berechtigung | Zweck |
|---|---|
| `ACTIVITY_RECOGNITION` | Zugriff auf den Schrittzähler des Geräts |

Die Berechtigung wird beim ersten Start zur Laufzeit angefragt und kann jederzeit in den Systemeinstellungen widerrufen werden.

## Verwendete Fremdbibliotheken

Alle eingesetzten Bibliotheken sind Standard-AndroidX-/Jetpack-Komponenten, deklariert in `gradle/libs.versions.toml`. Es wurde kein fremder Beispiel- oder Drittanbieter-Code in das Projekt übernommen.

## Einsatz von KI-Tools

Wir geben gemäß den Vorgaben offen an: Generative KI-Tools (Claude) wurden während der Entwicklung eingesetzt, um:
- Features zu brainstormen und architektonische Trade-offs zu diskutieren,
- Bei wiederkehrendem Boilerplate-Code zu helfen (z.B. ViewModel-Factories, DataStore-Wrapper),
- Refactorings vorzuschlagen und unbekannte Compose-APIs zu erklären.

Sämtlicher KI-generierter Code wurde von den Teammitgliedern geprüft, angepasst und getestet, bevor er committet wurde. Die alleinige Verantwortung für den entstandenen Code liegt beim Team.

## Bekannte Einschränkungen und Finalization-Plan

Die App ist im Rahmen der Projektarbeit funktional vollständig. Bewusst nicht implementiert wurden:

- **Habit-Erinnerungen / Notifications** — würde `WorkManager` plus einen Notification-Scheduler erfordern.
- **Eigener Settings-Screen** — bislang nur Theme-Toggle in der Home-TopBar; ein dedizierter Settings-Screen wäre ein logischer nächster Schritt.
- **Habit-Archivierung** — Löschen entfernt aktuell die gesamte Historie eines Habits; ein "Archiv"-Modus würde Streak-Daten erhalten.
- **Notizen pro Completion** — Completions speichern derzeit nur einen Zeitstempel.
- **Eigenes Launcher-Icon in optimalen DPI-Stufen** — ein eigenes Icon ist enthalten, jedoch nicht für sämtliche Auflösungen separat optimiert.