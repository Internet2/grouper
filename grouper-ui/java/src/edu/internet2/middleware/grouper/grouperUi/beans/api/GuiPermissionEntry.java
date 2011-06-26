package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.MapWrapper;
import edu.internet2.middleware.subject.Subject;

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

  /** raw gui permission entries */
  private List<GuiPermissionEntry> rawGuiPermissionEntries = null;
  
  /**
   * see if allowed
   */
  private boolean allowed;
  
  /**
   * if there is an immediate assignment
   */
  private boolean immediate;
  
  /**
   * reason why this row wasnt chose on analyze screen
   */
  private String compareWithBest;
  
  /**
   * reason why this row wasnt chose on analyze screen
   * @return reason why this row wasnt chose on analyze screen
   */
  public String getCompareWithBest() {
    return compareWithBest;
  }

  /**
   * reason why this row wasnt chose on analyze screen
   * @param compareWithBest1
   */
  public void setCompareWithBest(String compareWithBest1) {
    this.compareWithBest = compareWithBest1;
  }


  /**
   * process raw entries
   * @param actions 
   */
  public void processRawEntries() {

    //make a new set since we dont want to mess up the original one
    Set<PermissionEntry> permissionEntriesSet = new HashSet<PermissionEntry>();
    
    for (GuiPermissionEntry guiPermissionEntry : this.rawGuiPermissionEntries) {
      permissionEntriesSet.add(guiPermissionEntry.getPermissionEntry());
    }
    
    
    PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS.processPermissions(permissionEntriesSet, null);

    //we have the permissions, was anything returned?  take the first, and see if not disallowed
    this.allowed = permissionEntriesSet.size() == 0 ? false : !permissionEntriesSet.iterator().next().isDisallowed();
    
    //see if any are immediate
    for (GuiPermissionEntry guiPermissionEntry : this.rawGuiPermissionEntries) {
      
      PermissionEntry thePermissionEntry = guiPermissionEntry.getPermissionEntry();
      
      boolean theImmediate = false;
      if (thePermissionEntry.isImmediatePermission()) {
        if (this.permissionType == PermissionType.role && thePermissionEntry.getPermissionType() == PermissionType.role) {
          theImmediate = true;
        }
        if (this.permissionType == PermissionType.role_subject && thePermissionEntry.getPermissionType() == PermissionType.role_subject) {
          if (thePermissionEntry.isImmediateMembership()) {
            theImmediate = true;
          }
        }
      }
      
      this.immediate = this.immediate || theImmediate;
      if (!theImmediate) {
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
  public List<GuiPermissionEntry> getRawGuiPermissionEntries() {
    return this.rawGuiPermissionEntries;
  }

  /**
   * raw permission entries
   * @param rawGuiPermissionEntries1
   */
  public void setRawGuiPermissionEntries(List<GuiPermissionEntry> rawGuiPermissionEntries1) {
    this.rawGuiPermissionEntries = rawGuiPermissionEntries1;
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
   * string label short
   */
  private Map<Subject, String> stringLabelShortFromGuiSubject = new MapWrapper<Subject, String>() {

    @Override
    public String get(Object key) {
      GuiSubject guiSubject = new GuiSubject((Subject)key);
      String screenLabel = guiSubject.getScreenLabel();
      
      int maxWidth = TagUtils.mediaResourceInt("simplePermissionUpdate.maxOwnerSubjectChars", 50);
      if (maxWidth == -1) {
        return screenLabel;
      }
      return StringUtils.abbreviate(screenLabel, maxWidth);
    }
  };

  /**
   * @return map to convert subject to string
   */
  public Map<Subject, String> getStringLabelShortFromGuiSubject() {
    return this.stringLabelShortFromGuiSubject;
  }

  /**
   * map to convert subject to string
   * @return map
   */
  public Map<Subject, String> getStringLabelLongIfDifferentFromGuiSubject() {
    return this.stringLabelLongIfDifferentFromGuiSubject;
  }

  /**
   * string label short
   */
  private Map<Subject, String> stringLabelLongIfDifferentFromGuiSubject = new MapWrapper<Subject, String>() {

    @Override
    public String get(Object key) { 
      GuiSubject guiSubject = new GuiSubject((Subject)key);
      String screenLabel = guiSubject.getScreenLabel();
      int maxWidth = TagUtils.mediaResourceInt("simplePermissionUpdate.maxOwnerSubjectChars", 50);
      //this means the whole thing was printed in the screen, we dont need a tooltip
      if (maxWidth == -1 || screenLabel == null || screenLabel.length() <= maxWidth) {
        return null;
      }
      return screenLabel;
    }
  };

  /**
   * get short screen label 
   * @return short screen label
   */
  public boolean isNeedsTooltip() {
    this.initScreenLabels();
    return !StringUtils.equals(this.screenLabelLong, this.screenLabelShort);
  }
  
}
