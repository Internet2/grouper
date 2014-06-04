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
package edu.internet2.middleware.grouper.subj.decoratorExamples;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.subj.SubjectCustomizer;
import edu.internet2.middleware.grouper.subj.SubjectCustomizerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/**
 * filter students private information out from people who cant see them
 * @author mchyzer
 *
 */
public class SubjectCustomizerForDecoratorTestingHideStudentData extends SubjectCustomizerBase {

  /** student (protected data) group name */
  public static final String STUDENT_GROUP_NAME = "apps:subjectSecurity:groups:student";
  /** privileged employee group name */
  public static final String PRIVILEGED_EMPLOYEE_GROUP_NAME = "apps:subjectSecurity:groups:privilegedEmployee";

  /** source id we care about */
  private static final String SOURCE_ID = "jdbc";
  
  /**
   * @see SubjectCustomizer#filterSubjects(GrouperSession, Set, String)
   */
  @Override
  public Set<Subject> filterSubjects(GrouperSession grouperSession, Set<Subject> subjects, String findSubjectsInStemName) {
    
    //nothing to do if no results
    if (GrouperUtil.length(subjects) == 0) {
      return subjects;
    }
    
    //get results in one query
    MembershipResult groupMembershipResult = new MembershipFinder().assignCheckSecurity(false).addGroup(STUDENT_GROUP_NAME)
        .addGroup(PRIVILEGED_EMPLOYEE_GROUP_NAME).addSubjects(subjects).addSubject(grouperSession.getSubject())
        .findMembershipResult();
      
    //see if the user is privileged
    boolean grouperSessionIsPrivileged = groupMembershipResult.hasGroupMembership(PRIVILEGED_EMPLOYEE_GROUP_NAME, grouperSession.getSubject());
    
    //if so, we are done, they can see stuff
    if (grouperSessionIsPrivileged) {
      return subjects;
    }
    
    //loop through the subjects and see which are students, change their name and description to be their netId, with no other attributes
    Set<Subject> results = new LinkedHashSet<Subject>();
    for (Subject subject : subjects) {
      if (StringUtils.equals(SOURCE_ID, subject.getSourceId()) && groupMembershipResult.hasGroupMembership(STUDENT_GROUP_NAME, subject)) {
        String loginid = subject.getAttributeValue("loginid");
        Subject replacementSubject = new SubjectImpl(subject.getId(), loginid, loginid, subject.getTypeName(), subject.getSourceId());
        results.add(replacementSubject);
      } else {
        results.add(subject);
      }
    }
    return results;
  }
}
