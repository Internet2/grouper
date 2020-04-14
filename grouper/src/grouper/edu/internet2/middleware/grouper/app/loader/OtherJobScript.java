/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.ddl.GrouperAntProject;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class OtherJobScript extends OtherJobBase {

  /**
   * 
   */
  public OtherJobScript() {
  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    String jobName = otherJobInput.getJobName();
    
    // jobName = OTHER_JOB_csvSync
    jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
    if (hib3GrouperLoaderLog == null) {
      hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    }

    String scriptType = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".scriptType");
    
    String fileName = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".fileName");
    String scriptSource = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".scriptSource");

    if (StringUtils.isBlank(fileName) && StringUtils.isBlank(scriptSource)) {
      throw new RuntimeException("You must provide a \"otherJob." + jobName + ".fileName\" or \"otherJob." + jobName + ".scriptSource\"!!!");
    }
    if (!StringUtils.isBlank(fileName) && !StringUtils.isBlank(scriptSource)) {
      throw new RuntimeException("You must provide only one of \"otherJob." + jobName + ".fileName\" or \"otherJob." + jobName + ".scriptSource\"!!!");
    }

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
        // we need a file or a script
        if (!StringUtils.isBlank(fileName)) {
          
          localOutput = GrouperDdlUtils.runScriptFileIfShouldReturnString(file, true);
          
        } else if (!StringUtils.isBlank(scriptSource)) {
  
          localOutput = GrouperDdlUtils.runScriptIfShouldReturnString(scriptSource, true, true);
  
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
      
      GrouperGroovysh.GrouperGroovyResult grouperGroovyResult = GrouperGroovysh.runScript(scriptSource);
      output += grouperGroovyResult.getOutString();

      // [32mGroovy Shell[m (2.5.0-beta-2, JVM: 1.8.0_161)
      output = GrouperUtil.replace(output, "[32mGroovy Shell[m ", "");
      
      // Type '[1m:help[m' or '[1m:h[m' for help.
      output = GrouperUtil.replace(output, "[1m", "");
      output = GrouperUtil.replace(output, "[m", "");

      
    } else {
      throw new RuntimeException("Not expecting script type: '" + scriptType + "', expecting sql or gsh");
    }
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage(output);

    return null;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    GrouperGroovysh.GrouperGroovyResult grouperGroovyResult = GrouperGroovysh.runScript(
        "GrouperSession grouperSession = GrouperSession.startRootSession(); \n new GroupSave(grouperSession).assignName(\"stem1:a\").assignCreateParentStemsIfNotExist(true).save();");
    System.out.println(grouperGroovyResult.getOutString());
  }

}
