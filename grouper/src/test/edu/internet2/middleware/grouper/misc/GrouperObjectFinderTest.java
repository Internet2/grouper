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
 * 
 */
package edu.internet2.middleware.grouper.misc;

import java.util.ArrayList;
import java.util.List;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.ObjectPrivilege;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author mchyzer
 *
 */
public class GrouperObjectFinderTest extends GrouperTest {

  /**
   * 
   */
  public GrouperObjectFinderTest() {

  }
  
  /**
   * 
   * @param name
   */
  public GrouperObjectFinderTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    TestRunner.run(new GrouperObjectFinderTest("testFindFoldersAllowed"));

  }

  /**
   * 
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
  }

  /**
   * 
   */
  public void testFindFoldersAllowed() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem test = new StemSave(grouperSession).assignName("test").save();
    
    Stem stem1 = new StemSave(grouperSession).assignName("test:stem1").save();
    Group group1 = new GroupSave(grouperSession).assignName("test:stem1:group1").save();
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    
    Stem stem2 = new StemSave(grouperSession).assignName("test:stem2").save();
    Group group2 = new GroupSave(grouperSession).assignName("test:stem2:group2").save();
    group2.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN);
    
    Stem stem3 = new StemSave(grouperSession).assignName("test:stem3").save();
    Group group3 = new GroupSave(grouperSession).assignName("test:stem3:group3").save();
    group3.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW);
    
    Stem stem4 = new StemSave(grouperSession).assignName("test:stem4").save();
    Stem stem4sub = new StemSave(grouperSession).assignName("test:stem4:stem4sub").save();
    stem4sub.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    
    
    Stem stem5 = new StemSave(grouperSession).assignName("test:stem5").save();
    AttributeDef attributeDef5 = new AttributeDefSave(grouperSession).assignName("test:stem5:attributeDef5")
        .assignAttributeDefType(AttributeDefType.attr).assignValueType(AttributeDefValueType.marker).save();
    attributeDef5.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);

    Stem stem6 = new StemSave(grouperSession).assignName("test:stem6").save();
    Stem stem6sub = new StemSave(grouperSession).assignName("test:stem6:stem6sub").save();
    Group group6 = new GroupSave(grouperSession).assignName("test:stem6:stem6sub:group6").save();
    group6.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);

    Stem stem7 = new StemSave(grouperSession).assignName("test:stem7").save();
    stem7.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);

    Stem stem8 = new StemSave(grouperSession).assignName("test:stem8").save();
    Stem stem8sub = new StemSave(grouperSession).assignName("test:stem8:stem8sub").save();
    Stem stem8subsub = new StemSave(grouperSession).assignName("test:stem8:stem8sub:stem8subsub").save();
    stem8subsub.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);

    Stem stem9 = new StemSave(grouperSession).assignName("test:stem9").save();
    Stem stem9sub = new StemSave(grouperSession).assignName("test:stem9:stem9sub").save();
    AttributeDef attributeDef9sub = new AttributeDefSave(grouperSession).assignName("test:stem9:stem9sub:attributeDef9")
        .assignAttributeDefType(AttributeDefType.attr).assignValueType(AttributeDefValueType.marker).save();
    attributeDef9sub.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    QueryOptions queryOptions = QueryOptions.create("displayExtension", true, 1, 50);

    GrouperObjectFinder grouperObjectFinder = new GrouperObjectFinder()
      .assignObjectPrivilege(ObjectPrivilege.view)
      .assignParentStemId(test.getId())
      .assignQueryOptions(queryOptions)
      .assignSplitScope(true).assignStemScope(Scope.ONE)
      .assignSubject(GrouperSession.staticGrouperSession().getSubject());

