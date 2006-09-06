/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;

/**
 * {@link Subject} utility helper class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SubjectHelper.java,v 1.12 2006-09-06 19:50:21 blair Exp $
 */
class SubjectHelper {

  // PROTECTED CLASS METHODS //

  protected static boolean eq(Subject a, Subject b) {
    if ( (a == null) || (b == null) ) {
      return false;
    }
    if 
    (
      a.getId().equals(b.getId())
      && a.getType().equals(b.getType())
      && a.getSource().equals(b.getSource())
    )
    {
      return true;
    }
    return false;
  } // protected static boolean eq(a, b)

  protected static String getPretty(Subject subj) {
    String pretty = subj.getId();
    if (subj.getType().equals(SubjectTypeEnum.valueOf("group"))) {
      pretty = subj.getName();
    }
    return U.q(pretty) + "/" + U.q(subj.getType().getName()) + "/" + U.q(subj.getSource().getId());
  } // protected static String getPretty(subj)

  // Protected Class Constants
  public static final String BAD_SUBJ_ID   = "i do not exist";

} // class SubjectHelper
 
