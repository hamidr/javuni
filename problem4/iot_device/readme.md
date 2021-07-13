# Introduction
A IoT Device records generator.
If you run this like
``` 
./iot_device 100 http://localhost:8080/devices 10ms 100s
```
It will simulate `100` devices sending their own State to `http://localhost:8080/devices`, 
with interval of `10ms` for `100s`.


# Install tools
There is a file called `install.sh` to install the necessary tools.
```
bash ./install.sh
```

# Build
There is a file called `build.sh` to test and build the universally packaged APP and unzip it for use.
```
bash ./build.sh
```

# Run it wth
```
./bin/iot_device-0.1/bin/iot_device 100 http://localhost:8080/devices 10ms 100s
```
or
```
bash ./run.sh
```

#
