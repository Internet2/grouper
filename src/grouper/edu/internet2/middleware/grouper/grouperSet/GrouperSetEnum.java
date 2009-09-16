/**
 * @author mchyzer
 * $Id: GrouperSetEnum.java,v 1.1 2009-09-16 05:50:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.AttributeDefAssignmentType;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public enum GrouperSetEnum {

  /** attribute set grouper set */
  ATTRIBUTE_SET;

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperSetEnum.class);

//  /**
//   * 
//   * @param newAttributeDefName
//   * @return true if added, false if already there
//   */
//  public boolean addToAttributeDefNameSet(GrouperSet containerSet, GrouperSet newElement) {
//    
//    if (LOG.isDebugEnabled()) {
//      LOG.debug("Adding to attribute set " + this.__getName() + "\n  (" + this.__getId() + ")" 
//          + " this attribute: " + newAttributeDefName.getName() + " (" + newAttributeDefName.getId() + ")");
//    }
//    
//    //lets see if this one already exists
//    AttributeDefNameSet existingAttributeDefNameSet = GrouperDAOFactory.getFactory()
//      .getAttributeDefNameSet().findByIfThenImmediate(this.getId(), 
//        newAttributeDefName.getId(), false);
//    if (existingAttributeDefNameSet != null) {
//      return false;
//    }
//    
//    //lets see what implies having this existing def name
//    Set<AttributeDefNameSet> existingAttributeDefNameSetList = 
//      GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByThenHasAttributeDefNameId(this.getId());
//  
//    //lets see what having this new def name implies
//    Set<AttributeDefNameSet> newAttributeDefNameSetList = 
//      GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(newAttributeDefName.getId());
//    
//    List<AttributeDefNameSetPair> attributeDefNameSetPairs = new ArrayList<AttributeDefNameSetPair>();
//    List<AttributeDefNameSet> newSets = new ArrayList<AttributeDefNameSet>();
//    
//    //now lets merge the two lists
//    //they each must have one member
//    for (AttributeDefNameSet parent : existingAttributeDefNameSetList) {
//      for (AttributeDefNameSet child : newAttributeDefNameSetList) {
//        AttributeDefNameSetPair attributeDefNameSetPair = new AttributeDefNameSetPair();
//        
//        attributeDefNameSetPair.depth = 1 + parent.getDepth() + child.getDepth();
//  
//        attributeDefNameSetPair.parent = parent;
//        attributeDefNameSetPair.child = child;
//        attributeDefNameSetPairs.add(attributeDefNameSetPair);
//        if (LOG.isDebugEnabled()) {
//          AttributeDefName ifHasAttributeDefName = parent.getIfHasAttributeDefName();
//          AttributeDefName thenHasAttributeDefName = child.getThenHasAttributeDefName();
//          LOG.debug("Found pair to manage " + ifHasAttributeDefName.getName() 
//              + "\n  (parent set: " + parent.getId() + ", ifHasNameId: " + ifHasAttributeDefName.getId() + ")"
//              + "\n  to: " + thenHasAttributeDefName.getName()
//              + "(child set: " + child.getId() + ", thenHasNameId: " + thenHasAttributeDefName.getId() + ")"
//              + "\n  depth: " + attributeDefNameSetPair.depth
//          );
//        }
//        
//      }
//    }
//  
//    //sort by depth so we process correctly
//    Collections.sort(attributeDefNameSetPairs);
//    
//    //if has circular, then do more queries to be sure
//    boolean hasCircularReference = false;
//    OUTER: for (AttributeDefNameSetPair attributeDefNameSetPair : attributeDefNameSetPairs) {
//      
//      //check for circular reference
//      if (StringUtils.equals(attributeDefNameSetPair.parent.getIfHasAttributeDefNameId(), 
//          attributeDefNameSetPair.child.getThenHasAttributeDefNameId())
//          && attributeDefNameSetPair.depth > 0) {
//        if (LOG.isDebugEnabled()) {
//          AttributeDefName attributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
//          LOG.debug("Found circular reference, skipping " + attributeDefName.getName() 
//              + "\n  (" + attributeDefNameSetPair.parent.getIfHasAttributeDefNameId() 
//              + ", depth: " + attributeDefNameSetPair.depth + ")");
//        }
//        hasCircularReference = true;
//        //dont want to point to ourselves, circular reference, skip it
//        continue;
//      }
//      
//      AttributeDefNameSet attributeDefNameSet = new AttributeDefNameSet();
//      attributeDefNameSet.setId(GrouperUuid.getUuid());
//      attributeDefNameSet.setDepth(attributeDefNameSetPair.depth);
//      attributeDefNameSet.setIfHasAttributeDefNameId(attributeDefNameSetPair.parent.getIfHasAttributeDefNameId());
//      attributeDefNameSet.setThenHasAttributeDefNameId(attributeDefNameSetPair.child.getThenHasAttributeDefNameId());
//      attributeDefNameSet.setType(attributeDefNameSet.getDepth() == 1 ? 
//          AttributeDefAssignmentType.immediate : AttributeDefAssignmentType.effective);
//  
//      if (LOG.isDebugEnabled()) {
//        AttributeDefName ifHasAttributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
//        AttributeDefName thenHasAttributeDefName = attributeDefNameSetPair.child.getThenHasAttributeDefName();
//        
//        LOG.debug("Adding pair " + attributeDefNameSet.getId() + ",\n  ifHas: " 
//            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
//            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
//            + attributeDefNameSetPair.depth);
//      }
//  
//      //if a->a, parent is a
//      //if a->b, parent is a
//      //if a->b->c->d, then parent of a->d is a->c
//      if (attributeDefNameSetPair.child.getDepth() == 0) {
//        attributeDefNameSet.setParentAttrDefNameSetId(attributeDefNameSetPair.parent.getId());
//      } else {
//        
//        //check for same destination circular reference
//        if (StringUtils.equals(attributeDefNameSetPair.parent.getThenHasAttributeDefNameId(), 
//            attributeDefNameSetPair.child.getThenHasAttributeDefNameId())
//            && attributeDefNameSetPair.parent.getDepth() > 0 && attributeDefNameSetPair.child.getDepth() > 0) {
//          if (LOG.isDebugEnabled()) {
//            AttributeDefName ifHasAttributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
//            AttributeDefName thenHasAttributeDefName = attributeDefNameSetPair.child.getThenHasAttributeDefName();
//            
//            LOG.debug("Found same destination circular reference, skipping " + attributeDefNameSet.getId() + ",\n  ifHas: " 
//                + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
//                + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
//                + attributeDefNameSetPair.depth);
//          }
//          hasCircularReference = true;
//          //dont want to point to ourselves, circular reference, skip it
//          continue;
//        }
//  
//      }
//      
//      //if we found a circular reference in the lower levels, see if we are circling in on ourselves
//      if (hasCircularReference) {
//        int timeToLive = 1000;
//        //loop through parents and children and look for overlap
//        AttributeDefNameSet currentParentParent = attributeDefNameSetPair.parent;
//        while(timeToLive-- > 0) {
//          
//          AttributeDefNameSet currentChildParent = attributeDefNameSetPair.child;
//          while(timeToLive-- > 0) {
//          
//            if (StringUtils.equals(currentChildParent.getThenHasAttributeDefNameId(), 
//                currentParentParent.getThenHasAttributeDefNameId())) {
//              if (LOG.isDebugEnabled()) {
//                AttributeDefName attributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
//                LOG.debug("Found inner circular reference, skipping " + attributeDefName.getName() 
//                    + "\n  (" + attributeDefNameSetPair.parent.getIfHasAttributeDefNameId() 
//                    + ", depth: " + attributeDefNameSetPair.depth + ")");
//              }
//              //dont want to point to in a circle, circular reference, skip it
//              continue OUTER;
//              
//            }
//            
//            
//            AttributeDefNameSet previousChildParent = currentChildParent;
//            currentChildParent = currentChildParent.getParentAttributeDefSet();
//            //all the way up the chain
//            if (currentChildParent == previousChildParent) {
//              break;
//            }
//  
//          }
//          
//          AttributeDefNameSet previousParentParent = currentParentParent;
//          currentParentParent = currentParentParent.getParentAttributeDefSet();
//          //all the way up the chain
//          if (currentParentParent == previousParentParent) {
//            break;
//          }
//  
//        }
//          
//        if (timeToLive <= 0) {
//          throw new RuntimeException("TimeToLive too low! " + timeToLive);
//        }
//      }
//      
//      //if we still need to do this
//      if (StringUtils.isBlank(attributeDefNameSet.getParentAttrDefNameSetId())) {
//  
//        //find the parent of the child
//        AttributeDefNameSet parentOfChild = AttributeDefNameSetPair.find(attributeDefNameSetPairs,
//            newSets,
//            attributeDefNameSetPair.child.getParentAttrDefNameSetId());
//  
//        //check for circular reference
//        if (StringUtils.equals(attributeDefNameSetPair.parent.getIfHasAttributeDefNameId(), 
//            parentOfChild.getThenHasAttributeDefNameId())
//            && attributeDefNameSetPair.depth > 1) {
//          if (LOG.isDebugEnabled()) {
//            AttributeDefName attributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
//            LOG.debug("Found parent circular reference, skipping " + attributeDefName.getName() 
//                + "\n  (" + attributeDefNameSetPair.parent.getIfHasAttributeDefNameId() 
//                + ", depth: " + attributeDefNameSetPair.depth + ")");
//          }
//          hasCircularReference = true;
//          //dont want to point to ourselves, circular reference, skip it
//          continue;
//        }
//  
//        //find the set for the parent start to child parent end
//        AttributeDefNameSet parent = AttributeDefNameSetPair.find(attributeDefNameSetPairs, newSets,
//            attributeDefNameSetPair.parent.getIfHasAttributeDefNameId(), 
//            parentOfChild.getThenHasAttributeDefNameId(), attributeDefNameSetPair.depth-1);
//        
//        attributeDefNameSet.setParentAttrDefNameSetId(parent.getId());
//      }
//      
//      if (LOG.isDebugEnabled()) {
//        AttributeDefName ifHasAttributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
//        AttributeDefName thenHasAttributeDefName = attributeDefNameSetPair.child.getThenHasAttributeDefName();
//        AttributeDefNameSet parent = attributeDefNameSet.getParentAttributeDefSet();
//        AttributeDefName parentIfHasAttributeDefName = parent.getIfHasAttributeDefName();
//        AttributeDefName parentThenHasAttributeDefName = parent.getThenHasAttributeDefName();
//        
//        LOG.debug("Added pair " + attributeDefNameSet.getId() + ",\n  ifHas: " 
//            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
//            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + "), parent: "
//            + attributeDefNameSet.getParentAttrDefNameSetId() + ",\n  parentIfHas: "
//            + parentIfHasAttributeDefName.getName() + "(" + parentIfHasAttributeDefName.getId() + "),\n  parentThenHas: "
//            + parentThenHasAttributeDefName.getName() + "(" + parentThenHasAttributeDefName.getId() + ")");
//      }
//  
//      attributeDefNameSet.saveOrUpdate();
//      newSets.add(attributeDefNameSet);
//    }
//    return true;
//  }
//
//  /**
//     * 
//     * @param attributeDefNameToRemove
//     * @return true if removed, false if already removed
//     */
//    public boolean removeFromAttributeDefNameSet(AttributeDefName attributeDefNameToRemove) {
//      
//      if (LOG.isDebugEnabled()) {
//        LOG.debug("Removing from attribute set " + this.getName() + "\n  (" + this.getId() + ")" 
//            + " this attribute: " + attributeDefNameToRemove.getName() + " (" + attributeDefNameToRemove.getId() + ")");
//      }
//      
//      //lets see what implies having this existing def name
//      Set<AttributeDefNameSet> candidateAttributeDefNameSetToRemove = 
//        GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenHasAttributeDefNameId(this.getId(), attributeDefNameToRemove.getId());
//  
//      Set<AttributeDefNameSet> attributeDefNameSetWillRemove = new HashSet<AttributeDefNameSet>();
//      Set<String> attributeDefNameSetIdsWillRemove = new HashSet<String>();
//  
//      AttributeDefNameSet setToRemove = AttributeDefNameSet.findInCollection(
//          candidateAttributeDefNameSetToRemove, this.getId(), attributeDefNameToRemove.getId(), 1, false);
//      
//      if (setToRemove == null) {
//        return false;
//      }
//      
//      attributeDefNameSetWillRemove.add(setToRemove);
//      attributeDefNameSetIdsWillRemove.add(setToRemove.getId());
//      candidateAttributeDefNameSetToRemove.remove(setToRemove);
//      
//      Iterator<AttributeDefNameSet> iterator = candidateAttributeDefNameSetToRemove.iterator();
//  
//      //get the records whose parent ends on the node being cut, and who ends on the other node being cut.
//      //e.g. if A -> B -> C, and B -> C is being cut, then delete A -> C
//      while (iterator.hasNext()) {
//        AttributeDefNameSet attributeDefNameSet = iterator.next();
//        
//  //      if (LOG.isDebugEnabled()) {
//  //        AttributeDefName ifHasAttributeDefName = attributeDefNameSet.getIfHasAttributeDefName();
//  //        AttributeDefName thenHasAttributeDefName = attributeDefNameSet.getThenHasAttributeDefName();
//  //        LOG.debug("Initial check " + attributeDefNameSet.getId() + ",\n  ifHas: " 
//  //            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
//  //            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
//  //            + attributeDefNameSet.getDepth());
//  //      }
//        
//  //      String logPrefix = "Skipping initial set to remove ";
//        if (StringUtils.equals(attributeDefNameSet.getThenHasAttributeDefNameId(), 
//            attributeDefNameToRemove.getId())) {
//          AttributeDefNameSet parentSet = attributeDefNameSet.getParentAttributeDefSet();
//          if (StringUtils.equals(parentSet.getThenHasAttributeDefNameId(), this.getId())) {
//            attributeDefNameSetWillRemove.add(attributeDefNameSet);
//            attributeDefNameSetIdsWillRemove.add(attributeDefNameSet.getId());
//            iterator.remove();
//  //          logPrefix = "Found initial set to remove ";
//          }
//        }
//        
//  //      if (LOG.isDebugEnabled()) {
//  //        AttributeDefName ifHasAttributeDefName = attributeDefNameSet.getIfHasAttributeDefName();
//  //        AttributeDefName thenHasAttributeDefName = attributeDefNameSet.getThenHasAttributeDefName();
//  //        LOG.debug(logPrefix + attributeDefNameSet.getId() + ",\n  ifHas: " 
//  //            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
//  //            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
//  //            + attributeDefNameSet.getDepth());
//  //      }
//  
//      }
//  
//      
//      
//      int setToRemoveSize = attributeDefNameSetWillRemove.size();
//      int timeToLive = 100;
//      while (timeToLive-- > 0) {
//        iterator = candidateAttributeDefNameSetToRemove.iterator();
//        
//        //see if any parents destroyed
//        while (iterator.hasNext()) {
//          AttributeDefNameSet attributeDefNameSet = iterator.next();
//          //if the parent is there, it is gone
//          
//  //        String logPrefix = "Skipping set to remove ";
//          
//          if (attributeDefNameSetIdsWillRemove.contains(attributeDefNameSet.getParentAttrDefNameSetId())) {
//            attributeDefNameSetWillRemove.add(attributeDefNameSet);
//            attributeDefNameSetIdsWillRemove.add(attributeDefNameSet.getId());
//            iterator.remove();
//  //          logPrefix = "Found set to remove ";
//          }
//  //        if (LOG.isDebugEnabled()) {
//  //          AttributeDefName ifHasAttributeDefName = attributeDefNameSet.getIfHasAttributeDefName();
//  //          AttributeDefName thenHasAttributeDefName = attributeDefNameSet.getThenHasAttributeDefName();
//  //          LOG.debug(logPrefix + attributeDefNameSet.getId() + ",\n  ifHas: " 
//  //              + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
//  //              + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
//  //              + attributeDefNameSet.getDepth());
//  //        }
//        }
//        
//        //if we didnt make progress, we are done
//        if(setToRemoveSize == attributeDefNameSetWillRemove.size()) {
//          break;
//        }
//        
//        setToRemoveSize = attributeDefNameSetWillRemove.size();
//      }
//      
//      if (timeToLive <= 0) {
//        throw new RuntimeException("TimeToLive is under 0");
//      }
//      
//      //reverse sort by depth
//      List<AttributeDefNameSet> setsToRemove = new ArrayList<AttributeDefNameSet>(attributeDefNameSetWillRemove);
//      Collections.sort(setsToRemove, new Comparator<AttributeDefNameSet>() {
//  
//        public int compare(AttributeDefNameSet o1, AttributeDefNameSet o2) {
//          return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
//        }
//      });
//      Collections.reverse(setsToRemove);
//      
//      for (AttributeDefNameSet attributeDefNameSet : setsToRemove) {
//        if (LOG.isDebugEnabled()) {
//          AttributeDefName ifHasAttributeDefName = attributeDefNameSet.getIfHasAttributeDefName();
//          AttributeDefName thenHasAttributeDefName = attributeDefNameSet.getThenHasAttributeDefName();
//          LOG.debug("Deleting set " + attributeDefNameSet.getId() + ",\n  ifHas: " 
//              + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
//              + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
//              + attributeDefNameSet.getDepth());
//        }
//        attributeDefNameSet.delete();
//      }
//  
//  //    for (AttributeDefNameSet attributeDefNameSet : candidateAttributeDefNameSetToRemove) {
//  //      if (LOG.isDebugEnabled()) {
//  //        AttributeDefName ifHasAttributeDefName = attributeDefNameSet.getIfHasAttributeDefName();
//  //        AttributeDefName thenHasAttributeDefName = attributeDefNameSet.getThenHasAttributeDefName();
//  //        LOG.debug("Not deleting set " + attributeDefNameSet.getId() + ",\n  ifHas: " 
//  //            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
//  //            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
//  //            + attributeDefNameSet.getDepth());
//  //      }
//  //    }
//  
//      //now, if there is A -> B -> C, and you cut B -> C, then you need to remove A -> C
//      
//      return true;
//    }
  
}
