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
package edu.internet2.middleware.grouper.member;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SourceManager;
import edu.internet2.middleware.subject.util.ExpirableCache;

/**
 * @author shilen
 * $Id$
 */
public class TestMemberAttributes extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMemberAttributes("testPersonMember"));
    //TestRunner.run(TestMemberAttributes.class);
  }
  
  /** top level stems */
  private Stem edu, edu2;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param name
   */
  public TestMemberAttributes(String name) {
    super(name);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    edu2   = StemHelper.addChildStem(root, "edu2", "education2");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
  }
  
  /**
   * 
   */
  public void testPersonMember() {
    Subject subj = SubjectTestHelper.SUBJ0;
    edu.grantPriv(subj, NamingPrivilege.CREATE);

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(subj, true);

    assertEquals(subj.getName(), member.getName());
    assertEquals(subj.getDescription(), member.getDescription());
    
    //not case sensitive
    assertEquals(subj.getAttributeValue("LFNAME"), member.getSortString0());
    assertEquals(subj.getAttributeValue("lfname"), member.getSortString0());

    assertEquals(subj.getAttributeValue("lfname", true), member.getSortString0());
    assertEquals(subj.getAttributeValue("LFNAME", true), member.getSortString0());
    
    assertTrue(subj.getAttributes().keySet().contains("lfname"));
    
    assertEquals(subj.getAttributes().get("lfname").iterator().next(), member.getSortString0());
    assertEquals(subj.getAttributes().get("LFNAME").iterator().next(), member.getSortString0());
    
    assertEquals(subj.getAttributes(false).get("lfname").iterator().next(), member.getSortString0());
    assertEquals(subj.getAttributes(false).get("LFNAME").iterator().next(), member.getSortString0());
    
    assertEquals(subj.getAttributes(true).get("lfname").iterator().next(), member.getSortString0());
    assertEquals(subj.getAttributes(true).get("LFNAME").iterator().next(), member.getSortString0());
    
    assertTrue(subj.getAttributeValues("lfname").contains(member.getSortString0()));
    assertTrue(subj.getAttributeValues("LFNAME").contains(member.getSortString0()));
    
    assertTrue(subj.getAttributeValues("lfname", false).contains(member.getSortString0()));
    assertTrue(subj.getAttributeValues("LFNAME", false).contains(member.getSortString0()));
    
    assertTrue(subj.getAttributeValues("lfname", true).contains(member.getSortString0()));
    assertTrue(subj.getAttributeValues("LFNAME", true).contains(member.getSortString0()));
    
    assertEquals(subj.getAttributeValueOrCommaSeparated("lfname"), member.getSortString0());
    assertEquals(subj.getAttributeValueOrCommaSeparated("LFNAME"), member.getSortString0());
    
    assertEquals(subj.getAttributeValueOrCommaSeparated("lfname", false), member.getSortString0());
    assertEquals(subj.getAttributeValueOrCommaSeparated("LFNAME", false), member.getSortString0());
    
    assertEquals(subj.getAttributeValueOrCommaSeparated("lfname", true), member.getSortString0());
    assertEquals(subj.getAttributeValueOrCommaSeparated("LFNAME", true), member.getSortString0());
    
    assertEquals(subj.getAttributeValueSingleValued("lfname"), member.getSortString0());
    assertEquals(subj.getAttributeValueSingleValued("LFNAME"), member.getSortString0());
    
    assertEquals(subj.getAttributeValueSingleValued("lfname", false), member.getSortString0());
    assertEquals(subj.getAttributeValueSingleValued("LFNAME", false), member.getSortString0());
    
    assertEquals(subj.getAttributeValueSingleValued("lfname", true), member.getSortString0());
    assertEquals(subj.getAttributeValueSingleValued("LFNAME", true), member.getSortString0());
    
    assertEquals(subj.getAttributeValue("LOGINID"), member.getSortString1());

    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(subj.getName() + "," + subj.getAttributeValue("LFNAME") + "," + subj.getAttributeValue("LOGINID") + "," + subj.getDescription() + "," + subj.getAttributeValue("EMAIL").toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    
    // verify internal attributes
    assertFalse(subj.getAttributes().containsKey("searchAttribute0"));
    assertTrue(subj.getAttributes(false).containsKey("searchAttribute0"));
    
    // verify that an update will work
    member.setSortString0("bogus");
    member.setSortString1(null);
    member.store();
    
    // member record should get corrected by this
    SubjectFinder.flushCache();
    SubjectFinder.findById(subj.getId(), true);
    
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // ignore
    }
    
    GrouperCacheUtils.clearAllCaches();
    
    member = GrouperDAOFactory.getFactory().getMember().findBySubject(subj, true);
    assertEquals(subj.getAttributeValue("LFNAME"), member.getSortString0());
    assertEquals(subj.getAttributeValue("LOGINID"), member.getSortString1());
  }
  
  /**
   * 
   */
  public void testPersonMemberNonDefault() {
    
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectVirtualAttribute_2_sortAttribute2", "test2");
    source.addInitParam("sortAttribute2", "sortAttribute2");
    source.addInitParam("subjectVirtualAttribute_3_sortAttribute3", "test3");
    source.addInitParam("sortAttribute3", "sortAttribute3");
    source.addInitParam("subjectVirtualAttribute_4_sortAttribute4", "test4");
    source.addInitParam("sortAttribute4", "sortAttribute4");
    source.addInitParam("subjectVirtualAttribute_1_searchAttribute1", "test5");
    source.addInitParam("searchAttribute1", "searchAttribute1");
    source.addInitParam("subjectVirtualAttribute_2_searchAttribute2", "test6");
    source.addInitParam("searchAttribute2", "searchAttribute2");
    source.addInitParam("subjectVirtualAttribute_3_searchAttribute3", "test7");
    source.addInitParam("searchAttribute3", "searchAttribute3");
    source.addInitParam("subjectVirtualAttribute_4_searchAttribute4", "test8");
    source.addInitParam("searchAttribute4", "searchAttribute4");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
    
    Subject subj = SubjectFinder.findById("test.subject.0", true);
    edu.grantPriv(subj, NamingPrivilege.CREATE);

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(subj, true);
    assertEquals(subj.getName(), member.getName());
    assertEquals(subj.getDescription(), member.getDescription());
    
    assertEquals(subj.getAttributeValue("LFNAME"), member.getSortString0());
    assertEquals(subj.getAttributeValue("LOGINID"), member.getSortString1());
    assertEquals("test2", member.getSortString2());
    assertEquals("test3", member.getSortString3());
    assertEquals("test4", member.getSortString4());
    assertEquals(subj.getName() + "," + subj.getAttributeValue("LFNAME") + "," + subj.getAttributeValue("LOGINID") + "," + subj.getDescription() + "," + subj.getAttributeValue("EMAIL").toLowerCase(), member.getSearchString0());
    assertEquals("test5", member.getSearchString1());
    assertEquals("test6", member.getSearchString2());
    assertEquals("test7", member.getSearchString3());
    assertEquals("test8", member.getSearchString4());
    
    // reset the state
    source.removeInitParam("subjectVirtualAttribute_2_sortAttribute2");
    source.removeInitParam("sortAttribute2");
    source.removeInitParam("subjectVirtualAttribute_3_sortAttribute3");
    source.removeInitParam("sortAttribute3");
    source.removeInitParam("subjectVirtualAttribute_4_sortAttribute4");
    source.removeInitParam("sortAttribute4");
    source.removeInitParam("subjectVirtualAttribute_1_searchAttribute1");
    source.removeInitParam("searchAttribute1");
    source.removeInitParam("subjectVirtualAttribute_2_searchAttribute2");
    source.removeInitParam("searchAttribute2");
    source.removeInitParam("subjectVirtualAttribute_3_searchAttribute3");
    source.removeInitParam("searchAttribute3");
    source.removeInitParam("subjectVirtualAttribute_4_searchAttribute4");
    source.removeInitParam("searchAttribute4");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
  }
  
  /**
   * 
   */
  public void testInternalMembers() {
    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(GrouperConfig.ROOT, true);
    assertEquals(GrouperConfig.ROOT_NAME, member.getName());
    assertEquals(GrouperConfig.ROOT_NAME, member.getDescription());
    
    assertEquals(GrouperConfig.ROOT_NAME, member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(GrouperConfig.ROOT_NAME.toLowerCase() + "," + GrouperConfig.ROOT.toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    
    edu.grantPriv(SubjectFinder.findAllSubject(), NamingPrivilege.CREATE);
    member = GrouperDAOFactory.getFactory().getMember().findBySubject(GrouperConfig.ALL, true);
    assertEquals(GrouperConfig.ALL_NAME, member.getName());
    assertEquals(GrouperConfig.ALL_NAME, member.getDescription());
    
    assertEquals(GrouperConfig.ALL_NAME, member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(GrouperConfig.ALL_NAME.toLowerCase() + "," + GrouperConfig.ALL.toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    
    // verify internal attributes
    assertFalse(SubjectFinder.findAllSubject().getAttributes().containsKey("searchAttribute0"));
    assertTrue(SubjectFinder.findAllSubject().getAttributes(false).containsKey("searchAttribute0"));
  }
  
  /**
   * 
   */
  public void testInternalMembersNonDefaultAttributes() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.sortAttribute1.el", "test1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.sortAttribute2.el", "test2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.sortAttribute3.el", "test3");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.sortAttribute4.el", "test4");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.searchAttribute1.el", "test5");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.searchAttribute2.el", "test6");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.searchAttribute3.el", "test7");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.searchAttribute4.el", "test8");
    InternalSourceAdapter.instance().init();
    ExpirableCache.clearAll();

    edu.grantPriv(SubjectFinder.findById("GrouperAll", true), NamingPrivilege.CREATE);
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(GrouperConfig.ALL, true);

    assertEquals(GrouperConfig.ALL_NAME, member.getName());
    assertEquals(GrouperConfig.ALL_NAME, member.getDescription());
    
    assertEquals(GrouperConfig.ALL_NAME, member.getSortString0());
    assertEquals("test1", member.getSortString1());
    assertEquals("test2", member.getSortString2());
    assertEquals("test3", member.getSortString3());
    assertEquals("test4", member.getSortString4());
    assertEquals(GrouperConfig.ALL_NAME.toLowerCase() + "," + GrouperConfig.ALL.toLowerCase(), member.getSearchString0());
    assertEquals("test5", member.getSearchString1());
    assertEquals("test6", member.getSearchString2());
    assertEquals("test7", member.getSearchString3());
    assertEquals("test8", member.getSearchString4());
    
    // reset the state
    GrouperConfig.retrieveConfig().propertiesOverrideMap().clear();
    InternalSourceAdapter.instance().init();
    ExpirableCache.clearAll();
    GrouperCacheUtils.clearAllCaches();
    edu.revokePriv(SubjectFinder.findById("GrouperAll", true), NamingPrivilege.CREATE);
  }
  
  /**
   * 
   */
  public void testGroupAdd() {
    Group group = edu.addChildGroup("Test", "Test Display");
    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + ",", member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    
    // verify internal attributes
    assertFalse(group.toSubject().getAttributes().containsKey("searchAttribute0"));
    assertTrue(group.toSubject().getAttributes(false).containsKey("searchAttribute0"));
  }
  
  /**
   * 
   */
  public void testGroupAddNonDefault() {
    BaseSourceAdapter source = (BaseSourceAdapter) SubjectFinder.internal_getGSA();
    source.addInitParam("subjectVirtualAttribute_1_sortAttribute1", "test1");
    source.addInitParam("sortAttribute1", "sortAttribute1");
    source.addInitParam("subjectVirtualAttribute_2_sortAttribute2", "test2");
    source.addInitParam("sortAttribute2", "sortAttribute2");
    source.addInitParam("subjectVirtualAttribute_3_sortAttribute3", "test3");
    source.addInitParam("sortAttribute3", "sortAttribute3");
    source.addInitParam("subjectVirtualAttribute_4_sortAttribute4", "test4");
    source.addInitParam("sortAttribute4", "sortAttribute4");
    source.addInitParam("subjectVirtualAttribute_1_searchAttribute1", "test5");
    source.addInitParam("searchAttribute1", "searchAttribute1");
    source.addInitParam("subjectVirtualAttribute_2_searchAttribute2", "test6");
    source.addInitParam("searchAttribute2", "searchAttribute2");
    source.addInitParam("subjectVirtualAttribute_3_searchAttribute3", "test7");
    source.addInitParam("searchAttribute3", "searchAttribute3");
    source.addInitParam("subjectVirtualAttribute_4_searchAttribute4", "test8");
    source.addInitParam("searchAttribute4", "searchAttribute4");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
    Group group = edu.addChildGroup("Test", "Test Display");

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertEquals("test1", member.getSortString1());
    assertEquals("test2", member.getSortString2());
    assertEquals("test3", member.getSortString3());
    assertEquals("test4", member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + ",", member.getSearchString0());
    assertEquals("test5", member.getSearchString1());
    assertEquals("test6", member.getSearchString2());
    assertEquals("test7", member.getSearchString3());
    assertEquals("test8", member.getSearchString4());
    
    // reset the state
    source.removeInitParam("subjectVirtualAttribute_1_sortAttribute1");
    source.removeInitParam("sortAttribute1");
    source.removeInitParam("subjectVirtualAttribute_2_sortAttribute2");
    source.removeInitParam("sortAttribute2");
    source.removeInitParam("subjectVirtualAttribute_3_sortAttribute3");
    source.removeInitParam("sortAttribute3");
    source.removeInitParam("subjectVirtualAttribute_4_sortAttribute4");
    source.removeInitParam("sortAttribute4");
    source.removeInitParam("subjectVirtualAttribute_1_searchAttribute1");
    source.removeInitParam("searchAttribute1");
    source.removeInitParam("subjectVirtualAttribute_2_searchAttribute2");
    source.removeInitParam("searchAttribute2");
    source.removeInitParam("subjectVirtualAttribute_3_searchAttribute3");
    source.removeInitParam("searchAttribute3");
    source.removeInitParam("subjectVirtualAttribute_4_searchAttribute4");
    source.removeInitParam("searchAttribute4");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
  }
  
  /**
   * 
   */
  public void testGroupUpdateDescription() {
    Group group = edu.addChildGroup("Test", "Test Display");
    group.setDescription("Test Description");
    group.store();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertEquals(group.getDescription(), member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + ",", member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
  }
  
  /**
   * 
   */
  public void testGroupUpdateDisplayExtension() {
    Group group = edu.addChildGroup("Test", "Test Display");
    group.setDisplayExtension("Test Display2");
    group.store();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + ",", member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
  }
  
  /**
   * 
   */
  public void testGroupRename() {
    Group group = edu.addChildGroup("Test", "Test Display");
    group.setExtension("Test2");
    group.store();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + "," + group.getAlternateNameDb().toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
  }
  
  /**
   * 
   */
  public void testGroupMove() {
    Group group = edu.addChildGroup("Test", "Test Display");
    group.move(edu2);

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);

    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + "," + group.getAlternateNameDb().toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
  }
  
  /**
   * 
   */
  public void testStemRename() {
    Group group = edu.addChildGroup("Test", "Test Display");
    edu.setExtension("edu3");
    edu.store();
    group = GrouperDAOFactory.getFactory().getGroup().findByUuid(group.getId(), true);

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + "," + group.getAlternateNameDb().toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
  }

  /**
   * 
   */
  public void testStemDisplayExtensionUpdate() {
    Group group = edu.addChildGroup("Test", "Test Display");
    edu.setDisplayExtension("edu3");
    edu.store();
    group = GrouperDAOFactory.getFactory().getGroup().findByUuid(group.getId(), true);

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + ",", member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
  }
  
  /**
   * 
   */
  public void testBasicNonSecureSearchAndSort() {
    Group group = edu.addChildGroup("test", "test");
    Group group2 = edu.addChildGroup("test2", "test2");
    group.addMember(SubjectTestHelper.SUBJ2);
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ3);
    group.addMember(SubjectTestHelper.SUBJ4);
    group.addMember(group2.toSubject());
    
    // this should return all people members
    Member[] members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.getDefaultSearchString(), "Someschool test").toArray(new Member[0]);
    assertEquals(5, members.length);
    assertEquals(SubjectTestHelper.SUBJ0.getName(), members[0].getName());
    assertEquals(SubjectTestHelper.SUBJ1.getName(), members[1].getName());
    assertEquals(SubjectTestHelper.SUBJ2.getName(), members[2].getName());
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[3].getName());
    assertEquals(SubjectTestHelper.SUBJ4.getName(), members[4].getName());
    
    // this should only return one member
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.getDefaultSearchString(), "  Someschool 3  test  ").toArray(new Member[0]);
    assertEquals(1, members.length);
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[0].getName());
    
    // this shouldn't return anybody
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.getDefaultSearchString(), "Someschool 5 test").toArray(new Member[0]);
    assertEquals(0, members.length);
    
    // this should return the people and the group
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.getDefaultSearchString(), "Test").toArray(new Member[0]);
    assertEquals(6, members.length);
    assertEquals(group2.getName(), members[5].getName());
    assertEquals(SubjectTestHelper.SUBJ0.getName(), members[0].getName());
    assertEquals(SubjectTestHelper.SUBJ1.getName(), members[1].getName());
    assertEquals(SubjectTestHelper.SUBJ2.getName(), members[2].getName());
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[3].getName());
    assertEquals(SubjectTestHelper.SUBJ4.getName(), members[4].getName());
    
    // this should return the people and the group too
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_1, SearchStringEnum.getDefaultSearchString(), "Test").toArray(new Member[0]);
    assertEquals(6, members.length);
    
    // this should return the people and the group too
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_1, null).toArray(new Member[0]);
    assertEquals(6, members.length);
    assertEquals(group2.getName(), members[5].getName());
    assertEquals(SubjectTestHelper.SUBJ0.getName(), members[0].getName());
    assertEquals(SubjectTestHelper.SUBJ1.getName(), members[1].getName());
    assertEquals(SubjectTestHelper.SUBJ2.getName(), members[2].getName());
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[3].getName());
    assertEquals(SubjectTestHelper.SUBJ4.getName(), members[4].getName());
    
    // use query sort this time - asc
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.sortAsc("sortString0");
    
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, queryOptions, true, 
        null, SearchStringEnum.getDefaultSearchString(), "Test").toArray(new Member[0]);
    assertEquals(6, members.length);
    assertEquals(group2.getName(), members[5].getName());
    assertEquals(SubjectTestHelper.SUBJ0.getName(), members[0].getName());
    assertEquals(SubjectTestHelper.SUBJ1.getName(), members[1].getName());
    assertEquals(SubjectTestHelper.SUBJ2.getName(), members[2].getName());
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[3].getName());
    assertEquals(SubjectTestHelper.SUBJ4.getName(), members[4].getName());
    
    // use query sort this time - desc
    queryOptions = new QueryOptions();
    queryOptions.sortDesc("sortString0");
    
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, queryOptions, true, 
        null, SearchStringEnum.getDefaultSearchString(), "Test").toArray(new Member[0]);
    assertEquals(6, members.length);
    assertEquals(group2.getName(), members[0].getName());
    assertEquals(SubjectTestHelper.SUBJ0.getName(), members[5].getName());
    assertEquals(SubjectTestHelper.SUBJ1.getName(), members[4].getName());
    assertEquals(SubjectTestHelper.SUBJ2.getName(), members[3].getName());
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[2].getName());
    assertEquals(SubjectTestHelper.SUBJ4.getName(), members[1].getName());
  }
  
  /**
   * 
   */
  public void testGetDefaultSearchIndex() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("member.search.defaultIndexOrder", "3,1,4,0,2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string1.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string2.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string3.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string4.wheelOnly", "true");

    Group allowGroup1 = edu.addChildGroup("allowGroup1", "allowGroup1");
    Group allowGroup2 = edu.addChildGroup("allowGroup2", "allowGroup2");
    allowGroup1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    allowGroup2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    
    assertEquals(SearchStringEnum.SEARCH_STRING_3, SearchStringEnum.getDefaultSearchString());
    assertTrue(SearchStringEnum.SEARCH_STRING_0.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_1.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_2.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_3.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_4.hasAccess());
    
    
    // now verify that subj1 doesn't have a default index
    GrouperSession s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    assertNull(SearchStringEnum.getDefaultSearchString());
    assertFalse(SearchStringEnum.SEARCH_STRING_0.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_1.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_2.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_3.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_4.hasAccess());
    
    // now verify that subj1 can use index 2
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string2.wheelOnly", "false");
    assertEquals(SearchStringEnum.SEARCH_STRING_2, SearchStringEnum.getDefaultSearchString());

    // now verify that subj1 can use index 0
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.wheelOnly", "false");
    assertEquals(SearchStringEnum.SEARCH_STRING_0, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 4
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string4.wheelOnly", "false");
    assertEquals(SearchStringEnum.SEARCH_STRING_4, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 1
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string1.wheelOnly", "false");
    assertEquals(SearchStringEnum.SEARCH_STRING_1, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 3
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string3.wheelOnly", "false");
    assertEquals(SearchStringEnum.SEARCH_STRING_3, SearchStringEnum.getDefaultSearchString());
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string1.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string2.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string3.allowOnlyGroup", "edu:allowGroup2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string4.allowOnlyGroup", "edu:allowGroup2");

    // again, subj1 should have no access
    assertNull(SearchStringEnum.getDefaultSearchString());
    assertFalse(SearchStringEnum.SEARCH_STRING_0.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_1.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_2.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_3.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_4.hasAccess());
    
    // add subj1 to one of the allow groups and test again
    allowGroup1.addMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(SearchStringEnum.SEARCH_STRING_1, SearchStringEnum.getDefaultSearchString());
    assertTrue(SearchStringEnum.SEARCH_STRING_0.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_1.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_2.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_3.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_4.hasAccess());
    
    // add subj1 to the other allow group and test again
    allowGroup2.addMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(SearchStringEnum.SEARCH_STRING_3, SearchStringEnum.getDefaultSearchString());
    assertTrue(SearchStringEnum.SEARCH_STRING_0.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_1.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_2.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_3.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_4.hasAccess());
    
    // remove subj1 from both groups and verify access as each restriction is lifted.
    allowGroup1.deleteMember(SubjectTestHelper.SUBJ0);
    allowGroup2.deleteMember(SubjectTestHelper.SUBJ0);
    
    // now verify that subj1 can use index 2
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string2.allowOnlyGroup");
    assertEquals(SearchStringEnum.SEARCH_STRING_2, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 0
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string0.allowOnlyGroup");
    assertEquals(SearchStringEnum.SEARCH_STRING_0, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 4
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string4.allowOnlyGroup");
    assertEquals(SearchStringEnum.SEARCH_STRING_4, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 1
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string1.allowOnlyGroup");
    assertEquals(SearchStringEnum.SEARCH_STRING_1, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 3
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string3.allowOnlyGroup");
    assertEquals(SearchStringEnum.SEARCH_STRING_3, SearchStringEnum.getDefaultSearchString());
    
    s.stop();
  }
  
  /**
   * 
   */
  public void testGetDefaultSortIndex() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("member.sort.defaultIndexOrder", "3,1,4,0,2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string0.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string1.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string2.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string3.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string4.wheelOnly", "true");

    Group allowGroup1 = edu.addChildGroup("allowGroup1", "allowGroup1");
    Group allowGroup2 = edu.addChildGroup("allowGroup2", "allowGroup2");
    allowGroup1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    allowGroup2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    
    assertEquals(SortStringEnum.SORT_STRING_3, SortStringEnum.getDefaultSortString());
    assertTrue(SortStringEnum.SORT_STRING_0.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_1.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_2.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_3.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_4.hasAccess());
    
    
    // now verify that subj1 doesn't have a default index
    GrouperSession s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    assertNull(SortStringEnum.getDefaultSortString());
    assertFalse(SortStringEnum.SORT_STRING_0.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_1.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_2.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_3.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_4.hasAccess());
    
    // now verify that subj1 can use index 2
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string2.wheelOnly", "false");
    assertEquals(SortStringEnum.SORT_STRING_2, SortStringEnum.getDefaultSortString());

    // now verify that subj1 can use index 0
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string0.wheelOnly", "false");
    assertEquals(SortStringEnum.SORT_STRING_0, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 4
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string4.wheelOnly", "false");
    assertEquals(SortStringEnum.SORT_STRING_4, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 1
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string1.wheelOnly", "false");
    assertEquals(SortStringEnum.SORT_STRING_1, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 3
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string3.wheelOnly", "false");
    assertEquals(SortStringEnum.SORT_STRING_3, SortStringEnum.getDefaultSortString());
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string0.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string1.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string2.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string3.allowOnlyGroup", "edu:allowGroup2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string4.allowOnlyGroup", "edu:allowGroup2");

    // again, subj1 should have no access
    assertNull(SortStringEnum.getDefaultSortString());
    assertFalse(SortStringEnum.SORT_STRING_0.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_1.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_2.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_3.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_4.hasAccess());
    
    // add subj1 to one of the allow groups and test again
    allowGroup1.addMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(SortStringEnum.SORT_STRING_1, SortStringEnum.getDefaultSortString());
    assertTrue(SortStringEnum.SORT_STRING_0.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_1.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_2.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_3.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_4.hasAccess());
    
    // add subj1 to the other allow group and test again
    allowGroup2.addMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(SortStringEnum.SORT_STRING_3, SortStringEnum.getDefaultSortString());
    assertTrue(SortStringEnum.SORT_STRING_0.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_1.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_2.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_3.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_4.hasAccess());
    
    // remove subj1 from both groups and verify access as each restriction is lifted.
    allowGroup1.deleteMember(SubjectTestHelper.SUBJ0);
    allowGroup2.deleteMember(SubjectTestHelper.SUBJ0);
    
    // now verify that subj1 can use index 2
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.sort.string2.allowOnlyGroup");
    assertEquals(SortStringEnum.SORT_STRING_2, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 0
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.sort.string0.allowOnlyGroup");
    assertEquals(SortStringEnum.SORT_STRING_0, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 4
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.sort.string4.allowOnlyGroup");
    assertEquals(SortStringEnum.SORT_STRING_4, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 1
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.sort.string1.allowOnlyGroup");
    assertEquals(SortStringEnum.SORT_STRING_1, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 3
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.sort.string3.allowOnlyGroup");
    assertEquals(SortStringEnum.SORT_STRING_3, SortStringEnum.getDefaultSortString());
    
    s.stop();
  }
  
  /**
   * 
   */
  public void testSearchSecurity() {
    
    Group group = edu.addChildGroup("test", "test");
    Group group2 = edu.addChildGroup("test2", "test2");
    group.addMember(SubjectTestHelper.SUBJ2);
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ3);
    group.addMember(SubjectTestHelper.SUBJ4);
    group.addMember(group2.toSubject());
    
    GrouperSession s = GrouperSession.start(SubjectTestHelper.SUBJ0);

    // these should all work
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_0, "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_1, "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_2, "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_3, "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_4, "Someschool test");
    
    // now set restictions
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string1.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string2.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string3.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string4.wheelOnly", "true");
    
    // and subj1 cannot use any of the search strings now
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_0, "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_1, "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_2, "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_3, "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_4, "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    s.stop();
  }
  
  /**
   * 
   */
  public void testSortSecurity() {
    
    Group group = edu.addChildGroup("test", "test");
    Group group2 = edu.addChildGroup("test2", "test2");
    group.addMember(SubjectTestHelper.SUBJ2);
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ3);
    group.addMember(SubjectTestHelper.SUBJ4);
    group.addMember(group2.toSubject());
    
    GrouperSession s = GrouperSession.start(SubjectTestHelper.SUBJ0);

    // these should all work
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_0, SearchStringEnum.getDefaultSearchString(), "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_1, SearchStringEnum.getDefaultSearchString(), "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_2, SearchStringEnum.getDefaultSearchString(), "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_3, SearchStringEnum.getDefaultSearchString(), "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_4, SearchStringEnum.getDefaultSearchString(), "Someschool test");
    
    
    // now set restictions
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string0.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string1.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string2.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string3.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string4.wheelOnly", "true");
    
    // and subj1 cannot use any of the sort strings now
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.SORT_STRING_0, SearchStringEnum.getDefaultSearchString(), "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.SORT_STRING_1, SearchStringEnum.getDefaultSearchString(), "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.SORT_STRING_2, SearchStringEnum.getDefaultSearchString(), "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.SORT_STRING_3, SearchStringEnum.getDefaultSearchString(), "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.SORT_STRING_4, SearchStringEnum.getDefaultSearchString(), "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    s.stop();
  }
  
  /**
   * 
   */
  public void testNonDefaultSearchAndSort() {
    
    // we are going to sort by EMAIL and search by LOGINID
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("sortAttribute4", "EMAIL");
    source.addInitParam("searchAttribute4", "LOGINID");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
    
    // update subject attributes
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'def' where subjectid='test.subject.0' and name='email'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'ghi' where subjectid='test.subject.1' and name='email'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'abc' where subjectid='test.subject.2' and name='email'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'mno' where subjectid='test.subject.3' and name='email'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'jkl' where subjectid='test.subject.4' and name='email'");

    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'First,Second,Third' where subjectid='test.subject.0' and name='loginid'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'First,Third' where subjectid='test.subject.1' and name='loginid'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'Third,Forth' where subjectid='test.subject.2' and name='loginid'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'First,Second,Third,Forth' where subjectid='test.subject.3' and name='loginid'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'Second,Forth' where subjectid='test.subject.4' and name='loginid'");

    Subject subj0 = SubjectFinder.findById("test.subject.0", true);
    Subject subj1 = SubjectFinder.findById("test.subject.1", true);
    Subject subj2 = SubjectFinder.findById("test.subject.2", true);
    Subject subj3 = SubjectFinder.findById("test.subject.3", true);
    Subject subj4 = SubjectFinder.findById("test.subject.4", true);
    
    // add a few subjects to a group
    Group group = edu.addChildGroup("test", "test");
    group.addMember(subj0);
    group.addMember(subj1);
    group.addMember(subj2);
    group.addMember(subj3);
    group.addMember(subj4);
    
    // this should return all members since there's no searching
    Member[] members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_4, null, null).toArray(new Member[0]);
    assertEquals(5, members.length);
    assertEquals(subj2.getName(), members[0].getName());
    assertEquals(subj0.getName(), members[1].getName());
    assertEquals(subj1.getName(), members[2].getName());
    assertEquals(subj4.getName(), members[3].getName());
    assertEquals(subj3.getName(), members[4].getName());
    
    // this should return subj2, subj3, and subj4 since we're searching for "FORTH"
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_4, SearchStringEnum.SEARCH_STRING_4, "FORTH").toArray(new Member[0]);
    assertEquals(3, members.length);
    assertEquals(subj2.getName(), members[0].getName());
    assertEquals(subj4.getName(), members[1].getName());
    assertEquals(subj3.getName(), members[2].getName());
    
    // this should return subj0 and subj3 since we're searching for "THIRD SECOND"
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_4, SearchStringEnum.SEARCH_STRING_4, "THIRD SECOND").toArray(new Member[0]);
    assertEquals(2, members.length);
    assertEquals(subj0.getName(), members[0].getName());
    assertEquals(subj3.getName(), members[1].getName());
    
    // reset the state
    source.removeInitParam("sortAttribute4");
    source.removeInitParam("searchAttribute4");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
  }
}
