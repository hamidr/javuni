Introduction
===========

This assessment is intended to give insights in your coding and logical thinking skills.

You are also asked to present your results and findings on a laptop. We can provide a laptop if needed. After your presentation we will have a discussion about these results, which considerations did you take into account, the issues you encountered, ... . You can spend as much time on this assignment as you see fit in the given time frame. Please note: there is always room for improvement, so please time box this exercise to a duration you feel comfortable with. Any insights in terms of refactorings or refinements can be explained when you come over to demonstrate.

Please treat this exercise as confidential so that we can reuse it for any future candidates.

If you have any questions at all, do not hesitate to contact us.

**Before starting your assignment, please run `git init && git add . && git commit -am "initial commit"` so that we can track your changes from the base line.**

Good luck and enjoy!


Aggregate Service
====================

Aggregate service is a simple REST service implemented with Spray which provides sum, mean and max functionality for different numeric types.

The service accepts only POST requests with a JSON payload as follows:

Request:
```json
{
  "function": "sum", "values": "12.0, 13.00, 23.42", "valueType": "double"
}
```

Response:
```json
{
  "result": "48.42"
}
```

How to run the server:

- `sbt run` => Starts the server at `localhost:9090`

How to change the default host or port:

- This can be done by changing the default values for the host and port in the `application.conf` file.

Assignment
====================
In the given sbt project the code responsible to start the server, receive messages and send the response back, together with marshalling and unmarshalling of messages is written in Scala and located in the `src.main.scala.aggregateservice` package. The basic implementation of aggregate functions is written in Java and located at `src.main.java.aggregateservice.actions` package. At the moment only the **sum** function is implemented. We would like you to:

1. Convert all Java code to Scala.
1. Implement the remaining aggregate functions for the given types.
1. Add support for `BigDecimal` type.
1. Add the tests you see fit to show the functionality works as intended, also in Scala, for all aggregate functionality.

We encourage you to make changes to the aggregate functionality to improve the quality/readability of the code. We don't expect you to make changes in the server code (except for adding support for the `BigDecimal` type in the `doAggregate` function of `AggregateService.scala`), but feel free to do so if necessary or when you see room for improvement.

