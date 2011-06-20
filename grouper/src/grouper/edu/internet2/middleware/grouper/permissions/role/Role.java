/**
 * @author mchyzer
 * $Id: Role.java,v 1.2 2009-10-04 16:14:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.permissions.role;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.permissions.PermissionRoleDelegate;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public interface Role extends GrouperSetElement, Comparable {

  /**
   * @see Group#hasMember(Subject)
   * @param subject
   * @return true if has member, false if not
   */
  public boolean hasMember(Subject subject);
  
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
   * delegate calls to this class for role hierarchy stuff
   * @return the delegate
   */
  public RoleInheritanceDelegate getRoleInheritanceDelegate();
  
  /**
   * delegate calls to this class for permission role stuff
   * @return the delegate
   */
  public PermissionRoleDelegate getPermissionRoleDelegate();

  /**
   * Add a subject to this role as immediate member.
   * 
   * An immediate member is directly assigned to a role.
   * A composite role has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single role, and 0 to many effective memberships to a role.
   * A role can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   role.addMember(subj);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add subject
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the role
   * @return false if it already existed, true if it didnt already exist
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   */
  public boolean addMember(Subject subj, boolean exceptionIfAlreadyMember) 
    throws  InsufficientPrivilegeException,
            MemberAddException;

  /** 
   * remove a subject from this role, and subject must be immediate
   * member.  Will not remove the effective membership.
   * 
   * An immediate member is directly assigned to a role.
   * A composite role has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single role, and 0 to many effective memberships to a role.
   * A role can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(subj);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  remove this {@link Subject}
   * @param exceptionIfAlreadyDeleted 
   * @return false if it was already deleted, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   */
  public boolean deleteMember(Subject subj, boolean exceptionIfAlreadyDeleted)
    throws  InsufficientPrivilegeException,
            MemberDeleteException;
}
