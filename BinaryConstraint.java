import java.util.*;

public final class BinaryConstraint {
  private int firstVar, secondVar;
  private ArrayList<BinaryTuple> tuples;

  public BinaryConstraint(int fv, int sv, ArrayList<BinaryTuple> t) {
    firstVar = fv;
    secondVar = sv;
    tuples = t;
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("c(" + firstVar + ", " + secondVar + ")\n");
    for (BinaryTuple bt : tuples)
      result.append(bt + "\n");
    return result.toString();
  }

  // SUGGESTION: You will want to add methods here to reason about the constraint

  public int getFirst() {
    return firstVar;
  }

  public int getSecond() {
    return secondVar;
  }

  public ArrayList<BinaryTuple> getTuples() {
    return tuples;
  }

  public boolean isValidTuple(int v1, int v2) {
    for (int i = 0; i < tuples.size(); i++) {
      if (tuples.get(i).matches(v1, v2)) {
        return true;
      }
    }
    return false;
  }

}
