package edu.internet2.middleware.grouper;

/** 
 * {@link Grouper} subject lookup interface.
 * <p>
 * See {@link InternalGrouperSubject} for the default implementation
 * of this interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.7 2004-08-06 15:19:51 blair Exp $
 */
public interface GrouperSubject {

  /**
   * Looks up "subject" via the class specified in the
   * "interface.subject" configuration directive.
   * <p>
   * If successful, "subject" will be mapped to a {@link GrouperMember} object.
   *
   * @param   subjectID   A <i>memberID</i> or <i>presentationID</i>
   * @return  {@link GrouperMember} object representing
   *   <i>subjectID</i>.
   */
  public GrouperMember lookup(String subjectID);

  /**
   * Looks up "subject" via the class specified in the
   * "interface.subject" configuration directive.
   * <p>
   * If successful, "subject" will be mapped to a {@link GrouperMember} object.
   *
   * @param   subjectID   A <i>memberID</i> or <i>presentationID</i>
   * @param   isMember    True if <i>subjectID</i> is a
   *   <i>memberID</i>, false if it is a <i>presentationID</i>.
   * @return  {@link GrouperMember} object representing
   *   <i>subjectID</i>.
   */
  public GrouperMember lookup(String subjectID, boolean isMember);

}

