package edu.internet2.middleware.directory.grouper;

/** 
 * Public {@link Grouper} interface for identifying {@link Grouper}
 * subjects.
 * <p>
 * See {@link InternalGrouperSubject} for the default implementation
 * of this interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.4 2004-04-29 05:20:59 blair Exp $
 */
public interface GrouperSubject {
  /**
   * Looks up "subject" via the class specified in the
   * "interface.subject" configuration directive.
   * <p>
   * If successful, "subject" will be mapped to a {@link GrouperMember} object.
   *
   * @param   subject The subject that we are attempting to lookup.
   * @return  A {@link GrouperMember} object.
   */
  public GrouperMember lookup(String subject);

  /**
   * Looks up "subject" via the class specified in the
   * "interface.subject" configuration directive.
   * <p>
   * If successful, "subject" will be mapped to a {@link GrouperMember} object.
   *
   * @param   subject The subject that we are attempting to lookup.
   * @param   isMember  If true, the subject is assumed to be a
   * memberID and not a presentationID.
   * @return  A {@link GrouperMember} object.
   */
  public GrouperMember lookup(String subject, boolean isMember);
}

