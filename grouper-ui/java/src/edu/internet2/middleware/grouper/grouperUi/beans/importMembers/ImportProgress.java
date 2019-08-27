package edu.internet2.middleware.grouper.grouperUi.beans.importMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;

public class ImportProgress {
  
  private boolean viewPrivilegeError;
  
  private List<ImportError> subjectErrors = new ArrayList<ImportError>();
  
  private List<String> errors = new ArrayList<String>();
  
  private List<String> subjectNotFoundErrors = new ArrayList<String>();
  
  private Map<GuiGroup, ImportProgressForGroup> groupImportProgress = new HashMap<GuiGroup, ImportProgressForGroup>();
  
  private String key;
  
  private boolean finished;
  
  private int totalEntriesInFile;
  
  private int entriesProcessed;

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

  public int getTotalEntriesInFile() {
    return totalEntriesInFile;
  }

  public void setTotalEntriesInFile(int totalEntriesInFile) {
    this.totalEntriesInFile = totalEntriesInFile;
  }

  public int getEntriesProcessed() {
    return entriesProcessed;
  }

  public void setEntriesProcessed(int entriesProcessed) {
    this.entriesProcessed = entriesProcessed;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  public List<String> getSubjectNotFoundErrors() {
    return subjectNotFoundErrors;
  }

  public void setSubjectNotFoundErrors(List<String> subjectNotFoundErrors) {
    this.subjectNotFoundErrors = subjectNotFoundErrors;
  }
  
}
