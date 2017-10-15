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
 * $Id: AttributeDefFinder.java,v 1.2 2009-09-28 20:30:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;


/**
 * finder methods for attribute assign
 */
public class AttributeAssignFinder {

  /**
   * use security around attribute def?  default is true
   */
  private boolean attributeCheckReadOnAttributeDef = true;
  
  /**
   * use security around attribute def?  default is true
   * @param theAttributeDefNameUseSecurity
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeCheckReadOnAttributeDef(boolean theAttributeDefNameUseSecurity) {
    this.attributeCheckReadOnAttributeDef = theAttributeDefNameUseSecurity;
    return this;
  }
  
  /**
   * attribute def names ids
   */
  private Collection<String> attributeDefNameIds;
  
  /**
   * attribute def name id to find
   * @param attributeDefNameId
   * @return this for chaining
   */
  public AttributeAssignFinder addAttributeDefNameId(String attributeDefNameId) {
    if (this.attributeDefNameIds == null) {
      this.attributeDefNameIds = new LinkedHashSet<String>();
    }
    this.attributeDefNameIds.add(attributeDefNameId);
    return this;
  }
  
  /**
   * attribute def name ids to find
   * @param theAttributeDefNameIds
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeDefNameIds(Collection<String> theAttributeDefNameIds) {
    this.attributeDefNameIds = theAttributeDefNameIds;
    return this;
  }
  
  /**
   * 
   */
  private Collection<String> ownerGroupIds;
  
  /**
   * add owner group id
   * @param ownerGroupId
   * @return this for chaining
   */
  public AttributeAssignFinder addOwnerGroupId(String ownerGroupId) {
    if (this.ownerGroupIds == null) {
      this.ownerGroupIds = new LinkedHashSet<String>();
    }
    this.ownerGroupIds.add(ownerGroupId);
    return this;
  }
  
  /**
   * add owner group id
   * @param ownerGroupIds1
   * @return this for chaining
   */
  public AttributeAssignFinder assignOwnerGroupIds(Collection<String> ownerGroupIds1) {
    this.ownerGroupIds = ownerGroupIds1;
    return this;
  }
  
  /**
   * 
   */
  private Collection<String> ownerStemIds;
  
  /**
   * add owner stem id
   * @param ownerStemId
   * @return this for chaining
   */
  public AttributeAssignFinder addOwnerStemId(String ownerStemId) {
    if (this.ownerStemIds == null) {
      this.ownerStemIds = new LinkedHashSet<String>();
    }
    this.ownerStemIds.add(ownerStemId);
    return this;
  }
  
  /**
   * add owner stem id
   * @param ownerStemIds1
   * @return this for chaining
   */
  public AttributeAssignFinder assignOwnerStemIds(Collection<String> ownerStemIds1) {
    this.ownerStemIds = ownerStemIds1;
    return this;
  }
  
  /**
   * if assignments on assignments should also be included
   */
  private boolean includeAssignmentsOnAssignments = false;
  
  /**
   * if assignments on assignments should also be included
   * @param theIncludeAssignAssignmentsOnAssignments
   * @return this for chaining
   */
  public AttributeAssignFinder assignIncludeAssignmentsOnAssignments(boolean theIncludeAssignAssignmentsOnAssignments) {
    this.includeAssignmentsOnAssignments = theIncludeAssignAssignmentsOnAssignments;
    return this;
  }
  
  /**
   * find all the attribute assigns
   * @return the set of groups or the empty set if none found
   */
  public Set<AttributeAssign> findAttributeAssigns() {
  
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.emptySetOfLookupsReturnsNoResults", true)) {
  
      // if passed in empty set of group ids and no names, then no groups found
      if (this.ownerGroupIds != null && this.ownerGroupIds.size() == 0) {
        return new HashSet<AttributeAssign>();
      }
      
    }

    if (this.ownerGroupIds != null && this.ownerStemIds != null) {
      throw new RuntimeException("Cant pass in owner groups and owner stems");
    }

    if (this.ownerGroupIds != null) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssign()
          .findGroupAttributeAssignments(null, null, this.attributeDefNameIds, this.ownerGroupIds, null, true, 
              this.includeAssignmentsOnAssignments, null, null, null, this.attributeCheckReadOnAttributeDef);

    }
    
    if (this.ownerStemIds != null) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssign()
          .findStemAttributeAssignments(null, null, this.attributeDefNameIds, this.ownerStemIds, null, true, 
              this.includeAssignmentsOnAssignments, null, null, null, this.attributeCheckReadOnAttributeDef);

    }
    
    throw new RuntimeException("Bad query");
  }

  /**
   * find an attributeAssign by id.  This is a secure method, a GrouperSession must be open
   * @param id of attributeAssign
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute assign or null
   * @throws AttributeAssignNotFoundException
   */
  public static AttributeAssign findById(String id, boolean exceptionIfNull) {
    
    AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(id, exceptionIfNull);
    
    //at this point no exception should be thrown
    if (attributeAssign == null) {
      return null;
    }
    
    //now we need to check security
    if (PrivilegeHelper.canViewAttributeAssign(GrouperSession.staticGrouperSession(), attributeAssign, true)) {
      return attributeAssign;
    }
    if (exceptionIfNull) {
      throw new AttributeAssignNotFoundException("Not allowed to view attribute assign by id: " + id);
    }
    return null;
  }  

}
