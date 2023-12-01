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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyInput;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
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

/**
* <p>Use this class to execute a custom gsh template</p>
* <p>Sample call
* 
* <blockquote>
* <pre>
* GshTemplateExec exec = new GshTemplateExec();
* exec.assignConfigId("testGshTemplateConfig");
* exec.assignCurrentUser(subject);
* exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
* exec.assignOwnerStemName(ownerStem.getName());
* GshTemplateInput input = new GshTemplateInput();
* input.assignName("gsh_input_myExtension");
* input.assignValueString("zoomTest");
* exec.addGshTemplateInput(input);
* GshTemplateExecOutput output = exec.execute();
* if (output.getGshTemplateOutput().isError()) {
*   // handle this... e.g. from another template: gsh_builtin_gshTemplateOutput.addOutputLine("Error running sub-template");
* }
* if (GrouperUtil.length(output.getGshTemplateOutput().getValidationLines()) > 0) {
*   for (GshValidationLine gshValidationLine : output.getGshTemplateOutput().getValidationLines()) {
*     // handle this... e.g. from another template 
*     // gsh_builtin_gshTemplateOutput.addOutputLine((String)(gshValidationLine.getInputName() + ": " + gshValidationLine.getText()));
*   }
* }
* </pre>
* </blockquote>
* 
* </p>
*/
public class GshTemplateExec {
  
  /**
   * pass in so you have a reference
   */
  private GrouperGroovyRuntime grouperGroovyRuntime = null;
  
  /**
   * pass in so you have a reference
   * @return
   */
  public GrouperGroovyRuntime getGrouperGroovyRuntime() {
    return grouperGroovyRuntime;
  }

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

  private static Map<String, GshTemplateV2> configIdToGshTemplateV2 = new HashMap<>();
  
  private static Map<String, String> configIdToGshTemplateV2source = new HashMap<>();
  
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
    
    gshTemplateRuntime.setTemplateConfigId(configId);

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
          Group actAsGroup = GroupFinder.findByUuid(grouperSession, actAsGroupUUID, false);
          if (actAsGroup == null) {
            actAsGroup = GroupFinder.findByName(grouperSession, actAsGroupUUID, true);
          }
          
          if (!actAsGroup.hasMember(currentUser)) {
            throw new RuntimeException(currentUser.getId()+ " is not a member of "+actAsGroup.getName());
          }
          
