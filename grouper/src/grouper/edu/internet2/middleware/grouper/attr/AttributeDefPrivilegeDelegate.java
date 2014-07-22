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
/**
 * @author mchyzer
 * $Id: AttributeDefPrivilegeDelegate.java,v 1.3 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.UnableToPerformAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.beans.RulesPrivilegeBean;
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
    PrivilegeHelper.dispatch(GrouperSession.staticGrouperSession(), this.attributeDef, GrouperSession.staticGrouperSession().getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
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
  public boolean hasAttrOptin(Subject subj) {
    PrivilegeHelper.dispatch(GrouperSession.staticGrouperSession(), this.attributeDef, GrouperSession.staticGrouperSession().getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
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
    PrivilegeHelper.dispatch(GrouperSession.staticGrouperSession(), this.attributeDef, GrouperSession.staticGrouperSession().getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
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
    
    PrivilegeHelper.dispatch(GrouperSession.staticGrouperSession(), this.attributeDef, GrouperSession.staticGrouperSession().getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    
    AttributeDefResolver attributeDefResolver = GrouperSession.staticGrouperSession().getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(this.attributeDef, subj, AttributeDefPrivilege.ATTR_READ);
  } 
  
  /**
   * Check whether the subject has ATTR_DEF_ATTR_UPDATE on this attributeDef, or something else
   * that allows it (admin)
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().canAttrDefAttrUpdate(subj)) {
   *   // Has ATTR_DEF_ATTR_UPDATE
   * }
   * else {
   *   // Does not have ATTR_DEF_ATTR_UPDATE
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_DEF_ATTR_UPDATE.
   */
  public boolean canAttrDefAttrUpdate(Subject subj) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    PrivilegeHelper.dispatch(grouperSession, this.attributeDef, grouperSession.getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    return PrivilegeHelper.canAttrDefAttrUpdate(grouperSession, this.attributeDef, subj);
  }
  
  /**
   * Check whether the subject has ATTR_DEF_ATTR_READ on this attributeDef, or something else
   * that allows it (admin)
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().canAttrDefAttrRead(subj)) {
   *   // Has ATTR_DEF_ATTR_READ
   * }
   * else {
   *   // Does not have ATTR_DEF_ATTR_READ
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_DEF_ATTR_READ.
   */
  public boolean canAttrDefAttrRead(Subject subj) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    PrivilegeHelper.dispatch(grouperSession, this.attributeDef, grouperSession.getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    return PrivilegeHelper.canAttrDefAttrRead(grouperSession, this.attributeDef, subj);
  }

  /**
   * Check whether the subject has ATTR_DEF_ATTR_READ on this attributeDef.
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().hasAttrDefAttrRead(subj)) {
   *   // Has ATTR_DEF_ATTR_READ
   * }
   * else {
   *   // Does not have ATTR_DEF_ATTR_READ
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_DEF_ATTR_READ.
   */
  public boolean hasAttrDefAttrRead(Subject subj) {
    
    PrivilegeHelper.dispatch(GrouperSession.staticGrouperSession(), this.attributeDef, GrouperSession.staticGrouperSession().getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    
    AttributeDefResolver attributeDefResolver = GrouperSession.staticGrouperSession().getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(this.attributeDef, subj, AttributeDefPrivilege.ATTR_DEF_ATTR_READ);
  } 
  
  /**
   * Check whether the subject has ATTR_DEF_ATTR_UPDATE on this attributeDef.
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().hasAttrDefAttrUpdate(subj)) {
   *   // Has ATTR_DEF_ATTR_UPDATE
   * }
   * else {
   *   // Does not have ATTR_DEF_ATTR_UPDATE
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_DEF_ATTR_UPDATE.
   */
  public boolean hasAttrDefAttrUpdate(Subject subj) {
    
    PrivilegeHelper.dispatch(GrouperSession.staticGrouperSession(), this.attributeDef, GrouperSession.staticGrouperSession().getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    
    AttributeDefResolver attributeDefResolver = GrouperSession.staticGrouperSession().getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(this.attributeDef, subj, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE);
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
    PrivilegeHelper.dispatch(GrouperSession.staticGrouperSession(), this.attributeDef, GrouperSession.staticGrouperSession().getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
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
    PrivilegeHelper.dispatch(GrouperSession.staticGrouperSession(), this.attributeDef, GrouperSession.staticGrouperSession().getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
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
    return internal_grantPriv(subj, priv, exceptionIfAlreadyMember, null);
  }
  
  /**
   * grant privs to attributedef
   * @param subject to add
   * @param updateChecked
   * @param adminChecked
   * @param readChecked
   * @param viewChecked
   * @param optinChecked
   * @param optoutChecked
   * @param attrReadChecked
   * @param attrUpdateChecked
   * @param revokeIfUnchecked
   * @return if something was changed
   */
  public boolean grantPrivs(final Subject subject,
      final boolean adminChecked,
      final boolean updateChecked, 
      final boolean readChecked,
      final boolean viewChecked,
      final boolean optinChecked,
      final boolean optoutChecked,
      final boolean attrReadChecked,
      final boolean attrUpdateChecked, final boolean revokeIfUnchecked) {
    
    return (Boolean)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
  
        boolean hadChange = false;
        
        //see if add or remove
        if (adminChecked) {
          hadChange = hadChange | AttributeDefPrivilegeDelegate.this.grantPriv(subject, AttributeDefPrivilege.ATTR_ADMIN, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | AttributeDefPrivilegeDelegate.this.revokePriv(subject, AttributeDefPrivilege.ATTR_ADMIN, false);
          }
        }

        //see if add or remove
        if (updateChecked) {
          hadChange = hadChange | AttributeDefPrivilegeDelegate.this.grantPriv(subject, AttributeDefPrivilege.ATTR_UPDATE, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | AttributeDefPrivilegeDelegate.this.revokePriv(subject, AttributeDefPrivilege.ATTR_UPDATE, false);
          }
        }

        //see if add or remove
        if (readChecked) {
          hadChange = hadChange | AttributeDefPrivilegeDelegate.this.grantPriv(subject, AttributeDefPrivilege.ATTR_READ, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | AttributeDefPrivilegeDelegate.this.revokePriv(subject, AttributeDefPrivilege.ATTR_READ, false);
          }
        }

        //see if add or remove
        if (viewChecked) {
          hadChange = hadChange | AttributeDefPrivilegeDelegate.this.grantPriv(subject, AttributeDefPrivilege.ATTR_VIEW, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | AttributeDefPrivilegeDelegate.this.revokePriv(subject, AttributeDefPrivilege.ATTR_VIEW, false);
          }
        }

        //see if add or remove
        if (optinChecked) {
          hadChange = hadChange | AttributeDefPrivilegeDelegate.this.grantPriv(subject, AttributeDefPrivilege.ATTR_OPTIN, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | AttributeDefPrivilegeDelegate.this.revokePriv(subject, AttributeDefPrivilege.ATTR_OPTIN, false);
          }
        }

        //see if add or remove
        if (optoutChecked) {
          hadChange = hadChange | AttributeDefPrivilegeDelegate.this.grantPriv(subject, AttributeDefPrivilege.ATTR_OPTOUT, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | AttributeDefPrivilegeDelegate.this.revokePriv(subject, AttributeDefPrivilege.ATTR_OPTOUT, false);
          }
        }

        //see if add or remove
        if (attrReadChecked) {
          hadChange = hadChange | AttributeDefPrivilegeDelegate.this.grantPriv(subject, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | AttributeDefPrivilegeDelegate.this.revokePriv(subject, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
          }
        }

        //see if add or remove
        if (attrUpdateChecked) {
          hadChange = hadChange | AttributeDefPrivilegeDelegate.this.grantPriv(subject, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | AttributeDefPrivilegeDelegate.this.revokePriv(subject, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, false);
          }
        }

        return hadChange;
      }
    });
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
   * @param uuid is uuid or null for assigned
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @return false if it already existed, true if it didnt already exist
   */
  public boolean internal_grantPriv(final Subject subj, final Privilege priv, final boolean exceptionIfAlreadyMember, final String uuid)
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
  
          hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

          boolean assignedPrivilege = false;
          try {
            //note, this will validate the inputs
            GrouperSession.staticGrouperSession().getAttributeDefResolver().grantPrivilege(
                AttributeDefPrivilegeDelegate.this.attributeDef, subj, priv, uuid);
            
            RulesPrivilegeBean rulesPrivilegeBean = new RulesPrivilegeBean(AttributeDefPrivilegeDelegate.this.attributeDef, subj, priv);
            
            //fire rules related to subject assign in folder
            RuleEngine.fireRule(RuleCheckType.subjectAssignInStem, rulesPrivilegeBean);

            
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
    
        hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
    
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

  /**
   * Check whether the subject has ATTR_READ on this attributeDef, or something else
   * that allows read (admin)
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().cabAttrRead(subj)) {
   *   // Has ATTR_READ
   * }
   * else {
   *   // Does not have ATTR_READ
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_READ.
   */
  public boolean canAttrRead(Subject subj) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    PrivilegeHelper.dispatch(grouperSession, this.attributeDef, grouperSession.getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    return PrivilegeHelper.canAttrRead(grouperSession, this.attributeDef, subj);
  } 

  /**
   * Check whether the subject has ATTR_VIEW on this attributeDef, or something else
   * that allows view (admin, read, update, etc)
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().cabAttrRead(subj)) {
   *   // Has ATTR_VIEW
   * }
   * else {
   *   // Does not have ATTR_VIEW
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_VIEW.
   */
  public boolean canAttrView(Subject subj) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    PrivilegeHelper.dispatch(grouperSession, this.attributeDef, grouperSession.getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    return PrivilegeHelper.canAttrView(grouperSession, this.attributeDef, subj);
  } 

  /**
   * Check whether the subject has ATTR_UPDATE on this attributeDef, or something else
   * that allows update (admin)
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().cabAttrUpdate(subj)) {
   *   // Has ATTR_UPDATE
   * }
   * else {
   *   // Does not have ATTR_UPDATE
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_UPDATE.
   */
  public boolean canAttrUpdate(Subject subj) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    PrivilegeHelper.dispatch(grouperSession, this.attributeDef, grouperSession.getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    return PrivilegeHelper.canAttrUpdate(grouperSession, this.attributeDef, subj);
  } 
  
  /**
   * Check whether the subject has ATTR_ADMIN on this attributeDef, or something else
   * that allows admin (well, actually, there isnt anything)
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().cabAttrAdmin(subj)) {
   *   // Has ATTR_ADMIN
   * }
   * else {
   *   // Does not have ATTR_ADMIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_ADMIN.
   */
  public boolean canAttrAdmin(Subject subj) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    PrivilegeHelper.dispatch(grouperSession, this.attributeDef, grouperSession.getSubject(), AttributeDefPrivilege.ATTR_ADMIN);

    return PrivilegeHelper.canAttrAdmin(grouperSession, this.attributeDef, subj);
  } 

  /**
   * see if the subject has a privilege.  Note it returns only if the 
   * subject has this privilege.  
   * @param subject
   * @param privilegeOrListName
   * @return true if has privilege
   */
  public boolean hasPrivilege(Subject subject, String privilegeOrListName) {
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_ADMIN.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_ADMIN.getListName())) {
      return this.hasAttrAdmin(subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_UPDATE.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_UPDATE.getListName())) {
      return this.hasAttrUpdate(subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_READ.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_READ.getListName())) {
      return this.hasAttrRead(subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_VIEW.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_VIEW.getListName())) {
      return this.hasAttrView(subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_OPTIN.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_OPTIN.getListName())) {
      return this.hasAttrOptin(subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_OPTOUT.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_OPTOUT.getListName())) {
      return this.hasAttrOptout(subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_DEF_ATTR_READ.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_DEF_ATTR_READ.getListName())) {
      return this.hasAttrDefAttrRead(subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE.getListName())) {
      return this.hasAttrDefAttrUpdate(subject);
    }
    throw new RuntimeException("Cant find privilege: '" + privilegeOrListName + "'");

  }

  
  /**
   * Check whether the subject has ATTR_OPTIN on this attributeDef, or something else
   * that allows read (well, actually there isnt anything else right now)
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().cabAttrOptin(subj)) {
   *   // Has ATTR_OPTIN
   * }
   * else {
   *   // Does not have ATTR_OPTIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_OPTIN.
   */
  public boolean canAttrOptin(Subject subj) {

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();

    PrivilegeHelper.dispatch(grouperSession, this.attributeDef, grouperSession.getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    
    return PrivilegeHelper.canAttrOptin(grouperSession, this.attributeDef, subj);
  } 

  /**
   * Check whether the subject has ATTR_OPTOUT on this attributeDef, or something else
   * that allows optout (well, actually, there isnt anything else right now)
   * <pre class="eg">
   * if (attributeDef.getPrivilegeDelegate().cabAttrOptout(subj)) {
   *   // Has ATTR_OPTOUT
   * }
   * else {
   *   // Does not have ATTR_OPTOUT
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ATTR_OPTOUT.
   */
  public boolean canAttrOptout(Subject subj) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    PrivilegeHelper.dispatch(grouperSession, this.attributeDef, grouperSession.getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    return PrivilegeHelper.canAttrOptout(grouperSession, this.attributeDef, subj);
  }

  /**
   * see if the subject has a privilege or another privilege that implies this privilege.
   * @param subject
   * @param privilegeOrListName
   * @param secure if the user must be an admin to check
   * @return true if has privilege
   */
  public boolean canHavePrivilege(Subject subject, String privilegeOrListName, boolean secure) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    if (secure) {
      PrivilegeHelper.dispatch(grouperSession, this.attributeDef, grouperSession.getSubject(), AttributeDefPrivilege.ATTR_ADMIN);
    }
    
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_ADMIN.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_ADMIN.getListName())) {
      return PrivilegeHelper.canAttrAdmin(grouperSession, this.attributeDef, subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_UPDATE.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_UPDATE.getListName())) {
      return PrivilegeHelper.canAttrUpdate(grouperSession, this.attributeDef, subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_READ.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_READ.getListName())) {
      return PrivilegeHelper.canAttrRead(grouperSession, this.attributeDef, subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_VIEW.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_VIEW.getListName())) {
      return PrivilegeHelper.canAttrView(grouperSession, this.attributeDef, subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_OPTIN.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_OPTIN.getListName())) {
      return PrivilegeHelper.canAttrOptin(grouperSession, this.attributeDef, subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_OPTOUT.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_OPTOUT.getListName())) {
      return PrivilegeHelper.canAttrOptout(grouperSession, this.attributeDef, subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_DEF_ATTR_READ.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_DEF_ATTR_READ.getListName())) {
      return PrivilegeHelper.canAttrDefAttrRead(grouperSession, this.attributeDef, subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE.getListName())) {
      return PrivilegeHelper.canAttrDefAttrUpdate(grouperSession, this.attributeDef, subject);
    }
    throw new RuntimeException("Cant find privilege: '" + privilegeOrListName + "'");
  
  } 

}
