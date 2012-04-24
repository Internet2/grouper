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
/**
 * @author mchyzer
 * $Id: AttributeAssignAttributeDefDelegate.java,v 1.3 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * delegate privilege calls from attribute defs
 */
public class AttributeAssignAttributeDefDelegate extends AttributeAssignBaseDelegate {

  /**
   * reference to the attributedef in question
   */
  private AttributeDef attributeDef = null;
  
  /**
   * 
   * @param attributeDef1
   */
  public AttributeAssignAttributeDefDelegate(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }
  
  /**
   * @param attributeDefName
   * @param uuid
   * @return attribute assign
   */
  @Override
  AttributeAssign newAttributeAssign(String action, AttributeDefName attributeDefName, String uuid) {
    return new AttributeAssign(this.attributeDef, action, attributeDefName, uuid);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanReadAttributeDef(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  @Override
  public
  void assertCanReadAttributeDef(final AttributeDef attributeDefToAssign) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canReadAttribute = new boolean[1];
    final boolean[] canViewAttributeDefAssignTo = new boolean[1];
  
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canReadAttribute[0] = attributeDefToAssign.getPrivilegeDelegate().canAttrRead(subject);
        canViewAttributeDefAssignTo[0] = PrivilegeHelper.canAttrView(rootSession, AttributeAssignAttributeDefDelegate.this.attributeDef, subject);
        return null;
      }
    });
    
    if (!canReadAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot read attributeDef " + attributeDefToAssign.getName());
    }
  
    if (!canViewAttributeDefAssignTo[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot view attributeDef " + AttributeAssignAttributeDefDelegate.this.attributeDef.getName());
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanUpdateAttributeDefName(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public
  void assertCanUpdateAttributeDefName(AttributeDefName attributeDefName) {
    final AttributeDef attributeDefToAssign = attributeDefName.getAttributeDef();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canUpdateAttribute = new boolean[1];
    final boolean[] canAdminAttributeDefToAssignTo = new boolean[1];
 
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canUpdateAttribute[0] = attributeDefToAssign.getPrivilegeDelegate().canAttrUpdate(subject);
        canAdminAttributeDefToAssignTo[0] = PrivilegeHelper.canAttrAdmin(rootSession, AttributeAssignAttributeDefDelegate.this.attributeDef, subject);
        return null;
      }
    });
    
    if (!canUpdateAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot update attributeDef " + attributeDefToAssign.getName());
    }

    if (!canAdminAttributeDefToAssignTo[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot admin attributeDef " + AttributeAssignAttributeDefDelegate.this.attributeDef.getName());
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefNameId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefNameId(
      String attributeDefNameId) {
    return GrouperDAOFactory.getFactory().getAttributeAssign()
      .findByAttributeDefIdAndAttributeDefNameId(this.attributeDef.getId(), attributeDefNameId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefId(
      String attributeDefId) {
    return GrouperDAOFactory.getFactory()
    .getAttributeAssign().findByAttributeDefIdAndAttributeDefId(this.attributeDef.getUuid(), attributeDefId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwnerAndAttributeDefId(
      String attributeDefId) {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefNamesByAttributeDefIdAndAttributeDefId(this.attributeDef.getUuid(), attributeDefId);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "attributeDef", this.attributeDef)
      .toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanDelegateAttributeDefName(String, edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public
  void assertCanDelegateAttributeDefName(String action, AttributeDefName attributeDefName) {
    throw new RuntimeException("Cannot delegate an attribute on attribute assignment");
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanGrantAttributeDefName(String, edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public
  void assertCanGrantAttributeDefName(String action, AttributeDefName attributeDefName) {
    throw new RuntimeException("Cannot grant an attribute on attribute assignment");
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#getAttributeAssignable()
   */
  @Override
  public AttributeAssignable getAttributeAssignable() {
    return this.attributeDef;
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwner()
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwner() {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(this.attributeDef.getId()), null, null, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwner()
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwner() {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeDefNames(null, null, null, GrouperUtil.toSet(this.attributeDef.getId()),null, true);
  }

}
