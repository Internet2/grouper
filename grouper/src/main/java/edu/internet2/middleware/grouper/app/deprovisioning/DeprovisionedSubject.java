/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class DeprovisionedSubject {

  /**
   * 
   * @param deprovisionedSubjects
   * @return the subjects
   */
  public static Set<Subject> retrieveSubjectsFromDeprovisionedSubject(Collection<DeprovisionedSubject> deprovisionedSubjects) {
    Set<Subject> result = new HashSet<Subject>();
    for (DeprovisionedSubject deprovisionedSubject : GrouperUtil.nonNull(deprovisionedSubjects)) {
      result.add(deprovisionedSubject.getSubject());
    }
    return result;
  }
  
  /**
   * 
   */
  public DeprovisionedSubject() {
  }

  /** affiliations */
  private Set<String> affiliations;

  
  /**
   * @return the affiliations
   */
  public Set<String> getAffiliations() {
    return this.affiliations;
  }

  
  /**
   * @param affiliations1 the affiliations to set
   */
  public void setAffiliations(Set<String> affiliations1) {
    this.affiliations = affiliations1;
  }
  
  /** subject */
  private Subject subject;
  
  /**
   * @return the subject
   */
  public Subject getSubject() {
    return this.subject;
  }
  
  /**
   * @param subject1 the subject to set
   */
  public void setSubject(Subject subject1) {
    this.subject = subject1;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (other == null || (!(other instanceof DeprovisionedSubject))) {
      return false;
    }
    DeprovisionedSubject otherDeprovisionedSubject = (DeprovisionedSubject)other;
    if (this.subject == otherDeprovisionedSubject.subject) {
      return true;
    }
    if (this.subject == null) {
      return false;
    }
    return this.subject.equals(otherDeprovisionedSubject.subject);
  }


  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  
}
