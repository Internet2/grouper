/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
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

import  edu.internet2.middleware.grouper.*;
import  java.util.*;

/** 
 * Class for performing subject lookups.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.11 2004-09-19 17:09:05 blair Exp $
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

