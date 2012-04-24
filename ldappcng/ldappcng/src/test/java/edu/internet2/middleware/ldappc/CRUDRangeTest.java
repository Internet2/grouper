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

import java.io.File;
import java.util.ArrayList;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;

/**
 * Tests ldappc support for Active Directory groups with "large" (> 1500) members. May be
 * costly to run.
 */
public class CRUDRangeTest extends BaseLdappcTestCase {

  private Group groupA;

  private Group groupB;

  public CRUDRangeTest(String name) {
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
  }

  public void testADGroup3000() throws Exception {

    if (!useActiveDirectory()) {
      return;
    }

    loadLdif("CRUDTest.before.ldif");
    
    int subjects = 3002;

    String personLdif = LdappcTestHelper.readFile(getFile("CRUDTest.person.ldif"));
    
    for (int i = 2; i < subjects; i++) {
      String loadLdif = personLdif.replace("${i}", Integer.toString(i));
      LdappcTestHelper.loadLdif(loadLdif, propertiesFile, ldappc.getContext());
    }

    RegistryReset._addSubjects(10, subjects);

    Group groupC = StemHelper.addChildGroup(this.edu, "groupC", "Group C");
    groupC.setDescription("descriptionC");
    groupC.store();
    for (int i = 0; i < subjects; i++) {
      groupC.addMember(SubjectFinder.findById("test.subject." + i, true));
    }

    File ldif = dryRun(GroupDNStructure.bushy);

    verifyLdif("CRUDRangeTest.testADGroup3000DryRun.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }

    provision(GroupDNStructure.bushy, true);

    StringBuffer afterLdif = new StringBuffer();
    afterLdif.append(LdappcTestHelper.readFile(getFile("CRUDRangeTest.testADGroup3000.after.ldif")));
    for (int i = 2; i < subjects; i++) {
      afterLdif.append(personLdif.replace("${i}", Integer.toString(i)));
    }
    
    ArrayList<String> normalizeDnAttributes = new ArrayList<String>();
    normalizeDnAttributes.add(ldappc.getConfig().getGroupMembersDnListAttribute());
    
    LdappcTestHelper.verifyLdif(afterLdif.toString(), propertiesFile, normalizeDnAttributes,
        base, ldappc.getContext(), useActiveDirectory());
  }
}
