package edu.internet2.middleware.grouper.app.workflow;


public class GrouperWorkflowInstanceFileInfo {

  
  /**
   * { fileNamesAndPointers: [
{state: "initiate", fileName: "/something.html", filePointer: "something/something"}
]
}
   */
  
  private String state;
  
  private String fileName;
  
  private String filePointer;

  
  public String getState() {
    return state;
  }

  
  public void setState(String state) {
    this.state = state;
  }

  
  public String getFileName() {
    return fileName;
  }

  
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  
  public String getFilePointer() {
    return filePointer;
  }

  
  public void setFilePointer(String filePointer) {
    this.filePointer = filePointer;
  }
  
  
}
