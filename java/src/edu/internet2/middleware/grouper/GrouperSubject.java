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
import  edu.internet2.middleware.subject.*;
import  java.util.*;


/** 
 * Class for performing subject lookups.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.19 2004-11-12 17:55:30 blair Exp $
 */
public class GrouperSubject {

  public GrouperSubject() {
    super();
  }
    

  /*
   * PUBLIC CLASS METHODS 
   */

  /**
   * TODO
   *
   * @param   id      Subject ID
   * @param   typeID  Subject Type ID
   * @return  {@link GrouperSubject} object
   */
  public static Subject lookup(String id, String typeID) {
    Subject     subj  = null;
    // TODO Add static map of adapters and instantiated objects for
    //      each?
    SubjectType st    = Grouper.subjectType(typeID);
    if (st != null) {
      SubjectTypeAdapter sta = st.getAdapter();
      if (sta != null) {
        try {
          subj = sta.getSubject(st, id);
        } catch (SubjectNotFoundException e) {
          // TODO WRONG!
          System.err.println(e);
          System.exit(1);
        }
      }
    }
    return subj;
  }

}

