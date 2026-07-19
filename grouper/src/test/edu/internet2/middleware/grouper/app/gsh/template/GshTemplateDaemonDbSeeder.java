package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * Developer utility (NOT a JUnit test) that PERSISTS a compiled-Java daemon --
 * both the grouperGshTemplate config and the otherJob daemon config -- into the
 * grouper_config DB table, so it shows up in the running config editor / daemon
 * screens. This is the store the config editor UI reads, so after running this
 * you can see the template, see the daemon, and see the new gshTemplateConfigId
 * dropdown (GRP-7155) populated with this daemon template.
 *
 * The JUnit tests (GshTemplateDaemonTest) intentionally use the in-memory
 * propertiesOverrideMap so they leave no DB state behind for CI. This seeder is
 * the opposite: it writes real, persistent config and does NOT clean up, which
 * is exactly why it is a separate main() utility and not a test method.
 *
 * Usage (run as a Java application in Eclipse):
 *   - no args        -> seed the template + daemon into the DB
 *   - arg "delete"   -> remove the template + daemon config rows
 *
 * Runs against whatever DB your grouper.hibernate.properties points at (for the
 * local dev setup that is localhost postgres). Requires the runtime to come up,
 * so it starts a root GrouperSession first (GrouperDbConfig.store() also needs a
 * wheel/root session).
 *
 * GRP-7155
 */
public class GshTemplateDaemonDbSeeder {

  /**
   * config id of the grouperGshTemplate.* daemon template, and the otherJob.*
   * daemon job key -- same string, different config groups
   */
  private static final String CONFIG_ID = "demoCompiledDaemon";

  /**
   * the compiled-Java daemon body. Plain Java (compiled mode), extends
   * GrouperTemplateDaemon, writes a message to the loader-log row so a run is
   * visibly successful in the daemon logs.
   */
  private static final String DAEMON_JAVA_SOURCE = ""
      + "package edu.internet2.middleware.grouper.gshTest;\n"
      + "\n"
      + "import java.util.LinkedHashMap;\n"
      + "import java.util.Map;\n"
      + "\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateDaemon;\n"
      + "import edu.internet2.middleware.grouper.app.loader.OtherJobTemplateInput;\n"
      + "import edu.internet2.middleware.grouper.util.GrouperUtil;\n"
      + "\n"
      + "/**\n"
      + " * Demo compiled-Java daemon. Records a message on the loader-log row so the\n"
      + " * daemon run shows up as a successful job with a readable job message.\n"
      + " */\n"
      + "public class DemoCompiledDaemon extends GrouperTemplateDaemon {\n"
      + "\n"
      + "  @Override\n"
      + "  public void runDaemon(OtherJobTemplateInput otherJobTemplateInput) {\n"
      + "    // own our own debug map -- the compiled daemon input does not expose one\n"
      + "    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();\n"
      + "    debugMap.put(\"daemon\", \"demoCompiledDaemon\");\n"
      + "    debugMap.put(\"templateConfigId\", otherJobTemplateInput.getGshTemplateConfigId());\n"
      + "    debugMap.put(\"message\", \"compiled daemon ran successfully\");\n"
      + "    // surface the debug map on the loader-log row\n"
      + "    otherJobTemplateInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));\n"
      + "  }\n"
      + "}\n";

  /**
   * @param args pass "delete" to remove the seeded config; otherwise it seeds
   */
  public static void main(String[] args) {

    // bring the runtime up and satisfy GrouperDbConfig's wheel/root check
    GrouperSession.startRootSession();

    boolean delete = args != null && args.length > 0 && "delete".equalsIgnoreCase(args[0]);

    if (delete) {
      deleteConfig();
      System.out.println("Deleted compiled daemon config '" + CONFIG_ID + "' from the DB.");
    } else {
      seedConfig();
      System.out.println("Seeded compiled daemon config '" + CONFIG_ID + "' into the DB.");
      System.out.println("Config editor -> Miscellaneous -> GSH/Groovy templates: template id '" + CONFIG_ID + "'");
      System.out.println("Config editor -> Daemon jobs: job 'OTHER_JOB_" + CONFIG_ID + "' (run it on demand to see it work).");
    }

    // make the running config see the change immediately
    ConfigPropertiesCascadeBase.clearCache();
  }

