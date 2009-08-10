/**
 * @author mchyzer
 * $Id: GroupBreadcrumbTag.java,v 1.3 2009-08-10 03:27:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

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
    //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../public/assets/images/folderOpen.gif" class="groupIcon" alt="">Root:  
    //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../public/assets/images/folderOpen.gif" class="groupIcon" alt="">penn:
    //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../public/assets/images/folderOpen.gif" class="groupIcon" alt="Folder">etc:  
    //<span class="browseStemsLocationHere">
    //    <img onmouseover="grouperTooltip('Group - A collection of entities (members) which can be people, other groups or other things (e.g., resources)');" onmouseout="UnTip()" src="../public/assets/images/group.gif" class="groupIcon" alt="Folder">ldapUsers</span>
    //</div></div>
    result.append("<div class=\"browseStemsLocation\"><strong>" + GuiUtils.message("simpleMembershipUpdate.find.browse.here", false)
        + " </strong> &nbsp; \n");
    String[] names = GrouperUtil.splitTrim(this.groupName, ":");
    for (int i=0; i<GrouperUtil.length(names); i++) {
      //if its a folder
      if (i != GrouperUtil.length(names)-1) {
        result.append("<img onmouseover=\"grouperTooltip(\'" 
            + GuiUtils.escapeHtml(GuiUtils.message("stem.icon.tooltip", false), true, true) 
            + "\');\" onmouseout=\"UnTip()\" src=\"../public/assets/images/folderOpen.gif\" "
          + "class=\"groupIcon\" alt=\"" + GuiUtils.message("stem.icon.alt", false) + "\"/>" 
          + GuiUtils.escapeHtml(names[i], true, false) + ": ");
        
      } else {
        result.append("<span class=\"browseStemsLocationHere\">\n"
          + "<img onmouseover=\"grouperTooltip(\'" 
          + GuiUtils.escapeHtml(GuiUtils.message("group.icon.tooltip", true), true, true) + "\');\" onmouseout=\"UnTip()\""
          + " src=\"../public/assets/images/group.gif\" class=\"groupIcon\" alt=\"" + GuiUtils.message("group.icon.alt", true) 
          + "\"/>" + GuiUtils.escapeHtml(names[i], true, false) + "</span>\n");
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
