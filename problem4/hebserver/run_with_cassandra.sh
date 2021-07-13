#! /bin/sh

#Make sure Cassandra is installed and running on Port provided with the config in with_cassandra.conf
cat with_cassandra.conf

echo "Initializing the DB just in case"
cat INIT.cql  | cqlsh
./bin/hebserver-0.1/bin/hebserver -Dconfig.file=./with_cassandra.conf
