

# Introduction

Following WebService, uses "FS2, Http4s, cat-effect" in FP style utilizing Onion Architecture to provide
two endpoints for consuming IoT Logs and providing stats on these consumed ones.

Computation is happening inside the application and not the database just because I wanted to show that 
I can do Streaming processing.
It's obvious the performance wise, it would have been much better to compute the stats in the database 
and avoid the network I/O.

Two endpoints:
1. `POST /devices'
with following payload sample as an example, a unique ID of that device and state of sensors with Double value
```
{
"id":"765ba96c-706c-4b27-9cad-dbf32058bc0f",
"thermostat": 1.2,
"carFuel": 2.3,
"heartRate": 2.2
}
```

1. `GET /devices/stats'
with following payload sample, however unconventional as a GET request.
With `from` and `to` as a date range to compute, and sensors to compute and operations to ask for.
```
{
    "from":"2020-06-11T17:07:49.907676+02:00[Europe/Amsterdam]",
    "to":"2022-08-11T17:08:49.907676+02:00[Europe/Amsterdam]",
    "sensors":["carFuel", "thermostat", "heartRate"],
    "operations":["MAX", "MIN", "AVERAGE", "Median"]
}'

```


# Install tools
There is a file called `install.sh` to install the necessary tools.
```
bash ./install.sh
```

# Test/Build
```
There is a file called `build.sh` to test and build the universally packaged APP and unzip it for use.
```

# Database choice
For the sake of testing application there are databases used in the APP,
1. In memory set (threadsafe); related config is called "inmemory.conf".
2. Cassandra; related config is called "with_cassandra.conf".

# Cassandra
Make sure you have Cassandra running if you want to use it as a database with its PORTs.
And be sure it has warmed up and can accept connections.

```sudo docker run -p 7199:7199 -p 9042:9042 cassandra:4.0```

## Run it with Cassandra
This will run the application and then execute necessary CQL commands to create keyspace and table.
```
bash run_with_cassandra.sh
```

## Run it without database
```
bash run.sh
```
