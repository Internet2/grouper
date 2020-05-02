package edu.internet2.middleware.grouper.ddl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * this class represents a script and the various pieces of it
 * @author mchyzer
 *
 */
public class GrouperDdlScript {

  private List<String> createTableLines = new ArrayList<String>();
  
  private List<String> createIndexLines = new ArrayList<String>();
  
  private List<String> createForeignKeyLines = new ArrayList<String>();

  private List<String> createViewLines = new ArrayList<String>();

  private static enum ScriptType {
    table {

      @Override
      public void appendLine(GrouperDdlScript grouperDdlScript, String line) {
        grouperDdlScript.createTableLines.add(line);
      }
    }, 
    
    index {

      @Override
      public void appendLine(GrouperDdlScript grouperDdlScript, String line) {
        grouperDdlScript.createIndexLines.add(line);
      }
    }, 
    
    foreignKey {

      @Override
      public void appendLine(GrouperDdlScript grouperDdlScript, String line) {
        grouperDdlScript.createForeignKeyLines.add(line);
      }
    }, 
    
    view {

      @Override
      public void appendLine(GrouperDdlScript grouperDdlScript, String line) {
        grouperDdlScript.createViewLines.add(line);
      }
    };
    
    public static ScriptType findType(String scriptLine) {
      scriptLine = scriptLine.toLowerCase();
      
      // make sure multiple spaces are collapsed
      scriptLine = scriptLine.replaceAll("[\\s]{2,}", " ").trim();
      
      if (scriptLine.startsWith("create table")) {
        return ScriptType.table;
      }
      if (scriptLine.startsWith("create view") || scriptLine.startsWith("create or replace view")) {
        return ScriptType.view;
      }
      if (scriptLine.startsWith("alter table") && scriptLine.contains("foreign key")) {
        return ScriptType.foreignKey;
      }
      if (scriptLine.startsWith("create index") || scriptLine.contains("create unique index")) {
        return ScriptType.index;
      }
      // all comments can go with views at end
      if (scriptLine.startsWith("comment on")) {
        return ScriptType.view;
      }
      throw new RuntimeException("Cant parse line: '" + scriptLine +"'");
    }
    
    public abstract void appendLine(GrouperDdlScript grouperDdlScript, String line);
  }
  
  private String databaseConnection;
  
  public GrouperDdlScript assignDatabaseConnection(String theDatabaseConnection) {
    this.databaseConnection = theDatabaseConnection;
    return this;
  }

  /**
   * 
   * @param databaseConnection
   * @param scriptSubstringName e.g. Grouper_install
   */
  public GrouperDdlScript parseScript(String scriptSubstringName) {
    
    if (StringUtils.isBlank(this.databaseConnection)) {
      throw new RuntimeException("You need to set a database connection");
    }

    String scriptOverrideDatabaseName = GrouperDdlUtils.findScriptOverrideDatabase(databaseConnection);

    String resource = "ddl/GrouperDdl_" + scriptSubstringName + "_" + scriptOverrideDatabaseName + ".sql";
    String script = GrouperUtil.readResourceIntoString(resource, false);
    
    List<String> fileLines = GrouperUtil.splitFileLines(script);
    
    StringBuilder currentLine = null;

    for (String fileLine : fileLines) {
      
      if (StringUtils.isBlank(fileLine)) {
        continue;
      }
      
      if (currentLine == null) {
        currentLine = new StringBuilder();
      } else {
        currentLine.append(" ");
      }
      currentLine.append(StringUtils.trim(fileLine));
      if (fileLine.endsWith(";")) {
        String currentLineString = currentLine.toString();
        ScriptType scriptType = ScriptType.findType(currentLineString);
        scriptType.appendLine(this, currentLineString);
        currentLine = null;
      }
    }
    return this;
  }
  public GrouperDdlScript runTableScript() {
    this.runScriptHelper(this.createTableLines);
    return this;
  }
  public GrouperDdlScript runIndexScript() {
    this.runScriptHelper(this.createIndexLines);
    return this;
  }
  public GrouperDdlScript runForeignKeyScript() {
    this.runScriptHelper(this.createForeignKeyLines);
    return this;
  }
  public GrouperDdlScript runViewScript() {
    this.runScriptHelper(this.createViewLines);
    return this;
  }

  private GrouperDdlScript runScriptHelper(List<String> lines) {
    if (StringUtils.isBlank(this.databaseConnection)) {
      throw new RuntimeException("You need to set a database connection");
    }
    // probably ok
    if (lines.size() == 0) {
      return this;
    }
    
    StringBuilder script = new StringBuilder();
    
    for (String line : lines) {
      script.append(line).append("\n");
    }
    
    GrouperDdlUtils.runScriptIfShouldReturnString(this.databaseConnection, script.toString(), true, false);
    return this;
  }
}
