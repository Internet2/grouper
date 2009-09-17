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
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;


/**
 * @author shilen
 */
public class AddMissingGroupSets {

  private static Set<String> compositeOwnerIds = new HashSet<String>();
  
  /**
   * @param args
   */
  public static void main (String[] args) {
    
    addMissingSelfGroupSetsForGroups();
    
    addMissingSelfGroupSetsForStems();
    
    addMissingImmediateGroupSetsForGroupOwners();
    
    addMissingImmediateGroupSetsForStemOwners();
  }
  
  /**
   * Add missing self group sets for groups
   */
  public static void addMissingSelfGroupSetsForGroups() {
    cacheCompositeOwners();
    
    Set<Object[]> groupsAndFields = GrouperDAOFactory.getFactory().getGroupSet().findMissingSelfGroupSetsForGroups();
    Iterator<Object[]> groupsAndFieldsIter = groupsAndFields.iterator();
    while (groupsAndFieldsIter.hasNext()) {
      Object[] groupAndField = groupsAndFieldsIter.next();
      Group group = (Group)groupAndField[0];
      Field field = (Field)groupAndField[1];
      
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
            
      System.out.println("Added self groupSet for " + group.getName() + " for field " + field.getTypeString() + " / " + field.getName());
    }
  }
  
  /**
   * Add missing self group sets for stems
   */
  public static void addMissingSelfGroupSetsForStems() {
    Set<Object[]> stemsAndFields = GrouperDAOFactory.getFactory().getGroupSet().findMissingSelfGroupSetsForStems();
    Iterator<Object[]> stemsAndFieldsIter = stemsAndFields.iterator();
    while (stemsAndFieldsIter.hasNext()) {
      Object[] stemAndField = stemsAndFieldsIter.next();
      Stem stem = (Stem)stemAndField[0];
      Field field = (Field)stemAndField[1];
      
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
      
      String stemName = null;
      if (stem.isRootStem()) {
        stemName = "{rootStem}";
      } else {
        stemName = stem.getName();
      }
      
      System.out.println("Added self groupSet for " + stemName + " for field " + field.getTypeString() + " / " + field.getName());
    }
  }
  
  /**
   * Add missing group sets for immediate memberships where the owner is a group
   */
  public static void addMissingImmediateGroupSetsForGroupOwners() {
    Set<Membership> mships = GrouperDAOFactory.getFactory().getMembership().findMissingImmediateGroupSetsForGroupOwners();
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
      immediateGroupSet.setOwnerGroupId(mship.getOwnerGroupId());
      immediateGroupSet.setParentId(GrouperDAOFactory.getFactory().getGroupSet()
          .findSelfGroup(mship.getOwnerGroupId(), mship.getFieldId()).getId());
      GrouperDAOFactory.getFactory().getGroupSet().save(immediateGroupSet);
      
      System.out.println("Added groupSet for ownerGroupId = " + mship.getOwnerGroupId() + 
          ", memberGroupId = " + mship.getMemberSubjectId() + " for field " + field.getTypeString() + " / " + field.getName());
    }
  }
  
  /**
   * Add missing group sets for immediate memberships where the owner is a stem
   */
  public static void addMissingImmediateGroupSetsForStemOwners() {
    Set<Membership> mships = GrouperDAOFactory.getFactory().getMembership().findMissingImmediateGroupSetsForStemOwners();
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
      immediateGroupSet.setOwnerStemId(mship.getOwnerStemId());
      immediateGroupSet.setParentId(GrouperDAOFactory.getFactory().getGroupSet()
          .findSelfStem(mship.getOwnerStemId(), mship.getFieldId()).getId());
      GrouperDAOFactory.getFactory().getGroupSet().save(immediateGroupSet);
      
      System.out.println("Added groupSet for ownerStemId = " + mship.getOwnerStemId() + 
          ", memberGroupId = " + mship.getMemberSubjectId() + " for field " + field.getTypeString() + " / " + field.getName());
    }
  }
  
  /**
   * cache the composite owners
   */
  private static void cacheCompositeOwners() {
    Set<Composite> composites = GrouperDAOFactory.getFactory().getComposite().getAllComposites();
    Iterator<Composite> compositesIter = composites.iterator();
    
    while (compositesIter.hasNext()) {
      Composite c = compositesIter.next();
      compositeOwnerIds.add(c.getFactorOwnerUuid());
    }
  }
}
