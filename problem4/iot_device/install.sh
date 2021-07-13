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
