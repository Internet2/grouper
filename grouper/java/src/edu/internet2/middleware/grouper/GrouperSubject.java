/* 
 * Copyright (C) 2004 TODO
 * All Rights Reserved. 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted only as authorized by the Academic
 * Free License version 2.1.
 *
 * A copy of this license is available in the file LICENSE in the
 * top-level directory of the distribution or, alternatively, at
 * <http://www.opensource.org/licenses/afl-2.1.php>
 */

package edu.internet2.middleware.grouper;

/** 
 * {@link Grouper} subject lookup interface.
 * <p>
 * See {@link InternalGrouperSubject} for the default implementation
 * of this interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.8 2004-08-24 17:37:58 blair Exp $
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

