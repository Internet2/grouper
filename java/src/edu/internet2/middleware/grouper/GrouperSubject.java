package edu.internet2.middleware.directory.grouper;

/** 
 * {@link Grouper} Subject Interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.1 2004-04-11 03:13:44 blair Exp $
 */
public interface GrouperSubject {
  public GrouperMember lookup(String subjectID, boolean isMember);
}

