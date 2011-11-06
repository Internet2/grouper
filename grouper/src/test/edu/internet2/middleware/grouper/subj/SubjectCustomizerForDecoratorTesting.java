package edu.internet2.middleware.grouper.subj;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.membership.GroupMembershipResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/**
 * filter students out from people who cant see them
 * @author mchyzer
 *
 */
public class SubjectCustomizerForDecoratorTesting extends SubjectCustomizerBase {

  /** student (protected data) group name */
  private static final String STUDENT_GROUP_NAME = "apps:subjectSecurity:groups:student";
  /** privileged employee group name */
  private static final String PRIVILEGED_EMPLOYEE_GROUP_NAME = "apps:subjectSecurity:groups:privilegedEmployee";

  /** source id we care about */
  private static final String SOURCE_ID = "jdbc";
  
  /**
   * @see SubjectCustomizer#filterSubjects(GrouperSession, Set)
   */
  @Override
  public Set<Subject> filterSubjects(GrouperSession grouperSession, Set<Subject> subjects) {
    
    //nothing to do if no results
    if (GrouperUtil.length(subjects) == 0) {
      return subjects;
    }
    
    //get results in one query
    GroupMembershipResult groupMembershipResult = calculateMemberships(subjects, IncludeGrouperSessionSubject.TRUE, 
        GrouperUtil.toSet(STUDENT_GROUP_NAME,PRIVILEGED_EMPLOYEE_GROUP_NAME));
    
    //see if the user is privileged
    boolean grouperSessionIsPrivileged = groupMembershipResult.hasMembership(PRIVILEGED_EMPLOYEE_GROUP_NAME, grouperSession.getSubject());
    
    //if so, we are done, they can see stuff
    if (grouperSessionIsPrivileged) {
      return subjects;
    }
    
    //loop through the subjects and see which are students, change their name and description to be their netId, with no other attributes
    Set<Subject> results = new LinkedHashSet<Subject>();
    for (Subject subject : subjects) {
      if (StringUtils.equals(SOURCE_ID, subject.getSourceId()) && groupMembershipResult.hasMembership(STUDENT_GROUP_NAME, subject)) {
        String netId = subject.getAttributeValue("netId");
        Subject replacementSubject = new SubjectImpl(subject.getId(), netId, netId, subject.getTypeName(), subject.getSourceId());
        results.add(replacementSubject);
      } else {
        results.add(subject);
      }
    }
    return results;
  }

  
  
}
