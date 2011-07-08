package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;

/**
 * for displaying a permission entry limit bean row on the screen (could be multiple actions)
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GuiPermissionLimitBeanContainer implements Serializable, Comparable<GuiPermissionLimitBeanContainer> {
  
  /** permission limit and values */
  private PermissionLimitBean permissionLimitBean;

  /**
   * permission limit and values
   * @return permission limit and values
   */
  public PermissionLimitBean getPermissionLimitBean() {
    return this.permissionLimitBean;
  }

  /**
   * permission limit and values
   * @param permissionLimitBean1
   */
  public void setPermissionLimitBean(PermissionLimitBean permissionLimitBean1) {
    this.permissionLimitBean = permissionLimitBean1;
    this.limitDisplayExtension = permissionLimitBean1.getLimitAssign().getAttributeDefName().getDisplayExtension();
  }
  
  /** one limit can apply to multiple actions... keep them sorted */
  private Set<String> actions = new TreeSet<String>();

  /**
   * one limit can apply to multiple actions... keep them sorted
   * @return the set, never null
   */
  public Set<String> getActions() {
    return this.actions;
  }
  
  /** if immediate for any applicable action, i.e. can be deleted */
  private boolean immediate = false;

  /** limit display extension for sorting */
  private String limitDisplayExtension;
  
  /**
   * if immediate for any applicable action, i.e. can be deleted
   * @return if immediate
   */
  public boolean isImmediate() {
    return this.immediate;
  }

  /**
   * if immediate for any applicable action, i.e. can be deleted
   * @param immediate1
   */
  public void setImmediate(boolean immediate1) {
    this.immediate = immediate1;
  }

  /**
   * @see Comparable#compareTo(Object)
   */
  @Override
  public int compareTo(GuiPermissionLimitBeanContainer other) {
    //keep the limit display extension cached
    return this.limitDisplayExtension.compareTo(other.limitDisplayExtension);
  }

  
  
}
