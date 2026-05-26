package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.loader.OtherJobTemplateInput;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Tests for the GRP-7011 type framework — new GshTemplateType enum
 * values, new GshTemplateMode enum, OtherJobTemplateInput's copyFieldsTo
 * behavior, and a trivial instantiation test for each of the new
 * GrouperTemplate* base classes in the grouper module.
 *
 * The grouper-ui-module base class (GrouperTemplateCustomUi) and its bean
 * (CustomUiTemplateInput) are tested in a separate test in the
 * grouper-ui module.
 *
 * GRP-7011
 */
public class GrouperTemplateFrameworkTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperTemplateFrameworkTest("testGshTemplateModeEnum"));
    TestRunner.run(new GrouperTemplateFrameworkTest("testGshTemplateTypeNewValues"));
    TestRunner.run(new GrouperTemplateFrameworkTest("testOtherJobTemplateInputCopyFieldsToCopiesParentAndSubclassFields"));
    TestRunner.run(new GrouperTemplateFrameworkTest("testOtherJobTemplateInputCopyFieldsToWithNonSubclassTargetCopiesOnlyParent"));
    TestRunner.run(new GrouperTemplateFrameworkTest("testGrouperTemplateDaemonInstantiates"));
    TestRunner.run(new GrouperTemplateFrameworkTest("testGrouperTemplateDaemonChangeLogInstantiates"));
    TestRunner.run(new GrouperTemplateFrameworkTest("testGrouperTemplateReportInstantiates"));
  }

  /**
   *
   */
  public GrouperTemplateFrameworkTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperTemplateFrameworkTest(String name) {
    super(name);
  }

  // ---------- GshTemplateMode ----------

  /**
   * Both enum values resolve case-insensitively. Unknown values throw — the
   * `exceptionOnNotFound` boolean on GrouperUtil.enumValueOfIgnoreCase
   * always throws for unknowns in this version (the flag is misleadingly
   * named); we verify the throw rather than expect null.
   */
  public void testGshTemplateModeEnum() {
    assertEquals(GshTemplateMode.interpreted, GshTemplateMode.valueOfIgnoreCase("interpreted", true));
    assertEquals(GshTemplateMode.interpreted, GshTemplateMode.valueOfIgnoreCase("INTERPRETED", true));
    assertEquals(GshTemplateMode.compiled, GshTemplateMode.valueOfIgnoreCase("compiled", true));
    assertEquals(GshTemplateMode.compiled, GshTemplateMode.valueOfIgnoreCase("Compiled", true));
    try {
      GshTemplateMode.valueOfIgnoreCase("notARealMode", true);
      fail("expected RuntimeException for unknown mode");
    } catch (RuntimeException e) {
      assertTrue("error should list valid values, got: " + e.getMessage(),
          e.getMessage().contains("interpreted") && e.getMessage().contains("compiled"));
    }
  }

  // ---------- GshTemplateType ----------

  /**
   * The 6 new values (daemon, daemonChangeLog, report, customUi, hook,
   * library) added in GRP-7011 are all recognized by valueOfIgnoreCase,
   * alongside the existing 3 (gsh, abac, provisioner).
   */
  public void testGshTemplateTypeNewValues() {
    // existing
    assertEquals(GshTemplateType.gsh, GshTemplateType.valueOfIgnoreCase("gsh", true));
    assertEquals(GshTemplateType.abac, GshTemplateType.valueOfIgnoreCase("abac", true));
    assertEquals(GshTemplateType.provisioner, GshTemplateType.valueOfIgnoreCase("provisioner", true));
    // new in GRP-7011
    assertEquals(GshTemplateType.daemon, GshTemplateType.valueOfIgnoreCase("daemon", true));
    assertEquals(GshTemplateType.daemonChangeLog, GshTemplateType.valueOfIgnoreCase("daemonChangeLog", true));
    assertEquals(GshTemplateType.daemonChangeLog, GshTemplateType.valueOfIgnoreCase("DAEMONCHANGELOG", true));
    assertEquals(GshTemplateType.report, GshTemplateType.valueOfIgnoreCase("report", true));
    assertEquals(GshTemplateType.customUi, GshTemplateType.valueOfIgnoreCase("customUi", true));
    assertEquals(GshTemplateType.hook, GshTemplateType.valueOfIgnoreCase("hook", true));
    assertEquals(GshTemplateType.library, GshTemplateType.valueOfIgnoreCase("library", true));
  }

  // ---------- OtherJobTemplateInput ----------

  /**
   * copyFieldsTo on a template-to-template copy carries both the parent's
   * fields (session, jobName, log) and the subclass's fields (template
   * config id, GshTemplateConfig).
   */
  public void testOtherJobTemplateInputCopyFieldsToCopiesParentAndSubclassFields() {
    OtherJobTemplateInput source = new OtherJobTemplateInput();
    source.setJobName("OTHER_JOB_test");
    Hib3GrouperLoaderLog log = new Hib3GrouperLoaderLog();
    source.setHib3GrouperLoaderLog(log);
    source.setGshTemplateConfigId("templateConfigId123");

    OtherJobTemplateInput target = new OtherJobTemplateInput();
    source.copyFieldsTo(target);

    // parent fields
    assertEquals("OTHER_JOB_test", target.getJobName());
    assertSame(log, target.getHib3GrouperLoaderLog());
    // subclass fields
    assertEquals("templateConfigId123", target.getGshTemplateConfigId());
  }

  /**
   * copyFieldsTo from a template input to a plain OtherJobInput target
   * copies only the parent's fields; the subclass-specific fields are
   * silently skipped because the target can't hold them.
   */
  public void testOtherJobTemplateInputCopyFieldsToWithNonSubclassTargetCopiesOnlyParent() {
    OtherJobTemplateInput source = new OtherJobTemplateInput();
    source.setJobName("OTHER_JOB_test");
    source.setGshTemplateConfigId("templateConfigId123");

    OtherJobInput plainTarget = new OtherJobInput();
    source.copyFieldsTo(plainTarget);

    // parent field copied
    assertEquals("OTHER_JOB_test", plainTarget.getJobName());
    // plainTarget has no place for gshTemplateConfigId; nothing to check
    // beyond the parent field — the test passing without ClassCastException
    // confirms the instanceof guard works.
  }

  // ---------- GrouperTemplate* base classes ----------

  /**
   * A concrete GrouperTemplateDaemon subclass can be defined and its
   * runDaemon method invoked.
   */
  public void testGrouperTemplateDaemonInstantiates() {
    final boolean[] called = new boolean[] { false };
    GrouperTemplateDaemon daemon = new GrouperTemplateDaemon() {
      @Override
      public void runDaemon(OtherJobTemplateInput otherJobTemplateInput) {
        called[0] = true;
      }
    };
    daemon.runDaemon(new OtherJobTemplateInput());
    assertTrue("subclass's runDaemon should have been invoked", called[0]);
  }

  /**
   * A concrete GrouperTemplateDaemonChangeLog subclass can be defined,
   * its processRecords method returns the configured long, and a -1
   * return signals "no advance."
   */
  public void testGrouperTemplateDaemonChangeLogInstantiates() {
    GrouperTemplateDaemonChangeLog ok = new GrouperTemplateDaemonChangeLog() {
      @Override
      public long processRecords(
          edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript script) {
        return 1234567890L;
      }
    };
    assertEquals(1234567890L, ok.processRecords(null));

    GrouperTemplateDaemonChangeLog noAdvance = new GrouperTemplateDaemonChangeLog() {
      @Override
      public long processRecords(
          edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript script) {
        return -1L;
      }
    };
    assertEquals(-1L, noAdvance.processRecords(null));
  }

  /**
   * A concrete GrouperTemplateReport subclass can be defined and its
   * runReport method invoked.
   */
  public void testGrouperTemplateReportInstantiates() {
    final boolean[] called = new boolean[] { false };
    GrouperTemplateReport report = new GrouperTemplateReport() {
      @Override
      public void runReport(
          edu.internet2.middleware.grouper.app.reports.GshReportRuntime gshReportRuntime) {
        called[0] = true;
      }
    };
    report.runReport(null);
    assertTrue("subclass's runReport should have been invoked", called[0]);
  }

}
