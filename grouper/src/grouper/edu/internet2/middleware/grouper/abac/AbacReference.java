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
  private boolean memberOfAny = false;

  /**
   * For ATTRIBUTE refs: every value the field is checked against (empty for a bare presence
   * check). Also used for GROUP refs with memberOfAny to carry the full list of group names so
   * the terse renderer can list them (and cap with ", etc" in non-leaf summaries).
   */
  private List<String> attributeValues = new ArrayList<String>();

  /** for ATTRIBUTE refs: true when the check is "field is null / empty" */
  private boolean attributeNullCheck = false;

  /**
   * true when the condition uses an operator the terse visualization renderer does not
   * special-case (between / like / regex / comparison); the renderer then falls back to
   * the verbose displayDescription for this reference.
   */
  private boolean terseUnsupported = false;

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

  public void setNegated(boolean negated) {
    this.negated = negated;
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

  public boolean isMemberOfAny() {
    return memberOfAny;
  }

  public void setMemberOfAny(boolean memberOfAny) {
    this.memberOfAny = memberOfAny;
  }

  public List<String> getAttributeValues() {
    return attributeValues;
  }

  public void setAttributeValues(List<String> attributeValues) {
    this.attributeValues = attributeValues;
  }

  public boolean isAttributeNullCheck() {
    return attributeNullCheck;
  }

  public void setAttributeNullCheck(boolean attributeNullCheck) {
    this.attributeNullCheck = attributeNullCheck;
  }

  public boolean isTerseUnsupported() {
    return terseUnsupported;
  }

  public void setTerseUnsupported(boolean terseUnsupported) {
    this.terseUnsupported = terseUnsupported;
  }

  /**
   * For a ROW ref: true when the inner predicate's top-level connective is OR, so the
   * terse row renderer joins column siblings with " or " instead of " and ".
   */
  private boolean rowInnerOr;

  public boolean isRowInnerOr() {
    return rowInnerOr;
  }

  public void setRowInnerOr(boolean rowInnerOr) {
    this.rowInnerOr = rowInnerOr;
  }

  /**
   * Returns a unique identifier for this reference, suitable for use as a node ID.
   * The negated flag is folded into the prefix so a positive and a negated reference with
   * the same description (e.g. positive "affiliationActive" and the post-strip rendering of
   * "!affiliationActive" elsewhere in the same script) do not collapse to the same graph node.
   */
  public String computeId() {
    String neg = negated ? "neg:" : "";
    switch (refType) {
      case GROUP:
        if (displayDescription != null) {
          return "abac_group_ref:" + neg + displayDescription;
        }
        return "abac_group_ref:" + neg + name + (value != null ? ":" + value : "");
      case ATTRIBUTE:
        if (displayDescription != null) {
          return "data_attr:" + neg + displayDescription;
        }
        return "data_attr:" + neg + name + (value != null ? ":" + value : "");
      case ROW:
        if (displayDescription != null) {
          return "data_row:" + neg + displayDescription;
        }
        return "data_row:" + neg + name + (value != null ? ":" + value : "");
      case COMPOUND:
        StringBuilder sb = new StringBuilder("compound_" + name + ":" + neg);
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
        return "abac_unknown:" + neg + name;
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
        return name;
      default:
        return name;
    }
  }
}
