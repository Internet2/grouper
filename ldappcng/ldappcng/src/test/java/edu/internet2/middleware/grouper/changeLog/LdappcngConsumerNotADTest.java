/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.changeLog;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;

/** Tests for {@link LdappcngConsumer} to a target directory other than Active Directory. */
public class LdappcngConsumerNotADTest extends BaseLdappcngConsumerTest {

  /** Path to configuration. */
  public static final String CONFIG_PATH = TEST_PATH + "/spml/notad";

  /** Path to test data files. */
  public static final String DATA_PATH = CONFIG_PATH + "/data/";

  /**
   * Run tests.
   * 
   * @param args
   */
  public static void main(String[] args) {
    // TestRunner.run(LdappcngConsumerNotADTest.class);
    TestRunner.run(new LdappcngConsumerNotADTest("testMembershipDeleteEmptyList"));
  }

  /**
   * Constructor
   * 
   * @param name
   * @param confDir
   */
  public LdappcngConsumerNotADTest(String name) {
    super(name, CONFIG_PATH);
  }

  /**
   * Test adding a group with no members to a directory requiring the member attribute.
   * 
   * @throws Exception
   */
  public void testGroupAddEmptyList() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerNotADTest.testGroupAddEmptyList.before.ldif");

    setUpEduStem();

    clearChangeLog();

    groupA = StemHelper.addChildGroup(edu, "groupA", "Group A");

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerNotADTest.testGroupAddEmptyList.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerNotADTest.testGroupAddEmptyList.after.ldif");
  }

  /**
   * Test adding a membership to a group which already exists to a directory requiring the member attribute. The empty
   * value will still be present.
   * 
   * @throws Exception
   */
  public void testMembershipAddEmptyList() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerNotADTest.testMembershipAddEmptyList.before.ldif");

    setUpEduStem();
    groupA = StemHelper.addChildGroup(edu, "groupA", "Group A");

    clearChangeLog();

    groupA.addMember(SubjectTestHelper.SUBJ0);

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerNotADTest.testMembershipAddEmptyList.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerNotADTest.testMembershipAddEmptyList.after.ldif");
  }

  /**
   * Test deleting the last membership from a group in a directory which requires the member attribute.
   * 
   * @throws Exception
   */
  public void testMembershipDeleteEmptyList() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerNotADTest.testMembershipDeleteEmptyList.before.ldif");

    setUpEduStem();
    setUpGroupA();

    clearChangeLog();

    groupA.deleteMember(SubjectTestHelper.SUBJ0);

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerNotADTest.testMembershipDeleteEmptyList.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerNotADTest.testMembershipDeleteEmptyList.after.ldif");
  }

}
