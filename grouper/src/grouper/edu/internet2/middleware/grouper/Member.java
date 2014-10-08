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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignMemberDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.MemberHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMemberChangeSubjectBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperId;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.subj.SubjectBean;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.MemberModifyValidator;
import edu.internet2.middleware.grouper.validator.NotNullValidator;
import edu.internet2.middleware.grouper.xml.export.XmlExportMember;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;
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
 * @version $Id: Member.java,v 1.135 2009-12-28 06:08:37 mchyzer Exp $
 */
public class Member extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, 
    Comparable<Member>, XmlImportable<Member>, AttributeAssignable, GrouperId {

  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeAssignMemberDelegate attributeAssignMemberDelegate;
  
  /**
   * resolve subjects in one batch
   * @param members
   * @param resolveAllAlways true to always resolve all no matter how many, false
   * if there are more than 2000 or however many (e.g. for UI)
   */
  public static void resolveSubjects(Collection<Member> members, boolean resolveAllAlways) {
    
    if (GrouperUtil.length(members) == 0) {
      return;
    }
    
    //find the ones which are Lazy
    List<Member> membersNeedResolved = new ArrayList<Member>();
    for (Member member : members) {
      if (member.subj == null || member.subj instanceof LazySubject) {
        membersNeedResolved.add(member);
      }
    }
    
    if (GrouperUtil.length(membersNeedResolved) == 0) {
      return;
    }

    //if there are more than 2000, forget it, leave them lazy
    if (!resolveAllAlways 
        && GrouperUtil.length(membersNeedResolved) > GrouperConfig.retrieveConfig().propertyValueInt("memberLengthAboveWhichDontResolveBatch", 2000)) {
      return;
    }
    
    //lets resolve these
    Map<SubjectBean, Member> subjectBeanToMember = new HashMap<SubjectBean, Member>();
    Set<SubjectBean> subjectBeans = new HashSet<SubjectBean>();
    
    for (Member member : membersNeedResolved) {
      
      SubjectBean subjectBean = new SubjectBean(member.getSubjectId(), member.getSubjectSourceId());
      subjectBeans.add(subjectBean);
      subjectBeanToMember.put(subjectBean, member);
      
    }
    
    //resolve all members
    Map<SubjectBean, Subject> subjectBeanToSubject = SubjectFinder.findBySubjectBeans(subjectBeans);
    
    for (SubjectBean subjectBean : subjectBeanToSubject.keySet()) {
      Subject subject = subjectBeanToSubject.get(subjectBean);
      Member member = subjectBeanToMember.get(subjectBean);
      member.subj = subject;
    }
  }
  
  /**
   * 
   * @return the delegate
   */
  public AttributeAssignMemberDelegate getAttributeDelegate() {
    if (this.attributeAssignMemberDelegate == null) {
      this.attributeAssignMemberDelegate = new AttributeAssignMemberDelegate(this);
    }
    return this.attributeAssignMemberDelegate;
  }

  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeValueDelegate attributeValueDelegate;
  
  /**
   * this delegate works on attributes and values at the same time
   * @return the delegate
   */
  public AttributeValueDelegate getAttributeValueDelegate() {
    if (this.attributeValueDelegate == null) {
      this.attributeValueDelegate = new AttributeValueDelegate(this.getAttributeDelegate());
    }
    return this.attributeValueDelegate;
  }
  
  /**
   * print out a collection of members
   * @param collection
   * @return the subject ids comma separated
   */
  public static String subjectIds(Collection<Member> collection) {
    StringBuilder result = new StringBuilder();
    for (Member member : GrouperUtil.nonNull(collection)) {
      result.append(member.getSubjectId()).append(", ");
    }
    if (result.length() >= 2) {
      //take off the last comma and space
      result.delete(result.length()-2, result.length());
    }
    return result.toString();
  }

  /** constant for property name for: subjectId */
  public static final String PROPERTY_SUBJECT_ID = "subjectId";

  /** grouper_members table in the DB */
  public static final String TABLE_GROUPER_MEMBERS = "grouper_members";

  /** uuid col in db */
  public static final String COLUMN_MEMBER_UUID = "member_uuid";
  
  /** new uuid col in db */
  public static final String COLUMN_ID = "id";
  
  /** old id col for id conversion */
  public static final String COLUMN_OLD_ID = "old_id";
  
  /** old uuid id col for id conversion */
  public static final String COLUMN_OLD_MEMBER_UUID = "old_member_uuid";
  
  /** sortString0 */
  public static final String COLUMN_SORT_STRING0 = "sort_string0";

  /** sortString1 */
  public static final String COLUMN_SORT_STRING1 = "sort_string1";
  
  /** sortString2 */
  public static final String COLUMN_SORT_STRING2 = "sort_string2";
  
  /** sortString3 */
  public static final String COLUMN_SORT_STRING3 = "sort_string3";
  
  /** sortString4 */
  public static final String COLUMN_SORT_STRING4 = "sort_string4";
  
  /** searchString0 */
  public static final String COLUMN_SEARCH_STRING0 = "search_string0";

  /** searchString1 */
  public static final String COLUMN_SEARCH_STRING1 = "search_string1";

  /** searchString2 */
  public static final String COLUMN_SEARCH_STRING2 = "search_string2";

  /** searchString3 */
  public static final String COLUMN_SEARCH_STRING3 = "search_string3";

  /** searchString4 */
  public static final String COLUMN_SEARCH_STRING4 = "search_string4";

  /** name */
  public static final String COLUMN_NAME = "name";
  
  /** column */
  public static final String COLUMN_SUBJECT_ID = "subject_id";
  
  /** column */
  public static final String COLUMN_SUBJECT_SOURCE = "subject_source";

  /** column */
  public static final String COLUMN_SUBJECT_TYPE = "subject_type";
  
  /** description */
  public static final String COLUMN_DESCRIPTION = "description";
  
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

  /** constant for field name for: sortString0 */
  public static final String FIELD_SORT_STRING0 = "sortString0";

  /** constant for field name for: sortString1 */
  public static final String FIELD_SORT_STRING1 = "sortString1";
  
  /** constant for field name for: sortString2 */
  public static final String FIELD_SORT_STRING2 = "sortString2";
  
  /** constant for field name for: sortString3 */
  public static final String FIELD_SORT_STRING3 = "sortString3";
  
  /** constant for field name for: sortString4 */
  public static final String FIELD_SORT_STRING4 = "sortString4";
  
  /** constant for field name for: searchString0 */
  public static final String FIELD_SEARCH_STRING0 = "searchString0";
  
  /** constant for field name for: searchString1 */
  public static final String FIELD_SEARCH_STRING1 = "searchString1";
  
  /** constant for field name for: searchString2 */
  public static final String FIELD_SEARCH_STRING2 = "searchString2";
  
  /** constant for field name for: searchString3 */
  public static final String FIELD_SEARCH_STRING3 = "searchString3";
  
  /** constant for field name for: searchString4 */
  public static final String FIELD_SEARCH_STRING4 = "searchString4";
  
  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";
  
  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";
  
  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_MEMBER_UUID, FIELD_SUBJECT_ID, FIELD_SUBJECT_SOURCE_ID, FIELD_SUBJECT_TYPE_ID,
      FIELD_SORT_STRING0, FIELD_SORT_STRING1, FIELD_SORT_STRING2, FIELD_SORT_STRING3, FIELD_SORT_STRING4,
      FIELD_SEARCH_STRING0, FIELD_SEARCH_STRING1, FIELD_SEARCH_STRING2, FIELD_SEARCH_STRING3, FIELD_SEARCH_STRING4,
      FIELD_NAME, FIELD_DESCRIPTION);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DB_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_MEMBER_UUID, FIELD_SUBJECT_ID, 
      FIELD_SUBJECT_SOURCE_ID, FIELD_SUBJECT_TYPE_ID,
      FIELD_SORT_STRING0, FIELD_SORT_STRING1, FIELD_SORT_STRING2, FIELD_SORT_STRING3, FIELD_SORT_STRING4,
      FIELD_SEARCH_STRING0, FIELD_SEARCH_STRING1, FIELD_SEARCH_STRING2, FIELD_SEARCH_STRING3, FIELD_SEARCH_STRING4,
      FIELD_NAME, FIELD_DESCRIPTION);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  /** javabeans property for uuid */
  public static final String PROPERTY_UUID = "uuid";
  
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
  
  /** string that can be used to sort results */
  private String sortString0;
  
  /** string that can be used to sort results */
  private String sortString1;
  
  /** string that can be used to sort results */
  private String sortString2;
  
  /** string that can be used to sort results */
  private String sortString3;
  
  /** string that can be used to sort results */
  private String sortString4;
  
  /** string that can be used to filter results */
  private String searchString0;
  
  /** string that can be used to filter results */
  private String searchString1;
  
  /** string that can be used to filter results */
  private String searchString2;
  
  /** string that can be used to filter results */
  private String searchString3;
  
  /** string that can be used to filter results */
  private String searchString4;
  
  /** name of member -- helpful for unresolvable subjects */
  private String name;
  
  /** description of member -- helpful for unresolvable subjects */
  private String description;

  /** change a subject to the same subject (for testing) */
  public static int changeSubjectSameSubject = 0;
  
  /** change a subject to a subject which didnt exist */
  public static int changeSubjectDidntExist = 0;

  /** change a subject to a subject which did exist */
  public static int changeSubjectExist = 0;
  
  /** number of memberships edited */
  public static int changeSubjectMembershipAddCount = 0;
  
  /** number of memberships deleted due to duplicates */
  public static int changeSubjectMembershipDeleteCount = 0;
  
  
  /**
   * change the subject of a member to another subject.  This new subject might already exist, and it
   * might not.  If it doesnt exist, then this member object will have its subject and source information
   * updated.  If it does, then all objects in the system which use this member_id will be queried, and
   * the member_id updated to the new member_id, and the old member_id will be removed.
   * 
   * @param newSubject
   * @throws InsufficientPrivilegeException if not a root user
   */
  public void changeSubject(Subject newSubject) throws InsufficientPrivilegeException {
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
   * @throws InsufficientPrivilegeException if not a root user
   */
  public void changeSubject(final Subject newSubject, final boolean deleteOldMember) 
      throws InsufficientPrivilegeException {
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
   * @throws InsufficientPrivilegeException if not a root user
   */
  private void changeSubjectHelper(final Subject newSubject, final boolean deleteOldMember, 
      final StringBuilder report) throws InsufficientPrivilegeException {
    
    final String errorMessageSuffix = ", this subject: " + GrouperUtil.subjectToString(this.subj)
      + ", new subject: " + GrouperUtil.subjectToString(newSubject) + ", deleteOldMember: " + deleteOldMember
      + ", report? " + (report != null);

    
    //make sure root session
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    if (grouperSession == null  ||
        !PrivilegeHelper.isRoot(grouperSession)) {
      throw new InsufficientPrivilegeException("changeSubject requires GrouperSystem " +
      		"or wheel/sysAdmin group grouperSession");
    }
    
    final String thisSubjectId = this.getSubjectId();
    final String thisSourceId = this.getSubjectSourceId();
    final String thisMemberId = this.getUuid();
    
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
      theNewMember = GrouperDAOFactory.getFactory().getMember().findBySubject(newSubject, true);
    } catch (MemberNotFoundException mnfe) {
      theMemberDidntExist = true;
    }

    final boolean memberDidntExist = theMemberDidntExist;
    final Member newMember = theNewMember;
    
    AuditControl auditControl = report == null ? AuditControl.WILL_AUDIT : AuditControl.WILL_NOT_AUDIT;
    
    //this needs to run in a transaction
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, auditControl,
        new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        try {
          hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

          //hooks bean
          HooksMemberChangeSubjectBean hooksMemberChangeSubjectBean = new HooksMemberChangeSubjectBean(
              Member.this, newSubject, thisSubjectId, thisSourceId, deleteOldMember, memberDidntExist);
  
          if (report == null) {
            //call pre-hooks if registered
            GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.MEMBER, 
                MemberHooks.METHOD_MEMBER_PRE_CHANGE_SUBJECT, 
                hooksMemberChangeSubjectBean, VetoTypeGrouper.MEMBER_PRE_CHANGE_SUBJECT);
          }
          
          String newMemberUuid = null;
          
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
            newMemberUuid = newMember.getUuid();
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
                      group.setDontSetModified(true);
                    } else {
                      report.append("CHANGE group: " + group.getUuid() + ", " + group.getName()
                          + ", creator id FROM: " + group.getCreatorUuid() + ", TO: " + newMemberUuid + "\n");
                    }
                  }
                  if (StringUtils.equals(Member.this.getUuid(), group.getModifierUuid())) {
                    if (report == null) {
                      group.setModifierUuid(newMemberUuid);
                      group.setDontSetModified(true);
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
              Set<Membership> membershipsPrevious = GrouperDAOFactory.getFactory().getMembership().findAllByCreatorOrMember(Member.this, false);
              if (GrouperUtil.length(membershipsPrevious) > 0) {
                Set<Membership> membershipsNew = GrouperDAOFactory.getFactory().getMembership().findAllByMember(newMemberUuid, true);
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
                    GrouperDAOFactory.getFactory().getMembership().update(membershipsToUpdate);
                  }
                  if (GrouperUtil.length(membershipsToDelete) > 0) {
                    GrouperDAOFactory.getFactory().getMembership().delete(membershipsToDelete);
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
              //grouper_group_set.creator_id
              Set<GroupSet> groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllByCreator(Member.this);
              if (GrouperUtil.length(groupSets) > 0) {
                for (GroupSet gs : groupSets) {
                  if (report == null) {
                    gs.setCreatorId(newMemberUuid);
                  } else {
                    report.append("CHANGE groupSet: " 
                        + gs.getId() + ", creator id FROM: " + gs.getCreatorId() + ", TO: " + newMemberUuid + "\n");
                  }
                }
                if (report == null) {
                  GrouperDAOFactory.getFactory().getGroupSet().update(groupSets);
                }
              }
            }
            
            //finally delete this member object
            //TODO note, we should move over the attributes
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
            
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {

              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.MEMBER_CHANGE_SUBJECT, "oldMemberId", 
                  thisMemberId, "oldSubjectId", thisSubjectId,
                      "oldSourceId", thisSourceId, "newMemberId",  newMemberUuid,
                      "newSubjectId", newSubjectId, "newSourceId", newSourceId, 
                      "deleteOldMember", deleteOldMember ? "T" : "F", "memberIdChanged", (!memberDidntExist) ? "T" : "F");
                      
              auditEntry.setDescription("Member change subject: old subject: " + thisSourceId
                  + "." + thisSubjectId + ", new subject: " + newSourceId + "." + newSubjectId
                  + ", delete old member: " + deleteOldMember + ", member id changed: " + !memberDidntExist);
              auditEntry.saveOrUpdate(true);
            }
            
            hibernateSession.getSession().flush();          
          
            GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBER, 
                MemberHooks.METHOD_MEMBER_POST_COMMIT_CHANGE_SUBJECT, 
                hooksMemberChangeSubjectBean);
    
            GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.MEMBER, 
                MemberHooks.METHOD_MEMBER_POST_CHANGE_SUBJECT, 
                hooksMemberChangeSubjectBean, VetoTypeGrouper.MEMBER_POST_CHANGE_SUBJECT);
          }  
          
          return null;
        } catch (RuntimeException re) {
          GrouperUtil.injectInException(re, errorMessageSuffix);
          throw re;
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
    return this.getEffectiveGroups(Group.getDefaultList());
  } // public Set getEffectiveGroups()

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
   * @param field
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getEffectiveGroups(Field field) {
    return this.getEffectiveGroups(field, null, null, null, null, true);
  }

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
   * @throws  GrouperException
   */
  public Set<Membership> getEffectiveMemberships() 
    throws  GrouperException
  {
    try {
      return this.getEffectiveMemberships(Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
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
    return this.getGroups(Group.getDefaultList());
  } // public Set getGroups()

  /**
   * Get groups where this member is a member.
   * <pre class="eg">
   * // Get groups where this member is a member.
   * Set groups = m.getGroups();
   * </pre>
   * @param field to check, doesnt have to be list field
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getGroups(Field field) {
    return this.getGroups(field, null, null, null, null, true);
  } // public Set getGroups()

  /**
   * Get groups where this member is a member.
   * <pre class="eg">
   * // Get groups where this member is a member.
   * Set groups = m.getGroups();
   * </pre>
   * @param field to check, doesnt have to be list field
   * @param membershipType immediate, effective, non immediate, etc, or null for all
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param stem is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param queryOptions is what to sort/page to or null for all.  Can sort on name, displayName, extension, displayExtension
   * @param enabled is null for all, true for enabled only, false for enabled and disabled
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> _internal_getGroupsHelper(Field field, MembershipType membershipType, 
      String scope, Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    try {
      return GrouperDAOFactory.getFactory().getGroup().getAllGroupsMembershipSecure(
          field, scope, grouperSession, this.getSubject(), queryOptions, enabled, membershipType, stem, stemScope);
    } catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = "problem retrieving groups for member: " + this.subjectID + ", " + this.subjectSourceID + ", " 
        + GrouperUtil.toStringSafe(field) + ", " + membershipType + ", " + scope + ", " + stem + ", " 
        + stemScope + ", " + queryOptions + ", " + enabled;
      LOG.fatal( msg);
      throw new GrouperException(msg, eS);
    }
      
  } // public Set getGroupsHelper()

  /**
   * Get groups where this member is a member.
   * @param field to check, doesnt have to be list field
   * @param scope is a DB pattern that will have % appended to it, or null for all
   * @param stem is the stem to check in, or null if all
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param queryOptions is what to sort/page to or null for all.  can sort on name, displayName, extension, displayExtension
   * @param enabled is null for all, true for enabled only, false for enabled and disabled
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getGroups(Field field, 
      String scope, Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled) {
    return _internal_getGroupsHelper(field, null, scope, stem, stemScope, queryOptions, enabled);
  } 

  /**
   * Get groups where this member is an effective member.
   * @param field to check, doesnt have to be list field
   * @param scope is a DB pattern that will have % appended to it, or null for all
   * @param stem is the stem to check in, or null if all
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param queryOptions is what to sort/page to or null for all, can sort on name, displayName, extension, displayExtension
   * @param enabled is null for all, true for enabled only, false for enabled and disabled
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getEffectiveGroups(Field field, 
      String scope, Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled) {
    return _internal_getGroupsHelper(field, MembershipType.EFFECTIVE, scope, stem, stemScope, queryOptions, enabled);
  } 

  /**
   * Get groups where this member is an immediate member.
   * @param field to check, doesnt have to be list field
   * @param scope is a DB pattern that will have % appended to it, or null for all
   * @param stem is the stem to check in, or null if all
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param queryOptions is what to sort/page to or null for all.  can sort on name, displayName, extension, displayExtension
   * @param enabled is null for all, true for enabled only, false for enabled and disabled
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getImmediateGroups(Field field, 
      String scope, Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled) {
    return _internal_getGroupsHelper(field, MembershipType.IMMEDIATE, scope, stem, stemScope, queryOptions, enabled);
  } 

  /**
   * Get groups where this member is a nonimmediate member.
   * @param field to check, doesnt have to be list field
   * @param scope is a DB pattern that will have % appended to it, or null for all
   * @param stem is the stem to check in, or null if all
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param queryOptions is what to sort/page to or null for all.  can sort on name, displayName, extension, displayExtension
   * @param enabled is null for all, true for enabled only, false for enabled and disabled
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getNonImmediateGroups(Field field, 
      String scope, Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled) {
    return _internal_getGroupsHelper(field, MembershipType.NONIMMEDIATE, scope, stem, stemScope, queryOptions, enabled);
  } 


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
    return this.getImmediateGroups(Group.getDefaultList());
  } // public Set getImmediateGroups()

  /**
   * Get groups where this member has a non immediate membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   *
   * <pre class="eg">
   * // Get groups where this member is a non immediate member.
   * Set nonImmediates = m.getNonImmediateGroups();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getNonImmediateGroups() {
    return this.getNonImmediateGroups(Group.getDefaultList());
  } // public Set getImmediateGroups()

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
   * @param field 
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getImmediateGroups(Field field) {
    
    return this.getImmediateGroups(field, null, null, null, null, true);

  }

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
   * @throws  GrouperException
   */
  public Set<Membership> getImmediateMemberships() 
    throws  GrouperException
  {
    try {
      return this.getImmediateMemberships(Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal( msg);
      throw new GrouperException(msg, eS);
    }
  } // public Set getImmediateMemberships()

  /**
   * Get non-immediate memberships.  
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
   * Set immediates = m.getNonImmediateMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   * @throws  GrouperException
   */
  public Set<Membership> getNonImmediateMemberships() 
    throws  GrouperException
  {
    try {
      return this.getNonImmediateMemberships(Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal( msg);
      throw new GrouperException(msg, eS);
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
   * Get non-immediate memberships.  
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
   * Set immediates = m.getNonImmediateMemberships(f);
   * </pre>
   * @param   f   Get non-immediate memberships in this list field.
   * @return  Set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getNonImmediateMemberships(Field f) 
    throws  SchemaException {
    return MembershipFinder.internal_findAllNonImmediateByMemberAndField( GrouperSession.staticGrouperSession(), this, f );
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
   * @throws  GrouperException
   */
  public Set<Membership> getMemberships() 
    throws  GrouperException
  {
    try {
      return this.getMemberships(Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal( msg);
      throw new GrouperException(msg, eS);
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
    return getMemberships(f, true);
  }

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
   * @param requireListField if list field must be a list field, and not privilege
   * @return  Set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getMemberships(Field f, boolean requireListField) 
    throws  SchemaException
  {
    if (requireListField && !f.getType().equals(FieldType.LIST)) {
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
   * @throws  GrouperException
   */ 
  public Source getSubjectSource() 
    throws  GrouperException
  {
    try {
      return this.getSubject().getSource();
    }
    catch (SubjectNotFoundException eSNF) {
      String msg = E.MEMBER_SUBJNOTFOUND + eSNF.getMessage();
      LOG.fatal( msg);
      throw new GrouperException(msg, eSNF);
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
   * get a members id
   * @return the id
   */
  public String getId() {
    return this.getUuid();
  }
  
  /**
   * Get groups where this member has the ADMIN privilege.
   * <pre class="eg">
   * Set admin = m.hasAdmin();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperException
   */
  public Set<Group> hasAdmin() 
    throws  GrouperException {
    Set<Group> groups = new LinkedHashSet();
    try {
      groups = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.ADMIN
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return groups;
  } 

  /**
   * Get stems where this member has the ADMIN privilege of a group inside.
   * <pre class="eg">
   * Set<Stem> adminInStem = m.hasAdminInStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperException
   */
  public Set<Stem> hasAdminInStem() 
      throws  GrouperException {
    Set<Stem> stems = new LinkedHashSet();
    try {
      stems = GrouperSession.staticGrouperSession().getAccessResolver().getStemsWhereGroupThatSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.ADMIN
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return stems;
  } 
  
  /**
   * Get stems where this member has the GROUP_ATTR_READ privilege of a group inside.
   * <pre class="eg">
   * Set<Stem> results = m.hasGroupAttrReadInStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperException
   */
  public Set<Stem> hasGroupAttrReadInStem() 
      throws  GrouperException {
    Set<Stem> stems = new LinkedHashSet();
    try {
      stems = GrouperSession.staticGrouperSession().getAccessResolver().getStemsWhereGroupThatSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.GROUP_ATTR_READ
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return stems;
  } 
  
  /**
   * Get stems where this member has the GROUP_ATTR_UPDATE privilege of a group inside.
   * <pre class="eg">
   * Set<Stem> results = m.hasGroupAttrUpdateInStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperException
   */
  public Set<Stem> hasGroupAttrUpdateInStem() 
      throws  GrouperException {
    Set<Stem> stems = new LinkedHashSet();
    try {
      stems = GrouperSession.staticGrouperSession().getAccessResolver().getStemsWhereGroupThatSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.GROUP_ATTR_UPDATE
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return stems;
  } 

  /**
   * Get stems where this member has the OPTOUT privilege of a group inside.
   * <pre class="eg">
   * Set<Stem> optoutInStem = m.hasOptoutInStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperException
   */
  public Set<Stem> hasOptoutInStem() 
      throws  GrouperException {
    Set<Stem> stems = new LinkedHashSet();
    try {
      stems = GrouperSession.staticGrouperSession().getAccessResolver().getStemsWhereGroupThatSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.OPTOUT
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return stems;
  } 

  /**
   * Get stems where this member has the OPTIN privilege of a group inside.
   * <pre class="eg">
   * Set<Stem> optinInStem = m.hasOptinInStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperException
   */
  public Set<Stem> hasOptinInStem() 
      throws  GrouperException {
    Set<Stem> stems = new LinkedHashSet();
    try {
      stems = GrouperSession.staticGrouperSession().getAccessResolver().getStemsWhereGroupThatSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.OPTIN
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return stems;
  } 


  /**
   * Get stems where this member has the VIEW privilege of a group inside.
   * <pre class="eg">
   * Set<Stem> viewInStem = m.hasViewInStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperException
   */
  public Set<Stem> hasViewInStem() 
      throws  GrouperException {
    Set<Stem> stems = new LinkedHashSet();
    try {
      stems = GrouperSession.staticGrouperSession().getAccessResolver().getStemsWhereGroupThatSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.VIEW
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return stems;
  } 


  /**
   * Get stems where this member has the READ privilege of a group inside.
   * <pre class="eg">
   * Set<Stem> readInStem = m.hasReadInStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperException
   */
  public Set<Stem> hasReadInStem() 
      throws  GrouperException {
    Set<Stem> stems = new LinkedHashSet();
    try {
      stems = GrouperSession.staticGrouperSession().getAccessResolver().getStemsWhereGroupThatSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.READ
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return stems;
  } 

  /**
   * Get stems where this member has the UPDATE privilege of a group inside.
   * <pre class="eg">
   * Set<Stem> updateInStem = m.hasUpdateInStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperException
   */
  public Set<Stem> hasUpdateInStem() 
      throws  GrouperException {
    Set<Stem> stems = new LinkedHashSet();
    try {
      stems = GrouperSession.staticGrouperSession().getAccessResolver().getStemsWhereGroupThatSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.UPDATE
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return stems;
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
   * @throws  GrouperException
   */
  public Set<Stem> hasCreate() 
    throws  GrouperException
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
   * @throws  GrouperException
   */
  public Set<Group> hasOptin() 
    throws  GrouperException
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
   * @throws  GrouperException
   */
  public Set<Group> hasOptout() 
    throws  GrouperException
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
   * @throws  GrouperException
   */
  public Set<Group> hasRead() 
    throws  GrouperException
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
   * @throws  GrouperException
   */
  public Set<Stem> hasStem()
    throws  GrouperException
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
   * @throws  GrouperException
   */
  public Set<Group> hasUpdate() 
    throws  GrouperException
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
   * @throws  GrouperException
   */
  public Set<Group> hasView() 
    throws  GrouperException
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
   * @throws  GrouperException
   */
  public boolean isEffectiveMember(Group g) 
    throws  GrouperException
  {
    try {
      return this.isEffectiveMember(g, Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal( msg);
      throw new GrouperException(msg, eS);
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
    if ( dao.findAllEffectiveByGroupOwnerAndMemberAndField(g.getUuid(), this.getUuid(), f, true).size() > 0) {
      rv = true;
    }
    else if (
      dao.findAllEffectiveByGroupOwnerAndMemberAndField(g.getUuid(), MemberFinder.internal_findAllMember().getUuid(), f, true).size() > 0
    ) {
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
   * @throws  GrouperException
   */
  public boolean isImmediateMember(Group g) 
    throws  GrouperException
  {
    try {
      return this.isImmediateMember(g, Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal( msg);
      throw new GrouperException(msg, eS);
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
        MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), g, subj, f, true);
        rv = true;
      }
      catch (MembershipNotFoundException eMNF) {
        try {
          GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
            g.getUuid(), MemberFinder.internal_findAllMember().getUuid(), f, MembershipType.IMMEDIATE.getTypeString(), true, true
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
  }

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
   * @throws  GrouperException
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
      throw new GrouperException(msg, eShouldNeverHappen);
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
    GrouperDAOFactory.getFactory().getMember().update( this );
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
        this.g = GroupFinder.findByUuid( GrouperSession.staticGrouperSession(), this.getSubjectId(), true );
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
    Set<Membership> mships  = null;
    if (f.isGroupListField()) {
      mships = dao.findAllByGroupOwnerAndMemberAndField(ownerUUID, this.getUuid(), f, true);
    } else if (f.isStemListField()) {
      mships = dao.findAllByStemOwnerAndMemberAndField(ownerUUID, this.getUuid(), f, true);
    } else if (f.isAttributeDefListField()) {
      mships = dao.findAllByAttrDefOwnerAndMemberAndField(ownerUUID, this.getUuid(), f, true);
    } else {
      throw new RuntimeException("Cant find type of owner: " + ownerUUID + ", " + f);
    }
    if (mships.size() > 0) {
      rv = true;
    }
    else {
      Member all = MemberFinder.internal_findAllMember();
      if ( !this.equals(all) ) {
        if (f.isGroupListField()) {
          mships = dao.findAllByGroupOwnerAndMemberAndField(ownerUUID, all.getUuid(), f, true);
        } else if (f.isStemListField()) {
          mships = dao.findAllByStemOwnerAndMemberAndField(ownerUUID, all.getUuid(), f, true);
        } else if (f.isAttributeDefListField()) {
          mships = dao.findAllByAttrDefOwnerAndMemberAndField(ownerUUID, all.getUuid(), f, true);
        } else {
          throw new RuntimeException("Cant find type of owner: " + ownerUUID + ", " + f);
        }
        if (mships.size() > 0) {
          rv = true;
        }
      }
    }
    return rv;
  } // protected boolean isMember(ownerUUID, f);



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
   * if has privilege
   * @param attributeDef
   * @param priv
   * @return if has priv
   */
  private boolean _hasPriv(AttributeDef attributeDef, Privilege priv) {
    boolean rv = false;
    try {
      rv = GrouperSession.staticGrouperSession().getAttributeDefResolver().hasPrivilege( attributeDef, this.getSubject(), priv );
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
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_POST_INSERT, HooksMemberBean.class, 
        this, Member.class, VetoTypeGrouper.MEMBER_POST_INSERT, true, false);

    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBER, 
        MemberHooks.METHOD_MEMBER_POST_COMMIT_INSERT, HooksMemberBean.class, 
        this, Member.class);

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
  
    // change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.MEMBER_DELETE, 
        ChangeLogLabels.MEMBER_DELETE.id.name(), this.getUuid(), 
        ChangeLogLabels.MEMBER_DELETE.subjectId.name(), this.getSubjectIdDb(), 
        ChangeLogLabels.MEMBER_DELETE.subjectSourceId.name(), this.getSubjectSourceIdDb(),
        ChangeLogLabels.MEMBER_DELETE.subjectTypeId.name(), this.getSubjectTypeId()).save();
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
    
    // change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.MEMBER_ADD, 
        ChangeLogLabels.MEMBER_ADD.id.name(), this.getUuid(), 
        ChangeLogLabels.MEMBER_ADD.subjectId.name(), this.getSubjectIdDb(), 
        ChangeLogLabels.MEMBER_ADD.subjectSourceId.name(), this.getSubjectSourceIdDb(),
        ChangeLogLabels.MEMBER_ADD.subjectTypeId.name(), this.getSubjectTypeId()).save();
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
    
    //change log into temp table
    ChangeLogEntry.saveTempUpdates(ChangeLogTypeBuiltin.MEMBER_UPDATE, 
        this, this.dbVersion(),
        GrouperUtil.toList(
            ChangeLogLabels.MEMBER_UPDATE.id.name(), this.getUuid(), 
            ChangeLogLabels.MEMBER_UPDATE.subjectId.name(), this.getSubjectIdDb(), 
            ChangeLogLabels.MEMBER_UPDATE.subjectSourceId.name(), this.getSubjectSourceIdDb(),
            ChangeLogLabels.MEMBER_UPDATE.subjectTypeId.name(), this.getSubjectTypeId()),
        GrouperUtil.toList("subjectId", "subjectSourceId", "subjectTypeId"),
        GrouperUtil.toList(
            ChangeLogLabels.MEMBER_UPDATE.subjectId.name(),
            ChangeLogLabels.MEMBER_UPDATE.subjectSourceId.name(),
            ChangeLogLabels.MEMBER_UPDATE.subjectTypeId.name()));    
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

  /** context id of the transaction */
  private String contextId;

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * Can this {@link Member} <b>ATTR_ADMIN</b> on this {@link AttributeDef}.
   * <pre class="eg">
   * boolean rv = m.canAttrAdmin(attributeDef);
   * </pre>
   * @param   attributeDef   Check privileges on this {@link AttributeDef}.
   * @return if admin
   * @throws  IllegalArgumentException if null {@link AttributeDef}
   * @since   1.0
   */
  public boolean canAttrAdmin(AttributeDef attributeDef) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(attributeDef);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canAttrAdmin( GrouperSession.staticGrouperSession(), attributeDef, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  }
  
  /**
   * Can this {@link Member} <b>ATTR_DEF_ATTR_READ</b> on this {@link AttributeDef}.
   * <pre class="eg">
   * boolean rv = m.canAttrDefAttrRead(attributeDef);
   * </pre>
   * @param   attributeDef   Check privileges on this {@link AttributeDef}.
   * @return true if allowed
   * @throws  IllegalArgumentException if null {@link AttributeDef}
   * @since   1.0
   */
  public boolean canAttrDefAttrRead(AttributeDef attributeDef) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(attributeDef);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canAttrDefAttrRead( GrouperSession.staticGrouperSession(), attributeDef, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  }
  
  /**
   * Can this {@link Member} <b>ATTR_DEF_ATTR_UPDATE</b> on this {@link AttributeDef}.
   * <pre class="eg">
   * boolean rv = m.canAttrDefAttrUpdate(attributeDef);
   * </pre>
   * @param   attributeDef   Check privileges on this {@link AttributeDef}.
   * @return true if allowed
   * @throws  IllegalArgumentException if null {@link AttributeDef}
   * @since   1.0
   */
  public boolean canAttrDefAttrUpdate(AttributeDef attributeDef) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(attributeDef);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canAttrDefAttrUpdate( GrouperSession.staticGrouperSession(), attributeDef, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  }
  
  /**
   * Can this {@link Member} <b>GROUP_ATTR_UPDATE</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canGroupAttrUpdate(group);
   * </pre>
   * @param   group   Check privileges on this {@link Group}.
   * @return true if allowed
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canGroupAttrUpdate(Group group) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(group);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canGroupAttrUpdate( GrouperSession.staticGrouperSession(), group, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  }
  
  /**
   * Can this {@link Member} <b>GROUP_ATTR_READ</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canGroupAttrRead(group);
   * </pre>
   * @param   group   Check privileges on this {@link Group}.
   * @return true if allowed
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canGroupAttrRead(Group group) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(group);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canGroupAttrRead( GrouperSession.staticGrouperSession(), group, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  }
  
  /**
   * Can this {@link Member} <b>STEM_ATTR_READ</b> on this {@link Stem}.
   * <pre class="eg">
   * boolean rv = m.canStemAttrRead(stem);
   * </pre>
   * @param   stem   Check privileges on this {@link Stem}.
   * @return true if allowed
   * @throws  IllegalArgumentException if null {@link Stem}
   * @since   1.0
   */
  public boolean canStemAttrRead(Stem stem) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(stem);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canStemAttrRead( GrouperSession.staticGrouperSession(), stem, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  }
  
  /**
   * Can this {@link Member} <b>STEM_ATTR_UPDATE</b> on this {@link Stem}.
   * <pre class="eg">
   * boolean rv = m.canStemAttrUpdate(stem);
   * </pre>
   * @param   stem   Check privileges on this {@link Stem}.
   * @return true if allowed
   * @throws  IllegalArgumentException if null {@link Stem}
   * @since   1.0
   */
  public boolean canStemAttrUpdate(Stem stem) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(stem);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canStemAttrUpdate( GrouperSession.staticGrouperSession(), stem, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  }

  /**
   * Can this {@link Member} <b>ATTR_OPTIN</b> on this {@link AttributeDef}.
   * <pre class="eg">
   * boolean rv = m.canAttrAdmin(attributeDef);
   * </pre>
   * @param   attributeDef   Check privileges on this {@link AttributeDef}.
   * @return if can optin
   * @throws  IllegalArgumentException if null {@link AttributeDef}
   * @since   1.0
   */
  public boolean canAttrOptin(AttributeDef attributeDef) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(attributeDef);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canAttrOptin( GrouperSession.staticGrouperSession(), attributeDef, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  }

  /**
   * Can this {@link Member} <b>ATTR_OPTOUT</b> on this {@link AttributeDef}.
   * <pre class="eg">
   * boolean rv = m.canAttrOptout(attributeDef);
   * </pre>
   * @param   attributeDef   Check privileges on this {@link AttributeDef}.
   * @return if can optout
   * @throws  IllegalArgumentException if null {@link AttributeDef}
   * @since   1.0
   */
  public boolean canAttrOptout(AttributeDef attributeDef) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(attributeDef);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canAttrOptout( GrouperSession.staticGrouperSession(), attributeDef, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  }

  /**
   * Can this {@link Member} <b>ATTR_READ</b> on this {@link AttributeDef}.
   * <pre class="eg">
   * boolean rv = m.canAttrRead(attributeDef);
   * </pre>
   * @param   attributeDef   Check privileges on this {@link AttributeDef}.
   * @return if can read
   * @throws  IllegalArgumentException if null {@link AttributeDef}
   * @since   1.0
   */
  public boolean canAttrRead(AttributeDef attributeDef)
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(attributeDef);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canAttrRead( GrouperSession.staticGrouperSession(), attributeDef, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  }

  /**
   * Can this {@link Member} <b>ATTR_UPDATE</b> on this {@link AttributeDef}.
   * <pre class="eg">
   * boolean rv = m.canAttrUpdate(attributeDef);
   * </pre>
   * @param   attributeDef   Check privileges on this {@link AttributeDef}.
   * @return if can update
   * @throws  IllegalArgumentException if null {@link AttributeDef}
   * @since   1.0
   */
  public boolean canAttrUpdate(AttributeDef attributeDef) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(attributeDef);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canAttrUpdate( GrouperSession.staticGrouperSession(), attributeDef, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } // public boolean canUPDATE(g)

  /**
   * Can this {@link Member} <b>ATTR_VIEW</b> on this {@link AttributeDef}.
   * <pre class="eg">
   * boolean rv = m.canAttrView(attributeDef);
   * </pre>
   * @param   attributeDef   Check privileges on this {@link AttributeDef}.
   * @return if can view
   * @throws  IllegalArgumentException if null {@link AttributeDef}
   * @since   1.0
   */
  public boolean canAttrView(AttributeDef attributeDef) 
    throws  IllegalArgumentException
  {
    NotNullValidator v = NotNullValidator.validate(attributeDef);
    if (v.isInvalid()) {
      throw new IllegalArgumentException(E.GROUP_NULL);
    }
    try {
      return PrivilegeHelper.canAttrView( GrouperSession.staticGrouperSession(), attributeDef, this.getSubject() );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  }

  /**
     * Get attributeDefs where this member has the ATTR_ADMIN privilege.
     * <pre class="eg">
     * Set<AttributeDef> admins = hasAttrAdmin();
     * </pre>
     * @return  Set of {@link AttributeDef} objects.
     * @throws  GrouperException
     */
    public Set<AttributeDef> hasAttrAdmin() 
      throws  GrouperException {
      Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();
      try {
        attributeDefs = GrouperSession.staticGrouperSession().getAttributeDefResolver().getAttributeDefsWhereSubjectHasPrivilege(
                  this.getSubject(), AttributeDefPrivilege.ATTR_ADMIN
                );
      }
      catch (SubjectNotFoundException eSNF) {
        LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
      }
      return attributeDefs;
    }

  /**
   * Report whether this member has ATTR_ADMIN on the specified attributeDef.
   * <pre class="eg">
   * // Check whether this member has ATTR_ADMIN on the specified attributeDef.
   * if (m.hasAttrAdmin(attributeDef)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   attributeDef   Test for privilege on this {@link AttributeDef}
   * @return true if the member has the privilege.
   */
  public boolean hasAttrAdmin(AttributeDef attributeDef) {
    return this._hasPriv(attributeDef, AttributeDefPrivilege.ATTR_ADMIN);
  } 
  
  /**
   * Report whether this member has GROUP_ATTR_READ on the specified group.
   * <pre class="eg">
   * // Check whether this member has GROUP_ATTR_READ on the specified group.
   * if (m.hasGroupAttrRead(group)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   group   Test for privilege on this {@link Group}
   * @return true if the member has the privilege.
   */
  public boolean hasGroupAttrRead(Group group) {
    return this._hasPriv(group, AccessPrivilege.GROUP_ATTR_READ);
  } 
  
  /**
   * Report whether this member has GROUP_ATTR_UPDATE on the specified group.
   * <pre class="eg">
   * // Check whether this member has GROUP_ATTR_UPDATE on the specified group.
   * if (m.hasGroupAttrUpdate(group)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   group   Test for privilege on this {@link Group}
   * @return true if the member has the privilege.
   */
  public boolean hasGroupAttrUpdate(Group group) {
    return this._hasPriv(group, AccessPrivilege.GROUP_ATTR_UPDATE);
  } 
  
  /**
   * Report whether this member has STEM_ATTR_UPDATE on the specified stem.
   * <pre class="eg">
   * // Check whether this member has STEM_ATTR_UPDATE on the specified stem.
   * if (m.hasStemAttrUpdate(stem)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   stem   Test for privilege on this {@link Stem}
   * @return true if the member has the privilege.
   */
  public boolean hasStemAttrUpdate(Stem stem) {
    return this._hasPriv(stem, NamingPrivilege.STEM_ATTR_UPDATE);
  } 
  
  /**
   * Report whether this member has STEM_ATTR_READ on the specified stem.
   * <pre class="eg">
   * // Check whether this member has STEM_ATTR_READ on the specified stem.
   * if (m.hasStemAttrRead(stem)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   stem   Test for privilege on this {@link Stem}
   * @return true if the member has the privilege.
   */
  public boolean hasStemAttrRead(Stem stem) {
    return this._hasPriv(stem, NamingPrivilege.STEM_ATTR_READ);
  } 
  
  /**
   * Report whether this member has ATTR_DEF_ATTR_READ on the specified attributeDef.
   * <pre class="eg">
   * // Check whether this member has ATTR_DEF_ATTR_READ on the specified attributeDef.
   * if (m.hasAttrDefAttrRead(stem)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   attributeDef   Test for privilege on this {@link AttributeDef}
   * @return true if the member has the privilege.
   */
  public boolean hasAttrDefAttrRead(AttributeDef attributeDef) {
    return this._hasPriv(attributeDef, AttributeDefPrivilege.ATTR_DEF_ATTR_READ);
  } 
  
  /**
   * Report whether this member has ATTR_DEF_ATTR_UPDATE on the specified attributeDef.
   * <pre class="eg">
   * // Check whether this member has ATTR_DEF_ATTR_UPDATE on the specified attributeDef.
   * if (m.hasAttrDefAttrUpdate(stem)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   attributeDef   Test for privilege on this {@link AttributeDef}
   * @return true if the member has the privilege.
   */
  public boolean hasAttrDefAttrUpdate(AttributeDef attributeDef) {
    return this._hasPriv(attributeDef, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE);
  } 

  /**
   * Get attribute defs where this member has the ATTR_OPTIN privilege.
   * <pre class="eg">
   * Set<AttributeDef> optin = m.hasAttrOptin();
   * </pre>
   * @return  Set of {@link AttributeDef} objects.
   * @throws  GrouperException
   */
  public Set<AttributeDef> hasAttrOptin() 
    throws  GrouperException
  {
    Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();
    try {
      attributeDefs = GrouperSession.staticGrouperSession().getAttributeDefResolver().getAttributeDefsWhereSubjectHasPrivilege(
                this.getSubject(), AttributeDefPrivilege.ATTR_OPTIN
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return attributeDefs;
  }
  
  /**
   * Get attribute defs where this member has the ATTR_DEF_ATTR_READ privilege.
   * <pre class="eg">
   * Set<AttributeDef> results = m.hasAttrDefAttrRead();
   * </pre>
   * @return  Set of {@link AttributeDef} objects.
   * @throws  GrouperException
   */
  public Set<AttributeDef> hasAttrDefAttrRead() 
    throws  GrouperException
  {
    Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();
    try {
      attributeDefs = GrouperSession.staticGrouperSession().getAttributeDefResolver().getAttributeDefsWhereSubjectHasPrivilege(
                this.getSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_READ
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return attributeDefs;
  }
  
  /**
   * Get attribute defs where this member has the ATTR_DEF_ATTR_UPDATE privilege.
   * <pre class="eg">
   * Set<AttributeDef> results = m.hasAttrDefAttrUpdate();
   * </pre>
   * @return  Set of {@link AttributeDef} objects.
   * @throws  GrouperException
   */
  public Set<AttributeDef> hasAttrDefAttrUpdate() 
    throws  GrouperException
  {
    Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();
    try {
      attributeDefs = GrouperSession.staticGrouperSession().getAttributeDefResolver().getAttributeDefsWhereSubjectHasPrivilege(
                this.getSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return attributeDefs;
  }
  
  /**
   * Get groups where this member has the GROUP_ATTR_UPDATE privilege.
   * <pre class="eg">
   * Set<Group> results = m.hasGroupAttrUpdate();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperException
   */
  public Set<Group> hasGroupAttrUpdate() 
    throws  GrouperException
  {
    Set<Group> groups = new LinkedHashSet<Group>();
    try {
      groups = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.GROUP_ATTR_UPDATE
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return groups;
  }
  
  /**
   * Get groups where this member has the GROUP_ATTR_READ privilege.
   * <pre class="eg">
   * Set<Group> results = m.hasGroupAttrRead();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperException
   */
  public Set<Group> hasGroupAttrRead() 
    throws  GrouperException
  {
    Set<Group> groups = new LinkedHashSet<Group>();
    try {
      groups = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectHasPrivilege(
                this.getSubject(), AccessPrivilege.GROUP_ATTR_READ
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return groups;
  }
  
  /**
   * Get stems where this member has the STEM_ATTR_READ privilege.
   * <pre class="eg">
   * Set<Stem> results = m.hasStemAttrRead();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperException
   */
  public Set<Stem> hasStemAttrRead() 
    throws  GrouperException
  {
    Set<Stem> stems = new LinkedHashSet<Stem>();
    try {
      stems = GrouperSession.staticGrouperSession().getNamingResolver().getStemsWhereSubjectHasPrivilege(
                this.getSubject(), NamingPrivilege.STEM_ATTR_READ
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return stems;
  }
  
  /**
   * Get stems where this member has the STEM_ATTR_UPDATE privilege.
   * <pre class="eg">
   * Set<Stem> results = m.hasStemAttrUpdate();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperException
   */
  public Set<Stem> hasStemAttrUpdate() 
    throws  GrouperException
  {
    Set<Stem> stems = new LinkedHashSet<Stem>();
    try {
      stems = GrouperSession.staticGrouperSession().getNamingResolver().getStemsWhereSubjectHasPrivilege(
                this.getSubject(), NamingPrivilege.STEM_ATTR_UPDATE
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return stems;
  }

  /**
   * Report whether this member has ATTR_OPTIN on the specified AttributeDef.
   * <pre class="eg">
   * // Check whether this member has ATTR_OPTIN on the specified AttributeDef.
   * if (m.hasOptin(attributeDef)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   attributeDef   Test for privilege on this {@link AttributeDef}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasAttrOptin(AttributeDef attributeDef) {
    return this._hasPriv(attributeDef, AttributeDefPrivilege.ATTR_OPTIN);
  } 

  /**
     * Get groups where this member has the ATTR_OPTOUT privilege.
     * <pre class="eg">
     * Set<AttributeDef> optout = m.hasOptout();
     * </pre>
     * @return  Set of {@link AttributeDef} objects.
     * @throws  GrouperException
     */
    public Set<AttributeDef> hasAttrOptout() 
      throws  GrouperException
    {
      Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();
      try {
        attributeDefs = GrouperSession.staticGrouperSession().getAttributeDefResolver().getAttributeDefsWhereSubjectHasPrivilege(
                  this.getSubject(), AttributeDefPrivilege.ATTR_OPTOUT
                );
      }
      catch (SubjectNotFoundException eSNF) {
        LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
      }
      return attributeDefs;
    }

  /**
   * Report whether this member has ATTR_OPTOUT on the specified AttributeDef.
   * <pre class="eg">
   * // Check whether this member has ATTR_OPTOUT on the specified AttributeDef.
   * if (m.hasOptout(attributeDef)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   attributeDef   Test for privilege on this {@link AttributeDef}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasAttrOptout(AttributeDef attributeDef) {
    return this._hasPriv(attributeDef, AttributeDefPrivilege.ATTR_OPTOUT);
  } 

  /**
   * Get groups where this member has the ATTR_READ privilege.
   * <pre class="eg">
   * Set<AttributeDef> read = m.hasRead();
   * </pre>
   * @return  Set of {@link AttributeDef} objects.
   * @throws  GrouperException
   */
  public Set<AttributeDef> hasAttrRead() 
    throws  GrouperException
  {
    Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();
    try {
      attributeDefs = GrouperSession.staticGrouperSession().getAttributeDefResolver().getAttributeDefsWhereSubjectHasPrivilege(
                this.getSubject(), AttributeDefPrivilege.ATTR_READ
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return attributeDefs;
  }

  /**
   * Report whether this member has ATTR_READ on the specified AttributeDef.
   * <pre class="eg">
   * // Check whether this member has ATTR_READ on the specified AttributeDef.
   * if (m.hasRead(attributeDef)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   attributeDef   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasAttrRead(AttributeDef attributeDef) {
    return this._hasPriv(attributeDef, AttributeDefPrivilege.ATTR_READ);
  } 

  /**
   * Get groups where this member has the ATTR_UPDATE privilege.
   * <pre class="eg">
   * Set<AttributeDef> update = m.hasUpdate();
   * </pre>
   * @return  Set of {@link AttributeDef} objects.
   * @throws  GrouperException
   */
  public Set<AttributeDef> hasAttrUpdate() 
    throws  GrouperException
  {
    Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();
    try {
      attributeDefs = GrouperSession.staticGrouperSession().getAttributeDefResolver().getAttributeDefsWhereSubjectHasPrivilege(
                this.getSubject(), AttributeDefPrivilege.ATTR_UPDATE
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return attributeDefs;
  }

  /**
   * Report whether this member has ATTR_UPDATE on the specified AttributeDef.
   * <pre class="eg">
   * // Check whether this member has ATTR_UPDATE on the specified AttributeDef.
   * if (m.hasUpdate(attributeDef)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   attributeDef   Test for privilege on this {@link AttributeDef}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasAttrUpdate(AttributeDef attributeDef) {
    return this._hasPriv(attributeDef, AttributeDefPrivilege.ATTR_UPDATE);
  } 

  /**
   * Get groups where this member has the ATTR_VIEW privilege.
   * <pre class="eg">
   * Set<AttributeDef> view = m.hasView();
   * </pre>
   * @return  Set of {@link AttributeDef} objects.
   * @throws  GrouperException
   */
  public Set<AttributeDef> hasAttrView() 
    throws  GrouperException
  {
    Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();
    try {
      attributeDefs = GrouperSession.staticGrouperSession().getAttributeDefResolver().getAttributeDefsWhereSubjectHasPrivilege(
                this.getSubject(), AttributeDefPrivilege.ATTR_VIEW
              );
    }
    catch (SubjectNotFoundException eSNF) {
      LOG.error( E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return attributeDefs;
  }

  /**
   * Report whether this member has ATTR_VIEW on the specified AttributeDef.
   * <pre class="eg">
   * // Check whether this member has ATTR_VIEW on the specified AttributeDef.
   * if (m.hasView(attributeDef)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   attributeDef   Test for privilege on this {@link AttributeDef}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasAttrView(AttributeDef attributeDef) {
    return this._hasPriv(attributeDef, AttributeDefPrivilege.ATTR_VIEW);
  }

  /**
   * Test whether a member nonimmediately belongs to a group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (m.isNonImmediateMember(g)) {
   *   // Is an immediate member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @return  Boolean true if is a member.
   * @throws  GrouperException
   */
  public boolean isNonImmediateMember(Group g) 
    throws  GrouperException
  {
    try {
      return this.isNonImmediateMember(g, Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal( msg);
      throw new GrouperException(msg, eS);
    }
  } // public boolean isImmediateMember(g)

  /**
   * Test whether a member nonimmediately belongs to a group.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * // Does this member nonimmediately belong to the specified group?
   * if (m.isNonImmediateMember(g, f)) {
   *   // Is an immediate member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @param   f   Test for memberhip in this list field.
   * @return  Boolean true if is a member.
   * @throws  SchemaException
   */
  public boolean isNonImmediateMember(Group g, Field f) 
    throws  SchemaException {
    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllByGroupOwnerAndFieldAndMembersAndType(g.getUuid(), f, 
        GrouperUtil.toSet(this), MembershipType.NONIMMEDIATE.getTypeString(), true);
    return memberships.size() > 0;
  }

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
   * @param field 
   * @return  Set of {@link Group} objects.
   */
  public Set<Group> getNonImmediateGroups(Field field) {
    
    return this.getNonImmediateGroups(field, null, null, null, null, true);
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Member o) {
    if (this == o) {
      return 0;
    }
    //lets by null safe here
    if (o == null) {
      return -1;
    }
    int compare = GrouperUtil.compare(this.getSubjectSourceId(), o.getSubjectSourceId());
    if (compare != 0) {
      return compare;
    }
    return GrouperUtil.compare(this.getSubjectId(), o.getSubjectId());
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(Member other) {
    
    if (!StringUtils.equals(this.memberUUID, other.memberUUID)) {
      return true;
    }
    if (!StringUtils.equals(this.subjectID, other.subjectID)) {
      return true;
    }
    if (!StringUtils.equals(this.subjectSourceID, other.subjectSourceID)) {
      return true;
    }
    if (!StringUtils.equals(this.subjectTypeID, other.subjectTypeID)) {
      return true;
    }
    if (!StringUtils.equals(this.sortString0, other.sortString0)) {
      return true;
    }
    if (!StringUtils.equals(this.sortString1, other.sortString1)) {
      return true;
    }
    if (!StringUtils.equals(this.sortString2, other.sortString2)) {
      return true;
    }
    if (!StringUtils.equals(this.sortString3, other.sortString3)) {
      return true;
    }
    if (!StringUtils.equals(this.sortString4, other.sortString4)) {
      return true;
    }
    if (!StringUtils.equals(this.searchString0, other.searchString0)) {
      return true;
    }
    if (!StringUtils.equals(this.searchString1, other.searchString1)) {
      return true;
    }
    if (!StringUtils.equals(this.searchString2, other.searchString2)) {
      return true;
    }
    if (!StringUtils.equals(this.searchString3, other.searchString3)) {
      return true;
    }
    if (!StringUtils.equals(this.searchString4, other.searchString4)) {
      return true;
    }
    if (!StringUtils.equals(this.name, other.name)) {
      return true;
    }
    if (!StringUtils.equals(this.description, other.description)) {
      return true;
    }

    return false;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(Member other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    return false;

  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(Member existingRecord) {
    existingRecord.memberUUID = this.memberUUID;
    existingRecord.subjectID = this.subjectID;
    existingRecord.subjectSourceID = this.subjectSourceID;
    existingRecord.subjectTypeID = this.subjectTypeID;
    existingRecord.sortString0 = this.sortString0;
    existingRecord.sortString1 = this.sortString1;
    existingRecord.sortString2 = this.sortString2;
    existingRecord.sortString3 = this.sortString3;
    existingRecord.sortString4 = this.sortString4;
    existingRecord.searchString0 = this.searchString0;
    existingRecord.searchString1 = this.searchString1;
    existingRecord.searchString2 = this.searchString2;
    existingRecord.searchString3 = this.searchString3;
    existingRecord.searchString4 = this.searchString4;
    existingRecord.name = this.name;
    existingRecord.description = this.description;
  }



  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public Member xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getMember().findByUuidOrSubject(this.memberUUID, this.subjectID, this.subjectSourceID, false);
  }


  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public Member xmlSaveBusinessProperties(Member existingRecord) {

    //if its an insert, call the business method
    if (existingRecord == null) {
      existingRecord = this.clone();
      GrouperDAOFactory.getFactory().getMember().create(existingRecord);
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.store();
    return existingRecord;
  }


  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    
    GrouperDAOFactory.getFactory().getMember().saveUpdateProperties(this);
    
  }


  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportMember xmlToExportMember(GrouperVersion grouperVersion) {

    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportMember xmlExportMember = new XmlExportMember();
    xmlExportMember.setContextId(this.getContextId());
    xmlExportMember.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportMember.setSourceId(this.getSubjectSourceId());
    xmlExportMember.setSubjectId(this.getSubjectId());
    xmlExportMember.setSubjectType(this.getSubjectTypeId());
    xmlExportMember.setUuid(this.getUuid());
    
    xmlExportMember.setSortString0(this.getSortString0());
    xmlExportMember.setSortString1(this.getSortString1());
    xmlExportMember.setSortString2(this.getSortString2());
    xmlExportMember.setSortString3(this.getSortString3());
    xmlExportMember.setSortString4(this.getSortString4());
    xmlExportMember.setSearchString0(this.getSearchString0());
    xmlExportMember.setSearchString1(this.getSearchString1());
    xmlExportMember.setSearchString2(this.getSearchString2());
    xmlExportMember.setSearchString3(this.getSearchString3());
    xmlExportMember.setSearchString4(this.getSearchString4());
    xmlExportMember.setName(this.getName());
    xmlExportMember.setDescription(this.getDescription());

    return xmlExportMember;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlGetId()
   */
  public String xmlGetId() {
    return this.getUuid();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setUuid(theId);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("Member: " + this.getUuid() + ", " + this.getSubjectSourceId() + " - " + this.getSubjectId());

//    XmlExportUtils.toStringMember(null, stringWriter, this, false);
    
    return stringWriter.toString();
    
  }

  
  /**
   * @return the sortString0
   */
  public String getSortString0() {
    return sortString0;
  }

  
  /**
   * @param sortString0 the sortString0 to set
   */
  public void setSortString0(String sortString0) {
    this.sortString0 = sortString0;
  }

  
  /**
   * @return the sortString1
   */
  public String getSortString1() {
    return sortString1;
  }

  
  /**
   * @param sortString1 the sortString1 to set
   */
  public void setSortString1(String sortString1) {
    this.sortString1 = sortString1;
  }

  
  /**
   * @return the sortString2
   */
  public String getSortString2() {
    return sortString2;
  }

  
  /**
   * @param sortString2 the sortString2 to set
   */
  public void setSortString2(String sortString2) {
    this.sortString2 = sortString2;
  }

  
  /**
   * @return the sortString3
   */
  public String getSortString3() {
    return sortString3;
  }

  
  /**
   * @param sortString3 the sortString3 to set
   */
  public void setSortString3(String sortString3) {
    this.sortString3 = sortString3;
  }

  
  /**
   * @return the sortString4
   */
  public String getSortString4() {
    return sortString4;
  }

  
  /**
   * @param sortString4 the sortString4 to set
   */
  public void setSortString4(String sortString4) {
    this.sortString4 = sortString4;
  }

  
  /**
   * @return the searchString0
   */
  public String getSearchString0() {
    return searchString0;
  }

  
  /**
   * @param searchString0 the searchString0 to set
   */
  public void setSearchString0(String searchString0) {
    this.searchString0 = searchString0;
  }

  
  /**
   * @return the searchString1
   */
  public String getSearchString1() {
    return searchString1;
  }

  
  /**
   * @param searchString1 the searchString1 to set
   */
  public void setSearchString1(String searchString1) {
    this.searchString1 = searchString1;
  }

  
  /**
   * @return the searchString2
   */
  public String getSearchString2() {
    return searchString2;
  }

  
  /**
   * @param searchString2 the searchString2 to set
   */
  public void setSearchString2(String searchString2) {
    this.searchString2 = searchString2;
  }

  
  /**
   * @return the searchString3
   */
  public String getSearchString3() {
    return searchString3;
  }

  
  /**
   * @param searchString3 the searchString3 to set
   */
  public void setSearchString3(String searchString3) {
    this.searchString3 = searchString3;
  }

  
  /**
   * @return the searchString4
   */
  public String getSearchString4() {
    return searchString4;
  }

  
  /**
   * @param searchString4 the searchString4 to set
   */
  public void setSearchString4(String searchString4) {
    this.searchString4 = searchString4;
  }

  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  
  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  
  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }  
  
  /**
   * @param subject
   * @param storeChanges if there are changes, should they be saved to the database?
   */
  public void updateMemberAttributes(Subject subject, boolean storeChanges) {
    this.sortString0 = null;
    this.sortString1 = null;
    this.sortString2 = null;
    this.sortString3 = null;
    this.sortString4 = null;
    this.searchString0 = null;
    this.searchString1 = null;
    this.searchString2 = null;
    this.searchString3 = null;
    this.searchString4 = null;

    this.name = GrouperUtil.isEmpty(subject.getName()) ? null : subject.getName();
    this.description = GrouperUtil.isEmpty(subject.getDescription()) ? null : subject.getDescription();
    
    Map<Integer, String> sortAttributes = subject.getSource().getSortAttributes();
    if (sortAttributes.containsKey(0)) {
      this.sortString0 = subject.getAttributeValue(sortAttributes.get(0), false);
    }
    
    if (sortAttributes.containsKey(1)) {
      this.sortString1 = subject.getAttributeValue(sortAttributes.get(1), false);
    }

    if (sortAttributes.containsKey(2)) {
      this.sortString2 = subject.getAttributeValue(sortAttributes.get(2), false);
    }
    
    if (sortAttributes.containsKey(3)) {
      this.sortString3 = subject.getAttributeValue(sortAttributes.get(3), false);
    }
    
    if (sortAttributes.containsKey(4)) {
      this.sortString4 = subject.getAttributeValue(sortAttributes.get(4), false);
    }
    
    Map<Integer, String> searchAttributes = subject.getSource().getSearchAttributes();
    if (searchAttributes.containsKey(0)) {
      Set<String> attrs = subject.getAttributeValues(searchAttributes.get(0), false);
      if (attrs != null && attrs.size() > 0) {
        this.searchString0 = GrouperUtil.join(attrs.iterator(), ",");
      }
    }
    
    if (searchAttributes.containsKey(1)) {
      Set<String> attrs = subject.getAttributeValues(searchAttributes.get(1), false);
      if (attrs != null && attrs.size() > 0) {
        this.searchString1 = GrouperUtil.join(attrs.iterator(), ",");
      }
    }
    
    if (searchAttributes.containsKey(2)) {
      Set<String> attrs = subject.getAttributeValues(searchAttributes.get(2), false);
      if (attrs != null && attrs.size() > 0) {
        this.searchString2 = GrouperUtil.join(attrs.iterator(), ",");
      }
    }
    
    if (searchAttributes.containsKey(3)) {
      Set<String> attrs = subject.getAttributeValues(searchAttributes.get(3), false);
      if (attrs != null && attrs.size() > 0) {
        this.searchString3 = GrouperUtil.join(attrs.iterator(), ",");
      }
    }
    
    if (searchAttributes.containsKey(4)) {
      Set<String> attrs = subject.getAttributeValues(searchAttributes.get(4), false);
      if (attrs != null && attrs.size() > 0) {
        this.searchString4 = GrouperUtil.join(attrs.iterator(), ",");
      }
    }
        
    this.sortString0 = GrouperUtil.isEmpty(this.sortString0) ? null : GrouperUtil.truncateAscii(this.sortString0, 50);
    this.sortString1 = GrouperUtil.isEmpty(this.sortString1) ? null : GrouperUtil.truncateAscii(this.sortString1, 50);
    this.sortString2 = GrouperUtil.isEmpty(this.sortString2) ? null : GrouperUtil.truncateAscii(this.sortString2, 50);
    this.sortString3 = GrouperUtil.isEmpty(this.sortString3) ? null : GrouperUtil.truncateAscii(this.sortString3, 50);
    this.sortString4 = GrouperUtil.isEmpty(this.sortString4) ? null : GrouperUtil.truncateAscii(this.sortString4, 50);
    
    this.searchString0 = GrouperUtil.isEmpty(this.searchString0) ? null : GrouperUtil.truncateAscii(this.searchString0.toLowerCase(), 2048);
    this.searchString1 = GrouperUtil.isEmpty(this.searchString1) ? null : GrouperUtil.truncateAscii(this.searchString1.toLowerCase(), 2048);
    this.searchString2 = GrouperUtil.isEmpty(this.searchString2) ? null : GrouperUtil.truncateAscii(this.searchString2.toLowerCase(), 2048);
    this.searchString3 = GrouperUtil.isEmpty(this.searchString3) ? null : GrouperUtil.truncateAscii(this.searchString3.toLowerCase(), 2048);
    this.searchString4 = GrouperUtil.isEmpty(this.searchString4) ? null : GrouperUtil.truncateAscii(this.searchString4.toLowerCase(), 2048);

    //dont do this if hibernate is not initted, since the two threads will deadlock...
    if (storeChanges && this.dbVersionIsDifferent() && GrouperStartup.isFinishedStartupSuccessfully() && !StringUtils.isBlank(this.getUuid())) {
      
      //run in new thread, out of the Grouper thread pool
      //note, this should not affect or deadlock the current transaction, and should not have grouper stale state exception
      GrouperUtil.retrieveExecutorService().submit(new Callable<Object>() {
        
        public Object call() throws Exception {
          
          try {
            
            HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

              public Object callback(HibernateHandlerBean hibernateHandlerBean)
                  throws GrouperDAOException {
                String query = "update grouper_members set sort_string0 = ?, sort_string1 = ?, sort_string2 = ?, sort_string3 = ?, " +
                  "sort_string4 = ?, search_string0 = ?, search_string1 = ?, search_string2 = ?, " +
                  "search_string3 = ?, search_string4 = ?, name = ?, description = ? where id = ?";
                List<Object> bindVars = GrouperUtil.toList((Object)Member.this.sortString0, Member.this.sortString1,
                    Member.this.sortString2, Member.this.sortString3, Member.this.sortString4, Member.this.searchString0,
                    Member.this.searchString1, Member.this.searchString2, Member.this.searchString3, Member.this.searchString4, 
                    Member.this.name, Member.this.description,
                    Member.this.getUuid());
                hibernateHandlerBean.getHibernateSession().bySql().executeSql(query, bindVars);
                hibernateHandlerBean.getHibernateSession().commit(GrouperCommitType.COMMIT_NOW);
                return null;
              }
            });
          } catch (Exception e) {
            LOG.error("Error updating member attributes: ", e);
          }
          return null;
        }
      });


    }
  }
} 

