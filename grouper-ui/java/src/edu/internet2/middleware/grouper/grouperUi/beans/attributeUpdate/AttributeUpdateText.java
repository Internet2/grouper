/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperHtmlFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * get text in external URL (if applicable), and from nav.properties
 */
public class AttributeUpdateText {

  /** singleton */
  private static AttributeUpdateText simpleMembershipUpdateText = new AttributeUpdateText();
  
  /**
   * get singleton
   * @return singleton
   */
  public static AttributeUpdateText retrieveSingleton() {
    return simpleMembershipUpdateText;
  }

  /** grouper html filter */
  private static GrouperHtmlFilter grouperHtmlFilter;
  
  /** if found grouper html filter found */
  private static boolean grouperHtmlFilterFound = false;
 
  /**
   * edit panel submit
   * @return title
   */
  public String getEditPanelSubmit() {
    return text("simpleAttributeUpdate.editPanelSubmit");
  }

  /**
   * edit panel submit
   * @return title
   */
  public String getEditPanelCancel() {
    return text("simpleAttributeUpdate.editPanelCancel");
  }
  
  /**
   * edit panel actions
   * @return title
   */
  public String getEditPanelActions() {
    return text("simpleAttributeUpdate.editPanelEditActions");
  }
  
  

  /**
   * edit panel delete
   * @return title
   */
  public String getEditPanelDelete() {
    return text("simpleAttributeUpdate.editPanelDelete");
  }

  
  
  /**
   * cache this
   * @return grouper html filter
   */
  @SuppressWarnings("unchecked")
  private static GrouperHtmlFilter grouperHtmlFilter() {
    if (!grouperHtmlFilterFound) {
      String grouperHtmlFilterString = TagUtils.mediaResourceString("simpleMembershipUpdate.externalUrlTextProperties.grouperHtmlFilter");
      Class<GrouperHtmlFilter> grouperHtmlFilterClass = GrouperUtil.forName(grouperHtmlFilterString);
      grouperHtmlFilter = GrouperUtil.newInstance(grouperHtmlFilterClass);
      grouperHtmlFilterFound = true;
    }
    return grouperHtmlFilter;
  }
  
  /**
   * get text based on key
   * @param key
   * @return text
   */
  public String text(String key) {
    
    //finally, just go to nav.propreties
    return TagUtils.navResourceString(key);
    
  }
  
  /**
   * title of update screen
   * @return title
   */
  public String getAssignIndexTitle() {
    return text("simpleAttributeUpdate.assignIndexTitle");
  }
  
  /**
   * title of update screen
   * @return title
   */
  public String getEditId() {
    return text("simpleAttributeUpdate.editId");
  }
  
  
  
  /**
   * title of create edit screen
   * @return the title
   */
  public String getCreateEditIndexTitle() {
    return text("simpleAttributeUpdate.createEditIndexTitle");
  }

  /**
   * infodot of the title
   * @return the infodot of the title
   */
  public String getCreateEditIndexTitleInfodot() {
    return text("simpleAttributeUpdate.createEditIndexTitleInfodot");
  }
  
  /**
   * button to edit an existing attribute definition
   * @return the infodot of the title
   */
  public String getFilterAttributeDefButton() {
    return text("simpleAttributeUpdate.filterAttributeDefButton");
  }
  
  /**
   * infodot of title of update screen
   * @return title
   */
  public String getAssignIndexTitleInfodot() {
    return text("simpleAttributeUpdate.assignIndexTitleInfodot");
  }
  
  /**
   * new attribute def button
   * @return title
   */
  public String getNewAttributeDefButton() {
    return text("simpleAttributeUpdate.newAttributeDefButton");
  }
  
  
  
//  /**
//   * @param memberDescription 
//   * @return the label
//   */
//  public String getWarningSubjectAlreadyMember(String memberDescription) {
//    String theText = text("simpleMembershipUpdate.warningSubjectAlreadyMember");
//    memberDescription = GrouperUiUtils.escapeHtml(memberDescription, true, false);
//    theText = StringUtils.replace(theText, "{0}", memberDescription);
//    return theText;
//  }

  
}
