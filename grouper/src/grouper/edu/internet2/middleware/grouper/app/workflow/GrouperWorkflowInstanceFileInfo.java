package edu.internet2.middleware.grouper.app.workflow;


public class GrouperWorkflowInstanceFileInfo {

  
  /**
   * { fileNamesAndPointers: [
{state: "initiate", fileName: "/something.html", filePointer: "something/something"}
]
}
   */
  
  /**
   * state name
   */
  private String state;
  
  /**
   * file name
   */
  private String fileName;
  
  /**
   * file pointer
   */
  private String filePointer;

  /**
   * state name
   * @return
   */
  public String getState() {
    return state;
  }

  /**
   * state name
   * @param state
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * file name
   * @return
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * file name
   * @param fileName
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * file pointer
   * @return
   */
  public String getFilePointer() {
    return filePointer;
  }

  /**
   * file pointer
   * @param filePointer
   */
  public void setFilePointer(String filePointer) {
    this.filePointer = filePointer;
  }
  
  
}
