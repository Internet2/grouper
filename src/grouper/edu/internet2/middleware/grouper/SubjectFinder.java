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
import  java.util.*;
import  org.apache.commons.lang.time.*;

/**
 * Find I2MI subjects.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SubjectFinder.java,v 1.24 2006-07-10 15:18:34 blair Exp $
 */
public class SubjectFinder {

  // PRIVATE CLASS CONSTANTS //
  private static final SourceManager  MGR;
  private static final Subject        ALL; 


  // PROTECTED CLASS VARIABLES //
  protected static Source gsa;


  // STATIC //
  static {
    DebugLog.info(SubjectFinder.class, "Initializing source manager");
    try {
      MGR = SourceManager.getInstance();
      DebugLog.info(SubjectFinder.class, "Source manager initialized: " + MGR);
      // Add in internal source adapter
      BaseSourceAdapter isa = new InternalSourceAdapter(
        InternalSourceAdapter.ID, InternalSourceAdapter.NAME
      ); 
      MGR.loadSource(isa);
      DebugLog.info(SubjectFinder.class, "Added source: " + isa.getId());
      // Add in group source adapter
      DebugLog.info(SubjectFinder.class, "Subject finder initialized");
      try {
        ALL = SubjectFinder.findById(GrouperConfig.ALL, GrouperConfig.IST, InternalSourceAdapter.ID);
        DebugLog.info(SubjectFinder.class, "ALL subject initialized");
      }
      catch (SubjectNotFoundException eSNF) {
        String msg = E.SF_IAS + eSNF.getMessage();
        ErrorLog.fatal(SubjectFinder.class, msg);
        throw new GrouperRuntimeException(msg, eSNF);
      }
    } 
    catch (Exception e) {
      String msg = E.SF_INIT + e.getMessage();
      ErrorLog.fatal(SubjectFinder.class, msg);
      throw new GrouperRuntimeException(msg, e);
    }
  } // static


  // PUBLIC CLASS METHODS //

