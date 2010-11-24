/**
 * @author mchyzer
 * $Id: GroupBreadcrumbTag.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * shows the folders and group which is being looked at
 */
public class GroupBreadcrumbTag extends SimpleTagSupport  {

  /**
   * Text in the label if not simpleMembershipUpdate.find.browse.here
   */
  private String label;
  
  /**
   * Text in the label if not simpleMembershipUpdate.find.browse.here
   * @param theLabel
   */
  public void setLabel(String theLabel) {
    this.label = theLabel;
  }
  
  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws JspException, IOException {
    
    StringBuilder result = new StringBuilder();
    
    //<div class="browseStemsLocation">
    //  <strong>Current location is:</strong>
    //  <br><div class="currentLocationList">
    //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../../grouperExternal/public/assets/images/folderOpen.gif" class="groupIcon" alt="">Root:  
    //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../../grouperExternal/public/assets/images/folderOpen.gif" class="groupIcon" alt="">penn:
    //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../../grouperExternal/public/assets/images/folderOpen.gif" class="groupIcon" alt="Folder">etc:  
    //<span class="browseStemsLocationHere">
    //    <img onmouseover="grouperTooltip('Group - A collection of entities (members) which can be people, other groups or other things (e.g., resources)');" onmouseout="UnTip()" src="../../grouperExternal/public/assets/images/group.gif" class="groupIcon" alt="Folder">ldapUsers</span>
    //</div></div>
    String theLabel = StringUtils.isBlank(this.label) ? GrouperUiUtils.message("simpleMembershipUpdate.find.browse.here", false) : this.label;
    result.append("<div class=\"browseStemsLocation\"><strong>" + theLabel
        + " </strong> &nbsp; \n");
    String[] names = GrouperUtil.splitTrim(this.groupName, ":");
    for (int i=0; i<GrouperUtil.length(names); i++) {
      //if its a folder
      if (i != GrouperUtil.length(names)-1) {
        result.append("<img onmouseover=\"grouperTooltip(\'" 
            + GrouperUiUtils.escapeHtml(GrouperUiUtils.message("stem.icon.tooltip", false), true, true) 
            + "\');\" onmouseout=\"UnTip()\" src=\"../../grouperExternal/public/assets/images/folder.gif\" "
          + "class=\"groupIcon\" alt=\"" + GrouperUiUtils.message("stem.icon.alt", false) + "\"/>" 
          + GrouperUiUtils.escapeHtml(names[i], true, false) + ": ");
        
      } else {
        result.append("<span class=\"browseStemsLocationHere\">\n"
          + "<img onmouseover=\"grouperTooltip(\'" 
          + GrouperUiUtils.escapeHtml(GrouperUiUtils.message("group.icon.tooltip", true), true, true) + "\');\" onmouseout=\"UnTip()\""
          + " src=\"../../grouperExternal/public/assets/images/group.png\" class=\"groupIcon\" alt=\"" + GrouperUiUtils.message("group.icon.alt", true) 
          + "\"/>" + GrouperUiUtils.escapeHtml(names[i], true, false) + "</span>\n");
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
