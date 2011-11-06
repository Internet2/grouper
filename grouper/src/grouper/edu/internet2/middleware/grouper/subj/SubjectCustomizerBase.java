package edu.internet2.middleware.grouper.subj;

import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.membership.GroupMembershipResult;
import edu.internet2.middleware.grouper.membership.PermissionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * extend this to do a subject customizer
 * @author mchyzer
 *
 */
public abstract class SubjectCustomizerBase implements SubjectCustomizer {

  /**
   * calculate memberships in one query
   * @param subjects
   * @param includeGrouperSessionSubject
   * @param groupNames
   * @return results
   */
  public static GroupMembershipResult calculateMemberships(Collection<Subject> subjects, 
      IncludeGrouperSessionSubject includeGrouperSessionSubject,
      Collection<String> groupNames) {
    //TODO implement this
    return null;
  }

  /**
   * calculate memberships in one query
   * @return results
   */
  public static GroupMembershipResult calculateMembershipsInStems(Collection<Subject> subjects, 
      IncludeGrouperSessionSubject includeGrouperSessionSubject,
      Collection<String> groupNames, Collection<String> stemNames) {
    //TODO implement this
    return null;
  }
  
  public static PermissionResult calculatePermissionsInStem(Collection<Subject> subjects,
      IncludeGrouperSessionSubject includeGrouperSessionSubject,
      String stemName, Scope scope) {
    return null;
  }
  
  /**
   * @see SubjectCustomizer#decorateSubjects(GrouperSession, Collection, Collection)
   */
  public Set<Subject> decorateSubjects(GrouperSession grouperSession,
      Set<Subject> subjects, Collection<String> attributeNamesRequested) {
    return subjects;
  }

  /**
   * @see SubjectCustomizer#filterSubjects(GrouperSession, Collection)
   */
  public Set<Subject> filterSubjects(GrouperSession grouperSession, Set<Subject> subjects) {
    return subjects;
  }

  
  
}
