package edu.internet2.middleware.grouper.app.gsh.template;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.file.GrouperFile;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAOFactory;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * GRP-7438: GSH templates can hand the user a file to download.
 *
 * <p>The main use case is a daily report: the template looks for the grouper_file row for
 * today, and if it is not there, computes the CSV and saves it.  Either way the row is marked
 * for download.  Files are stored as plain (unencrypted) text with system_name
 * gshTemplateDownload and file_path /gshTemplateDownload/&lt;templateConfigId&gt;/&lt;yyyy-MM-dd&gt;/&lt;fileName&gt;,
 * so the date in the path drives both the daily lookup and the retention cleanup.</p>
 *
 * <p>A template can also point at an existing grouper_file row (any system name); that row is
 * not copied and is never deleted by the cleanup.  There is no central cleanup: each template
 * deletes its own old files.</p>
 */
public class GshTemplateDownloadFileTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateDownloadFileTest("testDailyReportComputeThenCache"));
  }

  /**
   *
   */
  public GshTemplateDownloadFileTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateDownloadFileTest(String name) {
    super(name);
  }

  /** template config id used by these tests (from test-gsh-template-config.properties) */
  private static final String CONFIG_ID = "testGshTemplateConfig";

  /** number of data rows in the generated csv, enough to push contents past varchar into the clob */
  private static final int CSV_ROW_COUNT = 5000;

  /**
   * Compiled template for the daily report use case.  The report date comes in through the
   * sample template's single string input (gsh_input_myExtension) so the test can control
   * "today".  The csv has a first line with System.nanoTime() so a recompute would produce
   * different contents than the cached row.
   */
  private static final String DAILY_REPORT_TEMPLATE_SOURCE = ""
      + "package edu.internet2.middleware.grouper.gshTest;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
      + "public class TestDownloadDailyReportTemplate extends GshTemplateV2 {\n"
      + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {\n"
      + "    GshTemplateOutput gshTemplateOutput = out.getGsh_builtin_gshTemplateOutput();\n"
      + "    String reportDate = in.getGsh_builtin_inputString(\"gsh_input_myExtension\");\n"
      + "    String fileName = \"mfaTextCallbackReport_\" + reportDate + \".csv\";\n"
      + "    // is the file for this day already there?\n"
      + "    String existingFileId = gshTemplateOutput.retrieveDownloadFileId(reportDate, fileName);\n"
      + "    if (existingFileId != null) {\n"
      + "      gshTemplateOutput.assignDownloadGrouperFileId(existingFileId);\n"
      + "      gshTemplateOutput.addOutputLine(\"cached\");\n"
      + "      return;\n"
      + "    }\n"
      + "    // not there, compute it\n"
      + "    StringBuilder csv = new StringBuilder();\n"
      + "    csv.append(\"\\\"computedAt\\\",\\\"\" + System.nanoTime() + \"\\\"\\n\");\n"
      + "    csv.append(\"\\\"user.name\\\",\\\"factor\\\",\\\"retiring_auth_count\\\"\\n\");\n"
      + "    for (int i = 0; i < " + CSV_ROW_COUNT + "; i++) {\n"
      + "      csv.append(\"\\\"user\" + i + \"\\\",\\\"sms_passcode\\\",\\\"\" + (i % 100) + \"\\\"\\n\");\n"
      + "    }\n"
      + "    gshTemplateOutput.assignDownloadFile(reportDate, fileName, csv.toString());\n"
      + "    gshTemplateOutput.addOutputLine(\"computed\");\n"
      + "  }\n"
      + "}\n";

  /**
   * Load the sample template config and flip it to compiled mode with the given java source.
   * The single input is made optional and long enough for a date.
   * @param javaSource
   */
  private static void configureCompiledTemplate(String javaSource) {
    GshTemplateClassLoaderRegistry.clearCache();

    String templateConfigLines = GrouperUtil.readResourceIntoString(
        "edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);

    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + CONFIG_ID + ".templateMode", "compiled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + CONFIG_ID + ".input.0.required", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + CONFIG_ID + ".gshTemplate", javaSource);
  }

  /**
   * Run the configured template from folder test2 as GrouperSystem.
   * @param inputValue value for gsh_input_myExtension (the report date for the daily template)
   * @return the output, asserted successful
   */
  private static GshTemplateExecOutput runTemplate(String inputValue) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Stem ownerStem = new StemSave(grouperSession).assignName("test2").save();

    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId(CONFIG_ID);
    exec.assignCurrentUser(SubjectFinder.findRootSubject());
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(ownerStem.getName());

    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    input.assignValue(inputValue);
    exec.addGshTemplateInput(input);

    GshTemplateExecOutput output = exec.execute();

    if (!output.isSuccess() && output.getException() != null) {
      output.getException().printStackTrace();
    }
    assertTrue("template should run successfully", output.isSuccess());
    return output;
  }

  /**
   * @param systemName
   * @return number of grouper_file rows with this system name
   */
  private static int countFiles(String systemName) {
    return new GcDbAccess().sql("select count(*) from grouper_file where system_name = ?")
        .addBindVar(systemName).select(int.class);
  }

  /**
   * Save a grouper_file row the way other features do (not through the download api).
   * @param systemName
   * @param filePath
   * @param fileName
   * @param contents
   * @return the saved row
   */
  private static GrouperFile saveOtherGrouperFile(String systemName, String filePath, String fileName, String contents) {
    GrouperFile grouperFile = new GrouperFile();
    grouperFile.setId(GrouperUuid.getUuid());
    grouperFile.setSystemName(systemName);
    grouperFile.setFilePath(filePath);
    grouperFile.setFileName(fileName);
    grouperFile.setValueToSave(contents);
    Hib3DAOFactory.getFactory().getGrouperFile().saveOrUpdate(grouperFile);
    return grouperFile;
  }

  /**
   * Daily report: first run computes and saves, second run on the same day reuses the row
   * (same id, same contents, no recompute), a run for another day creates a new row.
   */
  public void testDailyReportComputeThenCache() {

    // given
    GrouperSession.startRootSession();
    configureCompiledTemplate(DAILY_REPORT_TEMPLATE_SOURCE);

    String day1 = "2026-10-04";
    String day2 = "2026-10-05";
    String fileName1 = "mfaTextCallbackReport_" + day1 + ".csv";

    assertEquals(0, countFiles(GshTemplateDownloadFile.SYSTEM_NAME));

    // when: first run for day1
    GshTemplateExecOutput output1 = runTemplate(day1);

    // then: computed, one row, marked for download
    assertEquals(1, output1.getGshTemplateOutput().getOutputLines().size());
    assertEquals("computed", output1.getGshTemplateOutput().getOutputLines().get(0).getText());
    assertEquals(1, countFiles(GshTemplateDownloadFile.SYSTEM_NAME));

    String fileId1 = output1.getGshTemplateOutput().getDownloadGrouperFileId();
    assertTrue(StringUtils.isNotBlank(fileId1));

    GrouperFile grouperFile1 = Hib3DAOFactory.getFactory().getGrouperFile().findById(fileId1, true);
    assertEquals(GshTemplateDownloadFile.SYSTEM_NAME, grouperFile1.getSystemName());
    assertEquals(fileName1, grouperFile1.getFileName());
    assertEquals("/gshTemplateDownload/" + CONFIG_ID + "/" + day1 + "/" + fileName1, grouperFile1.getFilePath());
    assertEquals(GshTemplateDownloadFile.filePath(CONFIG_ID, day1, fileName1), grouperFile1.getFilePath());

    // stored as plain text (not encrypted), and big enough that it went to the clob
    String contents1 = grouperFile1.retrieveValue();
    assertTrue("csv ends with a newline", contents1.endsWith("\n"));
    // splitFileLines returns an empty last line after the trailing newline, so strip it first
    List<String> lines = GrouperUtil.splitFileLines(StringUtils.removeEnd(contents1, "\n"));
    assertEquals(2 + CSV_ROW_COUNT, lines.size());
    assertTrue(lines.get(0), lines.get(0).startsWith("\"computedAt\","));
    assertEquals("\"user.name\",\"factor\",\"retiring_auth_count\"", lines.get(1));
    assertEquals("\"user0\",\"sms_passcode\",\"0\"", lines.get(2));
    assertNull("large file should be in the clob, not varchar", grouperFile1.getFileContentsVarcharDb());
    assertEquals(contents1, grouperFile1.getFileContentsClobDb());

    // when: second run for the same day
    GshTemplateExecOutput output2 = runTemplate(day1);

    // then: cached, same row, contents unchanged (nanoTime line would differ on a recompute)
    assertEquals(1, output2.getGshTemplateOutput().getOutputLines().size());
    assertEquals("cached", output2.getGshTemplateOutput().getOutputLines().get(0).getText());
    assertEquals(fileId1, output2.getGshTemplateOutput().getDownloadGrouperFileId());
    assertEquals(1, countFiles(GshTemplateDownloadFile.SYSTEM_NAME));
    assertEquals(contents1, Hib3DAOFactory.getFactory().getGrouperFile().findById(fileId1, true).retrieveValue());

    // when: run for another day
    GshTemplateExecOutput output3 = runTemplate(day2);

    // then: computed again, new row
    assertEquals("computed", output3.getGshTemplateOutput().getOutputLines().get(0).getText());
    String fileId2 = output3.getGshTemplateOutput().getDownloadGrouperFileId();
    assertFalse(fileId1.equals(fileId2));
    assertEquals(2, countFiles(GshTemplateDownloadFile.SYSTEM_NAME));
    assertNotNull(GshTemplateDownloadFile.findByDate(CONFIG_ID, day2, "mfaTextCallbackReport_" + day2 + ".csv"));
  }

  /**
   * Saving the same date and file name again replaces the contents of the existing row
   * (file_path is unique) instead of creating a second row.
   */
  public void testSaveSameDayReplacesContents() {

    // given
    GrouperSession.startRootSession();
    String day = "2026-10-04";
    String fileName = "report.csv";

    // when
    GrouperFile first = GshTemplateDownloadFile.save(CONFIG_ID, day, fileName, "a,b\n1,2\n");
    GrouperFile second = GshTemplateDownloadFile.save(CONFIG_ID, day, fileName, "a,b\n3,4\n");

    // then
    assertEquals(first.getId(), second.getId());
    assertEquals(1, countFiles(GshTemplateDownloadFile.SYSTEM_NAME));
    assertEquals("a,b\n3,4\n", GshTemplateDownloadFile.findByDate(CONFIG_ID, day, fileName).retrieveValue());
    // id and file name lookups that do not load the contents
    assertEquals(first.getId(), GshTemplateDownloadFile.findIdByDate(CONFIG_ID, day, fileName));
    assertNull(GshTemplateDownloadFile.findIdByDate(CONFIG_ID, day, "missing.csv"));
    assertNull(GshTemplateDownloadFile.findIdByDate(CONFIG_ID, "2026-10-05", fileName));
    assertEquals(fileName, Hib3DAOFactory.getFactory().getGrouperFile().findFileNameById(first.getId()));
    assertNull(Hib3DAOFactory.getFactory().getGrouperFile().findFileNameById("notAnId"));
    // small file goes in varchar
    assertEquals("a,b\n3,4\n", Hib3DAOFactory.getFactory().getGrouperFile().findById(first.getId(), true).getFileContentsVarcharDb());
  }

  /**
   * GRP-7439: the grouper_file DAO sets created_on_micros and updated_on_micros on insert, and on a
   * second save of the same row (same date and file name) updated_on_micros advances while
   * created_on_micros stays the same.  Checked in the database, not just on the java object.
   */
  public void testSaveSetsCreatedAndUpdatedMicros() {

    // given
    GrouperSession.startRootSession();
    String day = "2026-10-04";
    String fileName = "report.csv";
    long beforeMicros = System.currentTimeMillis() * 1000L;

    // when: insert
    GrouperFile first = GshTemplateDownloadFile.save(CONFIG_ID, day, fileName, "a,b\n1,2\n");

    // then: both set, equal, and the db has them
    Long createdOnMicros = first.getCreatedOnMicros();
    Long updatedOnMicros = first.getUpdatedOnMicros();
    assertNotNull(createdOnMicros);
    assertEquals(createdOnMicros, updatedOnMicros);
    assertTrue(createdOnMicros >= beforeMicros);
    assertEquals(createdOnMicros, grp7439MicrosFromDb(first.getId(), "created_on_micros"));
    assertEquals(updatedOnMicros, grp7439MicrosFromDb(first.getId(), "updated_on_micros"));

    // when: same row saved again with new contents
    GrouperFile second = GshTemplateDownloadFile.save(CONFIG_ID, day, fileName, "a,b\n3,4\n");

    // then: same row, created unchanged, updated advanced
    assertEquals(first.getId(), second.getId());
    assertEquals(createdOnMicros, second.getCreatedOnMicros());
    assertTrue(second.getUpdatedOnMicros() > updatedOnMicros);
    assertEquals(createdOnMicros, grp7439MicrosFromDb(first.getId(), "created_on_micros"));
    assertEquals(second.getUpdatedOnMicros(), grp7439MicrosFromDb(first.getId(), "updated_on_micros"));

    // a reload from hibernate sees the same values
    GrouperFile reloaded = Hib3DAOFactory.getFactory().getGrouperFile().findById(first.getId(), true);
    assertEquals(createdOnMicros, reloaded.getCreatedOnMicros());
    assertEquals(second.getUpdatedOnMicros(), reloaded.getUpdatedOnMicros());
  }

  /**
   * read a timestamp column straight from grouper_file (bypasses the hibernate cache)
   * @param id grouper_file id
   * @param column created_on_micros or updated_on_micros
   * @return the value
   */
  private static Long grp7439MicrosFromDb(String id, String column) {
    return new GcDbAccess().sql("select " + column + " from grouper_file where id = ?")
        .addBindVar(id).select(Long.class);
  }

  /**
   * The date must be yyyy-MM-dd since the cleanup parses it from the path, and the file name
   * must fit the column and not contain a slash.
   */
  public void testSaveValidatesDateAndFileName() {

    GrouperSession.startRootSession();

    for (String badDate : new String[] {null, "", "2026-10-4", "20261004", "2026-13-01", "../2026-10-04"}) {
      try {
        GshTemplateDownloadFile.save(CONFIG_ID, badDate, "report.csv", "x");
        fail("should not accept date: " + badDate);
      } catch (RuntimeException re) {
        // expected
      }
    }

    for (String badFileName : new String[] {null, "", "a/b.csv", StringUtils.repeat("x", 101)}) {
      try {
        GshTemplateDownloadFile.save(CONFIG_ID, "2026-10-04", badFileName, "x");
        fail("should not accept file name: " + badFileName);
      } catch (RuntimeException re) {
        // expected
      }
    }

    assertEquals(0, countFiles(GshTemplateDownloadFile.SYSTEM_NAME));
  }

  /**
   * A template can point at an existing grouper_file row from some other feature.  The row is
   * marked for download as is: not copied, and no gshTemplateDownload row is created.
   */
  public void testDownloadExistingGrouperFile() {

    // given
    GrouperSession.startRootSession();
    GrouperFile otherFile = saveOtherGrouperFile("someOtherFeature", "/someOtherFeature/2020-01-01/other.csv",
        "other.csv", "x,y\n5,6\n");

    String javaSource = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class TestDownloadExistingFileTemplate extends GshTemplateV2 {\n"
        + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {\n"
        + "    String grouperFileId = in.getGsh_builtin_inputString(\"gsh_input_myExtension\");\n"
        + "    out.getGsh_builtin_gshTemplateOutput().assignDownloadGrouperFileId(grouperFileId);\n"
        + "  }\n"
        + "}\n";
    configureCompiledTemplate(javaSource);

    // when
    GshTemplateExecOutput output = runTemplate(otherFile.getId());

    // then
    assertEquals(otherFile.getId(), output.getGshTemplateOutput().getDownloadGrouperFileId());
    assertEquals(0, countFiles(GshTemplateDownloadFile.SYSTEM_NAME));
    assertEquals(1, countFiles("someOtherFeature"));
  }

  /**
   * Pointing at a grouper_file id that does not exist fails in the template, not later in the download.
   */
  public void testDownloadMissingGrouperFileIdFails() {

    GrouperSession.startRootSession();
    GshTemplateOutput gshTemplateOutput = new GshTemplateOutput();
    try {
      gshTemplateOutput.assignDownloadGrouperFileId("notAnId");
      fail("should fail for a missing file id");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("notAnId"));
    }
    assertNull(gshTemplateOutput.getDownloadGrouperFileId());
  }

  /**
   * A template that does not ask for a download leaves the download id blank.
   */
  public void testNoDownloadByDefault() {

    // given
    GrouperSession.startRootSession();
    String javaSource = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class TestNoDownloadTemplate extends GshTemplateV2 {\n"
        + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {\n"
        + "    out.getGsh_builtin_gshTemplateOutput().addOutputLine(\"no file\");\n"
        + "  }\n"
        + "}\n";
    configureCompiledTemplate(javaSource);

    // when
    GshTemplateExecOutput output = runTemplate(null);

    // then
    assertNull(output.getGshTemplateOutput().getDownloadGrouperFileId());
  }

  /**
   * Cleanup is per template: it deletes that template's rows whose date is older than the
   * retention period, keeps recent ones, and never touches another template's rows or rows
   * from other features even if their path has an old date in it.
   */
  public void testDeleteExpired() {

    // given
    GrouperSession.startRootSession();
    String today = "2026-10-04";
    String otherConfigId = "otherTemplateConfig";
    // like wildcard in the id should not match testXGshTemplateConfig
    String underscoreConfigId = "test_GshTemplateConfig";

    GshTemplateDownloadFile.save(CONFIG_ID, "2026-09-01", "old.csv", "old");
    GshTemplateDownloadFile.save(CONFIG_ID, "2026-10-02", "twoDaysAgo.csv", "twoDaysAgo");
    GshTemplateDownloadFile.save(CONFIG_ID, "2026-10-03", "yesterday.csv", "yesterday");
    GshTemplateDownloadFile.save(CONFIG_ID, today, "today.csv", "today");
    GshTemplateDownloadFile.save(otherConfigId, "2026-09-01", "old.csv", "other template old");
    GshTemplateDownloadFile.save("testXGshTemplateConfig", "2026-09-01", "old.csv", "similar id old");
    GrouperFile otherFile = saveOtherGrouperFile("someOtherFeature", "/someOtherFeature/2020-01-01/other.csv",
        "other.csv", "other");

    // when: keep 1 day back (today and yesterday)
    int deletedCount = GshTemplateDownloadFile.deleteExpired(CONFIG_ID, today, 1);

    // then
    assertEquals(2, deletedCount);
    assertNull(GshTemplateDownloadFile.findByDate(CONFIG_ID, "2026-09-01", "old.csv"));
    assertNull(GshTemplateDownloadFile.findByDate(CONFIG_ID, "2026-10-02", "twoDaysAgo.csv"));
    assertNotNull(GshTemplateDownloadFile.findByDate(CONFIG_ID, "2026-10-03", "yesterday.csv"));
    assertNotNull(GshTemplateDownloadFile.findByDate(CONFIG_ID, today, "today.csv"));
    assertNotNull("other template untouched", GshTemplateDownloadFile.findByDate(otherConfigId, "2026-09-01", "old.csv"));
    assertNotNull("other feature untouched", Hib3DAOFactory.getFactory().getGrouperFile().findById(otherFile.getId(), false));

    // when: id with an underscore (like wildcard) only cleans its own files
    assertEquals(0, GshTemplateDownloadFile.deleteExpired(underscoreConfigId, today, 1));

    // then
    assertNotNull(GshTemplateDownloadFile.findByDate("testXGshTemplateConfig", "2026-09-01", "old.csv"));

    // when: retention 0 keeps only today
    assertEquals(1, GshTemplateDownloadFile.deleteExpired(CONFIG_ID, today, 0));

    // then
    assertNull(GshTemplateDownloadFile.findByDate(CONFIG_ID, "2026-10-03", "yesterday.csv"));
    assertNotNull(GshTemplateDownloadFile.findByDate(CONFIG_ID, today, "today.csv"));
  }

  /**
   * A template cleans up its own files through GshTemplateOutput.deleteExpiredDownloadFiles(),
   * relative to the real today.
   */
  public void testTemplateSelfCleanup() {

    // given: files for this template from today and 10 days ago, and an old one for another template
    GrouperSession.startRootSession();
    LocalDate now = LocalDate.now();
    String today = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
    String tenDaysAgo = now.minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE);

    GshTemplateDownloadFile.save(CONFIG_ID, today, "report.csv", "today");
    GshTemplateDownloadFile.save(CONFIG_ID, tenDaysAgo, "report.csv", "old");
    GshTemplateDownloadFile.save("otherTemplateConfig", tenDaysAgo, "report.csv", "other old");

    String javaSource = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class TestDownloadSelfCleanupTemplate extends GshTemplateV2 {\n"
        + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {\n"
        + "    int deleted = out.getGsh_builtin_gshTemplateOutput().deleteExpiredDownloadFiles(7);\n"
        + "    out.getGsh_builtin_gshTemplateOutput().addOutputLine(\"deleted \" + deleted);\n"
        + "  }\n"
        + "}\n";
    configureCompiledTemplate(javaSource);

    // when
    GshTemplateExecOutput output = runTemplate(null);

    // then
    assertEquals("deleted 1", output.getGshTemplateOutput().getOutputLines().get(0).getText());
    assertNotNull(GshTemplateDownloadFile.findByDate(CONFIG_ID, today, "report.csv"));
    assertNull(GshTemplateDownloadFile.findByDate(CONFIG_ID, tenDaysAgo, "report.csv"));
    assertNotNull(GshTemplateDownloadFile.findByDate("otherTemplateConfig", tenDaysAgo, "report.csv"));
  }

}
