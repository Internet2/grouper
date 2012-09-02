/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.stem;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.grouperSet.GrouperSet;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class StemSet extends GrouperAPI implements Hib3GrouperVersioned, GrouperSet {
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "StemSet: " + this.id;
  }

  /** name of the stem set table */
  public static final String TABLE_GROUPER_STEM_SET = "grouper_stem_set";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_DEPTH = "depth";

  /** column */
  public static final String COLUMN_IF_HAS_STEM_ID = "if_has_stem_id";

  /** column */
  public static final String COLUMN_THEN_HAS_STEM_ID = "then_has_stem_id";

  /** column */
  public static final String COLUMN_PARENT_STEM_SET_ID = "parent_stem_set_id";

  /** column */
  public static final String COLUMN_TYPE = "type";



  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasStemId */
  public static final String FIELD_IF_HAS_STEM_ID = "ifHasStemId";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: parentStemSetId */
  public static final String FIELD_PARENT_STEM_SET_ID = "parentStemSetId";

  /** constant for field name for: thenHasStemId */
  public static final String FIELD_THEN_HAS_STEM_ID = "thenHasStemId";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH,  
      FIELD_ID, FIELD_IF_HAS_STEM_ID,  FIELD_LAST_UPDATED_DB, 
      FIELD_THEN_HAS_STEM_ID, FIELD_TYPE, FIELD_PARENT_STEM_SET_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH,  
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_IF_HAS_STEM_ID,  
      FIELD_LAST_UPDATED_DB, FIELD_THEN_HAS_STEM_ID, FIELD_TYPE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** membership type -- self, immediate, or effective */
  private StemHierarchyType type = StemHierarchyType.immediate;

  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   */
  private String parentStemSetId;

  /** stem id of the child stem */
  private String thenHasStemId;
  
  /** stem id of the parent stem */
  private String ifHasStemId;

  /**
   * depth - 0 for self records, 1 for immediate memberships, > 1 for effective 
   */
  private int depth;

  /**
   * time in millis when this stem set was created
   */
  private Long createdOnDb;

  /**
   * time in millis when this stem set was last modified
   */
  private Long lastUpdatedDb;

  /**
   * find a stem set, better be here
   * @param stemSets 
   * @param ifHasId 
   * @param thenHasId 
   * @param depth is the depth expecting
   * @param exceptionIfNull 
   * @return the stem set
   */
  public static StemSet findInCollection(
      Collection<StemSet> stemSets, String ifHasId, 
      String thenHasId, int depth, boolean exceptionIfNull) {

    //are we sure we are getting the right one here???
    for (StemSet stemSet : GrouperUtil.nonNull(stemSets)) {
      if (StringUtils.equals(ifHasId, stemSet.getIfHasStemId())
          && StringUtils.equals(thenHasId, stemSet.getThenHasStemId())
          && depth == stemSet.getDepth()) {
        return stemSet;
      }
    }
    if (exceptionIfNull) {
      throw new RuntimeException("Cant find stem set with id: " + ifHasId + ", " + thenHasId + ", " + depth);
    }
    return null;
  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    
    if (!(other instanceof StemSet)) {
      return false;
    }
    
    StemSet that = (StemSet) other;
    return new EqualsBuilder()
      .append(this.parentStemSetId, that.parentStemSetId)
      .append(this.thenHasStemId, that.thenHasStemId)
      .append(this.ifHasStemId, that.ifHasStemId)
      .isEquals();

  }
  
  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.parentStemSetId)
      .append(this.thenHasStemId)
      .append(this.ifHasStemId)
      .toHashCode();
  }
  
  /**
   * 
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
  
  /**
   * @return the parent stem set
   */
  public StemSet getParentStemSet() {
    if (this.depth == 0) {
      return this;
    }
    
    StemSet parent = GrouperDAOFactory.getFactory().getStemSet().findById(this.getParentStemSetId(), true);
    return parent;
  }
  
  /**
   * @return the parent stem
   */
  public Stem getIfHasStem() {
    return GrouperDAOFactory.getFactory().getStem().findByUuid(this.getIfHasStemId(), true);
  }
  
  /**
   * @return the child stem
   */
  public Stem getThenHasStem() {
    return GrouperDAOFactory.getFactory().getStem().findByUuid(this.getThenHasStemId(), true);
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
   * set context id
   * @param contextId
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  /**
   * @return parent id
   */
  public String getParentStemSetId() {
    return parentStemSetId;
  }

  
  /**
   * @param parentId1
   */
  public void setParentStemSetId(String parentId1) {
    this.parentStemSetId = parentId1;
  }

  
  /**
   * @return stem id of the child stem
   */
  public String getThenHasStemId() {
    return this.thenHasStemId;
  }

  /**
   * Set stem id of the child stem
   * @param stemId
   */
  public void setThenHasStemId(String stemId) {
    this.thenHasStemId = stemId;
  }

  /**
   * @return stem id of the parent stem
   */
  public String getIfHasStemId() {
    return this.ifHasStemId;
  }

  
  /**
   * Set stem id of the parent stem
   * @param stemId
   */
  public void setIfHasStemId(String stemId) {
    this.ifHasStemId = stemId;
  }
  
  
  /**
   * @return membership type (immediate, effective, or self)
   */
  public StemHierarchyType getType() {
    return this.type;
  }

  /**
   * get string value of type for hibernate
   * @return type
   */
  public String getTypeDb() {
    return this.type == null ? null : this.type.name();
  }
  
  /**
   * set stem set assignment type
   * @param type1
   */
  public void setType(StemHierarchyType type1) {
    this.type = type1;
  }

  /**
   * set stem set assignment type
   * @param type1
   */
  public void setTypeDb(String type1) {
    this.type = StemHierarchyType.valueOfIgnoreCase(type1, false);
  }

  /**
   * @return depth
   */
  public int getDepth() {
    return depth;
  }

  /**
   * set depth
   * @param depth
   */
  public void setDepth(int depth) {
    this.depth = depth;
  }

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
  }

  /**
   * when created
   * @return timestamp
   */
  public Long getCreatedOnDb() {
    return this.createdOnDb;
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }

  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getStemSet().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getStemSet().delete(this);
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getId()
   */
  public String __getId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getIfHasElementId()
   */
  public String __getIfHasElementId() {
    return this.getIfHasStemId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getThenHasElementId()
   */
  public String __getThenHasElementId() {
    return this.getThenHasStemId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getDepth()
   */
  public int __getDepth() {
    return this.getDepth();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getIfHasElement()
   */
  public GrouperSetElement __getIfHasElement() {
    return this.getIfHasStem();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getThenHasElement()
   */
  public GrouperSetElement __getThenHasElement() {
    return this.getThenHasStem();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__setParentGrouperSetId(java.lang.String)
   */
  public void __setParentGrouperSetId(String grouperSetId) {
    this.setParentStemSetId(grouperSetId);
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getParentGrouperSet()
   */
  public GrouperSet __getParentGrouperSet() {
    return this.getParentStemSet();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getParentGrouperSetId()
   */
  public String __getParentGrouperSetId() {
    return this.getParentStemSetId();
  }

  /**
   * @see GrouperAPI#onPreSave(HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    if (this.createdOnDb == null) {
      this.createdOnDb = System.currentTimeMillis();
    }
    
    this.lastUpdatedDb = System.currentTimeMillis();
  }

  /**
   * @see GrouperAPI#onPreUpdate(HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.lastUpdatedDb = System.currentTimeMillis();
    
    if (this.dbVersionDifferentFields().contains(FIELD_DEPTH)) {
      throw new RuntimeException("cannot update depth");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_IF_HAS_STEM_ID)) {
      throw new RuntimeException("cannot update ifHasStemId");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_THEN_HAS_STEM_ID)) {
      throw new RuntimeException("cannot update thenHasStemId");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_PARENT_STEM_SET_ID) && parentStemSetId != null) {
      throw new RuntimeException("cannot update parentStemSetId");
    }
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public StemSet dbVersion() {
    return (StemSet)this.dbVersion;
  }
  
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }


  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);
    return result;
  }
}
