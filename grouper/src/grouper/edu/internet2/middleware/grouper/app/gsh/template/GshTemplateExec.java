package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.text.StringEscapeUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh.GrouperGroovyResult;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.util.ProgressBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateExec {
  
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
     
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("createNewWorkingGroup");
    Subject subject = SubjectFinder.findRootSubject();
    exec.assignCurrentUser(subject);
     
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName("ref:incommon-collab"); // run the script from test2 folder
     
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_workingGroupExtension");
    input.assignValueString("myGroup");
    exec.addGshTemplateInput(input);
    input = new GshTemplateInput();
    input.assignName("gsh_input_workingGroupDisplayExtension");
    input.assignValueString("My group");
    exec.addGshTemplateInput(input);
    input = new GshTemplateInput();
    input.assignName("gsh_input_workingGroupDescription");
    input.assignValueString("My working group will do a lot of group work");
    exec.addGshTemplateInput(input);
    input = new GshTemplateInput();
    input.assignName("gsh_input_isSympa");
    input.assignValueString("true");
    exec.addGshTemplateInput(input);
    input = new GshTemplateInput();
    input.assignName("gsh_input_sympaDomain");
    input.assignValueString("internet2");
    exec.addGshTemplateInput(input);
    input = new GshTemplateInput();
    input.assignName("gsh_input_isSympaModerated");
    input.assignValueString("true");
    exec.addGshTemplateInput(input);
     
    // when
    GshTemplateExecOutput output = exec.execute();
     
    // then
    System.out.println("Success: " + output.isSuccess());
    if (!output.isSuccess() && output.getException() != null) {
      System.out.println(output.getExceptionStack());
    }
    System.out.println("Valid: " + output.isValid());
    System.out.println("Validation:");
    for (GshValidationLine gshValidationLine : output.getGshTemplateOutput().getValidationLines()) {
      System.out.println(gshValidationLine.getInputName() + ": " + gshValidationLine.getText());
    }
    System.out.println("Output from script:");
    for (GshOutputLine gshOutputLine : output.getGshTemplateOutput().getOutputLines()) {
      System.out.println(gshOutputLine.getMessageType() + ": " + gshOutputLine.getText());
    }
    System.out.println("Script output:");
    System.out.println(output.getGshScriptOutput());
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GshTemplateExec.class);
  
  private GshTemplateOwnerType gshTemplateOwnerType;
  
  private String ownerStemName;
  
  private String ownerGroupName;
  
  private String configId; // gsh template config to run
  
  private Subject currentUser; // user using the UI or the webservice
  
  private Subject actAsSubject; // only for WS
  
  private Subject originalCurrentUser; // user is the current user or if there's an actAs, it's the user calling the web service
  
  private List<GshTemplateInput> gshTemplateInputs = new ArrayList<GshTemplateInput>();
  
  public GshTemplateExec assignActAsSubject(Subject actAsSubject) {
    this.actAsSubject = actAsSubject;
    return this;
  }
  
  public GshTemplateExec assignGshTemplateOwnerType(GshTemplateOwnerType gshTemplateOwnerType) {
    this.gshTemplateOwnerType = gshTemplateOwnerType;
    return this;
  }
  
  public GshTemplateExec assignOwnerStemName(String ownerStemName) {
    this.ownerStemName = ownerStemName;
    return this;
  }
  
  public GshTemplateExec assignOwnerGroupName(String ownerGroupName) {
    this.ownerGroupName = ownerGroupName;
    return this;
  }
  
  public GshTemplateExec addGshTemplateInput(GshTemplateInput input) {
    gshTemplateInputs.add(input);
    return this;
  }
  
  public GshTemplateExec assignConfigId(String configId) {
    this.configId = configId;
    return this;
  }
  
  public GshTemplateExec assignCurrentUser(Subject currentUser) {
    this.currentUser = currentUser;
    return this;
  }

  public Subject getCurrentUser() {
    return currentUser;
  }

  public GshTemplateOwnerType getGshTemplateOwnerType() {
    return gshTemplateOwnerType;
  }

  
  public String getOwnerStemName() {
    return ownerStemName;
  }
  
  public String getOwnerGroupName() {
    return ownerGroupName;
  }

  
  public List<GshTemplateInput> getGshTemplateInputs() {
    return gshTemplateInputs;
  }
  
  
  public String getConfigId() {
    return configId;
  }

  
  
  public Subject getActAsSubject() {
    return actAsSubject;
  }

  /**
   * store the output
   */
  private GshTemplateExecOutput gshTemplateExecOutput = null;
  
  
  
  /**
   * store the output
   * @return the output
   */
  public GshTemplateExecOutput getGshTemplateExecOutput() {
    return gshTemplateExecOutput;
  }

  /**
   * array holds current line number
   */
  private int[] lineNumber = new int[] {0};
  
  /**
   * lines of script
   */
  private List<String> linesOfScript = new ArrayList<String>();
  
  /**
   * current line number
   * @return
   */
  public int getLineNumber() {
    return this.lineNumber[0];
  }
  
  /**
   * total number of lines in script
   * @return
   */
  public int getLinesOfScript() {
    return this.linesOfScript.size();
  }
  
  /**
   * execute the gsh template
   * @return
   */
  public GshTemplateExecOutput execute() {
    
    this.gshTemplateExecOutput = new GshTemplateExecOutput();
    
    final GshTemplateOutput gshTemplateOutput = new GshTemplateOutput();
    
    this.gshTemplateExecOutput.setGshTemplateOutput(gshTemplateOutput);
    
    final GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();
    
    if (StringUtils.isBlank(configId)) {
      
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.configIdBlank.message");
      this.gshTemplateExecOutput.getGshTemplateOutput().addValidationLine(errorMessage);
      return this.gshTemplateExecOutput;
    }
    
    if (currentUser == null) {
      throw new RuntimeException("currentUser cannot be null");
    }
    
    GshTemplateConfig templateConfig = new GshTemplateConfig(configId);
    
    final GshTemplateExec THIS = this;
    
    Subject grouperSessionSubject = (Subject) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Subject callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        originalCurrentUser = currentUser;
        
        templateConfig.populateConfiguration();
        
        if (actAsSubject != null) {
          String actAsGroupUUID = templateConfig.getActAsGroupUUID();
          if (actAsGroupUUID == null) {
            throw new RuntimeException("actAsGroupUUID has not been configured in the template");
          }
          Group actAsGroup = GroupFinder.findByUuid(grouperSession, actAsGroupUUID, true);
          
          if (!actAsGroup.hasMember(currentUser)) {
            throw new RuntimeException(currentUser.getId()+ " is not a member of "+actAsGroup.getName());
          }
          
          currentUser = actAsSubject;
          
        }
        
        gshTemplateRuntime.setCurrentSubject(currentUser);
        if (!new GshTemplateValidationService().validate(templateConfig, THIS, gshTemplateExecOutput)) {
          return null;
        }
        
        Subject grouperSessionSubject = null;
        
        if (templateConfig.getGshTemplateRunAsType() == GshTemplateRunAsType.GrouperSystem) {
          grouperSessionSubject = SubjectFinder.findRootSubject();
        } else if (templateConfig.getGshTemplateRunAsType() == GshTemplateRunAsType.currentUser) {
          grouperSessionSubject = currentUser;
        } else if (templateConfig.getGshTemplateRunAsType() == GshTemplateRunAsType.specifiedSubject) {
          
          String subjectId = templateConfig.getRunAsSpecifiedSubjectId();
          String sourceId = templateConfig.getRunAsSpecifiedSubjectSourceId();
          
          grouperSessionSubject = SubjectFinder.findByIdAndSource(subjectId, sourceId, true);
          
        } else {
          throw new RuntimeException("Invalid gsh template run as type "+templateConfig.getGshTemplateRunAsType());
        }
        
        return grouperSessionSubject;
      }
    });
    
  
    if (this.gshTemplateExecOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
      this.gshTemplateExecOutput.setValid(false);
      return this.gshTemplateExecOutput;
    } else {
      this.gshTemplateExecOutput.setValid(true);
    }
    
    StringBuilder scriptToRun = new StringBuilder();
    
    scriptToRun.append("import edu.internet2.middleware.grouper.app.gsh.template.*;");
    scriptToRun.append("\n");

    scriptToRun.append("import edu.internet2.middleware.grouper.util.*;");
    scriptToRun.append("\n");
    
    scriptToRun.append("GshTemplateOutput gsh_builtin_gshTemplateOutput = GshTemplateOutput.retrieveGshTemplateOutput(); ");
    scriptToRun.append("\n");
    
    scriptToRun.append("GshTemplateRuntime gsh_builtin_gshTemplateRuntime = GshTemplateRuntime.retrieveGshTemplateRuntime(); ");
    scriptToRun.append("\n");
    
    scriptToRun.append("GrouperSession gsh_builtin_grouperSession = gsh_builtin_gshTemplateRuntime.getGrouperSession();");
    scriptToRun.append("\n");
    
    scriptToRun.append("Subject gsh_builtin_subject = gsh_builtin_gshTemplateRuntime.getCurrentSubject();");
    scriptToRun.append("\n");
    
    scriptToRun.append("String gsh_builtin_subjectId = \""+ StringEscapeUtils.escapeJava(currentUser.getId()) + "\";");
    scriptToRun.append("\n");
    
    if (this.gshTemplateOwnerType == GshTemplateOwnerType.stem) {
      scriptToRun.append("String gsh_builtin_ownerStemName = \""+StringEscapeUtils.escapeJava(ownerStemName) + "\";");
      scriptToRun.append("\n");
    } else if (this.gshTemplateOwnerType == GshTemplateOwnerType.group) {
      scriptToRun.append("String gsh_builtin_ownerGroupName = \""+StringEscapeUtils.escapeJava(ownerGroupName) + "\";");
      scriptToRun.append("\n");
    } else {
      throw new RuntimeException("Invalid gsh template owner type "+this.gshTemplateOwnerType);
    }
    
    Map<String, GshTemplateInput> gshTemplateInputsMap = new HashMap<String, GshTemplateInput>();
    
    for (GshTemplateInput gshTemplateInput: gshTemplateInputs) {
      gshTemplateInputsMap.put(gshTemplateInput.getName(), gshTemplateInput);
    }
    
    for (GshTemplateInputConfig inputConfig: templateConfig.getGshTemplateInputConfigs()) {
      
      GshTemplateInput gshTemplateInput = gshTemplateInputsMap.get(inputConfig.getName());
      
      String valueFromUser = null;
      if (gshTemplateInput != null) {
        valueFromUser = gshTemplateInput.getValueString();
        if (inputConfig.isTrimWhitespace()) {
          valueFromUser = valueFromUser.trim();
        }
      }
      
      String gshVariable = inputConfig.getGshTemplateInputType().generateGshVariable(inputConfig, valueFromUser);
      
      scriptToRun.append(gshVariable);
      
    }
    
    scriptToRun.append(templateConfig.getGshTemplate());
    GrouperSession grouperSession = null;
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(System.currentTimeMillis());
    
    try {     
      GshTemplateOutput.assignThreadLocalGshTemplateOutput(gshTemplateOutput);
      GshTemplateRuntime.assignThreadLocalGshTemplateRuntime(gshTemplateRuntime);
      grouperSession = GrouperSession.start(grouperSessionSubject);
      gshTemplateRuntime.setGrouperSession(grouperSession);

      GrouperGroovyResult grouperGroovyResult = null;
      
      this.gshTemplateExecOutput.setTransaction(templateConfig.isRunGshInTransaction());
      
      StringBuilder inputsStringBuilder = new StringBuilder();
      for (GshTemplateInput input: this.gshTemplateInputs) {
        inputsStringBuilder.append(input.getName() + " = " + GrouperUtil.abbreviate(input.getValueString(), 50) + ";");
      }
      
      final boolean success[] = {true};
      
      grouperGroovyResult = (GrouperGroovyResult) HibernateSession.callbackHibernateSession(
          templateConfig.isRunGshInTransaction() ? GrouperTransactionType.READ_WRITE_OR_USE_EXISTING: GrouperTransactionType.NONE, 
              templateConfig.isUseIndividualAudits() ? AuditControl.WILL_NOT_AUDIT: AuditControl.WILL_AUDIT,
          new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          GrouperGroovyResult grouperGroovyResult = GrouperGroovysh.runScript(scriptToRun.toString(), templateConfig.isGshLightweight(), true, linesOfScript, lineNumber);
          
          for (GshOutputLine gshOutputLine: gshTemplateExecOutput.getGshTemplateOutput().getOutputLines()) {
            if (StringUtils.equals("error", gshOutputLine.getMessageType())) {
              success[0] = false;
            }
          }
          
          if (gshTemplateExecOutput.getGshTemplateOutput().isError() || gshTemplateExecOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
            success[0] = false;
          }
          
          if ( (success[0] == false || GrouperUtil.intValue(grouperGroovyResult.getResultCode(), -1) != 0) && templateConfig.isRunGshInTransaction()) {
            hibernateHandlerBean.getHibernateSession().rollback(GrouperRollbackType.ROLLBACK_NOW);
          }
          
          if (!templateConfig.isRunGshInTransaction() || (success[0] == true && templateConfig.isRunGshInTransaction())) {
            
            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GSH_TEMPLATE_EXEC,
                "gshTemplateConfigId", configId, "status",
                success[0] ? "success": "error");
            String actAsAdditionalLine = originalCurrentUser == currentUser ? "" : ("(WsUser: "+SubjectHelper.getPretty(originalCurrentUser) + ") ");
            auditEntry.setDescription("Execute gsh template "+actAsAdditionalLine + "with configId: " + configId + ", status: " + (success[0] ? "success": "error") + ", inputs: "+inputsStringBuilder.toString());
            auditEntry.saveOrUpdate(true);
          }
          
          return grouperGroovyResult;
        }
        
      });
      
      
      
      if (this.gshTemplateExecOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
        this.gshTemplateExecOutput.setValid(false);
        
        Set<String> configuredInputNames = new HashSet<String>();
        for (GshTemplateInputConfig inputConfig: templateConfig.getGshTemplateInputConfigs()) {
          configuredInputNames.add(inputConfig.getName());
        }
        
        for (GshValidationLine gshValidationLine: this.gshTemplateExecOutput.getGshTemplateOutput().getValidationLines()) {
          if (StringUtils.isNotBlank(gshValidationLine.getInputName()) && !configuredInputNames.contains(gshValidationLine.getInputName())) {
            LOG.error(gshValidationLine.getInputName() + " is not in list of configured input names");
          }
         }
        
      }
      
      if (success[0] == false || GrouperUtil.intValue(grouperGroovyResult.getResultCode(), -1) != 0) {
        this.gshTemplateExecOutput.setSuccess(false);
      } else {
        if (gshTemplateOutput.isError()) {
          this.gshTemplateExecOutput.setSuccess(false);
        } else {
          this.gshTemplateExecOutput.setSuccess(true);
        }
      }
      
      this.gshTemplateExecOutput.setGshScriptOutput(grouperGroovyResult.getOutString());
      this.gshTemplateExecOutput.setException(grouperGroovyResult.getException());
      
      if (this.gshTemplateExecOutput.getException() != null) {
        LOG.error(grouperGroovyResult.getOutString() + ", inputs:" +inputsStringBuilder.toString(), grouperGroovyResult.getException());
      } else {
        LOG.debug(grouperGroovyResult.getOutString() + ", inputs:" +inputsStringBuilder.toString(), grouperGroovyResult.getException());
      }
      
    } catch (RuntimeException e) {
      LOG.error("Error running template with config id: "+configId, e);
      this.gshTemplateExecOutput.setSuccess(false);
      this.gshTemplateExecOutput.setException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      GshTemplateOutput.removeThreadLocalGshTemplateOutput();
      GshTemplateRuntime.removeThreadLocalGshTemplateRuntime();
      
    }
    
    return this.gshTemplateExecOutput;
  }

  
}
