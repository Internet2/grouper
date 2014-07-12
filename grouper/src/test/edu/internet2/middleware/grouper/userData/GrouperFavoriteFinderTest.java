/*******************************************************************************
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
 ******************************************************************************/
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
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class GrouperFavoriteFinderTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperFavoriteFinderTest("testFavoriteFinder"));
  }
  
  /**
   * 
   */
  public GrouperFavoriteFinderTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public GrouperFavoriteFinderTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testFavoriteFinder() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef favorite0def = new AttributeDefSave(grouperSession).assignName("test:favorite0def").assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName favorite0name = new AttributeDefNameSave(grouperSession, favorite0def).assignName("test:favorite0name").assignCreateParentStemsIfNotExist(true).save();
    
    favorite0def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    favorite0def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_VIEW, false);
    
    AttributeDef favorite1def = new AttributeDefSave(grouperSession).assignName("test:favorite1def").assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName favorite1name = new AttributeDefNameSave(grouperSession, favorite1def).assignName("test:favorite1name").assignCreateParentStemsIfNotExist(true).save();
  
    favorite1def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    favorite1def.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
  
    String userDataGroupName = "test:testUserData";
    
    GrouperUserDataApi.favoriteAttributeDefNameAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0name);
    GrouperUserDataApi.favoriteAttributeDefNameAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite1name);

    Stem favorite0stem = new StemSave(grouperSession).assignName("test:favorite0stem").assignCreateParentStemsIfNotExist(true).save();
    Stem favorite1stem = new StemSave(grouperSession).assignName("test:favorite1stem").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperUserDataApi.favoriteStemAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0stem);
    GrouperUserDataApi.favoriteStemAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite1stem);

    Group favorite0group = new GroupSave(grouperSession).assignName("test:favorite0group").assignCreateParentStemsIfNotExist(true).save();
    Group favorite1group = new GroupSave(grouperSession).assignName("test:favorite1group").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperUserDataApi.favoriteGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0group);
    GrouperUserDataApi.favoriteGroupAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite1group);

    
    AttributeDef favorite0deffav = new AttributeDefSave(grouperSession).assignName("test:favorite0deffav").assignCreateParentStemsIfNotExist(true).save();
    
    favorite0deffav.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    favorite0deffav.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_VIEW, false);
    
    AttributeDef favorite1deffav = new AttributeDefSave(grouperSession).assignName("test:favorite1deffav").assignCreateParentStemsIfNotExist(true).save();

    favorite1deffav.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    favorite1deffav.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);

    GrouperUserDataApi.favoriteAttributeDefAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite0deffav);
    GrouperUserDataApi.favoriteAttributeDefAdd(userDataGroupName, SubjectTestHelper.SUBJ0, favorite1deffav);

    Member member7 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ7, true); 
    Member member8 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ8, true);
        
    GrouperUserDataApi.favoriteMemberAdd(userDataGroupName, SubjectTestHelper.SUBJ0, member7);
    GrouperUserDataApi.favoriteMemberAdd(userDataGroupName, SubjectTestHelper.SUBJ0, member8);
    
    //############ subj0 has 0 and 1 as favorites

    GrouperFavoriteFinder grouperFavoriteFinder = new GrouperFavoriteFinder()
      .assignUserDataGroupName(userDataGroupName).assignSubject(SubjectTestHelper.SUBJ0);
    
    Set<GrouperObject> favorites = grouperFavoriteFinder.findFavorites();
    assertEquals(10, GrouperUtil.length(favorites));

    favorites = grouperFavoriteFinder.assignQueryOptions(QueryOptions.create(null, null, 1, 2)).findFavorites();
    assertEquals(2, GrouperUtil.length(favorites));
    assertEquals(favorite0group.getName(), ((Group)GrouperUtil.get(favorites, 0)).getName());
    assertEquals(favorite1group.getName(), ((Group)GrouperUtil.get(favorites, 1)).getName());
    
    favorites = grouperFavoriteFinder.assignQueryOptions(null)
      .assignSplitScope(true).assignFilterText("stem test").findFavorites();

    assertEquals(2, GrouperUtil.length(favorites));
    assertEquals(favorite0stem.getName(), ((Stem)GrouperUtil.get(favorites, 0)).getName());
    assertEquals(favorite1stem.getName(), ((Stem)GrouperUtil.get(favorites, 1)).getName());

    //for (GrouperObject favorite : favorites) {
      //System.out.println(favorite.getName());
    //}

    GrouperSession.stopQuietly(grouperSession);


  }
  
}
