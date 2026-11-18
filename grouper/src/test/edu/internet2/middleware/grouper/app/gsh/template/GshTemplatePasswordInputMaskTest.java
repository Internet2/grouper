package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * <pre>
 * Advisory H4 (gshTemplatePasswordInputsStoredInAudit): the value of a GSH template input with
 * formElementType = password must not be written in clear text to the GSH_TEMPLATE_EXEC audit
 * description, nor to the exception text that is built when an input showEl script fails.
 *
 * Before the fix: testPasswordInputMaskedInAudit finds the secret in the audit description, and
 * testPasswordInputMaskedInShowElException finds it in the exception text (and the test does not
 * compile, since GshTemplateInputMask is new).
 *
 * Controls: a text input in the same run is still audited with its value; the template itself
 * still gets the real password value.
 *
 * Uses a V1 (GSH script) template and the root subject as runner so it works the same on every
 * release from 2.6 (no privileges, no V2 or compiled templates needed).
 * </pre>
 */
public class GshTemplatePasswordInputMaskTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplatePasswordInputMaskTest("testPasswordInputMaskedInAudit"));
  }

  /** */
  public GshTemplatePasswordInputMaskTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplatePasswordInputMaskTest(String name) {
    super(name);
  }

  /** config id of the test template */
  private static final String CONFIG_ID = "testGshTemplatePasswordMask";

  /** config prefix */
  private static final String PREFIX = "grouperGshTemplate." + CONFIG_ID + ".";

  /** the secret typed into the password input */
  private static final String SECRET = "S3cretPassValue176";

  /** the value typed into the text input */
  private static final String PLAIN = "plainValue176";

  /** root session */
  private GrouperSession rootSession;

  /** owner folder */
  private Stem ownerStem;

  /**
   * set a config value the test JVM sees right away
   * @param key
   * @param value
   */
  private static void config(String key, String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(key, value);
  }

  @Override
  protected void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    this.ownerStem = new StemSave(this.rootSession).assignName("test176").save();

    // a folder template run by anyone, as GrouperSystem, no transaction so the audit is always written
    config(PREFIX + "enabled", "true");
    config(PREFIX + "templateName", "Password mask test");
    config(PREFIX + "templateDescription", "Password mask test");
    config(PREFIX + "showOnFolders", "true");
    config(PREFIX + "folderShowType", "allFolders");
    config(PREFIX + "securityRunType", "everyone");
    config(PREFIX + "runAsType", "GrouperSystem");
    config(PREFIX + "runGshInTransaction", "false");
    // GSH (V1) body that fails the run unless it got the real password: the mask is for audit and logs only
    config(PREFIX + "gshTemplate", "if (!\"" + SECRET + "\".equals(gsh_input_secret)) { throw new RuntimeException(\"template did not get the real password\"); }");
    config(PREFIX + "numberOfInputs", "2");

    config(PREFIX + "input.0.name", "gsh_input_secret");
    config(PREFIX + "input.0.label", "Secret");
    config(PREFIX + "input.0.description", "Secret");
    config(PREFIX + "input.0.type", "string");
    config(PREFIX + "input.0.formElementType", "password");
    config(PREFIX + "input.0.required", "false");
    config(PREFIX + "input.0.validationType", "none");

    config(PREFIX + "input.1.name", "gsh_input_plain");
    config(PREFIX + "input.1.label", "Plain");
    config(PREFIX + "input.1.description", "Plain");
    config(PREFIX + "input.1.type", "string");
    config(PREFIX + "input.1.formElementType", "text");
    config(PREFIX + "input.1.required", "false");
    config(PREFIX + "input.1.validationType", "none");
  }

  @Override
  protected void tearDown() {
    // remove every override this test set
    for (String key : new java.util.ArrayList<String>(GrouperConfig.retrieveConfig().propertiesOverrideMap().keySet())) {
      if (key.startsWith(PREFIX)) {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(key);
      }
    }
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  /**
   * run the template as root with both inputs filled in
   * @return the output
   */
  private GshTemplateExecOutput runTemplate() {
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId(CONFIG_ID);
    exec.assignCurrentUser(SubjectFinder.findRootSubject());
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(this.ownerStem.getName());

    // assignValueString: every release has it (assignValue(Object) is 5.18.0+)
    GshTemplateInput secretInput = new GshTemplateInput();
    secretInput.assignName("gsh_input_secret");
    secretInput.assignValueString(SECRET);
    exec.addGshTemplateInput(secretInput);

    GshTemplateInput plainInput = new GshTemplateInput();
    plainInput.assignName("gsh_input_plain");
    plainInput.assignValueString(PLAIN);
    exec.addGshTemplateInput(plainInput);
    return exec.execute();
  }

  /**
   * descriptions of the GSH_TEMPLATE_EXEC audits of the test template
   * @return the descriptions joined
   */
  private static String execAuditDescriptions() {
    List<AuditEntry> audits = HibernateSession.byHqlStatic()
        .createQuery("from AuditEntry ae where ae.description like :theDescription")
        .setString("theDescription", "Execute gsh template %with configId: " + CONFIG_ID + ",%")
        .list(AuditEntry.class);
    StringBuilder result = new StringBuilder();
    for (AuditEntry auditEntry : GrouperUtil.nonNull(audits)) {
      result.append(auditEntry.getDescription()).append("\n");
    }
    return result.toString();
  }

  /**
   * the audit of a run names the password input but not its value; a text input keeps its value
   */
  public void testPasswordInputMaskedInAudit() {
    GshTemplateExecOutput output = runTemplate();
    assertNotNull(output);
    assertTrue("run should succeed (the template checks it got the real password): "
        + (output.getException() == null ? "" : GrouperUtil.getFullStackTrace(output.getException())), output.isSuccess());

    String descriptions = execAuditDescriptions();
    assertTrue("expected an audit for the run", descriptions.length() > 0);
    assertFalse("password input value must not be in the audit: " + descriptions, descriptions.contains(SECRET));
    assertTrue(descriptions, descriptions.contains("gsh_input_secret = " + GshTemplateInputMask.MASKED_VALUE));
    // control: text inputs are still audited
    assertTrue(descriptions, descriptions.contains("gsh_input_plain = " + PLAIN));
  }

  /**
   * when an input showEl script fails, the variable map put in the exception text masks the password
   */
  public void testPasswordInputMaskedInShowElException() {
    // throws while showEl is evaluated, after the inputs are in the variable map
    config(PREFIX + "input.1.showEl", "${grouperUtil.assertion(false, 'showEl failed on purpose')}");

    GshTemplateExecOutput output = null;
    String exceptionText = null;
    try {
      output = runTemplate();
      if (output != null && output.getException() != null) {
        exceptionText = GrouperUtil.getFullStackTrace(output.getException());
      }
    } catch (RuntimeException re) {
      exceptionText = GrouperUtil.getFullStackTrace(re);
    }
    assertNotNull("showEl exception expected", exceptionText);
    assertTrue(exceptionText, exceptionText.contains("showEl failed on purpose"));
    assertFalse("password input value must not be in the exception text: " + exceptionText, exceptionText.contains(SECRET));
    // control: text inputs are still shown
    assertTrue(exceptionText, exceptionText.contains(PLAIN));
  }

  /**
   * the helper masks only password inputs, does not change the map it is given, and leaves empty values
   */
  public void testHelper() {
    GshTemplateConfig templateConfig = new GshTemplateConfig(CONFIG_ID);
    templateConfig.populateConfiguration();

    assertTrue(GshTemplateInputMask.isPasswordInput(templateConfig, "gsh_input_secret"));
    assertFalse(GshTemplateInputMask.isPasswordInput(templateConfig, "gsh_input_plain"));
    assertFalse(GshTemplateInputMask.isPasswordInput(templateConfig, "gsh_input_notConfigured"));
    assertFalse(GshTemplateInputMask.isPasswordInput(null, "gsh_input_secret"));

    assertEquals(GshTemplateInputMask.MASKED_VALUE, GshTemplateInputMask.valueForLog(templateConfig, "gsh_input_secret", SECRET));
    assertEquals("", GshTemplateInputMask.valueForLog(templateConfig, "gsh_input_secret", ""));
    assertNull(GshTemplateInputMask.valueForLog(templateConfig, "gsh_input_secret", null));
    assertEquals(PLAIN, GshTemplateInputMask.valueForLog(templateConfig, "gsh_input_plain", PLAIN));

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("gsh_input_secret", SECRET);
    variableMap.put("gsh_input_plain", PLAIN);
    Map<String, Object> forLog = GshTemplateInputMask.variableMapForLog(templateConfig, variableMap);
    assertEquals(GshTemplateInputMask.MASKED_VALUE, forLog.get("gsh_input_secret"));
    assertEquals(PLAIN, forLog.get("gsh_input_plain"));
    // the template keeps the real value
    assertEquals(SECRET, variableMap.get("gsh_input_secret"));
    assertNull(GshTemplateInputMask.variableMapForLog(templateConfig, null));
  }

}
