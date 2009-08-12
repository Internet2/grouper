/*
 * @author mchyzer
 * $Id: GuiSubject.java,v 1.5 2009-08-12 05:20:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * subject for gui has all attributes etc
 */
public class GuiSubject implements Serializable {
  
  /** subject */
  private Subject subject;
  
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
    return GuiUtils.convertSubjectToLabel(this.subject);
  }
  
  /** attributes in string - string format */
  private Map<String, String> attributes = null;

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
      for (String key : (Set<String>)(Object)GrouperUtil.nonNull(this.subject.getAttributes().keySet())) {
        Object value = this.subject.getAttributes().get(key);
        if (value instanceof String) {
          //if a string
          result.put(key, (String)value);
        } else if (value instanceof Set) {
          //if set of one string, then add it
          if (((Set)value).size() == 1) {
            result.put(key, (String)((Set)value).iterator().next());
          }
        }
      }
      this.attributes = result;
    }
    return this.attributes;
  }

  
}
