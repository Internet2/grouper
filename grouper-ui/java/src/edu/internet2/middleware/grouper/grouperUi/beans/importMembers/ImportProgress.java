package edu.internet2.middleware.grouper.grouperUi.beans.importMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;

public class ImportProgress {
  
  private boolean viewPrivilegeError;
  
  private List<ImportError> subjectErrors = new ArrayList<ImportError>();
  
  private Map<GuiGroup, ImportProgressForGroup> groupImportProgress = new HashMap<GuiGroup, ImportProgressForGroup>();
  
  private String key;
  
  private boolean finished;

  public boolean isViewPrivilegeError() {
    return viewPrivilegeError;
  }

  public void setViewPrivilegeError(boolean viewPrivilegeError) {
    this.viewPrivilegeError = viewPrivilegeError;
  }

  public List<ImportError> getSubjectErrors() {
    return subjectErrors;
  }

  public void setSubjectErrors(List<ImportError> subjectErrors) {
    this.subjectErrors = subjectErrors;
  }

  public Map<GuiGroup, ImportProgressForGroup> getGroupImportProgress() {
    return groupImportProgress;
  }

  public void setGroupImportProgress(Map<GuiGroup, ImportProgressForGroup> groupImportProgress) {
    this.groupImportProgress = groupImportProgress;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public boolean isFinished() {
    return finished;
  }

  public void setFinished(boolean finished) {
    this.finished = finished;
  }
  
}
