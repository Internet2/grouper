package edu.internet2.middleware.directory.grouper;

/** 
 * Public {@link Grouper} interface for identifying {@link Grouper}
 * subjects.
 * <p>
 * See {@link InternalGrouperSubject} for the default implementation
 * of this interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.2 2004-04-12 04:14:36 blair Exp $
 */
public interface GrouperSubject {
  /**
   * Looks up "subjectID" via the class specified in the
   * "interface.subject" configuration directive.
   * <p>
   * If successful, "subjectID" will be mapped to a {@link GrouperMember} object.
   *
   * @param subjectID The subject that we are attempting to lookup.
   * @return A string representing the memberID of the subject or null.
   */
  public String lookup(String subjectID);

  /**
   * Looks up "subjectID" via the class specified in the
   * "interface.subject" configuration directive.
   * <p>
   * If successful, "subjectID" will be mapped to a {@link GrouperMember} object.
   *
   * @param subjectID The subject that we are attempting to lookup.
   * @param isMember  If true, the subjectID is assumed to be a
   * memberID and not a presentationID.
   * @return A string representing the memberID of the subject or null.
   */
  public String lookup(String subjectID, boolean isMember);
}

