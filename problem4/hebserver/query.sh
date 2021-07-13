curl --location --request GET 'http://0:8080/devices/stats' \
--header 'Content-Type: application/json' \
--data-raw '{
    "from":"2020-06-11T17:07:49.907676+02:00[Europe/Amsterdam]",
    "to":"2022-08-11T17:08:49.907676+02:00[Europe/Amsterdam]",
    "sensors":["carFuel", "thermostat", "heartRate"],
    "operations":["MAX", "MIN", "AVERAGE", "Median"]
}'
