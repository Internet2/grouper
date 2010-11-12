package edu.internet2.middleware.grouper.pit;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class PITAttributeAssignValue extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_ATTRIBUTE_ASSIGN_ID = "attribute_assign_id";
  
  /** column */
  public static final String COLUMN_VALUE_STRING = "value_string";
  
  /** column */
  public static final String COLUMN_VALUE_FLOATING = "value_floating";
  
  /** column */
  public static final String COLUMN_VALUE_INTEGER = "value_integer";
  
  /** column */
  public static final String COLUMN_VALUE_MEMBER_ID = "value_member_id";

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeAssignId */
  public static final String FIELD_ATTRIBUTE_ASSIGN_ID = "attributeAssignId";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: valueFloating */
  public static final String FIELD_VALUE_FLOATING = "valueFloating";

  /** constant for field name for: valueInteger */
  public static final String FIELD_VALUE_INTEGER = "valueInteger";

  /** constant for field name for: valueMemberId */
  public static final String FIELD_VALUE_MEMBER_ID = "valueMemberId";

  /** constant for field name for: valueString */
  public static final String FIELD_VALUE_STRING = "valueString";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_ATTRIBUTE_ASSIGN_ID, FIELD_CONTEXT_ID, FIELD_END_TIME_DB, 
      FIELD_ID, FIELD_START_TIME_DB, FIELD_VALUE_FLOATING, FIELD_VALUE_INTEGER, 
      FIELD_VALUE_MEMBER_ID, FIELD_VALUE_STRING);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_ATTRIBUTE_ASSIGN_ID, FIELD_CONTEXT_ID, FIELD_END_TIME_DB, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_START_TIME_DB, FIELD_VALUE_FLOATING, 
      FIELD_VALUE_INTEGER, FIELD_VALUE_MEMBER_ID, FIELD_VALUE_STRING);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  
  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_ATTRIBUTE_ASSIGN_VALUE = "grouper_pit_attr_assn_value";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /** attribute assignment in this value assignment */
  private String attributeAssignId;

  /** string value */
  private String valueString;

  /** floating point value */
  private Double valueFloating;

  /** integer value */
  private Long valueInteger;
  
  /** member id value */
  private String valueMemberId;

  
  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  
  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   * @return the contextId
   */
  public String getContextId() {
    return contextId;
  }

  
  /**
   * @param contextId the contextId to set
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  
  /**
   * @return the attributeAssignId
   */
  public String getAttributeAssignId() {
    return attributeAssignId;
  }

  
  /**
   * @param attributeAssignId the attributeAssignId to set
   */
  public void setAttributeAssignId(String attributeAssignId) {
    this.attributeAssignId = attributeAssignId;
  }

  
  /**
   * @return the valueString
   */
  public String getValueString() {
    return valueString;
  }

  
  /**
   * @param valueString the valueString to set
   */
  public void setValueString(String valueString) {
    this.valueString = valueString;
  }

  
  /**
   * @return the valueFloating
   */
  public Double getValueFloating() {
    return valueFloating;
  }

  
  /**
   * @param valueFloating the valueFloating to set
   */
  public void setValueFloating(Double valueFloating) {
    this.valueFloating = valueFloating;
  }

  
  /**
   * @return the valueInteger
   */
  public Long getValueInteger() {
    return valueInteger;
  }

  
  /**
   * @param valueInteger the valueInteger to set
   */
  public void setValueInteger(Long valueInteger) {
    this.valueInteger = valueInteger;
  }

  
  /**
   * @return the valueMemberId
   */
  public String getValueMemberId() {
    return valueMemberId;
  }

  
  /**
   * @param valueMemberId the valueMemberId to set
   */
  public void setValueMemberId(String valueMemberId) {
    this.valueMemberId = valueMemberId;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * save this object
   */
  public void save() {
    GrouperDAOFactory.getFactory().getPITAttributeAssignValue().saveOrUpdate(this);
  }
  
  /**
   * update this object
   */
  public void update() {
    GrouperDAOFactory.getFactory().getPITAttributeAssignValue().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITAttributeAssignValue().delete(this);
  }
}