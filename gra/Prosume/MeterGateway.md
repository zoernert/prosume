# Rolle: MeterGateway

Ein Metergateway definiert einen Messstellenbetreiber, der regelm��ig die Z�hlerst�nde von Messstellen (Consumer oder Prosumer) erh�lt und die eigentliche Bilanzierung zum Verbrauchszeitpunkt vornimmt.


## Ablauf der Bilanzierung

- Das Metergateway empf�ngt von der Zeitquelle den aktuellen Slot (15 Minuten id).
- F�r alle an diesem Meter vorhandenen Messstellen wird eine Bilanz erstellt.
- Die Bilanz besteht aus der gemessenen Einspeisung/Entnahme (produce/consume) sowie der Bilanzierung der Trades.
- Die Bilanzsumme wird ausgeglichen, durch eine entsprechende Ausgleichmengen Buchung.

