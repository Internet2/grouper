package edu.internet2.middleware.grouper.app.workflow;

import java.util.ArrayList;
import java.util.List;

public class GrouperWorkflowInstanceFilesInfo {
  
  private List<GrouperWorkflowInstanceFileInfo> fileNamesAndPointers = new ArrayList<GrouperWorkflowInstanceFileInfo>();

  
  public List<GrouperWorkflowInstanceFileInfo> getFileNamesAndPointers() {
    return fileNamesAndPointers;
  }

  
  public void setFileNamesAndPointers(
      List<GrouperWorkflowInstanceFileInfo> fileNamesAndPointers) {
    this.fileNamesAndPointers = fileNamesAndPointers;
  }
  

}
