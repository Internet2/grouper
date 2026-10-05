package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * Developer utility (NOT a JUnit test) that PERSISTS a compiled-Java gsh template into the
 * grouper_config DB table so it can be run from the UI to try GRP-7438 (template file download).
 *
 * <p>The template exports some grouper_members columns as a csv for today.  The first run of the
 * day computes it and saves it to grouper_file; later runs that day reuse the saved file.  Either
 * way the browser downloads it.  It also deletes its own files older than 7 days.</p>
 *
 * <p>Usage (run as a Java application in Eclipse):</p>
 * <ul>
 *   <li>no args: seed the template into the DB</li>
 *   <li>arg "delete": remove the template config rows</li>
 * </ul>
 *
 * <p>Then in the UI: go to any folder, More actions, Templates, pick
 * "Demo download members csv".  Runs as GrouperSystem, only wheel can run it.</p>
 *
 * GRP-7438
 */
public class GshTemplateDownloadDbSeeder {

  /** config id of the template */
  private static final String CONFIG_ID = "demoDownloadMembersCsv";

  /** the compiled-Java template body */
  private static final String TEMPLATE_JAVA_SOURCE = ""
      + "package edu.internet2.middleware.grouper.gshTest;\n"
      + "\n"
      + "import java.time.LocalDate;\n"
      + "import java.util.List;\n"
      + "\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
      + "import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;\n"
      + "\n"
      + "/**\n"
      + " * GRP-7438 demo: download a csv of grouper_members, computed once per day.\n"
      + " */\n"
      + "public class DemoDownloadMembersCsv extends GshTemplateV2 {\n"
      + "\n"
      + "  @Override\n"
      + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {\n"
      + "    GshTemplateOutput gshTemplateOutput = out.getGsh_builtin_gshTemplateOutput();\n"
      + "    String today = LocalDate.now().toString();\n"
      + "    String fileName = \"grouperMembers_\" + today + \".csv\";\n"
      + "\n"
      + "    // already computed today?  then just download it\n"
      + "    String existingFileId = gshTemplateOutput.retrieveDownloadFileId(today, fileName);\n"
      + "    if (existingFileId != null) {\n"
      + "      gshTemplateOutput.assignDownloadGrouperFileId(existingFileId);\n"
      + "      gshTemplateOutput.addOutputLine(\"Using the file already computed today: \" + fileName);\n"
      + "      return;\n"
      + "    }\n"
      + "\n"
      + "    // compute it\n"
      + "    List<Object[]> rows = new GcDbAccess().sql(\"select subject_source, subject_id, subject_identifier0, name, description \"\n"
      + "        + \"from grouper_members order by subject_source, subject_id\").selectList(Object[].class);\n"
      + "    StringBuilder csv = new StringBuilder(\"\\\"subject_source\\\",\\\"subject_id\\\",\\\"subject_identifier0\\\",\\\"name\\\",\\\"description\\\"\\n\");\n"
      + "    for (Object[] row : rows) {\n"
      + "      for (int i = 0; i < row.length; i++) {\n"
      + "        if (i > 0) {\n"
      + "          csv.append(',');\n"
      + "        }\n"
      + "        // quote every value, double up quotes inside\n"
      + "        String value = row[i] == null ? \"\" : row[i].toString();\n"
      + "        csv.append('\"').append(value.replace(\"\\\"\", \"\\\"\\\"\")).append('\"');\n"
      + "      }\n"
      + "      csv.append('\\n');\n"
      + "    }\n"
      + "    gshTemplateOutput.assignDownloadFile(today, fileName, csv.toString());\n"
      + "    gshTemplateOutput.addOutputLine(\"Computed \" + rows.size() + \" members: \" + fileName);\n"
      + "\n"
      + "    // clean up this template's old files\n"
      + "    int deleted = gshTemplateOutput.deleteExpiredDownloadFiles(7);\n"
      + "    if (deleted > 0) {\n"
      + "      gshTemplateOutput.addOutputLine(\"Deleted \" + deleted + \" old files\");\n"
      + "    }\n"
      + "  }\n"
      + "}\n";

  /** template config keys and values, in order */
  private static final String[][] TEMPLATE_CONFIG = new String[][] {
    {"templateType", "gsh"},
    {"templateMode", "compiled"},
    {"enabled", "true"},
    {"templateName", "Demo download members csv"},
    {"templateDescription", "GRP-7438 demo: downloads a csv of grouper_members, computed once per day"},
    {"showOnFolders", "true"},
    {"folderShowType", "allFolders"},
    {"showOnGroups", "false"},
    {"runAsType", "GrouperSystem"},
    {"securityRunType", "wheel"},
    {"numberOfInputs", "0"},
    {"gshTemplate", TEMPLATE_JAVA_SOURCE},
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
        System.out.println("UI: any folder -> More actions -> Templates -> 'Demo download members csv'");
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
