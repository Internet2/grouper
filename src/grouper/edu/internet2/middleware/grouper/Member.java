/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeRuntimeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.MemberHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMemberChangeSubjectBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.MemberModifyValidator;
import edu.internet2.middleware.grouper.validator.NotNullValidator;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/** 
 * A member within the Groups Registry.
 * 
 * All immediate subjects, and effective members are members.  
 * 
 * @author  blair christensen.
 * @version $Id: Member.java,v 1.116 2008-11-11 22:08:33 mchyzer Exp $
 */
public class Member extends GrouperAPI implements Hib3GrouperVersioned {

  /** grouper_members table in the DB */
  public static final String TABLE_GROUPER_MEMBERS = "grouper_members";

  /** uuid col in db */
  public static final String COLUMN_MEMBER_UUID = "member_uuid";
  
  /** old id col for id conversion */
  public static final String COLUMN_OLD_ID = "old_id";
  
  /** old uuid id col for id conversion */
  public static final String COLUMN_OLD_MEMBER_UUID = "old_member_uuid";
  
  /** serial version */
  public static final long serialVersionUID = 2348656645982471668L;

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: memberUUID */
  public static final String FIELD_MEMBER_UUID = "memberUUID";

  /** constant for field name for: subjectID */
  public static final String FIELD_SUBJECT_ID = "subjectID";

  /** constant for field name for: subjectSourceID */
  public static final String FIELD_SUBJECT_SOURCE_ID = "subjectSourceID";

  /** constant for field name for: subjectTypeID */
  public static final String FIELD_SUBJECT_TYPE_ID = "subjectTypeID";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_MEMBER_UUID, FIELD_SUBJECT_ID, FIELD_SUBJECT_SOURCE_ID, FIELD_SUBJECT_TYPE_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DB_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_MEMBER_UUID, FIELD_SUBJECT_ID, 
      FIELD_SUBJECT_SOURCE_ID, FIELD_SUBJECT_TYPE_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  /**  */
  @GrouperIgnoreFieldConstant 
  @GrouperIgnoreDbVersion
  @GrouperIgnoreClone
  private transient Group   g     = null;
  
  /**  */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private transient Subject subj  = null;

  /**  */
  private String  memberUUID;


  /**  */
  private String  subjectID;


  /**  */
  private String  subjectSourceID;


  /**  */
  private String  subjectTypeID;

  /** change a subject to the same subject (for testing) */
  static int changeSubjectSameSubject = 0;
  
  /** change a subject to a subject which didnt exist */
  static int changeSubjectDidntExist = 0;

  /** change a subject to a subject which did exist */
  static int changeSubjectExist = 0;
  
  /** number of memberships edited */
  static int changeSubjectMembershipAddCount = 0;
  
  /** number of memberships deleted due to duplicates */
  static int changeSubjectMembershipDeleteCount = 0;
  
  
  /**
   * change the subject of a member to another subject.  This new subject might already exist, and it
   * might not.  If it doesnt exist, then this member object will have its subject and source information
   * updated.  If it does, then all objects in the system which use this member_id will be queried, and
   * the member_id updated to the new member_id, and the old member_id will be removed.
   * 
   * @param newSubject
   * @throws InsufficientPrivilegeRuntimeException if not a root user
   */
  public void changeSubject(Subject newSubject) throws InsufficientPrivilegeRuntimeException {
    this.changeSubject(newSubject, true);
  }
  
  /**
   * change the subject of a member to another subject.  This new subject might already exist, and it
   * might not.  If it doesnt exist, then this member object will have its subject and source information
   * updated.  If it does, then all objects in the system which use this member_id will be queried, and
   * the member_id updated to the new member_id, and the old member_id will be removed if deleteOldMember is true.
   * 
   * @param newSubject
   * @param deleteOldMember is only applicable if the new member exists.  If true, it will delete the old one.
   * Generally you want this as true, and only set to false if there is a foreign key violation, and you want to 
   * get as far as you can.
   * @throws InsufficientPrivilegeRuntimeException if not a root user
   */
  public void changeSubject(final Subject newSubject, final boolean deleteOldMember) 
      throws InsufficientPrivilegeRuntimeException {
    this.changeSubjectHelper(newSubject, deleteOldMember, null);
  }
  
  /**
   * <pre>
   * This is the readonly dry run report version of this operation...  this will not call hooks
   * 
   * change the subject of a member to another subject.  This new subject might already exist, and it
   * might not.  If it doesnt exist, then this member object will have its subject and source information
   * updated.  If it does, then all objects in the system which use this member_id will be queried, and
   * the member_id updated to the new member_id, and the old member_id will be removed.
   * 
   * </pre>
   * @param newSubject
   * @param deleteOldMember is only applicable if the new member exists.  If true, it will delete the old one.
   * Generally you want this as true, and only set to false if there is a foreign key violation, and you want to 
   * get as far as you can.
   * @return the report
   */
  public String changeSubjectReport(final Subject newSubject, final boolean deleteOldMember) {
    StringBuilder result = new StringBuilder();
    this.changeSubjectHelper(newSubject, deleteOldMember, result);
    return result.toString();
  }

  /** boolean constant for gsh param */
  public static final boolean DELETE_OLD_MEMBER = true; 
  
