/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class PITAttributeDef extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** name */
  public static final String COLUMN_NAME = "name";
  
  /** stem */
  public static final String COLUMN_STEM_ID = "stem_id";
  
  /** attributeDefType */
  public static final String COLUMN_ATTRIBUTE_DEF_TYPE = "attribute_def_type";

  /** hibernate version */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";

  /** column */
  public static final String COLUMN_SOURCE_ID = "source_id";
  
  
  /** constant for field name for: sourceId */
  public static final String FIELD_SOURCE_ID = "sourceId";
  
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";
  
  /** constant for field name for: stemId */
  public static final String FIELD_STEM_ID = "stemId";
  
  /** constant for field name for: attributeDefType */
  public static final String FIELD_ATTRIBUTE_DEF_TYPE = "attributeDefType";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_NAME, FIELD_ATTRIBUTE_DEF_TYPE, FIELD_STEM_ID, FIELD_SOURCE_ID);



  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_ATTRIBUTE_DEF = "grouper_pit_attribute_def";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /** name */
  private String name;
  
  /** stem */
  private String stemId;
  
  /** attributeDefType */
  private String attributeDefType;

  /** sourceId */
  private String sourceId;
  
  /**
   * @return source id
   */
  public String getSourceId() {
    return sourceId;
  }

  /**
   * set source id
   * @param sourceId
   */
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }
  
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
   * Set name
   * @param name
   */
  public void setNameDb(String name) {
    this.name = name;
  }
  
  /**
   * @return stem id
   */
  public String getStemId() {
    return stemId;
  }
  
  /**
   * @param stemId
   */
  public void setStemId(String stemId) {
    this.stemId = stemId;
  }
  
  /**
   * @return attributeDefType
   */
  public String getAttributeDefTypeDb() {
    return this.attributeDefType;
  }
  
  /**
   * @param attributeDefType
   */
  public void setAttributeDefTypeDb(String attributeDefType) {
    this.attributeDefType = attributeDefType;
  }

  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getPITAttributeDef().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITAttributeDef().delete(this);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    if (this.isActive()) {
      throw new RuntimeException("Cannot delete active point in time attribute def object with id=" + this.getId());
    }
    
    // delete memberships
    Set<PITMembership> memberships = GrouperDAOFactory.getFactory().getPITMembership().findAllByPITOwner(this.getId());
    for (PITMembership membership : memberships) {
      GrouperDAOFactory.getFactory().getPITMembership().delete(membership);
    }
    
    // delete attribute assignments
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findByOwnerPITAttributeDefId(this.getId());
    for (PITAttributeAssign assignment : assignments) {
      GrouperDAOFactory.getFactory().getPITAttributeAssign().delete(assignment);
    }
    
    // delete self group sets and their children
    GrouperDAOFactory.getFactory().getPITGroupSet().deleteSelfByPITOwnerId(this.getId());
    
    // delete attribute def names
    Set<PITAttributeDefName> attrs = GrouperDAOFactory.getFactory().getPITAttributeDefName().findByPITAttributeDefId(this.getId());
    for (PITAttributeDefName attr : attrs) {
      GrouperDAOFactory.getFactory().getPITAttributeDefName().delete(attr);
    }
    
    // delete actions
    Set<PITAttributeAssignAction> actions = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findByPITAttributeDefId(this.getId());
    for (PITAttributeAssignAction action : actions) {
      GrouperDAOFactory.getFactory().getPITAttributeAssignAction().delete(action);
    }
  }
  
  private PITStem pitStem;
  
  /**
   * @return pitStem
   */
  public PITStem getPITStem() {
    if (pitStem == null) {
      pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(stemId, true);
    }
    
    return pitStem;
  }
}
