package edu.internet2.middleware.ldappc;

import java.io.File;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;

public class CLITest extends BaseLdappcTestCase {

  private Group groupA;

  private Group groupB;

  private static String config = GrouperUtil.fileFromResourceName(LDAPPC_TEST_XML)
      .getAbsolutePath();

  public static void main(String[] args) {
    // TestRunner.run(new CLITest("testCreateBushyDryRun"));
  }

  public CLITest(String name) {
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
      fail("An error occurred : " + e);
    }
  }

  private File getFile(String fileName) {
    return LdappcTestHelper.getFile(this, fileName);
  }

  private void loadLdif(String file) throws Exception {
    LdappcTestHelper.loadLdif(getFile(file), ldapContext);
  }

  public void testPrintUsage() {
    Ldappc.main(new String[] {});
  }

  public void testNeitherGroupsNorMembershipsOption() {
    try {
      String[] args = { "-c", config };
      Ldappc.main(args);
    } catch (LdappcException e) {
      // OK
    }
  }

  public void testCalculateBushy() throws Exception {

    loadLdif("CRUDTest.before.ldif");

    File tmpFile = File.createTempFile("ldappcCLITest", null);
    String ldifPath = tmpFile.getAbsolutePath();
    tmpFile.delete();

    String[] args = { "-g", "-m", "-c", config, "-calc", ldifPath };

    Ldappc.main(args);

    File ldifFile = new File(ldifPath);

    LdappcTestHelper.verifyLdif(getFile("CRUDTest.testCalculateBushy.after.ldif"),
        ldifFile);

    if (!ldifFile.delete()) {
      fail("could not delete " + ldifFile.getAbsolutePath());
    }
  }

  public void testCreateBushyDryRun() throws Exception {

    loadLdif("CRUDTest.before.ldif");

    File tmpFile = File.createTempFile("ldappcCLITest", null);
    String ldifPath = tmpFile.getAbsolutePath();
    tmpFile.delete();

    String[] args = { "-g", "-m", "-c", config, "-n", ldifPath };

    Ldappc.main(args);

    File ldifFile = new File(ldifPath);

    LdappcTestHelper.verifyLdif(getFile("CRUDTest.testCreateBushyDryRun.ldif"), ldifFile);
  }
}