  /**
   * change the subject of a member to another subject.  This new subject might already exist, and it
   * might not.  If it doesnt exist, then this member object will have its subject and source information
   * updated.  If it does, then all objects in the system which use this member_id will be queried, and
   * the member_id updated to the new member_id, and the old member_id will be removed.
   * 
   * @param newSubject
   * @param deleteOldMember is only applicable if the new member exists.  If true, it will delete the old one.
   * Generally you want this as true, and only set to false if there is a foreign key violation, and you want to 
   * get as far as you can.
   * @param report pass in report if only a dry run, dont actually do anything...
   * @throws InsufficientPrivilegeRuntimeException if not a root user
   */
  private void changeSubjectHelper(final Subject newSubject, final boolean deleteOldMember, 
      final StringBuilder report) throws InsufficientPrivilegeRuntimeException {
    
    //make sure root session
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    if (grouperSession == null  ||
        !PrivilegeHelper.isRoot(grouperSession)) {
      throw new InsufficientPrivilegeRuntimeException("changeSubject requires GrouperSystem " +
      		"or wheel/sysAdmin group grouperSession");
    }
    
    final String thisSubjectId = this.getSubjectId();
    final String thisSourceId = this.getSubjectSourceId();
    
    final String newSubjectId = newSubject.getId();
    final String newSourceId = newSubject.getSource().getId();
    
    String firstSummary = "changing subject from '" + thisSubjectId + "@" + thisSourceId + "' to '"
        + newSubjectId + "@" + newSourceId + "'";
    LOG.debug(firstSummary);
    if (report != null) {
      report.append(firstSummary).append("\n");
    }
    
    if (StringUtils.equals(thisSubjectId, newSubjectId)
        && StringUtils.equals(thisSourceId, newSourceId)) {
      String sameSubject = "new subject is same as current subject";
      LOG.debug(sameSubject);
      if (report != null) {
        report.append(sameSubject).append("\n");
      }
      changeSubjectSameSubject++;
      return;
    }
    
    //static grouper session should exist at this point
    Member theNewMember = null;
    boolean theMemberDidntExist = false;
    try {
      theNewMember = GrouperDAOFactory.getFactory().getMember().findBySubject(newSubject);
    } catch (MemberNotFoundException mnfe) {
      theMemberDidntExist = true;
    }

    final boolean memberDidntExist = theMemberDidntExist;
    final Member newMember = theNewMember;
    
    //this needs to run in a transaction
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession)
          throws GrouperDAOException {
        try {
          //hooks bean
          HooksMemberChangeSubjectBean hooksMemberChangeSubjectBean = new HooksMemberChangeSubjectBean(
              Member.this, newSubject, thisSubjectId, thisSourceId, deleteOldMember, memberDidntExist);
  
          if (report == null) {
            //call pre-hooks if registered
            GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.MEMBER, 
                MemberHooks.METHOD_MEMBER_PRE_CHANGE_SUBJECT, 
                hooksMemberChangeSubjectBean, VetoTypeGrouper.MEMBER_PRE_CHANGE_SUBJECT);
          }
          
          if (memberDidntExist) {
            changeSubjectDidntExist++;
            
            if (report == null) {
              //since it didnt exist, we can just change the subject id and source id of the existing member object
              Member.this.setSubjectIdDb(newSubjectId);
              Member.this.setSubjectSourceIdDb(newSourceId);
              Member.this.setSubjectTypeId(newSubject.getType().getName());
    
              Member.this.store();
            } else {
              report.append("[new member does not exist], CHANGE the " +
              		"subject id, source, type of old member: " + Member.this.getUuid()
              		+ " FROM: " + Member.this.subjectID + ", " + Member.this.subjectSourceID
              		+ ", " + Member.this.subjectTypeID + "  TO: " + newSubjectId
              		+ ", " + newSourceId + ", " + newSubject.getType().getName()).append("\n");
            }
          } else {
            changeSubjectExist++;
            final String newMemberUuid = newMember.getUuid();
            {
              //grouper_composites.creator_id
              Set<Composite> composites = GrouperDAOFactory.getFactory().getComposite().findByCreator(Member.this);
              if (GrouperUtil.length(composites) > 0) {
                for (Composite composite : composites) {
                  if (report == null) {
                    composite.setCreatorUuid(newMemberUuid);
                  } else {
                    report.append("CHANGE composite: " + composite.getUuid() + ", " + composite.getOwnerGroup().getName()
                        + ", creator id FROM: " + composite.getCreatorUuid() + ", TO: " + newMemberUuid + "\n");
                  }
                }
                if (report == null) {
                  hibernateSession.byObject().saveOrUpdate(composites);
                }
              }
            }
            
            {
              //grouper_groups.creator_id, 
              //  modifier_id
              Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findByCreatorOrModifier(Member.this);
              if (GrouperUtil.length(groups) > 0) {
                for (Group group : groups) {
                  if (StringUtils.equals(Member.this.getUuid(), group.getCreatorUuid())) {
                    if (report == null) {
                      group.setCreatorUuid(newMemberUuid);
                    } else {
                      report.append("CHANGE group: " + group.getUuid() + ", " + group.getName()
                          + ", creator id FROM: " + group.getCreatorUuid() + ", TO: " + newMemberUuid + "\n");
                    }
                  }
                  if (StringUtils.equals(Member.this.getUuid(), group.getModifierUuid())) {
                    if (report == null) {
                      group.setModifierUuid(newMemberUuid);
                    } else {
                      report.append("CHANGE group: " + group.getUuid() + ", " + group.getName()
                          + ", modifier id FROM: " + group.getCreatorUuid() + ", TO: " + newMemberUuid + "\n");
                      
                    }
                  }
                }
                if (report == null) {
                  hibernateSession.byObject().saveOrUpdate(groups);
                }
              }
            }
            
            {
              //this one is a little complicated since if the new member id already has a comparable membership,
              //then we dont want to create another one
              
              //grouper_memberships.member_id, 
              //  creator_id
              Set<Membership> membershipsPrevious = GrouperDAOFactory.getFactory().getMembership().findAllByCreatorOrMember(Member.this);
              if (GrouperUtil.length(membershipsPrevious) > 0) {
                Set<Membership> membershipsNew = GrouperDAOFactory.getFactory().getMembership().findAllByMember(newMemberUuid);
                Set<Membership> membershipsToUpdate = new HashSet<Membership>();
                Set<Membership> membershipsToDelete = new HashSet<Membership>();
    
                for (Membership membershipPrevious : membershipsPrevious) {
                  boolean isDeleting = false;
                  if (StringUtils.equals(Member.this.getUuid(), membershipPrevious.getMemberUuid())) {
                    
                    membershipPrevious.setMemberUuid(newMemberUuid);
                    //dont add a duplicate
                    if (membershipsNew.contains(membershipPrevious)) {
                      changeSubjectMembershipDeleteCount++;
                      isDeleting = true;
                      if (report == null) {
                        membershipsToDelete.add(membershipPrevious);
                      } else {
                        report.append("DELETE membership [since will already exist]: " 
                            + membershipPrevious.getUuid() + ", " + membershipPrevious.getOwnerName()
                            + ", " + membershipPrevious.getListType() + "-" + membershipPrevious.getListName() + "\n");
                      }
                    } else {
                      changeSubjectMembershipAddCount++;
                      if (report == null) {
                        membershipsToUpdate.add(membershipPrevious);
                      } else {
                        report.append("CHANGE membership: " 
                            + membershipPrevious.getUuid() + ", " + membershipPrevious.getOwnerName()
                            + ", " + membershipPrevious.getListType() + "-" + membershipPrevious.getListName()
                            + ", member id FROM: " + membershipPrevious.getMemberUuid() + ", TO: " + newMemberUuid + "\n");
                      }
                    }
                  }
                  if (!isDeleting && StringUtils.equals(Member.this.getUuid(), membershipPrevious.getCreatorUuid())) {
                    if (report == null) {
                      membershipPrevious.setCreatorUuid(newMemberUuid);
                      membershipsToUpdate.add(membershipPrevious);
                    } else {
                      report.append("CHANGE membership: " 
                          + membershipPrevious.getUuid() + ", " + membershipPrevious.getOwnerName() 
                          + ", " + membershipPrevious.getListType() + "-" + membershipPrevious.getListName()
                          + ", creator id FROM: " + membershipPrevious.getCreatorUuid() + ", TO: " + newMemberUuid + "\n");
                      
                    }
                  }
                }
                if (report == null) {
                  //see if there are objects to update or delete
                  if (GrouperUtil.length(membershipsToUpdate) > 0) {
                    hibernateSession.byObject().saveOrUpdate(membershipsToUpdate);
                  }
                  if (GrouperUtil.length(membershipsToDelete) > 0) {
                    hibernateSession.byObject().delete(membershipsToDelete);
                  }
                }
              }
            }
    
            {
              //grouper_stems.creator_id, 
              //  modifier_id
              Set<Stem> stems = GrouperDAOFactory.getFactory().getStem().findByCreatorOrModifier(Member.this);
              if (GrouperUtil.length(stems) > 0) {
                for (Stem stem : stems) {
                  if (StringUtils.equals(Member.this.getUuid(), stem.getCreatorUuid())) {
                    if (report == null) {
                      stem.setCreatorUuid(newMemberUuid);
                    } else {
                      report.append("CHANGE stem: " 
                          + stem.getUuid() + ", " + stem.getName()
                          + ", creator id FROM: " + stem.getCreatorUuid() + ", TO: " + newMemberUuid + "\n");
                    }
                  }
                  if (StringUtils.equals(Member.this.getUuid(), stem.getModifierUuid())) {
                    if (report == null) {
                      stem.setModifierUuid(newMemberUuid);
                    } else {
                      report.append("CHANGE stem: " 
                          + stem.getUuid() + ", " + stem.getName()
                          + ", modifier id FROM: " + stem.getModifierUuid() + ", TO: " + newMemberUuid + "\n");
                    }
                  }
                }
                if (report == null) {
                  hibernateSession.byObject().saveOrUpdate(stems);
                }
              }
            }
            
            {
              //grouper_types.creator_uuid
              Set<GroupType> groupTypes = GrouperDAOFactory.getFactory().getGroupType().findAllByCreator(Member.this);
              if (GrouperUtil.length(groupTypes) > 0) {
                for (GroupType groupType : groupTypes) {
                  if (report == null) {
                    groupType.setCreatorUuid(newMemberUuid);
                  } else {
                    report.append("CHANGE groupType: " 
                        + groupType.getUuid() + ", " + groupType.getName()
                        + ", creator id FROM: " + groupType.getCreatorUuid() + ", TO: " + newMemberUuid + "\n");
                  }
                }
                if (report == null) {
                  hibernateSession.byObject().saveOrUpdate(groupTypes);
                }
              }
            }
            
            //finally delete this member object
            if (deleteOldMember) {
              if (report == null) {
                hibernateSession.getSession().flush();          
                hibernateSession.byObject().delete(Member.this);
              } else {
                report.append("DELETE member: " 
                    + Member.this.getUuid() + ", " + Member.this.subjectID + "\n");
              }
            }
          }        
          if (report == null) {
            hibernateSession.getSession().flush();          
          
            GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBER, 
                MemberHooks.METHOD_MEMBER_POST_COMMIT_CHANGE_SUBJECT, 
                hooksMemberChangeSubjectBean);
    
            GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.MEMBER, 
                MemberHooks.METHOD_MEMBER_POST_CHANGE_SUBJECT, 
                hooksMemberChangeSubjectBean, VetoTypeGrouper.MEMBER_POST_CHANGE_SUBJECT);
          }  
          
          return null;
        } catch (GroupNotFoundException gnfe) {
          throw new GrouperDAOException(gnfe);
        }
      }
      
    });
    if (report == null) {
      this.setSubjectIdDb(newSubjectId);
      this.setSubjectSourceIdDb(newSourceId);
      this.setSubjectTypeId(newSubject.getType().getName());
    }      
  }

  /**
   * Can this {@link Member} <b>ADMIN</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canAdmin(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @return if admin
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canAdmin(Group g) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(g);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canAdmin( GrouperSession.staticGrouperSession(), g, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  }

  /**
   * Can this {@link Member} <b>CREATE</b> on this {@link Stem}.
   * <pre class="eg">
   * boolean rv = m.canCreate(ns);
   * </pre>
   * @param   ns  Check privileges on this {@link Stem}.
   * @return if can create stem
   * @throws  IllegalArgumentException if null {@link Stem}
   * @since   1.0
   */
  public boolean canCreate(Stem ns) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(ns);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.STEM_NULL);
    }
    try {
      return PrivilegeHelper.canCreate( GrouperSession.staticGrouperSession(), ns, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  } 

  /**
   * Can this {@link Member} <b>OPTIN</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canAdmin(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @return if can optin
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canOptin(Group g) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(g);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canOptin( GrouperSession.staticGrouperSession(), g, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } 

  /**
   * Can this {@link Member} <b>OPTOUT</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canOptout(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @return if can optout
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canOptout(Group g) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(g);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canOptout( GrouperSession.staticGrouperSession(), g, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } 

  /**
   * Can this {@link Member} <b>READ</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canRead(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @return if can read
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canRead(Group g)
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(g);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canRead( GrouperSession.staticGrouperSession(), g, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } 

  /**
   * Can this {@link Member} <b>STEM</b> on this {@link Stem}.
   * <pre class="eg">
   * boolean rv = m.canStem(ns);
   * </pre>
   * @param   ns  Check privileges on this {@link Stem}.
   * @return if can stem
   * @throws  IllegalArgumentException if null {@link Stem}
   * @since   1.0
   */
  public boolean canStem(Stem ns) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(ns);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.STEM_NULL);
    }
    try {
      return PrivilegeHelper.canStem( ns, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } 

  /**
   * Can this {@link Member} <b>UPDATE</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canUpdate(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @return if can update
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canUpdate(Group g) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(g);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canUpdate( GrouperSession.staticGrouperSession(), g, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } // public boolean canUPDATE(g)

  /**
   * Can this {@link Member} <b>VIEW</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canView(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @return if can view
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canView(Group g) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(g);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canView( GrouperSession.staticGrouperSession(), g, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  } 

  /**
   * Get groups where this member has an effective membership.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * // Get groups where this member is an effective member.
   * Set effectives = m.getEffectiveGroups();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getEffectiveGroups() {
    return this._getGroups( this.getEffectiveMemberships().iterator() );
  } // public Set getEffectiveGroups()

  /**
   * Get effective memberships.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set effectives = m.getEffectiveMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Membership> getEffectiveMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getEffectiveMemberships(Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getEffectiveMemberships()

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Member.class);

  /**
   * Get effective memberships.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set effectives = m.getEffectiveMemberships(f);
   * </pre>
   * @param   f   Get effective memberships in this list field.
   * @return  Set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getEffectiveMemberships(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.internal_findAllEffectiveByMemberAndField(
      GrouperSession.staticGrouperSession(), this, f
    );
  } // public Set getEffectiveMemberships(f)

  /**
   * Get groups where this member is a member.
   * <pre class="eg">
   * // Get groups where this member is a member.
   * Set groups = m.getGroups();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getGroups() {
    return this._getGroups( this.getMemberships().iterator() );
  } // public Set getGroups()

  /**
   * Get groups where this member has an immediate membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   *
   * <pre class="eg">
   * // Get groups where this member is an immediate member.
   * Set immediates = m.getImmediateGroups();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getImmediateGroups() {
    return this._getGroups( this.getImmediateMemberships().iterator() );
  } // public Set getImmediateGroups()

  /**
   * Get immediate memberships.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set immediates = m.getImmediateMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Membership> getImmediateMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getImmediateMemberships(Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal( msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getImmediateMemberships()

  /**
   * Get immediate memberships.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set immediates = m.getImmediateMemberships(f);
   * </pre>
   * @param   f   Get immediate memberships in this list field.
   * @return  Set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getImmediateMemberships(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.internal_findAllImmediateByMemberAndField( GrouperSession.staticGrouperSession(), this, f );
  } // public Set getImmediateMemberships(f)

  /**
   * Get memberships.   A member of a group (aka composite member) has either or both of
   * an immediate (direct) membership, or an effective (indirect) membership
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set groups = m.getMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Membership> getMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getMemberships(Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal( msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getMemberships()

  /**
   * Get memberships.   A member of a group (aka composite member) has either or both of
   * an immediate (direct) membership, or an effective (indirect) membership
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set groups = m.getMemberships(f);
   * </pre>
   * @param   f   Get memberships in this list field.
   * @return  Set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getMemberships(Field f) 
    throws  SchemaException
  {
    if (!f.getType().equals(FieldType.LIST)) {
      throw new SchemaException(f + " is not type " + FieldType.LIST);
    }
    return MembershipFinder.internal_findMemberships( GrouperSession.staticGrouperSession(), this, f );
  } // public Set getMemberships(f)

  /**
   * Find access privileges held by this member on a {@link Group}.
   * <pre class="eg">
   * Set access = m.getPrivs(g);
   * </pre>
   * @param   g   Find Access Privileges on this {@link Group}
   * @return  A set of {@link AccessPrivilege} objects.
   */
  public Set<AccessPrivilege> getPrivs(Group g) {
    Set privs = new LinkedHashSet();
    try {
      privs = GrouperSession.staticGrouperSession().getAccessResolver().getPrivileges( g, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
  } 

  /**
   * Find naming privileges held by this member on a {@link Stem}.
   * <pre class="eg">
   * Set naming = m.getPrivs(ns);
   * </pre>
   * @param   ns  Find Naming Privileges on this {@link Stem}
   * @return  A set of {@link NamingPrivilege} objects.
   */
  public Set<NamingPrivilege> getPrivs(Stem ns) {
    Set privs = new LinkedHashSet();
    try {
      privs = GrouperSession.staticGrouperSession().getNamingResolver().getPrivileges( ns, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage() );
    }
    return privs;
  }

  /**
   * Get {@link Subject} that maps to this member.
   * <pre class="eg">
   * // Convert a member back into a subject
   * try {
   *   Subject subj = m.getSubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   * </pre>
   * @return  A {@link Subject} object.
   * @throws  SubjectNotFoundException
   */ 
  public Subject getSubject() 
    throws  SubjectNotFoundException
  {
    if (this.subj == null) {
    	this.subj = new LazySubject(this);
    }
    return this.subj;
  } // public Subject getSubject()

  /**
   * Get the {@link Source} of the subject that maps to this member.
   * <pre class="eg">
   * Source sa = m.getSubjectSource();
   * </pre>
   * @return  Subject's {@link Source}
   * @throws  GrouperRuntimeException
   */ 
  public Source getSubjectSource() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getSubject().getSource();
    }
    catch (SubjectNotFoundException eSNF) {
      String msg = E.MEMBER_SUBJNOTFOUND + eSNF.getMessage();
      LOG.fatal( msg);
      throw new GrouperRuntimeException(msg, eSNF);
    }
  } // public Source getSubjectSource()

  /** Get the {@link Source} id of the subject that maps to this
   * member.
   * <pre class="eg">
   * String id = m.getSubjectSourceId();
   * </pre>
   * @return  Subject's {@link Source} id
   */
  public String getSubjectSourceId() {
    return this.subjectSourceID;
  } 

  /**
   * simple get subject source id
   * @return subject source id
   */
  public String getSubjectSourceIdDb() {
    return this.subjectSourceID;
  }
  
  /**
   * Get the {@link SubjectType} of the subject that maps to this member.
   * <pre class="eg">
   * // Get this member's subject type.
   * SubjectType type = m.getSubjectType();
   * </pre>
   * @return  Subject's {@link SubjectType}
   */ 
  public SubjectType getSubjectType() {
    return SubjectTypeEnum.valueOf( this.getSubjectTypeId() );
  } // public SubjectType getSubjectType()

  /**
   * Get the subject type id of the subject that maps to this member.
   * <pre class="eg">
   * // Get this member's subject type id.
   * String type = m.getSubjectTypeId();
   * </pre>
   * @return  Subject's type id.
   */ 
  public String getSubjectTypeId() {
    return this.subjectTypeID;
  }

  /**
   * Get member's UUID.
   * <pre class="eg">
   * // Get UUID of member.
   * String uuid = m.getUuid();
   * </pre>
   * @return  Member's UUID.
   */
  public String getUuid() {
    return this.memberUUID;
  }

  /**
   * Get groups where this member has the ADMIN privilege.
   * <pre class="eg">
   * Set admin = m.hasAdmin();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Group> hasAdmin() 
    throws  GrouperRuntimeException
{
    Set privs = new LinkedHashSet();
    try {
      privs = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.ADMIN
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
  } 

  /**
   * Report whether this member has ADMIN on the specified group.
   * <pre class="eg">
   * // Check whether this member has ADMIN on the specified group.
   * if (m.hasAdmin(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasAdmin(Group g) {
    return this._hasPriv(g, AccessPrivilege.ADMIN);
  } // public boolean hasAdmin(g)

  /**
   * Get stems where this member has the CREATE privilege.
   * <pre class="eg">
   * Set create = m.hasCreate();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Group> hasCreate() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = GrouperSession.staticGrouperSession().getNamingResolver().getStemsWhereSubjectHasPrivilege( this.getSubject(), NamingPrivilege.CREATE );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage() );
    }
    return privs;
  } 

  /**
   * Report whether this member has CREATE on the specified stem.
   * <pre class="eg">
   * // Check whether this member has CREATE on the specified stem.
   * if (m.hasCreate(ns)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   ns  Test for privilege on this {@link Stem}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasCreate(Stem ns) {
    return this._hasPriv(ns, NamingPrivilege.CREATE);
  } // public boolean hasCreate(ns)

  /**
   * Get groups where this member has the OPTIN privilege.
   * <pre class="eg">
   * Set optin = m.hasOptin();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Group> hasOptin() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.OPTIN
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
  } 

  /**
   * Report whether this member has OPTIN on the specified group.
   * <pre class="eg">
   * // Check whether this member has OPTIN on the specified group.
   * if (m.hasOptin(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasOptin(Group g) {
    return this._hasPriv(g, AccessPrivilege.OPTIN);
  } // public boolean hasOptin(g)

  /**
   * Get groups where this member has the OPTOUT privilege.
   * <pre class="eg">
   * Set optout = m.hasOptout();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Group> hasOptout() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
/*
    try {
      privs = PrivilegeResolver.internal_getGroupsWhereSubjectHasPriv(
        GrouperSession.staticGrouperSession(), this.getSubject(), AccessPrivilege.OPTOUT
      );
    }
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.OPTOUT;
      LOG.fatal( msg);
      throw new GrouperRuntimeException(msg, eS);
    }
*/
    try {
      privs = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.OPTOUT
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
  }

  /**
   * Report whether this member has OPTOUT on the specified group.
   * <pre class="eg">
   * // Check whether this member has OPTOUT on the specified group.
   * if (m.hasOptout(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasOptout(Group g) {
    return this._hasPriv(g, AccessPrivilege.OPTOUT);
  } // public boolean hasOptout(g)

  /**
   * Get groups where this member has the READ privilege.
   * <pre class="eg">
   * Set read = m.hasRead();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Group> hasRead() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.READ
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
  } 

  /**
   * Report whether this member has READ on the specified group.
   * <pre class="eg">
   * // Check whether this member has READ on the specified group.
   * if (m.hasRead(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasRead(Group g) {
    return this._hasPriv(g, AccessPrivilege.READ);
  } // public boolean _hasPriv(g)

  /**
   * Get stems where this member has the STEM privilege.
   * <pre class="eg">
   * Set stem = m.hasStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Stem> hasStem()
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = GrouperSession.staticGrouperSession().getNamingResolver().getStemsWhereSubjectHasPrivilege( this.getSubject(), NamingPrivilege.STEM );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage() );
    }
    return privs;
  } // public Set hasStem()

  /**
   * Report whether this member has STEM on the specified stem.
   * <pre class="eg">
   * if (m.hasStem(ns)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   ns  Test for privilege on this {@link Stem}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasStem(Stem ns) {
    return this._hasPriv(ns, NamingPrivilege.STEM);
  } // public boolean hasStem(ns)

  /**
   * Get groups where this member has the UPDATE privilege.
   * <pre class="eg">
   * Set update = m.hasUpdate();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set hasUpdate() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.UPDATE
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
  } 

  /**
   * Report whether this member has UPDATE on the specified group.
   * <pre class="eg">
   * // Check whether this member has UPDATE on the specified group.
   * if (m.hasUpdate(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasUpdate(Group g) {
    return this._hasPriv(g, AccessPrivilege.UPDATE);
  } // public boolean hasUpdate(g)

  /**
   * Get groups where this member has the VIEW privilege.
   * <pre class="eg">
   * Set view = m.hasView();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Group> hasView() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.VIEW
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
  } 

  /**
   * Report whether this member has VIEW on the specified group.
   * <pre class="eg">
   * // Check whether this member has VIEW on the specified group.
   * if (m.hasView(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasView(Group g) {
    return this._hasPriv(g, AccessPrivilege.VIEW);
  } // public boolean hasview(g)

  /**
   * Test whether a member effectively belongs to a group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (m.isEffectiveMember(g)) {
   *   // Is an effective member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @return  Boolean true if is a member.
   * @throws  GrouperRuntimeException
   */
  public boolean isEffectiveMember(Group g) 
    throws  GrouperRuntimeException
  {
    try {
      return this.isEffectiveMember(g, Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal( msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public boolean isEffectiveMember(g);

  /**
   * Test whether a member effectively belongs to a group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * // Does this member effectively belong to the specified group?
   * if (m.isEffectiveMember(g, f)) {
   *   // Is an effective member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @param   f   Test for membership in this list field.
   * @return  Boolean true if is a member.
   * @throws  SchemaException
   */
  public boolean isEffectiveMember(Group g, Field f) 
    throws  SchemaException
  {
    MembershipDAO dao = GrouperDAOFactory.getFactory().getMembership();    
    boolean       rv  = false;
    if ( dao.findAllEffectiveByOwnerAndMemberAndField( g.getUuid(), this.getUuid(), f ).size() > 0 ) {
      rv = true;
    }
    else if (
      dao.findAllEffectiveByOwnerAndMemberAndField( g.getUuid(), MemberFinder.internal_findAllMember().getUuid(), f ).size() > 0
    ) 
    {
      rv = true;
    }
    return rv;
  }

  /**
   * Test whether a member immediately belongs to a group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (m.isImmediateMember(g)) {
   *   // Is an immediate member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @return  Boolean true if is a member.
   * @throws  GrouperRuntimeException
   */
  public boolean isImmediateMember(Group g) 
    throws  GrouperRuntimeException
  {
    try {
      return this.isImmediateMember(g, Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal( msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public boolean isImmediateMember(g)

  /**
   * Test whether a member immediately belongs to a group.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * // Does this member immediately belong to the specified group?
   * if (m.isImmediateMember(g, f)) {
   *   // Is an immediate member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @param   f   Test for memberhip in this list field.
   * @return  Boolean true if is a member.
   * @throws  SchemaException
   */
  public boolean isImmediateMember(Group g, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    try {
      Subject subj = this.getSubject();
      try {
        MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), g, subj, f);
        rv = true;
      }
      catch (MembershipNotFoundException eMNF) {
        try {
          GrouperDAOFactory.getFactory().getMembership().findByOwnerAndMemberAndFieldAndType(
            g.getUuid(), MemberFinder.internal_findAllMember().getUuid(), f, Membership.IMMEDIATE
          );
          rv = true;
        }
        catch (MembershipNotFoundException anotherMNF) {
          // ignore
        }
      }
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return rv;
  } // public boolean isImmediateMember(g, f)

  /**
   * Test whether a member belongs to a group.   
   * 
   * A member of a group (aka composite member) has either or both of
   * an immediate (direct) membership, or an effective (indirect) membership
   * 
   * All immediate subjects, and effective members are members.  
   * 
   * <pre class="eg">
   * if (m.isMember(g)) {
   *   // Is a member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @return  Boolean true if is a member.
   * @throws  GrouperRuntimeException
   */
  public boolean isMember(Group g) 
  {
    try {
      return this.isMember( g, Group.getDefaultList() );
    }
    catch (SchemaException eShouldNeverHappen) {
      // If we don't have "members" we have serious issues
      String msg = "this should never happen: default group list not found: " + eShouldNeverHappen.getMessage();
      LOG.fatal( msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen);
    }
  } // public boolean isMember(g)

  /**
   * Test whether a member belongs to the specified group list.  A member of a group (aka composite member) has either or both of
   * an immediate (direct) membership, or an effective (indirect) membership.
   * 
   * All immediate subjects, and effective members are members.  
   * 
   * <p/>
   * <pre class="eg">
   * // Does this member belong to the specified group?
   * if (m.isMember(g, f)) {
   *   // Is a member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @param   f   Test for membership in this list {@link Field}.
   * @return  Boolean true if is a member.
   * @throws  SchemaException
   */
  public boolean isMember(Group g, Field f) 
    throws  SchemaException
  {
    return this.isMember( g.getUuid(), f );
  } // public boolean isMember(g, f)

  /**
   * simple set subject id
   * @param id
   */
  public void setSubjectIdDb(String id) {
    this.subjectID = id;
  }
  
  /**
   * Change subject id associated with member.
   * <p>
   * You must be a root-like {@link Subject} to use this method.
   * </p>
   * <pre class="eg">
   * try {
   *   m.setSubjectId("new id");
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to change subject id
   * }
   * </pre>
   * @param   id  Set subject id to this.
   * @throws  IllegalArgumentException
   * @throws  InsufficientPrivilegeException
   */
  public void setSubjectId(String id) 
    throws  IllegalArgumentException,
            InsufficientPrivilegeException
  {
    StopWatch sw    = new StopWatch();
    sw.start();
    GrouperValidator v = NotNullValidator.validate(id);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( v.getErrorMessage() );
    }
    v = MemberModifyValidator.validate(this);
    if (v.isInvalid()) {
      throw new InsufficientPrivilegeException( v.getErrorMessage() );
    }
    String    orig  = this.getSubjectId(); // preserve original for logging purposes
    this.subjectID = id;

    if (!GrouperConfig.getPropertyBoolean(GrouperConfig.PROP_SETTERS_DONT_CAUSE_QUERIES, false)) {
      GrouperDAOFactory.getFactory().getMember().update( this );
    }
    sw.stop();
    EventLog.info(
      GrouperSession.staticGrouperSession(),
      M.MEMBER_CHANGESID + Quote.single(this.getUuid()) + " old=" + Quote.single(orig) + " new=" + Quote.single(id),
      sw
    );
  } // public void setSubjectId(id)

  /**
   * will be implemented soon
   */
  public void store() {
    if (GrouperConfig.getPropertyBoolean(GrouperConfig.PROP_SETTERS_DONT_CAUSE_QUERIES, false)) {
      GrouperDAOFactory.getFactory().getMember().update( this );
    }
  }
  
  /**
   * simple set subject source id
   * @param id
   */
  public void setSubjectSourceIdDb(String id) {
    this.subjectSourceID = id;
  }
  
  /**
   * Change subject source id associated with member.
   * <p>
   * You must be a root-like {@link Subject} to use this method.
   * </p>
   * <pre class="eg">
   * try {
   *   m.setSubjectSourceId("new source id");
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to change subject source id
   * }
   * </pre>
   * @param   id  Set subject source id to this.
   * @throws  IllegalArgumentException
   * @throws  InsufficientPrivilegeException
   * @since   1.1.0
   */
  public void setSubjectSourceId(String id) 
    throws  IllegalArgumentException,
            InsufficientPrivilegeException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    GrouperValidator v = NotNullValidator.validate(id);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( v.getErrorMessage() );
    }
    v = MemberModifyValidator.validate(this);
    if (v.isInvalid()) {
      throw new InsufficientPrivilegeException( v.getErrorMessage() );
    }
    String    orig  = this.getSubjectSourceId();
    this.subjectSourceID = id;
    if (!GrouperConfig.getPropertyBoolean(GrouperConfig.PROP_SETTERS_DONT_CAUSE_QUERIES, false)) {
      GrouperDAOFactory.getFactory().getMember().update( this );
    }
    sw.stop();
    EventLog.info(
      GrouperSession.staticGrouperSession(),
      M.MEMBER_CHANGE_SSID + Quote.single(this.getUuid()) + " old=" + Quote.single(orig) + " new=" + Quote.single(id),
      sw
    );
  } // public void setSubjectSourceId(id)

  /**
   * Convert this member back to a {@link Group} object.
   * <p/>
   * <pre class="eg">
   * try {
   *   Group g = m.toGroup();
   * }
   * catch (GroupNotFoundException e) {
   *   // unable to convert member back to group
   * }
   * </pre>
   * @return  {@link Member} as a {@link Group}
   * @throws GroupNotFoundException 
   */
  public Group toGroup() 
    throws GroupNotFoundException 
  {
    if ( SubjectFinder.internal_getGSA().getId().equals( this.getSubjectSourceId() ) ) {
      if (this.g == null) {
        this.g = GroupFinder.findByUuid( GrouperSession.staticGrouperSession(), this.getSubjectId() );
      }
      return this.g;
    }
    throw new GroupNotFoundException("member is not a group");
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return SubjectHelper.getPretty( this );
  } // public String toString()


  /**
   * @param ownerUUID
   * @param f
   * @return if is member
   */
  public boolean isMember(String ownerUUID, Field f) {
    boolean       rv      = false;
    MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();
    Set           mships  = dao.findAllByOwnerAndMemberAndField(ownerUUID, this.getUuid(), f);
    if (mships.size() > 0) {
      rv = true;
    }
    else {
      Member all = MemberFinder.internal_findAllMember();
      if ( !this.equals(all) ) {
        mships = dao.findAllByOwnerAndMemberAndField( ownerUUID, all.getUuid(), f);
        if (mships.size() > 0) {
          rv = true;
        }
      }
    }
    return rv;
  } // protected boolean isMember(ownerUUID, f);


  /**
   * @param it
   * @return groups
   */
  private Set<Group> _getGroups(Iterator it) {
    Group       g;
    Set         groups  = new LinkedHashSet();
    Membership  ms;
    while (it.hasNext()) {
      ms = (Membership) it.next();
      try {
        g = ms.getGroup();
        groups.add(g);  
      }
      catch (GroupNotFoundException eGNF) {
        LOG.error(
          E.MEMBER_NOGROUP + Quote.single(this.getUuid()) + " membership="
          + Quote.single(ms.getUuid()) + " " + eGNF.getMessage()
        );
      }
    }
    return groups;
  } // private Set _getGroups(it)

  /**
   * 
   * @param g
   * @param priv
   * @return if has priv
   */
  private boolean _hasPriv(Group g, Privilege priv) {
    boolean rv = false;
    try {
      rv = GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege( g, this.getSubject(), priv );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return rv;
  } 

  /**
   * 
   * @param ns
   * @param priv
   * @return if has priv
   */
  private boolean _hasPriv(Stem ns, Privilege priv) {
    boolean rv = false;
    try {
      rv = GrouperSession.staticGrouperSession().getNamingResolver().hasPrivilege( ns, this.getSubject(), priv );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return rv;
  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Member)) {
      return false;
    }
    Member that = (Member) other;
    return new EqualsBuilder()
      .append( this.subjectID,       that.subjectID       )
      .append( this.subjectSourceID, that.subjectSourceID )
      .append( this.subjectTypeID,   that.subjectTypeID   )
      .isEquals();
  } // public boolean equals(other)

  /**
   * 
   * @return subject id
   */
  public String getSubjectId() {
    return this.subjectID;
  }

  /**
   * 
   * @return subject id db
   */
  public String getSubjectIdDb() {
    return this.subjectID;
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.subjectID      )
      .append( this.subjectSourceID)
      .append( this.subjectTypeID   )
      .toHashCode();
  } // public int hashCode()

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onDelete(org.hibernate.Session)
   */
  @Override
  public boolean onDelete(Session hs) 
    throws  CallbackException
  {
    GrouperDAOFactory.getFactory().getMember().existsCachePut( this.getUuid(), false );
    GrouperDAOFactory.getFactory().getMember().uuid2dtoCacheRemove( this.getUuid() );
    return Lifecycle.NO_VETO;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onSave(org.hibernate.Session)
   */
  @Override
  public boolean onSave(Session hs) 
    throws  CallbackException
  {
    GrouperDAOFactory.getFactory().getMember().existsCachePut( this.getUuid(), true );
    return Lifecycle.NO_VETO;
  }

  /**
   * 
   * @param subjectTypeID
   */
  public void setSubjectTypeId(String subjectTypeID) {
    this.subjectTypeID = subjectTypeID;
  }

  /**
   * 
   * @param memberUUID
   */
  public void setUuid(String memberUUID) {
    this.memberUUID = memberUUID;
  }

  /**
   * 
   * @return string
   */
  public String toStringDto() {
    return new ToStringBuilder(this)
      .append( "subjectId",       this.getSubjectId()       )
      .append( "subjectSourceId", this.getSubjectSourceId() )
      .append( "subjectTypeId",   this.getSubjectTypeId()   )
      .append( "uuid",            this.getUuid()            )
      .toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {

    super.onPostDelete(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_POST_COMMIT_DELETE, HooksMemberBean.class, 
        this, Member.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_POST_DELETE, HooksMemberBean.class, 
        this, Member.class, VetoTypeGrouper.MEMBER_POST_DELETE, false, true);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {

    super.onPostSave(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_POST_COMMIT_INSERT, HooksMemberBean.class, 
        this, Member.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_POST_INSERT, HooksMemberBean.class, 
        this, Member.class, VetoTypeGrouper.MEMBER_POST_INSERT, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {

    super.onPostUpdate(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_POST_COMMIT_UPDATE, HooksMemberBean.class, 
        this, Member.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_POST_UPDATE, HooksMemberBean.class, 
        this, Member.class, VetoTypeGrouper.MEMBER_POST_UPDATE, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_PRE_DELETE, HooksMemberBean.class, 
        this, Member.class, VetoTypeGrouper.MEMBER_PRE_DELETE, false, false);
  
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_PRE_INSERT, HooksMemberBean.class, 
        this, Member.class, VetoTypeGrouper.MEMBER_PRE_INSERT, false, false);
  
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_PRE_UPDATE, HooksMemberBean.class, 
        this, Member.class, VetoTypeGrouper.MEMBER_PRE_UPDATE, false, false);
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public Member dbVersion() {
    return (Member)this.dbVersion;
  }

  /**
   * note, these are massaged so that name, extension, etc look like normal fields.
   * access with fieldValue()
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

  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public Member clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }


} 

