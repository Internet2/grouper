package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperJexlScriptPart {

  /**
   * Role of this part in the tree of script parts. LEAF parts are method calls
   * (memberOf / hasRow / hasAttribute / ...). AND and OR parts are compounds whose
   * children are the other parts that name them as parentPart.
   */
  public enum Connective { LEAF, AND, OR }


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

  /**
   * whether this part represents a negated (NOT) condition
   */
  private boolean negated = false;

  public boolean isNegated() {
    return negated;
  }

  public void setNegated(boolean negated) {
    this.negated = negated;
  }

  /**
   * Connective for this part. Default LEAF means this is a terminal method-call part.
   * Set to AND or OR when the analyzer encounters an ASTAndNode / ASTOrNode while this
   * part is the accumulator — i.e. when this part's combined description is being built
   * from AND'd or OR'd children.
   */
  private Connective connective = Connective.LEAF;

  public Connective getConnective() {
    return connective;
  }

  public void setConnective(Connective connective) {
    this.connective = connective;
  }

  /**
   * The part that owns this one in the analysis tree. Null for the root part.
   * Computed in a post-analysis pass that walks each part's AST node upward in the
   * combined outer + per-hasRow inner ASTs.
   */
  private GrouperJexlScriptPart parentPart;

  public GrouperJexlScriptPart getParentPart() {
    return parentPart;
  }

  public void setParentPart(GrouperJexlScriptPart parentPart) {
    this.parentPart = parentPart;
  }

}
