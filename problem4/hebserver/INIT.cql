create KEYSPACE iot_server WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
use iot_server;
create TABLE records ( id text , thermostat DOUBLE, heartRate DOUBLE, carFuel DOUBLE, time TIMESTAMP, PRIMARY KEY(id, time));
