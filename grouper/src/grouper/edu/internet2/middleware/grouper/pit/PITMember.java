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
package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class PITMember extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** subject id */
  public static final String COLUMN_SUBJECT_ID = "subject_id";

  /** subject source */
  public static final String COLUMN_SUBJECT_SOURCE = "subject_source";
  
  /** subject type */
  public static final String COLUMN_SUBJECT_TYPE = "subject_type";
  
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

  /** constant for field name for: subjectId */
  public static final String FIELD_SUBJECT_ID = "subjectId";
  
  /** constant for field name for: subjectSource */
  public static final String FIELD_SUBJECT_SOURCE = "subjectSource";
  
  /** constant for field name for: subjectType */
  public static final String FIELD_SUBJECT_TYPE = "subjectType";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_SUBJECT_ID, FIELD_SUBJECT_SOURCE, FIELD_SUBJECT_TYPE, FIELD_SOURCE_ID);



  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_MEMBERS = "grouper_pit_members";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /** subjectId */
  private String subjectId;
  
  /** subjectSource */
  private String subjectSourceId;
  
  /** subjectType */
  private String subjectTypeId;

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
   * @return subjectId
   */
  public String getSubjectId() {
    return subjectId;
  }

  /**
   * Set subjectId
   * @param subjectId
   */
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }
  
  /**
   * @return subjectSourceId
   */
  public String getSubjectSourceId() {
    return subjectSourceId;
  }

  /**
   * Set subjectSourceId
   * @param subjectSourceId
   */
  public void setSubjectSourceId(String subjectSourceId) {
    this.subjectSourceId = subjectSourceId;
  }
  
  /**
   * @return subjectType
   */
  public String getSubjectTypeId() {
    return subjectTypeId;
  }

  /**
   * Set subjectTypeId
   * @param subjectTypeId
   */
  public void setSubjectTypeId(String subjectTypeId) {
    this.subjectTypeId = subjectTypeId;
  }

  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getPITMember().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITMember().delete(this);
  }
  
  /**
   * @param fieldSourceId specifies the field id.  This is required.
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param pitStem is the stem to check in, or null if all
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param pointInTimeFrom the start of the range of the point in time query.  This is optional.
   * @param pointInTimeTo the end of the range of the point in time query.  This is optional.  If this is the same as pointInTimeFrom, then the query will be done at a single point in time rather than a range.
   * @param queryOptions optional query options.
   * @return Set of PITGroup
   */
  public Set<PITGroup> getGroups(String fieldSourceId, String scope, PITStem pitStem, Scope stemScope, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo, QueryOptions queryOptions) {
    return PITMember.getGroups(this.getSourceId(), fieldSourceId, scope, pitStem, stemScope, pointInTimeFrom, pointInTimeTo, queryOptions);
  }
  
  /**
   * @param memberSourceId specifies the member id.  This is required.
   * @param fieldSourceId specifies the field id.  This is required.
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param pitStem is the stem to check in, or null if all
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param pointInTimeFrom the start of the range of the point in time query.  This is optional.
   * @param pointInTimeTo the end of the range of the point in time query.  This is optional.  If this is the same as pointInTimeFrom, then the query will be done at a single point in time rather than a range.
   * @param queryOptions optional query options.
   * @return Set of PITGroup
   */
  public static Set<PITGroup> getGroups(String memberSourceId, String fieldSourceId, String scope, PITStem pitStem, Scope stemScope, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo, QueryOptions queryOptions) {
    
    if (memberSourceId == null) {
      throw new IllegalArgumentException("memberSourceId required.");
    }
    
    if (fieldSourceId == null) {
      throw new IllegalArgumentException("fieldSourceId required.");
    }
        
    PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(memberSourceId, false);
    if (pitMember == null) {
      return new LinkedHashSet<PITGroup>();
    }
    
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(fieldSourceId, true);
    return GrouperDAOFactory.getFactory().getPITGroup().getAllGroupsMembershipSecure(
        pitMember.getId(), pitField.getId(), scope, pitStem, stemScope, pointInTimeFrom, pointInTimeTo, queryOptions);
  }
  
  /**
   * @param memberSourceId specifies the member id.  This is required.
   * @param fieldSourceId specifies the field id.  This is required.
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param stem is the stem to check in, or null if all
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param pointInTimeFrom the start of the range of the point in time query.  This is optional.
   * @param pointInTimeTo the end of the range of the point in time query.  This is optional.  If this is the same as pointInTimeFrom, then the query will be done at a single point in time rather than a range.
   * @param queryOptions optional query options.
   * @return Set of PITGroup
   */
  public static Set<PITGroup> getGroups(String memberSourceId, String fieldSourceId, String scope, Stem stem, Scope stemScope, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo, QueryOptions queryOptions) {
    
    PITStem pitStem = null;
    
    if (stem != null) {
      pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(stem.getUuid(), true);
    }
        
    return getGroups(memberSourceId, fieldSourceId, scope, pitStem, stemScope, pointInTimeFrom, pointInTimeTo, queryOptions);
  }
}
