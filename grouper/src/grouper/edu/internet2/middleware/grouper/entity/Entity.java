/**
 * @author mchyzer
 * $Id: Role.java,v 1.2 2009-10-04 16:14:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.entity;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignGroupDelegate;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupModifyAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.UnableToPerformAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.permissions.PermissionRoleDelegate;
import edu.internet2.middleware.grouper.permissions.role.RoleHierarchyType;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 *
 */
public interface Entity extends GrouperSetElement, Comparable {

  /**
   * delete this role.  Note if the role participates in role
   * inheritance, you need to break that inheritance first
   */
  public void delete();
  
  /**
   * uuid of role
   * @return id
   */
  public String getId();
  
  /**
   * name of role
   * @return name
   */
  public String getName();

  /**
   * description of role, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   * @return the description
   */
  public String getDescription();

  /**
   * displayExtension of role
   * @return display extension
   */
  public String getDisplayExtension();

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @return display name
   */
  public String getDisplayName();

  /**
   * extension of attribute expireTime
   * @return extension
   */
  public String getExtension();

  /**
   * stem that this attribute is in
   * @return the stem id
   */
  public String getStemId();

  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   * @param description1
   */
  public void setDescription(String description1);

  /**
   * displayExtension of attribute, e.g. Expire Date
   * @param displayExtension1
   */
  public void setDisplayExtension(String displayExtension1);

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @param displayName1
   */
  public void setDisplayName(String displayName1);

  /**
   * extension of attribute expireTime
   * @param extension1
   */
  public void setExtension(String extension1);

  /**
   * id of this attribute def name
   * @param id1
   */
  public void setId(String id1);

  /**
   * 
   * @param name1
   */
  public void setName(String name1);

  /**
   * stem that this attribute is in
   * @param stemId1
   */
  public void setStemId(String stemId1);

  /**
   * Get subjects with the ADMIN privilege on this group.
   * <pre class="eg">
   * Set admins = g.getAdmins();
   * </pre>
   * @return  Set of subjects with ADMIN
   * @throws  GrouperException
   */
  public Set<Subject> getAdmins();

  /**
   * Get subjects with the VIEW privilege on this group.
   * <pre class="eg">
   * Set viewers = g.getViewers();
   * </pre>
   * @return  Set of subjects with VIEW
   * @throws  GrouperException
   */
  public Set<Subject> getViewers();

  /**
   * Grant privilege to a subject on this group.
   * <pre class="eg">
   * try {
   *   g.grantPriv(subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Not privileged to grant this privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Unable to grant this privilege
   * }
   * </pre>
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @return false if it already existed, true if it didnt already exist
   */
  public boolean grantPriv(final Subject subj, final Privilege priv, final boolean exceptionIfAlreadyMember)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException;

  /**
   * Check whether the subject has ADMIN on this group.
   * <pre class="eg">
   * if (g.hasAdmin(subj)) {
   *   // Has ADMIN
   * }
   * else {
   *   // Does not have ADMIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ADMIN.
   */
  public boolean hasAdmin(Subject subj);

  /**
   * Check whether the subject has VIEW on this group.
   * <pre class="eg">
   * if (g.hasView(subj)) {
   *   // Has VIEW
   * }
   * else {
   *   // Does not have VIEW
   * }
   * </pre>
   * @param   subj  Check this member.
   * @return  Boolean true if subject has VIEW.
   */
  public boolean hasView(Subject subj);

  /**
   * Revoke a privilege from the specified subject.
   * <pre class="eg">
   * try {
   *   g.revokePriv(subj, AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.
   * @param exceptionIfAlreadyRevoked if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @return false if it was already revoked, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public boolean revokePriv(final Subject subj, final Privilege priv, 
      final boolean exceptionIfAlreadyRevoked) 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, SchemaException;

  /**
   * store this object to the DB.
   */
  public void store();

  /**
   * Convert this group to a {@link Member} object.
   * <p/>
   * <pre class="eg">
   * Member m = g.toMember();
   * </pre>
   * @return  {@link Group} as a {@link Member}
   * @throws  GrouperException
   */
  public Member toMember() 
    throws  GrouperException;

  /**
   * Convert this group to a {@link Subject} object.
   * <p/>
   * <pre class="eg">
   * Subject subj = g.toSubject();
   * </pre>
   * @return  {@link Group} as a {@link Subject}
   * @throws  GrouperException
   */
  public Subject toSubject() 
    throws  GrouperException;

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId();

  /**
   * Copy this group to another Stem.  If you want to specify options
   * for the copy, use GroupCopy.  This will use the default options.
   * @param stem
   * @return the new group
   * @throws InsufficientPrivilegeException 
   * @throws GroupAddException 
   */
  public Group copy(Stem stem);
  

  /**
   * Move this group to another Stem.  If you would like to specify options for the move, 
   * use GroupMove instead.  This will use the default options.
   * @param stem 
   * @throws GroupModifyException 
   * @throws InsufficientPrivilegeException 
   */
  public void move(Stem stem);

}
