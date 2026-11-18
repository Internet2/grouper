package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * <pre>
 * Advisory H4 (gshTemplateFailedRunNotAudited): a V2 GSH template with useIndividualAudits=false
 * and runGshInTransaction=false that makes a change and then throws must still write a
 * GSH_TEMPLATE_EXEC audit with status error (the change stays committed, and the inner calls
 * did not audit).
 *
 * Controls: the same template without the throw writes exactly one success audit; a
 * transactional run that throws is rolled back (no membership, no audit, as before).
 *
 * Before the fix the error-audit assertions fail (no GSH_TEMPLATE_EXEC row for the failed run).
 * The template body is a V2 class; on releases with compiled templates the compiled test runs
 * it as real Java, on older ones it runs as an interpreted V2 template.
 * </pre>
 */
public class GshTemplateFailedRunAuditTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateFailedRunAuditTest("testNonTransactionalFailedRunAudited"));
  }

  /** */
  public GshTemplateFailedRunAuditTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateFailedRunAuditTest(String name) {
    super(name);
  }

  /** config id of the sample template */
  private static final String CONFIG_ID = "testGshTemplateConfig";

  /** config prefix */
  private static final String PREFIX = "grouperGshTemplate." + CONFIG_ID + ".";

  /** root session */
  private GrouperSession rootSession;

  /** group the template adds SUBJ1 to */
  private Group targetGroup;

  /** owner folder, SUBJ2 is folder admin */
  private Stem ownerStem;

  @Override
  protected void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();

    // sample template scaffolding (same file GshTemplateExecTest uses)
    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);
    for (String keyValue: GrouperUtil.splitFileLines(templateConfigLines)) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "input.0.required", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "templateVersion", "V2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "useIndividualAudits", "false");

    this.targetGroup = new GroupSave(this.rootSession).assignName("test:gshAuditTarget")
        .assignCreateParentStemsIfNotExist(true).save();
    this.ownerStem = new StemSave(this.rootSession).assignName("test2").save();
    this.ownerStem.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ADMIN, false);
  }

  @Override
  protected void tearDown() {
    GshTemplateClassLoaderRegistryClear.clear();
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  /**
   * clears the compiled template cache if this release has one (no compile dependency on it)
   */
  private static class GshTemplateClassLoaderRegistryClear {
    /** */
    static void clear() {
      try {
        Class.forName("edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry")
          .getMethod("clearCache").invoke(null);
      } catch (Exception e) {
        // older release without compiled templates
      }
    }
  }

  // ---------------------------------------------------------------- helpers

  /**
   * a V2 template body that adds SUBJ1 to the target group, then optionally throws
   * @param className
   * @param throwAfterChange
   * @return the source
   */
  private static String templateSource(String className, boolean throwAfterChange) {
    return ""
        + "import edu.internet2.middleware.grouper.GroupFinder;\n"
        + "import edu.internet2.middleware.grouper.GrouperSession;\n"
        + "import edu.internet2.middleware.grouper.SubjectFinder;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class " + className + " extends GshTemplateV2 {\n"
        + "  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {\n"
        + "    GroupFinder.findByName(GrouperSession.staticGrouperSession(), \"test:gshAuditTarget\", true)\n"
        + "      .addMember(SubjectFinder.findById(\"test.subject.1\", true), false);\n"
        + (throwAfterChange ? "    if (true) { throw new RuntimeException(\"boom-after-change\"); }\n" : "")
        + "  }\n"
        + "}\n";
  }

  /**
   * run the sample template as SUBJ2 (folder admin, not wheel)
   * @return output
   */
  private GshTemplateExecOutput runAsSubj2() {
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId(CONFIG_ID);
    exec.assignCurrentUser(SubjectTestHelper.SUBJ2);
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(this.ownerStem.getName());
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    // assignValueString: every release has it (assignValue(Object) is 5.18.0+), same effect
    input.assignValueString(null);
    exec.addGshTemplateInput(input);
    try {
      return exec.execute();
    } catch (RuntimeException re) {
      // some releases rethrow instead of putting the exception in the output
      return null;
    }
  }

  /**
   * GSH_TEMPLATE_EXEC audits for the sample template with a status
   * @param status success or error
   * @return count
   */
  private static int execAudits(String status) {
    List<AuditEntry> audits = HibernateSession.byHqlStatic()
        .createQuery("from AuditEntry ae where ae.description like :theDescription")
        .setString("theDescription", "Execute gsh template %with configId: " + CONFIG_ID + ", status: " + status + "%")
        .list(AuditEntry.class);
    return GrouperUtil.length(audits);
  }

  /**
   * run a throwing non-transactional template and check the change stays and an error audit exists
   */
  private void assertFailedRunAudited() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "runGshInTransaction", "false");

    GshTemplateExecOutput output = runAsSubj2();
    if (output != null) {
      assertFalse(output.isSuccess());
    }

    // non-transactional: the change made before the exception is committed
    assertTrue(this.targetGroup.hasMember(SubjectTestHelper.SUBJ1));
    // ... so the run must be recorded
    assertEquals("failed non-transactional run must write an error audit", 1, execAudits("error"));
    assertEquals(0, execAudits("success"));
  }

  // ---------------------------------------------------------------- tests

  /**
   * interpreted V2 template (every release with V2 templates)
   */
  public void testNonTransactionalFailedRunAudited() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "gshTemplate", templateSource("TestFailedRunAuditInterpreted", true));
    assertFailedRunAudited();
  }

  /**
   * compiled V2 template (7.3.0 and later; skipped on older releases, which have no compiled templates)
   */
  public void testNonTransactionalFailedRunAuditedCompiled() {
    // compiled templates exist from 7.3.0; before that templateMode=compiled is not a setting, the
    // Java source would run as a GSH script and fail on its package line, so there is nothing to test
    try {
      Class.forName("edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry");
    } catch (ClassNotFoundException cnfe) {
      System.out.println("skip testNonTransactionalFailedRunAuditedCompiled: no compiled GSH templates in this release");
      return;
    }
    GshTemplateClassLoaderRegistryClear.clear();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "templateMode", "compiled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "gshTemplate",
        "package edu.internet2.middleware.grouper.gshTest;\n" + templateSource("TestFailedRunAuditCompiled", true));
    assertFailedRunAudited();
  }

  /**
   * control: a successful non-transactional run writes one success audit and no error audit
   */
  public void testNonTransactionalSuccessAuditedOnce() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "runGshInTransaction", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "gshTemplate", templateSource("TestFailedRunAuditSuccess", false));

    GshTemplateExecOutput output = runAsSubj2();
    assertNotNull(output);
    assertTrue(output.isSuccess());
    assertTrue(this.targetGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertEquals(1, execAudits("success"));
    assertEquals(0, execAudits("error"));
  }

  /**
   * control: a transactional run that throws is rolled back; behavior unchanged (no membership,
   * no audit, since nothing persisted)
   */
  public void testTransactionalFailedRunRolledBack() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "runGshInTransaction", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "gshTemplate", templateSource("TestFailedRunAuditTransactional", true));

    GshTemplateExecOutput output = runAsSubj2();
    if (output != null) {
      assertFalse(output.isSuccess());
    }
    assertFalse(this.targetGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertEquals(0, execAudits("success"));
    assertEquals(0, execAudits("error"));
  }

}
