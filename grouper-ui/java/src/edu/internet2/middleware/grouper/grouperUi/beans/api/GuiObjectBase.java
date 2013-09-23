package edu.internet2.middleware.grouper.grouperUi.beans.api;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public abstract class GuiObjectBase {

  /**
   * get the gui object
   * @return the object
   */
  public abstract GrouperObject getGrouperObject();
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "" + this.getGrouperObject();
  }
  
  /**
   * colon space separated path e.g.
   * Full : Path : To : The : Entity
   * @return the colon space separated path
   */
  public String getPathColonSpaceSeparated() {

    String parentStemName = GrouperUtil.parentStemNameFromName(this.getGrouperObject().getDisplayName());
    
    if (StringUtils.isBlank(parentStemName) || StringUtils.equals(":", parentStemName)) {
      return ":";
    }

    return parentStemName.replace(":", " : ");
    
  }
  
  /**
   * title for browser:
   * &lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
   * note, this is not xml escaped, if used in a title tag, it needs to be xml escaped...
   * @return the title
   */
  public String getTitle() {
    
    StringBuilder result = new StringBuilder();
    
    result.append("<strong>").append(
        TextContainer.retrieveFromRequest().getText().get("guiTooltipFolderLabel"))
        .append("</string><br />").append(GrouperUtil.xmlEscape(this.getPathColonSpaceSeparated(), true));
    result.append("<br />");
    result.append(GrouperUtil.xmlEscape(StringUtils.abbreviate(StringUtils.defaultString(this.getGrouperObject().getDescription()), 100), true));
    
    String resultString = result.toString();
    return resultString;
  }

  
}


