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
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.subj;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectTest;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectTooManyResults;
import edu.internet2.middleware.subject.provider.JDBCSourceAdapter;
import edu.internet2.middleware.subject.provider.JDBCSourceAdapter2;
import edu.internet2.middleware.subject.provider.JNDISourceAdapter;
import edu.internet2.middleware.subject.provider.JNDISourceAdapterLegacy;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * Test {@link SubjectFinder.findByIdentifier()} with {@link GrouperSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSubjectFinder.java,v 1.5 2009-09-02 05:57:26 mchyzer Exp $
 */
public class TestSubjectFinder extends GrouperTest {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestSubjectFinder.class);


  // Private Class Variables
  private GrouperSession  s;
  private Stem            edu, root;
  private Group           i2;

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(TestSubjectFinder.class);
    TestRunner.run(new TestSubjectFinder("testFindByIdentifiers"));
  }
  
  /**
   * 
   * @param name
   */
  public TestSubjectFinder(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    super.setUp();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    ExternalSubjectTest.setupHelper();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  
  /**
   * 
   */
  public void testSubjectDecorator() {
    
    SubjectFinder.internalClearSubjectCustomizerCache();
    
    
    try {

      //make a group that test1 can view, but test2 cannot
      Group group = new GroupSave(this.s).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      
      GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

      Subject groupSubject = SubjectFinder.findById(group.getId(), true);
      
      assertNotNull(groupSubject);
      
      
      
      //lets set the subject customizer to a test one
      
    } finally {
      SubjectFinder.internalClearSubjectCustomizerCache();
    }
  }

  /**
   * test find multiple in jdbc and jdbc2
   */
  public void testFindByIds() {
    
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
        
    List<Group> groups = new ArrayList<Group>();
    
    for (int i=0;i<10;i++) {
      
      Group group = new GroupSave(grouperSession)
        .assignName("test:testGroup" + i)
        .assignCreateParentStemsIfNotExist(true).save();
      groups.add(group);
    }
    
    groups.get(0).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    groups.get(1).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    groups.get(2).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    groups.get(0).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    groups.get(1).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    
    Set<String> ids = GrouperUtil.toSet(
        SubjectTestHelper.SUBJ0_ID, 
        SubjectTestHelper.SUBJ1_ID,
        SubjectTestHelper.SUBJ2_ID, 
        SubjectTestHelper.SUBJ3_ID, 
        SubjectTestHelper.SUBJ4_ID, 
        SubjectTestHelper.SUBJ5_ID, 
        SubjectTestHelper.SUBJ6_ID, 
        SubjectTestHelper.SUBJ7_ID, 
        SubjectTestHelper.SUBJ8_ID, 
        SubjectTestHelper.SUBJ9_ID);
    
    ids.add(SubjectFinder.findAllSubject().getId());
    ids.add(SubjectFinder.findRootSubject().getId());
    
    List<ExternalSubject> externalSubjects = new ArrayList<ExternalSubject>();
    
    for (int i=0;i<8;i++) {
      ExternalSubject externalSubject = new ExternalSubject();
      externalSubject.setEmail("a" + i + "@b.c");
      externalSubject.setIdentifier("a" + i + "@id.b.c");
      externalSubject.setInstitution("My Institution" + i);
      externalSubject.setName("My Name" + i);
      externalSubject.store();
      externalSubjects.add(externalSubject);
      ids.add(externalSubject.getUuid());
    }

    Set<String> idsExist = new HashSet<String>(ids);
    
    for (Group group : groups) {
      ids.add(group.getId());
    }
    
    idsExist.add(groups.get(0).getId());
    idsExist.add(groups.get(1).getId());
    idsExist.add(groups.get(2).getId());

    //one it wont find, thats ok
    ids.add("abc");
    
    EhcacheController.ehcacheController().flushCache();
    //clear cache so we can see it work
    SubjectFinder.flushCache();
    
    long initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    Map<String, Subject> subjectMap = SubjectFinder.findByIds(ids);
    
    assertEquals(ids.size()-1, subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_ID).getName());
    
    long numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    assertTrue("queries: " + numberOfQueries, numberOfQueries < 10);
    assertTrue("queries: " + numberOfQueries, numberOfQueries > 0);
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################ TRY AS SUBJ0
    
    long start = System.nanoTime();
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    EhcacheController.ehcacheController().flushCache();
    //clear cache so we can see it work
    SubjectFinder.flushCache();
    

    initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    subjectMap = SubjectFinder.findByIds(ids);
    
    assertEquals((ids.size()-1) - 7, subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_ID).getName());
    
    numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    assertTrue("queries: " + numberOfQueries, numberOfQueries < 10);
    assertTrue("queries: " + numberOfQueries, numberOfQueries > 0);
    
    System.out.println("Took: " + ((System.nanoTime() - start) / 1000000) + "ms");
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################ DO IT AGAIN IN CACHE
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    subjectMap = SubjectFinder.findByIds(idsExist);
    
    assertEquals(idsExist.size(), subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_ID).getName());
    
    numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    assertEquals("queries: " + numberOfQueries, 0, numberOfQueries);
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################ DO IT AGAIN BY SOURCE IN CACHE

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    subjectMap = SubjectFinder.findByIds(idsExist, "jdbc");

    assertEquals(10, subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_ID).getName());
    
    numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    //one for subjects in other sources
    assertEquals("queries: " + numberOfQueries, 1, numberOfQueries);
    
    GrouperSession.stopQuietly(grouperSession);
    
  }
  

  /**
   * 
   */
  public void testSubjectCachePrivileges() {
    
    //make a group that test1 can view, but test2 cannot
    Group group = new GroupSave(this.s).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    
    GrouperSession grouperSession = null;
    Subject groupSubject = null;
    Set<Subject> groupSubjects = null;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    groupSubject = SubjectFinder.findById(group.getId(), true);
    
    assertNotNull(groupSubject);
    
    groupSubject = SubjectFinder.findByIdentifier(group.getName(), true);
    
    assertNotNull(groupSubject);
    
    groupSubjects = SubjectFinder.findAll(group.getName());
    
    assertTrue(GrouperUtil.length(groupSubjects) > 0);

    groupSubjects = SubjectFinder.findPage(group.getName()).getResults();
    
    assertTrue(GrouperUtil.length(groupSubjects) > 0);

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    groupSubject = SubjectFinder.findById(group.getId(), false);
    
    assertNull(groupSubject);
    
    groupSubject = SubjectFinder.findByIdentifier(group.getName(), false);
    
    assertNull(groupSubject);
    
    groupSubjects = SubjectFinder.findAll(group.getName());
    
    assertTrue(GrouperUtil.length(groupSubjects) == 0);

    groupSubjects = SubjectFinder.findPage(group.getName()).getResults();
    
    assertTrue(GrouperUtil.length(groupSubjects) == 0);

    GrouperSession.stopQuietly(grouperSession);
    //lets set the subject customizer to a test one
  }

  /**
   * 
   */
  public void testSubjectCachePage() {
    
    //make a group that test1 can view, but test2 cannot
    Group group = new GroupSave(this.s).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    Group group1 = new GroupSave(this.s).assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave(this.s).assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    Group group3 = new GroupSave(this.s).assignName("test:testGroup3").assignCreateParentStemsIfNotExist(true).save();
    Group group4 = new GroupSave(this.s).assignName("test:testGroup4").assignCreateParentStemsIfNotExist(true).save();
    Group group5 = new GroupSave(this.s).assignName("test:testGroup5").assignCreateParentStemsIfNotExist(true).save();
    Group group6 = new GroupSave(this.s).assignName("test:testGroup6").assignCreateParentStemsIfNotExist(true).save();
    Group group7 = new GroupSave(this.s).assignName("test:testGroup7").assignCreateParentStemsIfNotExist(true).save();
    Group group8 = new GroupSave(this.s).assignName("test:testGroup8").assignCreateParentStemsIfNotExist(true).save();
    Group group9 = new GroupSave(this.s).assignName("test:testGroup9").assignCreateParentStemsIfNotExist(true).save();
    Group group10 = new GroupSave(this.s).assignName("test:testGroup10").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Set<Subject> groupSubjects = SubjectFinder.findPage("test:testGroup", 
        GrouperUtil.toSet(SubjectFinder.internal_getGSA(), SourceManager.getInstance().getSource("jdbc"))).getResults();
    
    assertEquals(11, GrouperUtil.length(groupSubjects));

    groupSubjects = SubjectFinder.findPage("test:testGroupwerwerwerwer", 
        GrouperUtil.toSet(SubjectFinder.internal_getGSA(), SourceManager.getInstance().getSource("jdbc"))).getResults();

    assertEquals(0, GrouperUtil.length(groupSubjects));

    groupSubjects = SubjectFinder.findPage("test:testGroup1", 
        GrouperUtil.toSet(SubjectFinder.internal_getGSA(), SourceManager.getInstance().getSource("jdbc"))).getResults();
    
    assertEquals(2, GrouperUtil.length(groupSubjects));
    
    GrouperSession.stopQuietly(grouperSession);
    //lets set the subject customizer to a test one
  }

  /**
   * 
   */
  public void testGroupQueries() {
    
    for (int i=0;i<200;i++) {
      new GroupSave(s).assignName("test:test" + i).assignCreateParentStemsIfNotExist(true).save();
    }
    
    long queryCount = GrouperContext.totalQueryCount;
    
    SubjectFinder.findPage("te");
    
    int diffQueryCount = (int)(GrouperContext.totalQueryCount - queryCount);
    
    //shouldnt be more than 20 queries to do this... 
    assertTrue("diff query count: " + diffQueryCount, diffQueryCount < 20);
    
  }
  
  /**
   * 
   */
  public void testFindByIdentifierGoodId() {
    LOG.info("testFindByIdentifierGoodId");
    SubjectTestHelper.getSubjectByIdentifier(i2.getName());
    Assert.assertTrue("found subject", true);
  } // public void testFindByIdentifierGoodId()

  /**
   * 
   */
  public void testFindByIdentifierGoodIdGoodType() {
    LOG.info("testFindByIdentifierGoodIdGoodType");
    Subject subj = SubjectTestHelper.getSubjectByIdentifierType(i2.getName(), "group");
    Assert.assertTrue("found subject", true);
    Map<String, Set<String>> attrs = subj.getAttributes();
    Assert.assertEquals("12 attributes", 12, attrs.size());
    // createSubjectId
    String attr = "createSubjectId";
    String val  = "GrouperSystem";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    // createSubjectType
    attr = "createSubjectType";
    val  = "application";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    // createTime
    attr = "createTime";
    Assert.assertNotNull(
      "attr => " + attr, subj.getAttributeValue(attr)
    );
    Assert.assertNotNull(
      "attrs => " + attr, attrs.get(attr)
    );
    // displayExtension
    attr = "displayExtension";
    val  = "internet2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    // displayName
    attr = "displayName";
    val  = "educational:internet2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    // extension
    attr = "extension";
    val  = "i2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    // name
    attr = "name";
    val  = "edu:i2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
  } // public void testFindByIdentifierGoodIdGoodType()

  public void testFindByIdGoodId() {
    LOG.info("testFindByIdGoodId");
    SubjectTestHelper.getSubjectById(i2.getUuid());
    Assert.assertTrue("found subject", true);
  } // public void testFindByIdGoodId()

  public void testFindByIdGoodIdGoodType() {
    LOG.info("testFindByIdGoodIdGoodType");
    LOG.debug("testFindByIdGoodIdGoodType.0");
    Subject subj = SubjectTestHelper.getSubjectByIdType(i2.getUuid(), "group");
    LOG.debug("testFindByIdGoodIdGoodType.1");
    Assert.assertTrue("found subject", true);
    LOG.debug("testFindByIdGoodIdGoodType.2");
    Map<String, Set<String>> attrs = subj.getAttributes();
    LOG.debug("testFindByIdGoodIdGoodType.3");
    Assert.assertEquals("12 attributes", 12, attrs.size());
    LOG.debug("testFindByIdGoodIdGoodType.4");
    // createSubjectId
    String attr = "createSubjectId";
    String val  = "GrouperSystem";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.5");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.6");
    // createSubjectType
    attr = "createSubjectType";
    val  = "application";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.7");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.8");
    // createTime
    attr = "createTime";
    Assert.assertNotNull(
      "attr => " + attr, subj.getAttributeValue(attr)
    );
    LOG.debug("testFindByIdGoodIdGoodType.9");
    Assert.assertNotNull(
      "attrs => " + attr, attrs.get(attr).iterator().next()
    );
    LOG.debug("testFindByIdGoodIdGoodType.10");
    // displayExtension
    attr = "displayExtension";
    val  = "internet2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.11");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.12");
    // displayName
    attr = "displayName";
    val  = "educational:internet2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.13");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.14");
    // extension
    attr = "extension";
    val  = "i2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.15");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.16");
    // name
    attr = "name";
    val  = "edu:i2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.17");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).iterator().next().equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.18");
  } // public void testFindByIdGoodIdGoodType()

  // Tests
  
  public void testFindByIdBadId() {
    SubjectTestHelper.getSubjectByBadId("i do not exist");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdBadId()

  // Tests
  
  public void testFindByIdentifierBadId() {
    SubjectTestHelper.getSubjectByBadId("i do not exist");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdentifierBadId()

  public void testFindByIdentifierGoodIdGoodTypeRoot() {
    SubjectTestHelper.getSubjectByIdentifierType(
      SubjectTestHelper.SUBJ_ROOT, "application"
    );
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdentifierGoodIdGoodTypeRoot()

  public void testFindByIdentifierGoodIdRoot() {
    SubjectTestHelper.getSubjectByIdentifier(SubjectTestHelper.SUBJ_ROOT);
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdentifierGoodIdRoot()

  public void testFindByIdGoodIdAll() {
    SubjectTestHelper.getSubjectById(SubjectTestHelper.SUBJ_ALL);
    Assert.assertTrue("found all subject", true);
  } // public void testFindByIdGoodIdAll()

  public void testFindByIdGoodIdGoodTypeAll() {
    SubjectTestHelper.getSubjectByIdType(
      SubjectTestHelper.SUBJ_ALL, "application"
    );
    Assert.assertTrue("found all subject", true);
  } // public void testFindByIdGoodIdGoodTypeAll()

  public void testFindByIdGoodIdGoodTypeRoot() {
    SubjectTestHelper.getSubjectByIdType(
      SubjectTestHelper.SUBJ_ROOT, "application"
    );
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdGoodIdGoodTypeRoot()

  public void testFindByIdGoodIdRoot() {
    SubjectTestHelper.getSubjectById(SubjectTestHelper.SUBJ_ROOT);
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdGoodIdRoot()

  // Tests
  
  public void testSearchBad() {
    LOG.info("testSearchBad");
    Set subjs = SubjectFinder.findAll("i do not exist");
    Assert.assertTrue("subjs == 0", subjs.size() == 0);
  } // public void testSearchBad()

  // Tests
  
  public void testSearchBadSearch() {
    LOG.info("testSearchBadSearch");
    Set subjs = SubjectFinder.findAll("i do not exist");
    Assert.assertTrue("subjs == 0", subjs.size() == 0);
  } // public void testSearchBadSearch()

  public void testSearchGood() {
    LOG.info("testSearchGood");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    StemHelper.addChildGroup(edu, "i2", "internet2");
    StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    StemHelper.addChildGroup(com, "dc", "devclue");
    Set             subjs = SubjectFinder.findAll("educational");
    Assert.assertTrue("subjs == 2", subjs.size() == 2);
  } // public void testSearchGood()

  public void testSearchPageMax() {

    StemHelper.addChildGroup(edu, "uofc2", "uchicago2");
    StemHelper.addChildGroup(edu, "uofc3", "uchicago3");
    StemHelper.addChildGroup(edu, "uofc4", "uchicago4");
    StemHelper.addChildGroup(edu, "uofc5", "uchicago5");
    StemHelper.addChildGroup(edu, "uofc6", "uchicago6");
    StemHelper.addChildGroup(edu, "uofc7", "uchicago7");

    {
      Set<Subject> subjects = SubjectFinder.findAll("test.subject");
      
      assertTrue(subjects.size() > 5);
      
      SubjectFinder.flushCache();
      
      //lets change the jdbc source
      Source jdbc = SubjectFinder.getSource("jdbc");
      
      //assign with reflection
      Integer maxResults = (Integer)GrouperUtil.fieldValue(jdbc, "maxResults");
      GrouperUtil.assignField(jdbc, "maxResults", 2);
      
      try {
        try {
          subjects = SubjectFinder.findAll("test.subject", "jdbc");
          fail("Should be past max results");
        } catch (SubjectTooManyResults stmr) {
          //good
        }
        try {
          subjects = SubjectFinder.findAll("test.subject");
          fail("Should be past max results");
        } catch (SubjectTooManyResults stmr) {
          //good
        }
      } finally {
        GrouperUtil.assignField(jdbc, "maxResults", maxResults);
      }
  
      Integer maxPage = (Integer)GrouperUtil.fieldValue(jdbc, "maxPage");
      GrouperUtil.assignField(jdbc, "maxPage", 2);
      
      SearchPageResult searchPageResult = null;
        
      try {
        searchPageResult = SubjectFinder.findPage("test.subject", "jdbc");
        
        assertTrue(searchPageResult.isTooManyResults());
        assertEquals(2, searchPageResult.getResults().size());
        
        searchPageResult = SubjectFinder.findPage("test.subject");
  
        assertTrue(searchPageResult.isTooManyResults());
        assertEquals(2, searchPageResult.getResults().size());
  
        //try one that fits
        searchPageResult = SubjectFinder.findPage("test.subject.0", "jdbc");
        
        assertFalse(searchPageResult.isTooManyResults());
        assertEquals(1, searchPageResult.getResults().size());
        
        searchPageResult = SubjectFinder.findPage("test.subject.0");
  
        assertFalse(searchPageResult.isTooManyResults());
        assertEquals(1, searchPageResult.getResults().size());
        
        
      } finally {
        GrouperUtil.assignField(jdbc, "maxPage", maxPage);
      }
    }
    
    {
      
      Set<Subject> subjects = SubjectFinder.findAll("chic");
      
      assertTrue(subjects.size() > 5);
      
      SubjectFinder.flushCache();
      
      //lets change the jdbc source
      Source groupSource = SubjectFinder.getSource("g:gsa");
      
      //assign with reflection
      Integer maxResults = (Integer)GrouperUtil.fieldValue(groupSource, "maxResults");
      GrouperUtil.assignField(groupSource, "maxResults", 2);
      
      try {
        try {
          subjects = SubjectFinder.findAll("chic", "g:gsa");
          fail("Should be past max results");
        } catch (SubjectTooManyResults stmr) {
          //good
        }
        try {
          subjects = SubjectFinder.findAll("chic");
          fail("Should be past max results");
        } catch (SubjectTooManyResults stmr) {
          //good
        }
      } finally {
        GrouperUtil.assignField(groupSource, "maxResults", maxResults);
      }
  
      Integer maxPage = (Integer)GrouperUtil.fieldValue(groupSource, "maxPage");
      GrouperUtil.assignField(groupSource, "maxPage", 2);
      
      SearchPageResult searchPageResult = null;
        
      try {
        searchPageResult = SubjectFinder.findPage("chic", "g:gsa");
        
        assertTrue(searchPageResult.isTooManyResults());
        assertEquals(2, searchPageResult.getResults().size());
        
        searchPageResult = SubjectFinder.findPage("chic");
  
        assertTrue(searchPageResult.isTooManyResults());
        assertEquals(2, searchPageResult.getResults().size());
  
        //try one that fits
        searchPageResult = SubjectFinder.findPage("chicago6", "g:gsa");
        
        assertFalse(searchPageResult.isTooManyResults());
        assertEquals(1, searchPageResult.getResults().size());
        
        searchPageResult = SubjectFinder.findPage("chicago6");
  
        assertFalse(searchPageResult.isTooManyResults());
        assertEquals(1, searchPageResult.getResults().size());
        
        
      } finally {
        GrouperUtil.assignField(groupSource, "maxPage", maxPage);
      }

    }
    

    {
      for (int i=0;i<8;i++) {
        ExternalSubject externalSubject = new ExternalSubject();
        externalSubject.setEmail("a" + i + "@b.c");
        externalSubject.setIdentifier("a" + i + "@id.b.c");
        externalSubject.setInstitution("My Institution" + i);
        externalSubject.setName("My Name" + i);
        externalSubject.store();

      }
      
      
      //this tests the jdbc2 source
      Set<Subject> subjects = SubjectFinder.findAll("id.b.c");
      
      assertTrue(subjects.size() > 5);
      
      SubjectFinder.flushCache();
      
      //lets change the jdbc source
      Source externalSource = SubjectFinder.getSource(ExternalSubject.sourceId());
      
      //assign with reflection
      Integer maxResults = (Integer)GrouperUtil.fieldValue(externalSource, "maxResults");
      GrouperUtil.assignField(externalSource, "maxResults", 2);
      
      try {
        try {
          subjects = SubjectFinder.findAll("id.b.c", ExternalSubject.sourceId());
          fail("Should be past max results");
        } catch (SubjectTooManyResults stmr) {
          //good
        }
        try {
          subjects = SubjectFinder.findAll("id.b.c");
          fail("Should be past max results");
        } catch (SubjectTooManyResults stmr) {
          //good
        }
      } finally {
        GrouperUtil.assignField(externalSource, "maxResults", maxResults);
      }
  
      Integer maxPage = (Integer)GrouperUtil.fieldValue(externalSource, "maxPage");
      GrouperUtil.assignField(externalSource, "maxPage", 2);
      
      SearchPageResult searchPageResult = null;
        
      try {
        searchPageResult = SubjectFinder.findPage("id.b.c", ExternalSubject.sourceId());
        
        assertEquals(2, searchPageResult.getResults().size());
        assertTrue(searchPageResult.isTooManyResults());
        
        searchPageResult = SubjectFinder.findPage("id.b.c");
  
        assertTrue(searchPageResult.isTooManyResults());
        assertEquals(2, searchPageResult.getResults().size());
  
        //try one that fits
        searchPageResult = SubjectFinder.findPage("a2@id", ExternalSubject.sourceId());
        
        assertFalse(searchPageResult.isTooManyResults());
        assertEquals(1, searchPageResult.getResults().size());
        
        searchPageResult = SubjectFinder.findPage("a2@id");
  
        assertFalse(searchPageResult.isTooManyResults());
        assertEquals(1, searchPageResult.getResults().size());
        
        
      } finally {
        GrouperUtil.assignField(externalSource, "maxPage", maxPage);
      }

    }
    
    
  
  }
  
  public void testSearchGoodAllId() {
    LOG.info("testSearchGoodAllId");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJA.getId());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodAllId()

  public void testSearchGoodAllIdentifier() {
    LOG.info("testSearchGoodAllIdentifier");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJA.getName());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodAllIdentifier()

  public void testSearchGoodRootId() {
    LOG.info("testSearchGoodRootId");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJR.getId());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodRootId()

  public void testSearchGoodRootIdentifier() {
    LOG.info("testSearchGoodRootIdentifier");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJR.getName());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodRootIdentifier()

  // Tests
  
  public void testFinderBadSubject() {
    SubjectTestHelper.getSubjectByBadId("i do not exist");
    Assert.assertTrue("failed to get bad subject", true);
  } // public void testFinderBadSubject()

  public void testFinderBadSubjectByIdentifier() {
    String id = "i do not exist";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, true);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderBadSubjectByIdentifier()

  public void testFinderBadSubjectByIdentifierWithBadType() {
    String  id    = "i do not exist";
    String  type  = "person";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type, true);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderBadSubjectByIdentifierWithBadType()

  public void testFinderBadSubjectByIdentifierWithGoodType() {
    String  id    = "i do not exist";
    String  type  = "application";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type, true);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderBadSubjectByIdentifierWithGoodType()

  public void testFinderGrouperSystemSubject() {
    String id = "GrouperSystem";
    try { 
      Subject subj = SubjectFinder.findById(id, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      //Configurable names means we should not assert this any longer
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubject()

  public void testFinderGrouperSystemSubjectByIdentifier() {
    String id = "GrouperSystem";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      //Configurable names means we should no longer make assertion
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectByIdentifier()

  public void testFinderGrouperSystemSubjectByIdentifierWithGoodType() {
    String  id    = "GrouperSystem";
    String  type  = "application";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      //Configurable names means we should no longer make assertion
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals(type)
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectByIdentifierWithGoodType()

  public void testFinderGrouperSystemSubjectWithGoodType() {
    String  id    = "GrouperSystem";
    String  type  = "application";
    try { 
      Subject subj = SubjectFinder.findById(id, type, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      //Configurable names means we should no longer make assertion
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals(type)
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectWithGoodType()

  /**
   * 
   */
  public void testFindAll() {
    Set<Source> sources = GrouperUtil.toSet(SourceManager.getInstance().getSource("jdbc"));
    Set<Subject> subjects = SubjectFinder.findAll("whatever", sources);
    assertEquals(0, GrouperUtil.length(subjects));
    
    sources = GrouperUtil.convertSources("jdbc,g:isa");
    
    subjects = SubjectFinder.findAll(SubjectTestHelper.SUBJ0_ID);
    
    if (GrouperDdlUtils.isPostgres()) {
      assertEquals("Note, with postgres, you need to adjust your sources.xml for it to " +
      		"work with postgres, uncomment the postgres part of the jdbc source, search sql " +
      		"part, and comment out the current part", 1, GrouperUtil.length(subjects));
    } else {
      assertEquals(1, GrouperUtil.length(subjects));
    }
    
    assertEquals(SubjectTestHelper.SUBJ0_ID, subjects.iterator().next().getId());
  }

  /**
   * 
   */
  public void testFindAllSourceException() {
    
    //should work
    try {
      SubjectFinder.findAll("string1");
      
      GrouperSourceAdapter.failOnSearchForTesting = true;
  
      try {
        SubjectFinder.findAll("string2");
        fail();
      } catch (SourceUnavailableException sue) {
        //good
      }
      
      GrouperSourceAdapter.failOnSearchForTesting = false;

      SubjectFinder.findAll("string3");

      JDBCSourceAdapter.failOnSearchForTesting = true;
      
      try {
        SubjectFinder.findAll("string4");
        fail();
      } catch (SourceUnavailableException sue) {
        //good
      }
      
      JDBCSourceAdapter.failOnSearchForTesting = false;
      

      SubjectFinder.findAll("string5");

    } finally {
      
      GrouperSourceAdapter.failOnSearchForTesting = false;
      JDBCSourceAdapter.failOnSearchForTesting = false;
      JNDISourceAdapterLegacy.failOnSearchForTesting = false;
      JDBCSourceAdapter2.failOnSearchForTesting = false;
      
    }
  }

  /**
   * 
   */
  public void testFindByOptionalArguments() {

    Subject subject = null;
    
    subject = SubjectFinder.findByOptionalArgs(null, null, "edu:i2", true);
    
    assertNotNull(subject);
    
    subject = SubjectFinder.findByOptionalArgs("g:gsa", null, "edu:i2", true);
    
    assertNotNull(subject);

    try {
      subject = SubjectFinder.findByOptionalArgs("g:gsa", null, "edu:i2abc", true);
      fail("Shouldnt get here");
    } catch (SubjectNotFoundException snfe) {
      //good
    }

    subject = SubjectFinder.findByOptionalArgs("g:gsa", null, "edu:i2abc", false);
    assertNull(subject);

    subject = SubjectFinder.findByOptionalArgs(null, this.i2.getId(), null, true);
    assertNotNull(subject);
    
    subject = SubjectFinder.findByOptionalArgs("g:gsa", this.i2.getId(), null, true);
    assertNotNull(subject);

    try {
      subject = SubjectFinder.findByOptionalArgs("g:gsa", "abc", null, true);
      fail("Shouldnt get here");
    } catch (SubjectNotFoundException snfe) {
      //good
    }

    subject = SubjectFinder.findByOptionalArgs("g:gsa", "abc", null, false);
    assertNull(subject);
    
    
    
  }
  
  /**
   * 
   */
  public void testFindByPackedSubjectString() {

    Subject subject = null;
    
    subject = SubjectFinder.findByPackedSubjectString(":::::: edu:i2", true);
    
    assertNotNull(subject);
    
    subject = SubjectFinder.findByPackedSubjectString("g:gsa :::::: edu:i2", true);
    
    assertNotNull(subject);

    try {
      subject = SubjectFinder.findByPackedSubjectString("g:gsa :::::: edu:i2abc", true);
      fail("Shouldnt get here");
    } catch (SubjectNotFoundException snfe) {
      //good
    }

    subject = SubjectFinder.findByPackedSubjectString("g:gsa :::::: edu:i2abc", false);
    assertNull(subject);

    subject = SubjectFinder.findByPackedSubjectString(this.i2.getId(), true);
    assertNotNull(subject);
    
    subject = SubjectFinder.findByPackedSubjectString("g:gsa :::: " + this.i2.getId(), true);
    assertNotNull(subject);

    try {
      subject = SubjectFinder.findByPackedSubjectString("g:gsa :::: abc", true);
      fail("Shouldnt get here");
    } catch (SubjectNotFoundException snfe) {
      //good
    }

    subject = SubjectFinder.findByPackedSubjectString("g:gsa :::: abc", false);
    assertNull(subject);

    subject = SubjectFinder.findByPackedSubjectString("abc", false);
    assertNull(subject);
    
    subject = SubjectFinder.findByPackedSubjectString("edu:i2", false);
    assertNotNull(subject);
    
    subject = SubjectFinder.findByPackedSubjectString(this.i2.getId(), false);
    assertNotNull(subject);
    
    subject = SubjectFinder.findByPackedSubjectString(":::::::: abc", false);
    assertNull(subject);
    
    subject = SubjectFinder.findByPackedSubjectString(":::::::: edu:i2", false);
    assertNotNull(subject);
    
    subject = SubjectFinder.findByPackedSubjectString(":::::::: " + this.i2.getId(), false);
    assertNotNull(subject);
    
    subject = SubjectFinder.findByPackedSubjectString("g:gsa :::::::: edu:i2", true);
    
    assertNotNull(subject);

    try {
      subject = SubjectFinder.findByPackedSubjectString("g:gsa :::::::: edu:i2abc", true);
      fail("Shouldnt get here");
    } catch (SubjectNotFoundException snfe) {
      //good
    }

    subject = SubjectFinder.findByPackedSubjectString("g:gsa :::::::: edu:i2abc", false);
    assertNull(subject);

    subject = SubjectFinder.findByPackedSubjectString("g:gsa :::::::: " + this.i2.getId(), true);
    assertNotNull(subject);

    try {
      subject = SubjectFinder.findByPackedSubjectString("g:gsa :::::::: abc", true);
      fail("Shouldnt get here");
    } catch (SubjectNotFoundException snfe) {
      //good
    }

    subject = SubjectFinder.findByPackedSubjectString("g:gsa :::::::: abc", false);
    assertNull(subject);
    
    
  }

  /**
   * test find multiple in jdbc and jdbc2
   */
  public void testFindByIdentifiers() {
    
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
        
    List<Group> groups = new ArrayList<Group>();
    
    for (int i=0;i<10;i++) {
      
      Group group = new GroupSave(grouperSession)
        .assignName("test:testGroup" + i)
        .assignCreateParentStemsIfNotExist(true).save();
      groups.add(group);
    }
    
    groups.get(0).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    groups.get(1).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    groups.get(2).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    groups.get(0).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    groups.get(1).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    
    Set<String> identifiers = GrouperUtil.toSet(
        SubjectTestHelper.SUBJ0_IDENTIFIER, 
        SubjectTestHelper.SUBJ1_IDENTIFIER,
        SubjectTestHelper.SUBJ2_IDENTIFIER, 
        SubjectTestHelper.SUBJ3_IDENTIFIER, 
        SubjectTestHelper.SUBJ4_IDENTIFIER, 
        SubjectTestHelper.SUBJ5_IDENTIFIER, 
        SubjectTestHelper.SUBJ6_IDENTIFIER, 
        SubjectTestHelper.SUBJ7_IDENTIFIER, 
        SubjectTestHelper.SUBJ8_IDENTIFIER, 
        SubjectTestHelper.SUBJ9_IDENTIFIER);
    
    identifiers.add(SubjectFinder.findAllSubject().getId());
    identifiers.add(SubjectFinder.findRootSubject().getId());
    
    List<ExternalSubject> externalSubjects = new ArrayList<ExternalSubject>();
    
    for (int i=0;i<8;i++) {
      ExternalSubject externalSubject = new ExternalSubject();
      externalSubject.setEmail("a" + i + "@b.c");
      externalSubject.setIdentifier("a" + i + "@id.b.c");
      externalSubject.setInstitution("My Institution" + i);
      externalSubject.setName("My Name" + i);
      externalSubject.store();
      externalSubjects.add(externalSubject);
      identifiers.add(externalSubject.getIdentifier());
    }
  
    Set<String> identifiersExist = new HashSet<String>(identifiers);
    
    for (Group group : groups) {
      identifiers.add(group.getName());
    }
    
    identifiersExist.add(groups.get(0).getName());
    identifiersExist.add(groups.get(1).getName());
    identifiersExist.add(groups.get(2).getName());
  
    //one it wont find, thats ok
    identifiers.add("abc");
    
    EhcacheController.ehcacheController().flushCache();
    //clear cache so we can see it work
    SubjectFinder.flushCache();
    
    long initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    Map<String, Subject> subjectMap = SubjectFinder.findByIdentifiers(identifiers);
    
    assertNotNull("subjectMap is null??????", subjectMap);
    
    assertEquals(identifiers.size()-1, subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_IDENTIFIER).getName());
    
    long numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    assertTrue("queries: " + numberOfQueries, numberOfQueries < 10);
    assertTrue("queries: " + numberOfQueries, numberOfQueries > 0);
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################ TRY AS SUBJ0
    
    long start = System.nanoTime();
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    EhcacheController.ehcacheController().flushCache();
    //clear cache so we can see it work
    SubjectFinder.flushCache();
    
  
    initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    subjectMap = SubjectFinder.findByIdentifiers(identifiers);
    
    assertEquals((identifiers.size()-1) - 7, subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_IDENTIFIER).getName());
    
    numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    assertTrue("queries: " + numberOfQueries, numberOfQueries < 10);
    assertTrue("queries: " + numberOfQueries, numberOfQueries > 0);
    
    System.out.println("Took: " + ((System.nanoTime() - start) / 1000000) + "ms");
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################ DO IT AGAIN IN CACHE
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    subjectMap = SubjectFinder.findByIdentifiers(identifiersExist);
    
    assertEquals(identifiersExist.size(), subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_IDENTIFIER).getName());
    
    numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    assertEquals("queries: " + numberOfQueries, 0, numberOfQueries);
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################ DO IT AGAIN BY SOURCE IN CACHE

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    subjectMap = SubjectFinder.findByIdentifiers(identifiersExist, "jdbc");

    assertEquals(10, subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_IDENTIFIER).getName());
    
    numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    //all the ones from other sources will be searched here...
    assertEquals("queries: " + numberOfQueries, 1, numberOfQueries);
    
    GrouperSession.stopQuietly(grouperSession);
    
    
  }

  /**
   * test find multiple in jdbc and jdbc2
   */
  public void testFindByIdsOrIdentifiers() {
    
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
        
    List<Group> groups = new ArrayList<Group>();
    
    for (int i=0;i<50;i++) {
      
      Group group = new GroupSave(grouperSession)
        .assignName("test:testGroup" + i)
        .assignCreateParentStemsIfNotExist(true).save();
      groups.add(group);
    }
    
    groups.get(0).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    groups.get(1).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    groups.get(2).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    groups.get(0).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    groups.get(1).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    
    Set<String> identifiers = GrouperUtil.toSet(
        SubjectTestHelper.SUBJ0_ID, 
        SubjectTestHelper.SUBJ1_IDENTIFIER,
        SubjectTestHelper.SUBJ2_ID, 
        SubjectTestHelper.SUBJ3_IDENTIFIER, 
        SubjectTestHelper.SUBJ4_ID, 
        SubjectTestHelper.SUBJ5_IDENTIFIER, 
        SubjectTestHelper.SUBJ6_ID, 
        SubjectTestHelper.SUBJ7_IDENTIFIER, 
        SubjectTestHelper.SUBJ8_ID, 
        SubjectTestHelper.SUBJ9_IDENTIFIER);
    
    identifiers.add(SubjectFinder.findAllSubject().getId());
    identifiers.add(SubjectFinder.findRootSubject().getId());
    
    List<ExternalSubject> externalSubjects = new ArrayList<ExternalSubject>();
    
    for (int i=0;i<50;i++) {
      ExternalSubject externalSubject = new ExternalSubject();
      externalSubject.setEmail("a" + i + "@b.c");
      externalSubject.setIdentifier("a" + i + "@id.b.c");
      externalSubject.setInstitution("My Institution" + i);
      externalSubject.setName("My Name" + i);
      externalSubject.store();
      externalSubjects.add(externalSubject);
      if (i%2==0) {
        identifiers.add(externalSubject.getIdentifier());
      } else {
        identifiers.add(externalSubject.getUuid());
      }
    }
  
    Set<String> identifiersExist = new HashSet<String>(identifiers);
    
    int index=0;
    for (Group group : groups) {
      if (index%2==0) {
        identifiers.add(group.getName());
      } else {
        identifiers.add(group.getUuid());
      }
      index++;
    }
    
    identifiersExist.add(groups.get(0).getName());
    identifiersExist.add(groups.get(1).getUuid());
    identifiersExist.add(groups.get(2).getName());
  
    //one it wont find, thats ok
    identifiers.add("abc");
    
    EhcacheController.ehcacheController().flushCache();
    //clear cache so we can see it work
    SubjectFinder.flushCache();
    
    long initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    Map<String, Subject> subjectMap = SubjectFinder.findByIdsOrIdentifiers(identifiers);
    
    assertNotNull("subjectMap is null??????", subjectMap);
    
    assertEquals(identifiers.size()-1, subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_ID).getName());
    assertEquals(SubjectTestHelper.SUBJ1_NAME, subjectMap.get(SubjectTestHelper.SUBJ1_IDENTIFIER).getName());
    
    long numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    assertTrue("queries: " + numberOfQueries, numberOfQueries < 20);
    assertTrue("queries: " + numberOfQueries, numberOfQueries > 0);
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################ TRY AS SUBJ0
    
    long start = System.nanoTime();
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    EhcacheController.ehcacheController().flushCache();
    //clear cache so we can see it work
    SubjectFinder.flushCache();
    
  
    initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    subjectMap = SubjectFinder.findByIdsOrIdentifiers(identifiers);
    
    assertEquals(65, subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_ID).getName());
    
    numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    assertTrue("queries: " + numberOfQueries, numberOfQueries < 30);
    assertTrue("queries: " + numberOfQueries, numberOfQueries > 0);
    
    System.out.println("Took: " + ((System.nanoTime() - start) / 1000000) + "ms");
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################ DO IT AGAIN IN CACHE
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    subjectMap = SubjectFinder.findByIdsOrIdentifiers(identifiersExist);
    
    assertEquals(identifiersExist.size(), subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_ID).getName());
    
    numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    assertEquals("queries: " + numberOfQueries, 0, numberOfQueries);
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################ DO IT AGAIN BY SOURCE IN CACHE
  
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    initialQueryCount = GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting;
    subjectMap = SubjectFinder.findByIdsOrIdentifiers(identifiersExist, "jdbc");
  
    assertEquals(10, subjectMap.size());
    assertEquals(SubjectTestHelper.SUBJ0_NAME, subjectMap.get(SubjectTestHelper.SUBJ0_ID).getName());
    
    numberOfQueries = (GrouperContext.totalQueryCount + JDBCSourceAdapter.queryCountforTesting) - initialQueryCount;
    
    //all the ones from other sources will be searched here for id and for identifier...
    assertEquals("queries: " + numberOfQueries, 2, numberOfQueries);
    
    GrouperSession.stopQuietly(grouperSession);
    
    
  }
}

