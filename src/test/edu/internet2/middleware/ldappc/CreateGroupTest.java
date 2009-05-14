package edu.internet2.middleware.ldappc;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;

public class CreateGroupTest extends BaseLdappcTestCase {

  public void setUp() {

    super.setUp();

    Group groupA = StemHelper.addChildGroup(this.edu, "groupA", "Group A");
    groupA.addMember(SubjectTestHelper.SUBJ0);

    Group groupB = StemHelper.addChildGroup(this.edu, "groupB", "Group B");
    groupB.addMember(SubjectTestHelper.SUBJ1);
    groupB.setDescription("descriptionB");
    groupB.store();
  }

  public void testSimpleBushy() throws Exception {

    setUpLdappc(LDAPPC_BUSHY_XML);
    ldifLoad(ldifBeforeFile(Thread.currentThread().getStackTrace()));
    provision();
    verify(ldifAfterFile(Thread.currentThread().getStackTrace()));
  }

  public void testSimpleFlat() throws Exception {

    setUpLdappc(LDAPPC_FLAT_XML);
    ldifLoad(ldifBeforeFile(Thread.currentThread().getStackTrace()));
    provision();
    verify(ldifAfterFile(Thread.currentThread().getStackTrace()));
  }

}
