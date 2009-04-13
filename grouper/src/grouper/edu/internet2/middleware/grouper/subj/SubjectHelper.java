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

package edu.internet2.middleware.grouper.subj;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.subject.Subject;

/**
 * {@link Subject} utility helper class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SubjectHelper.java,v 1.3 2009-04-13 16:53:08 mchyzer Exp $
 */
public class SubjectHelper {

  /** */
  private static final String SUBJECT_DELIM = "/";


  /**
   * @param a 
   * @param b 
   * @return  True if both objects are <code>Subject</code>s and equal.
   * @since   1.2.1
   */
  public static boolean eq(Object a, Object b) {
    // TODO 20070816 add tests
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
  } 

  /**
   * 
   * @param _m
   * @return string
   */
  public static String getPretty(Member _m) {
    
    return  Quote.single( _m.getSubjectId() ) // don't bother grabbing the name.  names aren't consistent, after all.
            + SUBJECT_DELIM
            + Quote.single( _m.getSubjectTypeId() ) 
            + SUBJECT_DELIM
            + Quote.single( _m.getSubjectSourceId() );
  } // protected static String getPretty(_m)

  /**
   * 
   * @param subj
   * @return string
   */
  public static String getPretty(Subject subj) {
    if (subj instanceof LazySubject) {
      return subj.toString();
    }
    return  Quote.single( subj.getId() )
            + SUBJECT_DELIM
            + Quote.single( subj.getType().getName() ) 
            + SUBJECT_DELIM
            + Quote.single( subj.getSource().getId() );
  }

  
  /**
   * remove duplicates from a set
   * @param subjects
   */
  public static void removeDuplicates(Collection<Subject> subjects) {
    if (subjects == null) {
      return;
    }
    Set<Subject> tempList = new HashSet<Subject>();
    Iterator<Subject> iterator = subjects.iterator();
    while(iterator.hasNext()) {
      Subject subject = iterator.next();

      //see if in tempList
      if (inList(tempList, subject)) {
        iterator.remove();
      } else {
        //if not, keep track
        tempList.add(subject);
      }
    }
  }

  /**
   * see if a subject is in a list
   * @param collection
   * @param subject
   * @return true if in list
   */
  public static boolean inList(Collection<Subject> collection, Subject subject) {
    if (collection == null) {
      return false;
    }
    for (Subject current : collection) {
      if (eq(current, subject)) {
        return true;
      }
    }
    return false;
  }
  
} // class SubjectHelper
 
