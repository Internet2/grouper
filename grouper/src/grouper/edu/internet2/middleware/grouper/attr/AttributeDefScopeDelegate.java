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
package edu.internet2.middleware.grouper.attr;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;


/**
 * delegate scope calls from attribute defs
 */
public class AttributeDefScopeDelegate {

  /**
   * reference to the attributedef in question
   */
  private AttributeDef attributeDef = null;
  
  /**
   * 
   * @param attributeDef1
   */
  public AttributeDefScopeDelegate(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }
  
  /**
   * 
   * @param attributeDefScopeType 
   * @param scopeString 
   * @param scopeString2 
   * @return attribute assign
   */
  public AttributeDefScope addScope(AttributeDefScopeType attributeDefScopeType, String scopeString, String scopeString2) {
    return internal_addScope(attributeDefScopeType, scopeString, scopeString2, null);
  }
  
  /**
   * adds scope if not already there
   * @param attributeDefScopeType 
   * @param scopeString 
   * @param scopeString2 
   * @return attribute assign
   */
  public AttributeDefScope assignScope(AttributeDefScopeType attributeDefScopeType, String scopeString, String scopeString2) {
    AttributeDefScope attributeDefScope = retrieveAttributeDefScope(attributeDefScopeType, scopeString, scopeString2);
    if (attributeDefScope == null) {
      attributeDefScope = internal_addScope(attributeDefScopeType, scopeString, scopeString2, null);
    }
    return attributeDefScope;
    
  }
  
  /**
   * adds scope if not already there
   * make sure this attributeDef is assigned directly in this stem
   * @param stem
   * @return the attribute def scope
   */
  public AttributeDefScope assignStemSubScope(Stem stem) {
    return this.assignScope(AttributeDefScopeType.nameLike, stem.getName() + ":%", null);
  }
  
  /**
   * adds scope if not already there
   * make sure this attributeDef is assigned directly in this stem
   * @param stem
   * @return the attribute def scope
   */
  public AttributeDefScope assignStemScope(Stem stem) {
    return this.assignScope(AttributeDefScopeType.inStem, stem.getUuid(), 
        stem.getName());
  }
  
  /**
   * adds scope if not already there
   * make sure this attributeDef is assigned directly in this stem
   * @param attributeDefName
   * @return the attribute def scope
   */
  public AttributeDefScope assignTypeDependence(AttributeDefName attributeDefName) {
    return this.assignTypeDependence(attributeDefName, null);
  }

  /**
   * adds scope if not already there
   * make sure this attributeDef is assigned to something that as another assignment
   * @param attributeDefName
   * @param action 
   * @return the attribute def scope
   */
  public AttributeDefScope assignTypeDependence(AttributeDefName attributeDefName, String action) {
    
    //make sure you can read the attribute to read
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    if (!PrivilegeHelper.canAttrAdmin(grouperSession, attributeDefName.getAttributeDef(), 
        grouperSession.getSubject())) {
      throw new RuntimeException("Cannot attr_admin attributeDef: " + this.attributeDef.getName());
    }
    
    return this.assignScope(AttributeDefScopeType.attributeDefNameIdAssigned, 
        attributeDefName.getId(), action);
  }
  
  /**
   * adds scope if not already there
   * make sure this attributeDef is assigned to something of a given name
   * @param name
   * @return the attribute def scope
   */
  public AttributeDefScope assignOwnerNameEquals(String name) {
    return this.assignScope(AttributeDefScopeType.nameEquals, 
        name, null);
  }
  
  /**
   * adds scope if not already there
   * make sure this attributeDef is assigned to this group
   * @param group
   * @return the attribute def scope
   */
  public AttributeDefScope assignOwnerGroup(Group group) {
    return this.assignScope(AttributeDefScopeType.idEquals, 
        group.getId(), group.getName());
  }
  
  
  /**
   * adds scope if not already there
   * make sure this attributeDef is assigned to this stem
   * @param stem
   * @return the attribute def scope
   */
  public AttributeDefScope assignOwnerStem(Stem stem) {
    return this.assignScope(AttributeDefScopeType.idEquals, 
        stem.getUuid(), stem.getName());
  }
  
  /**
   * adds scope if not already there
   * make sure this attributeDef is to this attribute def
   * @param attributeDef
   * @return the attribute def scope
   */
  public AttributeDefScope assignOwnerAttributeDef(AttributeDef attributeDef) {
    return this.assignScope(AttributeDefScopeType.idEquals, 
        attributeDef.getId(), attributeDef.getName());
  }
  
  /**
   * adds scope if not already there
   * make sure this attributeDef is assigned directly in this stem
   * @param membership
   * @return the attribute def scope
   */
  public AttributeDefScope assignOwnerMembership(Membership membership) {
    
    if (StringUtils.equals("immediate", membership.getType())) {
      return this.assignScope(AttributeDefScopeType.idEquals, 
          membership.getImmediateMembershipId(), null);
    } 
    throw new RuntimeException("Only immediate membership ids can be owners of attribute def scopes");
    
  }
  