//    if (!StringUtils.isBlank(filterText)) {
//      grouperObjectFinder.assignFilterText(filterText);
//    }

    List<GrouperObject> results = new ArrayList<GrouperObject>(grouperObjectFinder.findGrouperObjects());
    
    assertEquals(7, results.size());
    assertEquals(stem1.getName(), results.get(0).getName());
    assertEquals(stem2.getName(), results.get(1).getName());
    assertEquals(stem4.getName(), results.get(2).getName());
    assertEquals(stem5.getName(), results.get(3).getName());
    assertEquals(stem6.getName(), results.get(4).getName());
    assertEquals(stem8.getName(), results.get(5).getName());
    assertEquals(stem9.getName(), results.get(6).getName());
    
    GrouperSession.stopQuietly(grouperSession);

  
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    grouperObjectFinder = new GrouperObjectFinder()
      .assignObjectPrivilege(ObjectPrivilege.view)
      .assignParentStemId(test.getId())
      .assignQueryOptions(queryOptions)
      .assignSplitScope(true).assignStemScope(Scope.ONE)
      .assignSubject(GrouperSession.staticGrouperSession().getSubject());

    results = new ArrayList<GrouperObject>(grouperObjectFinder.findGrouperObjects());
    
    assertEquals(3, results.size());
    assertEquals(stem2.getName(), results.get(0).getName());
    assertEquals(stem3.getName(), results.get(1).getName());
    assertEquals(stem7.getName(), results.get(2).getName());
    
    GrouperSession.stopQuietly(grouperSession);

  
  }
  
  /**
   * 
   */
  public void testFindObjects() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    List<Stem> stems = new ArrayList<Stem>();
    
    //make 17 stems
    for (int i=0;i<17;i++) {
      stems.add(new StemSave(grouperSession).assignName("test:testStem_" 
          + StringUtils.leftPad(Integer.toString(i), 3, '0'))
          .assignCreateParentStemsIfNotExist(true).save());
      
      if (i<5) {
        stems.get(i).grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
        stems.get(i).grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
      }
      
    }

    //make a sub stem
    Stem subStem = new StemSave(grouperSession).assignName("test:testStem_000:aStem")
        .assignCreateParentStemsIfNotExist(true).save();

    subStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    subStem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);

    
    List<Group> groups = new ArrayList<Group>();
    
    //make 23 groups
    for (int i=0;i<13;i++) {
      groups.add(new GroupSave(grouperSession).assignName("test:testGroup_" 
          + StringUtils.leftPad(Integer.toString(i), 3, '0'))
          .assignCreateParentStemsIfNotExist(true).save());
      
      if (i<5) {
        groups.get(i).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
        groups.get(i).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
        groups.get(i).grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE);
        groups.get(i).grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
      }

    }

    //make a sub group
    Group subGroup = new GroupSave(grouperSession).assignName("test:testStem_000:aGroup")
        .assignCreateParentStemsIfNotExist(true).save();

    subGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    subGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    subGroup.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE);
    subGroup.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);

    List<AttributeDef> attributeDefs = new ArrayList<AttributeDef>();

    //make 11 attributedefs
    for (int i=0;i<11;i++) {
    
      attributeDefs.add(new AttributeDefSave(grouperSession).assignName("test:testAttributeDef_" 
          + StringUtils.leftPad(Integer.toString(i), 3, '0'))
          .assignCreateParentStemsIfNotExist(true).save());

      if (i<5) {
        attributeDefs.get(i).getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
        attributeDefs.get(i).getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
        attributeDefs.get(i).getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_UPDATE, false);
        attributeDefs.get(i).getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ3, AttributeDefPrivilege.ATTR_ADMIN, false);
      }

    }

    //make a sub attribute def
    AttributeDef subAttributeDef = new AttributeDefSave(grouperSession).assignName("test:testStem_000:anAttributeDef")
        .assignCreateParentStemsIfNotExist(true).save();

    subAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    subAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    subAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_UPDATE, false);
    subAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ3, AttributeDefPrivilege.ATTR_ADMIN, false);

    List<AttributeDefName> attributeDefNames = new ArrayList<AttributeDefName>();

    //make 7 attributedefnames
    for (int i=0;i<7;i++) {
      attributeDefNames.add(new AttributeDefNameSave(grouperSession, attributeDefs.get(0))
        .assignName("test:testAttributeDefName_" 
          + StringUtils.leftPad(Integer.toString(i), 3, '0'))
          .assignCreateParentStemsIfNotExist(true).save());
    }

    //make a sub attribute def name
    new AttributeDefNameSave(grouperSession, attributeDefs.get(0))
      .assignName("test:testStem_000:anAttributeDefName")
        .assignCreateParentStemsIfNotExist(true).save();


    //find all
    List<GrouperObject> grouperObjectList = new ArrayList<GrouperObject>(new GrouperObjectFinder().findGrouperObjects());
    
    //should return a lot of objects
    assertTrue(Integer.toString(GrouperUtil.length(grouperObjectList)), GrouperUtil.length(grouperObjectList) >= 52);

    Stem testStem = StemFinder.findByName(grouperSession, "test", false);

    //find in a stem
    grouperObjectList = new ArrayList<GrouperObject>(new GrouperObjectFinder()
      .assignParentStemId(testStem.getId()).assignStemScope(Scope.SUB).findGrouperObjects());

    //should return a lot of objects
    assertEquals(52, GrouperUtil.length(grouperObjectList));

    //lets look at some of the objects    
    //Stem: test:testStem_000
    //Stem: test:testStem_000:aStem
    //Stem: test:testStem_001
    //Stem: test:testStem_002
    //Stem: test:testStem_003
    //Stem: test:testStem_004
    //Stem: test:testStem_005
    //Stem: test:testStem_006
    //Stem: test:testStem_007
    //Stem: test:testStem_008
    //Stem: test:testStem_009
    //Stem: test:testStem_010
    //Stem: test:testStem_011
    //Stem: test:testStem_012
    //Stem: test:testStem_013
    //Stem: test:testStem_014
    //Stem: test:testStem_015
    //Stem: test:testStem_016
    //Group: test:testGroup_000
    //Group: test:testGroup_001
    //Group: test:testGroup_002
    //Group: test:testGroup_003
    //Group: test:testGroup_004
    //Group: test:testGroup_005
    //Group: test:testGroup_006
    //Group: test:testGroup_007
    //Group: test:testGroup_008
    //Group: test:testGroup_009
    //Group: test:testGroup_010
    //Group: test:testGroup_011
    //Group: test:testGroup_012
    //Group: test:testStem_000:aGroup
    //AttributeDef: test:testAttributeDef_000
    //AttributeDef: test:testAttributeDef_001
    //AttributeDef: test:testAttributeDef_002
    //AttributeDef: test:testAttributeDef_003
    //AttributeDef: test:testAttributeDef_004
    //AttributeDef: test:testAttributeDef_005
    //AttributeDef: test:testAttributeDef_006
    //AttributeDef: test:testAttributeDef_007
    //AttributeDef: test:testAttributeDef_008
    //AttributeDef: test:testAttributeDef_009
    //AttributeDef: test:testAttributeDef_010
    //AttributeDef: test:testStem_000:anAttributeDef
    //AttributeDefName: test:testAttributeDefName_000
    //AttributeDefName: test:testAttributeDefName_001
    //AttributeDefName: test:testAttributeDefName_002
    //AttributeDefName: test:testAttributeDefName_003
    //AttributeDefName: test:testAttributeDefName_004
    //AttributeDefName: test:testAttributeDefName_005
    //AttributeDefName: test:testAttributeDefName_006
    //AttributeDefName: test:testStem_000:anAttributeDefName    

    
    //Stem: test:testStem_000
    assertEquals("test:testStem_000", grouperObjectList.get(0).getName());
    //Stem: test:testStem_000:aStem
    assertEquals("test:testStem_000:aStem", grouperObjectList.get(1).getName());
    //Group: test:testGroup_000
    assertEquals("test:testGroup_000", grouperObjectList.get(18).getName());
    //Group: test:testStem_000:aGroup
    assertEquals("test:testStem_000:aGroup", grouperObjectList.get(31).getName());
    //AttributeDef: test:testAttributeDef_000
    assertEquals("test:testAttributeDef_000", grouperObjectList.get(32).getName());
    //AttributeDefName: test:testAttributeDefName_000
    assertEquals("test:testAttributeDefName_000", grouperObjectList.get(44).getName());

    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    grouperObjectList = new ArrayList<GrouperObject>(new GrouperObjectFinder()
      .assignObjectPrivilege(ObjectPrivilege.view).assignSubject(SubjectTestHelper.SUBJ0)
      .assignParentStemId(testStem.getId()).assignStemScope(Scope.ONE).findGrouperObjects());

    //should return a lot of objects
    assertEquals(34, GrouperUtil.length(grouperObjectList));

    grouperObjectList = new ArrayList<GrouperObject>(new GrouperObjectFinder()
      .assignObjectPrivilege(ObjectPrivilege.view).assignSubject(SubjectTestHelper.SUBJ0).assignFilterText("000")
      .assignParentStemId(testStem.getId()).assignStemScope(Scope.ONE).findGrouperObjects());
  
    //should return a lot of objects
    assertEquals(4, GrouperUtil.length(grouperObjectList));

    assertEquals("test:testStem_000", grouperObjectList.get(0).getName());
    assertEquals("test:testGroup_000", grouperObjectList.get(1).getName());
    assertEquals("test:testAttributeDef_000", grouperObjectList.get(2).getName());
    assertEquals("test:testAttributeDefName_000", grouperObjectList.get(3).getName());

    grouperObjectList = new ArrayList<GrouperObject>(new GrouperObjectFinder()
      .assignQueryOptions(QueryOptions.create("displayExtension", true, 1, 2))
      .assignObjectPrivilege(ObjectPrivilege.view).assignSubject(SubjectTestHelper.SUBJ0).assignFilterText("000")
      .assignParentStemId(testStem.getId()).assignStemScope(Scope.ONE).findGrouperObjects());
  
    //should return a lot of objects
    assertEquals(2, GrouperUtil.length(grouperObjectList));
  
    assertEquals("test:testStem_000", grouperObjectList.get(0).getName());
    assertEquals("test:testGroup_000", grouperObjectList.get(1).getName());

    grouperObjectList = new ArrayList<GrouperObject>(new GrouperObjectFinder()
      .assignQueryOptions(QueryOptions.create("displayExtension", true, 2, 2))
      .assignObjectPrivilege(ObjectPrivilege.view).assignSubject(SubjectTestHelper.SUBJ0).assignFilterText("000")
      .assignParentStemId(testStem.getId()).assignStemScope(Scope.ONE).findGrouperObjects());
  
    //should return a lot of objects
    assertEquals(2, GrouperUtil.length(grouperObjectList));
    
    
    assertEquals("test:testAttributeDef_000", grouperObjectList.get(0).getName());
    assertEquals("test:testAttributeDefName_000", grouperObjectList.get(1).getName());

    //lets find all based on a filter
    grouperObjectList = new ArrayList<GrouperObject>(new GrouperObjectFinder().assignFilterText("test.").findGrouperObjects());

    assertEquals(10, GrouperUtil.length(grouperObjectList));
    
    for (int i=0;i<10;i++) {
      assertEquals("test.subject." + i, ((GrouperObjectSubjectWrapper)grouperObjectList.get(i)).getSubject().getId());
    }

    //lets try the first two
    grouperObjectList = new ArrayList<GrouperObject>(new GrouperObjectFinder().assignFilterText("test.")
        .assignQueryOptions(QueryOptions.create(null, null, 1, 2)).findGrouperObjects());

    assertEquals(2, GrouperUtil.length(grouperObjectList));
    
    for (int i=0;i<2;i++) {
      assertEquals("test.subject." + i, ((GrouperObjectSubjectWrapper)grouperObjectList.get(i)).getSubject().getId());
    }
    
    //lets try a bunch of objects
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.retrieveCount(true);
    queryOptions.retrieveResults(false);
    grouperObjectList = new ArrayList<GrouperObject>(new GrouperObjectFinder().assignQueryOptions(queryOptions)
        .assignFilterText("test").findGrouperObjects());

    int theCount = queryOptions.getCount().intValue();

    int pageNumber = 1+ (theCount / 10);

    grouperObjectList = new ArrayList<GrouperObject>(new GrouperObjectFinder()
      .assignQueryOptions(QueryOptions.create(null, null, pageNumber, 10))
        .assignFilterText("test").findGrouperObjects());

    int itemsOnLastPage = theCount % 10;
    
    assertEquals(itemsOnLastPage, GrouperUtil.length(grouperObjectList));
    
    for (int i=0;i<itemsOnLastPage;i++) {
      assertEquals("test.subject." + (i+10-itemsOnLastPage), 
          ((GrouperObjectSubjectWrapper)grouperObjectList.get(i)).getSubject().getId());
    }

    
    System.out.println(grouperObjectList.size());
    
    for (GrouperObject grouperObject : grouperObjectList) {
      System.out.println(grouperObject.getClass().getSimpleName() + ": " + grouperObject.getName());
    }

  }
  
}
