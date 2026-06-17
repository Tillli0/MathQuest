# MathQuest – Der Kampf der Zahlen
## BlueJ-Projekt: 1×1 Kopfrechenspiel

### Installation & Start
1. Alle `.java`-Dateien in einen Ordner entpacken
2. In BlueJ: **Project → Open Non BlueJ...** → Ordner auswählen
3. Alle Klassen kompilieren (Compile-Button oder Strg+K)
4. Rechtsklick auf `MathQuest` → `void main(String[] args)` → OK

### Spielablauf
- Du bist ein Held in einer Fantasy-Arena
- Für jede richtige Antwort greifst du den Gegner an (Animationen!)
- Bei falscher Antwort schlägt der Gegner zurück
- Nach 10 Aufgaben: Auswertung mit Zeitdiagramm
- Highscore-Eintrag mit Name möglich

### Dateien
| Datei | Beschreibung |
|-------|-------------|
| `MathQuest.java` | Einstiegspunkt (main-Methode) |
| `StartScreen.java` | Animierter Titelbildschirm mit Menü |
| `GameScreen.java` | Hauptspiel: Held + 5 verschiedene Monster |
| `ResultScreen.java` | Ergebnis, Zeitdiagramm, Sterne, Konfetti |
| `HighscoreScreen.java` | Top-10 Highscore-Tabelle |
| `GameData.java` | Highscore-Speicherung (highscores.dat) |

### Features
- ✅ Zufällige 1×1-Aufgaben (1–10)
- ✅ Millisekunden-genaue Zeitmessung
- ✅ 5 verschiedene Monster (Goblin, Skelett, Troll, Werwolf, Drache)
- ✅ Angriffs-Animationen mit Partikeleffekten
- ✅ Schwebender Held mit Schwert & Schild
- ✅ HP-Balken für Held und Gegner
- ✅ Fortschrittsanzeige (10 Punkte)
- ✅ Flash-Effekte bei Treffern
- ✅ Konfetti bei 3-Sterne-Ergebnis
- ✅ Zeitbalken-Diagramm im Ergebnis
- ✅ Sterne-Bewertung (1–3 Sterne)
- ✅ Persistente Highscore-Tabelle (Top 10)
- ✅ Animierter Startscreen mit Sterne-Regen
- ✅ ~60 fps Animationen

### Bewertungssystem
| Sterne | Bedingung |
|--------|-----------|
| ★★★ | Ø < 2000ms UND ≥ 9 richtig |
| ★★☆ | Ø < 4000ms UND ≥ 7 richtig |
| ★☆☆ | sonst |
