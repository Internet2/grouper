package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.ddl.GrouperDdlCompareResult;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigFile;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigProperty;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiPITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 * @author mchyzer
 *
 */
public class ConfigurationContainer {

  /**
   * if we have french then show it on the screen
   * @return if has french
   */
  public boolean isHasFrench() {
    return GrouperConfig.retrieveConfig().textBundleFromLanguageAndCountry().containsKey("fr_fr");
  }
  
  /**
   * import count added properties
   */
  private int countAdded = 0;
  
  
  
  
  /**
   * import count added properties
   * @return count
   */
  public int getCountAdded() {
    return this.countAdded;
  }

  /**
   * import count added properties
   * @param countAdded1
   */
  public void setCountAdded(int countAdded1) {
    this.countAdded = countAdded1;
  }

  /**
   * import count updated
   */
  private int countUpdated = 0;
  
  
  
  /**
   * import count updated
   * @return import count updated
   */
  public int getCountUpdated() {
    return this.countUpdated;
  }

  /**
   * import count updated
   * @param countUpdated1
   */
  public void setCountUpdated(int countUpdated1) {
    this.countUpdated = countUpdated1;
  }

  /**
   * import count properties
   */
  private int countProperties = 0;

  /**
   * import count properties
   * @return count
   */
  public int getCountProperties() {
    return  this.countProperties;
  }

  /**
   * import count properties
   * @param countProperties1
   */
  public void setCountProperties(int countProperties1) {
    this.countProperties = countProperties1;
  }

  /**
   * import count successful inserts/updates
   */
  private int countSuccess = 0;

  /**
   * import count successful inserts/updates
   * @return import count successful inserts/updates
   */
  public int getCountSuccess() {
    return this.countSuccess;
  }

  /**
   * import count successful inserts/updates
   * @param countSuccess1
   */
  public void setCountSuccess(int countSuccess1) {
    this.countSuccess = countSuccess1;
  }

  /**
   * import count unchanged
   */
  private int countUnchanged = 0;

  /**
   * import count unchanged
   * @return count unchanged
   */
  public int getCountUnchanged() {
    return  this.countUnchanged;
  }

  /**
   * import count unchanged
   * @param countUnchanged1
   */
  public void setCountUnchanged(int countUnchanged1) {
    this.countUnchanged = countUnchanged1;
  }

  /**
   * import count fatal error
   */
  private int countError = 0;

  /**
   * import count fatal error
   * @return count fatal
   */
  public int getCountError() {
    return  this.countError;
  }

  /**
   * import count fatal error
   * @param countFatalError1
   */
  public void setCountError(int countFatalError1) {
    this.countError = countFatalError1;
  }

  /**
   * import count warnings
   */
  private int countWarning = 0;

  /**
   * import count properties
   * @return
   */
  public int getCountWarning() {
    return  this.countWarning;
  }

  /**
   * import count warnings
   * @param countWarning1
   */
  public void setCountWarning(int countWarning1) {
    this.countWarning = countWarning1;
  }

  /**
   * 
   * @return config file names for import
   */
  public String getConfigFileNamesForImport() {
    StringBuilder results = new StringBuilder();
    
    for (ConfigFileName configFileName : ConfigFileName.values()) {
      if (results.length() != 0) {
        results.append(", ");
      }
      results.append(configFileName.getConfigFileName());
    }
    
    return results.toString();
  }
  
  /**
   * current config property we are editing
   */
  private GuiConfigProperty currentGuiConfigProperty;
  
  /**
   * current config property we are editing
   */
  public GuiConfigProperty getCurrentGuiConfigProperty() {
    return currentGuiConfigProperty;
  }
  
  /**
   * current config property we are editing
   */
  public void setCurrentGuiConfigProperty(GuiConfigProperty currentConfigProperty1) {
    this.currentGuiConfigProperty = currentConfigProperty1;
  }

  /**
   * config file name
   */
  private String currentConfigFileName;

  /**
   * 
   * @return current config file name
   */
  public String getCurrentConfigFileName() {
    return this.currentConfigFileName;
  }

  /**
   * current config file name
   * @param currentConfigFileName1
   */
  public void setCurrentConfigFileName(String currentConfigFileName1) {
    this.currentConfigFileName = currentConfigFileName1;
  }

  /**
   * current config property name
   * @return 
   */
  public String getCurrentConfigPropertyName() {
    return this.currentConfigPropertyName;
  }

  /**
   * 
   * @param currentConfigPropertyName1
   */
  public void setCurrentConfigPropertyName(String currentConfigPropertyName1) {
    this.currentConfigPropertyName = currentConfigPropertyName1;
  }

  /**
   * config property name
   */
  private String currentConfigPropertyName;
  
