package edu.internet2.middleware.ldappc;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;

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

  public void testCreateBushy() throws Exception {

    setUpLdappc(LDAPPC_BUSHY_XML);

    loadLdif("CRUDTest.before.ldif");

    provision();

    verify("CRUDTest.testCreateBushy.after.ldif");
  }

  public void testCreateFlat() throws Exception {

    setUpLdappc(LDAPPC_FLAT_XML);

    loadLdif("CRUDTest.before.ldif");

    provision();

    verify("CRUDTest.testCreateFlat.after.ldif");
  }

  public void testCreateSubgroupBushy() throws Exception {
  
    setUpLdappc(LDAPPC_BUSHY_XML);
  
    loadLdif("CRUDTest.before.ldif");
  
    groupB.addMember(groupA.toSubject());
  
    provision();
  
    verify("CRUDTest.testCreateSubgroupBushy.after.ldif");
  }

  public void testCreateSubgroupFlat() throws Exception {

    setUpLdappc(LDAPPC_FLAT_XML);

    loadLdif("CRUDTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision();

    verify("CRUDTest.testCreateSubgroupFlat.after.ldif");
  }
  
  public void testCreateSubgroupPhasingFlat() throws Exception {

    setUpLdappc(LDAPPC_FLAT_XML);

    loadLdif("CRUDTest.before.ldif");

    groupA.addMember(groupB.toSubject());

    provision();

    // TODO GRP-275 will need to provision twice
    provision();

    verify("CRUDTest.testCreateSubgroupPhasingFlat.after.ldif");
  }

  public void testDeleteGroupsBushy() throws Exception {

    setUpLdappc(LDAPPC_BUSHY_XML);

    loadLdif("CRUDTest.testDeleteGroupsBushy.before.ldif");

    groupA.delete();
    groupB.delete();

    provision();

    verify("CRUDTest.testDeleteGroupsBushy.after.ldif");
  }

  public void testModifyMemberBushy() throws Exception {

    setUpLdappc(LDAPPC_BUSHY_XML);

    loadLdif("CRUDTest.testModifyMemberBushy.before.ldif");

    groupB.addMember(groupA.toSubject());

    provision();

    verify("CRUDTest.testModifyMemberBushy.after.ldif");
  }

}
