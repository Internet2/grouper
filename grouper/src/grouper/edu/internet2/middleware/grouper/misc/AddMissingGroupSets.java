/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.misc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;


/**
 * @author shilen
 */
public class AddMissingGroupSets {

  /** */
  private Set<String> compositeOwnerIds = new HashSet<String>();
  
  /** Whether or not to print out results of what's being done */
  private boolean showResults = true;
  
  /** Whether or not to actually save updates */
  private boolean saveUpdates = true;
  
  /**
   * Whether or not to print out results of what's being done
   * @param showResults
   * @return AddMissingGroupSets
   */
  public AddMissingGroupSets showResults(boolean showResults) {
    this.showResults = showResults;
    return this;
  }
  
  /**
   * Whether or not to actually save updates
   * @param saveUpdates
   * @return AddMissingGroupSets
   */
  public AddMissingGroupSets saveUpdates(boolean saveUpdates) {
    this.saveUpdates = saveUpdates;
    return this;
  }

  /**
   * Add all missing group sets
   * @param reportOnly If true, no changes will actually be made.
   */
  public void addAllMissingGroupSets() {
        
    addMissingSelfGroupSetsForGroups();
    
    addMissingSelfGroupSetsForStems();
    
    addMissingSelfGroupSetsForAttrDefs();

    addMissingImmediateGroupSetsForGroupOwners();
    
    addMissingImmediateGroupSetsForStemOwners();

    addMissingImmediateGroupSetsForAttrDefOwners();
  }
  
  /**
   * Add missing self group sets for groups
   */
  public void addMissingSelfGroupSetsForGroups() {
    cacheCompositeOwners();
    
    Set<Object[]> groupsAndFields = GrouperDAOFactory.getFactory().getGroupSet().findMissingSelfGroupSetsForGroups();
    Iterator<Object[]> groupsAndFieldsIter = groupsAndFields.iterator();
    while (groupsAndFieldsIter.hasNext()) {
      Object[] groupAndField = groupsAndFieldsIter.next();
      Group group = (Group)groupAndField[0];
      Field field = (Field)groupAndField[1];
      
      if (showResults) {
        System.out.println("Adding self groupSet for " + group.getName() + " for field " + field.getTypeString() + " / " + field.getName());
      }
      
      if (saveUpdates) {
        GroupSet groupSet = new GroupSet();
        groupSet.setId(GrouperUuid.getUuid());
        groupSet.setCreatorId(group.getCreatorUuid());
        groupSet.setCreateTime(group.getCreateTimeLong());
        groupSet.setDepth(0);
        groupSet.setMemberGroupId(group.getUuid());
        groupSet.setOwnerGroupId(group.getUuid());
        groupSet.setParentId(groupSet.getId());
        groupSet.setFieldId(field.getUuid());
        
        // if the default list and the group is a composite, set the groupSet type to composite
        if (Group.getDefaultList().equals(field) && compositeOwnerIds.contains(group.getUuid())) {
          groupSet.setType(Membership.COMPOSITE);
        }
        
        GrouperDAOFactory.getFactory().getGroupSet().save(groupSet);
      }
    }
  }
  
