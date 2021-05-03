/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.ddl.GrouperAntProject;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class OtherJobScript extends OtherJobBase {

  private static ThreadLocal<OtherJobScript> threadLocalOtherJobScript = new InheritableThreadLocal();
  
  private OtherJobInput otherJobInput;
  
  private OtherJobOutput otherJobOutput;
  
  public static OtherJobScript retrieveFromThreadLocal() {
    return threadLocalOtherJobScript.get();
  }
  
  public OtherJobInput getOtherJobInput() {
    return otherJobInput;
  }

  
  public void setOtherJobInput(OtherJobInput otherJobInput) {
    this.otherJobInput = otherJobInput;
  }

  
  public OtherJobOutput getOtherJobOutput() {
    return otherJobOutput;
  }

  
  public void setOtherJobOutput(OtherJobOutput otherJobOutput) {
    this.otherJobOutput = otherJobOutput;
  }

  /**
   * 
   */
  public OtherJobScript() {
  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInputParam) {
    threadLocalOtherJobScript.set(this);
    try {
      this.otherJobInput = otherJobInputParam;
      this.otherJobOutput = new OtherJobOutput();
      String jobName = otherJobInputParam.getJobName();
      
      // jobName = OTHER_JOB_csvSync
      jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());
  
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInputParam.getHib3GrouperLoaderLog();
      if (hib3GrouperLoaderLog == null) {
        hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
        otherJobInputParam.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
      }
  
      String scriptType = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".scriptType");
      
      String fileName = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".fileName");
      String scriptSource = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".scriptSource");

      String connectionName = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".connectionName");
  
      if (StringUtils.isBlank(fileName) && StringUtils.isBlank(scriptSource)) {
        throw new RuntimeException("You must provide a \"otherJob." + jobName + ".fileName\" or \"otherJob." + jobName + ".scriptSource\"!!!");
      }
      if (!StringUtils.isBlank(fileName) && !StringUtils.isBlank(scriptSource)) {
        throw new RuntimeException("You must provide only one of \"otherJob." + jobName + ".fileName\" or \"otherJob." + jobName + ".scriptSource\"!!!");
      }
      boolean lightWeight = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".lightWeight", false);
  
      File file = null;
      String output = "scriptType: " + scriptType + "\n";
      if (!StringUtils.isBlank(fileName)) {
        file = new File(fileName);
        if (!file.exists() || !file.isFile()) {
          throw new RuntimeException("File doesnt exist! '" + file.getAbsolutePath() + "'");
        }
        output += "fileName: " + fileName + "\n\n";
      } else {
        output += "scriptSource: " + GrouperUtil.abbreviate(scriptSource, 200) + "\n\n";
      }
      
  
      if (StringUtils.equalsIgnoreCase("sql", scriptType)) {
        StringBuilder theLog = new StringBuilder();
        GrouperAntProject.assignLoggingThreadLocal(theLog);
        try {
          String localOutput = null;
          if (StringUtils.isBlank(connectionName)) {
            connectionName = "grouper";
          }
          // we need a file or a script
          if (!StringUtils.isBlank(fileName)) {
            
            localOutput = GrouperDdlUtils.runScriptFileIfShouldReturnString(connectionName, file, true);
            
          } else if (!StringUtils.isBlank(scriptSource)) {
    
            localOutput = GrouperDdlUtils.runScriptIfShouldReturnString(connectionName, scriptSource, true, true);
    
          }
          if (theLog.length() > 0) {
            output += theLog.toString() + "\n";
          }
          output += localOutput;
        } finally {
          GrouperAntProject.clearLoggingThreadLocal();
        }
      } else if (StringUtils.equalsIgnoreCase("gsh", scriptType)) {
  
        if (file != null) {
          scriptSource = GrouperUtil.readFileIntoString(file);
        }
        
        output += GrouperUtil.gshRunScript(scriptSource, lightWeight);  
        
      } else {
        throw new RuntimeException("Not expecting script type: '" + scriptType + "', expecting sql or gsh");
      }
      // the script might have set a message, so append if so
      String message = otherJobInput.getHib3GrouperLoaderLog().getJobMessage();
      if (!StringUtils.isBlank(message)) {
        message += "\n";
      } else {
        message = "";
      }
      message += output;
      otherJobInputParam.getHib3GrouperLoaderLog().setJobMessage(message);
    } finally {
      threadLocalOtherJobScript.remove();
    }
    return this.otherJobOutput;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

//    GrouperGroovysh.GrouperGroovyResult grouperGroovyResult = GrouperGroovysh.runScript(
//        "GrouperSession grouperSession = GrouperSession.startRootSession(); \n new GroupSave(grouperSession).assignName(\"stem1:a\").assignCreateParentStemsIfNotExist(true).save();");
//    System.out.println(grouperGroovyResult.getOutString());
    
    GrouperStartup.startup();
    
    
    GrouperLoader.scheduleJobs();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.class", "edu.internet2.middleware.grouper.app.loader.OtherJobScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.quartzCron", "0 0 0 * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.scriptType", "gsh");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.fileName", "/Users/mchyzer/git/grouper_v2_5/grouper/temp/ldapToDatabase.gsh");

    GrouperLoader.scheduleJobs();
    
    OtherJobScript otherJobScript = new OtherJobScript();
    otherJobScript.execute("OTHER_JOB_scriptSql", null);
        
  }

}
