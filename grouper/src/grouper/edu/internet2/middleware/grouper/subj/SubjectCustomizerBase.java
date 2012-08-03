/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.subj;

import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.subject.Subject;

/**
 * extend this to do a subject customizer
 * @author mchyzer
 *
 */
public abstract class SubjectCustomizerBase implements SubjectCustomizer {

  /**
   * @see SubjectCustomizer#decorateSubjects(GrouperSession, Collection, Collection)
   */
  public Set<Subject> decorateSubjects(GrouperSession grouperSession,
      Set<Subject> subjects, Collection<String> attributeNamesRequested) {
    return subjects;
  }

  /**
   * @see SubjectCustomizer#filterSubjects(GrouperSession, Collection, String)
   */
  public Set<Subject> filterSubjects(GrouperSession grouperSession, Set<Subject> subjects, String findSubjectsInStemName) {
    return subjects;
  }

  
  
}
