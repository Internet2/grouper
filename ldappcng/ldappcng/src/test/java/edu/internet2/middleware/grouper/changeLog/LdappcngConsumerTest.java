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

import java.util.List;

import junit.textui.TestRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.ChangeLogDataConnector;
import edu.internet2.middleware.ldappc.LdappcTestHelper;

/** Tests for {@link LdappcngConsumer} */
public class LdappcngConsumerTest extends BaseLdappcngConsumerTest {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(LdappcngConsumerTest.class);

  /** Path to configuration. */
  public static final String CONFIG_PATH = TEST_PATH + "/spml";

  /** Path to test data files. */
  public static final String DATA_PATH = CONFIG_PATH + "/data/";

  /**
   * Run tests.
   * 
   * @param args
   */
  public static void main(String[] args) {
    // TestRunner.run(LdappcngConsumerTest.class);
    TestRunner.run(new LdappcngConsumerTest("testStemMove"));
  }

  /**
   * 
   * Constructor
   * 
   * @param name
   */
  public LdappcngConsumerTest(String name) {
    super(name, CONFIG_PATH);
  }

  public void printChangeLogEntries() {
    List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry().retrieveBatch(-1, 100);
    LOG.debug("change log entry list size {}", changeLogEntryList.size());
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
      LOG.debug(ChangeLogDataConnector.toStringDeep(changeLogEntry));
      LOG.debug(changeLogEntry.toStringDeep());
    }
  }

  /**
   * Determine if subtree renames are supported by attempting to rename a non-empty ou.
   * 
   * This method deletes the test target ldap directory and re-adds the basic test ldif.
   * 
   * @return true if subtree renames are suppported, false otherwise
   */
  public boolean isSubtreeRenameSupported() {

    try {
      LdappcTestHelper.deleteChildren(base, ldap);

      // create an ou with a child ou and rename it
      loadLdif(TEST_PATH + "/spml/data/PSPTest.before.ldif");
      loadLdif(DATA_PATH + "LdappcngConsumerTest.testStemRenameChildGroups.before.ldif");
    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occured : " + e.getMessage());
    }

    String oldDn = "ou=child1,ou=edu,ou=testgroups," + base;
    String newDn = "ou=newChild1,ou=edu,ou=testgroups," + base;

    try {
      ldap.rename(oldDn, newDn);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        // clean up and re add basic ldif
        LdappcTestHelper.deleteChildren(base, ldap);
        loadLdif(TEST_PATH + "/spml/data/PSPTest.before.ldif");
      } catch (Exception e) {
        e.printStackTrace();
        fail("An error occured : " + e.getMessage());
      }
    }

    return false;
  }

  /**
   * Test provisioning resulting from the adding of a group. The change log events include a membership addition.
   * 
   * @throws Exception
   */
  public void testGroupAdd() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testGroupAdd.before.ldif");

    setUpEduStem();

    clearChangeLog();

    setUpGroupA();

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testGroupAdd.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testGroupAdd.after.ldif");
  }

  /**
   * Test provisioning resulting from copying a group to a stem.
   * 
   * @throws Exception
   */
  public void testGroupCopy() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testGroupCopy.before.ldif");

    setUpEduStem();
    setUpGroupA();
    Stem child = StemHelper.addChildStem(edu, "child", "Child");

    clearChangeLog();

    groupA.copy(child);

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testGroupCopy.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testGroupCopy.after.ldif");
  }

  /**
   * Test provisioning resulting from deleting a group.
   * 
   * @throws Exception
   */
  public void testGroupDelete() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testGroupDelete.before.ldif");

    setUpEduStem();

    groupA = StemHelper.addChildGroup(edu, "groupA", "Group A");

    clearChangeLog();

    groupA.delete();

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testGroupDelete.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testGroupDelete.after.ldif");
  }

  /**
   * Test provisioning resulting from deleting a group which has already been deleted from the provisioned target.
   * 
   * @throws Exception
   */
  public void testGroupDeleteAlreadyDeleted() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testGroupDeleteAlreadyDeleted.before.ldif");

    setUpEduStem();

    groupA = StemHelper.addChildGroup(edu, "groupA", "Group A");

    clearChangeLog();

    groupA.delete();

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testGroupDeleteAlreadyDeleted.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testGroupDeleteAlreadyDeleted.after.ldif");
  }

  /**
   * Test provisioning resulting from moving a group.
   * 
   * @throws Exception
   */
  public void testGroupMove() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testGroupMove.before.ldif");

    setUpEduStem();
    setUpGroupA();
    Stem child = StemHelper.addChildStem(edu, "child", "Child");

    clearChangeLog();

    groupA.move(child);

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testGroupMove.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testGroupMove.after.ldif");
  }

  /**
   * Test provisioning resulting from renaming a group.
   * 
   * @throws Exception
   */
  public void testGroupRename() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testGroupRename.before.ldif");

    setUpEduStem();
    setUpGroupB();

    clearChangeLog();

    groupB.setExtension("newExtensionGroupB");
    groupB.store();

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testGroupRename.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testGroupRename.after.ldif");
  }

  /**
   * Test provisioning resulting from the adding of a membership.
   * 
   * @throws Exception
   */
  public void testMembershipAdd() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testMembershipAdd.before.ldif");

    setUpEduStem();
    setUpGroupA();

    clearChangeLog();

    groupA.addMember(SubjectTestHelper.SUBJ1);

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testMembershipAdd.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testMembershipAdd.after.ldif");
  }

  /**
   * Test provisioning resulting from the adding of a membership which has already been added on the provisioned target.
   * 
   * @throws Exception
   */
  public void testMembershipAddAlreadyExists() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testMembershipAddAlreadyExists.before.ldif");

    setUpEduStem();
    setUpGroupA();

    clearChangeLog();

    groupA.addMember(SubjectTestHelper.SUBJ1);

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testMembershipAddAlreadyExists.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testMembershipAddAlreadyExists.after.ldif");
  }

  /**
   * Test provisioning resulting from the deletion of a membership.
   * 
   * @throws Exception
   */
  public void testMembershipDelete() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testMembershipDelete.before.ldif");

    setUpEduStem();
    setUpGroupA();
    groupA.addMember(SubjectTestHelper.SUBJ1);

    clearChangeLog();

    groupA.deleteMember(SubjectTestHelper.SUBJ1);

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testMembershipDelete.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testMembershipDelete.after.ldif");
  }

  /**
   * Test provisioning resulting from the deletion of a membership which has already been deleted from the provisioned
   * target.
   * 
   * @throws Exception
   */
  public void testMembershipDeleteAlreadyDeleted() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testMembershipDeleteAlreadyDeleted.before.ldif");

    setUpEduStem();
    setUpGroupA();
    groupA.addMember(SubjectTestHelper.SUBJ1);

    clearChangeLog();

    groupA.deleteMember(SubjectTestHelper.SUBJ1);

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testMembershipDeleteAlreadyDeleted.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testMembershipDeleteAlreadyDeleted.after.ldif");
  }

  /**
   * Test provisioning resulting from the addition of a stem.
   * 
   * @throws Exception
   */
  public void testStemAdd() throws Exception {

    clearChangeLog();

    setUpEduStem();

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testStemAdd.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testStemAdd.after.ldif");
  }

  /**
   * Test provisioning resulting from copying a stem.
   * 
   * @throws Exception
   */
  public void testStemCopy() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testStemCopy.before.ldif");

    setUpEduStem();

    Stem child1 = StemHelper.addChildStem(edu, "child1", "Child 1");

    Group group1 = child1.addChildGroup("group1", "Group1");
    group1.addMember(SubjectTestHelper.SUBJ0);

    Group group2 = child1.addChildGroup("group2", "Group2");
    group2.addMember(SubjectTestHelper.SUBJ1);

    Stem child2 = StemHelper.addChildStem(edu, "child2", "Child 2");

    clearChangeLog();

    child1.copy(child2);

    ChangeLogTempToEntity.convertRecords();

    printChangeLogEntries();

    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testStemCopy.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testStemCopy.after.ldif");
  }

  /**
   * Test provisioning resulting from the deletion of a stem.
   * 
   * @throws Exception
   */
  public void testStemDelete() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testStemDelete.before.ldif");

    setUpEduStem();

    clearChangeLog();

    edu.delete();

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testStemDelete.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testStemDelete.after.ldif");
  }

  /**
   * Test provisioning resulting from the deletion of a stem which has already been deleted.
   * 
   * @throws Exception
   */
  public void testStemDeleteAlreadyDeleted() throws Exception {

    setUpEduStem();

    clearChangeLog();

    edu.delete();

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testStemDeleteAlreadyDeleted.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testStemDelete.after.ldif");
  }

  /**
   * Test provisioning resulting from moving a stem.
   * 
   * @throws Exception
   */
  public void testStemMove() throws Exception {

    boolean isSubtreeRenameSupported = isSubtreeRenameSupported();

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testStemMove.before.ldif");

    setUpEduStem();

    Stem child1 = StemHelper.addChildStem(edu, "child1", "Child 1");

    Group group1 = child1.addChildGroup("group1", "Group1");
    group1.addMember(SubjectTestHelper.SUBJ0);

    Group group2 = child1.addChildGroup("group2", "Group2");
    group2.addMember(SubjectTestHelper.SUBJ1);

    Stem child2 = StemHelper.addChildStem(edu, "child2", "Child 2");

    clearChangeLog();

    child1.move(child2);

    ChangeLogTempToEntity.convertRecords();

    runChangeLog();

    if (isSubtreeRenameSupported) {
      verifySpml(DATA_PATH + "LdappcngConsumerTest.testStemMove.subtreeRenameSupported.xml");
      verifyLdif(DATA_PATH + "LdappcngConsumerTest.testStemMove.subtreeRenameSupported.after.ldif");
    } else {
      verifySpml(DATA_PATH + "LdappcngConsumerTest.testStemMove.subtreeRenameNotSupported.xml");
      verifyLdif(DATA_PATH + "LdappcngConsumerTest.testStemMove.subtreeRenameNotSupported.after.ldif");
    }
  }

  /**
   * Test provisioning resulting from renaming a stem.
   * 
   * @throws Exception
   */
  public void testStemRename() throws Exception {

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testStemRename.before.ldif");

    setUpEduStem();

    clearChangeLog();

    edu.setExtension("newEdu");
    edu.store();

    ChangeLogTempToEntity.convertRecords();
    runChangeLog();

    verifySpml(DATA_PATH + "LdappcngConsumerTest.testStemRename.xml");
    verifyLdif(DATA_PATH + "LdappcngConsumerTest.testStemRename.after.ldif");
  }

  /**
   * Test provisioning resulting from renaming a stem with child groups.
   * 
   * @throws Exception
   */
  public void testStemRenameChildGroups() throws Exception {

    boolean isSubtreeRenameSupported = isSubtreeRenameSupported();

    loadLdif(DATA_PATH + "LdappcngConsumerTest.testStemRenameChildGroups.before.ldif");

    setUpEduStem();

    Stem child1 = StemHelper.addChildStem(edu, "child1", "Child 1");

    Group group1 = child1.addChildGroup("group1", "Group1");
    group1.addMember(SubjectTestHelper.SUBJ0);

    Group group2 = child1.addChildGroup("group2", "Group2");
    group2.addMember(SubjectTestHelper.SUBJ1);

    Stem child2 = StemHelper.addChildStem(child1, "child2", "Child 2");

    Group group3 = child2.addChildGroup("group3", "Group3");
    group3.addMember(SubjectTestHelper.SUBJ2);

    clearChangeLog();

    child1.setExtension("newChild1");
    child1.store();

    ChangeLogTempToEntity.convertRecords();

    printChangeLogEntries();

    runChangeLog();

    if (isSubtreeRenameSupported) {
      verifySpml(DATA_PATH + "LdappcngConsumerTest.testStemRenameChildGroups.subtreeRenameSupported.xml");
      verifyLdif(DATA_PATH + "LdappcngConsumerTest.testStemRenameChildGroups.subtreeRenameSupported.after.ldif");
    } else {
      verifySpml(DATA_PATH + "LdappcngConsumerTest.testStemRenameChildGroups.subtreeRenameNotSupported.xml");
      verifyLdif(DATA_PATH + "LdappcngConsumerTest.testStemRenameChildGroups.subtreeRenameNotSupported.after.ldif");
    }
  }

}
