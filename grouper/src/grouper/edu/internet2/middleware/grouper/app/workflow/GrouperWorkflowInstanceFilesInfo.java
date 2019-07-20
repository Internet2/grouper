package edu.internet2.middleware.grouper.app.workflow;

import java.util.ArrayList;
import java.util.List;

public class GrouperWorkflowInstanceFilesInfo {
  
  /**
   * list of file info objects
   */
  private List<GrouperWorkflowInstanceFileInfo> fileNamesAndPointers = new ArrayList<GrouperWorkflowInstanceFileInfo>();

  /**
   * list of file info objects
   * @return
   */
  public List<GrouperWorkflowInstanceFileInfo> getFileNamesAndPointers() {
    return fileNamesAndPointers;
  }

  /**
   * list of file info objects
   * @param fileNamesAndPointers
   */
  public void setFileNamesAndPointers(
      List<GrouperWorkflowInstanceFileInfo> fileNamesAndPointers) {
    this.fileNamesAndPointers = fileNamesAndPointers;
  }
  

}
