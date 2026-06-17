# ⚔️ MathQuest – Der Kampf der Zahlen

Ein Java-Lernspiel für das kleine Einmaleins. Löse Rechenaufgaben im Kampf gegen Monster – je schneller und genauer, desto besser dein Score.

---

## 🎮 Spielprinzip

Du kämpfst als Held gegen 10 verschiedene Monster. Für jede richtige Antwort greifst du an – bei einer falschen schlägt der Gegner zurück. Am Ende siehst du deine Durchschnittszeit, deine Trefferquote und eine Sternewertung.

---

## 🧩 Schwierigkeiten

Vor dem Spielstart kannst du eine von drei Stufen wählen:

| Stufe      | Faktoren   | Beispiel     |
|------------|------------|--------------|
| **Leicht** | 1 – 5      | 3 × 4 = ?    |
| **Mittel** | 1 – 10     | 7 × 8 = ?    |
| **Schwer** | 7 – 15     | 12 × 14 = ?  |

---

## 🏆 Highscores

Nach jedem Spiel kannst du deinen Namen eintragen. Das Spiel speichert die Top 10 nach Durchschnittszeit (weniger = besser). Bei Gleichstand gewinnt, wer mehr Aufgaben richtig hatte.

---

## 🛠️ Voraussetzungen

- **BlueJ** (empfohlen) oder ein beliebiges Java-IDE



## 🚀 Starten

### Mit BlueJ

1. ZIP entpacken
2. `MathQuest/` in BlueJ öffnen (`package.bluej` doppelklicken)
3. **Compile** (Strg+Shift+C) – einmalig nötig, da keine `.class`-Dateien enthalten sind
4. Rechtsklick auf `MathQuest` → `void main(String[] args)` → OK



## 📁 Projektstruktur

```
MathQuest_new/
├── MathQuest.java          # Einstiegspunkt (main)
├── StartScreen.java        # Hauptmenü mit Schwierigkeitsauswahl
├── GameScreen.java         # Spielbildschirm (Kampf, Animationen)
├── ResultScreen.java       # Ergebnisanzeige nach 10 Aufgaben
├── HighscoreScreen.java    # Highscore-Tabelle
├── GameData.java           # Schwierigkeiten, Scores, Datei-I/O
└── package.bluej           # BlueJ-Projektdatei
```

---

## 💾 Highscore-Datei

Scores werden als `highscores.dat` im Arbeitsverzeichnis gespeichert (Java-Serialisierung). Die Datei wird automatisch angelegt. Zum Zurücksetzen einfach löschen.

---

## 🔧 Änderungen gegenüber der Ursprungsversion

- Drei wählbare Schwierigkeitsstufen (Leicht / Mittel / Schwer) im Startmenü
- Schwierigkeit wird im Spielbildschirm als Badge angezeigt
- Farbkodierung je Schwierigkeit (grün / gelb / rot)
- Alte `.class`-Dateien entfernt – sauberes Projekt für BlueJ

---

*Erstellt mit BlueJ · Java Swing · keine externen Abhängigkeiten*
