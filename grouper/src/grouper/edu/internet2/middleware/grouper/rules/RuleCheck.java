package edu.internet2.middleware.grouper.rules;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * e.g.
 * check:
 *  - type: flattenedMembershipChange
 *  - groups: X,Z
 *  
 * the type of check and any params
 * @author mchyzer
 *
 */
public class RuleCheck {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: checkOwnerId */
  public static final String FIELD_CHECK_OWNER_ID = "checkOwnerId";

  /** constant for field name for: checkOwnerName */
  public static final String FIELD_CHECK_OWNER_NAME = "checkOwnerName";

  /** constant for field name for: checkStemScope */
  public static final String FIELD_CHECK_STEM_SCOPE = "checkStemScope";

  /** constant for field name for: checkType */
  public static final String FIELD_CHECK_TYPE = "checkType";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CHECK_OWNER_ID, FIELD_CHECK_OWNER_NAME, FIELD_CHECK_STEM_SCOPE, FIELD_CHECK_TYPE);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CHECK_OWNER_ID, FIELD_CHECK_OWNER_NAME, FIELD_CHECK_STEM_SCOPE, FIELD_CHECK_TYPE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public RuleCheck clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  
  /**
   * 
   */
  public RuleCheck() {
    super();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RuleCheck)) {
      return false;
    }
    RuleCheck ruleCheck = (RuleCheck)obj;
    return new EqualsBuilder()
      .append(this.checkOwnerId, ruleCheck.checkOwnerId)
      .append(this.checkStemScope, ruleCheck.checkStemScope)
      .append(this.checkOwnerName, ruleCheck.checkOwnerName)
      .append(this.checkType, ruleCheck.checkType).isEquals();
      
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.checkOwnerId)
      .append(this.checkOwnerName)
      .append(this.checkStemScope)
      .append(this.checkType).toHashCode();
  }

  /**
   * 
   * @param type
   * @param ownerId
   * @param ownerName
   * @param theCheckStemScope
   */
  public RuleCheck(String type, String ownerId, 
      String ownerName, String theCheckStemScope) {
    super();
    this.checkType = type;
    this.checkStemScope = theCheckStemScope;
    
    //now, if the stem scope is set, then we need the name in the owner, so we can compare...
    if (!StringUtils.isBlank(this.checkStemScope) 
        && !StringUtils.isBlank(ownerId) && StringUtils.isBlank(ownerName)) {
      
      GrouperSession grouperSession = GrouperSession.startRootSession(false);
      Stem stem = StemFinder.findByUuid(grouperSession, ownerId, false);
      if (stem == null) {
        LOG.error("Cant find stem in rule: " + ownerId);
      } else {
        ownerName = stem.getName();
        ownerId = null;
      }
    }
    
    this.checkOwnerId = ownerId;
    this.checkOwnerName = ownerName;
  }

  /** type of check */
  private String checkType;
  
  /** group/stem/etc which fires the rule */
  private String checkOwnerId;
  
  /** group/stem/etc which fires the rule */
  private String checkOwnerName;
  
  /** ALL or SUB */
  private String checkStemScope;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleCheck.class);
  
  
  /**
   * ALL or SUB
   * @return the checkStemScope
   */
  public String getCheckStemScope() {
    return this.checkStemScope;
  }

  
  /**
   * ALL or SUB
   * @param checkStemScope1 the checkStemScope to set
   */
  public void setCheckStemScope(String checkStemScope1) {
    this.checkStemScope = checkStemScope1;
  }

  /**
   * group/stem/etc which fires the rule
   * @return group/stem/etc which fires the rule
   */
  public String getCheckOwnerName() {
    return this.checkOwnerName;
  }

  /**
   * group/stem/etc which fires the rule
   * @param ownerName1
   */
  public void setCheckOwnerName(String ownerName1) {
    this.checkOwnerName = ownerName1;
  }

  /**
   * type of rule check
   * @return the type
   */
  public String getCheckType() {
    return this.checkType;
  }

  /**
   * type of rule check
   * @param type1
   */
  public void setCheckType(String type1) {
    //RuleCheckType.valueOfIgnoreCase(type1, false);
    this.checkType = type1;
  }

  /**
   * convert the type to an enum
   * @return rule check type
   */
  public RuleCheckType checkTypeEnum() {
    return RuleCheckType.valueOfIgnoreCase(this.checkType, false);
  }
  
  /**
   * convert the scope to an enum
   * @return rule check scope
   */
  public Stem.Scope stemScopeEnum() {
    return Stem.Scope.valueOfIgnoreCase(this.checkStemScope, false);
  }
  
  /**
   * group which fires the rule
   * @return the group
   */
  public String getCheckOwnerId() {
    return this.checkOwnerId;
  }

  /**
   * group which fires the rule
   * @param group1
   */
  public void setCheckOwnerId(String group1) {
    this.checkOwnerId = group1;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    toStringHelper(result);
    return result.toString();
  }
  
  /**
   * 
   * @param result
   */
  void toStringHelper(StringBuilder result) {
    if (!StringUtils.isBlank(this.checkOwnerId)) {
      result.append("checkOwnerId: ").append(this.checkOwnerId).append(", ");
    }
    if (!StringUtils.isBlank(this.checkOwnerName)) {
      result.append("checkOwnerName: ").append(this.checkOwnerName).append(", ");
    }
    if (!StringUtils.isBlank(this.checkStemScope)) {
      result.append("checkStemScope: ").append(this.checkStemScope).append(", ");
    }
    if (!StringUtils.isBlank(this.checkType)) {
      result.append("checkType: ").append(this.checkType).append(", ");
    }
  }

  /**
   * validate this 
   * @param ruleDefinition
   * @return error or null if ok
   */
  public String validate(RuleDefinition ruleDefinition) {
    if (StringUtils.isBlank(this.checkType)) {
      return "Enter the checkType!";
    }
    try {
      RuleCheckType.valueOfIgnoreCase(this.checkType, true);
    } catch (Exception e) {
      return e.getMessage();
    }
    
    if (!StringUtils.isBlank(this.checkStemScope) 
        && !StringUtils.isBlank(this.checkOwnerId) && StringUtils.isBlank(checkOwnerName)) {
      
      return "If you have a checkStemScope, then you need to provide the checkOwnerName, instead of checkOwnerId: " + this.checkOwnerId;
      
    }
    
    return this.checkTypeEnum().validate(ruleDefinition, this);
  }

  /**
   * add EL variables to the substitute map
   * @param ruleDefinition
   * @param variableMap
   * @param rulesBean 
   * @param hasAccessToElApi
   */
  public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
      RulesBean rulesBean, boolean hasAccessToElApi) {

    if (!StringUtils.isBlank(this.checkOwnerId)) {
      variableMap.put("checkOwnerId", this.checkOwnerId);
    }
    if (!StringUtils.isBlank(this.checkOwnerName)) {
      variableMap.put("checkOwnerName", this.checkOwnerName);
    }
    if (!StringUtils.isBlank(this.checkStemScope)) {
      variableMap.put("checkStemScope", this.checkStemScope);
    }
    RuleCheckType ruleCheckType = this.checkTypeEnum();
    
    if (ruleCheckType != null) {
      ruleCheckType.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }
  }
  
  /**
   * see if the owner is a group (note, owner requiredness not checked)
   * @param ruleDefinition 
   * @return the error message
   */
  public String validateOwnerGroup(RuleDefinition ruleDefinition) {
    return RuleUtils.validateGroup(this.checkOwnerId, this.checkOwnerName, 
        ruleDefinition.getAttributeAssignType().getOwnerGroupId());
  }
  
  /**
   * see if the owner is a stem (note, owner requiredness not checked)
   * @param ruleDefinition
   * @return the error message
   */
  public String validateOwnerStem(RuleDefinition ruleDefinition) {

    //if this is on a stem, then can be blank
    AttributeAssign attributeAssignType = ruleDefinition.getAttributeAssignType();
    if (attributeAssignType != null && !StringUtils.isBlank(attributeAssignType.getOwnerStemId())) {
      if (StringUtils.isBlank(this.checkOwnerId) && StringUtils.isBlank(this.checkOwnerName)) {
        return null;
      }
    }
    return RuleUtils.validateStem(this.checkOwnerId, this.checkOwnerName, attributeAssignType.getOwnerStemId());
  }
  
  /**
   * see if the owner is an attributeDef (note, owner requiredness not checked)
   * @param ruleDefinition
   * @return the error message
   */
  public String validateOwnerAttributeDef(RuleDefinition ruleDefinition) {

    //if this is on a stem, then can be blank
    AttributeAssign attributeAssignType = ruleDefinition.getAttributeAssignType();
    if (attributeAssignType != null && !StringUtils.isBlank(attributeAssignType.getOwnerAttributeDefId())) {
      if (StringUtils.isBlank(this.checkOwnerId) && StringUtils.isBlank(this.checkOwnerName)) {
        return null;
      }
    }
    return RuleUtils.validateAttributeDef(this.checkOwnerId, this.checkOwnerName, attributeAssignType.getOwnerAttributeDefId());
  }
  

}
