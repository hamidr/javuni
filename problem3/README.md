### Run it:

requirements: sbt

command: `sbt run`

config file: src/main/resources/application.conf


```
ipAddress = 0.0.0.0 #IP you want this to serve on
port = 8080 # PORT you want this app to serve on
dir = "./" # A directory (virtual or a filesystem based) for saving stream texts on it
filteringRules = ["aggressive_word"] # Available filters are ["aggressive_word", "unknown_word"]
#aggressive_word filter will filter out some specified words
#unknown_word filter will filter out some words that are only know to its database
readChunkSize = 1024 #The portion of stream to be read chunk by chunk for providing information on the saved stream texts
processChunkSize = 1024 #The portion for processing the words stream since this service will be infinite at some point and RAMs are not infinite
```

### APIs:

/stream - Example:

```
#where file name is "text.txt"
curl -X POST http://127.0.0.1:8080/stream -F stream-file=@./text.txt
```

/stream/{file-name} - Where file-name is mandatory to provide is it plays the role of a key in order to find the stream; Example:

```
#where file name is "text.txt"
curl -X GET http://127.0.0.1:8080/stream/text.txt
```

