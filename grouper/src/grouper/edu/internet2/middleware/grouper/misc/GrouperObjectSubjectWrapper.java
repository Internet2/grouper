/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.misc;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * implement GrouperObject for subjects
 * @author mchyzer
 *
 */
public class GrouperObjectSubjectWrapper implements GrouperObject {

  
  
  /**
   * subject
   */
  private Subject subject;
  
  /**
   * 
   * @param subject
   */
  public GrouperObjectSubjectWrapper(Subject subject) {
    super();
    this.subject = subject;
  }

  /**
   * 
   */
  public GrouperObjectSubjectWrapper() {
    super();
  }

  /**
   * subject
   * @return subject
   */
  public Subject getSubject() {
    return this.subject;
  }

  /**
   * subject
   * @param subject1
   */
  public void setSubject(Subject subject1) {
    this.subject = subject1;
  }

  /**
   * description
   */
  @Override
  public String getDescription() {
    return this.subject.getDescription();
  }

  /**
   * display name
   */
  @Override
  public String getDisplayName() {
    return this.subject.getName();
  }

  /**
   * name
   */
  @Override
  public String getName() {
    return this.subject.getName();
  }
  
  /**
   * @see GrouperObject#matchesLowerSearchStrings(Set)
   */
  @Override
  public boolean matchesLowerSearchStrings(Set<String> filterStrings) {

    if (GrouperUtil.length(filterStrings) == 0) {
      return true;
    }

    String lowerId = this.subject.getId().toLowerCase();
    String lowerName = StringUtils.defaultString(this.subject.getName()).toLowerCase();
    String lowerDescription = StringUtils.defaultString(this.subject.getDescription()).toLowerCase();
    
    for (String filterString : GrouperUtil.nonNull(filterStrings)) {
      
      //if all dont match, return false
      if (!lowerId.contains(filterString)
          && !lowerName.contains(filterString)
          && !lowerDescription.contains(filterString)) {
        return false;
      }
      
    }
    return true;
  }

  /**
   * @see GrouperObject#getId
   */
  @Override
  public String getId() {
    if (this.subject == null) {
      return null;
    }
    return this.subject.getSourceId() + "||||" + this.subject.getId();
  }

}
