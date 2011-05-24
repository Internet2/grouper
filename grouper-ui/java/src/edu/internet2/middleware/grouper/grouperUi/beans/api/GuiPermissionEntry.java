package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
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

  /**
   * long screen label
   */
  private String screenLabelLong = null;
  
  /**
   * short screen label
   */
  private String screenLabelShort = null;
  
  /**
   * permission entry
   * @return permission entry
   */
  public PermissionEntry getPermissionEntry() {
    return this.permissionEntry;
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

  /**
   * get short screen label 
   * @return short screen label
   */
  public boolean isNeedsTooltip() {
    this.initScreenLabels();
    return !StringUtils.equals(this.screenLabelLong, this.screenLabelShort);
  }

  /**
   * init screen labels
   */
  private void initScreenLabels() {
    if (this.screenLabelLong == null && this.screenLabelShort == null) {
      Member member = this.permissionEntry.getMember();
            
      GuiSubject guiSubject = new GuiSubject(member.getSubject());
            
      String screenLabel = guiSubject.getScreenLabel();
            
      this.screenLabelLong = screenLabel;
      int maxWidth = TagUtils.mediaResourceInt("simpleAttributeUpdate.maxOwnerSubjectChars", 50);
      if (maxWidth == -1) {
        this.screenLabelShort = screenLabel;
      } else {
        this.screenLabelShort = StringUtils.abbreviate(screenLabel, maxWidth);
      }
    }
  }

  /**
   * permission entry
   * @param permissionEntry1
   */
  public void setPermissionEntry(PermissionEntry permissionEntry1) {
    this.clear();
    this.permissionEntry = permissionEntry1;
  }

  /**
   * clear everything out
   */
  private void clear() {
    this.screenLabelLong = null;
    this.screenLabelShort = null;
  }
  
  /**
   * format on screen of config for milestone: yyyy/MM/dd (not hh:mm aa)
   */
  public static final String TIMESTAMP_FORMAT = "yyyy/MM/dd";

  /**
   * <pre> format: yyyy/MM/dd HH:mm:ss.SSS synchronize code that uses this standard formatter for timestamps </pre>
   */
  final static SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);

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
   * @param timestamp 
   * @return the string format
   */
  public synchronized static String formatEnabledDisabled(Timestamp timestamp) {
    return timestampFormat.format(timestamp);
  }

  /**
   * enabled disabled key from nav.properties
   * @return enabled disabled key
   */
  public String getEnabledDisabledKey() {
    if (this.permissionEntry == null || this.permissionEntry.isEnabled()) {
      return "simpleAttributeUpdate.assignEnabled";
    }
    return "simpleAttributeUpdate.assignDisabled";
  }
  
}
