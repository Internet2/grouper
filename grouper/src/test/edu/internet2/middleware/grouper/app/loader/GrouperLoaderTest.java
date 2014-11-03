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
/*
 * @author mchyzer
 * $Id: GrouperLoaderTest.java,v 1.12 2009-11-09 03:12:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.ByHql;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * 
 */
public class GrouperLoaderTest extends GrouperTest {

  /**
   * @param name
   */
  public GrouperLoaderTest(String name) {
    super(name);
  }

  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new GrouperLoaderTest("testLoaderTypes"));
    //new GrouperLoaderTest("whatever").ensureTestgrouperLoaderTables();
    //performanceRunSetupLoaderTables();
    performanceRun();
  }

  /**
   * 
   */
  public static void performanceRunSetupLoaderTables() {
    
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    try {
    
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperLoader").executeUpdate();
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperLoaderGroups").executeUpdate();
    
      
      for (int i=0;i<200;i++) {
        List<TestgrouperLoader> testDataList = new ArrayList<TestgrouperLoader>();
  
        for (int j=0;j<10;j++) {
          TestgrouperLoader group1subj0 = new TestgrouperLoader("loader:group_" + i + "_systemOfRecord", "test.subject." + j, null);
          testDataList.add(group1subj0);
        }
    
        HibernateSession.byObjectStatic().saveOrUpdate(testDataList);
        
      }
  
      //lets add a group which will load these
      Group loaderGroup = new GroupSave(grouperSession).assignName("loader:owner").assignCreateParentStemsIfNotExist(true).save();
      loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, 
          "select col1 as GROUP_NAME, col2 as SUBJECT_ID from testgrouper_loader");
  //    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_TYPES,
  //        "addIncludeExclude");
    } finally {
      grouperSessionResult.stopQuietlyIfCreated();
    }
  }
  
  /**
   * 
   */
  public static void performanceRun() {
      
    GrouperSession grouperSession = GrouperSession.startRootSession();

    try {
      Group loaderGroup = GroupFinder.findByName(grouperSession, "loader:owner", true);

      loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, 
          "select col1 as GROUP_NAME, col2 as SUBJECT_ID from testgrouper_loader");

      //prime the pump
      performanceRunClearOutSomeMembers(grouperSession);
  
      GrouperLoader.runJobOnceForGroup(grouperSession, loaderGroup);

      performanceRunClearOutAllMembers(grouperSession);

      GrouperSession.stopQuietly(grouperSession);
      SubjectFinder.flushCache();
      EhcacheController.ehcacheController().flushCache();
      grouperSession =  GrouperSession.startRootSession();
      
      long now = System.nanoTime();
      long queryCount = GrouperContext.totalQueryCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, loaderGroup);
      
      long tookMillis = (System.nanoTime() - now) / 1000000;
      
      System.out.println("All took: " + tookMillis + "ms, " + (GrouperContext.totalQueryCount - queryCount) + " queries");
      
      performanceRunClearOutSomeMembers(grouperSession);

      GrouperSession.stopQuietly(grouperSession);
      SubjectFinder.flushCache();
      EhcacheController.ehcacheController().flushCache();
      grouperSession =  GrouperSession.startRootSession();

      now = System.nanoTime();
      queryCount = GrouperContext.totalQueryCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, loaderGroup);
      
      tookMillis = (System.nanoTime() - now) / 1000000;
      
      System.out.println("Some took: " + tookMillis + "ms, " + (GrouperContext.totalQueryCount - queryCount) + " queries");

      loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, 
          "select col1 as GROUP_NAME, col2 as SUBJECT_ID, 'jdbc' as SUBJECT_SOURCE_ID from testgrouper_loader");

      System.out.println("\nAdded subject_source_id to query...\n");
      
      performanceRunClearOutAllMembers(grouperSession);

      GrouperSession.stopQuietly(grouperSession);
      SubjectFinder.flushCache();
      EhcacheController.ehcacheController().flushCache();
      grouperSession =  GrouperSession.startRootSession();
      
      now = System.nanoTime();
      queryCount = GrouperContext.totalQueryCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, loaderGroup);
      
      tookMillis = (System.nanoTime() - now) / 1000000;
      
      System.out.println("All took: " + tookMillis + "ms, " + (GrouperContext.totalQueryCount - queryCount) + " queries");
      
      performanceRunClearOutSomeMembers(grouperSession);

      GrouperSession.stopQuietly(grouperSession);
      SubjectFinder.flushCache();
      EhcacheController.ehcacheController().flushCache();
      grouperSession =  GrouperSession.startRootSession();
      
      now = System.nanoTime();
      queryCount = GrouperContext.totalQueryCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, loaderGroup);
      
      tookMillis = (System.nanoTime() - now) / 1000000;
      
      System.out.println("Some took: " + tookMillis + "ms, " + (GrouperContext.totalQueryCount - queryCount) + " queries");


    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param grouperSession
   */
  public static void performanceRunClearOutAllMembers(GrouperSession grouperSession) {
    //clear out the groups
    for (int i=0;i<10;i++) {
      String groupName = "loader:group_" + i + "_systemOfRecord";
      Group group = GroupFinder.findByName(grouperSession, groupName, false);
      if (group != null) {
        for (Member member : group.getImmediateMembers()) {
          group.deleteMember(member);
        }
      }
          
    }
  }
  
  /**
   * @param grouperSession
   */
  public static void performanceRunClearOutSomeMembers(GrouperSession grouperSession) {
    //clear out the groups
    for (int i=0;i<10;i++) {
      String groupName = "loader:group_" + i + "_systemOfRecord";
      Group group = GroupFinder.findByName(grouperSession, groupName, false);
      if (group != null) {
        group.deleteMember(SubjectTestHelper.SUBJ0, false);
        group.addMember(SubjectTestHelper.SUBJR, false);
      }
          
    }
  }
  
  /**
   * test the loader
   * @throws Exception 
   */
  public void testLoaderTypesGroupMeta() throws Exception {
    
    List<GrouperAPI> testDataList = new ArrayList<GrouperAPI>();
    
    TestgrouperLoader group1subj0 = new TestgrouperLoader("loader:group1_systemOfRecord", SubjectTestHelper.SUBJ0_ID, null);
    testDataList.add(group1subj0);
    TestgrouperLoader group1subj1 = new TestgrouperLoader("loader:group1_systemOfRecord", SubjectTestHelper.SUBJ1_ID, null);
    testDataList.add(group1subj1);
    TestgrouperLoader group2subj1 = new TestgrouperLoader("loader:group2_systemOfRecord", SubjectTestHelper.SUBJ1_ID, null);
    testDataList.add(group2subj1);
    TestgrouperLoader group2subj2 = new TestgrouperLoader("loader:group2_systemOfRecord", SubjectTestHelper.SUBJ2_ID, null);
    testDataList.add(group2subj2);
    TestgrouperLoader group3subj2 = new TestgrouperLoader("loader:group3_systemOfRecord", SubjectTestHelper.SUBJ2_ID, null);
    testDataList.add(group3subj2);
    TestgrouperLoader group3subj3 = new TestgrouperLoader("loader:group3_systemOfRecord", SubjectTestHelper.SUBJ3_ID, null);
    testDataList.add(group3subj3);
    TestgrouperLoader group4subj3 = new TestgrouperLoader("loader:group4_systemOfRecord", SubjectTestHelper.SUBJ3_ID, null);
    testDataList.add(group4subj3);
    TestgrouperLoader group4subj4 = new TestgrouperLoader("loader:group4_systemOfRecord", SubjectTestHelper.SUBJ4_ID, null);
    testDataList.add(group4subj4);
    TestgrouperLoader group6subj5 = new TestgrouperLoader("loader:group6_systemOfRecord", SubjectTestHelper.SUBJ5_ID, null);
    testDataList.add(group6subj5);
    TestgrouperLoader group6subj6 = new TestgrouperLoader("loader:group6_systemOfRecord", SubjectTestHelper.SUBJ6_ID, null);
    testDataList.add(group6subj6);
    
    // this one is not in the groupQuery so the group won't be added...
    TestgrouperLoader group5subj6 = new TestgrouperLoader("loader:group5_systemOfRecord", SubjectTestHelper.SUBJ6_ID, null);
    testDataList.add(group5subj6);

    TestgrouperLoaderGroups group1meta = new TestgrouperLoaderGroups("loader:group1_systemOfRecord", 
        "The loader:group 1 system of record", "This is the first group");
    testDataList.add(group1meta);
    TestgrouperLoaderGroups group2meta = new TestgrouperLoaderGroups("loader:group2_systemOfRecord", 
        "The loader:group 2 system of record", null);
    testDataList.add(group2meta);
    TestgrouperLoaderGroups group3meta = new TestgrouperLoaderGroups("loader:group3_systemOfRecord", 
        null, "This is the third group");
    testDataList.add(group3meta);
    TestgrouperLoaderGroups group4meta = new TestgrouperLoaderGroups("loader:group4_systemOfRecord", 
        null, "This is the forth group");
    testDataList.add(group4meta);
    TestgrouperLoaderGroups group6meta = new TestgrouperLoaderGroups("loader:group6_systemOfRecord", 
        null, "This is the sixth group");
    testDataList.add(group6meta);
    TestgrouperLoaderGroups group7meta = new TestgrouperLoaderGroups("loader:group7_systemOfRecord", 
        null, "This is the seventh group");
    testDataList.add(group7meta);
    
    
    HibernateSession.byObjectStatic().saveOrUpdate(testDataList);

    //lets add a group which will load these
    Group loaderGroup = Group.saveGroup(this.grouperSession, null, null, 
        "loader2:owner",null, null, null, true);
    loaderGroup.addType(GroupTypeFinder.find("grouperLoader", true));
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, 
        "select col1 as GROUP_NAME, col2 as SUBJECT_ID from testgrouper_loader");
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_TYPES,
        "addIncludeExclude");
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_QUERY,
      "select group_name, group_display_name, group_description from testgrouper_loader_groups");
    
    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);
    
    Group overallGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1", true);
    assertEquals("The loader:group 1", overallGroup1.getDisplayName());
    Group systemOfRecordGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1_systemOfRecord", true);
    assertEquals("This is the first group", systemOfRecordGroup1.getDescription());
    assertEquals("The loader:group 1 system of record", systemOfRecordGroup1.getDisplayName());
    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ6));
    
    Group overallGroup2 = GroupFinder.findByName(this.grouperSession, "loader:group2", true);

    assertEquals("The loader:group 2", overallGroup2.getDisplayName());
    Group systemOfRecordGroup2 = GroupFinder.findByName(this.grouperSession, "loader:group2_systemOfRecord", true);
    assertTrue(systemOfRecordGroup2.getDescription().length() > 0);
    assertEquals("The loader:group 2 system of record", systemOfRecordGroup2.getDisplayName());

    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup2.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ6));
    
    Group overallGroup3 = GroupFinder.findByName(this.grouperSession, "loader:group3", true);
    assertEquals("The loader:group3", overallGroup3.getDisplayName());
    Group systemOfRecordGroup3 = GroupFinder.findByName(this.grouperSession, "loader:group3_systemOfRecord", true);
    assertEquals("This is the third group", systemOfRecordGroup3.getDescription());
    assertEquals("The loader:group3_systemOfRecord", systemOfRecordGroup3.getDisplayName());

    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ6));

    Group overallGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4", true);
    assertEquals("The loader:group4", overallGroup4.getDisplayName());
    Group systemOfRecordGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4_systemOfRecord", true);
    assertTrue(systemOfRecordGroup4.getDescription().length() > 0);
    assertEquals("The loader:group4_systemOfRecord", systemOfRecordGroup4.getDisplayName());
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ6));

    Group overallGroup5 = GroupFinder.findByName(this.grouperSession, "loader:group5", false);
    assertNull(overallGroup5);
    
    //lets use the includes/excludes for group6
    Group group6includes = GroupFinder.findByName(this.grouperSession, "loader:group6_includes", true);
    group6includes.addMember(SubjectTestHelper.SUBJ9);

    Group overallGroup6 = GroupFinder.findByName(this.grouperSession, "loader:group6", true);
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ9));
    
    // group seven should exist without any members
    Group overallGroup7 = GroupFinder.findByName(this.grouperSession, "loader:group7_systemOfRecord", true);
    assertEquals(0, overallGroup7.getMembers().size());
    
    //lets make sure the security groups dont exist
    new StemSave(this.grouperSession).assignName("loaderSecurity")
      .assignSaveMode(SaveMode.INSERT).save();
    
    Group admins = GroupFinder.findByName(this.grouperSession, "loaderSecurity:admins", false);
    assertNull(admins);
    Group readers = GroupFinder.findByName(this.grouperSession, "loaderSecurity:readers", false);
    assertNull(readers);
    Group updaters = GroupFinder.findByName(this.grouperSession, "loaderSecurity:updaters", false);
    assertNull(updaters);
    Group viewers = GroupFinder.findByName(this.grouperSession, "loaderSecurity:viewers", false);
    assertNull(viewers);
    Group optins = GroupFinder.findByName(this.grouperSession, "loaderSecurity:optins", false);
    assertNull(optins);
    Group optouts = GroupFinder.findByName(this.grouperSession, "loaderSecurity:optouts", false);
    assertNull(optouts);
    Group groupAttrReaders = GroupFinder.findByName(this.grouperSession, "loaderSecurity:groupAttrReaders", false);
    assertNull(groupAttrReaders);
    Group groupAttrUpdaters = GroupFinder.findByName(this.grouperSession, "loaderSecurity:groupAttrUpdaters", false);
    assertNull(groupAttrUpdaters);
    
    //change the query to include all these groups
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_QUERY,
      "select group_name, group_display_name, group_description, 'loaderSecurity:groupAttrReaders' as group_attr_readers, 'loaderSecurity:groupAttrUpdaters' as group_attr_updaters, 'loaderSecurity:admins' as admins, 'loaderSecurity:readers' as readers, 'loaderSecurity:viewers' as viewers, 'loaderSecurity:updaters' as updaters, 'loaderSecurity:optins' as optins, 'loaderSecurity:optouts' as optouts from testgrouper_loader_groups");
    loaderGroup.store();
    
    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);

    admins = GroupFinder.findByName(this.grouperSession, "loaderSecurity:admins", false);
    assertNotNull(admins);
    readers = GroupFinder.findByName(this.grouperSession, "loaderSecurity:readers", false);
    assertNotNull(readers);
    updaters = GroupFinder.findByName(this.grouperSession, "loaderSecurity:updaters", false);
    assertNotNull(updaters);
    viewers = GroupFinder.findByName(this.grouperSession, "loaderSecurity:viewers", false);
    assertNotNull(viewers);
    optins = GroupFinder.findByName(this.grouperSession, "loaderSecurity:optins", false);
    assertNotNull(optins);
    optouts = GroupFinder.findByName(this.grouperSession, "loaderSecurity:optouts", false);
    assertNotNull(optouts);
    groupAttrReaders = GroupFinder.findByName(this.grouperSession, "loaderSecurity:groupAttrReaders", false);
    assertNotNull(groupAttrReaders);
    groupAttrUpdaters = GroupFinder.findByName(this.grouperSession, "loaderSecurity:groupAttrUpdaters", false);
    assertNotNull(groupAttrUpdaters);
    
    //make sure they have the privilege
    overallGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4", true);
    assertTrue(overallGroup4.hasRead(readers.toSubject()));
    assertFalse(overallGroup4.hasAdmin(viewers.toSubject()));
    assertTrue(overallGroup4.hasView(viewers.toSubject()));
    //note, on include/exclude groups, shouldnt have update on overall group...
    assertFalse(overallGroup4.hasUpdate(updaters.toSubject()));
    assertTrue(overallGroup4.hasOptin(optins.toSubject()));
    assertTrue(overallGroup4.hasOptout(optouts.toSubject()));
    assertTrue(overallGroup4.hasAdmin(admins.toSubject()));
    assertTrue(overallGroup4.hasGroupAttrRead(groupAttrReaders.toSubject()));
    assertTrue(overallGroup4.hasGroupAttrUpdate(groupAttrUpdaters.toSubject()));
    assertFalse(overallGroup4.hasGroupAttrRead(groupAttrUpdaters.toSubject()));
    assertFalse(overallGroup4.hasGroupAttrUpdate(groupAttrReaders.toSubject()));
    
    Group includesGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4_includes", true);
    assertTrue(includesGroup4.hasRead(readers.toSubject()));
    assertFalse(includesGroup4.hasAdmin(viewers.toSubject()));
    assertTrue(includesGroup4.hasView(viewers.toSubject()));
    //note, on include/exclude groups, should have update on overall group...
    assertTrue(includesGroup4.hasUpdate(updaters.toSubject()));
    assertTrue(includesGroup4.hasOptin(optins.toSubject()));
    assertTrue(includesGroup4.hasOptout(optouts.toSubject()));
    assertTrue(includesGroup4.hasAdmin(admins.toSubject()));
    assertTrue(includesGroup4.hasGroupAttrRead(groupAttrReaders.toSubject()));
    assertTrue(includesGroup4.hasGroupAttrUpdate(groupAttrUpdaters.toSubject()));
    assertFalse(includesGroup4.hasGroupAttrRead(groupAttrUpdaters.toSubject()));
    assertFalse(includesGroup4.hasGroupAttrUpdate(groupAttrReaders.toSubject()));
    
    Group excludesGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4_excludes", true);
    assertTrue(excludesGroup4.hasRead(readers.toSubject()));
    assertFalse(excludesGroup4.hasAdmin(viewers.toSubject()));
    assertTrue(excludesGroup4.hasView(viewers.toSubject()));
    //note, on include/exclude groups, should have update on overall group...
    assertTrue(excludesGroup4.hasUpdate(updaters.toSubject()));
    assertTrue(excludesGroup4.hasOptin(optins.toSubject()));
    assertTrue(excludesGroup4.hasOptout(optouts.toSubject()));
    assertTrue(excludesGroup4.hasAdmin(admins.toSubject()));
    assertTrue(excludesGroup4.hasGroupAttrRead(groupAttrReaders.toSubject()));
    assertTrue(excludesGroup4.hasGroupAttrUpdate(groupAttrUpdaters.toSubject()));
    assertFalse(excludesGroup4.hasGroupAttrRead(groupAttrUpdaters.toSubject()));
    assertFalse(excludesGroup4.hasGroupAttrUpdate(groupAttrReaders.toSubject()));

    systemOfRecordGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4_systemOfRecord", true);
    assertTrue(systemOfRecordGroup4.hasRead(readers.toSubject()));
    assertFalse(systemOfRecordGroup4.hasAdmin(viewers.toSubject()));
    assertTrue(systemOfRecordGroup4.hasView(viewers.toSubject()));
    //note, on include/exclude groups, shouldnt have update on overall group...
    assertFalse(systemOfRecordGroup4.hasUpdate(updaters.toSubject()));
    assertTrue(systemOfRecordGroup4.hasOptin(optins.toSubject()));
    assertTrue(systemOfRecordGroup4.hasOptout(optouts.toSubject()));
    assertTrue(systemOfRecordGroup4.hasAdmin(admins.toSubject()));
    assertTrue(systemOfRecordGroup4.hasGroupAttrRead(groupAttrReaders.toSubject()));
    assertTrue(systemOfRecordGroup4.hasGroupAttrUpdate(groupAttrUpdaters.toSubject()));
    assertFalse(systemOfRecordGroup4.hasGroupAttrRead(groupAttrUpdaters.toSubject()));
    assertFalse(systemOfRecordGroup4.hasGroupAttrUpdate(groupAttrReaders.toSubject()));

    
    //make sure they have the privilege -- check group with no members
    overallGroup7 = GroupFinder.findByName(this.grouperSession, "loader:group7", true);
    assertTrue(overallGroup7.hasRead(readers.toSubject()));
    assertFalse(overallGroup7.hasAdmin(viewers.toSubject()));
    assertTrue(overallGroup7.hasView(viewers.toSubject()));
    //note, on include/exclude groups, shouldnt have update on overall group...
    assertFalse(overallGroup7.hasUpdate(updaters.toSubject()));
    assertTrue(overallGroup7.hasOptin(optins.toSubject()));
    assertTrue(overallGroup7.hasOptout(optouts.toSubject()));
    assertTrue(overallGroup7.hasAdmin(admins.toSubject()));
    assertTrue(overallGroup7.hasGroupAttrRead(groupAttrReaders.toSubject()));
    assertTrue(overallGroup7.hasGroupAttrUpdate(groupAttrUpdaters.toSubject()));
    assertFalse(overallGroup7.hasGroupAttrRead(groupAttrUpdaters.toSubject()));
    assertFalse(overallGroup7.hasGroupAttrUpdate(groupAttrReaders.toSubject()));
    
    Group includesGroup7 = GroupFinder.findByName(this.grouperSession, "loader:group7_includes", true);
    assertTrue(includesGroup7.hasRead(readers.toSubject()));
    assertFalse(includesGroup7.hasAdmin(viewers.toSubject()));
    assertTrue(includesGroup7.hasView(viewers.toSubject()));
    //note, on include/exclude groups, should have update on overall group...
    assertTrue(includesGroup7.hasUpdate(updaters.toSubject()));
    assertTrue(includesGroup7.hasOptin(optins.toSubject()));
    assertTrue(includesGroup7.hasOptout(optouts.toSubject()));
    assertTrue(includesGroup7.hasAdmin(admins.toSubject()));
    assertTrue(includesGroup7.hasGroupAttrRead(groupAttrReaders.toSubject()));
    assertTrue(includesGroup7.hasGroupAttrUpdate(groupAttrUpdaters.toSubject()));
    assertFalse(includesGroup7.hasGroupAttrRead(groupAttrUpdaters.toSubject()));
    assertFalse(includesGroup7.hasGroupAttrUpdate(groupAttrReaders.toSubject()));
    
    Group excludesGroup7 = GroupFinder.findByName(this.grouperSession, "loader:group7_excludes", true);
    assertTrue(excludesGroup7.hasRead(readers.toSubject()));
    assertFalse(excludesGroup7.hasAdmin(viewers.toSubject()));
    assertTrue(excludesGroup7.hasView(viewers.toSubject()));
    //note, on include/exclude groups, should have update on overall group...
    assertTrue(excludesGroup7.hasUpdate(updaters.toSubject()));
    assertTrue(excludesGroup7.hasOptin(optins.toSubject()));
    assertTrue(excludesGroup7.hasOptout(optouts.toSubject()));
    assertTrue(excludesGroup7.hasAdmin(admins.toSubject()));
    assertTrue(excludesGroup7.hasGroupAttrRead(groupAttrReaders.toSubject()));
    assertTrue(excludesGroup7.hasGroupAttrUpdate(groupAttrUpdaters.toSubject()));
    assertFalse(excludesGroup7.hasGroupAttrRead(groupAttrUpdaters.toSubject()));
    assertFalse(excludesGroup7.hasGroupAttrUpdate(groupAttrReaders.toSubject()));
    
    Group systemOfRecordGroup7 = GroupFinder.findByName(this.grouperSession, "loader:group7_systemOfRecord", true);
    assertTrue(systemOfRecordGroup7.hasRead(readers.toSubject()));
    assertFalse(systemOfRecordGroup7.hasAdmin(viewers.toSubject()));
    assertTrue(systemOfRecordGroup7.hasView(viewers.toSubject()));
    //note, on include/exclude groups, shouldnt have update on overall group...
    assertFalse(systemOfRecordGroup7.hasUpdate(updaters.toSubject()));
    assertTrue(systemOfRecordGroup7.hasOptin(optins.toSubject()));
    assertTrue(systemOfRecordGroup7.hasOptout(optouts.toSubject()));
    assertTrue(systemOfRecordGroup7.hasAdmin(admins.toSubject()));
    assertTrue(systemOfRecordGroup7.hasGroupAttrRead(groupAttrReaders.toSubject()));
    assertTrue(systemOfRecordGroup7.hasGroupAttrUpdate(groupAttrUpdaters.toSubject()));
    assertFalse(systemOfRecordGroup7.hasGroupAttrRead(groupAttrUpdaters.toSubject()));
    assertFalse(systemOfRecordGroup7.hasGroupAttrUpdate(groupAttrReaders.toSubject()));
    
    // add a member to group7 and make sure it gets deleted by the loader job
    systemOfRecordGroup7.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);

    systemOfRecordGroup7 = GroupFinder.findByName(this.grouperSession, "loader:group7_systemOfRecord", true);
    assertEquals(0, systemOfRecordGroup7.getMembers().size());
  }
  /**
   * test the loader
   * @throws Exception 
   */
  public void testLoaderTypes() throws Exception {
    
    List<TestgrouperLoader> testDataList = new ArrayList<TestgrouperLoader>();
    
    TestgrouperLoader group1subj0 = new TestgrouperLoader("loader:group1_systemOfRecord", SubjectTestHelper.SUBJ0_ID, null);
    testDataList.add(group1subj0);
    TestgrouperLoader group1subj1 = new TestgrouperLoader("loader:group1_systemOfRecord", SubjectTestHelper.SUBJ1_ID, null);
    testDataList.add(group1subj1);
    TestgrouperLoader group2subj1 = new TestgrouperLoader("loader:group2_systemOfRecord", SubjectTestHelper.SUBJ1_ID, null);
    testDataList.add(group2subj1);
    TestgrouperLoader group2subj2 = new TestgrouperLoader("loader:group2_systemOfRecord", SubjectTestHelper.SUBJ2_ID, null);
    testDataList.add(group2subj2);
    TestgrouperLoader group3subj2 = new TestgrouperLoader("loader:group3_systemOfRecord", SubjectTestHelper.SUBJ2_ID, null);
    testDataList.add(group3subj2);
    TestgrouperLoader group3subj3 = new TestgrouperLoader("loader:group3_systemOfRecord", SubjectTestHelper.SUBJ3_ID, null);
    testDataList.add(group3subj3);
    TestgrouperLoader group4subj3 = new TestgrouperLoader("loader:group4_systemOfRecord", SubjectTestHelper.SUBJ3_ID, null);
    testDataList.add(group4subj3);
    TestgrouperLoader group4subj4 = new TestgrouperLoader("loader:group4_systemOfRecord", SubjectTestHelper.SUBJ4_ID, null);
    testDataList.add(group4subj4);
    TestgrouperLoader group6subj5 = new TestgrouperLoader("loader:group6_systemOfRecord", SubjectTestHelper.SUBJ5_ID, null);
    testDataList.add(group6subj5);
    TestgrouperLoader group6subj6 = new TestgrouperLoader("loader:group6_systemOfRecord", SubjectTestHelper.SUBJ6_ID, null);
    testDataList.add(group6subj6);

    HibernateSession.byObjectStatic().saveOrUpdate(testDataList);

    //lets add a group which will load these
    Group loaderGroup = Group.saveGroup(this.grouperSession, null, null, 
        "loader:owner",null, null, null, true);
    loaderGroup.addType(GroupTypeFinder.find("grouperLoader", true));
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, 
        "select col1 as GROUP_NAME, col2 as SUBJECT_ID from testgrouper_loader");
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_TYPES,
        "addIncludeExclude");
    
    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);
    
    Group overallGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1", true);
    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ6));
    
    Group overallGroup2 = GroupFinder.findByName(this.grouperSession, "loader:group2", true);
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup2.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ6));
    
    Group overallGroup3 = GroupFinder.findByName(this.grouperSession, "loader:group3", true);
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ6));

    Group overallGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4", true);
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ6));

    Group overallGroup5 = GroupFinder.findByName(this.grouperSession, "loader:group5", false);
    assertNull(overallGroup5);
    
    //lets use the includes/excludes for group6
    Group group6includes = GroupFinder.findByName(this.grouperSession, "loader:group6_includes", true);
    group6includes.addMember(SubjectTestHelper.SUBJ9);

    Group overallGroup6 = GroupFinder.findByName(this.grouperSession, "loader:group6", true);
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ9));

    
    //#########################################################
    //change around the groups for another run
    
    // now lets use a group in another group
    Group anotherGroup = Group.saveGroup(this.grouperSession, "aStem:anotherGroup", null, 
        "aStem:anotherGroup", null, null, null, true);
    anotherGroup.addMember(SubjectFinder.findById(overallGroup1.getUuid(), true));
    
    //now lets change around the memberships...
    //delete group1, group2, and group3subj2
    HibernateSession.byObjectStatic().delete(GrouperUtil.toList(group1subj0, 
        group1subj1, group2subj1, group2subj2, group3subj2, group6subj5, group6subj6));
    
    //add group3subj4
    TestgrouperLoader group3subj4 = new TestgrouperLoader(
        "loader:group3_systemOfRecord", SubjectTestHelper.SUBJ4_ID, null);
    
    //add group5subj4, group5subj5
    TestgrouperLoader group5subj4 = new TestgrouperLoader(
        "loader:group5_systemOfRecord", SubjectTestHelper.SUBJ4_ID, null);
    TestgrouperLoader group5subj5 = new TestgrouperLoader(
        "loader:group5_systemOfRecord", SubjectTestHelper.SUBJ5_ID, null);
    
    HibernateSession.byObjectStatic().saveOrUpdate(GrouperUtil.toList(group3subj4, group5subj4, group5subj5));

    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);

    //###################################################################
    //we didnt add the attribute yet, so nothing should work yet
    
    //group1 is used in another group, so it should exist, with no members
    overallGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1", true);
    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ0));
    
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUPS_LIKE, "loader:group%_systemOfRecord");
    
    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);
    
    //###################################################################
    //make sure everything worked...
    
    //group1 is used in another group, so it should exist, with no members
    overallGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1", true);
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ6));
    
    //group2 should be removed
    overallGroup2 = GroupFinder.findByName(this.grouperSession, "loader:group2", false);
    assertNull(overallGroup2);
    
    //group3 should remove subj2 and add subj4
    overallGroup3 = GroupFinder.findByName(this.grouperSession, "loader:group3", true);
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ6));

    //group4 should be unchanged
    overallGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4", true);
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ6));
    
    //group5 should be added, with subj4 and subj5
    overallGroup5 = GroupFinder.findByName(this.grouperSession, "loader:group5", true);
    assertFalse(overallGroup5.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup5.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup5.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup5.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup5.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(overallGroup5.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup5.hasMember(SubjectTestHelper.SUBJ6));

    //group6 should be removed, and the excludes, but not the includes
    group6includes = GroupFinder.findByName(this.grouperSession, "loader:group6_includes", true);
    assertTrue(group6includes.hasMember(SubjectTestHelper.SUBJ9));

    overallGroup6 = GroupFinder.findByName(this.grouperSession, "loader:group6", false);
    assertNull(overallGroup6);
    
    Group group6excludes = GroupFinder.findByName(this.grouperSession, "loader:group6_excludes", false);
    assertNull(group6excludes);

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    try {
      GrouperDdlUtils.internal_printDdlUpdateMessage = false;
      this.grouperSession = GrouperSession.startRootSession();
      
      ensureTestgrouperLoaderTables();
  
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperLoader").executeUpdate();
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperLoaderGroups").executeUpdate();
      
      GrouperStartup.initLoaderType();
      
      setupTestConfigForIncludeExclude();
      
      GrouperStartup.initIncludeExcludeType();

      GrouperCheckConfig.checkAttributes();
      
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.attribute.rootStem", "my:attrRoot");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.attribute.loader.autoconfigure", "true");

    
    //override whatever is in the config
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("loader.autoadd.typesAttributes", "true");
  }

  /**
   * 
   */
  public void ensureTestgrouperLoaderTables() {
    //we need to delete the test table if it is there, and create a new one
    //drop field id col, first drop foreign keys
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();

        {
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"testgrouper_loader");
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
              Types.VARCHAR, "255", true, true);
  
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "hibernate_version_number", 
              Types.BIGINT, "12", false, true);
  
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "col1", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "col2", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "col3", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
              "testgrouper_loader", "sample table that can be used by loader");
      
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_loader", "col1", 
              "col1");
      
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_loader", "col2", 
              "col2");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_loader", "col3", 
              "col3");
        }
        {      
          Table loaderGroupsTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"testgrouper_loader_groups");
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderGroupsTable, "id", 
              Types.VARCHAR, "255", true, true);
  
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderGroupsTable, "hibernate_version_number", 
              Types.BIGINT, "12", false, true);
  
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderGroupsTable, "group_name", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderGroupsTable, "group_display_name", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderGroupsTable, "group_description", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
              "testgrouper_loader_groups", "sample group metadata table that can be used by loader");
      
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_loader_groups", "group_name", 
              "name of a group in loader");
      
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_loader_groups", "group_display_name", 
              "display name of group in loader");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_loader_groups", "group_description", 
              "description of group in loader");
        }
      }
      
    });
  }

  /**
   * 
   */
  public void dropTestgrouperLoaderTables() {
    //we need to delete the test table if it is there, and create a new one
    //drop field id col, first drop foreign keys
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();

        {
          Table loaderTable = database.findTable("testgrouper_loader");
          
          if (loaderTable != null) {
            database.removeTable(loaderTable);
          }
        }
        {
          Table loaderGroupsTable = database.findTable("testgrouper_loader_groups");
          
          if (loaderGroupsTable != null) {
            database.removeTable(loaderGroupsTable);
          }
          
        }
        
      }
      
    });
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    dropTestgrouperLoaderTables();
    GrouperSession.stopQuietly(this.grouperSession);
    GrouperDdlUtils.internal_printDdlUpdateMessage = true;

  }

  /**
   * test the loader
   * @throws Exception 
   */
  public void testLoaderAttributeDef() throws Exception {
    
    List<GrouperAPI> testDataList = new ArrayList<GrouperAPI>();
    
    TestgrouperLoader aToB = new TestgrouperLoader("a:a", "a:b:b", null);
    testDataList.add(aToB);
    TestgrouperLoader aToC = new TestgrouperLoader("a:a", "a:c:c", null);
    testDataList.add(aToC);
    TestgrouperLoader bToD = new TestgrouperLoader("a:b:b", "a:b:d:d", null);
    testDataList.add(bToD);
  
    TestgrouperLoaderGroups attributeDefNameA = new TestgrouperLoaderGroups("a:a", 
        "theA:theA", "This is the a attributeDefName");
    testDataList.add(attributeDefNameA);
    TestgrouperLoaderGroups attributeDefNameB = new TestgrouperLoaderGroups("a:b:b", 
        "theA:theB:theB", "This is the b attributeDefName");
    testDataList.add(attributeDefNameB);
    TestgrouperLoaderGroups attributeDefNameC = new TestgrouperLoaderGroups("a:c:c", 
        "theA:theC:theC", "This is the c attributeDefName");
    testDataList.add(attributeDefNameC);
    TestgrouperLoaderGroups attributeDefNameD = new TestgrouperLoaderGroups("a:b:d:d", 
        "theA:theB:theD:theD", "This is the d attributeDefName");
    testDataList.add(attributeDefNameD);
    TestgrouperLoaderGroups attributeDefNameE = new TestgrouperLoaderGroups("a:b:e:e", 
        "theA:theB:theE:theE", "This is the e attributeDefName");
    testDataList.add(attributeDefNameE);
    TestgrouperLoaderGroups attributeDefNameF = new TestgrouperLoaderGroups("a:f:f", 
        "theA:theF:theF", "This is the f attributeDefName");
    testDataList.add(attributeDefNameF);
    
    HibernateSession.byObjectStatic().saveOrUpdate(testDataList);
  
    //lets add an attributeDef which will load these
    AttributeDef orgsAttributeDef = new AttributeDefSave(this.grouperSession)
      .assignName("loader:orgs").assignCreateParentStemsIfNotExist(true).save();

    //assign the type
    orgsAttributeDef.getAttributeDelegate().assignAttributeByName(
        GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoader");

    //now we can configure the loader
    orgsAttributeDef.getAttributeValueDelegate().assignValue(
        GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderType", "ATTR_SQL_SIMPLE");
    orgsAttributeDef.getAttributeValueDelegate().assignValue(
        GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderQuartzCron", "0 0 0 0 0 ?");
    orgsAttributeDef.getAttributeValueDelegate().assignValue(
        GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderAttrQuery", 
        "select group_name as attr_name, group_display_name as attr_display_name, " +
        "group_description as attr_description from testgrouper_loader_groups");
    orgsAttributeDef.getAttributeValueDelegate().assignValue(
        GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderAttrSetQuery", 
        "select col1 as if_has_attr_name, col2 as then_has_attr_name from testgrouper_loader");
    
    //we should have 0 attributeDefNames now
    Set<AttributeDefName> attributeDefNames = GrouperDAOFactory.getFactory().getAttributeDefName().findByAttributeDef(orgsAttributeDef.getId());
    
    assertEquals(0, attributeDefNames.size());
    
    //we should have 1 action
    Set<String> actionNames = orgsAttributeDef.getAttributeDefActionDelegate().allowedActionStrings();
    
    assertEquals(1, actionNames.size());
    assertEquals("assign", actionNames.iterator().next());
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = GrouperLoader._internal_runJobOnceForAttributeDef(this.grouperSession, orgsAttributeDef);

    //now we should have 6 orgs and 3 relationships
    List<AttributeDefName> attributeDefNamesList = new ArrayList<AttributeDefName>(GrouperDAOFactory.getFactory().getAttributeDefName().findByAttributeDef(orgsAttributeDef.getId()));
    
    Map<String, AttributeDefName> attributeDefNamesById = new HashMap<String, AttributeDefName>();
    for (AttributeDefName attributeDefName : attributeDefNamesList) {
      attributeDefNamesById.put(attributeDefName.getId(), attributeDefName);
    }
    
    
    assertEquals(6, attributeDefNamesList.size());
    assertEquals("a:a", attributeDefNamesList.get(0).getName());
    assertEquals("a:b:b", attributeDefNamesList.get(1).getName());
    assertEquals("a:b:d:d", attributeDefNamesList.get(2).getName());
    assertEquals("a:b:e:e", attributeDefNamesList.get(3).getName());
    assertEquals("a:c:c", attributeDefNamesList.get(4).getName());
    assertEquals("a:f:f", attributeDefNamesList.get(5).getName());

    
    Set<AttributeDefNameSet> attributeDefNameSets = GrouperDAOFactory.getFactory()
      .getAttributeDefNameSet().findByDepthOneForAttributeDef(orgsAttributeDef.getId());
    
    Set<MultiKey> attributeDefNameSetMultiKeyNames = new HashSet<MultiKey>();
    
    for (AttributeDefNameSet attributeDefNameSet : attributeDefNameSets) {
      AttributeDefName ifHasAttributeDefName = attributeDefNamesById.get(attributeDefNameSet.getIfHasAttributeDefNameId());
      AttributeDefName thenHasAttributeDefName = attributeDefNamesById.get(attributeDefNameSet.getThenHasAttributeDefNameId());
      attributeDefNameSetMultiKeyNames.add(new MultiKey(ifHasAttributeDefName.getName(), thenHasAttributeDefName.getName()));
    }
    
    assertEquals(3, attributeDefNameSets.size());
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:a", "a:b:b")));
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:a", "a:c:c")));
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:b:b", "a:b:d:d")));
    
    assertEquals(9, hib3GrouperLoaderLog.getTotalCount().intValue());
    assertEquals(9, hib3GrouperLoaderLog.getInsertCount().intValue());
    assertEquals(0, hib3GrouperLoaderLog.getUpdateCount().intValue());
    assertEquals(0, hib3GrouperLoaderLog.getDeleteCount().intValue());

    //#########################################
    //## do nothing, should do nothing
    
    hib3GrouperLoaderLog = GrouperLoader._internal_runJobOnceForAttributeDef(this.grouperSession, orgsAttributeDef);

    //now we should have 6 orgs and 3 relationships
    attributeDefNamesList = new ArrayList<AttributeDefName>(GrouperDAOFactory.getFactory().getAttributeDefName().findByAttributeDef(orgsAttributeDef.getId()));
    
    attributeDefNamesById = new HashMap<String, AttributeDefName>();
    for (AttributeDefName attributeDefName : attributeDefNamesList) {
      attributeDefNamesById.put(attributeDefName.getId(), attributeDefName);
    }
    
    
    assertEquals(6, attributeDefNamesList.size());
    assertEquals("a:a", attributeDefNamesList.get(0).getName());
    assertEquals("a:b:b", attributeDefNamesList.get(1).getName());
    assertEquals("a:b:d:d", attributeDefNamesList.get(2).getName());
    assertEquals("a:b:e:e", attributeDefNamesList.get(3).getName());
    assertEquals("a:c:c", attributeDefNamesList.get(4).getName());
    assertEquals("a:f:f", attributeDefNamesList.get(5).getName());

    
    attributeDefNameSets = GrouperDAOFactory.getFactory()
      .getAttributeDefNameSet().findByDepthOneForAttributeDef(orgsAttributeDef.getId());
    
    attributeDefNameSetMultiKeyNames = new HashSet<MultiKey>();
    
    for (AttributeDefNameSet attributeDefNameSet : attributeDefNameSets) {
      AttributeDefName ifHasAttributeDefName = attributeDefNamesById.get(attributeDefNameSet.getIfHasAttributeDefNameId());
      AttributeDefName thenHasAttributeDefName = attributeDefNamesById.get(attributeDefNameSet.getThenHasAttributeDefNameId());
      attributeDefNameSetMultiKeyNames.add(new MultiKey(ifHasAttributeDefName.getName(), thenHasAttributeDefName.getName()));
    }
    
    assertEquals(3, attributeDefNameSets.size());
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:a", "a:b:b")));
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:a", "a:c:c")));
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:b:b", "a:b:d:d")));
    
    assertEquals(9, hib3GrouperLoaderLog.getTotalCount().intValue());
    assertEquals(0, hib3GrouperLoaderLog.getInsertCount().intValue());
    assertEquals(0, hib3GrouperLoaderLog.getUpdateCount().intValue());
    assertEquals(0, hib3GrouperLoaderLog.getDeleteCount().intValue());
    

    //#########################################
    //## Add one, delete one (update if applicable)
    
    HibernateSession.byObjectStatic().delete(aToC);
    TestgrouperLoader bToE = new TestgrouperLoader("a:b:b", "a:b:e:e", null);
    HibernateSession.byObjectStatic().saveOrUpdate(bToE);
  
    attributeDefNameA.setGroupDescription("new description");
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDefNameA);
    
    HibernateSession.byObjectStatic().delete(attributeDefNameF);
    TestgrouperLoaderGroups attributeDefNameG = new TestgrouperLoaderGroups("a:g:g", 
        "theA:theG:theG", "This is the g attributeDefName");
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDefNameG);
    
    hib3GrouperLoaderLog = GrouperLoader._internal_runJobOnceForAttributeDef(this.grouperSession, orgsAttributeDef);

    //now we should have 7 orgs and 3 relationships
    attributeDefNamesList = new ArrayList<AttributeDefName>(GrouperDAOFactory.getFactory().getAttributeDefName().findByAttributeDef(orgsAttributeDef.getId()));
    
    attributeDefNamesById = new HashMap<String, AttributeDefName>();
    for (AttributeDefName attributeDefName : attributeDefNamesList) {
      attributeDefNamesById.put(attributeDefName.getId(), attributeDefName);
    }
    
    //not deleted yet since no like string
    assertEquals(7, attributeDefNamesList.size());
    assertEquals("a:a", attributeDefNamesList.get(0).getName());
    assertEquals("a:b:b", attributeDefNamesList.get(1).getName());
    assertEquals("a:b:d:d", attributeDefNamesList.get(2).getName());
    assertEquals("a:b:e:e", attributeDefNamesList.get(3).getName());
    assertEquals("a:c:c", attributeDefNamesList.get(4).getName());
    assertEquals("a:f:f", attributeDefNamesList.get(5).getName());
    assertEquals("a:g:g", attributeDefNamesList.get(6).getName());

    
    attributeDefNameSets = GrouperDAOFactory.getFactory()
      .getAttributeDefNameSet().findByDepthOneForAttributeDef(orgsAttributeDef.getId());
    
    attributeDefNameSetMultiKeyNames = new HashSet<MultiKey>();
    
    for (AttributeDefNameSet attributeDefNameSet : attributeDefNameSets) {
      AttributeDefName ifHasAttributeDefName = attributeDefNamesById.get(attributeDefNameSet.getIfHasAttributeDefNameId());
      AttributeDefName thenHasAttributeDefName = attributeDefNamesById.get(attributeDefNameSet.getThenHasAttributeDefNameId());
      attributeDefNameSetMultiKeyNames.add(new MultiKey(ifHasAttributeDefName.getName(), thenHasAttributeDefName.getName()));
    }
    
    assertEquals(4, attributeDefNameSets.size());
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:a", "a:b:b")));
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:a", "a:c:c")));
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:b:b", "a:b:e:e")));
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:b:b", "a:b:d:d")));
    
    //this is 9 since it doesnt count existing values...
    assertEquals(9, hib3GrouperLoaderLog.getTotalCount().intValue());
    assertEquals(2, hib3GrouperLoaderLog.getInsertCount().intValue());
    assertEquals(1, hib3GrouperLoaderLog.getUpdateCount().intValue());
    assertEquals(0, hib3GrouperLoaderLog.getDeleteCount().intValue());


    //#########################################
    //## Set the like string, and it should delete
    
    orgsAttributeDef.getAttributeValueDelegate().assignValue(
        GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderAttrsLike", "%");

    hib3GrouperLoaderLog = GrouperLoader._internal_runJobOnceForAttributeDef(this.grouperSession, orgsAttributeDef);

    //now we should have 6 orgs and 3 relationships
    attributeDefNamesList = new ArrayList<AttributeDefName>(GrouperDAOFactory.getFactory().getAttributeDefName().findByAttributeDef(orgsAttributeDef.getId()));
    
    attributeDefNamesById = new HashMap<String, AttributeDefName>();
    for (AttributeDefName attributeDefName : attributeDefNamesList) {
      attributeDefNamesById.put(attributeDefName.getId(), attributeDefName);
    }
    
    //not deleted yet since no like string
    assertEquals(6, attributeDefNamesList.size());
    assertEquals("a:a", attributeDefNamesList.get(0).getName());
    assertEquals("a:b:b", attributeDefNamesList.get(1).getName());
    assertEquals("a:b:d:d", attributeDefNamesList.get(2).getName());
    assertEquals("a:b:e:e", attributeDefNamesList.get(3).getName());
    assertEquals("a:c:c", attributeDefNamesList.get(4).getName());
    assertEquals("a:g:g", attributeDefNamesList.get(5).getName());

    
    attributeDefNameSets = GrouperDAOFactory.getFactory()
      .getAttributeDefNameSet().findByDepthOneForAttributeDef(orgsAttributeDef.getId());
    
    attributeDefNameSetMultiKeyNames = new HashSet<MultiKey>();
    
    for (AttributeDefNameSet attributeDefNameSet : attributeDefNameSets) {
      AttributeDefName ifHasAttributeDefName = attributeDefNamesById.get(attributeDefNameSet.getIfHasAttributeDefNameId());
      AttributeDefName thenHasAttributeDefName = attributeDefNamesById.get(attributeDefNameSet.getThenHasAttributeDefNameId());
      attributeDefNameSetMultiKeyNames.add(new MultiKey(ifHasAttributeDefName.getName(), thenHasAttributeDefName.getName()));
    }
    
    assertEquals(3, attributeDefNameSets.size());
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:a", "a:b:b")));
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:b:b", "a:b:e:e")));
    assertTrue(attributeDefNameSetMultiKeyNames.contains(new MultiKey("a:b:b", "a:b:d:d")));
    
    assertEquals(9, hib3GrouperLoaderLog.getTotalCount().intValue());
    assertEquals(0, hib3GrouperLoaderLog.getInsertCount().intValue());
    assertEquals(0, hib3GrouperLoaderLog.getUpdateCount().intValue());
    assertEquals(2, hib3GrouperLoaderLog.getDeleteCount().intValue());

    //##########################
    // try different like string to run query
    
    orgsAttributeDef.getAttributeValueDelegate().assignValue(
        GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderAttrsLike", "%%");

    hib3GrouperLoaderLog = GrouperLoader._internal_runJobOnceForAttributeDef(this.grouperSession, orgsAttributeDef);

    
//    Group overallGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1", true);
//    assertEquals("The loader:group 1", overallGroup1.getDisplayName());
//    Group systemOfRecordGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1_systemOfRecord", true);
//    assertEquals("This is the first group", systemOfRecordGroup1.getDescription());
//    assertEquals("The loader:group 1 system of record", systemOfRecordGroup1.getDisplayName());
//    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ0));
//    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ1));
//    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ2));
//    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ3));
//    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ4));
//    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ5));
//    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ6));
    
  
  
  }

  /**
     * test the loader
     * @throws Exception 
     */
    public void testLoaderAttributeDefActions() throws Exception {
      
      List<GrouperAPI> testDataList = new ArrayList<GrouperAPI>();
      
      TestgrouperLoader aToB = new TestgrouperLoader("a:a", "a:b:b", null);
      testDataList.add(aToB);
      TestgrouperLoader aToC = new TestgrouperLoader("a:a", "a:c:c", null);
      testDataList.add(aToC);
      TestgrouperLoader bToD = new TestgrouperLoader("a:b:b", "a:b:d:d", null);
      testDataList.add(bToD);
    
      TestgrouperLoaderGroups attributeDefNameA = new TestgrouperLoaderGroups("a:a", 
          null, null);
      testDataList.add(attributeDefNameA);
      TestgrouperLoaderGroups attributeDefNameB = new TestgrouperLoaderGroups("a:b:b", 
          null, null);
      testDataList.add(attributeDefNameB);
      TestgrouperLoaderGroups attributeDefNameC = new TestgrouperLoaderGroups("a:c:c", 
          null, null);
      testDataList.add(attributeDefNameC);
      TestgrouperLoaderGroups attributeDefNameD = new TestgrouperLoaderGroups("a:b:d:d", 
          null, null);
      testDataList.add(attributeDefNameD);
      TestgrouperLoaderGroups attributeDefNameE = new TestgrouperLoaderGroups("a:b:e:e", 
          null, null);
      testDataList.add(attributeDefNameE);
      TestgrouperLoaderGroups attributeDefNameF = new TestgrouperLoaderGroups("a:f:f", 
          null, null);
      testDataList.add(attributeDefNameF);
      
      HibernateSession.byObjectStatic().saveOrUpdate(testDataList);
    
      //lets add an attributeDef which will load these
      AttributeDef orgsAttributeDef = new AttributeDefSave(this.grouperSession)
        .assignName("loader:orgs").assignCreateParentStemsIfNotExist(true).save();
  
      //assign the type
      orgsAttributeDef.getAttributeDelegate().assignAttributeByName(
          GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoader");
  
      //now we can configure the loader
      orgsAttributeDef.getAttributeValueDelegate().assignValue(
          GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderType", "ATTR_SQL_SIMPLE");
      orgsAttributeDef.getAttributeValueDelegate().assignValue(
          GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderQuartzCron", "0 0 0 0 0 ?");
      orgsAttributeDef.getAttributeValueDelegate().assignValue(
          GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderActionQuery", 
          "select group_name as action_name from testgrouper_loader_groups");
      orgsAttributeDef.getAttributeValueDelegate().assignValue(
          GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderActionSetQuery", 
          "select col1 as if_has_action_name, col2 as then_has_action_name from testgrouper_loader");
      
      //we should have 0 attributeDefNames now
      Set<AttributeAssignAction> attributeActions = orgsAttributeDef.getAttributeDefActionDelegate().allowedActions();
      
      assertEquals(1, attributeActions.size());
      
      //we should have 1 action
      Set<String> actionNames = orgsAttributeDef.getAttributeDefActionDelegate().allowedActionStrings();
      
      assertEquals(1, actionNames.size());
      assertEquals("assign", actionNames.iterator().next());
      
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = GrouperLoader._internal_runJobOnceForAttributeDef(this.grouperSession, orgsAttributeDef);
  
      //its cached in the delegate, so get another
      orgsAttributeDef = AttributeDefFinder.findById(orgsAttributeDef.getUuid(), true);
      
      //now we should have 6 actions and 3 relationships
      List<AttributeAssignAction> attributeActionsList = new ArrayList<AttributeAssignAction>(orgsAttributeDef.getAttributeDefActionDelegate().allowedActions());
      
      Map<String, AttributeAssignAction> attributeActionsById = new HashMap<String, AttributeAssignAction>();
      for (AttributeAssignAction attributeAction : attributeActionsList) {
        attributeActionsById.put(attributeAction.getId(), attributeAction);
      }
      
      
      assertEquals(6, attributeActionsList.size());
      assertEquals("a:a", attributeActionsList.get(0).getName());
      assertEquals("a:b:b", attributeActionsList.get(1).getName());
      assertEquals("a:b:d:d", attributeActionsList.get(2).getName());
      assertEquals("a:b:e:e", attributeActionsList.get(3).getName());
      assertEquals("a:c:c", attributeActionsList.get(4).getName());
      assertEquals("a:f:f", attributeActionsList.get(5).getName());
  
      
      Set<AttributeAssignActionSet> attributeActionSets = GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSet().findByDepthOneForAttributeDef(orgsAttributeDef.getId());
      
      Set<MultiKey> attributeActionSetMultiKeyNames = new HashSet<MultiKey>();
      
      for (AttributeAssignActionSet attributeActionSet : attributeActionSets) {
        AttributeAssignAction ifHasAction = attributeActionsById.get(attributeActionSet.getIfHasAttrAssignActionId());
        AttributeAssignAction thenHasAction = attributeActionsById.get(attributeActionSet.getThenHasAttrAssignActionId());
        attributeActionSetMultiKeyNames.add(new MultiKey(ifHasAction.getName(), thenHasAction.getName()));
      }
      
      assertEquals(3, attributeActionSets.size());
      assertTrue(attributeActionSetMultiKeyNames.contains(new MultiKey("a:a", "a:b:b")));
      assertTrue(attributeActionSetMultiKeyNames.contains(new MultiKey("a:a", "a:c:c")));
      assertTrue(attributeActionSetMultiKeyNames.contains(new MultiKey("a:b:b", "a:b:d:d")));
      
      assertEquals(9, hib3GrouperLoaderLog.getTotalCount().intValue());
      assertEquals(9, hib3GrouperLoaderLog.getInsertCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getUpdateCount().intValue());
      //deletes "assign"
      assertEquals(1, hib3GrouperLoaderLog.getDeleteCount().intValue());
  
      //#########################################
      //## do nothing, should do nothing
      
      hib3GrouperLoaderLog = GrouperLoader._internal_runJobOnceForAttributeDef(this.grouperSession, orgsAttributeDef);
  
      //its cached in the delegate, so get another
      orgsAttributeDef = AttributeDefFinder.findById(orgsAttributeDef.getUuid(), true);
      
      //now we should have 6 orgs and 3 relationships
      attributeActionsList = new ArrayList<AttributeAssignAction>(orgsAttributeDef.getAttributeDefActionDelegate().allowedActions());
      
      attributeActionsById = new HashMap<String, AttributeAssignAction>();
      for (AttributeAssignAction attributeAction : attributeActionsList) {
        attributeActionsById.put(attributeAction.getId(), attributeAction);
      }
      
      
      assertEquals(6, attributeActionsList.size());
      assertEquals("a:a", attributeActionsList.get(0).getName());
      assertEquals("a:b:b", attributeActionsList.get(1).getName());
      assertEquals("a:b:d:d", attributeActionsList.get(2).getName());
      assertEquals("a:b:e:e", attributeActionsList.get(3).getName());
      assertEquals("a:c:c", attributeActionsList.get(4).getName());
      assertEquals("a:f:f", attributeActionsList.get(5).getName());
  
      
      attributeActionSets = GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSet().findByDepthOneForAttributeDef(orgsAttributeDef.getId());
      
      attributeActionSetMultiKeyNames = new HashSet<MultiKey>();
      
      for (AttributeAssignActionSet attributeActionSet : attributeActionSets) {
        AttributeAssignAction ifHasAction = attributeActionsById.get(attributeActionSet.getIfHasAttrAssignActionId());
        AttributeAssignAction thenHasAction = attributeActionsById.get(attributeActionSet.getThenHasAttrAssignActionId());
        attributeActionSetMultiKeyNames.add(new MultiKey(ifHasAction.getName(), thenHasAction.getName()));
      }      

      assertEquals(3, attributeActionSets.size());
      assertTrue(attributeActionSetMultiKeyNames.contains(new MultiKey("a:a", "a:b:b")));
      assertTrue(attributeActionSetMultiKeyNames.contains(new MultiKey("a:a", "a:c:c")));
      assertTrue(attributeActionSetMultiKeyNames.contains(new MultiKey("a:b:b", "a:b:d:d")));
      
      assertEquals(9, hib3GrouperLoaderLog.getTotalCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getInsertCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getUpdateCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getDeleteCount().intValue());
      
  
      //#########################################
      //## Add one, delete one (update if applicable)
      
      HibernateSession.byObjectStatic().delete(aToC);
      TestgrouperLoader bToE = new TestgrouperLoader("a:b:b", "a:b:e:e", null);
      HibernateSession.byObjectStatic().saveOrUpdate(bToE);
    
      attributeDefNameA.setGroupDescription("new description");
      HibernateSession.byObjectStatic().saveOrUpdate(attributeDefNameA);
      
      HibernateSession.byObjectStatic().delete(attributeDefNameF);
      TestgrouperLoaderGroups attributeDefNameG = new TestgrouperLoaderGroups("a:g:g", 
          "theA:theG:theG", "This is the g attributeDefName");
      HibernateSession.byObjectStatic().saveOrUpdate(attributeDefNameG);
      
      hib3GrouperLoaderLog = GrouperLoader._internal_runJobOnceForAttributeDef(this.grouperSession, orgsAttributeDef);
  
      //its cached in the delegate, so get another
      orgsAttributeDef = AttributeDefFinder.findById(orgsAttributeDef.getUuid(), true);
      
      //now we should have 7 orgs and 3 relationships
      attributeActionsList = new ArrayList<AttributeAssignAction>(orgsAttributeDef.getAttributeDefActionDelegate().allowedActions());
      
      attributeActionsById = new HashMap<String, AttributeAssignAction>();
      for (AttributeAssignAction attributeAction : attributeActionsList) {
        attributeActionsById.put(attributeAction.getId(), attributeAction);
      }
      
      assertEquals(6, attributeActionsList.size());
      assertEquals("a:a", attributeActionsList.get(0).getName());
      assertEquals("a:b:b", attributeActionsList.get(1).getName());
      assertEquals("a:b:d:d", attributeActionsList.get(2).getName());
      assertEquals("a:b:e:e", attributeActionsList.get(3).getName());
      assertEquals("a:c:c", attributeActionsList.get(4).getName());
      assertEquals("a:g:g", attributeActionsList.get(5).getName());
  
      
      attributeActionSets = GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSet().findByDepthOneForAttributeDef(orgsAttributeDef.getId());
      
      attributeActionSetMultiKeyNames = new HashSet<MultiKey>();
      
      for (AttributeAssignActionSet attributeActionSet : attributeActionSets) {
        AttributeAssignAction ifHasAction = attributeActionsById.get(attributeActionSet.getIfHasAttrAssignActionId());
        AttributeAssignAction thenHasAction = attributeActionsById.get(attributeActionSet.getThenHasAttrAssignActionId());
        attributeActionSetMultiKeyNames.add(new MultiKey(ifHasAction.getName(), thenHasAction.getName()));
      }      
      
      assertEquals(3, attributeActionSets.size());
      assertTrue(attributeActionSetMultiKeyNames.contains(new MultiKey("a:a", "a:b:b")));
      assertTrue(attributeActionSetMultiKeyNames.contains(new MultiKey("a:b:b", "a:b:e:e")));
      assertTrue(attributeActionSetMultiKeyNames.contains(new MultiKey("a:b:b", "a:b:d:d")));
      
      //this is 9 since it doesnt count existing values...
      assertEquals(9, hib3GrouperLoaderLog.getTotalCount().intValue());
      assertEquals(2, hib3GrouperLoaderLog.getInsertCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getUpdateCount().intValue());
      assertEquals(2, hib3GrouperLoaderLog.getDeleteCount().intValue());
  
  
    
    }

  /**
   * test the loader
   * @throws Exception 
   */
  public void testLoaderSubjectIdentifier() throws Exception {
    
    List<TestgrouperLoader> testDataList = new ArrayList<TestgrouperLoader>();
    
    TestgrouperLoader subj0 = new TestgrouperLoader("id.test.subject.0", null, null);
    testDataList.add(subj0);
    TestgrouperLoader subj1 = new TestgrouperLoader("id.test.subject.1", null, null);
    testDataList.add(subj1);
    TestgrouperLoader subj2 = new TestgrouperLoader("id.test.subject.2", null, null);
    testDataList.add(subj2);
  
    HibernateSession.byObjectStatic().saveOrUpdate(testDataList);
  
    //lets add a group which will load these
    Group loaderGroup = Group.saveGroup(this.grouperSession, null, null, 
        "loader:owner",null, null, null, true);
    loaderGroup.addType(GroupTypeFinder.find("grouperLoader", true));
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, 
        "select col1 as SUBJECT_IDENTIFIER from testgrouper_loader");
    
    System.out.println(GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup));
    System.out.println(GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup));
    
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ1));
  
  }

  /**
   * test the loader
   * @throws Exception 
   */
  public void testLoaderUnresolvable() throws Exception {
    
    List<TestgrouperLoader> testDataList = new ArrayList<TestgrouperLoader>();
    
    TestgrouperLoader subj0 = new TestgrouperLoader("test.subject.0", null, null);
    testDataList.add(subj0);
    TestgrouperLoader subj1 = new TestgrouperLoader("test.subject.1", null, null);
    testDataList.add(subj1);
    TestgrouperLoader subj2 = new TestgrouperLoader("test.subject.2", null, null);
    testDataList.add(subj2);
  
    HibernateSession.byObjectStatic().saveOrUpdate(testDataList);
  
    //lets add a group which will load these
    Group loaderGroup = Group.saveGroup(this.grouperSession, null, null, 
        "loader:owner",null, null, null, true);
    loaderGroup.addType(GroupTypeFinder.find("grouperLoader", true));
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, 
        "select col1 as SUBJECT_ID from testgrouper_loader");
    
    System.out.println(GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup));
    System.out.println(GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup));
    
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ1));
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //now make a member unresolvable
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    //set the subject ID to make unresolvable
    member.setSubjectId("whateverYall");
    member.store();

    assertFalse(loaderGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ1));
    
    assertEquals(3, loaderGroup.getMembers().size());
    
    //run it again, it should remove that person, and add another
    System.out.println(GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup));
    System.out.println(GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup));
    
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(loaderGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertEquals(3, loaderGroup.getMembers().size());
    
  }
  
  
