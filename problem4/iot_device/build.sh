#!/bin/sh
rm -rf ./bin
#compile and build the project
sbt "test; universal:packageBin"
unzip ./target/universal/hebserver-0.1.zip -d ./bin
