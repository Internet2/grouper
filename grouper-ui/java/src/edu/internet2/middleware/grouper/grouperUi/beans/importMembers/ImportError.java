package edu.internet2.middleware.grouper.grouperUi.beans.importMembers;

public class ImportError {
  
  private int rowNumber = -1;
  
  private String errorKey;
  
  private String subjectId;
  
  public ImportError() {}
  
  public ImportError(int rowNumber, String errorKey) {
    this(rowNumber, errorKey, null);
  }
  
  public ImportError(int rowNumber, String errorKey, String subjectId) {
    this.rowNumber = rowNumber;
    this.errorKey = errorKey;
    this.subjectId = subjectId;
  }
  
  public ImportError(String errorKey) {
    this(-1, errorKey);
  }

  public int getRowNumber() {
    return rowNumber;
  }

  public void setRowNumber(int rowNumber) {
    this.rowNumber = rowNumber;
  }

  public String getErrorKey() {
    return errorKey;
  }

  public void setErrorKey(String errorKey) {
    this.errorKey = errorKey;
  }

  public String getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }
  
}