  /**
   * Add missing self group sets for stems
   */
  public void addMissingSelfGroupSetsForStems() {
    Set<Object[]> stemsAndFields = GrouperDAOFactory.getFactory().getGroupSet().findMissingSelfGroupSetsForStems();
    Iterator<Object[]> stemsAndFieldsIter = stemsAndFields.iterator();
    while (stemsAndFieldsIter.hasNext()) {
      Object[] stemAndField = stemsAndFieldsIter.next();
      Stem stem = (Stem)stemAndField[0];
      Field field = (Field)stemAndField[1];
      
      if (showResults) {
        String stemName = null;
        if (stem.isRootStem()) {
          stemName = "{rootStem}";
        } else {
          stemName = stem.getName();
        }
        System.out.println("Adding self groupSet for " + stemName + " for field " + field.getTypeString() + " / " + field.getName());
      }
      
      if (saveUpdates) {
        GroupSet groupSet = new GroupSet();
        groupSet.setId(GrouperUuid.getUuid());
        groupSet.setCreatorId(stem.getCreatorUuid());
        groupSet.setCreateTime(stem.getCreateTimeLong());
        groupSet.setDepth(0);
        groupSet.setMemberStemId(stem.getUuid());
        groupSet.setOwnerStemId(stem.getUuid());
        groupSet.setParentId(groupSet.getId());
        groupSet.setFieldId(field.getUuid());
        GrouperDAOFactory.getFactory().getGroupSet().save(groupSet);
      }
    }
  }
  
  /**
   * Add missing group sets for immediate memberships where the owner is a group
   */
  public void addMissingImmediateGroupSetsForGroupOwners() {
    Set<Membership> mships = GrouperDAOFactory.getFactory().getMembership().findMissingImmediateGroupSetsForGroupOwners();
    Iterator<Membership> mshipsIter = mships.iterator();
    
    while (mshipsIter.hasNext()) {
      Membership mship = mshipsIter.next();
      Field field = FieldFinder.findById(mship.getFieldId(), true);
      
      if (showResults) {
        System.out.println("Adding groupSet for ownerGroupId = " + mship.getOwnerGroupId() + 
            ", memberGroupId = " + mship.getMemberSubjectId() + " for field " + field.getTypeString() + " / " + field.getName());
      }
      
      if (saveUpdates) {
        GroupSet immediateGroupSet = new GroupSet();
        immediateGroupSet.setId(GrouperUuid.getUuid());
        immediateGroupSet.setCreatorId(mship.getCreatorUuid());
        immediateGroupSet.setCreateTime(mship.getCreateTimeLong());
        immediateGroupSet.setDepth(1);
        immediateGroupSet.setFieldId(field.getUuid());
        immediateGroupSet.setMemberGroupId(mship.getMemberSubjectId());
        immediateGroupSet.setType(Membership.EFFECTIVE);
        immediateGroupSet.setOwnerGroupId(mship.getOwnerGroupId());
        immediateGroupSet.setParentId(GrouperDAOFactory.getFactory().getGroupSet()
            .findSelfGroup(mship.getOwnerGroupId(), mship.getFieldId()).getId());
        GrouperDAOFactory.getFactory().getGroupSet().save(immediateGroupSet);
      }
    }
  }
  
  /**
   * Add missing group sets for immediate memberships where the owner is a stem
   */
  public void addMissingImmediateGroupSetsForStemOwners() {
    Set<Membership> mships = GrouperDAOFactory.getFactory().getMembership().findMissingImmediateGroupSetsForStemOwners();
    Iterator<Membership> mshipsIter = mships.iterator();
    
    while (mshipsIter.hasNext()) {
      Membership mship = mshipsIter.next();
      Field field = FieldFinder.findById(mship.getFieldId(), true);
      
      if (showResults) {
        System.out.println("Adding groupSet for ownerStemId = " + mship.getOwnerStemId() + 
            ", memberGroupId = " + mship.getMemberSubjectId() + " for field " + field.getTypeString() + " / " + field.getName());
      }
      
      if (saveUpdates) {
        GroupSet immediateGroupSet = new GroupSet();
        immediateGroupSet.setId(GrouperUuid.getUuid());
        immediateGroupSet.setCreatorId(mship.getCreatorUuid());
        immediateGroupSet.setCreateTime(mship.getCreateTimeLong());
        immediateGroupSet.setDepth(1);
        immediateGroupSet.setFieldId(field.getUuid());
        immediateGroupSet.setMemberGroupId(mship.getMemberSubjectId());
        immediateGroupSet.setType(Membership.EFFECTIVE);
        immediateGroupSet.setOwnerStemId(mship.getOwnerStemId());
        immediateGroupSet.setParentId(GrouperDAOFactory.getFactory().getGroupSet()
            .findSelfStem(mship.getOwnerStemId(), mship.getFieldId()).getId());
        GrouperDAOFactory.getFactory().getGroupSet().save(immediateGroupSet);
      }
    }
  }
  
