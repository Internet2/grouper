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
package edu.internet2.middleware.grouper.userData;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperUserDataApiTest extends GrouperTest {
  
  /**
   * 
   */
  public GrouperUserDataApiTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperUserDataApiTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperUserDataApiTest("testRecentlyUsedGroups2"));
  }


  /**
   * 
   */
  public void testFavoriteGroupsTimes() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group favorite0 = new GroupSave(grouperSession).assignName("test:favorite0").assignCreateParentStemsIfNotExist(true).save();
    Group favorite1 = new GroupSave(grouperSession).assignName("test:favorite1").assignCreateParentStemsIfNotExist(true).save();
    Group favorite2 = new GroupSave(grouperSession).assignName("test:favorite2").assignCreateParentStemsIfNotExist(true).save();
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.favoriteGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    GrouperUserDataApi.favoriteGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite1);
    GrouperUserDataApi.favoriteGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite2);
    GrouperUserDataApi.favoriteGroupRemove(userDataGroupName, SubjectTestHelper.SUBJ0, favorite2);
    
    GrouperUserDataApi.favoriteGroups(userDataGroupName, SubjectTestHelper.SUBJ0);

    
    long start = System.nanoTime();
    
    for (int i=0;i<100; i++) {
      
      GrouperUserDataApi.favoriteGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite2);
      // System.out.println("Took: " + ((System.nanoTime() - start) / 1000000) + "ms");
      start = System.nanoTime();
      
      GrouperUserDataApi.favoriteGroupRemove(userDataGroupName, SubjectTestHelper.SUBJ0, favorite2);
      // System.out.println("Took: " + ((System.nanoTime() - start) / 1000000) + "ms");
      start = System.nanoTime();

      GrouperUserDataApi.favoriteGroups(userDataGroupName, SubjectTestHelper.SUBJ0);
      // System.out.println("Took: " + ((System.nanoTime() - start) / 1000000) + "ms");
      start = System.nanoTime();
      
    }
    
    // System.out.println("Took: " + ((System.nanoTime() - start) / 1000000) + "ms");
    
    ChangeLogTempToEntity.convertRecords();
    
    GrouperSession.stopQuietly(grouperSession);
    
  }

  /**
   * 
   */
  public void testFavoriteGroups() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group favorite0 = new GroupSave(grouperSession).assignName("test:favorite0").assignCreateParentStemsIfNotExist(true).save();
    Group favorite1 = new GroupSave(grouperSession).assignName("test:favorite1").assignCreateParentStemsIfNotExist(true).save();
    Group favorite2 = new GroupSave(grouperSession).assignName("test:favorite2").assignCreateParentStemsIfNotExist(true).save();
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.favoriteGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    GrouperUserDataApi.favoriteGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite1);
    GrouperUserDataApi.favoriteGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ1, favorite0);
    
    //############ subj0 has 0 and 1 as favorite groups
    Set<Group> favoriteGroups = GrouperUserDataApi.favoriteGroups(userDataGroupName, SubjectTestHelper.SUBJ0);
    
    assertEquals(2, GrouperUtil.length(favoriteGroups));
    assertTrue(favoriteGroups.contains(favorite0));
    assertTrue(favoriteGroups.contains(favorite1));

    //############ subj1 has 0 as favorite group
    favoriteGroups = GrouperUserDataApi.favoriteGroups(userDataGroupName, SubjectTestHelper.SUBJ1);    
      
    assertEquals(1, GrouperUtil.length(favoriteGroups));
    assertTrue(favoriteGroups.contains(favorite0));

    //############# subj2 has no favorites
    favoriteGroups = GrouperUserDataApi.favoriteGroups(userDataGroupName, SubjectTestHelper.SUBJ2);    
    
    assertEquals(0, GrouperUtil.length(favoriteGroups));
    
    //############# add same favorite again
    GrouperUserDataApi.favoriteGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    favoriteGroups = GrouperUserDataApi.favoriteGroups(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(favoriteGroups));
    assertTrue(favoriteGroups.contains(favorite0));
    assertTrue(favoriteGroups.contains(favorite1));
    
    //############# remove one that isnt there
    GrouperUserDataApi.favoriteGroupRemove(userDataGroupName, SubjectTestHelper.SUBJ0, favorite2);
    favoriteGroups = GrouperUserDataApi.favoriteGroups(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(favoriteGroups));
    assertTrue(favoriteGroups.contains(favorite0));
    assertTrue(favoriteGroups.contains(favorite1));

    //############# remove one that is there
    GrouperUserDataApi.favoriteGroupRemove(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    favoriteGroups = GrouperUserDataApi.favoriteGroups(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(1, GrouperUtil.length(favoriteGroups));
    assertTrue(favoriteGroups.contains(favorite1));

    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testRecentlyUsedGroups2() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group recentlyUsed0 = new GroupSave(grouperSession).assignName("test:recentlyUsed0").assignCreateParentStemsIfNotExist(true).save();
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.recentlyUsedGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed0);
    GrouperUserDataApi.recentlyUsedGroupRemove(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed0);

    GrouperUserDataApi.recentlyUsedGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ1, recentlyUsed0);
    GrouperUserDataApi.recentlyUsedGroupRemove(userDataGroupName, SubjectTestHelper.SUBJ1, recentlyUsed0);

    Set<AttributeAssignValue> missingPitValues = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findMissingActivePITAttributeAssignValues();
    assertEquals(GrouperUtil.toStringForLog(missingPitValues), 0, GrouperUtil.length(missingPitValues));
    
  }
  
  /**
   * 
   */
  public void testRecentlyUsedGroups() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group recentlyUsed0 = new GroupSave(grouperSession).assignName("test:recentlyUsed0").assignCreateParentStemsIfNotExist(true).save();
    Group recentlyUsed1 = new GroupSave(grouperSession).assignName("test:recentlyUsed1").assignCreateParentStemsIfNotExist(true).save();
    Group recentlyUsed2 = new GroupSave(grouperSession).assignName("test:recentlyUsed2").assignCreateParentStemsIfNotExist(true).save();
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.recentlyUsedGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed0);
    GrouperUserDataApi.recentlyUsedGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed1);
    GrouperUserDataApi.recentlyUsedGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ1, recentlyUsed0);
    
    //############ subj0 has 0 and 1 as recentlyUsed groups
    Set<Group> recentlyUsedGroups = GrouperUserDataApi.recentlyUsedGroups(userDataGroupName, SubjectTestHelper.SUBJ0);
    
    assertEquals(2, GrouperUtil.length(recentlyUsedGroups));
    assertTrue(recentlyUsedGroups.contains(recentlyUsed0));
    assertTrue(recentlyUsedGroups.contains(recentlyUsed1));

    //############ subj1 has 0 as recentlyUsed group
    recentlyUsedGroups = GrouperUserDataApi.recentlyUsedGroups(userDataGroupName, SubjectTestHelper.SUBJ1);    
      
    assertEquals(1, GrouperUtil.length(recentlyUsedGroups));
    assertTrue(recentlyUsedGroups.contains(recentlyUsed0));

    //############# subj2 has no recentlyUseds
    recentlyUsedGroups = GrouperUserDataApi.recentlyUsedGroups(userDataGroupName, SubjectTestHelper.SUBJ2);    
    
    assertEquals(0, GrouperUtil.length(recentlyUsedGroups));
    
    //############# add same recentlyUsed again
    GrouperUserDataApi.recentlyUsedGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed0);
    recentlyUsedGroups = GrouperUserDataApi.recentlyUsedGroups(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(recentlyUsedGroups));
    assertTrue(recentlyUsedGroups.contains(recentlyUsed0));
    assertTrue(recentlyUsedGroups.contains(recentlyUsed1));
    
    //############# remove one that isnt there
    GrouperUserDataApi.recentlyUsedGroupRemove(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed2);
    recentlyUsedGroups = GrouperUserDataApi.recentlyUsedGroups(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(recentlyUsedGroups));
    assertTrue(recentlyUsedGroups.contains(recentlyUsed0));
    assertTrue(recentlyUsedGroups.contains(recentlyUsed1));

    //############# remove one that is there
    GrouperUserDataApi.recentlyUsedGroupRemove(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed0);
    recentlyUsedGroups = GrouperUserDataApi.recentlyUsedGroups(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(1, GrouperUtil.length(recentlyUsedGroups));
    assertTrue(recentlyUsedGroups.contains(recentlyUsed1));

    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testFavoriteAttributeDefs() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef favorite0 = new AttributeDefSave(grouperSession).assignName("test:favorite0").assignCreateParentStemsIfNotExist(true).save();
    
    favorite0.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    favorite0.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_VIEW, false);
    
    AttributeDef favorite1 = new AttributeDefSave(grouperSession).assignName("test:favorite1").assignCreateParentStemsIfNotExist(true).save();

    favorite1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    favorite1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);

    AttributeDef favorite2 = new AttributeDefSave(grouperSession).assignName("test:favorite2").assignCreateParentStemsIfNotExist(true).save();
    
    favorite2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    favorite2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.favoriteAttributeDefAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    GrouperUserDataApi.favoriteAttributeDefAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite1);
    GrouperUserDataApi.favoriteAttributeDefAdd(userDataGroupName, SubjectTestHelper.SUBJ1, favorite0);
    
    //############ subj0 has 0 and 1 as favorite groups
    Set<AttributeDef> favoriteAttributeDefs = GrouperUserDataApi.favoriteAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ0);
    
    assertEquals(2, GrouperUtil.length(favoriteAttributeDefs));
    assertTrue(favoriteAttributeDefs.contains(favorite0));
    assertTrue(favoriteAttributeDefs.contains(favorite1));
  
    //############ subj1 has 0 as favorite group
    favoriteAttributeDefs = GrouperUserDataApi.favoriteAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ1);    
      
    assertEquals(1, GrouperUtil.length(favoriteAttributeDefs));
    assertTrue(favoriteAttributeDefs.contains(favorite0));
  
    //############# subj2 has no favorites
    favoriteAttributeDefs = GrouperUserDataApi.favoriteAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ2);    
    
    assertEquals(0, GrouperUtil.length(favoriteAttributeDefs));
    
    //############# add same favorite again
    GrouperUserDataApi.favoriteAttributeDefAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    favoriteAttributeDefs = GrouperUserDataApi.favoriteAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(favoriteAttributeDefs));
    assertTrue(favoriteAttributeDefs.contains(favorite0));
    assertTrue(favoriteAttributeDefs.contains(favorite1));
    
    //############# remove one that isnt there
    GrouperUserDataApi.favoriteAttributeDefRemove(userDataGroupName, SubjectTestHelper.SUBJ0, favorite2);
    favoriteAttributeDefs = GrouperUserDataApi.favoriteAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(favoriteAttributeDefs));
    assertTrue(favoriteAttributeDefs.contains(favorite0));
    assertTrue(favoriteAttributeDefs.contains(favorite1));
  
    //############# remove one that is there
    GrouperUserDataApi.favoriteAttributeDefRemove(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    favoriteAttributeDefs = GrouperUserDataApi.favoriteAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(1, GrouperUtil.length(favoriteAttributeDefs));
    assertTrue(favoriteAttributeDefs.contains(favorite1));
  
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testFavoriteAttributeDefNames() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef favorite0def = new AttributeDefSave(grouperSession).assignName("test:favorite0def").assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName favorite0 = new AttributeDefNameSave(grouperSession, favorite0def).assignName("test:favorite0").assignCreateParentStemsIfNotExist(true).save();
    
    favorite0def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    favorite0def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_VIEW, false);
    
    AttributeDef favorite1def = new AttributeDefSave(grouperSession).assignName("test:favorite1def").assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName favorite1 = new AttributeDefNameSave(grouperSession, favorite1def).assignName("test:favorite1").assignCreateParentStemsIfNotExist(true).save();
  
    favorite1def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    favorite1def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
  
    AttributeDef favorite2def = new AttributeDefSave(grouperSession).assignName("test:favorite2def").assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName favorite2 = new AttributeDefNameSave(grouperSession, favorite2def).assignName("test:favorite2").assignCreateParentStemsIfNotExist(true).save();
    
    favorite2def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    favorite2def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.favoriteAttributeDefNameAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    GrouperUserDataApi.favoriteAttributeDefNameAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite1);
    GrouperUserDataApi.favoriteAttributeDefNameAdd(userDataGroupName, SubjectTestHelper.SUBJ1, favorite0);
    
    //############ subj0 has 0 and 1 as favorite groups
    Set<AttributeDefName> favoriteAttributeDefNames = GrouperUserDataApi.favoriteAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ0);
    
    assertEquals(2, GrouperUtil.length(favoriteAttributeDefNames));
    assertTrue(favoriteAttributeDefNames.contains(favorite0));
    assertTrue(favoriteAttributeDefNames.contains(favorite1));
  
    //############ subj1 has 0 as favorite group
    favoriteAttributeDefNames = GrouperUserDataApi.favoriteAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ1);    
      
    assertEquals(1, GrouperUtil.length(favoriteAttributeDefNames));
    assertTrue(favoriteAttributeDefNames.contains(favorite0));
  
    //############# subj2 has no favorites
    favoriteAttributeDefNames = GrouperUserDataApi.favoriteAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ2);    
    
    assertEquals(0, GrouperUtil.length(favoriteAttributeDefNames));
    
    //############# add same favorite again
    GrouperUserDataApi.favoriteAttributeDefNameAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    favoriteAttributeDefNames = GrouperUserDataApi.favoriteAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(favoriteAttributeDefNames));
    assertTrue(favoriteAttributeDefNames.contains(favorite0));
    assertTrue(favoriteAttributeDefNames.contains(favorite1));
    
    //############# remove one that isnt there
    GrouperUserDataApi.favoriteAttributeDefNameRemove(userDataGroupName, SubjectTestHelper.SUBJ0, favorite2);
    favoriteAttributeDefNames = GrouperUserDataApi.favoriteAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(favoriteAttributeDefNames));
    assertTrue(favoriteAttributeDefNames.contains(favorite0));
    assertTrue(favoriteAttributeDefNames.contains(favorite1));
  
    //############# remove one that is there
    GrouperUserDataApi.favoriteAttributeDefNameRemove(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    favoriteAttributeDefNames = GrouperUserDataApi.favoriteAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(1, GrouperUtil.length(favoriteAttributeDefNames));
    assertTrue(favoriteAttributeDefNames.contains(favorite1));
  
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testFavoriteStems() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem favorite0 = new StemSave(grouperSession).assignName("test:favorite0").assignCreateParentStemsIfNotExist(true).save();
    Stem favorite1 = new StemSave(grouperSession).assignName("test:favorite1").assignCreateParentStemsIfNotExist(true).save();
    Stem favorite2 = new StemSave(grouperSession).assignName("test:favorite2").assignCreateParentStemsIfNotExist(true).save();
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.favoriteStemAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    GrouperUserDataApi.favoriteStemAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite1);
    GrouperUserDataApi.favoriteStemAdd(userDataGroupName, SubjectTestHelper.SUBJ1, favorite0);
    
    //############ subj0 has 0 and 1 as favorite groups
    Set<Stem> favoriteStems = GrouperUserDataApi.favoriteStems(userDataGroupName, SubjectTestHelper.SUBJ0);
    
    assertEquals(2, GrouperUtil.length(favoriteStems));
    assertTrue(favoriteStems.contains(favorite0));
    assertTrue(favoriteStems.contains(favorite1));
  
    //############ subj1 has 0 as favorite group
    favoriteStems = GrouperUserDataApi.favoriteStems(userDataGroupName, SubjectTestHelper.SUBJ1);    
      
    assertEquals(1, GrouperUtil.length(favoriteStems));
    assertTrue(favoriteStems.contains(favorite0));
  
    //############# subj2 has no favorites
    favoriteStems = GrouperUserDataApi.favoriteStems(userDataGroupName, SubjectTestHelper.SUBJ2);    
    
    assertEquals(0, GrouperUtil.length(favoriteStems));
    
    //############# add same favorite again
    GrouperUserDataApi.favoriteStemAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    favoriteStems = GrouperUserDataApi.favoriteStems(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(favoriteStems));
    assertTrue(favoriteStems.contains(favorite0));
    assertTrue(favoriteStems.contains(favorite1));
    
    //############# remove one that isnt there
    GrouperUserDataApi.favoriteStemRemove(userDataGroupName, SubjectTestHelper.SUBJ0, favorite2);
    favoriteStems = GrouperUserDataApi.favoriteStems(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(favoriteStems));
    assertTrue(favoriteStems.contains(favorite0));
    assertTrue(favoriteStems.contains(favorite1));
  
    //############# remove one that is there
    GrouperUserDataApi.favoriteStemRemove(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0);
    favoriteStems = GrouperUserDataApi.favoriteStems(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(1, GrouperUtil.length(favoriteStems));
    assertTrue(favoriteStems.contains(favorite1));
  
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testRecentlyUsedStems() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem recentlyUsed0 = new StemSave(grouperSession).assignName("test:recentlyUsed0").assignCreateParentStemsIfNotExist(true).save();
    Stem recentlyUsed1 = new StemSave(grouperSession).assignName("test:recentlyUsed1").assignCreateParentStemsIfNotExist(true).save();
    Stem recentlyUsed2 = new StemSave(grouperSession).assignName("test:recentlyUsed2").assignCreateParentStemsIfNotExist(true).save();
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.recentlyUsedStemAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed0);
    GrouperUserDataApi.recentlyUsedStemAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed1);
    GrouperUserDataApi.recentlyUsedStemAdd(userDataGroupName, SubjectTestHelper.SUBJ1, recentlyUsed0);
    
    //############ subj0 has 0 and 1 as recentlyUsed groups
    Set<Stem> recentlyUsedStems = GrouperUserDataApi.recentlyUsedStems(userDataGroupName, SubjectTestHelper.SUBJ0);
    
    assertEquals(2, GrouperUtil.length(recentlyUsedStems));
    assertTrue(recentlyUsedStems.contains(recentlyUsed0));
    assertTrue(recentlyUsedStems.contains(recentlyUsed1));
  
    //############ subj1 has 0 as recentlyUsed group
    recentlyUsedStems = GrouperUserDataApi.recentlyUsedStems(userDataGroupName, SubjectTestHelper.SUBJ1);    
      
    assertEquals(1, GrouperUtil.length(recentlyUsedStems));
    assertTrue(recentlyUsedStems.contains(recentlyUsed0));
  
    //############# subj2 has no recentlyUseds
    recentlyUsedStems = GrouperUserDataApi.recentlyUsedStems(userDataGroupName, SubjectTestHelper.SUBJ2);    
    
    assertEquals(0, GrouperUtil.length(recentlyUsedStems));
    
    //############# add same recentlyUsed again
    GrouperUserDataApi.recentlyUsedStemAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed0);
    recentlyUsedStems = GrouperUserDataApi.recentlyUsedStems(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(recentlyUsedStems));
    assertTrue(recentlyUsedStems.contains(recentlyUsed0));
    assertTrue(recentlyUsedStems.contains(recentlyUsed1));
    
    //############# remove one that isnt there
    GrouperUserDataApi.recentlyUsedStemRemove(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed2);
    recentlyUsedStems = GrouperUserDataApi.recentlyUsedStems(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(recentlyUsedStems));
    assertTrue(recentlyUsedStems.contains(recentlyUsed0));
    assertTrue(recentlyUsedStems.contains(recentlyUsed1));
  
    //############# remove one that is there
    GrouperUserDataApi.recentlyUsedStemRemove(userDataGroupName, SubjectTestHelper.SUBJ0, recentlyUsed0);
    recentlyUsedStems = GrouperUserDataApi.recentlyUsedStems(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(1, GrouperUtil.length(recentlyUsedStems));
    assertTrue(recentlyUsedStems.contains(recentlyUsed1));
  
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testRecentlyUsedAttributeDefs() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef recent0 = new AttributeDefSave(grouperSession).assignName("test:recent0").assignCreateParentStemsIfNotExist(true).save();
    
    recent0.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    recent0.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_VIEW, false);
    
    AttributeDef recent1 = new AttributeDefSave(grouperSession).assignName("test:recent1").assignCreateParentStemsIfNotExist(true).save();
  
    recent1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    recent1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
  
    AttributeDef recent2 = new AttributeDefSave(grouperSession).assignName("test:recent2").assignCreateParentStemsIfNotExist(true).save();
    
    recent2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    recent2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.recentlyUsedAttributeDefAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recent0);
    GrouperUserDataApi.recentlyUsedAttributeDefAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recent1);
    GrouperUserDataApi.recentlyUsedAttributeDefAdd(userDataGroupName, SubjectTestHelper.SUBJ1, recent0);
    
    //############ subj0 has 0 and 1 as recent groups
    Set<AttributeDef> recentAttributeDefs = GrouperUserDataApi.recentlyUsedAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ0);
    
    assertEquals(2, GrouperUtil.length(recentAttributeDefs));
    assertTrue(recentAttributeDefs.contains(recent0));
    assertTrue(recentAttributeDefs.contains(recent1));
  
    //############ subj1 has 0 as recent group
    recentAttributeDefs = GrouperUserDataApi.recentlyUsedAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ1);    
      
    assertEquals(1, GrouperUtil.length(recentAttributeDefs));
    assertTrue(recentAttributeDefs.contains(recent0));
  
    //############# subj2 has no recents
    recentAttributeDefs = GrouperUserDataApi.recentlyUsedAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ2);    
    
    assertEquals(0, GrouperUtil.length(recentAttributeDefs));
    
    //############# add same recent again
    GrouperUserDataApi.recentlyUsedAttributeDefAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recent0);
    recentAttributeDefs = GrouperUserDataApi.recentlyUsedAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(recentAttributeDefs));
    assertTrue(recentAttributeDefs.contains(recent0));
    assertTrue(recentAttributeDefs.contains(recent1));
    
    //############# remove one that isnt there
    GrouperUserDataApi.recentlyUsedAttributeDefRemove(userDataGroupName, SubjectTestHelper.SUBJ0, recent2);
    recentAttributeDefs = GrouperUserDataApi.recentlyUsedAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(recentAttributeDefs));
    assertTrue(recentAttributeDefs.contains(recent0));
    assertTrue(recentAttributeDefs.contains(recent1));
  
    //############# remove one that is there
    GrouperUserDataApi.recentlyUsedAttributeDefRemove(userDataGroupName, SubjectTestHelper.SUBJ0, recent0);
    recentAttributeDefs = GrouperUserDataApi.recentlyUsedAttributeDefs(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(1, GrouperUtil.length(recentAttributeDefs));
    assertTrue(recentAttributeDefs.contains(recent1));
  
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testRecentlyUsedAttributeDefNames() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef recent0def = new AttributeDefSave(grouperSession).assignName("test:recent0def").assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName recent0 = new AttributeDefNameSave(grouperSession, recent0def).assignName("test:recent0").assignCreateParentStemsIfNotExist(true).save();
    
    recent0def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    recent0def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_VIEW, false);
    
    AttributeDef recent1def = new AttributeDefSave(grouperSession).assignName("test:recent1def").assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName recent1 = new AttributeDefNameSave(grouperSession, recent1def).assignName("test:recent1").assignCreateParentStemsIfNotExist(true).save();
  
    recent1def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    recent1def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
  
    AttributeDef recent2def = new AttributeDefSave(grouperSession).assignName("test:recent2def").assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName recent2 = new AttributeDefNameSave(grouperSession, recent2def).assignName("test:recent2").assignCreateParentStemsIfNotExist(true).save();
    
    recent2def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    recent2def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.recentlyUsedAttributeDefNameAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recent0);
    GrouperUserDataApi.recentlyUsedAttributeDefNameAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recent1);
    GrouperUserDataApi.recentlyUsedAttributeDefNameAdd(userDataGroupName, SubjectTestHelper.SUBJ1, recent0);
    
    //############ subj0 has 0 and 1 as recent groups
    Set<AttributeDefName> recentAttributeDefNames = GrouperUserDataApi.recentlyUsedAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ0);
    
    assertEquals(2, GrouperUtil.length(recentAttributeDefNames));
    assertTrue(recentAttributeDefNames.contains(recent0));
    assertTrue(recentAttributeDefNames.contains(recent1));
  
    //############ subj1 has 0 as recent group
    recentAttributeDefNames = GrouperUserDataApi.recentlyUsedAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ1);    
      
    assertEquals(1, GrouperUtil.length(recentAttributeDefNames));
    assertTrue(recentAttributeDefNames.contains(recent0));
  
    //############# subj2 has no recents
    recentAttributeDefNames = GrouperUserDataApi.recentlyUsedAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ2);    
    
    assertEquals(0, GrouperUtil.length(recentAttributeDefNames));
    
    //############# add same recent again
    GrouperUserDataApi.recentlyUsedAttributeDefNameAdd(userDataGroupName, SubjectTestHelper.SUBJ0, recent0);
    recentAttributeDefNames = GrouperUserDataApi.recentlyUsedAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(recentAttributeDefNames));
    assertTrue(recentAttributeDefNames.contains(recent0));
    assertTrue(recentAttributeDefNames.contains(recent1));
    
    //############# remove one that isnt there
    GrouperUserDataApi.recentlyUsedAttributeDefNameRemove(userDataGroupName, SubjectTestHelper.SUBJ0, recent2);
    recentAttributeDefNames = GrouperUserDataApi.recentlyUsedAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(recentAttributeDefNames));
    assertTrue(recentAttributeDefNames.contains(recent0));
    assertTrue(recentAttributeDefNames.contains(recent1));
  
    //############# remove one that is there
    GrouperUserDataApi.recentlyUsedAttributeDefNameRemove(userDataGroupName, SubjectTestHelper.SUBJ0, recent0);
    recentAttributeDefNames = GrouperUserDataApi.recentlyUsedAttributeDefNames(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(1, GrouperUtil.length(recentAttributeDefNames));
    assertTrue(recentAttributeDefNames.contains(recent1));
  
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testFavoriteMembers() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Member member7 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ7, true);
    Member member8 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ8, true);
    Member member9 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ9, true);
        
    new StemSave(grouperSession).assignName("test").save();
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.favoriteMemberAdd(userDataGroupName, SubjectTestHelper.SUBJ0, member7);
    GrouperUserDataApi.favoriteMemberAdd(userDataGroupName, SubjectTestHelper.SUBJ0, member8);
    GrouperUserDataApi.favoriteMemberAdd(userDataGroupName, SubjectTestHelper.SUBJ1, member7);
    
    //############ subj0 has 0 and 1 as favorite groups
    Set<Member> favoriteMembers = GrouperUserDataApi.favoriteMembers(userDataGroupName, SubjectTestHelper.SUBJ0);
    
    assertEquals(2, GrouperUtil.length(favoriteMembers));
    assertTrue(favoriteMembers.contains(member7));
    assertTrue(favoriteMembers.contains(member8));
  
    //############ subj1 has 0 as favorite group
    favoriteMembers = GrouperUserDataApi.favoriteMembers(userDataGroupName, SubjectTestHelper.SUBJ1);    
      
    assertEquals(1, GrouperUtil.length(favoriteMembers));
    assertTrue(favoriteMembers.contains(member7));
  
    //############# subj2 has no favorites
    favoriteMembers = GrouperUserDataApi.favoriteMembers(userDataGroupName, SubjectTestHelper.SUBJ2);    
    
    assertEquals(0, GrouperUtil.length(favoriteMembers));
    
    //############# add same favorite again
    GrouperUserDataApi.favoriteMemberAdd(userDataGroupName, SubjectTestHelper.SUBJ0, member7);
    favoriteMembers = GrouperUserDataApi.favoriteMembers(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(favoriteMembers));
    assertTrue(favoriteMembers.contains(member7));
    assertTrue(favoriteMembers.contains(member8));
    
    //############# remove one that isnt there
    GrouperUserDataApi.favoriteMemberRemove(userDataGroupName, SubjectTestHelper.SUBJ0, member9);
    favoriteMembers = GrouperUserDataApi.favoriteMembers(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(favoriteMembers));
    assertTrue(favoriteMembers.contains(member7));
    assertTrue(favoriteMembers.contains(member8));
  
    //############# remove one that is there
    GrouperUserDataApi.favoriteMemberRemove(userDataGroupName, SubjectTestHelper.SUBJ0, member7);
    favoriteMembers = GrouperUserDataApi.favoriteMembers(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(1, GrouperUtil.length(favoriteMembers));
    assertTrue(favoriteMembers.contains(member8));
  
    GrouperSession.stopQuietly(grouperSession);
  }

  /**
   * 
   */
  public void testRecentlyUsedMembers() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Member member7 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ7, true);
    Member member8 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ8, true);
    Member member9 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ9, true);
        
    new StemSave(grouperSession).assignName("test").save();
    
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.recentlyUsedMemberAdd(userDataGroupName, SubjectTestHelper.SUBJ0, member7);
    GrouperUserDataApi.recentlyUsedMemberAdd(userDataGroupName, SubjectTestHelper.SUBJ0, member8);
    GrouperUserDataApi.recentlyUsedMemberAdd(userDataGroupName, SubjectTestHelper.SUBJ1, member7);
    
    //############ subj0 has 0 and 1 as recentlyUsed groups
    Set<Member> recentlyUsedMembers = GrouperUserDataApi.recentlyUsedMembers(userDataGroupName, SubjectTestHelper.SUBJ0);
    
    assertEquals(2, GrouperUtil.length(recentlyUsedMembers));
    assertTrue(recentlyUsedMembers.contains(member7));
    assertTrue(recentlyUsedMembers.contains(member8));
  
    //############ subj1 has 0 as recentlyUsed group
    recentlyUsedMembers = GrouperUserDataApi.recentlyUsedMembers(userDataGroupName, SubjectTestHelper.SUBJ1);    
      
    assertEquals(1, GrouperUtil.length(recentlyUsedMembers));
    assertTrue(recentlyUsedMembers.contains(member7));
  
    //############# subj2 has no recentlyUseds
    recentlyUsedMembers = GrouperUserDataApi.recentlyUsedMembers(userDataGroupName, SubjectTestHelper.SUBJ2);    
    
    assertEquals(0, GrouperUtil.length(recentlyUsedMembers));
    
    //############# add same recentlyUsed again
    GrouperUserDataApi.recentlyUsedMemberAdd(userDataGroupName, SubjectTestHelper.SUBJ0, member7);
    recentlyUsedMembers = GrouperUserDataApi.recentlyUsedMembers(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(recentlyUsedMembers));
    assertTrue(recentlyUsedMembers.contains(member7));
    assertTrue(recentlyUsedMembers.contains(member8));
    
    //############# remove one that isnt there
    GrouperUserDataApi.recentlyUsedMemberRemove(userDataGroupName, SubjectTestHelper.SUBJ0, member9);
    recentlyUsedMembers = GrouperUserDataApi.recentlyUsedMembers(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(2, GrouperUtil.length(recentlyUsedMembers));
    assertTrue(recentlyUsedMembers.contains(member7));
    assertTrue(recentlyUsedMembers.contains(member8));
  
    //############# remove one that is there
    GrouperUserDataApi.recentlyUsedMemberRemove(userDataGroupName, SubjectTestHelper.SUBJ0, member7);
    recentlyUsedMembers = GrouperUserDataApi.recentlyUsedMembers(userDataGroupName, SubjectTestHelper.SUBJ0);    
    
    assertEquals(1, GrouperUtil.length(recentlyUsedMembers));
    assertTrue(recentlyUsedMembers.contains(member8));
  
    GrouperSession.stopQuietly(grouperSession);
  }
  
}
