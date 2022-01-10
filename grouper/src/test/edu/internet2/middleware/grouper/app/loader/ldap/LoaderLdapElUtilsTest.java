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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader.ldap;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerIncrementalTest;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.TestgrouperLoader;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisionerTest;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.misc.GrouperFailsafe;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;


/**
 *
 */
public class LoaderLdapElUtilsTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LoaderLdapElUtilsTest("testLoaderShouldAbortListWithoutChangingMembershipsOverallMinMemberships"));
  }
  
  /**
   * @param name
   */
  public LoaderLdapElUtilsTest(String name) {
    super(name);
  }
  
  protected void setUp () {
    super.setUp();

  }

  protected void tearDown () {
    super.tearDown();
  }

  /**
   * loader job should abort when it is trying to remove more number of members than
   * configured in the loader.failsafe.maxPercentRemove and also when the group size is more than
   *  loader.failsafe.minGroupSize
   * @throws Exception 
   */
  @SuppressWarnings("deprecation")
  public void testLoaderShouldAbortWithoutChangingMembershipsSimpleIgnoreMinGroupSize() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    setupLdap();
    
    try {
      
      Group group = new GroupSave(grouperSession).assignName("test:testLdapSimpleConvertDn").assignCreateParentStemsIfNotExist(true).save();
      
      Set<Member> members = group.getMembers();
      assertEquals(0, members.size());

      AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(LoaderLdapUtils.grouperLoaderLdapAttributeDefName()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(uid=a*)");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), "0 0 6 * * ?");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), "ou=People,dc=example,dc=edu");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), "SUBTREE_SCOPE");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapServerIdName(), "personLdap");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), "personLdapSource");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), "uid");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), "subjectId");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapTypeName(), "LDAP_SIMPLE");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFailsafeUseName(), "T");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapMinGroupSizeName(), "12");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapMaxGroupPercentRemoveName(), "20");
      
      GrouperLoader.runJobOnceForGroup(grouperSession, group);

      members = group.getMembers();
      assertTrue(""+members.size(), members.size() > 900);
      
      Subject aanderson = SubjectFinder.findByIdAndSource("a-aanderson", "personLdapSource", true);
      Subject ablewis = SubjectFinder.findByIdAndSource("a-blewis", "personLdapSource", true);

      Member aandersonMember = MemberFinder.findBySubject(grouperSession, aanderson, true); 
      Member ablewisMember = MemberFinder.findBySubject(grouperSession, ablewis, true); 

      assertTrue(members.contains(aandersonMember));
      assertTrue(members.contains(ablewisMember));
      
      GrouperUtil.sleep(1000);
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(uid=a-a*)");

      try {
        GrouperLoader.runJobOnceForGroup(grouperSession, group);
        fail();
      } catch (Exception e) {
        
      }

      String jobName = "LDAP_SIMPLE__" + group.getName() + "__" + group.getId();
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
      assertEquals("ERROR_FAILSAFE", hib3GrouperLoaderLog.getStatus());

      members = group.getMembers();
      assertTrue(""+members.size(), members.size() > 900);
      assertTrue(members.contains(aandersonMember));
      assertTrue(members.contains(ablewisMember));
      
      GrouperUtil.sleep(1000);
      
      assertFalse(GrouperFailsafe.isApproved(jobName));
      GrouperFailsafe.assignApproveNextRun(jobName);
      assertTrue(GrouperFailsafe.isApproved(jobName));
      
      try {
        GrouperLoader.runJobOnceForGroup(grouperSession, group);
      }
      catch(RuntimeException e) {
        fail();
      }

      hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
      assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());

      assertFalse(GrouperFailsafe.isApproved(jobName));
      assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));

      members = group.getMembers();
      assertTrue(""+members.size(), members.size() < 900);
      assertTrue(members.contains(aandersonMember));
      assertFalse(members.contains(ablewisMember));
      
    } finally {
      teardownLdap();
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  public void testLoaderShouldAbortListWithoutChangingMembershipsOverallMinMemberships() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    setupLdap();
    
    try {
      
      Group group = new GroupSave(grouperSession).assignName("test:testLdapSimpleConvertDn").assignCreateParentStemsIfNotExist(true).save();
      
      Set<Member> members = group.getMembers();
      assertEquals(0, members.size());

      AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(LoaderLdapUtils.grouperLoaderLdapAttributeDefName()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(uid=a*)");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), "0 0 6 * * ?");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), "ou=People,dc=example,dc=edu");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), "SUBTREE_SCOPE");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapServerIdName(), "personLdap");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), "personLdapSource");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), "uid");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), "subjectId");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapTypeName(), "LDAP_GROUPS_FROM_ATTRIBUTES");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapGroupAttributeName(), "sn");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionName(), "someFolder:${groupAttributes['sn']}");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapGroupsLikeName(), "test:someFolder:%");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFailsafeUseName(), "T");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapMaxGroupPercentRemoveName(), "20");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapMaxOverallPercentGroupsRemoveName(), "-1");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapMaxOverallPercentMembershipsRemoveName(), "-1");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapMinGroupSizeName(), "-1");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapMinManagedGroupsName(), "-1");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapMinOverallNumberOfMembersName(), "900");
            
      GrouperLoader.runJobOnceForGroup(grouperSession, group);

      Stem stem = StemFinder.findByName(grouperSession, "test:someFolder", true);
      
      Set<Group> groups = new GroupFinder().assignParentStemId(stem.getId()).assignStemScope(Scope.SUB).findGroups();
      
      assertTrue(""+groups.size(), groups.size() > 10);
      
      GrouperUtil.sleep(1000);
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(uid=a-aa*)");

      try {
        GrouperLoader.runJobOnceForGroup(grouperSession, group);
        fail();
      } catch (Exception e) {
        
      }

      String jobName = "LDAP_GROUPS_FROM_ATTRIBUTES__" + group.getName() + "__" + group.getId();
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
      assertEquals("ERROR_FAILSAFE", hib3GrouperLoaderLog.getStatus());

      
      GrouperUtil.sleep(1000);
      
      assertFalse(GrouperFailsafe.isApproved(jobName));
      GrouperFailsafe.assignApproveNextRun(jobName);
      assertTrue(GrouperFailsafe.isApproved(jobName));
      
      try {
        GrouperLoader.runJobOnceForGroup(grouperSession, group);
      }
      catch(RuntimeException e) {
        fail();
      }

      hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
      assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());

      assertFalse(GrouperFailsafe.isApproved(jobName));
      assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));

      groups = new GroupFinder().assignParentStemId(stem.getId()).assignStemScope(Scope.SUB).findGroups();
      assertTrue(""+groups.size(), groups.size() < 10 && groups.size() > 0);
      
    } finally {
      teardownLdap();
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  
  private void setupLdap() {
    // TODO LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();
    LdapProvisionerTestUtils.setupLdapExternalSystem();
    LdapProvisionerTestUtils.setupSubjectSource();
  }
  
  /**
   * 
   */
  public void testLoaderLdapSimpleConvertDn() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    setupLdap();
    
    try {
      
      Group group = new GroupSave(grouperSession).assignName("test:testLdapSimpleConvertDn").assignCreateParentStemsIfNotExist(true).save();
      
      Set<Member> members = group.getMembers();
      assertEquals(0, members.size());

      AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(LoaderLdapUtils.grouperLoaderLdapAttributeDefName()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(cn=users)");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), "0 0 6 * * ?");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), "ou=Groups,dc=example,dc=edu");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), "SUBTREE_SCOPE");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapServerIdName(), "personLdap");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), "personLdapSource");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), "uniqueMember");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName(), 
          "${loaderLdapElUtils.convertDnToSpecificValue(subjectId)}");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), "subjectId");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapTypeName(), "LDAP_SIMPLE");

      GrouperLoader.runJobOnceForGroup(grouperSession, group);

      members = group.getMembers();
      assertEquals(2, members.size());
      
      Subject banderson = SubjectFinder.findByIdAndSource("banderson", "personLdapSource", true);
      Subject jsmith = SubjectFinder.findByIdAndSource("jsmith", "personLdapSource", true);

      Member bandersonMember = MemberFinder.findBySubject(grouperSession, banderson, true); 
      Member jsmithMember = MemberFinder.findBySubject(grouperSession, jsmith, true); 

      assertTrue(members.contains(bandersonMember));
      assertTrue(members.contains(jsmithMember));
      
    } finally {
      teardownLdap();
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  private void teardownLdap() {
    // TODO LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
  }
  
  public void testLoaderLdapReverse() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();
    try {

      LdapModificationItem item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("description", "cn=users,ou=Groups,dc=example,dc=edu"));
      List<LdapModificationItem> ldapModificationItems = new ArrayList<LdapModificationItem>();
      ldapModificationItems.add(item);

      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("description", "cn=employees,ou=Groups,dc=example,dc=edu"));
      ldapModificationItems.add(item);

      new LdapSyncDaoForLdap().modify("personLdap", "uid=banderson,ou=People,dc=example,dc=edu", ldapModificationItems);

      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("description", "cn=users,ou=Groups,dc=example,dc=edu"));
      ldapModificationItems = new ArrayList<LdapModificationItem>();
      ldapModificationItems.add(item);

      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("description", "cn=students,ou=Groups,dc=example,dc=edu"));
      ldapModificationItems.add(item);

      new LdapSyncDaoForLdap().modify("personLdap", "uid=jsmith,ou=People,dc=example,dc=edu", ldapModificationItems);

      
      Group group = new GroupSave(grouperSession).assignName("test:testLdapSimpleReverse").assignCreateParentStemsIfNotExist(true).save();
      
      Set<Member> members = group.getMembers();
      assertEquals(0, members.size());

      AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(LoaderLdapUtils.grouperLoaderLdapAttributeDefName()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(description=cn=users,ou=Groups,dc=example,dc=edu)");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), "0 0 6 * * ?");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), "ou=People,dc=example,dc=edu");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), "SUBTREE_SCOPE");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapServerIdName(), "personLdap");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), "personLdapSource");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), "uid");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), "subjectId");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapTypeName(), "LDAP_SIMPLE");

      GrouperLoader.runJobOnceForGroup(grouperSession, group);

      members = group.getMembers();
      assertEquals(2, members.size());
      
      Subject banderson = SubjectFinder.findByIdAndSource("banderson", "personLdapSource", true);
      Subject jsmith = SubjectFinder.findByIdAndSource("jsmith", "personLdapSource", true);

      Member bandersonMember = MemberFinder.findBySubject(grouperSession, banderson, true); 
      Member jsmithMember = MemberFinder.findBySubject(grouperSession, jsmith, true); 

      assertTrue(members.contains(bandersonMember));
      assertTrue(members.contains(jsmithMember));
      
    } finally {
      LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * 
   */
  public void testLoaderLdapLookupByDn() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();

    try {
      
      Group group = new GroupSave(grouperSession).assignName("test:testLdapSimpleLookup").assignCreateParentStemsIfNotExist(true).save();
      
      Set<Member> members = group.getMembers();
      assertEquals(0, members.size());

      AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(LoaderLdapUtils.grouperLoaderLdapAttributeDefName()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(cn=users)");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), "0 0 6 * * ?");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), "ou=Groups,dc=example,dc=edu");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), "SUBTREE_SCOPE");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapServerIdName(), "personLdap");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), "personLdapSource");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), "uniqueMember");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName(), 
          "${ldapLookup.assignLdapConfigId('personLdap').assignAttributeNameResult('uid').assignSearchDn('%TERM%')"
          + ".assignTerm(subjectId).doLookup()}");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), "subjectId");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapTypeName(), "LDAP_SIMPLE");

      int ldapLookupFilterCount = LdapLookup.test_filterCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, group);

      assertEquals(2, LdapLookup.test_filterCount - ldapLookupFilterCount);
      
      members = group.getMembers();
      assertEquals(2, members.size());
      
      Subject banderson = SubjectFinder.findByIdAndSource("banderson", "personLdapSource", true);
      Subject jsmith = SubjectFinder.findByIdAndSource("jsmith", "personLdapSource", true);

      Member bandersonMember = MemberFinder.findBySubject(grouperSession, banderson, true); 
      Member jsmithMember = MemberFinder.findBySubject(grouperSession, jsmith, true); 

      assertTrue(members.contains(bandersonMember));
      assertTrue(members.contains(jsmithMember));

      ldapLookupFilterCount = LdapLookup.test_filterCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, group);

      assertEquals(2, LdapLookup.test_filterCount - ldapLookupFilterCount);

    } finally {
      LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * 
   */
  public void testLoaderLdapLookupByDnCache() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();

    try {
      
      Group group = new GroupSave(grouperSession).assignName("test:testLdapSimpleLookupCache").assignCreateParentStemsIfNotExist(true).save();
      
      Set<Member> members = group.getMembers();
      assertEquals(0, members.size());

      AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(LoaderLdapUtils.grouperLoaderLdapAttributeDefName()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(cn=users)");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), "0 0 6 * * ?");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), "ou=Groups,dc=example,dc=edu");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), "SUBTREE_SCOPE");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapServerIdName(), "personLdap");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), "personLdapSource");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), "uniqueMember");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName(), 
          "${ldapLookup.assignLdapConfigId('personLdap').assignAttributeNameResult('uid').assignCacheForMinutes(24*60).assignSearchDn('%TERM%')"
          + ".assignTerm(subjectId).doLookup()}");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), "subjectId");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapTypeName(), "LDAP_SIMPLE");

      int ldapLookupFilterCount = LdapLookup.test_filterCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, group);

      assertEquals(2, LdapLookup.test_filterCount - ldapLookupFilterCount);
      
      members = group.getMembers();
      assertEquals(2, members.size());
      
      Subject banderson = SubjectFinder.findByIdAndSource("banderson", "personLdapSource", true);
      Subject jsmith = SubjectFinder.findByIdAndSource("jsmith", "personLdapSource", true);

      Member bandersonMember = MemberFinder.findBySubject(grouperSession, banderson, true); 
      Member jsmithMember = MemberFinder.findBySubject(grouperSession, jsmith, true); 

      assertTrue(members.contains(bandersonMember));
      assertTrue(members.contains(jsmithMember));

      ldapLookupFilterCount = LdapLookup.test_filterCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, group);

      assertEquals(0, LdapLookup.test_filterCount - ldapLookupFilterCount);

    } finally {
      LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * 
   */
  public void testConvertDnToSpecificValue() {
    assertEquals("someapp", LoaderLdapElUtils.convertDnToSpecificValue("cn=someapp,ou=groups,dc=upenn,dc=edu"));

    assertEquals("hyzer, chris", LoaderLdapElUtils.convertDnToSpecificValue("cn=hyzer\\, chris,ou=groups,dc=upenn,dc=edu"));

    Map<String, Object> envVars = new HashMap<String, Object>();
    envVars.put("subjectId", "cn=someapp,ou=groups,dc=upenn,dc=edu");
    
    assertEquals("someapp", LoaderLdapUtils.substituteEl("${loaderLdapElUtils.convertDnToSpecificValue(subjectId)}", envVars));
    
  }
  
  /**
   * 
   */
  public void testConvertGroupNameAttributes() {
    
    Map<String, Object> envVars = new HashMap<String, Object>();
    
    Map<String, String> groupAttributes = new HashMap<String, String>();
    groupAttributes.put("cn", "test:testGroup");
    
    envVars.put("groupAttributes", groupAttributes);
    
    assertEquals("groups:test:testGroup", LoaderLdapUtils.substituteEl("groups:${groupAttributes['cn']}", envVars));
    
  }
  
  /**
   * 
   */
  public void testConvertDnToSubpath() {
    assertEquals("a:b:c", LoaderLdapElUtils.convertDnToSubPath("cn=a:b:c,ou=groups,dc=upenn,dc=edu", "dc=upenn,dc=edu", "ou=groups"));
    assertEquals("groups:a:b:c", LoaderLdapElUtils.convertDnToSubPath("cn=a:b:c,ou=groups,dc=upenn,dc=edu", "dc=edu", "dc=upenn"));
    assertEquals("gr,oups:a:b:c", LoaderLdapElUtils.convertDnToSubPath("cn=a:b:c,ou=gr\\,oups,dc=upenn,dc=edu", "dc=edu", "dc=upenn"));
    
  }

  /**
   * 
   */
  public void testLoaderLdapLookupByFilter() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();
  
    try {
      
      Group group = new GroupSave(grouperSession).assignName("test:testLdapSimpleLookupFilter").assignCreateParentStemsIfNotExist(true).save();
      
      Set<Member> members = group.getMembers();
      assertEquals(0, members.size());
  
      AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(LoaderLdapUtils.grouperLoaderLdapAttributeDefName()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(cn=users)");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), "0 0 6 * * ?");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), "ou=Groups,dc=example,dc=edu");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), "SUBTREE_SCOPE");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapServerIdName(), "personLdap");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), "personLdapSource");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), "uniqueMember");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName(), 
          "${ldapLookup.assignLdapConfigId('personLdap').assignTerm(loaderLdapElUtils.convertDnToSpecificValue(subjectId))"
          + ".assignSearchDn('ou=People,dc=example,dc=edu').assignSearchScope('SUBTREE_SCOPE').assignFilter('(uid=%TERM%)')"
          + ".assignAttributeNameResult('uid').doLookup()}");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), "subjectId");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapTypeName(), "LDAP_SIMPLE");
  
      int ldapLookupFilterCount = LdapLookup.test_filterCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, group);
  
      assertEquals(2, LdapLookup.test_filterCount - ldapLookupFilterCount);
      
      members = group.getMembers();
      assertEquals(2, members.size());
      
      Subject banderson = SubjectFinder.findByIdAndSource("banderson", "personLdapSource", true);
      Subject jsmith = SubjectFinder.findByIdAndSource("jsmith", "personLdapSource", true);
  
      Member bandersonMember = MemberFinder.findBySubject(grouperSession, banderson, true); 
      Member jsmithMember = MemberFinder.findBySubject(grouperSession, jsmith, true); 
  
      assertTrue(members.contains(bandersonMember));
      assertTrue(members.contains(jsmithMember));
  
      ldapLookupFilterCount = LdapLookup.test_filterCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, group);
  
      assertEquals(2, LdapLookup.test_filterCount - ldapLookupFilterCount);
  
    } finally {
      LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * 
   */
  public void testLoaderLdapLookupByFilterCache() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();
  
    try {
      
      Group group = new GroupSave(grouperSession).assignName("test:testLdapSimpleLookupFilterCache").assignCreateParentStemsIfNotExist(true).save();
      
      Set<Member> members = group.getMembers();
      assertEquals(0, members.size());
  
      AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(LoaderLdapUtils.grouperLoaderLdapAttributeDefName()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(cn=users)");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), "0 0 6 * * ?");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), "ou=Groups,dc=example,dc=edu");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), "SUBTREE_SCOPE");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapServerIdName(), "personLdap");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), "personLdapSource");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), "uniqueMember");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName(), 
          "${ldapLookup.assignLdapConfigId('personLdap').assignTerm(loaderLdapElUtils.convertDnToSpecificValue(subjectId))"
          + ".assignSearchDn('ou=People,dc=example,dc=edu').assignSearchScope('SUBTREE_SCOPE').assignFilter('(uid=%TERM%)')"
          + ".assignAttributeNameResult('uid').assignCacheForMinutes(24*60).doLookup()}");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), "subjectId");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapTypeName(), "LDAP_SIMPLE");
  
      int ldapLookupFilterCount = LdapLookup.test_filterCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, group);
  
      assertEquals(2, LdapLookup.test_filterCount - ldapLookupFilterCount);
      
      members = group.getMembers();
      assertEquals(2, members.size());
      
      Subject banderson = SubjectFinder.findByIdAndSource("banderson", "personLdapSource", true);
      Subject jsmith = SubjectFinder.findByIdAndSource("jsmith", "personLdapSource", true);
  
      Member bandersonMember = MemberFinder.findBySubject(grouperSession, banderson, true); 
      Member jsmithMember = MemberFinder.findBySubject(grouperSession, jsmith, true); 
  
      assertTrue(members.contains(bandersonMember));
      assertTrue(members.contains(jsmithMember));
  
      ldapLookupFilterCount = LdapLookup.test_filterCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, group);
  
      assertEquals(0, LdapLookup.test_filterCount - ldapLookupFilterCount);
  
    } finally {
      LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * 
   */
  public void testLoaderLdapBulkLookupByFilterCache() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();
  
    try {
      
      Group loaderGroup = new GroupSave(grouperSession).assignName("test:testLdapBulkLookupFilterCache").assignCreateParentStemsIfNotExist(true).save();
      
      Set<Member> members = loaderGroup.getMembers();
      assertEquals(0, members.size());
  
      AttributeAssign attributeAssign = loaderGroup.getAttributeDelegate().assignAttribute(LoaderLdapUtils.grouperLoaderLdapAttributeDefName()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(cn=*)");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), "0 0 6 * * ?");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), "ou=Groups,dc=example,dc=edu");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), "SUBTREE_SCOPE");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapServerIdName(), "personLdap");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), "personLdapSource");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), "uniqueMember");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapGroupsLikeName(), "test:ldap:%");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionName(), "ldap:${loaderLdapElUtils.convertDnToSpecificValue(groupAttributes['dn'])}");
      
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName(), 
          "${ldapLookup.assignLdapConfigId('personLdap').assignAttributeNameQuery('dn').assignTerm(subjectId)"
          + ".assignSearchDn('ou=People,dc=example,dc=edu').assignSearchScope('SUBTREE_SCOPE').assignFilter('(uid=*)')"
          + ".assignAttributeNameResult('uid').assignBulkLookup(true).assignCacheForMinutes(24*60).doLookup()}");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), "subjectId");
      attributeAssign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapTypeName(), "LDAP_GROUP_LIST");
  
      int ldapLookupFilterCount = LdapLookup.test_filterCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, loaderGroup);
  
      assertEquals(1, LdapLookup.test_filterCount - ldapLookupFilterCount);
      Group loadedGroup = GroupFinder.findByName(grouperSession, "test:ldap:users", true);
      members = loadedGroup.getMembers();
      assertEquals(2, members.size());
      
      Subject banderson = SubjectFinder.findByIdAndSource("banderson", "personLdapSource", true);
      Subject jsmith = SubjectFinder.findByIdAndSource("jsmith", "personLdapSource", true);
  
      Member bandersonMember = MemberFinder.findBySubject(grouperSession, banderson, true); 
      Member jsmithMember = MemberFinder.findBySubject(grouperSession, jsmith, true); 
  
      assertTrue(members.contains(bandersonMember));
      assertTrue(members.contains(jsmithMember));
  
      ldapLookupFilterCount = LdapLookup.test_filterCount;
      
      GrouperLoader.runJobOnceForGroup(grouperSession, loaderGroup);
  
      assertEquals(0, LdapLookup.test_filterCount - ldapLookupFilterCount);
  
    } finally {
      LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * 
   */
  public void testLoaderCopyLdapToSql() {
  
    final String tableName = "testgrouper_ldapsql_single";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          
          Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "the_dn", Types.VARCHAR, "200", true, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "mail", Types.VARCHAR, "10", false, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "description", Types.VARCHAR, "1024", false, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "some_int", Types.BIGINT, "12", false, false);
        }
        
      });
    }

    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    //LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();
  
    try {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.class", "edu.internet2.middleware.grouper.app.ldapToSql.LdapToSqlSyncDaemon");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.0.ldapName", "dn");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.0.sqlColumn", "the_dn");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.0.uniqueKey", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.1.ldapName", "mail");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.1.sqlColumn", "mail");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.2.sqlColumn", "description");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.2.translation", "${ldapAttribute__givenname + ', ' + ldapAttribute__uid}");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.3.sqlColumn", "some_int");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.3.translation", "${123}");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlBaseDn", "ou=People,dc=example,dc=edu");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlDbConnection", "grouper");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlExtraAttributes", "uid,givenName");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlFilter", "(uid=aa*)");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlLdapConnection", "personLdap");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlNumberOfAttributes", "4");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlSearchScope", "SUBTREE_SCOPE");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlTableName", "testgrouper_ldapsql_single");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.quartzCron", "0 03 5 * * ?");

      int ldapLookupFilterCount = LdapLookup.test_filterCount;

      GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_ldapToSqlSingleValued");
  
      List<Object[]> sqlRows = new GcDbAccess().connectionName("grouper").sql("select the_dn, mail, description, some_int from testgrouper_ldapsql_single order by 1").selectList(Object[].class);

      assertEquals(2, sqlRows.size());
      assertEquals(0, LdapLookup.test_filterCount - ldapLookupFilterCount);
  
    } finally {
      //LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
      GrouperSession.stopQuietly(grouperSession);
    }
    
    SqlProvisionerTest.dropTableSyncTable(tableName);
  }

}
