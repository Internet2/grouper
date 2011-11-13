/*
 * @author mchyzer
 * $Id: GuiSubject.java,v 1.2 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * subject for gui has all attributes etc, and getter to be accessed from screen
 */
public class GuiSubject implements Serializable {
  
  /** subject */
  private Subject subject;
  
  /**
   * init screen labels
   */
  private void initScreenLabels() {
    if (this.screenLabelLong == null && this.screenLabelShort == null) {
      
      String screenLabel = GrouperUiUtils.convertSubjectToLabelLong(this.subject);
            
      this.screenLabelLong = screenLabel;
      
      screenLabel = GrouperUiUtils.convertSubjectToLabel(this.subject);
      
      int maxWidth = TagUtils.mediaResourceInt("subject.maxChars", 100);
      if (maxWidth == -1) {
        this.screenLabelShort = screenLabel;
      } else {
        this.screenLabelShort = StringUtils.abbreviate(screenLabel, maxWidth);
      }
    }
  }

  /**
   * construct with subject
   * @param subject1
   */
  public GuiSubject(Subject subject1) {
    this.subject = subject1;
  }
  
  /**
   * get screen label
   * @return screen label
   */
  public String getScreenLabel() {
    this.initScreenLabels();
    return this.screenLabelShort;
  }
  
  /**
   * get screen label
   * @return screen label
   */
  public String getScreenLabelLong() {
    this.initScreenLabels();
    return this.screenLabelLong;
  }
  
  /** attributes in string - string format */
  private Map<String, String> attributes = null;
  /**
   * long screen label
   */
  private String screenLabelLong = null;
  /**
   * short screen label
   */
  private String screenLabelShort = null;

  /**
   * subject
   * @return the subject
   */
  public Subject getSubject() {
    return this.subject;
  }
  
  /**
   * Gets a map attribute names and value. The map's key
   * contains the attribute name and the map's value
   * contains a Set of attribute value(s).  Note, this only does single valued attributes
   * @return the map of attributes
   */
  @SuppressWarnings({ "cast", "unchecked" })
  public Map<String, String> getAttributes() {
    if (this.attributes == null) {
      Map<String, String> result = new LinkedHashMap<String, String>();
      for (String key : (Set<String>)(Object)GrouperUtil.nonNull(this.subject.getAttributes()).keySet()) {
        Object value = this.subject.getAttributes().get(key);
        if (value instanceof String) {
          //if a string
          result.put(key, (String)value);
        } else if (value instanceof Set) {
          //if set of one string, then add it
          if (((Set)value).size() == 1) {
            result.put(key, (String)((Set)value).iterator().next());
          } else if (((Set)value).size() > 1) {
            //put commas in between?  not sure what else to do here
            result.put(key, GrouperUtil.setToString((Set)value));
          }
        }
      }
      this.attributes = result;
    }
    return this.attributes;
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
  public boolean isNeedsTooltip() {
    this.initScreenLabels();
    return !StringUtils.equals(this.screenLabelLong, this.screenLabelShort);
  }


  /**
   * 
   * @param subject
   * @param attrName
   * @return the value
   */
  public static String attributeValue(Subject subject, String attrName) {
    if (StringUtils.equalsIgnoreCase("screenLabel", attrName)) {
      return GrouperUiUtils.convertSubjectToLabel(subject);
    }
    if (StringUtils.equalsIgnoreCase("subjectId", attrName)) {
      return subject.getId();
    }
    if (StringUtils.equalsIgnoreCase("name", attrName)) {
      return subject.getName();
    }
    if (StringUtils.equalsIgnoreCase("description", attrName)) {
      return subject.getDescription();
    }
    if (StringUtils.equalsIgnoreCase("typeName", attrName)) {
      return subject.getType().getName();
    }
    if (StringUtils.equalsIgnoreCase("sourceId", attrName)) {
      return subject.getSource().getId();
    }
    if (StringUtils.equalsIgnoreCase("sourceName", attrName)) {
      return subject.getSource().getName();
    }
    //TODO switch this to attribute values comma separated
    return subject.getAttributeValue(attrName);
  }
  
}
