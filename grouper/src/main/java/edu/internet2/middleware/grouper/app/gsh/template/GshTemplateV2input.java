package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyInput;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateV2input {

  
  private GshTemplateRuntime gsh_builtin_gshTemplateRuntime;
  
  private GrouperSession gsh_builtin_grouperSession;
  
  private Subject gsh_builtin_subject;
  
  private String gsh_builtin_subjectId;

  private String gsh_builtin_ownerStemName;
  private String gsh_builtin_ownerGroupName;
  
  private Map<String, Object> gsh_builtin_inputs = new HashMap<>();

    
  public GshTemplateRuntime getGsh_builtin_gshTemplateRuntime() {
    return gsh_builtin_gshTemplateRuntime;
  }

  
  public void setGsh_builtin_gshTemplateRuntime(
      GshTemplateRuntime gsh_builtin_gshTemplateRuntime) {
    this.gsh_builtin_gshTemplateRuntime = gsh_builtin_gshTemplateRuntime;
  }

  
  public GrouperSession getGsh_builtin_grouperSession() {
    return gsh_builtin_grouperSession;
  }

  
  public void setGsh_builtin_grouperSession(GrouperSession gsh_builtin_grouperSession) {
    this.gsh_builtin_grouperSession = gsh_builtin_grouperSession;
  }

  
  public Subject getGsh_builtin_subject() {
    return gsh_builtin_subject;
  }

  
  public void setGsh_builtin_subject(Subject gsh_builtin_subject) {
    this.gsh_builtin_subject = gsh_builtin_subject;
  }

  
  public String getGsh_builtin_subjectId() {
    return gsh_builtin_subjectId;
  }

  
  public void setGsh_builtin_subjectId(String gsh_builtin_subjectId) {
    this.gsh_builtin_subjectId = gsh_builtin_subjectId;
  }

  
  public String getGsh_builtin_ownerStemName() {
    return gsh_builtin_ownerStemName;
  }

  
  public void setGsh_builtin_ownerStemName(String gsh_builtin_ownerStemName) {
    this.gsh_builtin_ownerStemName = gsh_builtin_ownerStemName;
  }

  
  public String getGsh_builtin_ownerGroupName() {
    return gsh_builtin_ownerGroupName;
  }

  
  public void setGsh_builtin_ownerGroupName(String gsh_builtin_ownerGroupName) {
    this.gsh_builtin_ownerGroupName = gsh_builtin_ownerGroupName;
  }

  public String getGsh_builtin_inputString(String inputName) {
    return (String)this.gsh_builtin_inputs.get(inputName);
  }

  public Boolean getGsh_builtin_inputBoolean(String inputName) {
    return (Boolean)this.gsh_builtin_inputs.get(inputName);
  }

  public Integer getGsh_builtin_inputInteger(String inputName) {
    return (Integer)this.gsh_builtin_inputs.get(inputName);
  }
  
  public Map<String, Object> getGsh_builtin_inputs() {
    return gsh_builtin_inputs;
  }

  

}