  /**
   * cache the composite owners
   */
  private void cacheCompositeOwners() {
    Set<Composite> composites = GrouperDAOFactory.getFactory().getComposite().getAllComposites();
    Iterator<Composite> compositesIter = composites.iterator();
    
    while (compositesIter.hasNext()) {
      Composite c = compositesIter.next();
      compositeOwnerIds.add(c.getFactorOwnerUuid());
    }
  }

  /**
   * Add missing group sets for immediate memberships where the owner is a stem
   */
  public static void addMissingImmediateGroupSetsForAttrDefOwners() {
    Set<Membership> mships = GrouperDAOFactory.getFactory().getMembership().findMissingImmediateGroupSetsForAttrDefOwners();
    Iterator<Membership> mshipsIter = mships.iterator();
    
    while (mshipsIter.hasNext()) {
      Membership mship = mshipsIter.next();
      Field field = FieldFinder.findById(mship.getFieldId(), true);
      
      GroupSet immediateGroupSet = new GroupSet();
      immediateGroupSet.setId(GrouperUuid.getUuid());
      immediateGroupSet.setCreatorId(mship.getCreatorUuid());
      immediateGroupSet.setCreateTime(mship.getCreateTimeLong());
      immediateGroupSet.setDepth(1);
      immediateGroupSet.setFieldId(field.getUuid());
      immediateGroupSet.setMemberGroupId(mship.getMemberSubjectId());
      immediateGroupSet.setType(Membership.EFFECTIVE);
      immediateGroupSet.setOwnerAttrDefId(mship.getOwnerAttrDefId());
      immediateGroupSet.setParentId(GrouperDAOFactory.getFactory().getGroupSet()
          .findSelfStem(mship.getOwnerAttrDefId(), mship.getFieldId()).getId());
      GrouperDAOFactory.getFactory().getGroupSet().save(immediateGroupSet);
      
      System.out.println("Added groupSet for ownerAttrDefId = " + mship.getOwnerStemId() + 
          ", memberGroupId = " + mship.getMemberSubjectId() + " for field " + field.getTypeString() + " / " + field.getName());
    }
  }

  /**
   * Add missing self group sets for stems
   */
  public static void addMissingSelfGroupSetsForAttrDefs() {
    Set<Object[]> attrDefsAndFields = GrouperDAOFactory.getFactory().getGroupSet().findMissingSelfGroupSetsForAttrDefs();
    Iterator<Object[]> attrDefsAndFieldsIter = attrDefsAndFields.iterator();
    while (attrDefsAndFieldsIter.hasNext()) {
      Object[] attrDefAndField = attrDefsAndFieldsIter.next();
      AttributeDef attributeDef = (AttributeDef)attrDefAndField[0];
      Field field = (Field)attrDefAndField[1];
      
      GroupSet groupSet = new GroupSet();
      groupSet.setId(GrouperUuid.getUuid());
      groupSet.setCreatorId(attributeDef.getCreatorId());
      groupSet.setCreateTime(attributeDef.getCreatedOnDb());
      groupSet.setDepth(0);
      groupSet.setMemberAttrDefId(attributeDef.getId());
      groupSet.setOwnerStemId(attributeDef.getId());
      groupSet.setParentId(groupSet.getId());
      groupSet.setFieldId(field.getUuid());
      GrouperDAOFactory.getFactory().getGroupSet().save(groupSet);
      
      System.out.println("Added self groupSet for " + attributeDef.getName() + " for field " + field.getTypeString() + " / " + field.getName());
    }
  }
}
