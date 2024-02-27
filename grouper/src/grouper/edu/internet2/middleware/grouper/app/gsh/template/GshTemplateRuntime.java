package edu.internet2.middleware.grouper.app.gsh.template;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateRuntime {
  
  /**
   * arbitrary wsInput
   */
  private Map<String, Object> wsInput = new HashMap<String, Object>();
  
  
  
  /**
   * arbitrary wsInput
   * @return
   */
  public Map<String, Object> getWsInput() {
    return wsInput;
  }

  /**
   * arbitrary wsInput
   * @param wsInput
   */
  public void setWsInput(Map<String, Object> wsInput) {
    this.wsInput = wsInput;
  }

  /**
   * set this from script
   */
  private GshTemplateV2 gshTemplateV2;

  /**
   * set this from script
   * @param gshTemplateV2
   */
  public void assignGshTemplateV2(GshTemplateV2 gshTemplateV2) {
    this.gshTemplateV2 = gshTemplateV2;
  }
  
  /**
   * set this from script
   * @param gshTemplateV2
   */
  public void assignGshTemplateV2internal(GshTemplateV2 gshTemplateV2) {
    // if the script didnt set it, try to set it
    if (this.gshTemplateV2 == null) { 
      this.gshTemplateV2 = gshTemplateV2;
    }
  }
  

  /**
   * set this from script
   * @return
   */
  public GshTemplateV2 getGshTemplateV2() {
    return gshTemplateV2;
  }

  private String templateConfigId;
  
  public String getTemplateConfigId() {
    return templateConfigId;
  }
  
  public void setTemplateConfigId(String templateConfigId) {
    this.templateConfigId = templateConfigId;
  }

  private Subject currentSubject;
  
  private static ThreadLocal<WeakReference<GshTemplateRuntime>> threadLocalGshTemplateRuntime = new InheritableThreadLocal<>();

  
  public Subject getCurrentSubject() {
    return currentSubject;
  }

  
  public void setCurrentSubject(Subject currentSubject) {
    this.currentSubject = currentSubject;
  }

  
  public GrouperSession getGrouperSession() {
    GrouperGroovyRuntime grouperGroovyRuntime = GrouperGroovyRuntime.retrieveGrouperGroovyRuntime();
    if (grouperGroovyRuntime != null) {
      return grouperGroovyRuntime.getGrouperSession();
    }
    return null;
  }

  
  public static GshTemplateRuntime retrieveGshTemplateRuntime() {
    WeakReference<GshTemplateRuntime> weakReference = threadLocalGshTemplateRuntime.get();
    return weakReference == null ? null : weakReference.get();
  }
  
  
  public static void assignThreadLocalGshTemplateRuntime(GshTemplateRuntime gshTemplateRuntime) {
    threadLocalGshTemplateRuntime.set(new WeakReference(gshTemplateRuntime));
  }
  
  public static void removeThreadLocalGshTemplateRuntime() {
    threadLocalGshTemplateRuntime.remove();
  }

  /**
   * owner stem name where template was called
   */
  private String ownerStemName;

  /**
   * owner stem name where template was called
   * @param ownerStemName
   */
  public void setOwnerStemName(String ownerStemName) {
    this.ownerStemName = ownerStemName;
  }

  /**
   * owner group name where template was called
   */
  private String ownerGroupName;
  
  /**
   * owner group name where template was called
   * @param ownerGroupName
   */
  public void setOwnerGroupName(String ownerGroupName) {
    this.ownerGroupName = ownerGroupName;
  }

  /**
   * owner stem name where template was called
   * @return
   */
  public String getOwnerStemName() {
    return ownerStemName;
  }

  /**
   * owner group name where template was called
   * @return
   */
  public String getOwnerGroupName() {
    return ownerGroupName;
  }
  
}
