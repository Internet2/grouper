/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
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
   * convert sources to id's comma separated
   * @param sources
   * @return the string or null if none
   */
  public static String sourcesToIdsString(Collection<Source> sources) {
    if (GrouperUtil.length(sources) == 0) {
      return null;
    }
    
    StringBuilder result = new StringBuilder();
    
    int index = 0;
    
    for (Source source : sources) {
      
      if (index > 0) {
        result.append(",");
      }
      
      result.append(source.getId());
      index++;
    }
    return result.toString();
  }

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
   * sort subjects with best on top
   */
  private static class SubjectSorterBean implements Comparable<SubjectSorterBean> {
    
    /** subject */
    private Subject subject = null;
    
    /** how many attribute matches there are */
    private int attributeMatches = 0;
    
    /** if the id or identifier match */
    private boolean subjectIdOrIdentifierMatches = false;

    /** keep the original order */
    private int order = 0;

    /**
     * subject
     * @return the subject
     */
    public Subject getSubject() {
      return this.subject;
    }

    
    /**
     * subject
     * @param subject1 the subject to set
     */
    public void setSubject(Subject subject1) {
      this.subject = subject1;
    }

    
    /**
     * how many attribute matches there are
     * @return the attributeMatches
     */
    public int getAttributeMatches() {
      return this.attributeMatches;
    }

    
    /**
     * how many attribute matches there are
     * @param attributeMatches1 the attributeMatches to set
     */
    public void setAttributeMatches(int attributeMatches1) {
      this.attributeMatches = attributeMatches1;
    }

    
    /**
     * if the id or identifier match
     * @return the subjectIdOrIdentifierMatches
     */
    public boolean isSubjectIdOrIdentifierMatches() {
      return this.subjectIdOrIdentifierMatches;
    }

    
    /**
     * if the id or identifier match
     * @param subjectIdOrIdentifierMatches the subjectIdOrIdentifierMatches to set
     */
    public void setSubjectIdOrIdentifierMatches(boolean subjectIdOrIdentifierMatches) {
      this.subjectIdOrIdentifierMatches = subjectIdOrIdentifierMatches;
    }

    
    /**
     * keep the original order
     * @return the order
     */
    public int getOrder() {
      return this.order;
    }

    
    /**
     * keep the original order
     * @param order the order to set
     */
    public void setOrder(int order) {
      this.order = order;
    }


    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(SubjectSorterBean other) {
      
      //if id or identifier, that is better
      if (this.isSubjectIdOrIdentifierMatches() != other.isSubjectIdOrIdentifierMatches()) {
        return this.isSubjectIdOrIdentifierMatches() ? -1 : 1;
      }
      
      //if different number of attribute matches use that
      if (this.getAttributeMatches() != other.getAttributeMatches()) {
        return this.getAttributeMatches() > other.getAttributeMatches() ? -1 : 1;
      }
      
      //go back to original order
      if (this.getOrder() < other.getOrder()) {
        return -1;
      }
      if (this.getOrder() > other.getOrder()) {
        return 1;
      }
      return 0;
    }
    
    
    
  }
  
  /**
   * sort a set of subjects for a search, match id's and identifiers at top
   * @param subjectsIn
   * @param searchTerm
   * @return the set with close matches at top
   */
  public static Set<Subject> sortSetForSearch(Collection<Subject> subjectsIn, String searchTerm) {
    return sortSetForSearch(subjectsIn, searchTerm, null);
  }
  
  /**
   * sort a set of subjects for a search, match id's and identifiers at top
   * @param subjectsIn
   * @param searchTerm
   * @param idOrIdentifierMatches null if not known, but if you know of some, pass that in here
   * @return the set with close matches at top
   */
  public static Set<Subject> sortSetForSearch(Collection<Subject> subjectsIn, String searchTerm, Set<Subject> idOrIdentifierMatches) {
    
    //if there is no search term and no idOrIdentifierMatches, then not much to do
    if (subjectsIn == null || (StringUtils.isBlank(searchTerm) && GrouperUtil.length(idOrIdentifierMatches) == 0)) {
      return null;
    }
    
    List<SubjectSorterBean> subjectSorterBeans = new ArrayList<SubjectSorterBean>(subjectsIn.size());
    int index=0;
    for (Subject subject : subjectsIn) {
      
      SubjectSorterBean subjectSorterBean = new SubjectSorterBean();
      subjectSorterBean.setSubject(subject);
      subjectSorterBeans.add(subjectSorterBean);
      subjectSorterBean.setOrder(index++);
      //see if we know the id or identifier matches
      if (inList(idOrIdentifierMatches, subject) || (!StringUtils.isBlank(searchTerm) && StringUtils.equals(searchTerm, subject.getId()))) {
        subjectSorterBean.setSubjectIdOrIdentifierMatches(true);
      }
      
      Map<String, Set<String>> attributes = subject.getAttributes();
      Object valuesObject = attributes == null ? null : attributes.values();
      int matches = 0;
      if (valuesObject instanceof Collection) {
        Collection values = (Collection)valuesObject;
        
        //do a case insensitive match to be more accurate
        for (Object attributeValues: values) {
          if (attributeValues instanceof Set) {
            Set<String> attributeValuesSet = (Set<String>)attributeValues;
            if (attributeValuesSet != null) {
              for (String value : attributeValuesSet) {
                if (!StringUtils.isBlank(searchTerm) && StringUtils.equalsIgnoreCase(value, searchTerm)) {
                  matches++;
                }
              }
            }
          } else if (attributeValues instanceof String) {
            if (!StringUtils.isBlank(searchTerm) && StringUtils.equalsIgnoreCase(searchTerm, (String)attributeValues)) {
              matches++;
            }
          }
        }
      }
      subjectSorterBean.setAttributeMatches(matches);
    }
    
    //now we need to filter out too many partial matches...
    int maxMatches = 0;
    for (SubjectSorterBean subjectSorterBean : subjectSorterBeans) {
      maxMatches = Math.max(subjectSorterBean.getAttributeMatches(), maxMatches);
    }

    if (maxMatches > 0) {
      
      int[] matchHistogram = new int[maxMatches+1];
      for (SubjectSorterBean subjectSorterBean : subjectSorterBeans) {
        matchHistogram[subjectSorterBean.getAttributeMatches()]++;
      }
      int totalAttributeMatches = 0;
      int attributeMatchesToRemove = -1;
      for (int i=maxMatches; i>0; i--) {
        totalAttributeMatches += matchHistogram[i];
        //too many to put at top
        if (totalAttributeMatches > 5) {
          attributeMatchesToRemove = i;
          break;
        }
      }
      if (attributeMatchesToRemove > 0) {
        for (SubjectSorterBean subjectSorterBean : subjectSorterBeans) {
          if (subjectSorterBean.getAttributeMatches() <= attributeMatchesToRemove) {
            subjectSorterBean.setAttributeMatches(0);
          }
        }
      }
    }
    
    //sort them
    Collections.sort(subjectSorterBeans);

    Set<Subject> subjectsOut = new LinkedHashSet<Subject>(subjectsIn.size());


    for (SubjectSorterBean subjectSorterBean : subjectSorterBeans) {
      //add the rest
      subjectsOut.add(subjectSorterBean.getSubject());
    }
    
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
   * @param a 
   * @param b 
   * @return  True if both objects are <code>Source</code>s and equal.
   * @since   2.0.2
   */
  public static boolean eqSource(Object a, Object b) {
    if (a==b) {
      return true;
    }
    if ( (a == null) || (b == null) ) {
      return false;
    }
    if ( !(a instanceof Source) ) {
      return false;
    }
    if ( !(b instanceof Source) ) {
      return false;
    }
    return StringUtils.equals(((Source)a).getId(), ((Source)b).getId());
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
    Subject subject = findInList(collection, sourceId, subjectId, false);
    return subject != null;
  }
  
  /**
   * see if a subject is in a list, if so return it
   * @param collection
   * @param sourceId 
   * @param subjectId 
   * @param exceptionIfNotFound true if an exception should be thrown if not found
   * @return subject or null if not found or exception
   */
  public static Subject findInList(Collection<Subject> collection, String sourceId, String subjectId, boolean exceptionIfNotFound) {
    
    Subject subject = null;
    
    if (collection != null) {
      for (Subject current : collection) {
        if (StringUtils.equals(current.getSourceId(), sourceId)
            && StringUtils.equals(current.getId(), subjectId)) {
          subject = current;
          break;
        }
      }
    }
    
    if (subject != null || !exceptionIfNotFound) {
      return subject;
    }
    
    throw new RuntimeException("Cant find subject in list: '" + sourceId + "', '" + subjectId + "', list size: " + GrouperUtil.length(collection) );
  }

  /**
   * see if a source is in a list
   * @param collection
   * @param source
   * @return true if in list
   */
  public static boolean inSourceList(Collection<Source> collection, Source source) {
    if (collection == null) {
      return false;
    }
    for (Source current : collection) {
      if (eqSource(current, source)) {
        return true;
      }
    }
    return false;
  }
  
} // class SubjectHelper
 
