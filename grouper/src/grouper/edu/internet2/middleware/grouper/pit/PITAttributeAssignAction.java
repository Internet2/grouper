package edu.internet2.middleware.grouper.pit;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class PITAttributeAssignAction extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";
  
  /** column */
  public static final String COLUMN_NAME = "name";
  
  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_ID = "attribute_def_id";
  
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";
  
  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: attributeDefId */
  public static final String FIELD_ATTRIBUTE_DEF_ID = "attributeDefId";
  
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_ACTIVE_DB, FIELD_START_TIME_DB, FIELD_END_TIME_DB,
      FIELD_NAME, FIELD_ATTRIBUTE_DEF_ID);



  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_ATTR_ASSIGN_ACTION = "grouper_pit_attr_assn_actn";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /** id of this attribute def  */
  private String attributeDefId;
  
  /** name */
  private String name;
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * @return context id
   */
  public String getContextId() {
    return contextId;
  }

  /**
   * set context id
   * @param contextId
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  /**
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /**
   * @return attributeDefId
   */
  public String getAttributeDefId() {
    return attributeDefId;
  }

  /**
   * @param attributeDefId
   */
  public void setAttributeDefId(String attributeDefId) {
    this.attributeDefId = attributeDefId;
  }

  /**
   * @return name
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return name
   */
  public String getNameDb() {
    return name;
  }

  /**
   * @param name
   */
  public void setNameDb(String name) {
    this.name = name;
  }

  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getPITAttributeAssignAction().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITAttributeAssignAction().delete(this);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    if (this.isActive()) {
      throw new RuntimeException("Cannot delete active point in time attribute assign action object with id=" + this.getId());
    }
    
    // delete attribute assignments
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findByAttributeAssignActionId(this.getId());
    for (PITAttributeAssign assignment : assignments) {
      GrouperDAOFactory.getFactory().getPITAttributeAssign().delete(assignment);
    }
    
    // delete self action sets and their children
    GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().deleteSelfByAttributeAssignActionId(this.getId());
    
    // delete action sets by thenHasAttributeAssignActionId ... and their children.
    Set<PITAttributeAssignActionSet> actionSets = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findByThenHasAttributeAssignActionId(this.getId());
    for (PITAttributeAssignActionSet actionSet : actionSets) {
      GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().delete(actionSet);
    }
  }
}
