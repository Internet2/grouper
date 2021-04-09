package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateRuntime {
  
  private Subject currentSubject;
  
  private static ThreadLocal<GshTemplateRuntime> threadLocalGshTemplateRuntime = new InheritableThreadLocal<GshTemplateRuntime>();

  
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
    return threadLocalGshTemplateRuntime.get();
  }
  
  
  public static void assignThreadLocalGshTemplateRuntime(GshTemplateRuntime gshTemplateRuntime) {
    threadLocalGshTemplateRuntime.set(gshTemplateRuntime);
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