          currentUser = actAsSubject;
          
        }
        
        gshTemplateRuntime.setCurrentSubject(currentUser);
        templateConfig.setCurrentUser(currentUser);
        if (!new GshTemplateValidationService().validate(templateConfig, THIS, gshTemplateExecOutput.getGshTemplateOutput())) {
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
    
    if (templateConfig.isGshLightweight()) {
      scriptToRun.append("import edu.internet2.middleware.grouper.app.gsh.template.*;\n");
      scriptToRun.append("import edu.internet2.middleware.subject.*;\n");
    }
    
    boolean templateVersionV1 = StringUtils.equals("V1", templateConfig.getTemplateVersion());
    boolean templateVersionV2 = StringUtils.equals("V2", templateConfig.getTemplateVersion());
    GshTemplateV2 gshTemplateV2 = templateVersionV1 ? null : executeForTemplateV2instance();

    GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();
    gshTemplateV2input.setGsh_builtin_gshTemplateRuntime(gshTemplateRuntime);

    GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();
    gshTemplateV2output.setGsh_builtin_gshTemplateOutput(gshTemplateOutput);

    if (templateVersionV1) {
      scriptToRun.append("GshTemplateRuntime gsh_builtin_gshTemplateRuntime = GshTemplateRuntime.retrieveGshTemplateRuntime();\n");

      scriptToRun.append("GshTemplateOutput gsh_builtin_gshTemplateOutput = GshTemplateOutput.retrieveGshTemplateOutput();\n");
      
      scriptToRun.append("GrouperSession gsh_builtin_grouperSession = grouperGroovyRuntime.getGrouperSession();\n");
      
      scriptToRun.append("Subject gsh_builtin_subject = gsh_builtin_gshTemplateRuntime.getCurrentSubject();\n");
      
      scriptToRun.append("String gsh_builtin_subjectId = gsh_builtin_gshTemplateRuntime.getCurrentSubject().getId();\n");
    }

    if (this.gshTemplateOwnerType == GshTemplateOwnerType.stem) {
      gshTemplateRuntime.setOwnerStemName(ownerStemName);
    } else if (this.gshTemplateOwnerType == GshTemplateOwnerType.group) {
      gshTemplateRuntime.setOwnerGroupName(ownerGroupName);
    } else {
      throw new RuntimeException("Invalid gsh template owner type "+this.gshTemplateOwnerType);
    }
    
    if (templateVersionV1) {
      scriptToRun.append("String gsh_builtin_ownerStemName = gsh_builtin_gshTemplateRuntime.getOwnerStemName();\n");
      scriptToRun.append("String gsh_builtin_ownerGroupName = gsh_builtin_gshTemplateRuntime.getOwnerGroupName();\n");
    }
    
    if (templateVersionV2) {

      gshTemplateV2input.setGsh_builtin_ownerStemName(gshTemplateRuntime.getOwnerStemName());
      gshTemplateV2input.setGsh_builtin_ownerGroupName(gshTemplateRuntime.getOwnerGroupName());

      gshTemplateV2input.setGsh_builtin_subject(gshTemplateRuntime.getCurrentSubject());
      gshTemplateV2input.setGsh_builtin_subjectId(gshTemplateRuntime.getCurrentSubject().getId());

    }
    Map<String, GshTemplateInput> gshTemplateInputsMap = new HashMap<String, GshTemplateInput>();
    
    for (GshTemplateInput gshTemplateInput: gshTemplateInputs) {
      gshTemplateInputsMap.put(gshTemplateInput.getName(), gshTemplateInput);
    }
    
    GrouperGroovyInput grouperGroovyInput = new GrouperGroovyInput();

    // keep a handle of the runtime
    if (this.grouperGroovyRuntime == null) {
      this.grouperGroovyRuntime = new GrouperGroovyRuntime();
    }
    grouperGroovyInput.assignGrouperGroovyRuntime(this.grouperGroovyRuntime);
    
    for (GshTemplateInputConfig inputConfig: templateConfig.getGshTemplateInputConfigs()) {
      
      GshTemplateInput gshTemplateInput = gshTemplateInputsMap.get(inputConfig.getName());
      
      String valueFromUser = null;
      if (gshTemplateInput != null) {
        valueFromUser = gshTemplateInput.getValueString();
      }
      
      if (templateVersionV1) {
        String gshVariable = inputConfig.getGshTemplateInputType().generateGshVariable(grouperGroovyInput, inputConfig, valueFromUser);
        scriptToRun.append(gshVariable);
      }
      if (templateVersionV2) {
        Object realValue = inputConfig.getGshTemplateInputType().convertToType(valueFromUser);
        gshTemplateV2input.getGsh_builtin_inputs().put(inputConfig.getName(), realValue);
      }
    }
    
    scriptToRun.append(templateConfig.getGshTemplate());
    GrouperSession grouperSession = null;
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(System.currentTimeMillis());
    
    try {     
      GshTemplateOutput.assignThreadLocalGshTemplateOutput(gshTemplateOutput);
      GshTemplateRuntime.assignThreadLocalGshTemplateRuntime(gshTemplateRuntime);
      
      grouperSession = GrouperSession.start(grouperSessionSubject, false);
      if (templateVersionV2) {
        gshTemplateV2input.setGsh_builtin_grouperSession(grouperSession);
      }
      
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          GrouperGroovyResult grouperGroovyResult = null;
          
          GshTemplateExec.this.gshTemplateExecOutput.setTransaction(templateConfig.isRunGshInTransaction());
          
          StringBuilder inputsStringBuilder = new StringBuilder();
          for (GshTemplateInput input: GshTemplateExec.this.gshTemplateInputs) {
            inputsStringBuilder.append(input.getName() + " = " + GrouperUtil.abbreviate(input.getValueString(), 100) + ";");
          }
          
          final boolean success[] = {true};
          
          grouperGroovyResult = (GrouperGroovyResult) HibernateSession.callbackHibernateSession(
              templateConfig.isRunGshInTransaction() ? GrouperTransactionType.READ_WRITE_OR_USE_EXISTING: GrouperTransactionType.NONE, 
                  templateConfig.isUseIndividualAudits() ? AuditControl.WILL_NOT_AUDIT: AuditControl.WILL_AUDIT,
              new HibernateHandler() {
      
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {

              grouperGroovyInput.assignUseTransaction(templateConfig.isRunGshInTransaction());
              grouperGroovyInput.assignScript(scriptToRun.toString());
              grouperGroovyInput.assignLightWeight(templateConfig.isGshLightweight());
              
              GrouperGroovyResult grouperGroovyResult = new GrouperGroovyResult();
              gshTemplateExecOutput.setGrouperGroovyResult(grouperGroovyResult);

              // if v2, only run the script if it is new in cache or changed
              if (templateVersionV1) {
                GrouperGroovysh.runScript(grouperGroovyInput, grouperGroovyResult);
                if (GrouperUtil.intValue(grouperGroovyResult.getResultCode(), -1) != 0) {
                  success[0] = false;
                }
              }
              if (templateVersionV2) {

                gshTemplateV2.gshRunLogic(gshTemplateV2input, gshTemplateV2output);
              }
              
              for (GshOutputLine gshOutputLine: gshTemplateExecOutput.getGshTemplateOutput().getOutputLines()) {
                if (StringUtils.equals("error", gshOutputLine.getMessageType())) {
                  success[0] = false;
                }
              }
              
              if (gshTemplateExecOutput.getGshTemplateOutput().isError() || gshTemplateExecOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
                success[0] = false;
              }
              
              if (success[0] == false && templateConfig.isRunGshInTransaction()) {
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
          
          
          
          if (GshTemplateExec.this.gshTemplateExecOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
            GshTemplateExec.this.gshTemplateExecOutput.setValid(false);
            
            Set<String> configuredInputNames = new HashSet<String>();
            for (GshTemplateInputConfig inputConfig: templateConfig.getGshTemplateInputConfigs()) {
              configuredInputNames.add(inputConfig.getName());
            }
            
            for (GshValidationLine gshValidationLine: GshTemplateExec.this.gshTemplateExecOutput.getGshTemplateOutput().getValidationLines()) {
              if (StringUtils.isNotBlank(gshValidationLine.getInputName()) && !configuredInputNames.contains(gshValidationLine.getInputName())) {
                LOG.error(gshValidationLine.getInputName() + " is not in list of configured input names");
              }
             }
            
          }
          
          if (success[0] == false || GrouperUtil.intValue(grouperGroovyResult.getResultCode(), -1) != 0) {
            GshTemplateExec.this.gshTemplateExecOutput.setSuccess(false);
          } else {
            if (gshTemplateOutput.isError()) {
              GshTemplateExec.this.gshTemplateExecOutput.setSuccess(false);
            } else {
              GshTemplateExec.this.gshTemplateExecOutput.setSuccess(true);
            }
          }
          
          GshTemplateExec.this.gshTemplateExecOutput.setGshScriptOutput(grouperGroovyResult.fullOutput());
          GshTemplateExec.this.gshTemplateExecOutput.setException(grouperGroovyResult.getException());
          
          if (GshTemplateExec.this.gshTemplateExecOutput.getException() != null) {
            LOG.error("Exception in GSH template: " + GshTemplateExec.this.configId + ", " + SubjectHelper.getPretty(GshTemplateExec.this.currentUser), grouperGroovyResult.getException());
          } else {
            LOG.debug("Exception in GSH template: " + GshTemplateExec.this.configId + ", " + SubjectHelper.getPretty(GshTemplateExec.this.currentUser), grouperGroovyResult.getException());
          }
          return null;
        }
      });
      
      
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

  /**
   * execute the gsh template (calls newInstance).
   * @return
   */
  public GshTemplateV2 executeForTemplateV2instance() {
    
    if (this.gshTemplateExecOutput == null) {
      this.gshTemplateExecOutput = new GshTemplateExecOutput();
    }
    
    GrouperUtil.assertion(StringUtils.isNotBlank(configId), "Config ID null");

    GshTemplateConfig templateConfig = new GshTemplateConfig(configId);
    
    final GshTemplateExec THIS = this;
    
    GshTemplateV2[] gshTemplateV2 = new GshTemplateV2[] {null};

    boolean unassignTemplateRuntime = GshTemplateRuntime.retrieveGshTemplateRuntime() == null;
    if (GshTemplateRuntime.retrieveGshTemplateRuntime() == null) {
      GshTemplateRuntime.assignThreadLocalGshTemplateRuntime(new GshTemplateRuntime());
    }
    GshTemplateRuntime gshTemplateRuntime = GshTemplateRuntime.retrieveGshTemplateRuntime();
    gshTemplateRuntime.setTemplateConfigId(configId);
    
    try {     
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Subject callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          templateConfig.populateConfiguration();

          StringBuilder scriptToRun = new StringBuilder();
          
          scriptToRun.append("GshTemplateRuntime gsh_builtin_gshTemplateRuntime = GshTemplateRuntime.retrieveGshTemplateRuntime();\n");
          
          boolean templateVersionV2 = StringUtils.equals("V2", templateConfig.getTemplateVersion());
          
          GrouperUtil.assertion(templateVersionV2, "GSH template must be template type V2!");
          GrouperUtil.assertion(!templateConfig.isGshLightweight(), "V2 GSH template must not be 'lightweight'!");
          
          gshTemplateV2[0] = configIdToGshTemplateV2.get(configId);
          String cachedSource = configIdToGshTemplateV2source.get(configId);
          boolean needsNewV2template = gshTemplateV2[0] == null || !StringUtils.equals(cachedSource, templateConfig.getGshTemplate());
        
          if (needsNewV2template) {
            GrouperGroovyInput grouperGroovyInput = new GrouperGroovyInput();
          
            // keep a handle of the runtime
            if (GshTemplateExec.this.grouperGroovyRuntime == null) {
              GshTemplateExec.this.grouperGroovyRuntime = new GrouperGroovyRuntime();
            }
            grouperGroovyInput.assignGrouperGroovyRuntime(GshTemplateExec.this.grouperGroovyRuntime);
            
            scriptToRun.append(templateConfig.getGshTemplate());
            
            GrouperGroovyResult grouperGroovyResult = new GrouperGroovyResult();
            grouperGroovyInput.assignScript(scriptToRun.toString());

            gshTemplateExecOutput.setGrouperGroovyResult(grouperGroovyResult);
            GrouperGroovysh.runScript(grouperGroovyInput, grouperGroovyResult);
            
            if (GrouperUtil.intValue(grouperGroovyResult.getResultCode(), -1) != 0) {
              throw new RuntimeException("Error getting gsh template v2 instance: " + grouperGroovyResult.getResultCode() 
                + ", " + grouperGroovyResult.getOutString(), grouperGroovyResult.getException());
            }
            
            gshTemplateV2[0] = gshTemplateRuntime.getGshTemplateV2();
            
            if (gshTemplateV2[0] == null) {
              throw new RuntimeException("The script did not set the template! gsh_builtin_gshTemplateRuntime.assignGshTemplateV2(gshTemplateV2);");
            }
            
            configIdToGshTemplateV2.put(configId, gshTemplateV2[0]);
            configIdToGshTemplateV2source.put(configId, templateConfig.getGshTemplate());
            if (GrouperUtil.intValue(grouperGroovyResult.getResultCode(), -1) != 0) {
              GshTemplateExec.this.gshTemplateExecOutput.setGshScriptOutput(grouperGroovyResult.fullOutput());
              GshTemplateExec.this.gshTemplateExecOutput.setException(grouperGroovyResult.getException());
              
              if (GshTemplateExec.this.gshTemplateExecOutput.getException() != null) {
  //              GshTemplateExec.this.gshTemplateExecOutput.setSuccess(false);
  //              LOG.error("Error with GSH template configId: " + configId + ", " + GshTemplateExec.this.gshTemplateExecOutput.getException());
                throw new RuntimeException(GshTemplateExec.this.gshTemplateExecOutput.getException());
              }
  
            }
          }
          return null;
        }
      });
              
    } finally {
      if (unassignTemplateRuntime) {
        GshTemplateRuntime.removeThreadLocalGshTemplateRuntime();
      }
    }
    return GrouperUtil.newInstance(gshTemplateV2[0].getClass());
    
  }

  
}
