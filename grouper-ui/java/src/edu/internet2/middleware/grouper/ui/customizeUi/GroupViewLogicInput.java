package edu.internet2.middleware.grouper.ui.customizeUi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.Group;

public class GroupViewLogicInput extends CustomizeUiInputBase {

  /**
   * add reference to group
   */
  private Group group;
  
  /** getter */
  public Group getGroup() {
    return group;
  }

  /** setter */
  public void setGroup(Group group) {
    this.group = group;
  }
  
  
  public GroupViewLogicInput(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    super(httpServletRequest, httpServletResponse);
  }
  
}
