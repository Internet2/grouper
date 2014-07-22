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
public class PITStem extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** name */
  public static final String COLUMN_NAME = "name";
  
  /** parent stem */
  public static final String COLUMN_PARENT_STEM_ID = "parent_stem_id";

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
  
  /** constant for field name for: parentStemId */
  public static final String FIELD_PARENT_STEM_ID = "parentStemId";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_NAME, FIELD_PARENT_STEM_ID, FIELD_SOURCE_ID);



  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_STEMS = "grouper_pit_stems";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /** name */
  private String name;
  
  /** parent stem */
  private String parentStemId;

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
   * @return parent stem id
   */
  public String getParentStemId() {
    return parentStemId;
  }
  
  /**
   * @param parentStemId
   */
  public void setParentStemId(String parentStemId) {
    this.parentStemId = parentStemId;
  }

  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getPITStem().saveOrUpdate(this);
  }
  
  /** if we are printing results during delete */
  private static ThreadLocal<Boolean> printOutputOnDelete = new ThreadLocal<Boolean>();

  /**
   * delete this object
   * @param printOutput
   */
  public void delete(boolean printOutput) {
    printOutputOnDelete.set(printOutput);

    try {
      GrouperDAOFactory.getFactory().getPITStem().delete(this);
    } finally {
      printOutputOnDelete.remove();
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {
    super.onPostDelete(hibernateSession);
    
    boolean printOutput = printOutputOnDelete != null && printOutputOnDelete.get() != null && printOutputOnDelete.get() == true ? true : false;

    if (printOutput) {
      System.out.println("Done obliterating stem from point in time: " + this.getName() + ", ID=" + this.getId());
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    boolean printOutput = printOutputOnDelete != null && printOutputOnDelete.get() != null && printOutputOnDelete.get() == true ? true : false;
    
    if (this.isActive()) {
      throw new RuntimeException("Cannot delete active point in time stem object with id=" + this.getId());
    }
    
    if (printOutput) {
      System.out.println("Obliterating stem from point in time: " + this.getName() + ", ID=" + this.getId());
    }
    
    // delete memberships
    Set<PITMembership> memberships = GrouperDAOFactory.getFactory().getPITMembership().findAllByPITOwner(this.getId());
    for (PITMembership membership : memberships) {
      GrouperDAOFactory.getFactory().getPITMembership().delete(membership);
    }
    
    // delete attribute assignments
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findByOwnerPITStemId(this.getId());
    for (PITAttributeAssign assignment : assignments) {
      GrouperDAOFactory.getFactory().getPITAttributeAssign().delete(assignment);
    }
    
    // delete self group sets and their children
    GrouperDAOFactory.getFactory().getPITGroupSet().deleteSelfByPITOwnerId(this.getId());
    
    // delete groups
    Set<PITGroup> groups = GrouperDAOFactory.getFactory().getPITGroup().findByPITStemId(this.getId());
    for (PITGroup group : groups) {
      GrouperDAOFactory.getFactory().getPITGroup().delete(group);
      if (printOutput) {
        System.out.println("Done deleting group from point in time: " + group.getName() + ", ID=" + group.getId());
      }
    }
    
    // delete attribute def names
    Set<PITAttributeDefName> attrs = GrouperDAOFactory.getFactory().getPITAttributeDefName().findByPITStemId(this.getId());
    for (PITAttributeDefName attr : attrs) {
      GrouperDAOFactory.getFactory().getPITAttributeDefName().delete(attr);
      if (printOutput) {
        System.out.println("Done deleting attributeDefName from point in time: " + attr.getName() + ", ID=" + attr.getId());
      }
    }
    
    // delete attribute defs
    Set<PITAttributeDef> defs = GrouperDAOFactory.getFactory().getPITAttributeDef().findByPITStemId(this.getId());
    for (PITAttributeDef def : defs) {
      GrouperDAOFactory.getFactory().getPITAttributeDef().delete(def);
      if (printOutput) {
        System.out.println("Done deleting attributeDef from point in time: " + def.getName() + ", ID=" + def.getId());
      }
    }
    
    // delete child stems
    Set<PITStem> stems = GrouperDAOFactory.getFactory().getPITStem().findByParentPITStemId(this.getId());
    for (PITStem stem : stems) {
      GrouperDAOFactory.getFactory().getPITStem().delete(stem);
    }
  }
  
  private PITStem pitParentStem;
  
  /**
   * @return pitParentStem
   */
  public PITStem getParentPITStem() {
    if (pitParentStem == null && parentStemId != null) {
      pitParentStem = GrouperDAOFactory.getFactory().getPITStem().findById(parentStemId, true);
    }
    
    return pitParentStem;
  }
}
