package algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Standardizes an expression according to some specific rules described as below.
 * 
 * The multiplication is non-commutative, but associative. The addition is commutative.
 * XY + YX = Z
 * YZ + ZY = X
 * XZ + ZX = Y
 * 
 * An expression is standard if it is the sum of some standard terms.
 * A term is standard if it is written as X^iY^jZ^k.
 * 
 * For example,
 * XY-YX = 2XY-Z
 * YZX = Y(-XZ+Y) = -(-XY+Z)Z+Y^2 = XYZ+Y^2-Z^2
 * XYXY = X(-XY+Z)Y = -X^2Y^2-XYZ+X^2
 * The RHS expressions are standard.
 */
public class ExpressionStandardizer {
  private static final int STANDARD = -1;
  public static final Map<String, Expression> ELEMENTARY_STANDARD_EXPRESSION = initElementaryExpressions();
  
  private static Map<String, Expression> initElementaryExpressions(){
    Map<String, Expression> result = new HashMap<String, Expression>();
    result.put("YX", new Expression().addTerm("XY", -1).addTerm("Z"));
    result.put("ZY", new Expression().addTerm("YZ", -1).addTerm("X"));
    result.put("ZX", new Expression().addTerm("XZ", -1).addTerm("Y"));
    return result;
  }
  
  public static String preprocess(String input) {
    String term = "";
    if (Character.isLetterOrDigit(input.charAt(0))) {
      term += '+';
    }
    input = input.toUpperCase();
    for (int i = 0; i < input.length(); i++) {
      if (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '+' || input.charAt(i) == '-') {
        term += input.charAt(i);
      }
    }
    return term;
  }
  
  private static Collection<String> split(String expressionStr) {
    Collection<String> result = new ArrayList<>();
    int iCurrent = 0;
    for (int i = 1; i < expressionStr.length(); i++) {
      if (expressionStr.charAt(i) == '+' || expressionStr.charAt(i) == '-') {
        result.add(expressionStr.substring(iCurrent, i));
        iCurrent = i;
      }
    }
    result.add(expressionStr.substring(iCurrent));
    return result;
  }
  
  private static ReadData read(String term, int index) {
    char ch = term.charAt(index);
    int occ = 0;
    index++;
    while (index < term.length() && Character.isDigit(term.charAt(index))) {
      occ = occ * 10 + (term.charAt(index) - '0');
      index++;
    }
    if (occ == 0) {
      occ = 1;
    }
    return new ReadData(ch, occ, index);
  }
  
  /*
   * Returns the index of the first letter that makes the one-term expression non standard.
   * If the one-term expression is already standard, returns STANDARD (-1).
   */
  private static int firstIdNonStandard(String term){
    int iCurrent = 0;
    for (int i = 1; i < term.length(); i++) {
      if (Character.isLetter(term.charAt(i))) {
        if (term.charAt(i) <= term.charAt(iCurrent)) {
          return iCurrent;
        }
        iCurrent = i;
      }
    }
    return STANDARD;
  }
  
  private static String compactize(String term) {
    String result = "";
    int i = 0;
    char charLast = 0;
    int occLast = 0;
    while (i < term.length()) {
      ReadData data = read(term, i);
      char charCurrent = data.ch;
      int occCurrent = data.occ;
      i = data.nextIndex;
      if (charCurrent == charLast) {
        occLast += occCurrent;
      } else {
        if (charLast != 0) {
          result += charLast + (occLast == 1 ? "" : Integer.toString(occLast));
        }
        charLast = charCurrent;
        occLast = occCurrent;
      }
    }
    result += charLast + (occLast == 1 ? "" : Integer.toString(occLast));
    return result;
  }
  
  private static Expression reduceOneStep(String term, int firstId) {
    // Parse to find the first char, first occurrence, second char, second occurrence. 
    ReadData data = read(term, firstId);
    char firstChar = data.ch;
    int firstOcc = data.occ;
    int secondId = data.nextIndex;
    data = read(term, secondId);
    char secondChar = data.ch;
    int secondOcc = data.occ;
    
    String front = term.substring(0, firstId);
    String end = term.substring(data.nextIndex);
    
    // Distribute an elementary term.
    firstOcc--;
    secondOcc--;
    String elementaryTerm = firstChar + "" + secondChar;
    Expression elementaryExp = ELEMENTARY_STANDARD_EXPRESSION.get(elementaryTerm);
    Expression result = new Expression();
    for (Entry<String, Integer> entry : elementaryExp.getEntrySet()) {
      String newTerm = "";
      newTerm += front;
      newTerm += firstOcc > 0 ? firstChar : "";
      newTerm += firstOcc > 1 ? firstOcc : "";
      newTerm += entry.getKey();
      newTerm += secondOcc > 0 ? secondChar : "";
      newTerm += secondOcc > 1 ? secondOcc : "";
      newTerm += end;
      result.addTerm(compactize(newTerm), entry.getValue());
    }

    return result;
  }
  
  public static Expression stringToExpression(String expressionStr) {
    expressionStr = preprocess(expressionStr);
    Collection<String> termSet = split(expressionStr);
    Expression exp = new Expression();
    for (String term : termSet) {
      ReadData data = read(term, 0);
      int sign = term.charAt(0) == '+' ? 1 : -1;
      int occ = data.occ * sign;
      
      String newTerm = term.substring(data.nextIndex);
      exp.addTerm(compactize(newTerm), occ);
    }
    return exp;
  }
  
  public static Expression standardize(Expression exp) {
    Expression result = new Expression();
    while (!exp.getTerms().isEmpty()) {
      Set<String> terms = new HashSet<>(exp.getTerms());
      for (String term : terms) {
        int firstId = firstIdNonStandard(term);
        if (firstId != STANDARD) {
          int times = exp.getOccurrence(term);
          exp.removeTerm(term);
          Expression expTemp = reduceOneStep(term, firstId);
          exp.addExpression(expTemp, times);
        } else {
          result.addTerm(term, exp.getOccurrence(term));
          exp.removeTerm(term);
        }
      }
    }
    return result;
  }
  
  public static String standardize(String expressionStr) {
    Expression standardExpression = standardize(stringToExpression(expressionStr));
    return standardExpression.print();
  }
  
  public static void main(String[] args) {
    String input = args[0];
//    String input = "Y^2X^6";
    System.out.println(standardize(input));
  }
  
}

class ReadData {
  char ch;
  int occ;
  int nextIndex;
  public ReadData(char ch, int occ, int nextIndex) {
    this.ch = ch;
    this.occ = occ;
    this.nextIndex = nextIndex;
  }
}
