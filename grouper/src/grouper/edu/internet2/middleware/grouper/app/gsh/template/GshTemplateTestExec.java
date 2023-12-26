package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.ui.util.ProgressBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
* <p>Use this class to execute a gsh template tests</p>
*/
public class GshTemplateTestExec {
  
  /**
   * have a progress bean to be able to communicate progress to the UI
   */
  private ProgressBean progressBean = new ProgressBean();
  
  /**
   * have a progress bean to be able to communicate progress to the UI
   * @return the progressBean
   */
  public ProgressBean getProgressBean() {
    return this.progressBean;
  }

  public static void main(String[] args) {
    GrouperStartup.startup();
    GrouperSession.startRootSession();
     
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GshTemplateTestExec.class);
  
  private String configId; // gsh template config to run

  /**
   * store the output
   */
  private GshTemplateExecTestOutput gshTemplateExecTestOutput = new GshTemplateExecTestOutput();
  
  public GshTemplateTestExec assignConfigId(String configId) {
    this.configId = configId;
    return this;
  }
  
  public String getConfigId() {
    return configId;
  }

  
  
  /**
   * execute the gsh template
   * @return
   */
  public GshTemplateExecTestOutput executeTests() {
    
    if (StringUtils.isBlank(configId)) {
      throw new RuntimeException("Config id is null");
    }
    
    GshTemplateConfig templateConfig = new GshTemplateConfig(configId);
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Subject callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        templateConfig.populateConfiguration();
        
        return null;
      }
    });
    
    boolean templateVersionV2 = StringUtils.equals("V2", templateConfig.getTemplateVersion());
    if (!templateVersionV2) {
      this.gshTemplateExecTestOutput.getGshTemplateOutput().addOutputLine("error", "Only templates with version V2 can be tested!");
    } else {
    
      // instantiate
      GshTemplateV2 gshTemplateV2 = new GshTemplateExec().assignConfigId(configId).executeForTemplateV2instance();
      
      Map<String, GshTemplateV2test> gshTemplateV2tests = GshTemplateV2utils.gshDiscoverTests(gshTemplateV2);
      if (gshTemplateV2tests.size() == 0) {
  
        this.gshTemplateExecTestOutput.getGshTemplateOutput().addOutputLine("error", "No tests defined in template!");
  
      } else {
        
        this.getProgressBean().setProgressTotalRecords(gshTemplateV2tests.size());
        
        int testsDone = 0;
        
        for (String testName : gshTemplateV2tests.keySet()) {
          GshTemplateV2test gshTemplateV2test = gshTemplateV2tests.get(testName);
          
          GshTemplateV2testOutput gshTemplateV2testOutput = GshTemplateV2utils.gshRunTest(gshTemplateV2, gshTemplateV2test, testName);
          GshTemplateV2utils.analyzeTestOutput(this.gshTemplateExecTestOutput, gshTemplateV2testOutput);
          testsDone++;
          this.getProgressBean().setProgressCompleteRecords(testsDone);
        }
        
        String outputLine = this.gshTemplateExecTestOutput.toStringSummary();
        this.gshTemplateExecTestOutput.getGshTemplateOutput().addOutputLine(this.gshTemplateExecTestOutput.getSuccesses() == gshTemplateV2tests.size() ? "success" : "error",
            outputLine);
        
        AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GSH_TEMPLATE_TEST,
            "gshTemplateConfigId", configId, "status",
            this.gshTemplateExecTestOutput.getSuccesses() == gshTemplateV2tests.size() ? "success": "error");
        auditEntry.setDescription("Execute gsh template with configId: " + configId + ", " + outputLine);
        auditEntry.saveOrUpdate(true);
        
      }
    }


    return this.gshTemplateExecTestOutput;
  }

  /**
   * store the output
   * @return the output
   */
  public GshTemplateExecTestOutput getGshTemplateExecTestOutput() {
    return gshTemplateExecTestOutput;
  }

  
}
