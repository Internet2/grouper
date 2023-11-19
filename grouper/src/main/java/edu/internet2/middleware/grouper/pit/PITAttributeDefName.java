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
public class PITAttributeDefName extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";
  
  /** column */
  public static final String COLUMN_NAME = "name";
  
  /** column */
  public static final String COLUMN_STEM_ID = "stem_id";
  
  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_ID = "attribute_def_id";
  
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

  /** constant for field name for: attributeDefId */
  public static final String FIELD_ATTRIBUTE_DEF_ID = "attributeDefId";
  
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_ACTIVE_DB, FIELD_START_TIME_DB, FIELD_END_TIME_DB,
      FIELD_NAME, FIELD_STEM_ID, FIELD_ATTRIBUTE_DEF_ID, FIELD_SOURCE_ID);



  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_ATTRIBUTE_DEF_NAME = "grouper_pit_attr_def_name";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /** id of this attribute def  */
  private String attributeDefId;

  /** stem that this attribute is in */
  private String stemId;

  /** name of attribute, e.g. school:community:students:expireDate */
  private String name;
  
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
   * @return stemId
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
    GrouperDAOFactory.getFactory().getPITAttributeDefName().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITAttributeDefName().delete(this);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    if (this.isActive()) {
      throw new RuntimeException("Cannot delete active point in time attribute def name object with id=" + this.getId());
    }
    
    // delete attribute assignments
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findByPITAttributeDefNameId(this.getId());
    for (PITAttributeAssign assignment : assignments) {
      GrouperDAOFactory.getFactory().getPITAttributeAssign().delete(assignment);
    }
    
    // delete self attribute def name sets and their children
    GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().deleteSelfByPITAttributeDefNameId(this.getId());
    
    // delete attribute def name sets by thenHasAttributeDefNameId ... and their children.
    Set<PITAttributeDefNameSet> attributeDefNameSets = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findByThenHasPITAttributeDefNameId(this.getId());
    for (PITAttributeDefNameSet attributeDefNameSet : attributeDefNameSets) {
      GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().delete(attributeDefNameSet);
    }
  }
  
  private PITStem pitStem;
  private PITAttributeDef pitAttributeDef = null;

  /**
   * @return pitAttributeDef
   */
  public PITAttributeDef getPITAttributeDef() {
    if (pitAttributeDef == null) {
      pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(attributeDefId, true);
    }
    
    return pitAttributeDef;
  }
  
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
