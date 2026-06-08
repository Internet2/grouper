package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.reports.GrouperReportSettings.reportConfigStemName;
import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Tests for the compiled report template type — a compiled GrouperTemplateReport
 * resolved by ReportConfigType.GSH when a gshTemplateConfigId is configured.
 *
 * GRP-7031
 */
public class GshTemplateReportTest extends GrouperTest {

  /**
   * counter the compiled report template increments (referenced via the parent
   * classloader)
   */
  public static int runReportCount = 0;

  /**
   * gsh template config id the report points at
   */
  private static final String TEMPLATE_CONFIG_ID = "testGshTemplateConfig";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateReportTest("testCompiledReportRunsViaReportConfigType"));
  }

  /**
   * @param name
   */
  public GshTemplateReportTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    GshTemplateClassLoaderRegistry.clearCache();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }

  /**
   * Configure TEMPLATE_CONFIG_ID as a compiled report template with the given
   * Java source, reusing the sample config scaffolding.
   * @param javaSource the report template's Java body
   */
  private void configureReportTemplate(String javaSource) {

    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);

    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateType", "report");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateMode", "compiled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".gshTemplate", javaSource);
  }

  /**
   * A compiled report template runs via ReportConfigType.GSH when the report
   * config references it by gshTemplateConfigId, and produces report data.
   */
  public void testCompiledReportRunsViaReportConfigType() {

    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("reporting.storage.option", "fileSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("reporting.file.system.path", GrouperUtil.tmpDir(true) + "grouperReports");

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import java.util.ArrayList;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateReport;\n"
        + "import edu.internet2.middleware.grouper.app.reports.GrouperReportData;\n"
        + "import edu.internet2.middleware.grouper.app.reports.GshReportRuntime;\n"
        + "import edu.internet2.middleware.grouper.app.reports.GshTemplateReportTest;\n"
        + "public class TestCompiledReport extends GrouperTemplateReport {\n"
        + "  public void runReport(GshReportRuntime gshReportRuntime) {\n"
        + "    GshTemplateReportTest.runReportCount++;\n"
        + "    GrouperReportData grouperReportData = gshReportRuntime.getGrouperReportData();\n"
        + "    ArrayList<String> headers = new ArrayList<String>();\n"
        + "    headers.add(\"col1\");\n"
        + "    grouperReportData.setHeaders(headers);\n"
        + "    ArrayList<String[]> rows = new ArrayList<String[]>();\n"
        + "    rows.add(new String[] {\"value1\"});\n"
        + "    grouperReportData.setData(rows);\n"
        + "  }\n"
        + "}\n";

    configureReportTemplate(source);

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();

    AttributeAssign configAttributeAssign = saveCompiledReportConfig(stem0, "test compiled report");
    GrouperReportConfigurationBean reportConfigBean = GrouperReportConfigService.getGrouperReportConfigBean(configAttributeAssign.getId());

    GrouperReportInstance newReportInstance = new GrouperReportInstance();
    newReportInstance.setGrouperReportConfigurationBean(reportConfigBean);
    newReportInstance.setReportInstanceConfigMarkerAssignmentId(reportConfigBean.getAttributeAssignmentMarkerId());
    newReportInstance.setReportInstanceMillisSince1970(System.currentTimeMillis());
    newReportInstance.setReportInstanceDownloadCount(0L);

    int originalCount = runReportCount;

    GrouperReportLogic.runReport(reportConfigBean, newReportInstance, stem0);

    assertEquals(originalCount + 1, runReportCount);
    assertEquals(GrouperReportInstance.STATUS_SUCCESS, newReportInstance.getReportInstanceStatus());
    assertNotNull(newReportInstance.getReportInstanceRows());
  }

  /**
   * Build a GSH report config that references a compiled GSH template by
   * gshTemplateConfigId (no inline script).
   * @param stem owner stem
   * @param reportConfigName report name
   * @return the config attribute assignment
   */
  private static AttributeAssign saveCompiledReportConfig(Stem stem, String reportConfigName) {

    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperReportConfigAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_DESCRIPTION, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "test description");

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_EMAIL_BODY, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "test email body");

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_EMAIL_SUBJECT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "email subject");

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_ENABLED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(true));

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FILE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "file name");

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FORMAT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "CSV");

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigName);

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_QUARTZ_CRON, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "0 */2 * ? * *");

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_GSH_TEMPLATE_CONFIG_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), TEMPLATE_CONFIG_ID);

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(false));

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_VIEWERS, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(false));

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_TYPE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "GSH");

    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_VIEWERS_GROUP_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abcdef");

    attributeAssign.saveOrUpdate();

    return attributeAssign;
  }

}
