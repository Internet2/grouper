/**
 * @author mchyzer
 * $Id: GroupBreadcrumbTag.java,v 1.1 2009-08-05 00:57:20 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import edu.internet2.middleware.grouper.grouperUi.json.GuiSettings;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GroupBreadcrumbTag extends SimpleTagSupport  {

  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws JspException, IOException {
    
    StringBuilder result = new StringBuilder();
    
    //<div class="browseStemsLocation">
    //  <strong>Current location is:</strong>
    //  <br><div class="currentLocationList">
    //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../public/assets/folderOpen.gif" class="groupIcon" alt="">Root:  
    //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../public/assets/folderOpen.gif" class="groupIcon" alt="">penn:
    //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../public/assets/folderOpen.gif" class="groupIcon" alt="Folder">etc:  
    //<span class="browseStemsLocationHere">
    //    <img onmouseover="grouperTooltip('Group - A collection of entities (members) which can be people, other groups or other things (e.g., resources)');" onmouseout="UnTip()" src="../public/assets/group.gif" class="groupIcon" alt="Folder">ldapUsers</span>
    //</div></div>
    GuiSettings guiSettings = GuiSettings.retrieveFromRequest();
    result.append("<div class=\"browseStemsLocation\"><strong>" + guiSettings.getText().get("browseStemsLocationLabel")
        + " </strong> &nbsp; \n");
    String[] names = GrouperUtil.splitTrim(this.groupName, ":");
    for (int i=0; i<GrouperUtil.length(names); i++) {
      //if its a folder
      if (i != GrouperUtil.length(names)-1) {
        result.append("<img onmouseover=\"grouperTooltip(\'Folder - A tree structure used to organize groups, "
          + "subfolders, and folder-level permissions\');\" onmouseout=\"UnTip()\" src=\"../public/assets/folderOpen.gif\" "
          + "class=\"groupIcon\" alt=\"Folder\">" + GuiUtils.escapeHtml(names[i], true, false) + ": ");
        
      } else {
        result.append("<span class=\"browseStemsLocationHere\">\n"
          + "<img onmouseover=\"grouperTooltip(\'Group - A collection of entities (members) which"
          + " can be people, other groups or other things (e.g., resources)\');\" onmouseout=\"UnTip()\""
          + " src=\"../public/assets/group.gif\" class=\"groupIcon\" alt=\"Object\">" + GuiUtils.escapeHtml(names[i], true, false) + "</span>\n");
      }
    }
    result.append("</div>\n");
    this.getJspContext().getOut().print(result.toString());
  }

  /** group name */
  private String groupName;

  
  /**
   * group name
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }

  
  /**
   * group name
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  
}