  /**
   * adds scope if not already there
   * make sure this attributeDef is assigned to members in this source
   * @param source
   * @return the attribute def scope
   */
  public AttributeDefScope assignOwnerSource(Source source) {
    return this.assignScope(AttributeDefScopeType.sourceId, 
        source.getId(), null);
  }
  
  /**
   * remove all scopes with this type and scope string
   * @param attributeDefScopeType
   * @param scopeString
   * @return set of attribute def scopes removes
   */
  public Set<AttributeDefScope> removeScope(AttributeDefScopeType attributeDefScopeType, 
      String scopeString) {
    
    //make sure you can admin the constraint scope
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    if (!PrivilegeHelper.canAttrAdmin(grouperSession, this.attributeDef, grouperSession.getSubject())) {
      throw new RuntimeException("Cannot attr_admin attributeDef: " + this.attributeDef.getName());
    }

    Set<AttributeDefScope> attributeDefScopes = new HashSet<AttributeDefScope>();
    for (AttributeDefScope attributeDefScope : this.retrieveAttributeDefScopes()) {
      if (GrouperUtil.equals(attributeDefScopeType, attributeDefScope.getAttributeDefScopeType())
          && StringUtils.equals(StringUtils.trimToNull(scopeString), StringUtils.trimToNull(attributeDefScope.getScopeString()))) {
        attributeDefScopes.add(attributeDefScope);
        attributeDefScope.delete();
      }
    }
    return attributeDefScopes;
  }
  
  /**
   * remove all scopes with this type, scope string, and scope string2
   * @param attributeDefScopeType
   * @param scopeString
   * @param scopeString2 
   * @return set of attribute def scopes removes
   */
  public Set<AttributeDefScope> removeScope(AttributeDefScopeType attributeDefScopeType, 
      String scopeString, String scopeString2) {
    
    //make sure you can admin the constraint scope
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    if (!PrivilegeHelper.canAttrAdmin(grouperSession, this.attributeDef, grouperSession.getSubject())) {
      throw new RuntimeException("Cannot attr_admin attributeDef: " + this.attributeDef.getName());
    }
    
    Set<AttributeDefScope> attributeDefScopes = new HashSet<AttributeDefScope>();
    for (AttributeDefScope attributeDefScope : this.retrieveAttributeDefScopes()) {
      if (GrouperUtil.equals(attributeDefScopeType, attributeDefScope.getAttributeDefScopeType())
          && StringUtils.equals(StringUtils.trimToNull(scopeString), 
              StringUtils.trimToNull(attributeDefScope.getScopeString()))
          && StringUtils.equals(StringUtils.trimToNull(scopeString2), 
              StringUtils.trimToNull(attributeDefScope.getScopeString2()))) {
        attributeDefScopes.add(attributeDefScope);
        attributeDefScope.delete();
      }
    }
    return attributeDefScopes;
    
  }
  
  /**
   * @param attributeDefScopeType 
   * @param scopeString 
   * @param scopeString2 
   * @param uuid is uuid or null for generated
   * @return attribute assign
   */
  public AttributeDefScope internal_addScope(AttributeDefScopeType attributeDefScopeType, String scopeString, String scopeString2, String uuid) {
    
    //make sure you can admin the constraint scope
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    if (!PrivilegeHelper.canAttrAdmin(grouperSession, this.attributeDef, grouperSession.getSubject())) {
      throw new RuntimeException("Cannot attr_admin attributeDef: " + this.attributeDef.getName());
    }
    
    AttributeDefScope attributeDefScope = new AttributeDefScope();
    attributeDefScope.setAttributeDefScopeType(attributeDefScopeType);
    attributeDefScope.setScopeString(scopeString);
    attributeDefScope.setScopeString2(scopeString2);
    attributeDefScope.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);
    attributeDefScope.setAttributeDefId(this.attributeDef.getId());
    attributeDefScope.saveOrUpdate();
    return attributeDefScope;
  }

  /**
   * find a scope and return it if there or null if not
   * @param attributeDefScopeType
   * @param scopeString
   * @param scopeString2
   * @return the scope if there
   */
  public AttributeDefScope retrieveAttributeDefScope(AttributeDefScopeType attributeDefScopeType, String scopeString, String scopeString2) {
    for (AttributeDefScope attributeDefScope : this.retrieveAttributeDefScopes()) {
      if (GrouperUtil.equals(attributeDefScopeType, attributeDefScope.getAttributeDefScopeType())
          && StringUtils.equals(scopeString, attributeDefScope.getScopeString())
          && StringUtils.equals(scopeString2, attributeDefScope.getScopeString2())) {
        return attributeDefScope;
      }
    }
    return null;
  }
  
  /**
   * 
   * @return scopes (non null)
   */
  public Set<AttributeDefScope> retrieveAttributeDefScopes() {
    return retrieveAttributeDefScopes(null);
  }

  /**
   * 
   * @param queryOptions 
   * @return scopes (non null)
   */
  public Set<AttributeDefScope> retrieveAttributeDefScopes(QueryOptions queryOptions) {
    return GrouperDAOFactory.getFactory().getAttributeDefScope().findByAttributeDefId(this.attributeDef.getId(), queryOptions);
  }

}
