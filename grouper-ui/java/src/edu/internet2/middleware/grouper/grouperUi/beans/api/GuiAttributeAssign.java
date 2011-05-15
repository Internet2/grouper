package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * for displaying an attribute assignment on the screen
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GuiAttributeAssign implements Serializable {
  
  /** attribute assignment */
  private AttributeAssign attributeAssign;

  /**
   * long screen label
   */
  private String screenLabelLong = null;
  
  /**
   * short screen label
   */
  private String screenLabelShort = null;
  
  /**
   * attribute assignment
   * @return attribute assignment
   */
  public AttributeAssign getAttributeAssign() {
    return this.attributeAssign;
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
      
      //lets do a few types of assignments here
      if (this.attributeAssign != null) {
        switch (this.attributeAssign.getAttributeAssignType()) {
          
          case stem:
          {
              Stem stem = this.attributeAssign.getOwnerStem();
              this.screenLabelLong = stem.getDisplayName();
              this.screenLabelShort = stem.getDisplayExtension();
              
              break;
          }
          case attr_def:
          {
              AttributeDef attributeDef = this.attributeAssign.getOwnerAttributeDef();
              this.screenLabelLong = attributeDef.getName();
              this.screenLabelShort = attributeDef.getExtension();
              
              break;
          }
          case group:
          {
              Group group = this.attributeAssign.getOwnerGroup();
              this.screenLabelLong = group.getDisplayName();
              this.screenLabelShort = group.getDisplayExtension();
              
              break;
          }
          case any_mem:
          {
              Member member = this.attributeAssign.getOwnerMember();
              Group group = this.attributeAssign.getOwnerGroup();
              
              Subject subject = member.getSubject();
              
              initScreenLabelsMembership(group, subject);
              
              break;
          }
          case imm_mem:
          {
            Membership membership = this.attributeAssign.getOwnerMembership();
            Group group = membership.getGroup();
            
            Subject subject = membership.getMember().getSubject();

            initScreenLabelsMembership(group, subject);

            break;
          }
          case member:
          {
            Member member = this.attributeAssign.getOwnerMember();
            
            GuiSubject guiSubject = new GuiSubject(member.getSubject());
            
            String screenLabel = guiSubject.getScreenLabel();
            
            this.screenLabelLong = screenLabel;
            int maxWidth = TagUtils.mediaResourceInt("simpleAttributeUpdate.maxOwnerSubjectChars", 50);
            if (maxWidth == -1) {
              this.screenLabelShort = screenLabel;
            } else {
              this.screenLabelShort = StringUtils.abbreviate(screenLabel, maxWidth);
            }

            break;
          }
          default: 
            throw new RuntimeException("Not expecting attributeAssignType: " + this.attributeAssign.getAttributeAssignType());
          
        }
      }
    }

  }

  /**
   * init screen labels for a membership
   * @param group
   * @param subject
   */
  private void initScreenLabelsMembership(Group group, Subject subject) {
    GuiSubject guiSubject = new GuiSubject(subject);
    
    String screenLabel = guiSubject.getScreenLabel();
    
    this.screenLabelLong = group.getDisplayName() + " - " + screenLabel;
    int maxWidth = TagUtils.mediaResourceInt("simpleAttributeUpdate.maxOwnerSubjectChars", 50);
    String abbreviatedSubject = null;
    if (maxWidth == -1) {
      abbreviatedSubject = screenLabel;
    } else {
      abbreviatedSubject = StringUtils.abbreviate(screenLabel, maxWidth);
    }
    this.screenLabelShort = group.getDisplayExtension() + " - " + abbreviatedSubject;
  }

  /**
   * attribute assignment
   * @param attributeAssign1
   */
  public void setAttributeAssign(AttributeAssign attributeAssign1) {
    this.clear();
    this.attributeAssign = attributeAssign1;
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
    if (this.attributeAssign == null || this.attributeAssign.getDisabledTime() == null) {
      return null;
    }
    return formatEnabledDisabled(this.attributeAssign.getDisabledTime());
  }

  /**
   * 
   * @return the enabled date
   */
  public String getEnabledDate() {
    if (this.attributeAssign == null || this.attributeAssign.getEnabledTime() == null) {
      return null;
    }
    return formatEnabledDisabled(this.attributeAssign.getEnabledTime());
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
    if (this.attributeAssign == null || this.attributeAssign.isEnabled()) {
      return "simpleAttributeUpdate.assignEnabled";
    }
    return "simpleAttributeUpdate.assignDisabled";
  }
  
}
