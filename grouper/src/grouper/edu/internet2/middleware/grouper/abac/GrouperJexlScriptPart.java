package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperJexlScriptPart {

  /**
   * 
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this, null);
  }

  /**
   * 
   */
  @Override
  protected GrouperJexlScriptPart clone() {
    GrouperJexlScriptPart clone = new GrouperJexlScriptPart();
    for (MultiKey argument : this.arguments) {
      clone.arguments.add(new MultiKey(argument.getKeys()));
    }
    clone.displayDescription.append(this.displayDescription);
    clone.whereClause.append(this.whereClause);
    return clone;
  }

  public GrouperJexlScriptPart() {
  }

  /**
   * arguments for SQL (bind vars)
   */
  private List<MultiKey> arguments = new ArrayList<MultiKey>();

  /**
   * arguments for SQL (bind vars)
   * @return
   */
  public List<MultiKey> getArguments() {
    return arguments;
  }

  /**
   * arguments for SQL (bind vars)
   * @param arguments
   */
  public void setArguments(List<MultiKey> arguments) {
    this.arguments = arguments;
  }

  /**
   * where clause for SQL
   */
  private StringBuilder whereClause = new StringBuilder();

  /**
   * where clause for SQL
   * @return
   */
  public StringBuilder getWhereClause() {
    return whereClause;
  }

  /**
   * where clause for SQL
   * @param whereClause
   */
  public void setWhereClause(StringBuilder whereClause) {
    this.whereClause = whereClause;
  }

  /**
   * description of this scriptlet
   */
  private StringBuilder displayDescription = new StringBuilder();

  /**
   * description of this scriptlet
   * @return
   */
  public StringBuilder getDisplayDescription() {
    return displayDescription;
  }

  /**
   * description of this scriptlet
   * @param displayDescription
   */
  public void setDisplayDescription(StringBuilder displayDescription) {
    this.displayDescription = displayDescription;
  }

  private int populationCount = -1;
  
  
  public int getPopulationCount() {
    return populationCount;
  }

  
  public void setPopulationCount(int populationCount) {
    this.populationCount = populationCount;
  }

  
  public boolean isContainsSubject() {
    return containsSubject;
  }

  
  public void setContainsSubject(boolean containsSubject) {
    this.containsSubject = containsSubject;
  }

  private boolean containsSubject = false;
  
}
