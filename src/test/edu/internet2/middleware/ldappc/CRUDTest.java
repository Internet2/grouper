package edu.internet2.middleware.ldappc;

import java.io.File;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;

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
      setUpLdappc(pathToConfig, pathToProperties);
    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }
  }

  public void testCalculateBushy() throws Exception {

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCalculateBushy.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCalculateChildStems() throws Exception {

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

    loadLdif("CRUDTest.before.ldif");

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushy.after.ldif");
  }

  public void testCreateBushyDryRun() throws Exception {

    loadLdif("CRUDTest.before.ldif");

    File ldif = dryRun(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushyDryRun.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCreateBushyDryRunOneStep() throws Exception {

    ((ConfigManager) ldappc.getConfig()).setProvisionGroupsTwoStep(false);

    loadLdif("CRUDTest.before.ldif");

    File ldif = dryRun(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushyDryRunOneStep.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCreateBushyWriteLdif() throws Exception {

    File tmpFile = File.createTempFile("ldappcTestCreateBushyWriteLdif", null);
    String tmpPath = tmpFile.getAbsolutePath();
    tmpFile.delete();

    ldappc.getOptions().setWriteLdif(true);
    ldappc.getOptions().setOutputFileLocation(tmpPath);

    loadLdif("CRUDTest.before.ldif");

    File ldif = provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushy.after.ldif");

    if (ldappc.getConfig().getBundleModifications()) {
      verifyLdif("CRUDTest.testCreateBushyWriteLdif.ldif", ldif);
    } else {
      verifyLdif("CRUDTest.testCreateBushyWriteLdif.unbundled.ldif", ldif);
    }

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCreateFlat() throws Exception {

    loadLdif("CRUDTest.before.ldif");

    provision(GroupDNStructure.flat);

    verifyLdif("CRUDTest.testCreateFlat.after.ldif");
  }

  public void testCreateSubgroupBushy() throws Exception {

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateSubgroupBushy.after.ldif");
  }

  public void testCreateSubgroupBushyNoMemberGroups() throws Exception {

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    ((ConfigManager) ldappc.getConfig()).setProvisionMemberGroups(false);

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateSubgroupBushyNoMemberGroups.after.ldif");
  }

  public void testCreateSubgroupFlat() throws Exception {

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision(GroupDNStructure.flat);

    verifyLdif("CRUDTest.testCreateSubgroupFlat.after.ldif");
  }

  public void testCreateSubgroupFlatNoMemberGroups() throws Exception {

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    ((ConfigManager) ldappc.getConfig()).setProvisionMemberGroups(false);

    provision(GroupDNStructure.flat);

    verifyLdif("CRUDTest.testCreateSubgroupFlatNoMemberGroups.after.ldif");
  }

  public void testCreateSubgroupPhasingFlat() throws Exception {

    loadLdif("CRUDTest.before.ldif");

    groupA.addMember(groupB.toSubject());

    provision(GroupDNStructure.flat);

    verifyLdif("CRUDTest.testCreateSubgroupPhasingFlat.after.ldif");
  }

  public void testCreateSubgroupPhasingFlatOneStep() throws Exception {

    ((ConfigManager) ldappc.getConfig()).setProvisionGroupsTwoStep(false);

    loadLdif("CRUDTest.before.ldif");

    groupA.addMember(groupB.toSubject());

    provision(GroupDNStructure.flat);

    verifyLdif("CRUDTest.testCreateSubgroupPhasingFlatOneStep.after.ldif");
  }

  public void testDeleteGroupsBushy() throws Exception {

    loadLdif("CRUDTest.testDeleteGroupsBushy.before.ldif");

    groupA.delete();
    groupB.delete();

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testDeleteGroupsBushy.after.ldif");
  }

  public void testDeleteGroupsBushyDryRun() throws Exception {

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

    loadLdif("CRUDTest.testModifyMemberBushy.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testModifyMemberBushy.after.ldif");
  }

  public void testModifyMemberBushyDryRun() throws Exception {

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

    loadLdif("CRUDTest.testModifyEmptyListValueAddMember.before.ldif");

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testCreateBushy.after.ldif");
  }

  public void testModifyEmptyListValueAddMemberDryRun() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

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

    loadLdif("CRUDTest.testCreateBushy.after.ldif");

    groupA.deleteMember(SubjectTestHelper.SUBJ0);

    provision(GroupDNStructure.bushy);

    verifyLdif("CRUDTest.testModifyEmptyListValueDeleteMember.after.ldif");
  }

  public void testModifyEmptyListDeleteMemberDryRun() throws Exception {

    if (useActiveDirectory()) {
      return;
    }

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

}
