#!/bin/sh

sudo apt install -y curl javacc unzip

# install coursier
curl -fLo cs https://git.io/coursier-cli-"$(uname | tr LD ld)"
chmod +x cs

#install JVM 14
eval "$(cs java --jvm 14 --env)"
export PATH="/home/$USER/.local/share/coursier/bin:$PATH"

#install sbt
./cs install sbt

#compile and build the project
sbt universal:packageBin
unzip ./target/universal/iot_device-0.1.zip -d ./bin
