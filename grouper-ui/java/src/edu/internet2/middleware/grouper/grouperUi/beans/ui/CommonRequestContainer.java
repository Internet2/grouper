package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;


public class CommonRequestContainer {

  /**
   * if tooltip should be shown
   */
  private boolean showTooltip = false;
  
  /**
   * if tooltip should be shown
   * @return if tooltip
   */
  public boolean isShowTooltip() {
    return this.showTooltip;
  }

  /**
   * if tooltip should be shown
   * @param showTooltip1
   */
  public void setShowTooltip(boolean showTooltip1) {
    this.showTooltip = showTooltip1;
  }

  /**
   * if should show path
   */
  private boolean showPath = false;
  
  /**
   * if should show path
   * @return if path
   */
  public boolean isShowPath() {
    return this.showPath;
  }

  /**
   * if should show path
   * @param showPath1
   */
  public void setShowPath(boolean showPath1) {
    this.showPath = showPath1;
  }

  /**
   * if should show icon
   */
  private boolean showIcon = false;
  
  /**
   * if should show icon
   * @return if should show icon
   */
  public boolean isShowIcon() {
    return this.showIcon;
  }

  /**
   * if should show icon
   * @param showIcon1
   */
  public void setShowIcon(boolean showIcon1) {
    this.showIcon = showIcon1;
  }

  /**
   * gui attribute def name
   */
  private GuiAttributeDefName guiAttributeDefName;
  
  
  
  /**
   * gui attribute def name
   * @return gui attribute def name
   */
  public GuiAttributeDefName getGuiAttributeDefName() {
    return this.guiAttributeDefName;
  }

  /**
   * gui attribute def name
   * @param guiAttributeDefName1
   */
  public void setGuiAttributeDefName(GuiAttributeDefName guiAttributeDefName1) {
    this.guiAttributeDefName = guiAttributeDefName1;
  }

  /**
   * gui group
   */
  private GuiGroup guiGroup;

  /**
   * gui group
   * @return gui group
   */
  public GuiGroup getGuiGroup() {
    return this.guiGroup;
  }

  /**
   * gui group
   * @param guiGroup1
   */
  public void setGuiGroup(GuiGroup guiGroup1) {
    this.guiGroup = guiGroup1;
  }
  
  /**
   * gui member
   */
  private GuiMember guiMember;

  /**
   * gui member
   * @return gui member
   */
  public GuiMember getGuiMember() {
    return this.guiMember;
  }

  /**
   * gui member
   * @param guiMember1
   */
  public void setGuiMember(GuiMember guiMember1) {
    this.guiMember = guiMember1;
  }
  
  /**
   * gui subject
   */
  private GuiSubject guiSubject;

  /**
   * gui subject
   * @return gui subject
   */
  public GuiSubject getGuiSubject() {
    return this.guiSubject;
  }

  /**
   * gui subject
   * @param guiSubject1
   */
  public void setGuiSubject(GuiSubject guiSubject1) {
    this.guiSubject = guiSubject1;
  }

  
}
