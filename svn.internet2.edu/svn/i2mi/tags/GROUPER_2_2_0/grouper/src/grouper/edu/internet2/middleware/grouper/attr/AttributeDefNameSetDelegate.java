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
 * $Id: AttributeDefNameSetDelegate.java,v 1.3 2009-11-08 13:07:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * delegate the attribute def name set
 */
@SuppressWarnings("serial")
public class AttributeDefNameSetDelegate implements Serializable {

  /** keep a reference to the attribute def name */
  private AttributeDefName attributeDefName;
  
  /**
   * 
   * @param attributeDefName1
   */
  public AttributeDefNameSetDelegate(AttributeDefName attributeDefName1) {
    this.attributeDefName = attributeDefName1;

  }

  /**
   * get all the THEN rows from attributeDefNameSet about this id.  The ones returned
   * are implied if this one is assigned.  Those are the children, this is the parent
   * @return set of attributeDefNames, or empty set if none available
   */
  public Set<AttributeDefName> getAttributeDefNamesImpliedByThis() {
    return GrouperDAOFactory.getFactory().getAttributeDefNameSet().attributeDefNamesImpliedByThis(this.attributeDefName.getId());
  }
  
  /**
   * get attribute def names implied by this
   * @return names
   */
  public Set<String> getAttributeDefNameNamesImpliedByThis() {
    
    Set<String> names = new HashSet<String>();
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(this.getAttributeDefNamesImpliedByThis())) {
      names.add(attributeDefName.getName());
    }
    return names;
    
  }

  /**
   * get attribute def names implied by this immediate
   * @return names
   */
  public Set<String> getAttributeDefNameNamesImpliedByThisImmediate() {
    
    Set<String> names = new HashSet<String>();
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(this.getAttributeDefNamesImpliedByThisImmediate())) {
      names.add(attributeDefName.getName());
    }
    return names;
    
  }

  /**
   * get attribute def names that imply this immediate
   * @return names
   */
  public Set<String> getAttributeDefNameNamesThatImplyThisImmediate() {
    
    Set<String> names = new HashSet<String>();
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(this.getAttributeDefNamesThatImplyThisImmediate())) {
      names.add(attributeDefName.getName());
    }
    return names;
    
  }

  /**
   * get attribute def names that imply this immediate
   * @return names
   */
  public Set<String> getAttributeDefNameNamesThatImplyThis() {
    
    Set<String> names = new HashSet<String>();
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(this.getAttributeDefNamesThatImplyThis())) {
      names.add(attributeDefName.getName());
    }
    return names;
    
  }

  /**
   * get all the THEN rows from attributeDefNameSet about this id (immediate only).  The ones returned
   * are implied if this one is assigned.  Those are the children, this is the parent
   * @return set of attributeDefNames, or empty set if none available
   */
  public Set<AttributeDefName> getAttributeDefNamesImpliedByThisImmediate() {
    return GrouperDAOFactory.getFactory().getAttributeDefNameSet().attributeDefNamesImpliedByThisImmediate(this.attributeDefName.getId());
  }

  /**
   * get all the IF rows from attributeDefNameSet about this id.  The ones returned imply that
   * this is also assigned.  Those are the parents, this is the child.
   * @return set of attributeDefNames, or empty set if none available
   */
  public Set<AttributeDefName> getAttributeDefNamesThatImplyThis() {
    return GrouperDAOFactory.getFactory().getAttributeDefNameSet()
      .attributeDefNamesThatImplyThis(this.attributeDefName.getId());
  }

  /**
   * get all the IF rows from attributeDefNameSet about this id (immediate only).  The ones returned imply that
   * this is also assigned.  Those are the parents, this is the child.
   * @return set of attributeDefNames, or empty set if none available
   */
  public Set<AttributeDefName> getAttributeDefNamesThatImplyThisImmediate() {
    return GrouperDAOFactory.getFactory().getAttributeDefNameSet()
      .attributeDefNamesThatImplyThisImmediate(this.attributeDefName.getId());
  }

  /**
   * for instance if the argument is arts and sciences, and this is all, then calling this will 
   * allow all to imply arts and sciences
   * @param newAttributeDefName
   * @return true if added, false if already there
   */
  public boolean addToAttributeDefNameSet(AttributeDefName newAttributeDefName) {
    return internal_addToAttributeDefNameSet(newAttributeDefName, null);
  }

  /**
   * @param uuid is uuid or null to generate one
   * @param newAttributeDefName
   * @return true if added, false if already there
   */
  public boolean internal_addToAttributeDefNameSet(AttributeDefName newAttributeDefName, String uuid) {
    assertCanAdminAttributeDefs(newAttributeDefName);
    return GrouperSetEnum.ATTRIBUTE_SET.addToGrouperSet(this.attributeDefName, newAttributeDefName, uuid);
  }

  /**
   * 
   * @param attributeDefNameToRemove
   * @return true if removed, false if already removed
   */
  public boolean removeFromAttributeDefNameSet(AttributeDefName attributeDefNameToRemove) {
    assertCanAdminAttributeDefs(attributeDefNameToRemove);
    return GrouperSetEnum.ATTRIBUTE_SET.removeFromGrouperSet(this.attributeDefName, attributeDefNameToRemove);
  }

  /**
   * 
   * @param other
   */
  private void assertCanAdminAttributeDefs(AttributeDefName other) {
    this.attributeDefName.assertCanAdminAttributeDefStatic();
    other.assertCanAdminAttributeDefStatic();
  }

}
