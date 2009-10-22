package edu.internet2.middleware.ldappc;

import java.io.File;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter.OnNotFound;

public class CRUDTest extends BaseLdappcTestCase {

  private Group groupA;

  private Group groupB;

  public static void main(String[] args) {
    // TestRunner.run(new CRUDTest("testCalculateBushy"));
  }

  public CRUDTest(String name) {
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
    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }
  }

  private void setUpLdappc() throws Exception {
    setUpLdappc(pathToConfig, pathToProperties);
  }

  private void setUpLdappc(String configResource) throws Exception {

    File file = GrouperUtil.fileFromResourceName(TEST_PATH + configResource);
    if (file == null) {
      throw new LdappcException("Unable to find resource '" + configResource + "'");
    }

    setUpLdappc(file.getAbsolutePath(), pathToProperties);
  }

  public void testCalculateBushy() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateBushy.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCalculateChildStems() throws Exception {

    setUpLdappc();

    Stem courses = this.edu.addChildStem("courses", "Courses");

    Stem spring = courses.addChildStem("spring", "Spring");
    Stem fall = courses.addChildStem("fall", "Fall");

    spring.addChildGroup("courseA", "Course A");
    spring.addChildGroup("courseB", "Course B");

    fall.addChildGroup("courseA", "Course A");
    fall.addChildGroup("courseB", "Course B");

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateChildStems.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCreateBushy() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.before.ldif");

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushy.after.ldif");
  }

  public void testCreateBushyDryRun() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.before.ldif");

    File ldif = dryRun(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushyDryRun.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCreateBushyDryRunOneStep() throws Exception {

    setUpLdappc();

    ((ConfigManager) ldappc.getConfig()).setProvisionGroupsTwoStep(false);

    loadLdif("CRUDTest.before.ldif");

    File ldif = dryRun(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushyDryRunOneStep.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCreateFlat() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.before.ldif");

    provision(GroupDNStructure.flat);

    verifyLdif("CRUDTest.testCreateFlat.after.ldif");
  }

  public void testCreateSubgroupBushy() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateSubgroupBushy.after.ldif");
  }

  public void testCreateSubgroupBushyNoMemberGroups() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    ((ConfigManager) ldappc.getConfig()).setProvisionMemberGroups(false);

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateSubgroupBushyNoMemberGroups.after.ldif");
  }

  public void testCreateSubgroupFlat() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision(GroupDNStructure.flat);

    verifyLdif("CRUDTest.testCreateSubgroupFlat.after.ldif");
  }

  public void testCreateSubgroupFlatNoMemberGroups() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    ((ConfigManager) ldappc.getConfig()).setProvisionMemberGroups(false);

    provision(GroupDNStructure.flat);

    verifyLdif("CRUDTest.testCreateSubgroupFlatNoMemberGroups.after.ldif");
  }

  public void testCreateSubgroupPhasingFlat() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.before.ldif");

    groupA.addMember(groupB.toSubject());

    provision(GroupDNStructure.flat);

    verifyLdif("CRUDTest.testCreateSubgroupPhasingFlat.after.ldif");
  }

  public void testCreateSubgroupPhasingFlatOneStep() throws Exception {

    setUpLdappc();

    ((ConfigManager) ldappc.getConfig()).setProvisionGroupsTwoStep(false);

    loadLdif("CRUDTest.before.ldif");

    groupA.addMember(groupB.toSubject());

    provision(GroupDNStructure.flat);

    verifyLdif("CRUDTest.testCreateSubgroupPhasingFlatOneStep.after.ldif");
  }

  public void testDeleteGroupsBushy() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.testDeleteGroupsBushy.before.ldif");

    groupA.delete();
    groupB.delete();

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testDeleteGroupsBushy.after.ldif");
  }

  public void testDeleteGroupsBushyDryRun() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.testDeleteGroupsBushy.before.ldif");

    groupA.delete();
    groupB.delete();

    File ldif = dryRun(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testDeleteGroupsBushyDryRun.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testDeleteGroupsBushyDryRunOneStep() throws Exception {

    setUpLdappc();

    ((ConfigManager) ldappc.getConfig()).setProvisionGroupsTwoStep(false);

    loadLdif("CRUDTest.testDeleteGroupsBushy.before.ldif");

    groupA.delete();
    groupB.delete();

    File ldif = dryRun(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testDeleteGroupsBushyDryRunOneStep.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testModifyMemberBushy() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.testModifyMemberBushy.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testModifyMemberBushy.after.ldif");
  }

  public void testModifyMemberBushyDryRun() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.testModifyMemberBushy.before.ldif");

    groupB.addMember(groupA.toSubject());

    File ldif = dryRun(GroupDNStructure.bushy);

    if (ldappc.getConfig().getBundleModifications()) {
      verifyLdif("CRUDTest.testModifyMembersBushyDryRun.ldif", ldif);
    } else {
      verifyLdif("CRUDTest.testModifyMembersBushyDryRun.unbundled.ldif", ldif);
    }

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testModifyEmptyListValueAddMember() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

    setUpLdappc();

    loadLdif("CRUDTest.testModifyEmptyListValueAddMember.before.ldif");

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushy.after.ldif");
  }

  public void testModifyEmptyListValueAddMemberDryRun() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

    setUpLdappc();

    loadLdif("CRUDTest.testModifyEmptyListValueAddMember.before.ldif");

    File ldif = dryRun(GroupDNStructure.bushy);

    if (ldappc.getConfig().getBundleModifications()) {
      verifyLdif("CRUDTest.testModifyEmptyListValueAddMemberDryRun.ldif", ldif);
    } else {
      verifyLdif("CRUDTest.testModifyEmptyListValueAddMemberDryRun.unbundled.ldif", ldif);
    }

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testModifyEmptyListDeleteMember() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

    setUpLdappc();

    loadLdif("CRUDTest.testCreateBushy.after.ldif");

    groupA.deleteMember(SubjectTestHelper.SUBJ0);

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testModifyEmptyListValueDeleteMember.after.ldif");
  }

  public void testModifyEmptyListDeleteMemberDryRun() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

    setUpLdappc();

    loadLdif("CRUDTest.testCreateBushy.after.ldif");

    groupA.deleteMember(SubjectTestHelper.SUBJ0);

    File ldif = dryRun(GroupDNStructure.bushy);

    if (ldappc.getConfig().getBundleModifications()) {
      verifyLdif("CRUDTest.testModifyEmptyListValueDeleteMemberDryRun.ldif", ldif);
    } else {
      verifyLdif("CRUDTest.testModifyEmptyListValueDeleteMemberDryRun.unbundled.ldif",
          ldif);
    }

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCalculateBushyResolverObjectClass() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

    setUpLdappc("ldappc.test.resolverObjectClass.xml");

    loadLdif("CRUDTest.before.ldif");

    try {
      calculate(GroupDNStructure.bushy);
    } catch (LdappcException e) {
      // OK
    }
  }

  public void testCalculateBushySubjectNotFound() throws Exception {

    groupA.addMember(SubjectTestHelper.SUBJ2);

    setUpLdappc();

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateBushySubjectNotFound.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCalculateBushySubjectNotFoundFail() throws Exception {

    groupA.addMember(SubjectTestHelper.SUBJ2);

    setUpLdappc();

    ((ConfigManager) ldappc.getConfig()).getSourceSubjectLdapFilter("jdbc")
        .setOnNotFound(OnNotFound.fail);

    loadLdif("CRUDTest.before.ldif");

    try {
      calculate(GroupDNStructure.bushy);
    } catch (LdappcException e) {
      // OK
    }
  }

  public void testCalculateBushySubjectNotFoundIgnore() throws Exception {

    groupA.addMember(SubjectTestHelper.SUBJ2);

    setUpLdappc();

    ((ConfigManager) ldappc.getConfig()).getSourceSubjectLdapFilter("jdbc")
        .setOnNotFound(OnNotFound.ignore);

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateBushySubjectNotFound.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCalculateBushySubjectNotFoundWarn() throws Exception {

    groupA.addMember(SubjectTestHelper.SUBJ2);

    setUpLdappc();

    ((ConfigManager) ldappc.getConfig()).getSourceSubjectLdapFilter("jdbc")
        .setOnNotFound(OnNotFound.warn);

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateBushySubjectNotFound.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCalculateBushyMultipleSubjects() throws Exception {

    setUpLdappc();

    ((ConfigManager) ldappc.getConfig()).getSourceSubjectLdapFilter("jdbc")
        .setMultipleResults(true);

    loadLdif("CRUDTest.testCalculateBushyMultipleSubjects.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateBushyMultipleSubjects.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCalculateBushyMultipleSubjectsFail() throws Exception {

    setUpLdappc();

    loadLdif("CRUDTest.testCalculateBushyMultipleSubjects.before.ldif");

    try {
      calculate(GroupDNStructure.bushy);
    } catch (LdappcException e) {
      // OK
    }
  }

  public void testCreateBushyMultipleSubjects() throws Exception {

    setUpLdappc();

    ((ConfigManager) ldappc.getConfig()).getSourceSubjectLdapFilter("jdbc")
        .setMultipleResults(true);

    loadLdif("CRUDTest.testCalculateBushyMultipleSubjects.before.ldif");

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushyMultipleSubjects.after.ldif");
  }

  public void testCalculateBushyMultipleObjectClassMapping() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

    setUpLdappc("ldappc.test.multipleObjectClassMapping.xml");

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateBushyMultipleObjectClass.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCreateBushyMultipleObjectClassMapping() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

    setUpLdappc("ldappc.test.multipleObjectClassMapping.xml");

    loadLdif("CRUDTest.before.ldif");

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushyMultipleObjectClass.after.ldif");
  }

  public void testCalculateBushyMultipleObjectClassResolver() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

    setUpLdappc("ldappc.test.multipleObjectClassResolver.xml");

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateBushyMultipleObjectClass.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCreateBushyMultipleObjectClassResolver() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

    setUpLdappc("ldappc.test.multipleObjectClassResolver.xml");

    loadLdif("CRUDTest.before.ldif");

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushyMultipleObjectClass.after.ldif");
  }

  public void testCalculateSubjectNameMap() throws Exception {

    setUpLdappc("ldappc.test.subjectNameMap.xml");

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateSubjectNameMap.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCalculateSubjectIdMap() throws Exception {

    setUpLdappc("ldappc.test.subjectIdMap.xml");

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateSubjectIdMap.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

}