  /**
   * if show configure link
   * @return the configure link
   */
  public boolean isConfigureShow() {

    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.configuration.enabled", true)) {
      return false;

    }

    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }
  
  /**
   * result of the database DDL deep check (gsh -registry -deep -check), shown on the DDL deep check screen
   */
  private GrouperDdlCompareResult ddlCompareResult;

  /**
   * result of the database DDL deep check (gsh -registry -deep -check), shown on the DDL deep check screen
   * @return the ddl compare result
   */
  public GrouperDdlCompareResult getDdlCompareResult() {
    return this.ddlCompareResult;
  }

  /**
   * result of the database DDL deep check (gsh -registry -deep -check), shown on the DDL deep check screen
   * @param ddlCompareResult1
   */
  public void setDdlCompareResult(GrouperDdlCompareResult ddlCompareResult1) {
    this.ddlCompareResult = ddlCompareResult1;
  }

  /**
   * upgrade tasks (with their computed status) shown on the Configure -&gt; Upgrade tasks screen, or null
   * until the user presses the load button - the work is not done on the GET, only when the button posts
   */
  private List<GuiUpgradeTask> guiUpgradeTasks;

  /**
   * upgrade tasks (with their computed status) shown on the Configure -&gt; Upgrade tasks screen
   * @return the upgrade tasks, or null if not loaded yet
   */
  public List<GuiUpgradeTask> getGuiUpgradeTasks() {
    return this.guiUpgradeTasks;
  }

  /**
   * @param guiUpgradeTasks1 the upgrade tasks to show
   */
  public void setGuiUpgradeTasks(List<GuiUpgradeTask> guiUpgradeTasks1) {
    this.guiUpgradeTasks = guiUpgradeTasks1;
  }

  /**
   * DDL deep check result rendered as HTML: each line is HTML-escaped and the lines are color coded
   * by their leading word (Note: dark blue, Success: dark green, Warning: dark orange, Error: dark red).
   * The text is escaped in here so the JSP can output this raw inside a pre.
   * @return the color coded, HTML-escaped DDL compare result, or null if there is no result yet
   */
  public String getDdlCompareResultHtml() {
    if (this.ddlCompareResult == null || this.ddlCompareResult.getResult() == null) {
      return null;
    }

    String[] lines = this.ddlCompareResult.getResult().toString().split("\n", -1);

    StringBuilder html = new StringBuilder();

    for (int i = 0; i < lines.length; i++) {

      String line = lines[i];

      // find the first non whitespace char so leading indentation is preserved as is
      int start = 0;
      while (start < line.length() && Character.isWhitespace(line.charAt(start))) {
        start++;
      }
      String afterWhitespace = line.substring(start).toLowerCase();

      // figure out the leading word (Note:/Success:/Warning:/Error:) and its color, if any
      String color = null;
      int prefixLength = 0;
      if (afterWhitespace.startsWith("note:")) {
        color = "#00008b"; // dark blue
        prefixLength = "note:".length();
      } else if (afterWhitespace.startsWith("success:")) {
        color = "#006400"; // dark green
        prefixLength = "success:".length();
      } else if (afterWhitespace.startsWith("warning:")) {
        color = "#cc6600"; // dark orange
        prefixLength = "warning:".length();
      } else if (afterWhitespace.startsWith("error:")) {
        color = "#8b0000"; // dark red
        prefixLength = "error:".length();
      }

      if (color != null) {
        // color only the prefix word, leave leading whitespace and the rest of the line default
        html.append(htmlEscape(line.substring(0, start)));
        html.append("<span style=\"color: ").append(color).append(";\">");
        html.append(htmlEscape(line.substring(start, start + prefixLength)));
        html.append("</span>");
        html.append(htmlEscape(line.substring(start + prefixLength)));
      } else {
        html.append(htmlEscape(line));
      }

      if (i < lines.length - 1) {
        html.append("\n");
      }
    }

    return html.toString();
  }

  /**
   * escape html special chars so DB object names etc cannot inject markup
   * @param text raw text
   * @return escaped text
   */
  private static String htmlEscape(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  /**
   * cache the config so it is consistent as the page draws
   */
  private ConfigPropertiesCascadeBase config;

  /**
   * cache the config so it is consistent as the page draws
   * @return the config
   */
  public ConfigPropertiesCascadeBase getConfig() {
    if (this.config == null) {
      this.config = this.getConfigFileName().getConfig();
    }
    return this.config;
  }

  /**
   * return config names from the selected config
   * @return the config names
   */
  public Set<String> getPropertyNames() {
    return this.getConfig().propertyNames();
  }
  
  /**
   * gui config file
   */
  private GuiConfigFile guiConfigFile;

  /**
   * gui config file
   * @return config file
   */
  public GuiConfigFile getGuiConfigFile() {
    return this.guiConfigFile;
  }

  /**
   * gui config file
   * @param guiConfigFile1
   */
  public void setGuiConfigFile(GuiConfigFile guiConfigFile1) {
    this.guiConfigFile = guiConfigFile1;
  }

  /**
   * cache the config so it is consistent as the page draws
   * @param config1
   */
  public void setConfig(ConfigPropertiesCascadeBase config1) {
    this.config = config1;
  }

  /**
   * config file name selected if any
   */
  private ConfigFileName configFileName;
  
  /**
   * filter to apply
   */
  private String filter;
  
  /**
   * filter config source 
   */
  private String configSource;

  /**
   * config file name selected if any
   * @return config file name
   */
  public ConfigFileName getConfigFileName() {
    return this.configFileName;
  }

  /**
   * config file name selected if any
   * @param configFileName1
   */
  public void setConfigFileName(ConfigFileName configFileName1) {
    this.configFileName = configFileName1;
  }
  
  /**
   * config file name selected if any
   * @return config file name array
   */
  public ConfigFileName[] getAllConfigFileNames() {
    return ConfigFileName.values();
  }

  /**
   * filter to apply
   */
  public void setFilter(String filter) {
    this.filter = filter;
  }

  /**
   * filter to apply
   */
  public String getFilter() {
    return filter;
  }

  /**
   * gui pit config to show history on the ui
   */
  private List<GuiPITGrouperConfigHibernate> guiPitConfigs;


  public List<GuiPITGrouperConfigHibernate> getGuiPitConfigs() {
    return guiPitConfigs;
  }
  
  public void setGuiPitConfigs(List<GuiPITGrouperConfigHibernate> guiPitConfigs) {
    this.guiPitConfigs = guiPitConfigs;
  }

  
  public String getConfigSource() {
    return configSource;
  }

  public void setConfigSource(String configSource) {
    this.configSource = configSource;
  }
  
  /**
   * keep track of the paging on the config history screen
   */
  private GuiPaging guiPaging = null;

  
  /**
   * keep track of the paging on the config history screen
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  
  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }
  
}
