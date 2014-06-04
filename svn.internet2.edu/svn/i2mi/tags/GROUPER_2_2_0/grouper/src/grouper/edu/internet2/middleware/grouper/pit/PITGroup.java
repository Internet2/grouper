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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class PITGroup extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** name */
  public static final String COLUMN_NAME = "name";
  
  /** stem */
  public static final String COLUMN_STEM_ID = "stem_id";

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
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_NAME, FIELD_STEM_ID, FIELD_SOURCE_ID);



  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_GROUPS = "grouper_pit_groups";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /** name */
  private String name;
  
  /** stem */
  private String stemId;

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
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getPITGroup().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITGroup().delete(this);
  }
  
  /**
   * Get members of the group using point in time and the specified field.
   * If the group currently exists, you must have read access to it.  If it has been deleted, you must be wheel or root.
   * You must also be wheel or root if the field has been deleted.
   * An empty set is returned if you do not have appropriate privileges.
   * @param fieldSourceId specifies the field id.  This is required.
   * @param pointInTimeFrom the start of the range of the point in time query.  This is optional.
   * @param pointInTimeTo the end of the range of the point in time query.  This is optional.  If this is the same as pointInTimeFrom, then the query will be done at a single point in time rather than a range.
   * @param sources optionally filter on subject source ids.
   * @param queryOptions optional query options.
   * @return set of pit members
   */
  public Set<Member> getMembers(String fieldSourceId, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, Set<Source> sources, QueryOptions queryOptions) {
    
    if (fieldSourceId == null) {
      throw new IllegalArgumentException("fieldSourceId required.");
    }
    
    Set<Member> members = new LinkedHashSet<Member>();
    try {
      GrouperSession session = GrouperSession.staticGrouperSession();
      if (!this.isActive() && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
        return members;
      }
      
      Field field = FieldFinder.findById(fieldSourceId, false);
      if (field == null && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
        return members;
      }

      if (this.isActive() && field != null) {
        Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(this.getSourceId(), true);
        PrivilegeHelper.dispatch(session, group, session.getSubject(), field.getReadPriv());
      }
      
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(fieldSourceId, true);
      members = GrouperDAOFactory.getFactory().getPITMembershipView().findAllMembersByPITOwnerAndPITField( 
          this.getId(), pitField.getId(), pointInTimeFrom, pointInTimeTo, sources, queryOptions);
    }
    catch (InsufficientPrivilegeException e) {
      // ignore -- this is what Group.getMembers() does too...  
    }
    
    return members;
  }
  
  /**
   * Check if the group has a member using point in time and the specified field.
   * @param subject specifies the subject.  This is required.
   * @param fieldSourceId specifies the field id.  This is required.
   * @param pointInTimeFrom the start of the range of the point in time query.  This is optional.
   * @param pointInTimeTo the end of the range of the point in time query.  This is optional.  If this is the same as pointInTimeFrom, then the query will be done at a single point in time rather than a range.
   * @param queryOptions optional query options.
   * @return boolean
   */
  public boolean hasMember(Subject subject, String fieldSourceId, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions) {
    
    if (subject == null) {
      throw new IllegalArgumentException("subject required.");
    }
    
    if (fieldSourceId == null) {
      throw new IllegalArgumentException("fieldSourceId required.");
    }
    
    Member m = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(fieldSourceId, true);
    PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(m.getUuid(), false);
    if (pitMember != null) {
      int size = GrouperDAOFactory.getFactory().getPITMembershipView().findAllByPITOwnerAndPITMemberAndPITField(
          this.getId(), pitMember.getId(), pitField.getId(), pointInTimeFrom, pointInTimeTo, queryOptions).size();
      
      if (size > 0) {
        return true;
      }    
    }
    
    // need to check GrouperAll as well...
    Member all = MemberFinder.internal_findAllMember();
    if (!all.getUuid().equals(m.getUuid())) {
      PITMember pitMemberAll = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(all.getUuid(), true);
      int size = GrouperDAOFactory.getFactory().getPITMembershipView().findAllByPITOwnerAndPITMemberAndPITField(
          this.getId(), pitMemberAll.getId(), pitField.getId(), pointInTimeFrom, pointInTimeTo, queryOptions).size();
      
      if (size > 0) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    
    if (!(other instanceof PITGroup)) {
      return false;
    }
    
    return new EqualsBuilder().append(this.getId(), ((PITGroup) other).getId()).isEquals();
  }
  
  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder().append(this.getId()).toHashCode();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    if (this.isActive()) {
      throw new RuntimeException("Cannot delete active point in time group object with id=" + this.getId());
    }
    
    // delete memberships
    Set<PITMembership> memberships = GrouperDAOFactory.getFactory().getPITMembership().findAllByPITOwner(this.getId());
    for (PITMembership membership : memberships) {
      GrouperDAOFactory.getFactory().getPITMembership().delete(membership);
    }
    
    // delete attribute assignments
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findByOwnerPITGroupId(this.getId());
    for (PITAttributeAssign assignment : assignments) {
      GrouperDAOFactory.getFactory().getPITAttributeAssign().delete(assignment);
    }
    
    // delete self group sets and their children
    GrouperDAOFactory.getFactory().getPITGroupSet().deleteSelfByPITOwnerId(this.getId());
    
    // delete group sets where this group is a member ... and their children.
    Set<PITGroupSet> groupSets = GrouperDAOFactory.getFactory().getPITGroupSet().findAllByMemberPITGroup(this.getId());
    for (PITGroupSet groupSet : groupSets) {
      GrouperDAOFactory.getFactory().getPITGroupSet().delete(groupSet);
    }
    
    // delete memberships where this group is a member
    Set<PITMember> members = GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType(this.getSourceId(), "g:gsa", "group");
    for (PITMember member : members) {
      memberships = GrouperDAOFactory.getFactory().getPITMembership().findAllByPITMember(member.getId());
      for (PITMembership membership : memberships) {
        GrouperDAOFactory.getFactory().getPITMembership().delete(membership);
      }
    }
    
    // delete self role sets and their children
    GrouperDAOFactory.getFactory().getPITRoleSet().deleteSelfByPITRoleId(this.getId());
    
    // delete role sets by thenHasRoleId ... and their children.
    Set<PITRoleSet> roleSets = GrouperDAOFactory.getFactory().getPITRoleSet().findByThenHasPITRoleId(this.getId());
    for (PITRoleSet roleSet : roleSets) {
      GrouperDAOFactory.getFactory().getPITRoleSet().delete(roleSet);
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
