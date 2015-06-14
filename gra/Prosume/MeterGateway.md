# Rolle: MeterGateway

Ein Metergateway definiert einen Messstellenbetreiber, der regelmäßig die Zählerstände von Messstellen (Consumer oder Prosumer) erhält und die eigentliche Bilanzierung zum Verbrauchszeitpunkt vornimmt.


## Ablauf der Bilanzierung

- Das Metergateway empfängt von der Zeitquelle den aktuellen Slot (15 Minuten id).
- Für alle an diesem Meter vorhandenen Messstellen wird eine Bilanz erstellt.
- Die Bilanz besteht aus der gemessenen Einspeisung/Entnahme (produce/consume) sowie der Bilanzierung der Trades.
- Die Bilanzsumme wird ausgeglichen, durch eine entsprechende Ausgleichmengen Buchung.