  /**
   * Get a subject by id.
   * <pre class="eg">
   * // Find the subject - within all sources - with the specified id.
   * try {
   *   Subject subj = SubjectFinder.findById(subjectID);
   * }
   * catch (SubjectNotFoundException eSNF)  {
   *   // Subject not found
   * }
   * catch (SubjectNotUniqueException eSNU) {
   *   // Subject not unique
   * }
   *  </pre>
   * @param   id      Subject ID
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public static Subject findById(String id) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    List subjects  = SubjectFinder._findById(
      id, MGR.getSources().iterator()
    );
    if (subjects.size() == 1) {
      return (Subject) subjects.get(0);
    }
    else if (subjects.size() > 1) {
      throw new SubjectNotUniqueException(E.SF_SNU + id);
    }
    throw new SubjectNotFoundException(E.SF_SNF + id);
  } // public static Subject findById(id)

  /**
   * Get a subject by id and the specified type.
   * <pre class="eg">
   * // Find the subject - within all sources - with the specified id
   * // and type.
   * try {
   *   Subject subj = SubjectFinder.findById(subjectID, type);
   * }
   * catch (SubjectNotFoundException eSNF)  {
   *   // Subject not found
   * }
   * catch (SubjectNotUniqueException eSNU) {
   *   // Subject not unique
   * }
   *  </pre>
   * @param   id      Subject ID
   * @param   type    Subject type.
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public static Subject findById(String id, String type) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = SubjectCache.getCache(SubjectCache.ID).get(id, type);
    if (subj != null) {
      return subj;
    }
    List subjects  = SubjectFinder._findById(
      id, MGR.getSources(SubjectTypeEnum.valueOf(type)).iterator()
    );
    if (subjects.size() == 1) {
      return (Subject) subjects.get(0);
    }
    else if (subjects.size() > 1) {
      throw new SubjectNotUniqueException(E.SF_SNU + id + "," + type); 
    }
    throw new SubjectNotFoundException(E.SF_SNF + id + "," + type);
  } // public static Subject findById(id, type)

  /**
   * Get a subject by id, type and source.
   * <p>
   * <b>NOTE:</b> This method does not perform any caching.
   * </p>
   * <pre class="eg">
   * try {
   *   Subject subj = SubjectFinder.findById(id, type, source);
   * }
   * catch (SourceUnavailableException eSU) {
   *   // unable to query source
   * }
   * catch (SubjectNotFoundException eSNF) {
   *   // subject not found
   * }
   *  </pre>
   * @param   id      Subject ID
   * @param   type    Subject type.
   * @param   source  {@link Source} adapter to search.
   * @return  A {@link Subject} object
   * @throws  SourceUnavailableException
   * @throws  SubjectNotFoundException
   * @throws  SubjectNotUniqueException
   */
  public static Subject findById(String id, String type, String source) 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    // FIXME Caching support
    Source  sa    = getSource(source);
    Subject subj  = sa.getSubject(id);
    if (subj.getType().getName().equals(type)) {
      return subj;
    }
    throw new SubjectNotFoundException(E.SF_SNF + id + "," + type);
  } // public static Subject findById(id, type, source)

  /**
   * Get a subject by a well-known identifier.
   * <pre class="eg">
   * // Find the subject - within all sources - with the well-known
   * // identifier.
   * try {
   *   Subject subj = SubjectFinder.findByIdentifier(identifier);
   * }
   * catch (SubjectNotFoundException eSNF)  {
   *   // Subject not found
   * }
   * catch (SubjectNotUniqueException eSNU) {
   *   // Subject not unique
   * }
   *  </pre>
   * @param   id      Subject identifier.
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public static Subject findByIdentifier(String id) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    List subjects  = SubjectFinder._findByIdentifier(
      id, MGR.getSources().iterator()
    );
    if (subjects.size() == 1) {
      return (Subject) subjects.get(0);
    }
    else if (subjects.size() > 1) {
      throw new SubjectNotUniqueException(E.SF_SNU + id);
    }
    throw new SubjectNotFoundException(E.SF_SNF + id);
  } // public static Subject findByIdentifier(id)

  /**
   * Get a subject by a well-known identifier and the specified type.
   * <pre class="eg">
   * // Find the subject - within all sources - with the specified id
   * // and type.
   * try {
   *   Subject subj = SubjectFinder.findByIdentifier(identifier, type);
   * }
   * catch (SubjectNotFoundException eSNF)  {
   *   // subject not found
   * }
   * catch (SubjectNotUniqueException eSNU) {
   *   // subject not found
   * }
   *  </pre>
   * @param   id      Subject identifier.
   * @param   type    Subject type.
   * @return  A {@link Subject} object
   * @throws  SubjectNotFoundException
   * @throws  SubjectNotUniqueException
   */
  public static Subject findByIdentifier(String id, String type) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = SubjectCache.getCache(SubjectCache.IDFR).get(id, type);
    if (subj != null) {
      return subj;
    }
    List subjects  = SubjectFinder._findByIdentifier(
      id, MGR.getSources(SubjectTypeEnum.valueOf(type)).iterator()
    );
    if (subjects.size() == 1) {
      return (Subject) subjects.get(0);
    }
    else if (subjects.size() > 1) {
      throw new SubjectNotUniqueException(E.SF_SNU + id + "," + type); 
    }
    throw new SubjectNotFoundException(E.SF_SNF + id + "," + type);
  } // public static Subject findByIdentifier(id, type)

  /**
   * Get a subject by a well-known identifier, type and source.
   * <p>
   * <b>NOTE:</b> This method does not perform any caching.
   * </p>
   * <pre class="eg">
   * try {
   *   Subject subj = SubjectFinder.findByIdentifier(id, type, source);
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *  </pre>
   * @param   id      Well-known identifier.
   * @param   type    Subject type.
   * @param   source  {@link Source} adapter to search.
   * @return  A {@link Subject} object
   * @throws  SourceUnavailableException
   * @throws  SubjectNotFoundException
   * @throws  SubjectNotUniqueException
   */
  public static Subject findByIdentifier(String id, String type, String source) 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    // FIXME Caching support
    Source  sa    = getSource(source);
    Subject subj  = sa.getSubjectByIdentifier(id);
    if (subj.getType().getName().equals(type)) {
      return subj;
    }
    throw new SubjectNotFoundException(E.SF_SNF + id + "," + type);
  } // public static Subject findByIdentifier(id, type, source)

  /**
   * Find all subjects matching the query.
   * <p>
   * The query string specification is currently unique to each subject
   * source adapter.  Queries may not work or may lead to erratic
   * results across different source adapters.  Consult the
   * documentation for each source adapter for more information on the
   * query language supported by each adapter.
   * </p>
   * <p>
   * <b>NOTE:</b> This method does not perform any caching.
   * </p>
   * <pre class="eg">
   * // Find all subjects matching the given query string.
   * Set subjects = SubjectFinder.findAll(query);
   * </pre>
   * @param   query     Subject query string.
   * @return  A {@link Set} of {@link Subject} objects.
   */
  public static Set findAll(String query) {
    Set       subjects  = new LinkedHashSet();
    Source    sa;
    Iterator  iter      = MGR.getSources().iterator();
    while (iter.hasNext()) {
      sa = (Source) iter.next();
      Set found = sa.search(query);
      DebugLog.info(SubjectFinder.class, "Found subjects in " + sa.getId() + ": " + found.size());
      subjects.addAll(found);
    }
    return subjects;
  } // public static Set findAll(query)

  /**
   * Find all subjects matching the query within the specified {@link Source}.
   * <p>
   * <b>NOTE:</b> This method does not perform any caching.
   * </p>
   * <pre class="eg">
   * try {
   *   Set subjects = SubjectFinder.findAll(query, source);
   * }
   * catch (SourceUnavailableException eSU) {
   *   // unable to query source
   * }
   *  </pre>
   * @param   query   Subject query string.r.
   * @param   source  {@link Source} adapter to search.
   * @return  A {@link Set} of {@link Subject}s.
   * @throws  SourceUnavailableException
   */
  public static Set findAll(String query, String source)
    throws  SourceUnavailableException
  {
    // FIXME Caching support
    Source sa = getSource(source);
    return sa.search(query);
  } // public static Set findAll(query, source)

  /**
   * Get <i>GrouperAll</i> subject.
   * <pre class="eg">
   * Subject all = SubjectFinder.findAllSubject();
   *  </pre>
   * @return  The <i>GrouperAll</i> {@link Subject} 
   */
  public static Subject findAllSubject() {
    return ALL;
  } // public static Subject findAllSubject()

  /**
   * Get {@link Source} for specified by id.
   * <pre class="eg">
   * try {
   *   Source sa = SubjectFinder.getSource(id);
   * }
   * catch (SourceUnavailableException eSU) {
   *   // unable to retrieve source
   * }
   * </pre>
   * @param   id  Name of source to retrieve.
   * @return  {@link Source} adapter.
   * @throws  SourceUnavailableException
   */
  public static Source getSource(String id) 
    throws  SourceUnavailableException
  {
    return MGR.getSource(id);  
  } // public static Source getSource(id)

  /**
   * Get all sources.
   * <pre class="eg">
   * Set sources = SubjectFinder.getSources();
   * </pre>
   * @return  {@link Set} of configured {@link Source} adapters.
   */
  public static Set getSources() {
    return new LinkedHashSet( MGR.getSources() );
  } // public static Set getSources()

  /**
   * Get all sources that support the specified subject type.
   * <pre class="eg">
   * Set personSources = SubjectFinder.getSources("person");
   * </pre>
   * @param   type  Find {@link Source} adapters that support this type.
   * @return  {@link Set} of configured {@link Source} adapters.
   */
  public static Set getSources(String type) {
    return new LinkedHashSet( 
      MGR.getSources( SubjectTypeEnum.valueOf(type) ) 
    );
  } // public static Set getSources(type)



  // PROTECTED CLASS METHODS //

  // @since   1.0
  protected static Source getGSA() {
    if (gsa == null) {
      Iterator iter = MGR.getSources().iterator();
      while (iter.hasNext()) {
        Source sa = (Source) iter.next();
        if (sa instanceof GrouperSourceAdapter) {
          gsa = sa;
          break;
        }
      }
      Validator.valueNotNull(gsa, E.SF_GETSA);
    }
    return gsa;
  } // protected static Source getGSA()


  // PRIVATE CLASS METHODS //
  private static List _findById(String id, Iterator iter) {
    Subject subj      = null;
    Source  sa;
    List    subjects  = new ArrayList();
    while (iter.hasNext()) {
      sa = (Source) iter.next();
      try {
        subj = sa.getSubject(id);
        DebugLog.info(SubjectFinder.class, "Found subject in " + sa.getId() + ": " + id);
        SubjectCache.getCache(SubjectCache.ID).put(subj);
        subjects.add(subj);
      }
      catch (SubjectNotFoundException eSNF)   {
        DebugLog.info(SubjectFinder.class, "Subject not found in " + sa.getId() + ": " + id);
      }
      catch (SubjectNotUniqueException eSNU)  {
        DebugLog.info(SubjectFinder.class, "Subject not found in " + sa.getId() + ": " + id);
      }
    }
    return subjects;
  } // private static List _findById(id, iter) 

  private static List _findByIdentifier(String id, Iterator iter) {
    Subject subj      = null;
    List    subjects  = new ArrayList();
    Source  sa;
    while (iter.hasNext()) {
      sa = (Source) iter.next();
      try {
        subj = sa.getSubjectByIdentifier(id);
        DebugLog.info(SubjectFinder.class, "Found subject in " + sa.getId() + ": " + id);
        SubjectCache.getCache(SubjectCache.IDFR).put(subj);
        subjects.add(subj);
      }
      catch (SubjectNotFoundException e) {
        DebugLog.info(SubjectFinder.class, "Subject not found in " + sa.getId() + ": " + id);
      }
      catch (SubjectNotUniqueException e) {
        DebugLog.info(SubjectFinder.class, "Subject not unique in " + sa.getId() + ": " + id);
      }
    }
    return subjects;
  } // private static List _findByIdentifier(id, iter) 

}

