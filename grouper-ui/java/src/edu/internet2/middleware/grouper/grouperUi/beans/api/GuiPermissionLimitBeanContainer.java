package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
   * get actions comma separated
   * @return actions comma separated
   */
  public String getActionsCommaSeparated() {
    return StringUtils.join(GrouperUtil.nonNull(this.actions).iterator(), ", ");
  }
  
  /**
   * 
   * @return if has multiple actions
   */
  public boolean isHasMultipleActions() {
    return GrouperUtil.nonNull(this.actions).size() > 1;
  }
  
  /**
   * if it has values
   * @return if has values
   */
  public boolean isHasValues() {
    return GrouperUtil.nonNull(this.permissionLimitBean.getLimitAssignValues()).size() > 0;
  }
  
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
   * get the tooltip for where assigned
   * @return the tooltip
   */
  public String getAssignedToTooltip() {
    StringBuilder result = new StringBuilder();
    
    //everything has a role
    AttributeAssign limitAssign = this.getPermissionLimitBean().getLimitAssign();
    
    AttributeAssignType attributeAssignType = limitAssign.getAttributeAssignType();
    if (AttributeAssignType.group == attributeAssignType) {
      result.append(StringUtils.replace(TagUtils.navResourceString("simplePermissionUpdate.limitRoleTypeLabel"), "\"", "&quot;")).append("<br />");
    } else if (AttributeAssignType.any_mem == attributeAssignType) {
      result.append(StringUtils.replace(TagUtils.navResourceString("simplePermissionUpdate.limitMembershipTypeLabel"), "\"", "&quot;")).append("<br />");
    } else if (AttributeAssignType.any_mem_asgn == attributeAssignType || AttributeAssignType.group_asgn == attributeAssignType) {
      result.append(StringUtils.replace(TagUtils.navResourceString("simplePermissionUpdate.limitPermisssionTypeLabel"), "\"", "&quot;")).append("<br />");
    } else {
      throw new RuntimeException("Not expecting limit assign type: " + attributeAssignType);
    }

    AttributeAssign ownerAttributeAssign = limitAssign.getAttributeAssignType().isAssignmentOnAssignment() ? limitAssign.getOwnerAttributeAssign() : null;

    Role role = limitAssign.getAttributeAssignType().isAssignmentOnAssignment() ? ownerAttributeAssign.getOwnerGroup() : limitAssign.getOwnerGroup();
    result.append(GrouperUiUtils.escapeHtml(TagUtils.navResourceString("simplePermissionUpdate.limitAssignedToRoleLabel"), true));
    result.append(" ").append(role.getDisplayName()).append("<br />");

    if (AttributeAssignType.any_mem == attributeAssignType) {
      GuiSubject guiSubject = new GuiSubject(limitAssign.getOwnerMember().getSubject());
      
      result.append(GrouperUiUtils.escapeHtml(TagUtils.navResourceString("simplePermissionUpdate.limitAssignedToEntityLabel"), true));
      result.append(" ").append(guiSubject.getScreenLabel()).append("<br />");
    }

    if (AttributeAssignType.any_mem_asgn == attributeAssignType) {
      GuiSubject guiSubject = new GuiSubject(ownerAttributeAssign.getOwnerMember().getSubject());
      
      result.append(GrouperUiUtils.escapeHtml(TagUtils.navResourceString("simplePermissionUpdate.limitAssignedToEntityLabel"), true));
      result.append(" ").append(guiSubject.getScreenLabel()).append("<br />");
    }
    
    if (limitAssign.getAttributeAssignType().isAssignmentOnAssignment()) {
      
      result.append(GrouperUiUtils.escapeHtml(TagUtils.navResourceString("simplePermissionUpdate.limitAssignedToPermissionActionLabel"), true));
      result.append(" ").append(ownerAttributeAssign.getAttributeAssignAction().getName()).append("<br />");
      
      result.append(GrouperUiUtils.escapeHtml(TagUtils.navResourceString("simplePermissionUpdate.limitAssignedToPermissionNameLabel"), true));
      result.append(" ").append(ownerAttributeAssign.getAttributeDefName().getDisplayName()).append("<br />");
      
    }
    
    result.append(GrouperUiUtils.escapeHtml(TagUtils.navResourceString("simplePermissionUpdate.limitIdLabel"), true));
    result.append(" ").append(limitAssign.getId());
        
    return result.toString();
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
