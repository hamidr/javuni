
## The ASCII text glutton 

You have been tasked with exposing an API or access point that allows third parties to stream text files to your application.

You can assume these files only contain ASCII characters and are no longer than 10 MBs.

### Task

The task is to process the file and either:
   - Store (either in memory or in database) and provide way to retrieve it;
   - Return immediately the result to client.
 
### Digested text
 
The processed text should be digested into a data type with:
   - Number of individual words;
   - How many occurrences of each word;
   - Text summary(*).

(*) The text summary should be based on some set of rules loaded from some persistence, either a database or a configuration file.
  The rule(s) are completely arbitrary and up to the candidate. Some examples are:
   - Aggressive words; 
   - unknown words;
   - Words that are not part of the english vocabulary or another language;
   - Words that are over certain length.
   
The summary format is completely up to the candidate.

### Expected delivery

Remember that the delivered code should be something you would feel comfortable in deploying to production, 
hence, we will take into consideration:
   - test coverage;
   - excellent code readability;
   - code architecture / style;
   - Performance;
   - Health-check with alive + readiness endpoint.

The delivered solution should be accompanied by a README file with instructions to run tests and the application plus 
any information helpful to understand the solution (i.e. API documentation, rational for choice of technology, others)

The solution should be written in Scala and there is complete freedom to how the candidate approaches the problem. 
You may choose whatever technology you may like but be prepared to explain your choices.

#### Bonus points:

- Pure functional programming solutions, for instance, using type level stack (http4s, fs2, cats, cats-effect, doobie);
- Use known libraries like pure-config, circe. 

It is completely fine to use other technologies or libraries but be prepared to justify your decisions.

### Extra questions:

- Assume the stream can ingest any type of data. How would you handle unexpected cases?

- Assume the stream is infinite. How you handle such case?

- Assume we change from data/file streaming to structured messages, like events, or commands. What should be changed? What technologies should be introduced?

## The ASCII text glutton Part 2:

Our text glutton was delegated with a secret task. Now, only he knows that for each word a specific side effect should be triggered in the system.

A side effect can be considered to be a Task that has some useful work to do. It can be synchronous or asynchronous.

After processing our secret files, we should group by word occurrences, deploy the side effects for each occurrence sequentially (i.e. 10 words go triggers 10 tasks that should be ran one after the other) 
but different side effects can be run in parallel (i.e. go side-effects and stop side-effects can be ran in parallel)

How would you design such a system? 

### Extra questions:

- Now you are required to be able to replay side effects. How would you enable your application to do that?

- What happens if the application suddenly shuts down? What could be done to recover? 
