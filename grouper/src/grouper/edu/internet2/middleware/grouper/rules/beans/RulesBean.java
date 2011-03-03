/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.subject.Subject;


/**
 * base class for rules beans
 */
public abstract class RulesBean {

  /**
   * if has group
   * @return true or false
   */
  public boolean hasGroup() {
    return false;
  }
  
  /**
   * if has attributeDefName
   * @return attributeDefName
   */
  public AttributeDefName getAttributeDefName() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }


  /**
   * if has attributeDefName
   * @return true or false
   */
  public boolean hasAttributeDefName() {
    return false;
  }
  
  /**
   * if has stem
   * @return true or false
   */
  public boolean hasStem() {
    return false;
  }
  
  /**
   * if has attributeDef
   * @return true or false
   */
  public boolean hasAttributeDef() {
    return false;
  }
  
  /**
   * get this group
   * @return this group
   */
  public Group getGroup() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }
  
  /**
   * get this stem
   * @return this stem
   */
  public Stem getStem() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }
  
  /**
   * get this member id
   * @return this member id
   */
  public String getMemberId() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }
  
  /**
   * get this subject
   * @return this subject
   */
  public Subject getSubject() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * get this subject source id
   * @return this subject
   */
  public String getSubjectSourceId() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  /**
   * get this attributeDef
   * @return this attributeDef
   */
  public AttributeDef getAttributeDef() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }
  
}
