/**
 * @author mchyzer
 * $Id: AttributeDefPrivilegeDelegate.java,v 1.1 2009-09-21 06:14:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr;

import org.apache.commons.lang.time.StopWatch;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.UnableToPerformAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * delegate privilege calls from attribute defs
 */
public class AttributeDefPrivilegeDelegate {

  /**
   * reference to the attribute def in question
   */
  private AttributeDef attributeDef = null;
  
  /**
   * 
   * @param attributeDef1
   */
  AttributeDefPrivilegeDelegate(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }
  
  /**
   * Check whether the subject has ATTR_ADMIN on this attributeDef.
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().hasAttrAdmin(subj)) {
   *   // Has ATTR_ADMIN
   * }
   * else {
   *   // Does not have ATTR_ADMIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_ADMIN.
   */
  public boolean hasAttrAdmin(Subject subj) {
    AttributeDefResolver attributeDefResolver = GrouperSession.staticGrouperSession().getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(this.attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN);
  } 

  /**
   * Check whether the subject has ATTR_OPTIN on this attributeDef.
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().hasAttrOptin(subj)) {
   *   // Has ATTR_OPTIN
   * }
   * else {
   *   // Does not have ATTR_OPTIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_OPTIN.
   */
  public boolean hasAttrOptn(Subject subj) {
    AttributeDefResolver attributeDefResolver = GrouperSession.staticGrouperSession().getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(this.attributeDef, subj, AttributeDefPrivilege.ATTR_OPTIN);
  } 

  /**
   * Check whether the subject has ATTR_OPTOUT on this attributeDef.
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().hasAttrOptout(subj)) {
   *   // Has ATTR_OPTOUT
   * }
   * else {
   *   // Does not have ATTR_OPTOUT
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_OPTOUT.
   */
  public boolean hasAttrOptout(Subject subj) {
    AttributeDefResolver attributeDefResolver = GrouperSession.staticGrouperSession().getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(this.attributeDef, subj, AttributeDefPrivilege.ATTR_OPTOUT);
  } 

  /**
   * Check whether the subject has ATTR_READ on this attributeDef.
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().hasAttrRead(subj)) {
   *   // Has ATTR_READ
   * }
   * else {
   *   // Does not have ATTR_READ
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_READ.
   */
  public boolean hasAttrRead(Subject subj) {
    AttributeDefResolver attributeDefResolver = GrouperSession.staticGrouperSession().getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(this.attributeDef, subj, AttributeDefPrivilege.ATTR_READ);
  } 

  /**
   * Check whether the subject has ATTR_UPDATE on this attributeDef.
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().hasAttrUpdate(subj)) {
   *   // Has ATTR_UPDATE
   * }
   * else {
   *   // Does not have ATTR_UPDATE
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_UPDATE.
   */
  public boolean hasAttrUpdate(Subject subj) {
    AttributeDefResolver attributeDefResolver = GrouperSession.staticGrouperSession().getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(this.attributeDef, subj, AttributeDefPrivilege.ATTR_UPDATE);
  } 

  /**
   * Check whether the subject has ATTR_VIEW on this attributeDef.
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().hasAttrView(subj)) {
   *   // Has ATTR_VIEW
   * }
   * else {
   *   // Does not have ATTR_VIEW
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_VIEW.
   */
  public boolean hasAttrView(Subject subj) {
    AttributeDefResolver attributeDefResolver = GrouperSession.staticGrouperSession().getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(this.attributeDef, subj, AttributeDefPrivilege.ATTR_VIEW);
  }

  /**
   * Grant privilege to a subject on this attributeDef.
   * <pre class="eg">
   * try {
   *   attributeDef.getPrivilegeDelegate().grantPriv(subj, AttributeDefPrivilege.ATTR_ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Cannot grant this privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Unable to grant this privilege
   * }
   * </pre>
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the list
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @return false if it already existed, true if it didnt already exist
   */
  public boolean grantPriv(final Subject subj, final Privilege priv, final boolean exceptionIfAlreadyMember)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException {
    final StopWatch sw = new StopWatch();
    sw.start();
  
    final String errorMessageSuffix = ", attributeDef name: " + this.attributeDef.getName()
      + ", subject: " + GrouperUtil.subjectToString(subj) + ", privilege: " + (priv == null ? null : priv.getName());
  
    return (Boolean)HibernateSession.callbackHibernateSession(
      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
      new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
  
          boolean assignedPrivilege = false;
          try {
            //note, this will validate the inputs
            GrouperSession.staticGrouperSession().getAttributeDefResolver().grantPrivilege(
                AttributeDefPrivilegeDelegate.this.attributeDef, subj, priv);
            assignedPrivilege = true;
  
          } catch (UnableToPerformAlreadyExistsException eUTP) {
            if (exceptionIfAlreadyMember) {
              throw new GrantPrivilegeAlreadyExistsException(eUTP.getMessage() + errorMessageSuffix, eUTP);
            }
          } catch (UnableToPerformException eUTP) {
            throw new GrantPrivilegeException( eUTP.getMessage() + errorMessageSuffix, eUTP );
          }
          sw.stop();
          return assignedPrivilege;
        }
      });
  }

  /**
   * Revoke a privilege from the specified subject.
   * <pre class="eg">
   * try {
   *   g.getPrivilegeDelegate().revokePriv(subj, AttributeDefPrivilege.ATTR_ADMIN);
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
   * then dont throw a MemberAddException if the member is already in the list
   * @return false if it was already revoked, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public boolean revokePriv(final Subject subj, final Privilege priv, 
      final boolean exceptionIfAlreadyRevoked) 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, SchemaException {
  
    final StopWatch sw = new StopWatch();
    sw.start();
  
    final String errorMessageSuffix = ", attributeDef name: " + this.attributeDef.getName() 
      + ", subject: " + GrouperUtil.subjectToString(subj) + ", privilege: " + (priv == null ? null : priv.getName());
  
    return (Boolean)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
    
    
        boolean wasntAlreadyRevoked = true;
        try {
          //this will validate
          GrouperSession.staticGrouperSession().getAttributeDefResolver().revokePrivilege(
              AttributeDefPrivilegeDelegate.this.attributeDef, subj, priv);
    
        } catch (UnableToPerformAlreadyExistsException eUTP) {
          if (exceptionIfAlreadyRevoked) {
            throw new RevokePrivilegeAlreadyRevokedException( eUTP.getMessage() + errorMessageSuffix, eUTP );
          }
          wasntAlreadyRevoked = false;
        } catch (UnableToPerformException eUTP) {
          throw new RevokePrivilegeException( eUTP.getMessage() + errorMessageSuffix, eUTP );
        }
        sw.stop();
        return wasntAlreadyRevoked;
      }
    });
  } 


  
  
}
