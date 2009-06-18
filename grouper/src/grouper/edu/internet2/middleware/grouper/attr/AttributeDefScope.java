/**
 * 
 */
package edu.internet2.middleware.grouper.attr;


/**
 * clamp down an attribute def to a set of scopes which are like strings in the DB.
 * could be a group/stem name, or 
 * @author mchyzer
 *
 */
public class AttributeDefScope {

  /** id of this scope */
  private String id;

  /** id of the attribute def */
  private String attributeDefId;
  
  /** scope string, either a group or stem name or like string or something */
  private String scopeString;
  
  /** type of scope */
  private AttributeDefScopeType attributeDefScopeType;
  
  /**
   * id of this scope
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * id of this scope
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * scope string, either a group or stem name or like string or something
   * @return scope string
   */
  public String getScopeString() {
    return scopeString;
  }

  /**
   * scope string, either a group or stem name or like string or something
   * @param scopeString1
   */
  public void setScopeString(String scopeString1) {
    this.scopeString = scopeString1;
  }

  /**
   * id of the attribute def
   * @return the id
   */
  public String getAttributeDefId() {
    return attributeDefId;
  }

  /**
   * id of the attribute def
   * @param attributeDefId1
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  /**
   * type of scope
   * @return the type of scope
   */
  public AttributeDefScopeType getAttributeDefScopeType() {
    return this.attributeDefScopeType;
  }

  /**
   * type of scope
   * @param attributeDefScopeType1
   * 
   */
  public void setAttributeDefScopeType(AttributeDefScopeType attributeDefScopeType1) {
    this.attributeDefScopeType = attributeDefScopeType1;
  }
  
  /**
   * type of scope
   * @return the type of scope
   */
  public String getAttributeDefScopeTypeDb() {
    return this.attributeDefScopeType == null ? null : this.attributeDefScopeType.name();
  }

  /**
   * type of scope
   * @param theAttributeDefScopeType1
   * 
   */
  public void setAttributeDefScopeTypeDb(String theAttributeDefScopeType1) {
    this.attributeDefScopeType = AttributeDefScopeType.valueOfIgnoreCase(theAttributeDefScopeType1, false);
  }
  
}
