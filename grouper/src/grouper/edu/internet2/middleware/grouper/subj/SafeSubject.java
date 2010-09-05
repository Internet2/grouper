/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.subject.Subject;


/**
 * bean to wrap a subject so it can be safely used from EL.  Dont return any real objects, just
 * primitives and strings and safe stuff
 */
public class SafeSubject {

  /**
   * get the email address of this subject based on attributes in grouper.properties
   * @return the email address
   */
  public String getEmailAddress() {
    String emailAttributeName = null;
    GrouperEmailUtils.GrouperSubjectAttributeBean grouperSubjectAttributeBean = GrouperEmailUtils.subjectSourceAttributes(this.subject.getSourceId());
    if (grouperSubjectAttributeBean != null) {
      emailAttributeName = grouperSubjectAttributeBean.getEmailAttributeName();
    }
    if (!StringUtils.isBlank(emailAttributeName)) {
      return this.subject.getAttributeValue(emailAttributeName);
    }
    return null;
  }
  
  /**
   * construct with a subject
   * @param theSubject
   */
  public SafeSubject(Subject theSubject) {
    if (theSubject == null) {
      throw new NullPointerException();
    }
    this.subject = theSubject;
  }
  
  /** subject */
  private Subject subject;

  
  /**
   * Gets this Subject's ID.
   * @return string
   */
  public String getId() {
    return this.subject.getId();
  }

  /**
   * get the type name
   * @return the type name
   */
  public String getTypeName() {
    return this.subject.getTypeName();
  }
  
  /**
   * get the source id of a subject
   * @return the source id
   */
  public String getSourceId() {
    return this.subject.getSourceId();
  }
  
  /**
   * Gets this Subject's name.
   * @return name or null if not there
   */
  public String getName() {
    return this.subject.getName();
  }

  /**
   * Gets this Subject's description.
   * @return description or null if not there
   */
  public String getDescription() {
    return this.subject.getDescription();
  }

  /**
   * Returns the value of a single-valued attribute.
   * If multivalued, this returns the first value
   * @param attributeName 
   * @return value or null if not found
   */
  public String getAttributeValue(String attributeName) {
    return this.subject.getAttributeValue(attributeName);
  }
  
  /**
   * <pre>
   * Returns the attribute value if single-valued, or
   * if multi-valued, returns the values comma separated (with a space too).
   * So if the values are: a b c; this would return the string: "a, b, c"
   * Implementors can use the static helper in SubjectImpl
   * </pre>
   * @param attributeName
   * @return value or values or null if not there
   */
  public String getAttributeValueOrCommaSeparated(String attributeName) {
    return this.subject.getAttributeValueOrCommaSeparated(attributeName);
  }
  
}
