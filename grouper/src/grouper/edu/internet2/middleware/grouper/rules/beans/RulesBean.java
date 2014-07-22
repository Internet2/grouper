/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
   * subject of the original grouper session (or null if none)
   */
  private Subject subjectUnderlyingSession;
  
  /**
   * subject of the original grouper session (or null if none)
   * @return the subject
   */
  public Subject getSubjectUnderlyingSession() {
    return this.subjectUnderlyingSession;
  }

  /**
   * subject of the original grouper session (or null if none)
   * @param subjectOriginalSession1
   */
  public void setSubjectUnderlyingSession(Subject subjectOriginalSession1) {
    this.subjectUnderlyingSession = subjectOriginalSession1;
  }

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
