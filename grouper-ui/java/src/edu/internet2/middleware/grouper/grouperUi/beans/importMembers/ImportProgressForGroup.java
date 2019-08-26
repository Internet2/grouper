package edu.internet2.middleware.grouper.grouperUi.beans.importMembers;

import java.util.ArrayList;
import java.util.List;

public class ImportProgressForGroup {
  
  private int originalCount;
  
  private int addedCount;
  
  private int deletedCount;
  
  private boolean updatePrivilegeError;
  
  private List<String> importErrors = new ArrayList<String>();

  public int getOriginalCount() {
    return originalCount;
  }

  public void setOriginalCount(int originalCount) {
    this.originalCount = originalCount;
  }

  public int getAddedCount() {
    return addedCount;
  }

  public void setAddedCount(int addedCount) {
    this.addedCount = addedCount;
  }

  public int getDeletedCount() {
    return deletedCount;
  }

  public void setDeletedCount(int deletedCount) {
    this.deletedCount = deletedCount;
  }
  
  public boolean isUpdatePrivilegeError() {
    return updatePrivilegeError;
  }

  public void setUpdatePrivilegeError(boolean updatePrivilegeError) {
    this.updatePrivilegeError = updatePrivilegeError;
  }

  public List<String> getImportErrors() {
    return importErrors;
  }

  public void setImportErrors(List<String> importErrors) {
    this.importErrors = importErrors;
  }
  
}