//  /**
//   * test the loader to make sure you can apply security to include/exclude
//   * without removing custom members
//   * @throws Exception 
//   */
//  public void testLoaderTypesGroupMetaDontRemove() throws Exception {
//    
//    List<GrouperAPI> testDataList = new ArrayList<GrouperAPI>();
//    
//    TestgrouperLoader group1subj0 = new TestgrouperLoader("loader:group1_systemOfRecord", SubjectTestHelper.SUBJ0_ID, null);
//    testDataList.add(group1subj0);
//    TestgrouperLoader group1subj1 = new TestgrouperLoader("loader:group1_systemOfRecord", SubjectTestHelper.SUBJ1_ID, null);
//    testDataList.add(group1subj1);
//    TestgrouperLoader group2subj1 = new TestgrouperLoader("loader:group2_systemOfRecord", SubjectTestHelper.SUBJ1_ID, null);
//    testDataList.add(group2subj1);
//    TestgrouperLoader group2subj2 = new TestgrouperLoader("loader:group2_systemOfRecord", SubjectTestHelper.SUBJ2_ID, null);
//    testDataList.add(group2subj2);
//    TestgrouperLoader group3subj2 = new TestgrouperLoader("loader:group3_systemOfRecord", SubjectTestHelper.SUBJ2_ID, null);
//    testDataList.add(group3subj2);
//    TestgrouperLoader group3subj3 = new TestgrouperLoader("loader:group3_systemOfRecord", SubjectTestHelper.SUBJ3_ID, null);
//    testDataList.add(group3subj3);
//    TestgrouperLoader group4subj3 = new TestgrouperLoader("loader:group4_systemOfRecord", SubjectTestHelper.SUBJ3_ID, null);
//    testDataList.add(group4subj3);
//    TestgrouperLoader group4subj4 = new TestgrouperLoader("loader:group4_systemOfRecord", SubjectTestHelper.SUBJ4_ID, null);
//    testDataList.add(group4subj4);
//    TestgrouperLoader group6subj5 = new TestgrouperLoader("loader:group6_systemOfRecord", SubjectTestHelper.SUBJ5_ID, null);
//    testDataList.add(group6subj5);
//    TestgrouperLoader group6subj6 = new TestgrouperLoader("loader:group6_systemOfRecord", SubjectTestHelper.SUBJ6_ID, null);
//    testDataList.add(group6subj6);
//  
//    TestgrouperLoaderGroups group1meta = new TestgrouperLoaderGroups("loader:group1_systemOfRecord", 
//        "The loader:group 1 system of record", "This is the first group");
//    testDataList.add(group1meta);
//    TestgrouperLoaderGroups group2meta = new TestgrouperLoaderGroups("loader:group2_systemOfRecord", 
//        "The loader:group 2 system of record", null);
//    testDataList.add(group2meta);
//    TestgrouperLoaderGroups group3meta = new TestgrouperLoaderGroups("loader:group3_systemOfRecord", 
//        null, "This is the third group");
//    testDataList.add(group3meta);
//    
//    HibernateSession.byObjectStatic().saveOrUpdate(testDataList);
//  
//    //lets add a group which will load these
//    Group loaderGroup = Group.saveGroup(this.grouperSession, null, null, 
//        "loader2:owner",null, null, null, true);
//    loaderGroup.addType(GroupTypeFinder.find("grouperLoader"));
//    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, 
//        "select col1 as GROUP_NAME, col2 as SUBJECT_ID from testgrouper_loader");
//    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_TYPES,
//        "addIncludeExclude");
//    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_QUERY,
//      "select group_name, group_display_name, group_description from testgrouper_loader_groups");
//    
//    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);
//    
//    Group overallGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1");
//    assertEquals("The loader:group 1", overallGroup1.getDisplayName());
//    Group systemOfRecordGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1_systemOfRecord");
//    assertEquals("This is the first group", systemOfRecordGroup1.getDescription());
//    assertEquals("The loader:group 1 system of record", systemOfRecordGroup1.getDisplayName());
//    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ0));
//    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ1));
//    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ2));
//    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ3));
//    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ4));
//    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ5));
//    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ6));
//    
//    Group overallGroup2 = GroupFinder.findByName(this.grouperSession, "loader:group2");
//  
//    assertEquals("The loader:group 2", overallGroup2.getDisplayName());
//    Group systemOfRecordGroup2 = GroupFinder.findByName(this.grouperSession, "loader:group2_systemOfRecord");
//    assertTrue(systemOfRecordGroup2.getDescription().length() > 0);
//    assertEquals("The loader:group 2 system of record", systemOfRecordGroup2.getDisplayName());
//  
//    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ0));
//    assertTrue(overallGroup2.hasMember(SubjectTestHelper.SUBJ1));
//    assertTrue(overallGroup2.hasMember(SubjectTestHelper.SUBJ2));
//    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ3));
//    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ4));
//    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ5));
//    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ6));
//    
//    Group overallGroup3 = GroupFinder.findByName(this.grouperSession, "loader:group3");
//    assertEquals("The loader:group3", overallGroup3.getDisplayName());
//    Group systemOfRecordGroup3 = GroupFinder.findByName(this.grouperSession, "loader:group3_systemOfRecord");
//    assertEquals("This is the third group", systemOfRecordGroup3.getDescription());
//    assertEquals("The loader:group3_systemOfRecord", systemOfRecordGroup3.getDisplayName());
//  
//    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ0));
//    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ1));
//    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ2));
//    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ3));
//    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ4));
//    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ5));
//    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ6));
//  
//    Group overallGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4");
//    assertEquals("The loader:group4", overallGroup4.getDisplayName());
//    Group systemOfRecordGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4_systemOfRecord");
//    assertTrue(systemOfRecordGroup4.getDescription().length() > 0);
//    assertEquals("The loader:group4_systemOfRecord", systemOfRecordGroup4.getDisplayName());
//    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ0));
//    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ1));
//    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ2));
//    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ3));
//    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ4));
//    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ5));
//    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ6));
//  
//    Group overallGroup5 = GroupFinder.findByName(this.grouperSession, "loader:group5", false);
//    assertNull(overallGroup5);
//    
//    //lets use the includes/excludes for group6
//    Group group6includes = GroupFinder.findByName(this.grouperSession, "loader:group6_includes");
//    group6includes.addMember(SubjectTestHelper.SUBJ9);
//  
//    Group overallGroup6 = GroupFinder.findByName(this.grouperSession, "loader:group6");
//    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ0));
//    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ1));
//    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ2));
//    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ3));
//    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ4));
//    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ5));
//    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ6));
//    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ9));
//    
//    //lets make sure the security groups dont exist
//    new StemSave(this.grouperSession).assignName("loaderSecurity")
//      .assignSaveMode(SaveMode.INSERT).saveUnchecked();
//    
//    Group admins = GroupFinder.findByName(this.grouperSession, "loaderSecurity:admins", false);
//    assertNull(admins);
//    Group readers = GroupFinder.findByName(this.grouperSession, "loaderSecurity:readers", false);
//    assertNull(readers);
//    Group updaters = GroupFinder.findByName(this.grouperSession, "loaderSecurity:updaters", false);
//    assertNull(updaters);
//    Group viewers = GroupFinder.findByName(this.grouperSession, "loaderSecurity:viewers", false);
//    assertNull(viewers);
//    Group optins = GroupFinder.findByName(this.grouperSession, "loaderSecurity:optins", false);
//    assertNull(optins);
//    Group optouts = GroupFinder.findByName(this.grouperSession, "loaderSecurity:optouts", false);
//    assertNull(optouts);
//  
//    //add a record to the includes
//    Group group4includes = GroupFinder.findByName(this.grouperSession, "loader:group4_includes", true);
//    group4includes.addMember(SubjectTestHelper.SUBJ8);
//    
//    //change the query to include all these groups
//    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_QUERY,
//      "select group_name, group_display_name, group_description, 'loaderSecurity:admins' as admins, " +
//      "'loaderSecurity:readers' as readers, 'loaderSecurity:viewers' as viewers, " +
//      "'loaderSecurity:updaters' as updaters, 'loaderSecurity:optins' as optins, " +
//      "'loaderSecurity:optouts' as optouts, 'T' as manage_deletes from testgrouper_loader_groups union " +
//      "select 'loader:group4_includes' as group_name, 'loader:group4 includes' as group_display_name, " +
//      "null as group_description, 'loaderSecurity:admins' as admins, " +
//      "'loaderSecurity:readers' as readers, 'loaderSecurity:viewers' as viewers, " +
//      "'loaderSecurity:updaters' as updaters, 'loaderSecurity:optins' as optins, " +
//      "'loaderSecurity:optouts' as optouts, 'F' as manage_deletes from testgrouper_loader_groups where group_name = 'loader:group4_systemOfRecord' ");
//    loaderGroup.store();
//    
//    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);
//  
//    admins = GroupFinder.findByName(this.grouperSession, "loaderSecurity:admins", false);
//    assertNotNull(admins);
//    readers = GroupFinder.findByName(this.grouperSession, "loaderSecurity:readers", false);
//    assertNotNull(readers);
//    updaters = GroupFinder.findByName(this.grouperSession, "loaderSecurity:updaters", false);
//    assertNotNull(updaters);
//    viewers = GroupFinder.findByName(this.grouperSession, "loaderSecurity:viewers", false);
//    assertNotNull(viewers);
//    optins = GroupFinder.findByName(this.grouperSession, "loaderSecurity:optins", false);
//    assertNotNull(optins);
//    optouts = GroupFinder.findByName(this.grouperSession, "loaderSecurity:optouts", false);
//    assertNotNull(optouts);
//    
//    //make sure they have the privilege
//    overallGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4");
//    assertTrue(overallGroup4.hasRead(readers.toSubject()));
//    assertFalse(overallGroup4.hasAdmin(viewers.toSubject()));
//    assertTrue(overallGroup4.hasView(viewers.toSubject()));
//    assertTrue(overallGroup4.hasUpdate(updaters.toSubject()));
//    assertTrue(overallGroup4.hasOptin(optins.toSubject()));
//    assertTrue(overallGroup4.hasOptout(optouts.toSubject()));
//    assertTrue(overallGroup4.hasAdmin(admins.toSubject()));
//    
//    Group overallGroup4includes = GroupFinder.findByName(this.grouperSession, "loader:group4_includes");
//    assertTrue(overallGroup4includes.hasRead(readers.toSubject()));
//    assertFalse(overallGroup4includes.hasAdmin(viewers.toSubject()));
//    assertTrue(overallGroup4includes.hasView(viewers.toSubject()));
//    assertTrue(overallGroup4includes.hasUpdate(updaters.toSubject()));
//    assertTrue(overallGroup4includes.hasOptin(optins.toSubject()));
//    assertTrue(overallGroup4includes.hasOptout(optouts.toSubject()));
//    assertTrue(overallGroup4includes.hasAdmin(admins.toSubject()));
//    
//    Group overallGroup4excludes = GroupFinder.findByName(this.grouperSession, "loader:group4_excludes");
//    assertFalse(overallGroup4excludes.hasRead(readers.toSubject()));
//    assertFalse(overallGroup4excludes.hasView(viewers.toSubject()));
//    assertFalse(overallGroup4excludes.hasUpdate(updaters.toSubject()));
//    assertFalse(overallGroup4excludes.hasOptin(optins.toSubject()));
//    assertFalse(overallGroup4excludes.hasOptout(optouts.toSubject()));
//    assertFalse(overallGroup4excludes.hasAdmin(admins.toSubject()));
//    
//    //make sure the member is still there
//    group4includes = GroupFinder.findByName(this.grouperSession, "loader:group4_includes", true);
//    assertTrue(group4includes.hasMember(SubjectTestHelper.SUBJ8));
//    
//    //set manage deletes to true
//    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_QUERY,
//        "select group_name, group_display_name, group_description, 'loaderSecurity:admins' as admins, " +
//        "'loaderSecurity:readers' as readers, 'loaderSecurity:viewers' as viewers, " +
//        "'loaderSecurity:updaters' as updaters, 'loaderSecurity:optins' as optins, " +
//        "'loaderSecurity:optouts' as optouts, 'T' as manage_deletes from testgrouper_loader_groups union " +
//        "select 'loader:group4_includes' as group_name, 'loader:group4 includes' as group_display_name, " +
//        "null as group_description, 'loaderSecurity:admins' as admins, " +
//        "'loaderSecurity:readers' as readers, 'loaderSecurity:viewers' as viewers, " +
//        "'loaderSecurity:updaters' as updaters, 'loaderSecurity:optins' as optins, " +
//        "'loaderSecurity:optouts' as optouts, 'T' as manage_deletes from testgrouper_loader_groups where group_name = 'loader:group4_systemOfRecord' ");
//    loaderGroup.store();
//      
//    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);
//    
//    group4includes = GroupFinder.findByName(this.grouperSession, "loader:group4_includes", true);
//    assertFalse(group4includes.hasMember(SubjectTestHelper.SUBJ8));
//
//    
//  }
  
}
