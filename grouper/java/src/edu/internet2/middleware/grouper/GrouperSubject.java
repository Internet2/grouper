/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.util.*;

/** 
 * Class for performing subject lookups.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.12 2004-10-05 18:35:54 blair Exp $
 */
public class GrouperSubject {

  /**
   * Query the <i>grouper_member</i> table for a specific member.
   *
   * @param   id    Member ID
   * @param   type  Member Type
   * @return  {@link GrouperMember} object or null.
   */
  public static GrouperMember lookup(String id, String type) {
    return GrouperBackend.member(id, type);
  }

}

