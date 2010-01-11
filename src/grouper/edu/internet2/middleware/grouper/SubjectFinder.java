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
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.subj.SubjectResolver;
import edu.internet2.middleware.grouper.subj.SubjectResolverFactory;
import edu.internet2.middleware.grouper.validator.NotNullValidator;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectTooManyResults;


/**
 * Find I2MI subjects.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SubjectFinder.java,v 1.45 2009-10-30 12:39:26 mchyzer Exp $
 */
public class SubjectFinder {

  /** */
  private static        Subject         all;
  /** */
  private static        Subject         root;
  /** */
  private static        SubjectResolver resolver;
  /** */
  static                Source          gsa;

  /**
   * find by id or identifier
   * @param idOrIdentifier
   * @param exceptionIfNull if SubjectNotFoundException or null
   * @return the subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  public static Subject findByIdOrIdentifier(String idOrIdentifier, boolean exceptionIfNull) 
      throws SubjectNotFoundException, SubjectNotUniqueException {
    Subject subject = null;
    
    //try by id first
    subject = SubjectFinder.findById(idOrIdentifier, false);

    //try by identifier if not by id
    if (subject == null) {
      subject = SubjectFinder.findByIdentifier(idOrIdentifier, false);
    }
    
    //if null at this point, and exception, then throw it
    if (subject == null && exceptionIfNull) {
      throw new SubjectNotFoundException("Cant find subject by id or identifier: '" + idOrIdentifier + "'"); 
    }

    return subject;
  }

  /**
   * Search within all configured sources for subject with identified by <i>id</i>.
   * <pre class="eg">
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
   * @deprecated
   */
  @Deprecated
  public static Subject findById(String id) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException {
    return findById(id, true); 
  } 

  /**
   * Search within all configured sources for subject with identified by <i>id</i>.
   * <pre class="eg">
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
   * @param exceptionIfNull 
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public static Subject findById(String id, boolean exceptionIfNull) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException {
    try {
      return getResolver().find(id);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }
  } 

  /**
   * flush the cache (e.g. for testing)
   */
  public static void flushCache() {
    getResolver().flushCache();
  }
  
