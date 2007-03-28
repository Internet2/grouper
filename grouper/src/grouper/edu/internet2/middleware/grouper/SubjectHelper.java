/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

/**
 * {@link Subject} utility helper class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SubjectHelper.java,v 1.19 2007-03-28 18:12:12 blair Exp $
 */
class SubjectHelper {

  // PRIVATE CLASS CONSTANTS //
  private static final String SUBJECT_DELIM = "/";


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static boolean eq(Object a, Object b) {
    if ( (a == null) || (b == null) ) {
      return false;
    }
    if ( !(a instanceof Subject) ) {
      return false;
    }
    if ( !(b instanceof Subject) ) {
      return false;
    }
    Subject subjA = (Subject) a;
    Subject subjB = (Subject) b;
    if (
         subjA.getId().equals( subjB.getId() )
      && subjA.getSource().getId().equals( subjB.getSource().getId() )
      && subjA.getType().getName().equals( subjB.getType().getName() )
    )
    {
      return true;
    }
    return false;
  } // protected static boolean eq(a, b)
 
  // @since   1.2.0
  protected static String getPretty(MemberDTO _m) {
    return  U.internal_q( _m.getSubjectId() ) // don't bother grabbing the name.  names aren't consistent, after all.
            + SUBJECT_DELIM
            + U.internal_q( _m.getSubjectTypeId() ) 
            + SUBJECT_DELIM
            + U.internal_q( _m.getSubjectSourceId() );
  } // protected static String getPretty(_m)
 
  // @since   1.2.0
  protected static String getPretty(Subject subj) {
    return  U.internal_q( subj.getId() )
            + SUBJECT_DELIM
            + U.internal_q( subj.getType().getName() ) 
            + SUBJECT_DELIM
            + U.internal_q( subj.getSource().getId() );
  } // protected static String getPretty(subj)

} // class SubjectHelper
 
