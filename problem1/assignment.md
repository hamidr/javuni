# NewMotion programming assignment

## First things first

If you encounter errors or unclarities about the requirements, please contact us!

### Technology and language

You are free to use any technology and **JVM** language you prefer, provided that we can have your solution running in a shell in a matter of minutes. We work all the time with Scala so we would of course appreciate it if you'd choose it (and, consequently some library from our stack like Spray, Akka HTTP, ...) for your assignment. That said, what we eventually want out of this assignment is a good understanding of your programming skills so we leave it up to you: pick Scala if you think you can write code you are proud of, pick something else otherwise. This will not influence negatively our evaluation!

### Let's keep things simple

For the sake of this assignment, storing data in-memory using plain collections is totally fine. Also, no UI is required.

### A note about datetimes

All datetime strings described in the assignment are to be represented in the format specified by [RFC 3339](https://tools.ietf.org/html/rfc3339). So both of these example datetime strings are valid:
- "2017-04-12T23:20:50.52Z"
- "2018-01-19T16:39:57-08:00"

### Submitting your solution

When you submit your finished assignment, please include any relevant implementation-dependent information that we will need to use your application, for example:

- how we can run it
- what endpoints your applications exposes

Please send us your finished assignment by e-mail.

Good luck, we look forward to receiving your solution!

## The assignment

VenerableInertia (TVI) is a company that makes money from charging sessions of electric vehicles on their charge points. In this assignment, you will implement a simple back-office system for them that computes how much money electric drivers owe TVI for their charging sessions.

As a TVI bookkeeper I want the back-office system to compute session fees.

A session is described by a start datetime, an end datetime and the electricity volume consumed (expressed in kilowatt-hours).

### Story 1: Set tariff

A customer should of course pay for his charging sessions, to pay for the electricity and to compensate VenerableInertia for their work. The session tariff consists of three components: a start fee to pay for VenerableInertia's processing, a time-based fee to pay for the occupation of the parking spot and a kWh fee for the energy consumed.

The bookkeeper want to set the tariff by sending a POST request to the back-office with a JSON message like this:

```
{
  "currency": "EUR",
  "startFee": 0.20,
  "hourlyFee": 1.00,
  "feePerKWh": 0.25,
  "activeStarting": "2017-01-01T00:00:00.000Z"
}
```

The fields have the following meaning:

|Field name|Type|Description|Example|
|--- |--- |--- |--- |
|currency|string, ISO-4217 currency code|Currency in which all fees are expressed|"EUR"|
|startFee|(optional) number|A one-time fee that applies to every session|1.00|
|hourlyFee|(optional) number|A fee for the time that the car occupied the charger|0.20|
|feePerKWh|(optional) number|A fee for the energy that the car consumed|0.25|
|activeStarting|datetime string|The moment in time from which this tariff is active|"2017-01-01T00:00:00.000Z"|


A tariff is considered invalid (and should be rejected) if all of its `fee` components are empty (= not defined) at the same time.

Your application should listen for POSTs of this format on a certain URL. Your back-office system should check that when a tariff message is POSTed, the `activeStarting` value is later than the current time, because we cannot retroactively change the charging tariff. For the same reason, the `activeStarting` value should also be later than the latest `activeStarting` value in a previous tariff message.

### Story 2: Receive and store sessions

As a VenerableInertia bookkeeper, I want to receive and provide information about customers' sessions so that I can eventually invoice them.

The charge points speak a very simple charge point communication protocol known as Simple Charge Point Protocol (SCPP). In SCPP, there is only one kind of message: the Session message. A Session message is a piece of data in the [JSON](http://json.org/) format. The SCPP message is a JSON object with at least the following four fields:

|Field name|Type|Description|Example value|
|--- |--- |--- |--- |
|customerId|string|Name of the customer who charged his car in this session|"pete"|
|startTime|datetime string|When the session started|"2017-01-27T13:32:14+02:00"|
|endTime|datetime string|When the session ended|"2017-01-27T14:32:14+02:00"|
|volume|number|The amount of energy consumed, in kWh|13.21|

An example of a full SCPP message would thus be:

```
{
  "customerId": "john",
  "startTime": "2017-01-28T09:34:17Z",
  "endTime": "2017-01-28T16:45:13Z",
  "volume": 32.03
}
  
```

The SCPP messages will be delivered to the backoffice with HTTP POST requests to an endpoint URL that you are free to choose. 

### Story 3: Compute session fees and retrieve sessions

The bookkeeper would like to get an overview of each customer's sessions in a textual format (CSV or JSON): the bookeeper would also like to know how much a customer should pay for each of his/her sessions (= his/her session fees). To compute the total fee of a session, you calculate and add up the three parts of the applicable tariff: the hourly fee multiplied by the session duration in hours, the fee per kWh multiplied by the consumed energy in kWh, and the start fee. The applicable tariff is the tariff that was in effect before the session started.
