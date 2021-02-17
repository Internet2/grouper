package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateRuntime {
  
  private Subject currentSubject;
  
  private GrouperSession grouperSession;
  
  private static ThreadLocal<GshTemplateRuntime> threadLocalGshTemplateRuntime = new InheritableThreadLocal<GshTemplateRuntime>();

  
  public Subject getCurrentSubject() {
    return currentSubject;
  }

  
  public void setCurrentSubject(Subject currentSubject) {
    this.currentSubject = currentSubject;
  }

  
  public GrouperSession getGrouperSession() {
    return grouperSession;
  }

  
  public void setGrouperSession(GrouperSession grouperSession) {
    this.grouperSession = grouperSession;
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
  
}
