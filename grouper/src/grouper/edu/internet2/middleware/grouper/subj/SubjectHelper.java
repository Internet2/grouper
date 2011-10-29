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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/**
 * {@link Subject} utility helper class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SubjectHelper.java,v 1.7 2009-12-28 06:08:37 mchyzer Exp $
 */
public class SubjectHelper {

  /** */
  private static final String SUBJECT_DELIM = "/";

  /**
   * if keeping the subjects in a map where the subject is the key, this
   * multikey will identify the subject
   * @param subject
   * @return the multikey of source id and subject id
   */
  public static MultiKey convertToMultiKey(Subject subject) {
    MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId());
    return multiKey;
  }
  
  /**
   * sort a set or list by subject description
   * @param subjects
   */
  public static void sortByDescription(Collection<Subject> subjects) {
    if (subjects == null) {
      return;
    }
    if (!(subjects instanceof List<?>) && !(subjects instanceof LinkedHashSet<?>)) {
      throw new RuntimeException("expecting LinkedHashSet or List: " + subjects.getClass().getName());
    }
    
    List<Subject> subjectList = subjects instanceof List<?> ? (List<Subject>)subjects : new ArrayList<Subject>(subjects);
    
    Collections.sort(subjectList, new Comparator<Subject>() {

      public int compare(Subject subject1, Subject subject2) {
        return StringUtils.defaultString(subject1.getDescription()).compareTo(StringUtils.defaultString(subject2.getDescription()));
      }
    });
    
    if (subjects instanceof Set<?>) {
      subjects.clear();
      subjects.addAll(subjectList);
    }
    
  }
  
  /**
   * sort a set of subjects for a search, match id's and identifiers at top
   * @param subjectsIn
   * @param searchTerm
   * @return the set with close matches at top
   */
  public static Set<Subject> sortSetForSearch(Set<Subject> subjectsIn, String searchTerm) {
    
    if (subjectsIn == null) {
      return null;
    }
    Set<Subject> subjectsOut = new LinkedHashSet<Subject>(subjectsIn.size());
    //look for subjectId's
    Iterator<Subject> iterator = subjectsIn.iterator();
    while (iterator.hasNext()) {
      Subject subject = iterator.next();
      if (StringUtils.equals(searchTerm, subject.getId())) {
        subjectsOut.add(subject);
        iterator.remove();
      }
    }
    
    //look for any attribute
    iterator = subjectsIn.iterator();
    Set<Subject> subjectsInExtra = new LinkedHashSet<Subject>();
    while (iterator.hasNext()) {
      Subject subject = iterator.next();
      Map<String, Set<String>> attributes = subject.getAttributes();
      Object valuesObject = attributes == null ? null : attributes.values();
      boolean foundMatch = false;
      if (valuesObject instanceof Collection) {
        Collection values = (Collection)valuesObject;
        
        //do a case insensitive match to be more accurate
        for (Object attributeValues: values) {
          if (attributeValues instanceof Set) {
            Set<String> attributeValuesSet = (Set<String>)attributeValues;
            if (attributeValuesSet != null) {
              for (String value : attributeValuesSet) {
                if (StringUtils.equalsIgnoreCase(value, searchTerm)) {
                  foundMatch = true;
                }
              }
            }
          } else if (attributeValues instanceof String) {
            if (StringUtils.equalsIgnoreCase(searchTerm, (String)attributeValues)) {
              foundMatch = true;
            }
          }
        }
      }
      //lets not remove from result set... 
      if (foundMatch) {
        subjectsOut.add(subject);
      } else {
        subjectsInExtra.add(subject);
      }
    }
    
    //add the rest
    subjectsOut.addAll(subjectsInExtra);
    return subjectsOut;
    
  }

  /**
   * @param a 
   * @param b 
   * @return  True if both objects are <code>Subject</code>s and equal.
   * @since   1.2.1
   */
  public static boolean eq(Object a, Object b) {
    if (a==b) {
      return true;
    }
    if ( (a == null) || (b == null) ) {
      return false;
    }
    if ( !(a instanceof Subject) ) {
      return false;
    }
    if ( !(b instanceof Subject) ) {
      return false;
    }
    return SubjectImpl.equalsStatic((Subject)a, b);
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
   * 
   * @param subj
   * @return string
   */
  public static String getPrettyComplete(Subject subj) {
    
    StringBuilder result = new StringBuilder(subj.getId()
            + SUBJECT_DELIM
            + subj.getType().getName() 
            + SUBJECT_DELIM
            + subj.getSource().getId()
            + SUBJECT_DELIM + "name: "
            + subj.getName()
            + SUBJECT_DELIM + "desc: "
            + subj.getDescription());
    Map<String, Set<String>> attributes = subj.getAttributes();
    if (attributes != null) {
      for (String key : attributes.keySet()) {
        Set<String> set = attributes.get(key);
        result.append(SUBJECT_DELIM).append(key).append(": ");
        for (String value : set) {
          result.append(value).append(", ");
        }
        //remove last comma
        result.delete(result.length()-2, result.length());
      }
    }
    
    return result.toString();
  
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
  
  /**
   * see if a subject is in a list
   * @param collection
   * @param sourceId 
   * @param subjectId 
   * @return true if in list
   */
  public static boolean inList(Collection<Subject> collection, String sourceId, String subjectId) {
    if (collection == null) {
      return false;
    }
    for (Subject current : collection) {
      if (StringUtils.equals(current.getSourceId(), sourceId)
          && StringUtils.equals(current.getId(), subjectId)) {
        return true;
      }
    }
    return false;
  }
  
} // class SubjectHelper
 
