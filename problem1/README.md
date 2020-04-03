Hello there,


A few notes:
This application doesn't need any database since it would have been hard for you guys to run it even with a simple postgresql.
Tests are quite extensive and I made sure to have a complete coverage and of course it might lack more but since this is a test it doesn't have to be a problem.

It's written in Scala with the help of Akka HTTP. The design is based on a Actor Per Request and it'd have been more fun if I could have used akka-persistence with CQRS.
I tried to keep it simple as possible but it's scala and the reason that I used this language was to show my knowledge and experience around this ecosystem.

First install the SBT.

Then you can run the tests:
`sbt test`

Then you can run the application:
`sbt run`

It will run on your localhost:8080.

The exposed APIs(all are json based on REST protocol):

POST /tariffs (Set a tariff)
POST /sessions (Create a session)
GET  /sessions/$customerId (Get all of customer's Computed Fees without pagination because of in memory database)


