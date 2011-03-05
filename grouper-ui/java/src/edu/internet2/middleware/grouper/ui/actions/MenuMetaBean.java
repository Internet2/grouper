/*
 * @author mchyzer
 * $Id: MenuMetaBean.java,v 1.1 2008-04-08 07:51:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * Information about menu
 */
public class MenuMetaBean implements Serializable {

  /** the menu */
  private List<Map<String, String>> menu;
  
  /** the enrollmentMenu */
  private List<Map<String, String>> enrollmentMenu;
  
  /** the responsibilitiesMenu */
  private List<Map<String, String>> responsibilitiesMenu;
  
  /** the toolsMenu */
  private List<Map<String, String>> toolsMenu;
  
  /**
   * menu meta bean with menu
   * @param theMenu
   */
  public MenuMetaBean(List<Map<String, String>> theMenu) {
    this.menu = theMenu;
  }
  
  /**
   * 
   * @return the enrollment menu part
   */
  public List<Map<String, String>> getEnrollmentMenuPart() {
    if (this.enrollmentMenu == null) {
      List<Map<String, String>> result = new ArrayList<Map<String, String>>();
      for (Map<String, String> menuItem : this.menu) {
        //link key
        String functionalArea = menuItem.get("functionalArea");
        if (StringUtils.equals(functionalArea, "MyGroups") || 
            StringUtils.equals(functionalArea, "JoinGroups")) {
          result.add(menuItem);
        }
      }
      this.enrollmentMenu = result;
    }
    return this.enrollmentMenu;
  }
  
  /**
   * 
   * @return the responsibilities menu part
   */
  public List<Map<String, String>> getResponsibilitiesMenuPart() {
    if (this.responsibilitiesMenu == null) {
      List<Map<String, String>> result = new ArrayList<Map<String, String>>();
      for (Map<String, String> menuItem : this.menu) {
        //link key
        String functionalArea = menuItem.get("functionalArea");
        if (StringUtils.equals(functionalArea, "ManageGroups") || 
            StringUtils.equals(functionalArea, "CreateGroups")) {
          result.add(menuItem);
        }
      }
      this.responsibilitiesMenu = result;
    }
    return this.responsibilitiesMenu;
  }
  
  /**
   * 
   * @return the responsibilities menu part
   */
  public List<Map<String, String>> getToolsMenuPart() {
    if (this.toolsMenu == null) {
      List<Map<String, String>> result = new ArrayList<Map<String, String>>(this.menu);
      //remove the other menus, and see whats left
      result.removeAll(this.getEnrollmentMenuPart());
      result.removeAll(this.getResponsibilitiesMenuPart());
      this.toolsMenu = result;
    }
    return this.toolsMenu;
  }
  
  /**
   * see if menu has enrollment
   * @return the enrollment
   */
  public boolean isHasEnrollment() {
    return this.getEnrollmentMenuPart().size() > 0;
  }
  
  /**
   * see if menu has responsibilities
   * @return the responsibilities
   */
  public boolean isHasResponsibilities() {
    return this.getResponsibilitiesMenuPart().size() > 0;
  }
}
