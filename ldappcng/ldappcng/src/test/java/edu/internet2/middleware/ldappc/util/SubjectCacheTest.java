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
package edu.internet2.middleware.ldappc.util;

import java.util.Set;

import javax.naming.Name;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.ldappc.BaseLdappcTestCase;
import edu.internet2.middleware.ldappc.ConfigManager;

public class SubjectCacheTest extends BaseLdappcTestCase {

  private Group groupA;

  private Group groupB;

  private SubjectCache cache;

  public SubjectCacheTest(String name) {
    super(name);
  }

  public void setUp() {
    super.setUp();

    groupA = StemHelper.addChildGroup(this.edu, "groupA", "Group A");
    groupA.addMember(SubjectTestHelper.SUBJ0);

    groupB = StemHelper.addChildGroup(this.edu, "groupB", "Group B");
    groupB.addMember(SubjectTestHelper.SUBJ1);
    groupB.setDescription("descriptionB");
    groupB.store();

    try {
      setUpLdapContext();
      setUpLdappc(pathToConfig, pathToProperties);
    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }

    cache = new SubjectCache(ldappc);
  }

  public void testSimple() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

    ((ConfigManager) ldappc.getConfig()).getSourceSubjectLdapFilter("jdbc")
        .setMultipleResults(true);

    loadLdif("CRUDTest.testCalculateBushyMultipleSubjects.before.ldif");

    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0,
        false);

    Set<Name> dns01;
    Set<Name> dns02;

    Set<Name> dns11;
    Set<Name> dns12;

    dns01 = cache.findSubjectDn(member0);
    dns02 = cache.findSubjectDn(member0);
    assertEquals(dns01, dns02);
    assertEquals(1, cache.getSubjectIdLookups());
    assertEquals(1, cache.getSubjectIdTableHits());

    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1,
        false);

    dns11 = cache.findSubjectDn(member1);
    dns12 = cache.findSubjectDn(member1);
    assertEquals(dns11, dns12);
    assertEquals(2, cache.getSubjectIdLookups());
    assertEquals(2, cache.getSubjectIdTableHits());

    cache.init();

    dns01 = cache.findSubjectDn(member0);
    dns02 = cache.findSubjectDn(member0);
    assertEquals(dns01, dns02);
    assertEquals(1, cache.getSubjectIdLookups());
    assertEquals(1, cache.getSubjectIdTableHits());

    dns11 = cache.findSubjectDn(member1);
    dns12 = cache.findSubjectDn(member1);
    assertEquals(dns11, dns12);
    assertEquals(2, cache.getSubjectIdLookups());
    assertEquals(2, cache.getSubjectIdTableHits());
  }

}
