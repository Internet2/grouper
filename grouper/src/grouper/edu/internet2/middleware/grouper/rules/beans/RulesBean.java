/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.subject.Subject;


/**
 * base class for rules beans
 */
public abstract class RulesBean {

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
   * get this attributeDef
   * @return this attributeDef
   */
  public AttributeDef getAttributeDef() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }
  
}
