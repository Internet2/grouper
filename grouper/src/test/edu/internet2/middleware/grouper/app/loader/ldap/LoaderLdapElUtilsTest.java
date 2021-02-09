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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerIncrementalTest;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
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
    TestRunner.run(new LoaderLdapElUtilsTest("testLoaderLdapBulkLookupByFilterCache"));
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
   * 
   */
  public void testLoaderLdapSimpleConvertDn() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();
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
      LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
      GrouperSession.stopQuietly(grouperSession);
    }
    
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
    //LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();
  
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
      //LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
}
