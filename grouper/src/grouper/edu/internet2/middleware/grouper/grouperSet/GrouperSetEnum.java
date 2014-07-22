/**
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
 */
/**
 * @author mchyzer
 * $Id: GrouperSetEnum.java,v 1.5 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.AttributeDefAssignmentType;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionType;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.RoleHierarchyType;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public enum GrouperSetEnum {

  /** attribute set grouper set */
  ATTRIBUTE_SET {

    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByIfThenImmediate(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public GrouperSet findByIfThenImmediate(String idIf, String idThen,
        boolean exceptionIfNotFound) {
      
      //lets see if this one already exists
      AttributeDefNameSet existingAttributeDefNameSet = GrouperDAOFactory.getFactory()
        .getAttributeDefNameSet().findByIfThenImmediate(idIf, 
          idThen, exceptionIfNotFound);
      return existingAttributeDefNameSet;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByIfHasElementId(java.lang.String)
     */
    @Override
    public Set<? extends GrouperSet> findByIfHasElementId(String idIf) {
      Set<AttributeDefNameSet> existingAttributeDefNameSetList = 
        GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(idIf);
      return existingAttributeDefNameSetList;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByIfThenHasElementId(java.lang.String, java.lang.String)
     */
    @Override
    public Set<? extends GrouperSet> findByIfThenHasElementId(String idForThens, String idForIfs) {
      Set<AttributeDefNameSet> candidateAttributeDefNameSetToRemove = 
        GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenHasAttributeDefNameId(
            idForThens, idForIfs);
      return candidateAttributeDefNameSetToRemove;
    }
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByThenHasElementId(java.lang.String)
     */
    @Override
    public Set<? extends GrouperSet> findByThenHasElementId(String idThen) {
      Set<AttributeDefNameSet> existingAttributeDefNameSetList = 
        GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByThenHasAttributeDefNameId(idThen);
      return existingAttributeDefNameSetList;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#newInstance(String, String, int, String)
     */
    @Override
    public GrouperSet newInstance(String ifHasId, String thenHasId, int depth, String uuid) {
      AttributeDefNameSet attributeDefNameSet = new AttributeDefNameSet();
      attributeDefNameSet.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid );
      attributeDefNameSet.setDepth(depth);
      attributeDefNameSet.setIfHasAttributeDefNameId(ifHasId);
      attributeDefNameSet.setThenHasAttributeDefNameId(thenHasId);
      attributeDefNameSet.setType(depth == 1 ? 
          AttributeDefAssignmentType.immediate : AttributeDefAssignmentType.effective);
      return attributeDefNameSet;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findById(java.lang.String, boolean)
     */
    @Override
    public GrouperSet findById(String id, boolean exceptionIfNull) {
      return GrouperDAOFactory.getFactory()
        .getAttributeDefNameSet().findById(id, exceptionIfNull);
    }
  },
  /** role set grouper set */
  ROLE_SET {

    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByIfThenImmediate(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public GrouperSet findByIfThenImmediate(String idIf, String idThen,
        boolean exceptionIfNotFound) {
      
      //lets see if this one already exists
      RoleSet existingRoleSet = GrouperDAOFactory.getFactory()
        .getRoleSet().findByIfThenImmediate(idIf, 
          idThen, exceptionIfNotFound);
      return existingRoleSet;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByIfHasElementId(java.lang.String)
     */
    @Override
    public Set<? extends GrouperSet> findByIfHasElementId(String idIf) {
      Set<RoleSet> existingRoleSetList = 
        GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(idIf);
      return existingRoleSetList;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByIfThenHasElementId(java.lang.String, java.lang.String)
     */
    @Override
    public Set<? extends GrouperSet> findByIfThenHasElementId(String idForThens, String idForIfs) {
      Set<RoleSet> candidateRoleSetToRemove = 
        GrouperDAOFactory.getFactory().getRoleSet().findByIfThenHasRoleId(
            idForThens, idForIfs);
      return candidateRoleSetToRemove;
    }
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByThenHasElementId(java.lang.String)
     */
    @Override
    public Set<? extends GrouperSet> findByThenHasElementId(String idThen) {
      Set<RoleSet> existingRoleSetList = 
        GrouperDAOFactory.getFactory().getRoleSet().findByThenHasRoleId(idThen);
      return existingRoleSetList;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#newInstance(String, String, int, String)
     */
    @Override
    public GrouperSet newInstance(String ifHasId, String thenHasId, int depth, String uuid) {
      RoleSet roleSet = new RoleSet();
      roleSet.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);
      roleSet.setDepth(depth);
      roleSet.setIfHasRoleId(ifHasId);
      roleSet.setThenHasRoleId(thenHasId);
      roleSet.setType(depth == 1 ? 
          RoleHierarchyType.immediate : RoleHierarchyType.effective);
      return roleSet;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findById(java.lang.String, boolean)
     */
    @Override
    public GrouperSet findById(String id, boolean exceptionIfNull) {
      return GrouperDAOFactory.getFactory()
        .getAttributeDefNameSet().findById(id, exceptionIfNull);
    }
  }, 
  /** attribute assign action set grouper set */
  ATTRIBUTE_ASSIGN_ACTION_SET {
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByIfThenImmediate(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public GrouperSet findByIfThenImmediate(String idIf, String idThen,
        boolean exceptionIfNotFound) {
      
      //lets see if this one already exists
      AttributeAssignActionSet existingAttributeAssignActionSet = GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSet().findByIfThenImmediate(idIf, 
          idThen, exceptionIfNotFound);
      return existingAttributeAssignActionSet;
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByIfHasElementId(java.lang.String)
     */
    @Override
    public Set<? extends GrouperSet> findByIfHasElementId(String idIf) {
      Set<AttributeAssignActionSet> existingAttributeAssignActionSetList = 
        GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(idIf);
      return existingAttributeAssignActionSetList;
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByIfThenHasElementId(java.lang.String, java.lang.String)
     */
    @Override
    public Set<? extends GrouperSet> findByIfThenHasElementId(String idForThens, String idForIfs) {
      Set<AttributeAssignActionSet> candidateAttributeAssignActionSetToRemove = 
        GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenHasAttributeAssignActionId(
            idForThens, idForIfs);
      return candidateAttributeAssignActionSetToRemove;
    }
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findByThenHasElementId(java.lang.String)
     */
    @Override
    public Set<? extends GrouperSet> findByThenHasElementId(String idThen) {
      Set<AttributeAssignActionSet> existingAttributeAssignActionSetList = 
        GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByThenHasAttributeAssignActionId(idThen);
      return existingAttributeAssignActionSetList;
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#newInstance(String, String, int, String)
     */
    @Override
    public GrouperSet newInstance(String ifHasId, String thenHasId, int depth, String uuid) {
      AttributeAssignActionSet attributeAssignActionSet = new AttributeAssignActionSet();
      attributeAssignActionSet.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);
      attributeAssignActionSet.setDepth(depth);
      attributeAssignActionSet.setIfHasAttrAssignActionId(ifHasId);
      attributeAssignActionSet.setThenHasAttrAssignActionId(thenHasId);
      attributeAssignActionSet.setType(depth == 1 ? 
          AttributeAssignActionType.immediate : AttributeAssignActionType.effective);
      return attributeAssignActionSet;
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum#findById(java.lang.String, boolean)
     */
    @Override
    public GrouperSet findById(String id, boolean exceptionIfNull) {
      return GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSet().findById(id, exceptionIfNull);
    }
  }
  
  ;

  /**
   * parent child set for calulcating
   */
  private static class GrouperSetPair implements Comparable {
    /** parent */
    private GrouperSet parent;
    /** child */
    private GrouperSet child;
    /** number of hops from one to another */
    private int depth;
  
    /**
     * sort these by depth so we create the path as we go
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
      return ((Integer)this.depth).compareTo(((GrouperSetPair)o).depth);
    }
    
    
  }

  /**
   * find an grouper set, better be here
   * @param grouperSetPairs
   * @param grouperSets 
   * @param id to find
   * @return the def name set
   */
  private GrouperSet find(List<GrouperSetPair> grouperSetPairs, 
      List<GrouperSet> grouperSets, String id) {
    for (GrouperSetPair grouperSetPair : grouperSetPairs) {
      if (StringUtils.equals(id, grouperSetPair.parent.__getId())) {
        return grouperSetPair.parent;
      }
      if (StringUtils.equals(id, grouperSetPair.child.__getId())) {
        return grouperSetPair.child;
      }
    }
    for (GrouperSet grouperSet : GrouperUtil.nonNull(grouperSets)) {
      if (StringUtils.equals(id, grouperSet.__getId())) {
        return grouperSet;
      }
    }
    String grouperElementIfName = "<unknown>";
    String grouperElementThenName = "<unknown>";
    try {
      GrouperSet grouperSet = GrouperDAOFactory.getFactory()
        .getAttributeDefNameSet().findById(id, true);
      grouperElementIfName = grouperSet.__getIfHasElement().__getName();
      grouperElementThenName = grouperSet.__getThenHasElement().__getName();
    } catch (Exception e) {
      grouperElementIfName = "<exception: " + e.getMessage() + ">";
      grouperElementThenName = "<exception: " + e.getMessage() + ">";
    }
    throw new RuntimeException("Cant find grouper set with id: " + id
        + " ifName: " + grouperElementIfName + ", thenName: " + grouperElementThenName);
  }
  
  /**
   * find an attribute def name set, better be here
   * @param grouperSetPairs
   * @param grouperSets 
   * @param ifHasId 
   * @param thenHasId 
   * @param depth is the depth expecting
   * @return the def name set
   */
  private GrouperSet find(List<GrouperSetPair> grouperSetPairs,
      List<GrouperSet> grouperSets, String ifHasId, String thenHasId, int depth) {
    //are we sure we are getting the right one here??? 
    for (GrouperSetPair attributeDefNameSetPair : grouperSetPairs) {
      if (StringUtils.equals(ifHasId, attributeDefNameSetPair.parent.__getIfHasElementId())
          && StringUtils.equals(thenHasId, attributeDefNameSetPair.parent.__getThenHasElementId())
          && depth == attributeDefNameSetPair.parent.__getDepth()) {
        return attributeDefNameSetPair.parent;
      }
      if (StringUtils.equals(ifHasId, attributeDefNameSetPair.child.__getIfHasElementId())
          && StringUtils.equals(thenHasId, attributeDefNameSetPair.child.__getThenHasElementId())
          && depth == attributeDefNameSetPair.child.__getDepth()) {
        return attributeDefNameSetPair.child;
      }
    }
    for (GrouperSet grouperSet : GrouperUtil.nonNull(grouperSets)) {
      if (StringUtils.equals(ifHasId, grouperSet.__getIfHasElementId())
          && StringUtils.equals(thenHasId, grouperSet.__getThenHasElementId())
          && depth == grouperSet.__getDepth()) {
        return grouperSet;
      }
    }
    throw new RuntimeException("Cant find grouper set with ifHasId: " 
        + ifHasId + ", thenHasId: " + thenHasId + ", depth: " + depth);
  }

  
  /**
   * find an element by if then immediate (or null
   * @param idIf
   * @param idThen
   * @param exceptionIfNotFound true if exception if not found
   * @return the element if found
   */
  public abstract GrouperSet findByIfThenImmediate(String idIf, String idThen, boolean exceptionIfNotFound);
  
  /**
   * find set of sets which have a then as a certain value
   * @param idThen
   * @return the set
   */
  public abstract Set<? extends GrouperSet> findByThenHasElementId(String idThen);
  
  /**
   * find set of sets which have an if as a certain value
   * @param idIf
   * @return the set
   */
  public abstract Set<? extends GrouperSet> findByIfHasElementId(String idIf);
  
  /**
   * find by id if has id
   * @param id
   * @param exceptionIfNull
   * @return the grouper set
   */
  public abstract GrouperSet findById(String id, boolean exceptionIfNull);
  
  /**
   * new instance of the grouper set object
   * @param ifHasId 
   * @param thenHasId 
   * @param depth 
   * @param uuid is uuid or null if generate one
   * @return the grouper set
   */
  public abstract GrouperSet newInstance(String ifHasId, String thenHasId, int depth, String uuid);
  
  /**
   * find canidates to delete by if and then
   * @param idForThens
   * @param idForIfs
   * @return the canidate set to delete
   */
  public abstract Set<? extends GrouperSet> findByIfThenHasElementId(String idForThens, String idForIfs);

  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperSetEnum.class);

  /**
   * 
   * @param containerSetElement 
   * @param newElement 
   * @param uuid is the uuid or null to generate
   * @return true if added, false if already there
   */
  public boolean addToGrouperSet(GrouperSetElement containerSetElement, GrouperSetElement newElement, String uuid) {

    //TODO, if doesnt point to itself, add a self referential record
    if (LOG.isDebugEnabled()) {
      LOG.debug("Adding to grouper set " + this.name() + ": " + containerSetElement.__getName() + "\n  (" + containerSetElement.__getId() + ")" 
          + " this attribute: " + newElement.__getName() + " (" + newElement.__getId() + ")");
    }
    
    //lets see if this one already exists
    GrouperSet existingSet = this.findByIfThenImmediate(containerSetElement.__getId(), 
          newElement.__getId(), false);
    if (existingSet != null) {
      return false;
    }
    
    //lets see what implies having this existing def name
    Set<? extends GrouperSet> existingGrouperSetList = 
      this.findByThenHasElementId(containerSetElement.__getId());
  
    //lets see what having this new def name implies
    Set<? extends GrouperSet> newGrouperSetList = 
      this.findByIfHasElementId(newElement.__getId());
    
    List<GrouperSetPair> grouperSetPairs = new ArrayList<GrouperSetPair>();
    List<GrouperSet> newSets = new ArrayList<GrouperSet>();
    
    //now lets merge the two lists
    //they each must have one member
    for (GrouperSet parent : existingGrouperSetList) {
      for (GrouperSet child : newGrouperSetList) {
        GrouperSetPair grouperSetPair = new GrouperSetPair();
        
        grouperSetPair.depth = 1 + parent.__getDepth() + child.__getDepth();
  
        grouperSetPair.parent = parent;
        grouperSetPair.child = child;
        grouperSetPairs.add(grouperSetPair);
        if (LOG.isDebugEnabled()) {
          GrouperSetElement ifHasElement = parent.__getIfHasElement();
          GrouperSetElement thenHasElement = child.__getThenHasElement();
          LOG.debug("Found pair to manage " + this.name() + ": " + ifHasElement.__getName() 
              + "\n  (parent set: " + parent.__getId() + ", ifHasNameId: " + ifHasElement.__getId() + ")"
              + "\n  to: " + thenHasElement.__getName()
              + "(child set: " + child.__getId() + ", thenHasNameId: " + thenHasElement.__getId() + ")"
              + "\n  depth: " + grouperSetPair.depth
          );
        }
        
      }
    }
  
    //sort by depth so we process correctly
    Collections.sort(grouperSetPairs);
    
    //if has circular, then do more queries to be sure
    boolean hasCircularReference = false;
    OUTER: for (GrouperSetPair grouperSetPair : grouperSetPairs) {
      
      //check for circular reference
      if (StringUtils.equals(grouperSetPair.parent.__getIfHasElementId(), 
          grouperSetPair.child.__getThenHasElementId())
          && grouperSetPair.depth > 0) {
        if (LOG.isDebugEnabled()) {
          GrouperSetElement grouperSetElement = grouperSetPair.parent.__getIfHasElement();
          LOG.debug("Found circular reference, skipping " + this.name() + ": " + grouperSetElement.__getName() 
              + "\n  (" + grouperSetPair.parent.__getIfHasElementId() 
              + ", depth: " + grouperSetPair.depth + ")");
        }
        hasCircularReference = true;
        //dont want to point to ourselves, circular reference, skip it
        continue;
      }
      
      // if one is passed in, and this is the one depth 1 set, then use whats passed in, otherwise null 
      // means generate
      String theUuid = (grouperSetPair.depth == 1 && !StringUtils.isBlank(uuid)) ? uuid : null;
      
      GrouperSet grouperSet = this.newInstance(grouperSetPair.parent.__getIfHasElementId(),
          grouperSetPair.child.__getThenHasElementId(), grouperSetPair.depth, theUuid);
  
      if (LOG.isDebugEnabled()) {
        GrouperSetElement ifHasElement = grouperSetPair.parent.__getIfHasElement();
        GrouperSetElement thenHasElement = grouperSetPair.child.__getThenHasElement();
        
        LOG.debug("Adding pair " + this.name() + ": " + grouperSet.__getId() + ",\n  ifHas: " 
            + ifHasElement.__getName() + "(" + ifHasElement.__getId() + "),\n  thenHas: "
            + thenHasElement.__getName() + "(" + thenHasElement.__getId() + ")\n  depth: " 
            + grouperSetPair.depth);
      }
  
      //if a->a, parent is a
      //if a->b, parent is a
      //if a->b->c->d, then parent of a->d is a->c
      if (grouperSetPair.child.__getDepth() == 0) {
        grouperSet.__setParentGrouperSetId(grouperSetPair.parent.__getId());
      } else {
        
        //check for same destination circular reference
        if (StringUtils.equals(grouperSetPair.parent.__getThenHasElementId(),
            grouperSetPair.child.__getThenHasElementId())
            && grouperSetPair.parent.__getDepth() > 0 && grouperSetPair.child.__getDepth() > 0) {
          if (LOG.isDebugEnabled()) {
            GrouperSetElement ifGrouperSetElement = grouperSetPair.parent.__getIfHasElement();
            GrouperSetElement thenGrouperSetElement = grouperSetPair.child.__getThenHasElement();
            
            LOG.debug("Found same destination circular reference, skipping " + this.name() 
                + ": " + grouperSet.__getId() + ",\n  ifHas: " 
                + ifGrouperSetElement.__getName() + "(" + ifGrouperSetElement.__getId() + "),\n  thenHas: "
                + thenGrouperSetElement.__getName() + "(" + thenGrouperSetElement.__getId() + ")\n  depth: " 
                + grouperSetPair.depth);
          }
          hasCircularReference = true;
          //dont want to point to ourselves, circular reference, skip it
          continue;
        }
  
      }
      
      //if we found a circular reference in the lower levels, see if we are circling in on ourselves
      if (hasCircularReference) {
        int timeToLive = 1000;
        //loop through parents and children and look for overlap
        GrouperSet currentParentParent = grouperSetPair.parent;
        while(timeToLive-- > 0) {
          
          GrouperSet currentChildParent = grouperSetPair.child;
          while(timeToLive-- > 0) {
          
            if (StringUtils.equals(currentChildParent.__getThenHasElementId(), 
                currentParentParent.__getThenHasElementId())) {
              if (LOG.isDebugEnabled()) {
                GrouperSetElement grouperSetElement = grouperSetPair.parent.__getIfHasElement();
                LOG.debug("Found inner circular reference, skipping " + this.name() + ": " + grouperSetElement.__getName() 
                    + "\n  (" + grouperSetPair.parent.__getIfHasElementId() 
                    + ", depth: " + grouperSetPair.depth + ")");
              }
              //dont want to point to in a circle, circular reference, skip it
              continue OUTER;
              
            }
            
            
            GrouperSet previousChildParent = currentChildParent;
            currentChildParent = currentChildParent.__getParentGrouperSet();
            //all the way up the chain
            if (currentChildParent == previousChildParent) {
              break;
            }
  
          }
          
          GrouperSet previousParentParent = currentParentParent;
          currentParentParent = currentParentParent.__getParentGrouperSet();
          //all the way up the chain
          if (currentParentParent == previousParentParent) {
            break;
          }
  
        }
          
        if (timeToLive <= 0) {
          throw new RuntimeException("TimeToLive too low! " + timeToLive);
        }
      }
      
      //if we still need to do this
      if (StringUtils.isBlank(grouperSet.__getParentGrouperSetId())) {
  
        //find the parent of the child
        GrouperSet parentOfChild = this.find(grouperSetPairs,
            newSets,
            grouperSetPair.child.__getParentGrouperSetId());
  
        //check for circular reference
        if (StringUtils.equals(grouperSetPair.parent.__getIfHasElementId(), 
            parentOfChild.__getThenHasElementId())
            && grouperSetPair.depth > 1) {
          if (LOG.isDebugEnabled()) {
            GrouperSetElement grouperSetElement = grouperSetPair.parent.__getIfHasElement();
            LOG.debug("Found parent circular reference, skipping " + this.name() + ": " + grouperSetElement.__getName() 
                + "\n  (" + grouperSetPair.parent.__getIfHasElementId() 
                + ", depth: " + grouperSetPair.depth + ")");
          }
          hasCircularReference = true;
          //dont want to point to ourselves, circular reference, skip it
          continue;
        }
  
        //find the set for the parent start to child parent end
        GrouperSet parent = this.find(grouperSetPairs, newSets,
            grouperSetPair.parent.__getIfHasElementId(), 
            parentOfChild.__getThenHasElementId(), grouperSetPair.depth-1);
        
        grouperSet.__setParentGrouperSetId(parent.__getId());
      }
      
      if (LOG.isDebugEnabled()) {
        GrouperSetElement ifHasAttributeDefName = grouperSetPair.parent.__getIfHasElement();
        GrouperSetElement thenHasAttributeDefName = grouperSetPair.child.__getThenHasElement();
        GrouperSet parent = grouperSet.__getParentGrouperSet();
        GrouperSetElement parentIfHasAttributeDefName = parent.__getIfHasElement();
        GrouperSetElement parentThenHasAttributeDefName = parent.__getThenHasElement();
        
        LOG.debug("Added pair " + this.name() + ": " + grouperSet.__getId() + ",\n  ifHas: " 
            + ifHasAttributeDefName.__getName() + "(" + ifHasAttributeDefName.__getId() + "),\n  thenHas: "
            + thenHasAttributeDefName.__getName() + "(" + thenHasAttributeDefName.__getId() + "), parent: "
            + grouperSet.__getParentGrouperSetId() + ",\n  parentIfHas: "
            + parentIfHasAttributeDefName.__getName() + "(" + parentIfHasAttributeDefName.__getId() + "),\n  parentThenHas: "
            + parentThenHasAttributeDefName.__getName() + "(" + parentThenHasAttributeDefName.__getId() + ")");
      }
  
      grouperSet.saveOrUpdate();
      newSets.add(grouperSet);
    }
    return true;
  }

  /**
   * find a grouper set, better be here
   * @param grouperSets 
   * @param ifHasId 
   * @param thenHasId 
   * @param depth is the depth expecting
   * @param exceptionIfNull 
   * @return the def name set
   */
  private GrouperSet findInCollection(
      Collection<? extends GrouperSet> grouperSets, String ifHasId, 
      String thenHasId, int depth, boolean exceptionIfNull) {

    //are we sure we are getting the right one here???
    for (GrouperSet grouperSet : GrouperUtil.nonNull(grouperSets)) {
      if (StringUtils.equals(ifHasId, grouperSet.__getIfHasElementId())
          && StringUtils.equals(thenHasId, grouperSet.__getThenHasElementId())
          && depth == grouperSet.__getDepth()) {
        return grouperSet;
      }
    }
    if (exceptionIfNull) {
      throw new RuntimeException("Cant find grouper set with id: " + ifHasId + ", " + thenHasId + ", " + depth);
    }
    return null;
  }

  /**
     * @param setToRemoveFrom
     * @param elementToRemove
     * @return true if removed, false if already removed
     */
    public boolean removeFromGrouperSet(GrouperSetElement setToRemoveFrom, GrouperSetElement elementToRemove) {
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Removing from attribute set " + this.name() + ": " + setToRemoveFrom.__getName() 
            + "\n  (" + setToRemoveFrom.__getId() + ")" 
            + " this attribute: " + elementToRemove.__getName() + " (" + elementToRemove.__getId() + ")");
      }
      
      //lets see what implies having this existing def name
      Set<? extends GrouperSet> candidateGrouperSetToRemove = 
        this.findByIfThenHasElementId(setToRemoveFrom.__getId(), elementToRemove.__getId());
  
      Set<GrouperSet> grouperSetWillRemove = new HashSet<GrouperSet>();
      Set<String> attributeDefNameSetIdsWillRemove = new HashSet<String>();
  
      GrouperSet setToRemove = findInCollection(
          candidateGrouperSetToRemove, setToRemoveFrom.__getId(), elementToRemove.__getId(), 1, false);
      
      if (setToRemove == null) {
        return false;
      }
      
      grouperSetWillRemove.add(setToRemove);
      attributeDefNameSetIdsWillRemove.add(setToRemove.__getId());
      candidateGrouperSetToRemove.remove(setToRemove);
      
      Iterator<? extends GrouperSet> iterator = candidateGrouperSetToRemove.iterator();
  
      //get the records whose parent ends on the node being cut, and who ends on the other node being cut.
      //e.g. if A -> B -> C, and B -> C is being cut, then delete A -> C
      while (iterator.hasNext()) {
        GrouperSet grouperSet = iterator.next();
        
  //      if (LOG.isDebugEnabled()) {
  //        AttributeDefName ifHasAttributeDefName = attributeDefNameSet.getIfHasAttributeDefName();
  //        AttributeDefName thenHasAttributeDefName = attributeDefNameSet.getThenHasAttributeDefName();
  //        LOG.debug("Initial check " + attributeDefNameSet.getId() + ",\n  ifHas: " 
  //            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
  //            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
  //            + attributeDefNameSet.getDepth());
  //      }
        
  //      String logPrefix = "Skipping initial set to remove ";
        if (StringUtils.equals(grouperSet.__getThenHasElementId(), 
            elementToRemove.__getId())) {
          GrouperSet parentSet = grouperSet.__getParentGrouperSet();
          if (StringUtils.equals(parentSet.__getThenHasElementId(), setToRemoveFrom.__getId())) {
            grouperSetWillRemove.add(grouperSet);
            attributeDefNameSetIdsWillRemove.add(grouperSet.__getId());
            iterator.remove();
  //          logPrefix = "Found initial set to remove ";
          }
        }
        
  //      if (LOG.isDebugEnabled()) {
  //        AttributeDefName ifHasAttributeDefName = attributeDefNameSet.getIfHasAttributeDefName();
  //        AttributeDefName thenHasAttributeDefName = attributeDefNameSet.getThenHasAttributeDefName();
  //        LOG.debug(logPrefix + attributeDefNameSet.getId() + ",\n  ifHas: " 
  //            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
  //            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
  //            + attributeDefNameSet.getDepth());
  //      }
  
      }
  
      
      
      int setToRemoveSize = grouperSetWillRemove.size();
      int timeToLive = 100;
      while (timeToLive-- > 0) {
        iterator = candidateGrouperSetToRemove.iterator();
        
        //see if any parents destroyed
        while (iterator.hasNext()) {
          GrouperSet grouperSet = iterator.next();
          //if the parent is there, it is gone
          
  //        String logPrefix = "Skipping set to remove ";
          
          if (attributeDefNameSetIdsWillRemove.contains(grouperSet.__getParentGrouperSetId())) {
            grouperSetWillRemove.add(grouperSet);
            attributeDefNameSetIdsWillRemove.add(grouperSet.__getId());
            iterator.remove();
  //          logPrefix = "Found set to remove ";
          }
  //        if (LOG.isDebugEnabled()) {
  //          AttributeDefName ifHasAttributeDefName = attributeDefNameSet.getIfHasAttributeDefName();
  //          AttributeDefName thenHasAttributeDefName = attributeDefNameSet.getThenHasAttributeDefName();
  //          LOG.debug(logPrefix + attributeDefNameSet.getId() + ",\n  ifHas: " 
  //              + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
  //              + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
  //              + attributeDefNameSet.getDepth());
  //        }
        }
        
        //if we didnt make progress, we are done
        if(setToRemoveSize == grouperSetWillRemove.size()) {
          break;
        }
        
        setToRemoveSize = grouperSetWillRemove.size();
      }
      
      if (timeToLive <= 0) {
        throw new RuntimeException("TimeToLive is under 0");
      }
      
      //reverse sort by depth
      List<GrouperSet> setsToRemove = new ArrayList<GrouperSet>(grouperSetWillRemove);
      Collections.sort(setsToRemove, new Comparator<GrouperSet>() {
  
        public int compare(GrouperSet o1, GrouperSet o2) {
          return ((Integer)o1.__getDepth()).compareTo(o2.__getDepth());
        }
      });
      Collections.reverse(setsToRemove);
      
      for (GrouperSet grouperSet : setsToRemove) {
        if (LOG.isDebugEnabled()) {
          GrouperSetElement ifHasElement = grouperSet.__getIfHasElement();
          GrouperSetElement thenHasElement = grouperSet.__getThenHasElement();
          LOG.debug("Deleting set " + this.name() + ": " + grouperSet.__getId() + ",\n  ifHas: " 
              + ifHasElement.__getName() + "(" + ifHasElement.__getId() + "),\n  thenHas: "
              + thenHasElement.__getName() + "(" + thenHasElement.__getId() + ")\n  depth: " 
              + grouperSet.__getDepth());
        }
        grouperSet.delete();
      }
  
  //    for (AttributeDefNameSet attributeDefNameSet : candidateAttributeDefNameSetToRemove) {
  //      if (LOG.isDebugEnabled()) {
  //        AttributeDefName ifHasAttributeDefName = attributeDefNameSet.getIfHasAttributeDefName();
  //        AttributeDefName thenHasAttributeDefName = attributeDefNameSet.getThenHasAttributeDefName();
  //        LOG.debug("Not deleting set " + attributeDefNameSet.getId() + ",\n  ifHas: " 
  //            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
  //            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
  //            + attributeDefNameSet.getDepth());
  //      }
  //    }
  
      //now, if there is A -> B -> C, and you cut B -> C, then you need to remove A -> C
      
      return true;
    }
  
}
