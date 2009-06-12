package edu.internet2.middleware.ldappc;

import java.io.File;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.ldappc.ProvisionerConfiguration.GroupDNStructure;

public class CRUDTest extends BaseLdappcTestCase {

  private Group groupA;

  private Group groupB;

  public void setUp() {

    super.setUp();

    groupA = StemHelper.addChildGroup(this.edu, "groupA", "Group A");
    groupA.addMember(SubjectTestHelper.SUBJ0);

    groupB = StemHelper.addChildGroup(this.edu, "groupB", "Group B");
    groupB.addMember(SubjectTestHelper.SUBJ1);
    groupB.setDescription("descriptionB");
    groupB.store();
  }

  public void testCalculateBushy() throws Exception {

    setUpLdappc(LDAPPC_TEST_XML);

    loadLdif("CRUDTest.before.ldif");

    File ldif = calculate(GroupDNStructure.bushy);

    verify("CRUDTest.testCalculateBushy.after.ldif", ldif);

    if (!ldif.delete()) {
      fail("could not delete " + ldif.getAbsolutePath());
    }
  }

  public void testCreateBushy() throws Exception {

    setUpLdappc(LDAPPC_TEST_XML);

    loadLdif("CRUDTest.before.ldif");

    provision(GroupDNStructure.bushy);

    verify("CRUDTest.testCreateBushy.after.ldif");
  }

  public void testCreateFlat() throws Exception {

    setUpLdappc(LDAPPC_TEST_XML);

    loadLdif("CRUDTest.before.ldif");

    provision(GroupDNStructure.flat);

    verify("CRUDTest.testCreateFlat.after.ldif");
  }

  public void testCreateSubgroupBushy() throws Exception {

    setUpLdappc(LDAPPC_TEST_XML);

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision(GroupDNStructure.bushy);

    verify("CRUDTest.testCreateSubgroupBushy.after.ldif");
  }

  public void testCreateSubgroupBushyNoMemberGroups() throws Exception {

    setUpLdappc(LDAPPC_TEST_XML);

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    configuration.setProvisionMemberGroups(false);

    provision(GroupDNStructure.bushy);

    verify("CRUDTest.testCreateSubgroupBushyNoMemberGroups.after.ldif");
  }

  public void testCreateSubgroupFlat() throws Exception {

    setUpLdappc(LDAPPC_TEST_XML);

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision(GroupDNStructure.flat);

    verify("CRUDTest.testCreateSubgroupFlat.after.ldif");
  }

  public void testCreateSubgroupFlatNoMemberGroups() throws Exception {

    setUpLdappc(LDAPPC_TEST_XML);

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    configuration.setProvisionMemberGroups(false);

    provision(GroupDNStructure.flat);

    verify("CRUDTest.testCreateSubgroupFlatNoMemberGroups.after.ldif");
  }

  public void testCreateSubgroupPhasingFlat() throws Exception {

    setUpLdappc(LDAPPC_TEST_XML);

    loadLdif("CRUDTest.before.ldif");

    groupA.addMember(groupB.toSubject());

    provision(GroupDNStructure.flat);

    // TODO GRP-275 will need to provision twice
    provision(GroupDNStructure.flat);

    verify("CRUDTest.testCreateSubgroupPhasingFlat.after.ldif");
  }

  public void testDeleteGroupsBushy() throws Exception {

    setUpLdappc(LDAPPC_TEST_XML);

    loadLdif("CRUDTest.testDeleteGroupsBushy.before.ldif");

    groupA.delete();
    groupB.delete();

    provision(GroupDNStructure.bushy);

    verify("CRUDTest.testDeleteGroupsBushy.after.ldif");
  }

  public void testModifyMemberBushy() throws Exception {

    setUpLdappc(LDAPPC_TEST_XML);

    loadLdif("CRUDTest.testModifyMemberBushy.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision(GroupDNStructure.bushy);

    verify("CRUDTest.testModifyMemberBushy.after.ldif");
  }

}
