package aggregateservice.actions;

public class AggregateActionLong implements IAggregateAction {

  @Override
  public Object sum(String values) {
    Long result = null;

    result = Long.valueOf(0);
    String[] numbers = values.split(", ");
    
    for(String n : numbers) {
      result += Long.valueOf(n);
    }

    return result;
  }

  @Override
  public Object mean(String values) {
    return null;
  }
  
  @Override
  public Object max(String values) {
    return null;
  }

}