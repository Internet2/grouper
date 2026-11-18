package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Decides whom a caller may name as the act-as subject of a GSH template run
 * (gshTemplateActAsSubjectLookup in the gshTemplateExec web service).
 *
 * The template's actAsGroupUUID group says WHO may run a template as someone else, not WHOM
 * they may run it as.  Without a limit on the target, a member of that group could name
 * GrouperSystem or a sysadmin, and a runAsType=currentUser template would then run in a root
 * or wheel session.  So: a caller who is not root or a wheel member may not act as root or as a
 * member of the wheel, readonly wheel or viewonly wheel group.  Wheel and root callers may act
 * as anyone (they already have those powers), and acting as yourself is always allowed.
 *
 * Kept in its own small class (not in GshTemplateExec, which differs in almost every release) so
 * the same file works in every release; it only uses PrivilegeHelper and SubjectHelper methods
 * that exist from 2.6 on, and Java 8 syntax.
 * </pre>
 */
public class GshTemplateActAsAuthorization {

  /**
   * no instances
   */
  private GshTemplateActAsAuthorization() {
  }

  /**
   * see if a subject is root, or a member of the wheel, readonly wheel or viewonly wheel group
   * (each wheel group only counts when its groups.wheel*.use setting is on, as everywhere else).
   * Runs as root so the answer does not depend on who can see the wheel groups.
   * @param subject to check
   * @return true if the subject is root or any kind of wheel member
   */
  public static boolean isPrivilegedSubject(final Subject subject) {
    if (subject == null) {
      return false;
    }
    if (PrivilegeHelper.isSystemSubject(subject)) {
      return true;
    }
    return (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        // isWheelOrRootOrViewonlyRoot covers root, wheel, readonly wheel and viewonly wheel
        return PrivilegeHelper.isWheelOrRootOrViewonlyRoot(subject);
      }
    });
  }

  /**
   * see if a subject is root or a member of the (admin) wheel group, as root
   * @param subject to check
   * @return true if root or wheel
   */
  public static boolean isWheelOrRootSubject(final Subject subject) {
    if (subject == null) {
      return false;
    }
    if (PrivilegeHelper.isSystemSubject(subject)) {
      return true;
    }
    return (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return PrivilegeHelper.isWheelOrRoot(subject);
      }
    });
  }

  /**
   * see if the caller may run a template as the target
   * @param caller authenticated subject (already checked to be in the template's act-as group)
   * @param target subject the caller wants to act as
   * @return true if allowed
   */
  public static boolean allowedToActAs(Subject caller, Subject target) {
    if (caller == null) {
      return false;
    }
    // nothing to decide
    if (target == null || SubjectHelper.eq(caller, target)) {
      return true;
    }
    // a wheel or root caller already has every power the target could have
    if (isWheelOrRootSubject(caller)) {
      return true;
    }
    // a non-wheel caller may act as ordinary subjects only
    return !isPrivilegedSubject(target);
  }

  /**
   * throw if the caller may not run a template as the target; call before the target is used for
   * template validation or as the template session subject
   * @param caller authenticated subject
   * @param target subject the caller wants to act as
   */
  public static void assertAllowedToActAs(Subject caller, Subject target) {
    if (!allowedToActAs(caller, target)) {
      throw new RuntimeException((caller == null ? "null" : caller.getId())
          + " is not allowed to run a GSH template as " + (target == null ? "null" : target.getId())
          + ": only root or a wheel member may act as root or a wheel, readonly wheel or viewonly wheel member");
    }
  }

}
