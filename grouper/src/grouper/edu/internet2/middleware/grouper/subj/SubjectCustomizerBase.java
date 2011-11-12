package edu.internet2.middleware.grouper.subj;

import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.permissions.PermissionResult;
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
