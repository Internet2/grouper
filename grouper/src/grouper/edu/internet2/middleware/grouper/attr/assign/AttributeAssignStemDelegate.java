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
 * $Id: AttributeAssignStemDelegate.java,v 1.5 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
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
public class AttributeAssignStemDelegate extends AttributeAssignBaseDelegate {

  /**
   * reference to the stem in question
   */
  private Stem stem = null;
  
  /**
   * 
   * @param stem1
   */
  public AttributeAssignStemDelegate(Stem stem1) {
    this.stem = stem1;
  }
  

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanReadAttributeDef(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  @Override
  public
  void assertCanReadAttributeDef(final AttributeDef attributeDef) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canReadAttribute = new boolean[1];
    final boolean[] canStemAttrRead = new boolean[1];

    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canReadAttribute[0] = attributeDef.getPrivilegeDelegate().canAttrRead(subject);
        
        //can be stem or create or stemAttrRead to read an attribute
        canStemAttrRead[0] = PrivilegeHelper.canStemAttrRead(rootSession, AttributeAssignStemDelegate.this.stem, subject);
        if (!canStemAttrRead[0]) {
          canStemAttrRead[0] = PrivilegeHelper.canCreate(rootSession, AttributeAssignStemDelegate.this.stem, subject);
        }
        if (!canStemAttrRead[0]) {
          canStemAttrRead[0] = PrivilegeHelper.canStem(AttributeAssignStemDelegate.this.stem, subject);
        }
        return null;
      }
    });
    
    if (!canReadAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot read attributeDef " + attributeDef.getName());
    }
    
    if (!canStemAttrRead[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot create/stem/stemAttrRead in stem " + stem.getName());
    }
  
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#newAttributeAssign(java.lang.String, edu.internet2.middleware.grouper.attr.AttributeDefName, java.lang.String)
   */
  @Override
  AttributeAssign newAttributeAssign(String action, AttributeDefName attributeDefName, String uuid) {
    return new AttributeAssign(this.stem, action, attributeDefName, uuid);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanUpdateAttributeDefName(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public
  void assertCanUpdateAttributeDefName(AttributeDefName attributeDefName) {
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canUpdateAttribute = new boolean[1];
    final boolean[] canStemAttrUpdate = new boolean[1];
 
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canUpdateAttribute[0] = attributeDef.getPrivilegeDelegate().canAttrUpdate(subject);
        
        //can be stem or create or stemAttrUpdate to assign an attribute
        canStemAttrUpdate[0] = PrivilegeHelper.canStemAttrUpdate(rootSession, AttributeAssignStemDelegate.this.stem, subject);
        if (!canStemAttrUpdate[0]) {
          canStemAttrUpdate[0] = PrivilegeHelper.canCreate(rootSession, AttributeAssignStemDelegate.this.stem, subject);
        }
        if (!canStemAttrUpdate[0]) {
          canStemAttrUpdate[0] = PrivilegeHelper.canStem(AttributeAssignStemDelegate.this.stem, subject);
        }
        return null;
      }
    });
    
    if (!canUpdateAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot update attributeDef " + attributeDef.getName());
    }

    if (!canStemAttrUpdate[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot create/stem/stemAttrUpdate in stem " + stem.getName());
    }

  }


  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefNameId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefNameId(
      String attributeDefNameId) {
    return GrouperDAOFactory.getFactory().getAttributeAssign()
      .findByStemIdAndAttributeDefNameId(this.stem.getUuid(), attributeDefNameId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefId(
      String attributeDefId) {
    return GrouperDAOFactory.getFactory()
    .getAttributeAssign().findByStemIdAndAttributeDefId(this.stem.getUuid(), attributeDefId);
  }


  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwnerAndAttributeDefId(
      String attributeDefId) {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefNamesByStemIdAndAttributeDefId(this.stem.getUuid(), attributeDefId);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "stem", this.stem)
      .toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanDelegateAttributeDefName(String, edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public
  void assertCanDelegateAttributeDefName(String action, AttributeDefName attributeDefName) {
    throw new RuntimeException("Cannot delegate an attribute on stem assignment");
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanGrantAttributeDefName(String, edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public
  void assertCanGrantAttributeDefName(String action, AttributeDefName attributeDefName) {
    throw new RuntimeException("Cannot grant an attribute on stem assignment");
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#getAttributeAssignable()
   */
  @Override
  public AttributeAssignable getAttributeAssignable() {
    return this.stem;
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwner()
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwner() {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(this.stem.getUuid()), null, null, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwner()
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwner() {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeDefNames(null, null, null, GrouperUtil.toSet(this.stem.getUuid()),null, true);
  }

}
