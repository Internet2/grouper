package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reference extracted from an ABAC/JEXL script for visualization purposes.
 * Can be a leaf reference (group, attribute, row) or a compound node grouping
 * OR/AND subexpressions.
 */
public class AbacReference {

  public enum RefType { GROUP, ATTRIBUTE, ROW, COMPOUND }
  public enum Connective { AND, OR }

  private RefType refType;
  private String name;
  private String value;
  private boolean negated;
  private Connective connective;
  private List<AbacReference> children;
  private int populationCount = -1;
  private String displayDescription;
  private boolean containsSubject = false;
  /**
   * Constructor for leaf references (group, attribute, row).
   *
   * @param refType the type of reference (group, attribute, or row)
   * @param name the group name, attribute alias, or row alias
   * @param value for attributes: the value condition (or null); for rows: the filter expression; for groups: null
   * @param negated whether this reference is negated (preceded by NOT in the script)
   * @param connective the parent connective context (AND or OR)
   */
  public AbacReference(RefType refType, String name, String value, boolean negated, Connective connective) {
    this.refType = refType;
    this.name = name;
    this.value = value;
    this.negated = negated;
    this.connective = connective;
  }

  /**
   * Constructor for compound references (OR or AND subexpressions).
   *
   * @param compoundConnective the internal connective of this compound (OR or AND)
   * @param negated whether this compound is negated
   * @param parentConnective how this compound connects to its parent (AND or OR)
   */
  public AbacReference(Connective compoundConnective, boolean negated, Connective parentConnective) {
    this.refType = RefType.COMPOUND;
    this.connective = parentConnective;
    this.negated = negated;
    this.name = compoundConnective == Connective.OR ? "or" : "and";
    this.children = new ArrayList<AbacReference>();
  }

  public RefType getRefType() {
    return refType;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public boolean isNegated() {
    return negated;
  }

  public Connective getConnective() {
    return connective;
  }

  public void setConnective(Connective connective) {
    this.connective = connective;
  }

  public List<AbacReference> getChildren() {
    return children;
  }

  public void addChild(AbacReference child) {
    if (this.children == null) {
      this.children = new ArrayList<AbacReference>();
    }
    this.children.add(child);
  }

  public int getPopulationCount() {
    return populationCount;
  }

  public void setPopulationCount(int populationCount) {
    this.populationCount = populationCount;
  }

  public String getDisplayDescription() {
    return displayDescription;
  }

  public void setDisplayDescription(String displayDescription) {
    this.displayDescription = displayDescription;
  }

  public boolean isContainsSubject() {
    return containsSubject;
  }

  public void setContainsSubject(boolean containsSubject) {
    this.containsSubject = containsSubject;
  }

  /**
   * Returns a unique identifier for this reference, suitable for use as a node ID.
   */
  public String computeId() {
    switch (refType) {
      case GROUP:
        return "abac_group_ref:" + name + (value != null ? ":" + value : "");
      case ATTRIBUTE:
        if (displayDescription != null) {
          return "data_attr:" + displayDescription;
        }
        return "data_attr:" + name + (value != null ? ":" + value : "");
      case ROW:
        if (displayDescription != null) {
          return "data_row:" + displayDescription;
        }
        return "data_row:" + name + (value != null ? ":" + value : "");
      case COMPOUND:
        StringBuilder sb = new StringBuilder("compound_" + name + ":");
        if (children != null) {
          for (int i = 0; i < children.size(); i++) {
            if (i > 0) {
              sb.append("+");
            }
            sb.append(children.get(i).computeId());
          }
        }
        return sb.toString();
      default:
        return "abac_unknown:" + name;
    }
  }

  /**
   * Returns a human-readable display label for this reference.
   */
  public String computeDisplayLabel() {
    if (displayDescription != null) {
      return displayDescription;
    }
    switch (refType) {
      case ATTRIBUTE:
        if (value != null) {
          return name + " = '" + value + "'";
        }
        return name;
      case ROW:
        if (value != null) {
          return name + ": " + value;
        }
        return name;
      case COMPOUND:
        if (children != null) {
          StringBuilder sb = new StringBuilder("(");
          for (int i = 0; i < children.size(); i++) {
            if (i > 0) {
              sb.append(" ").append(name).append(" ");
            }
            sb.append(children.get(i).computeDisplayLabel());
          }
          sb.append(")");
          return sb.toString();
        }
        return name;
      case GROUP:
        // value is set for memberOfAny to hold the combined group names
        if (value != null) {
          return value;
        }
        return name;
      default:
        return name;
    }
  }
}
