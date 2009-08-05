/*
 * @author mchyzer
 * $Id: GuiSubject.java,v 1.3 2009-08-05 06:38:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * subject for gui has all attributes etc
 */
public class GuiSubject {
  
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
  

  /**
   * Gets this Subject's ID.
   * @return the id
   */
  public String getId() {
    return this.subject.getId();
  }

  /**
   * Gets this Subject's type.
   * @return the type string
   */
  public String getTypeString() {
    return this.subject.getType().getName();
  }

  /**
   * Gets this Subject's name.
   * @return the name of subject
   */
  public String getName() {
    return this.subject.getName();
  }

  /**
   * Gets this Subject's description.
   * @return the description
   */
  public String getDescription() {
    return this.subject.getDescription();
  }

  /**
   * Gets a map attribute names and value. The map's key
   * contains the attribute name and the map's value
   * contains a Set of attribute value(s).  Note, this only does single valued attributes
   * @return the map of attributes
   */
  @SuppressWarnings({ "cast", "unchecked" })
  public Map<String, String> getAttributes() {
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
    return null;
  }

  /**
   * Returns the Source of this Subject.
   * @return the name of source
   */
  public String getSourceString() {
    return this.subject.getSource().getId();
  }

  
}
