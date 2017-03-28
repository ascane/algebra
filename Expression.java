package algebra;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Expression {
  private Map<String, Integer> occurrenceByTerm;
  
  public Expression() {
    occurrenceByTerm = new HashMap<>();
  }
  
  public Expression addTerm(String term){
    addTerm(term, 1);
    return this;
  }
  
  public Expression addTerm(String term, int times){
    if (occurrenceByTerm.containsKey(term)){
      occurrenceByTerm.put(term, occurrenceByTerm.get(term) + times);
    } else {
      occurrenceByTerm.put(term, times);
    }
    return this;
  }
  
  public Expression addExpression(Expression that) {
    addExpression(that, 1);
    return this;
  }
  
  public Expression addExpression(Expression that, int times) {
    for (Entry<String, Integer> entry : that.occurrenceByTerm.entrySet()) {
      String term = entry.getKey();
      int occ = entry.getValue();
      if (occurrenceByTerm.containsKey(term)) {
        occurrenceByTerm.put(term, occurrenceByTerm.get(term) + occ * times);
      } else {
        occurrenceByTerm.put(term, occ * times);
      }
    }
    return this;
  }
  
  public void removeTerm(String term) {
    occurrenceByTerm.remove(term);
  }
  
  public Set<String> getTerms() {
    return occurrenceByTerm.keySet();
  }
  
  public Integer getOccurrence(String term) {
    return occurrenceByTerm.get(term);
  }
  
  public Set<Entry<String, Integer>> getEntrySet() {
    return occurrenceByTerm.entrySet();
  }
  
  public String print() {
    String result = "";
    boolean firstTerm = true;
    for (Entry<String, Integer> entry : occurrenceByTerm.entrySet()) {
      String term = entry.getKey();
      int occ = entry.getValue();
      if (occ > 0 && !firstTerm) {
        result += "+";
      }
      result += occ == 1 ? "" : (occ == -1 ? "-" : Integer.toString(occ));
      result += term;
      firstTerm = false;
    }
    result = result.replaceAll("([XYZ])(\\d)", "$1^$2");
    return result;
  }
}
