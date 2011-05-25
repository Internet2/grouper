package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;

/**
 * for displaying a permission entry on the screen
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GuiPermissionEntry implements Serializable {
  
  /** permission entry */
  private PermissionEntry permissionEntry;

  /** permission type */
  private PermissionType permissionType;

  /** raw permission entries */
  private List<PermissionEntry> rawPermissionEntries = null;
  
  /**
   * see if allowed
   */
  private boolean allowed;
  
  /**
   * if there is an immediate assignment
   */
  private boolean immediate;
  
  /**
   * process raw entries
   * @param actions 
   */
  public void processRawEntries() {
    
    for (PermissionEntry thePermissionEntry : this.rawPermissionEntries) {
      
      if (thePermissionEntry.isEnabled()) {
        this.allowed = true;
      }
      
      boolean theImmediate = thePermissionEntry.isImmediatePermission() && thePermissionEntry.isImmediateMembership();
      
      //if it is a role assignment, and we are looking at entities, then it is not immediate
      if (thePermissionEntry.getPermissionType() == PermissionType.role && this.permissionType == PermissionType.role_subject) {
        theImmediate = false;
      }
      
      if (theImmediate) {
        
        this.immediate = true;
      } else {
        this.effective = true;
      }
    }
    
  }

  /**
   * if there is an effective assignment
   */
  private boolean effective;

  /**
   * long screen label
   */
  private String screenLabelLong = null;

  /**
   * short screen label
   */
  private String screenLabelShort = null;

  /**
   * format on screen of config for milestone: yyyy/MM/dd (not hh:mm aa)
   */
  public static final String TIMESTAMP_FORMAT = "yyyy/MM/dd";

  /**
   * <pre> format: yyyy/MM/dd HH:mm:ss.SSS synchronize code that uses this standard formatter for timestamps </pre>
   */
  final static SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
  
  /**
   * if it is effective
   * @return effective
   */
  public boolean isEffective() {
    return this.effective;
  }


  /**
   * if immediate
   * @return if immediate
   */
  public boolean isImmediate() {
    return this.immediate;
  }


  /**
   * if this is allowed
   * @return if allowed
   */
  public boolean isAllowed() {
    return this.allowed;
  }


  /**
   * permission entry
   * @return permission entry
   */
  public PermissionEntry getPermissionEntry() {
    return this.permissionEntry;
  }

  /**
   * permission entry
   * @param permissionEntry1
   */
  public void setPermissionEntry(PermissionEntry permissionEntry1) {
    this.permissionEntry = permissionEntry1;
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
   * @param timestamp 
   * @return the string format
   */
  public synchronized static String formatEnabledDisabled(Timestamp timestamp) {
    return timestampFormat.format(timestamp);
  }


  /**
   * 
   * @return the disabled date
   */
  public String getDisabledDate() {
    if (this.permissionEntry == null || this.permissionEntry.getDisabledTime() == null) {
      return null;
    }
    return formatEnabledDisabled(this.permissionEntry.getDisabledTime());
  }


  /**
   * 
   * @return the enabled date
   */
  public String getEnabledDate() {
    if (this.permissionEntry == null || this.permissionEntry.getEnabledTime() == null) {
      return null;
    }
    return formatEnabledDisabled(this.permissionEntry.getEnabledTime());
  }


  /**
   * enabled disabled key from nav.properties
   * @return enabled disabled key
   */
  public String getEnabledDisabledKey() {
    if (this.permissionEntry == null || this.permissionEntry.isEnabled()) {
      return "simplePermissionUpdate.assignEnabled";
    }
    return "simplePermissionUpdate.assignDisabled";
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
   * get short screen label 
   * @return short screen label
   */
  public String getScreenLabelShort() {
    this.initScreenLabels();
    return this.screenLabelShort;
  }


  /**
   * init screen labels
   */
  private void initScreenLabels() {
    if (this.screenLabelLong == null && this.screenLabelShort == null && this.permissionEntry != null) {
      GuiSubject guiSubject = new GuiSubject(this.permissionEntry.getMember().getSubject());
      String screenLabel = guiSubject.getScreenLabel();
            
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
   * get short screen label 
   * @return short screen label
   */
  public boolean isNeedsTooltip() {
    this.initScreenLabels();
    return !StringUtils.equals(this.screenLabelLong, this.screenLabelShort);
  }
  
}
