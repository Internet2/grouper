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

  /** hibernate version */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";

  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_NAME);



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
   * @param pitFieldId specifies the field id.  This is required.
   * @param pointInTimeFrom the start of the range of the point in time query.  This is optional.
   * @param pointInTimeTo the end of the range of the point in time query.  This is optional.  If this is the same as pointInTimeFrom, then the query will be done at a single point in time rather than a range.
   * @param sources optionally filter on subject source ids.
   * @param queryOptions optional query options.
   * @return set of pit members
   */
  public Set<Member> getMembers(String pitFieldId, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, Set<Source> sources, QueryOptions queryOptions) {
    
    if (pitFieldId == null) {
      throw new IllegalArgumentException("pitFieldId required.");
    }
    
    Set<Member> members = new LinkedHashSet<Member>();
    try {
      GrouperSession session = GrouperSession.staticGrouperSession();
      if (!this.isActive() && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
        return members;
      }
      
      Field field = FieldFinder.findById(pitFieldId, false);
      if (field == null && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
        return members;
      }

      if (this.isActive() && field != null) {
        Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(this.getId(), true);
        PrivilegeHelper.dispatch(session, group, session.getSubject(), field.getReadPriv());
      }
      
      members = GrouperDAOFactory.getFactory().getPITMembership().findAllMembersByOwnerAndField( 
          this.getId(), pitFieldId, pointInTimeFrom, pointInTimeTo, sources, queryOptions);
    }
    catch (InsufficientPrivilegeException e) {
      // ignore -- this is what Group.getMembers() does too...  
    }
    
    return members;
  }
  
  /**
   * Check if the group has a member using point in time and the specified field.
   * @param subject specifies the subject.  This is required.
   * @param pitFieldId specifies the field id.  This is required.
   * @param pointInTimeFrom the start of the range of the point in time query.  This is optional.
   * @param pointInTimeTo the end of the range of the point in time query.  This is optional.  If this is the same as pointInTimeFrom, then the query will be done at a single point in time rather than a range.
   * @param queryOptions optional query options.
   * @return boolean
   */
  public boolean hasMember(Subject subject, String pitFieldId, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions) {
    
    if (subject == null) {
      throw new IllegalArgumentException("subject required.");
    }
    
    if (pitFieldId == null) {
      throw new IllegalArgumentException("pitFieldId required.");
    }
    
    Member m = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    
    int size = GrouperDAOFactory.getFactory().getPITMembership().findAllByOwnerAndMemberAndField(
        this.getId(), m.getUuid(), pitFieldId, pointInTimeFrom, pointInTimeTo, queryOptions).size();
    
    if (size > 0) {
      return true;
    }
    
    // need to check GrouperAll as well...
    Member all = MemberFinder.internal_findAllMember();
    if (!all.getUuid().equals(m.getUuid())) {
      size = GrouperDAOFactory.getFactory().getPITMembership().findAllByOwnerAndMemberAndField(
          this.getId(), all.getUuid(), pitFieldId, pointInTimeFrom, pointInTimeTo, queryOptions).size();
      
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
    
    return new EqualsBuilder()
      .append(this.getName(), ((PITGroup) other).getName())
      .append(this.getStartTimeDb(), ((PITGroup) other).getStartTimeDb())
      .isEquals();
  }
  
  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.getName())
      .append(this.getStartTimeDb())
      .toHashCode();
  }
}
