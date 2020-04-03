package aggregateservice.actions;

public interface IAggregateAction {
  
  //We expect default return value of zero for sum function
  Object sum(String values);
  
  Object mean(String values);
  
  Object max(String values);

}
