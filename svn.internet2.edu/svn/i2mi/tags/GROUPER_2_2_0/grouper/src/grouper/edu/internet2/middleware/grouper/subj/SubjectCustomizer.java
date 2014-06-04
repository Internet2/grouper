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
/**
 * 
 */
package edu.internet2.middleware.grouper.subj;

import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.subject.Subject;


/**
 * add the ability to decorate a list of subjects with more attributes.
 * note, while you are decorating, you can check security to see if the
 * groupersession is allowed to see those attributes
 * @author mchyzer
 *
 */
public interface SubjectCustomizer {

  /**
   * decorate subjects based on attributes requested
   * @param grouperSession
   * @param subjects
   * @param attributeNamesRequested
   * @return the subjects if same set, or make a new set
   */
  public Set<Subject> decorateSubjects(GrouperSession grouperSession, Set<Subject> subjects, Collection<String> attributeNamesRequested);
  
  /**
   * you can edit the subjects (or replace), but you shouldnt remove them
   * @param grouperSession
   * @param subjects
   * @param findSubjectsInStemName if this is a findSubjectsInStem call, this is the stem name.  This is useful
   * to filter when searching for subjects to add to a certain group
   * @return the subjects if same set, or make a new set
   */
  public Set<Subject> filterSubjects(GrouperSession grouperSession, Set<Subject> subjects, String findSubjectsInStemName);
 
  
}