  /**
   * Write the minimal-valid daemon template + otherJob daemon config to the DB.
   * The grouperGshTemplate.* fields are the minimal required set for a
   * templateType=daemon, templateMode=compiled template (templateName,
   * templateDescription, runAsType, securityRunType, gshTemplate are required;
   * the gsh-run-flow fields do not apply to a daemon).
   */
  private static void seedConfig() {

    String templatePrefix = "grouperGshTemplate." + CONFIG_ID + ".";

    storeMain(templatePrefix + "templateType", "daemon");
    storeMain(templatePrefix + "templateMode", "compiled");
    storeMain(templatePrefix + "enabled", "true");
    storeMain(templatePrefix + "templateName", "Demo compiled daemon");
    storeMain(templatePrefix + "templateDescription", "Demo compiled-Java daemon seeded from GshTemplateDaemonDbSeeder");
    storeMain(templatePrefix + "runAsType", "GrouperSystem");
    storeMain(templatePrefix + "securityRunType", "wheel");
    storeMain(templatePrefix + "gshTemplate", DAEMON_JAVA_SOURCE);

    String jobPrefix = "otherJob." + CONFIG_ID + ".";

    storeLoader(jobPrefix + "class", "edu.internet2.middleware.grouper.app.loader.OtherJobScript");
    storeLoader(jobPrefix + "quartzCron", "0 0 5 * * ?");
    storeLoader(jobPrefix + "scriptType", "compiledJava");
    storeLoader(jobPrefix + "gshTemplateConfigId", CONFIG_ID);
  }

  /**
   * Delete the seeded template + otherJob config rows.
   */
  private static void deleteConfig() {

    String templatePrefix = "grouperGshTemplate." + CONFIG_ID + ".";

    deleteMain(templatePrefix + "templateType");
    deleteMain(templatePrefix + "templateMode");
    deleteMain(templatePrefix + "enabled");
    deleteMain(templatePrefix + "templateName");
    deleteMain(templatePrefix + "templateDescription");
    deleteMain(templatePrefix + "runAsType");
    deleteMain(templatePrefix + "securityRunType");
    deleteMain(templatePrefix + "gshTemplate");

    String jobPrefix = "otherJob." + CONFIG_ID + ".";

    deleteLoader(jobPrefix + "class");
    deleteLoader(jobPrefix + "quartzCron");
    deleteLoader(jobPrefix + "scriptType");
    deleteLoader(jobPrefix + "gshTemplateConfigId");
  }

  /**
   * store one property into grouper.properties (DB)
   * @param key config key
   * @param value config value
   */
  private static void storeMain(String key, String value) {
    new GrouperDbConfig().configFileName(ConfigFileName.GROUPER_PROPERTIES.getConfigFileName())
        .propertyName(key).value(value).store();
  }

  /**
   * store one property into grouper-loader.properties (DB)
   * @param key config key
   * @param value config value
   */
  private static void storeLoader(String key, String value) {
    new GrouperDbConfig().configFileName(ConfigFileName.GROUPER_LOADER_PROPERTIES.getConfigFileName())
        .propertyName(key).value(value).store();
  }

  /**
   * delete one property from grouper.properties (DB); ignore if not present
   * @param key config key
   */
  private static void deleteMain(String key) {
    new GrouperDbConfig().configFileName(ConfigFileName.GROUPER_PROPERTIES.getConfigFileName())
        .propertyName(key).delete();
  }

  /**
   * delete one property from grouper-loader.properties (DB); ignore if not present
   * @param key config key
   */
  private static void deleteLoader(String key) {
    new GrouperDbConfig().configFileName(ConfigFileName.GROUPER_LOADER_PROPERTIES.getConfigFileName())
        .propertyName(key).delete();
  }

}
