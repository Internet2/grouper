package edu.internet2.middleware.grouper.grouperUi.beans.ui;


public class GroupImportError {

  public GroupImportError() {
    super();
    // TODO Auto-generated constructor stub
  }


  public GroupImportError(String subjectLabel, String errorEscaped, Integer rowNumber) {
    super();
    this.subjectLabel = subjectLabel;
    this.errorEscaped = errorEscaped;
    this.rowNumber = rowNumber;
  }

  public GroupImportError(String subjectLabel, String errorEscaped) {
    super();
    this.subjectLabel = subjectLabel;
    this.errorEscaped = errorEscaped;
  }


  private String subjectLabel;
  
  private String errorEscaped;
  
  private Integer rowNumber;

  
  public String getSubjectLabel() {
    return subjectLabel;
  }

  
  public void setSubjectLabel(String subjectLabel) {
    this.subjectLabel = subjectLabel;
  }

  
  public String getErrorEscaped() {
    return errorEscaped;
  }

  
  public void setErrorEscaped(String errorEscaped) {
    this.errorEscaped = errorEscaped;
  }

  
  public Integer getRowNumber() {
    return rowNumber;
  }

  
  public void setRowNumber(Integer rowNumber) {
    this.rowNumber = rowNumber;
  }
  
  
  
  
}
