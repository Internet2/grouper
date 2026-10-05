package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * Developer utility (NOT a JUnit test) that PERSISTS a compiled-Java gsh template into the
 * grouper_config DB table so it can be run from the UI to try GRP-7446 (binary template download).
 *
 * <p>The template exports some grouper_members columns as a csv, zips it, and the browser downloads
 * grouperMembers_yyyy-MM-dd.zip.  The first run of the day computes it and saves it to grouper_file
 * (file_contents_blob); later runs that day reuse the saved file.  It also deletes its own files older than
 * 7 days.  The same template source is used by GshTemplateDownloadFileTest.testZipDownloadFullCycle.</p>
 *
 * <p>Usage (run as a Java application in Eclipse):</p>
 * <ul>
 *   <li>no args: seed the template into the DB</li>
 *   <li>arg "delete": remove the template config rows</li>
 * </ul>
 *
 * <p>Then in the UI: go to any folder, More actions, Templates, pick
 * "Demo download members csv zip".  Runs as GrouperSystem, only wheel can run it.</p>
 *
 * GRP-7446
 */
public class GshTemplateDownloadZipDbSeeder {

  /** config id of the template */
  private static final String CONFIG_ID = "demoDownloadMembersCsvZip";

  /** placeholder in the template source for the java expression that gives the report date */
  private static final String DATE_EXPRESSION_PLACEHOLDER = "REPORT_DATE_EXPRESSION";

  /**
   * the compiled-Java template body.  The csv quote and newline are built from char codes so the source
   * has no escapes that would need double escaping here
   */
  private static final String TEMPLATE_JAVA_SOURCE_WITH_PLACEHOLDER = """
      package edu.internet2.middleware.grouper.gshTest;

      import java.io.ByteArrayOutputStream;
      import java.io.IOException;
      import java.nio.charset.StandardCharsets;
      import java.util.List;
      import java.util.zip.ZipEntry;
      import java.util.zip.ZipOutputStream;

      import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
      import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
      import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
      import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
      import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

      /**
       * GRP-7446 demo: download a zipped csv of grouper_members, computed once per day.
       */
      public class DemoDownloadMembersCsvZip extends GshTemplateV2 {

        @Override
        public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {
          GshTemplateOutput gshTemplateOutput = out.getGsh_builtin_gshTemplateOutput();
          String reportDate = REPORT_DATE_EXPRESSION;
          String csvFileName = "grouperMembers_" + reportDate + ".csv";
          String zipFileName = "grouperMembers_" + reportDate + ".zip";

          // already computed for this day?  then just download it
          String existingFileId = gshTemplateOutput.retrieveDownloadFileId(reportDate, zipFileName);
          if (existingFileId != null) {
            gshTemplateOutput.assignDownloadGrouperFileId(existingFileId);
            gshTemplateOutput.addOutputLine("Using the file already computed for " + reportDate + ": " + zipFileName);
            return;
          }

          // compute the csv
          List<Object[]> rows = new GcDbAccess().sql("select subject_source, subject_id, subject_identifier0, name, description "
              + "from grouper_members order by subject_source, subject_id").selectList(Object[].class);
          String quote = String.valueOf((char) 34);
          String newline = String.valueOf((char) 10);
          StringBuilder csv = new StringBuilder();
          String[] headers = new String[] {"subject_source", "subject_id", "subject_identifier0", "name", "description"};
          for (int i = 0; i < headers.length; i++) {
            csv.append(i > 0 ? "," : "").append(quote).append(headers[i]).append(quote);
          }
          csv.append(newline);
          for (Object[] row : rows) {
            for (int i = 0; i < row.length; i++) {
              // quote every value, double up quotes inside
              String value = row[i] == null ? "" : row[i].toString();
              csv.append(i > 0 ? "," : "").append(quote).append(value.replace(quote, quote + quote)).append(quote);
            }
            csv.append(newline);
          }

          // zip it, one entry
          ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
          try (ZipOutputStream zipOutputStream = new ZipOutputStream(zipBytes)) {
            zipOutputStream.putNextEntry(new ZipEntry(csvFileName));
            zipOutputStream.write(csv.toString().getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
          } catch (IOException ioe) {
            throw new RuntimeException("Error zipping " + csvFileName, ioe);
          }

          // binary download (file_contents_blob)
          gshTemplateOutput.assignDownloadFile(reportDate, zipFileName, zipBytes.toByteArray());
          gshTemplateOutput.addOutputLine("Computed " + rows.size() + " members: " + zipFileName + " (" + zipBytes.size() + " bytes)");

          // clean up this template's old files
          int deleted = gshTemplateOutput.deleteDownloadFilesOlderThanMinutes(7 * 24 * 60);
          if (deleted > 0) {
            gshTemplateOutput.addOutputLine("Deleted " + deleted + " old files");
          }
        }
      }
      """;

  /**
   * the template source with the report date coming from the given java expression
   * @param reportDateJavaExpression e.g. java.time.LocalDate.now().toString()
   * @return the java source
   */
  public static String templateJavaSource(String reportDateJavaExpression) {
    return TEMPLATE_JAVA_SOURCE_WITH_PLACEHOLDER.replace(DATE_EXPRESSION_PLACEHOLDER, reportDateJavaExpression);
  }

  /** template config keys and values, in order */
  private static final String[][] TEMPLATE_CONFIG = new String[][] {
    {"templateType", "gsh"},
    {"templateMode", "compiled"},
    {"enabled", "true"},
    {"templateName", "Demo download members csv zip"},
    {"templateDescription", "GRP-7446 demo: downloads a zipped csv of grouper_members, computed once per day"},
    {"showOnFolders", "true"},
    {"folderShowType", "allFolders"},
    {"showOnGroups", "false"},
    {"runAsType", "GrouperSystem"},
    {"securityRunType", "wheel"},
    {"numberOfInputs", "0"},
    {"gshTemplate", templateJavaSource("java.time.LocalDate.now().toString()")},
  };

  /**
   * @param args pass "delete" to remove the seeded config; otherwise it seeds
   */
  public static void main(String[] args) {
    try {
      // bring the runtime up and satisfy GrouperDbConfig's wheel/root check
      GrouperSession.startRootSession();

      boolean delete = args != null && args.length > 0 && "delete".equalsIgnoreCase(args[0]);

      String templatePrefix = "grouperGshTemplate." + CONFIG_ID + ".";

      for (String[] keyValue : TEMPLATE_CONFIG) {
        GrouperDbConfig grouperDbConfig = new GrouperDbConfig().configFileName(ConfigFileName.GROUPER_PROPERTIES.getConfigFileName())
            .propertyName(templatePrefix + keyValue[0]);
        if (delete) {
          grouperDbConfig.delete();
        } else {
          grouperDbConfig.value(keyValue[1]).store();
        }
      }

      if (delete) {
        System.out.println("Deleted gsh template config '" + CONFIG_ID + "' from the DB.");
      } else {
        System.out.println("Seeded gsh template config '" + CONFIG_ID + "' into the DB.");
        System.out.println("UI: any folder -> More actions -> Templates -> 'Demo download members csv zip'");
      }

      // make the running config see the change immediately
      ConfigPropertiesCascadeBase.clearCache();
    } catch (RuntimeException re) {
      re.printStackTrace();
      // grouper threads keep the jvm alive, so exit explicitly
      System.exit(1);
    }
    // grouper threads keep the jvm alive, so exit explicitly
    System.exit(0);
  }

}
