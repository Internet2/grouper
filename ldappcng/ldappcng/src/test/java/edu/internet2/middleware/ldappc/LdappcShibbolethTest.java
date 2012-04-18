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
package edu.internet2.middleware.ldappc;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;

public class LdappcShibbolethTest extends BaseLdappcTestCase {

  private Group groupA;

  private Group groupB;

  private Group groupD;
  
  public static void main(String[] args) {
    // TestRunner.run(new CRUDTest("testResolverQueries"));
  }

  public LdappcShibbolethTest(String name) {
    super(name);
  }

  public void setUp() {
    super.setUp();
    
    groupA = StemHelper.addChildGroup(edu, "groupA", "Group A");
    groupA.addMember(SubjectTestHelper.SUBJ0);

    groupB = StemHelper.addChildGroup(edu, "groupB", "Group B");
    groupB.addMember(SubjectTestHelper.SUBJ1);

    groupD = StemHelper.addChildGroup(edu, "groupD", "Group D");

    try {
      setUpLdapContext();
    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }
  }

  public void testResolverQueries() throws Exception {
    setUpLdappc("ldappc.test.resolverQueries.xml");

    Set<Group> groups = ldappc.buildGroupSet();

    assertEquals(2, groups.size());

    assertTrue(groups.contains(groupA));
    assertTrue(groups.contains(groupB));
    assertFalse(groups.contains(groupD));
  }
}
