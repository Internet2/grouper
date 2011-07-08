package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * container for permission entry to show a row on the permissions screen
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GuiPermissionEntryContainer implements Serializable, Comparable<GuiPermissionEntryContainer> {
  
  /** key is action, value is the gui permission entry */
  private Map<String, GuiPermissionEntry> actionToGuiPermissionEntryMap;

  /** memberId */
  private String memberId;
  
  /**
   * memberId
   * @return memberId
   */
  public String getMemberId() {
    return this.memberId;
  }
  
  /**
   * memberId
   * @param memberId1
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
  }

  /**
   * key is action, value is the gui permission entry
   * @return key is action, value is the gui permission entry
   */
  public Map<String, GuiPermissionEntry> getActionToGuiPermissionEntryMap() {
    return this.actionToGuiPermissionEntryMap;
  }

  /**
   * key is action, value is the gui permission entry
   * @param actionToGuiPermissionEntryMap1
   */
  public void setActionToGuiPermissionEntryMap(
      Map<String, GuiPermissionEntry> actionToGuiPermissionEntryMap1) {
    this.actionToGuiPermissionEntryMap = actionToGuiPermissionEntryMap1;
  }

  /**
   * long screen label
   */
  private String screenLabelLong = null;
  
  /**
   * short screen label
   */
  private String screenLabelShort = null;
  
  /** gui attribute assign metadata on permissions */
  private Set<GuiAttributeAssign> guiAttributeAssigns;

  /**
   * gui attribute assign metadata on permissions
   * @param theGuiAttributeAssigns
   */
  public void setGuiAttributeAssigns(Set<GuiAttributeAssign> theGuiAttributeAssigns) {
    this.guiAttributeAssigns = theGuiAttributeAssigns;
  }

  /**
   * gui attribute assign metadata on permissions
   * @return attribute assigns
   */
  public Set<GuiAttributeAssign> getGuiAttributeAssigns() {
    return this.guiAttributeAssigns;
  }

  /**
   * 
   * @return long label if different than the short one
   */
  public String getScreenLabelLongIfDifferent() {
    this.initScreenLabels();
    if (this.isNeedsTooltip()) {
      return this.screenLabelLong;
    }
    return null;
  }
  
  /**
   * get long screen label (tooltip)
   * @return tooltip
   */
  public String getScreenLabelLong() {
    this.initScreenLabels();
    return this.screenLabelLong;
  }

  /**
   * get short screen label 
   * @return short screen label
   */
  public String getScreenLabelShort() {
    this.initScreenLabels();
    return this.screenLabelShort;
  }
  
  /** role for this row */
  private Role role;
  
  /**
   * role
   * @return role
   */
  public Role getRole() {
    return this.role;
  }

  /**
   * role
   * @param role1
   */
  public void setRole(Role role1) {
    this.role = role1;
  }

  /**
   * permission definition
   */
  private AttributeDef permissionDefinition;
  
  /**
   * permission definition
   * @return permission definition
   */
  public AttributeDef getPermissionDefinition() {
    return this.permissionDefinition;
  }

  /**
   * permission definition
   * @param permissionDefinition1
   */
  public void setPermissionDefinition(AttributeDef permissionDefinition1) {
    this.permissionDefinition = permissionDefinition1;
  }

  /** permission resource for this row */
  private AttributeDefName permissionResource;
  
  /**
   * permission resource
   * @return permission resource
   */
  public AttributeDefName getPermissionResource() {
    return this.permissionResource;
  }

  /**
   * permission resource
   * @param permissionResource1
   */
  public void setPermissionResource(AttributeDefName permissionResource1) {
    this.permissionResource = permissionResource1;
  }

  /**
   * get short screen label 
   * @return short screen label
   */
  public boolean isNeedsTooltip() {
    this.initScreenLabels();
    return !StringUtils.equals(this.screenLabelLong, this.screenLabelShort);
  }

  /** gui subject */
  private GuiSubject guiSubject;

  /** raw permission entries */
  private List<PermissionEntry> rawPermissionEntries = null;

  /** 
   * limits for this row for any action in this row, note, if a limit applies to multiple actions
   * only one item will be in this list.  Also, this is a sorted set
   */
  private Set<GuiPermissionLimitBeanContainer> guiPermissionLimitBeanContainers = null;
  
  /**
   * limits for this row for any action in this row, note, if a limit applies to multiple actions
   * only one item will be in this list.  Also, this is a sorted set
   * @return set
   */
  public Set<GuiPermissionLimitBeanContainer> getGuiPermissionLimitBeanContainers() {
    return this.guiPermissionLimitBeanContainers;
  }

  /** permission type */
  private PermissionType permissionType;
  
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

  /**
   * init screen labels
   */
  private void initScreenLabels() {
    if (this.screenLabelLong == null && this.screenLabelShort == null && this.guiSubject != null) {
      
      String screenLabel = this.guiSubject.getScreenLabel();
            
      this.screenLabelLong = screenLabel;
      int maxWidth = TagUtils.mediaResourceInt("simplePermissionUpdate.maxOwnerSubjectChars", 50);
      if (maxWidth == -1) {
        this.screenLabelShort = screenLabel;
      } else {
        this.screenLabelShort = StringUtils.abbreviate(screenLabel, maxWidth);
      }
    }
  }

  /**
   * raw permission entries
   * @return raw permission entries
   */
  public List<PermissionEntry> getRawPermissionEntries() {
    return rawPermissionEntries;
  }

  /**
   * raw permission entries
   * @param rawPermissionEntries1
   */
  public void setRawPermissionEntries(List<PermissionEntry> rawPermissionEntries1) {
    this.rawPermissionEntries = rawPermissionEntries1;
  }

  /**
   * permission type
   * @return permission type
   */
  public PermissionType getPermissionType() {
    return this.permissionType;
  }

  /**
   * permission type
   * @param permissionType1
   */
  public void setPermissionType(PermissionType permissionType1) {
    this.permissionType = permissionType1;
  }
 
  /**
   * process raw entries
   * @param actions 
   * @param permissionEntryLimitBeanMap is the map of permission entry to the set of limit beans (limits and values)
   */
  public void processRawEntries(List<String> actions, Map<PermissionEntry, Set<PermissionLimitBean>> permissionEntryLimitBeanMap) {
    
    this.actionToGuiPermissionEntryMap = new HashMap<String, GuiPermissionEntry>();

    //lets get an entry for each action if there is an assignment or not
    for (String action: actions) {
      GuiPermissionEntry guiPermissionEntry = new GuiPermissionEntry();
      this.actionToGuiPermissionEntryMap.put(action, guiPermissionEntry);
      guiPermissionEntry.setPermissionType(this.permissionType);
      guiPermissionEntry.setRawGuiPermissionEntries(new ArrayList<GuiPermissionEntry>());
    }

    //we only want one entry for each limit, so keep track
    Map<AttributeAssign, GuiPermissionLimitBeanContainer> limitToGuiPermissionLimitBeanContainerMap = 
      new HashMap<AttributeAssign, GuiPermissionLimitBeanContainer>();

    for (PermissionEntry permissionEntry : this.getRawPermissionEntries()) {
      GuiPermissionEntry guiPermissionEntry = this.actionToGuiPermissionEntryMap.get(permissionEntry.getAction());
      
      //if not found, why?
      if (guiPermissionEntry == null) {
        throw new RuntimeException("Why no guiPermissionEntry for action: " 
            + permissionEntry.getAction() + ", " + GrouperUtil.stringValue(actions));
      }
      
      GuiPermissionEntry currentGui = new GuiPermissionEntry();
      currentGui.setPermissionEntry(permissionEntry);
      
      guiPermissionEntry.getRawGuiPermissionEntries().add(currentGui);
      
      //lets setup the limits
      Set<PermissionLimitBean> permissionLimitBeanSet = permissionEntryLimitBeanMap.get(permissionEntry);
      
      //if we got some
      if (GrouperUtil.length(permissionLimitBeanSet) > 0) {
        if (this.guiPermissionLimitBeanContainers == null) {
          this.guiPermissionLimitBeanContainers = new TreeSet<GuiPermissionLimitBeanContainer>();
        }
        for (PermissionLimitBean permissionLimitBean : permissionLimitBeanSet) {
          //see if we have a row for this limit yet
          GuiPermissionLimitBeanContainer guiPermissionLimitBeanContainer = limitToGuiPermissionLimitBeanContainerMap.get(permissionLimitBean.getLimitAssign());
          
          //if not, make one
          if (guiPermissionLimitBeanContainer == null) {
            guiPermissionLimitBeanContainer = new GuiPermissionLimitBeanContainer();
            guiPermissionLimitBeanContainer.setPermissionLimitBean(permissionLimitBean);
          }
          
          guiPermissionLimitBeanContainer.getActions().add(permissionEntry.getAction());
          //for the limit to be immediate, it must be an assignment on the permission, and the permission needs to be immediate
          if (permissionEntry.isImmediate(this.permissionType)) {
            AttributeAssign limitAssign = permissionLimitBean.getLimitAssign();
            if (StringUtils.equals(limitAssign.getOwnerAttributeAssignId(), permissionEntry.getAttributeAssignId())) {
              guiPermissionLimitBeanContainer.setImmediate(true);
            }
          }
          
        }
      }
      
    }
    
    //now lets process the inner objects
    for (GuiPermissionEntry guiPermissionEntry : this.actionToGuiPermissionEntryMap.values()) {
      guiPermissionEntry.processRawEntries();
    }
    
  }

  /**
   * sort on the screen from left to right on the columns
   */
  @Override
  public int compareTo(GuiPermissionEntryContainer other) {
    CompareToBuilder compareToBuilder = new CompareToBuilder().append(this.getRole().getDisplayExtension(), other.getRole().getDisplayExtension());
    if (this.permissionType == PermissionType.role_subject) {
      compareToBuilder.append(this.getScreenLabelShort(), other.getScreenLabelShort());
    }
    compareToBuilder.append(this.getPermissionResource().getDisplayExtension(), other.getPermissionResource().getDisplayExtension());
    return compareToBuilder.toComparison();
  }

}
