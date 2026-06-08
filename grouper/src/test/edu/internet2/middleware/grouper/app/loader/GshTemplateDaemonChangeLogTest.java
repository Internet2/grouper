package edu.internet2.middleware.grouper.app.loader;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Tests for the compiled change-log daemon template type — a compiled
 * GrouperTemplateDaemonChangeLog dispatched from EsbPublisherChangeLogScript
 * via changeLogScriptType=compiledJava.
 *
 * GRP-7030
 */
public class GshTemplateDaemonChangeLogTest extends GrouperTest {

  /**
   * counter the compiled template increments (referenced from the compiled body
   * via the parent classloader)
   */
  public static int count = 0;

  /**
   * gsh template config id the consumer points at
   */
  private static final String TEMPLATE_CONFIG_ID = "testGshTemplateConfig";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateDaemonChangeLogTest("testCompiledChangeLogDaemonProcessesEvents"));
  }

  /**
   * @param name
   */
  public GshTemplateDaemonChangeLogTest(String name) {
    super(name);
  }

  /**
   * Clear registry state between tests so cached classes don't leak.
   */
  @Override
  protected void setUp() {
    super.setUp();
    GshTemplateClassLoaderRegistry.clearCache();
  }

  /**
   * Configure TEMPLATE_CONFIG_ID as a compiled daemonChangeLog template with the
   * given Java source, reusing the sample config scaffolding.
   * @param javaSource the change-log daemon template's Java body
   */
  private void configureChangeLogTemplate(String javaSource) {

    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);

    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateType", "daemonChangeLog");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateMode", "compiled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".gshTemplate", javaSource);
  }

  /**
   * A compiled change-log daemon template fires through the
   * EsbPublisherChangeLogScript compiledJava branch, runs processRecords for the
   * batch, and advances the cursor.
   */
  public void testCompiledChangeLogDaemonProcessesEvents() {

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateDaemonChangeLog;\n"
        + "import edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript;\n"
        + "import edu.internet2.middleware.grouper.app.loader.GshTemplateDaemonChangeLogTest;\n"
        + "import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;\n"
        + "public class TestCompiledChangeLog extends GrouperTemplateDaemonChangeLog {\n"
        + "  public long processRecords(EsbPublisherChangeLogScript esbPublisherChangeLogScript) {\n"
        + "    long lastSequenceProcessed = -1;\n"
        + "    for (EsbEventContainer esbEventContainer : esbPublisherChangeLogScript.getEsbEventContainers()) {\n"
        + "      GshTemplateDaemonChangeLogTest.count++;\n"
        + "      lastSequenceProcessed = esbEventContainer.getSequenceNumber();\n"
        + "    }\n"
        + "    return lastSequenceProcessed;\n"
        + "  }\n"
        + "}\n";

    configureChangeLogTemplate(source);

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myCompiledChangeLog.class").value("edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myCompiledChangeLog.quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myCompiledChangeLog.elfilter").value("(event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD') &&  (event.groupName =~ '^test\\:.*$')").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myCompiledChangeLog.publisher.class").value("edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myCompiledChangeLog.changeLogScriptType").value("compiledJava").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myCompiledChangeLog.gshTemplateConfigId").value(TEMPLATE_CONFIG_ID).store();

    int originalCount = count;

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_myCompiledChangeLog");

    assertEquals(originalCount, count);

    Group group = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_myCompiledChangeLog");

    assertEquals(originalCount + 2, count);
  }

}
