package aggregateservice.actions;

public class AggregateActionDouble implements IAggregateAction {

  @Override
  public Object sum(String values) {
    Double result = null;

    result = Double.valueOf(0);
    String[] numbers = values.split(", ");
    
    for(String n : numbers) {
      result += Double.valueOf(n);
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