  /**
   * Search within all configured sources providing <i>type</i> for subject with identified by <i>id</i>.
   * <pre class="eg">
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
   * @deprecated
   */
  @Deprecated
  public static Subject findById(String id, String type) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    return findById(id, type, true);
  } 

  /**
   * Search for subject by <i>id</i>, <i>type</i> and <i>source</i>.
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
   * @param   source  Subject source.
   * @return  Matching subject.
   * @throws  SourceUnavailableException
   * @throws  SubjectNotFoundException
   * @throws  SubjectNotUniqueException
   * @deprecated
   */
  @Deprecated
  public static Subject findById(String id, String type, String source) 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    return findById(id, type, source, true); 
  } 

  /**
   * Get a subject by a well-known identifier.
   * <pre class="eg">
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
   * @deprecated
   */
  @Deprecated
  public static Subject findByIdentifier(String id) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    return findByIdentifier(id, true);
  } 

  /**
   * Get a subject by a well-known identifier and the specified type.
   * <pre class="eg">
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
   * @deprecated
   */
  @Deprecated
  public static Subject findByIdentifier(String id, String type) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException {
    return findByIdentifier(id, type, true);
  } 

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
   * @deprecated
   */
  @Deprecated
  public static Subject findByIdentifier(String id, String type, String source) 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException {
    return findByIdentifier(id, type, source, true); 
  } 

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
   * @throws SubjectTooManyResults if more results than configured
   */
  public static Set<Subject> findAll(String query) {
    return getResolver().findAll(query);
  } 

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
  public static Set<Subject> findAll(String query, String source)
    throws  SourceUnavailableException
  {
    return getResolver().findAll(query, source);
  } 

  /**
   * Get <i>GrouperAll</i> subject.
   * <pre class="eg">
   * Subject all = SubjectFinder.findAllSubject();
   *  </pre>
   * @return  The <i>GrouperAll</i> {@link Subject} 
   * Get <i>GrouperAll</i> subject.
   * <pre class="eg">
   * Subject all = SubjectFinder.findAllSubject();
   *  </pre>
   * @throws  GrouperException if unable to retrieve <i>GrouperAll</i>.
   * @since   1.1.0
   */
  public static Subject findAllSubject() 
    throws  GrouperException
  {
    if (all == null) {
      try {
        all = getResolver().find( GrouperConfig.ALL, GrouperConfig.IST, InternalSourceAdapter.ID );
      }
      catch (Exception e) {
        throw new GrouperException( "unable to retrieve GrouperAll: " + e.getMessage() );
      }
    }
    return all;
  } 

  /**
   * Get <i>GrouperSystem</i> subject.
   * <pre class="eg">
   * Subject root = SubjectFinder.findRootSubject();
   *  </pre>
   * @return  The <i>GrouperSystem</i> subject.
   * @throws  GrouperException if unable to retrieve <i>GrouperSystem</i>.
   * @since   1.1.0
   */
  public static Subject findRootSubject() 
    throws  GrouperException
  {
    if (root == null) {
      try {
        root = getResolver().find( GrouperConfig.ROOT, GrouperConfig.IST, InternalSourceAdapter.ID );
      }
      catch (Exception e) {
        throw new GrouperException( "unable to retrieve GrouperSystem: " + e.getMessage() );
      }
    }
    return root;
  } 

  /**
   * @return  Singleton {@link SubjectResolver}.
   * @since   1.2.1
   */
  private static SubjectResolver getResolver() {
    if (resolver == null) { 
      resolver = SubjectResolverFactory.getInstance();
    }
    return resolver;
  }

  /**
   * <pre class="eg">
   * try {
   *   Source sa = SubjectFinder.getSource(id);
   * }
   * catch (SourceUnavailableException eSU) {
   *   // unable to retrieve source
   * }
   * </pre>
   * @param id 
   * @return  <i>Source</i> identified by <i>id</i>.
   * @throws  IllegalArgumentException if <i>id</i> is null.
   * @throws  SourceUnavailableException if unable to retrieve source.
   */
  public static Source getSource(String id) 
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    return getResolver().getSource(id);
  } 

  /**
   * <pre class="eg">
   * Set sources = SubjectFinder.getSources();
   * </pre>
   * @return  Set of all {@link Source} adapters.
   */
  public static Set<Source> getSources() {
    return getResolver().getSources();
  }

  /**
   * <pre class="eg">
   * Set personSources = SubjectFinder.getSources("person");
   * </pre>
   * @param subjectType 
   * @return  Set of <i>Source</i> adapters providing <i>subjectType</i>.
   */
  public static Set<Source> getSources(String subjectType) {
    return getResolver().getSources(subjectType);
  }

  /**
   * TODO 20070803 what is the point of this method?
   * @return source
   * @since   1.2.0
   */
  public static Source internal_getGSA() {
    if (gsa == null) {
      for ( Source sa : getResolver().getSources() ) {
        if (sa instanceof GrouperSourceAdapter) {
          gsa = sa;
          break;
        }
      }
      // TODO 20070803 go away.  the exception is wrong as well.
      NotNullValidator v = NotNullValidator.validate(gsa);
      if (v.isInvalid()) {
        throw new IllegalArgumentException(E.SF_GETSA); 
      }
    }
    return gsa;
  } 

  /**
   * Reset <code>SubjectResolver</code>.
   * @since   1.2.1
   */
  public static void reset() {
    resolver = null; // TODO 20070807 this could definitely be improved    
  }

  /**
   * Search within all configured sources providing <i>type</i> for subject with identified by <i>id</i>.
   * <pre class="eg">
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
   * @param exceptionIfNull 
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public static Subject findById(String id, String type, boolean exceptionIfNull) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      return getResolver().find(id, type);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }
  }

  /**
   * Search for subject by <i>id</i>, <i>type</i> and <i>source</i>.
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
   * @param   source  Subject source.
   * @param exceptionIfNull 
   * @return  Matching subject.
   * @throws  SourceUnavailableException
   * @throws  SubjectNotFoundException
   * @throws  SubjectNotUniqueException
   */
  public static Subject findById(String id, String type, String source, boolean exceptionIfNull) 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException {
    try {
      return getResolver().find(id, type, source);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;        
      }
      return null;
    }
  }
 
  /**
   * Get a subject by a well-known identifier.
   * <pre class="eg">
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
   * @param exceptionIfNotFound 
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public static Subject findByIdentifier(String id, boolean exceptionIfNotFound) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      return getResolver().findByIdentifier(id);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNotFound) {
        throw snfe;
      }
      return null;
    }
  }

  /**
   * Get a subject by a well-known identifier and the specified type.
   * <pre class="eg">
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
   * @param exceptionIfNull 
   * @return  A {@link Subject} object
   * @throws  SubjectNotFoundException
   * @throws  SubjectNotUniqueException
   */
  public static Subject findByIdentifier(String id, String type, boolean exceptionIfNull) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      return getResolver().findByIdentifier(id, type);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }
    
  }

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
   * @param exceptionIfNull 
   * @return  A {@link Subject} object
   * @throws  SourceUnavailableException
   * @throws  SubjectNotFoundException
   * @throws  SubjectNotUniqueException
   */
  public static Subject findByIdentifier(String id, String type, String source, boolean exceptionIfNull) 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      return getResolver().findByIdentifier(id, type, source);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }
  }

}

