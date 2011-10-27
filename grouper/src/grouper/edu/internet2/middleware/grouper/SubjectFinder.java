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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.entity.EntitySourceAdapter;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.rules.RuleCheck;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.RuleIfCondition;
import edu.internet2.middleware.grouper.rules.RuleIfConditionEnum;
import edu.internet2.middleware.grouper.rules.RuleThen;
import edu.internet2.middleware.grouper.rules.RuleThenEnum;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.subj.SubjectResolver;
import edu.internet2.middleware.grouper.subj.SubjectResolverFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
 * @version $Id: SubjectFinder.java,v 1.47 2009-12-28 06:08:37 mchyzer Exp $
 */
public class SubjectFinder {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(SubjectFinder.class);

  /** */
  private static        Subject         all;
  /** */
  private static        Subject         root;
  /** */
  private static        SubjectResolver resolver;
  /** */
  static                Source          gsa;
  /** */
  static                Source          esa;

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
    try {
      return getResolver().findByIdOrIdentifier(idOrIdentifier);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }
  }

  /**
   * pass in the source (optional), and the id or identifier
   * @param sourceId
   * @param subjectId
   * @param subjectIdentifier
   * @param exceptionIfNotFound
   * @return the subject or null
   */
  public static Subject findByOptionalArgs(String sourceId, String subjectId, String subjectIdentifier, boolean exceptionIfNotFound) {
    if (!StringUtils.isBlank(sourceId)) {
      
      if (!StringUtils.isBlank(subjectId)) {
        return SubjectFinder.findByIdAndSource(subjectId, sourceId, exceptionIfNotFound);
      }
      
      if (!StringUtils.isBlank(subjectIdentifier)) {
        return SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId, exceptionIfNotFound);
      }
      
    }
    //no source
    if (!StringUtils.isBlank(subjectId)) {
      return SubjectFinder.findById(subjectId, exceptionIfNotFound);
    }
    
    if (!StringUtils.isBlank(subjectIdentifier)) {
      return SubjectFinder.findByIdentifier(subjectIdentifier, exceptionIfNotFound);
    }
    
    if (exceptionIfNotFound) {
      throw new RuntimeException("Cant find subject: " + sourceId + ", " + subjectId + ", " + subjectIdentifier);
    }

    return null;

  }

  /**
   * find by id or identifier
   * @param idOrIdentifier
   * @param source 
   * @param exceptionIfNull if SubjectNotFoundException or null
   * @return the subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  public static Subject findByIdOrIdentifierAndSource(String idOrIdentifier, String source, boolean exceptionIfNull) 
      throws SubjectNotFoundException, SubjectNotUniqueException {
    try {
      return getResolver().findByIdOrIdentifier(idOrIdentifier, source);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }
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
   * Search within all configured sources for subject with identified by <i>id</i>.
   * <pre class="eg">
   * try {
   *   Subject subj = SubjectFinder.findByIdAndSource(subjectID, source, true);
   * }
   * catch (SubjectNotFoundException eSNF)  {
   *   // Subject not found
   * }
   * catch (SubjectNotUniqueException eSNU) {
   *   // Subject not unique
   * }
   *  </pre>
   * @param   id      Subject ID
   * @param source is the source to check in
   * @param exceptionIfNull 
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public static Subject findByIdAndSource(String id, String source, boolean exceptionIfNull) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException {

    try {
      return getResolver().find(id, source);
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
   * Find all subjects matching the query, in a certain folder.  If there are
   * rules restricting subjects, then dont search those folders
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
   * @param stemName stem name to search in
   * @param   query     Subject query string.
   * @return  A {@link Set} of {@link Subject} objects.
   * @throws SubjectTooManyResults if more results than configured
   */
  public static Set<Subject> findAllInStem(String stemName, String query) {
    
    return getResolver().findAllInStem(stemName, query);
    
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
   * Find all subjects matching the query within the specified {@link Source}s.
   * <p>
   * <b>NOTE:</b> This method does not perform any caching.
   * </p>
   * <pre class="eg">
   * try {
   *   Set subjects = SubjectFinder.findAll(query, sources);
   * }
   * catch (SourceUnavailableException eSU) {
   *   // unable to query source
   * }
   *  </pre>
   * @param   query   Subject query string.
   * @param   sources  {@link Source} adapters to search.
   * @return  A {@link Set} of {@link Subject}s.
   * @throws  SourceUnavailableException
   */
  public static Set<Subject> findAll(String query, Set<Source> sources)
      throws  SourceUnavailableException {
    if (sources == null || sources.isEmpty()) {
      return findAll(query);
    }
    Set<Subject> results = new LinkedHashSet<Subject>();
    for (Source source: sources) {
      Set<Subject> current = findAll(query, source.getId());
      if (current != null) {
        results.addAll(current);
      }
    }
    return results;
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
        all = getResolver().find( GrouperConfig.ALL, InternalSourceAdapter.ID );
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
        root = getResolver().find( GrouperConfig.ROOT, InternalSourceAdapter.ID );
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
   * @param failIfError 
   * @return source
   * @since   2.1.0
   */
  public static Source internal_getEntitySourceAdapter(boolean failIfError) {
    if (esa == null) {
      for ( Source sa : getResolver().getSources() ) {
        if (sa instanceof EntitySourceAdapter) {
          esa = sa;
          break;
        }
      }
      if (esa == null && failIfError) {
        throw new RuntimeException("No entity source configured in sources.xml, see sources.example.xml for an example");
      }
    }
    return esa;
  } 

  /**
   * Reset <code>SubjectResolver</code>.
   * @since   1.2.1
   */
  public static void reset() {
    resolver = null; // TODO 20070807 this could definitely be improved    
    HibernateSession.bySqlStatic().executeSql("delete from subject where subjectId = 'GrouperSystem'");
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
   * @deprecated since type is no longer an identifier... just use id or id/source
   */
  @Deprecated
  public static Subject findById(String id, String type, boolean exceptionIfNull) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    return findById(id, exceptionIfNull);
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
   * @param   type    Subject type.  If blank dont consider type
   * @param   source  Subject source.
   * @param exceptionIfNull 
   * @return  Matching subject.
   * @throws  SourceUnavailableException
   * @throws  SubjectNotFoundException
   * @throws  SubjectNotUniqueException
   * @Deprecated since type is no longer an id, just use id or id/source
   */
  @Deprecated
  public static Subject findById(String id, String type, String source, boolean exceptionIfNull) 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException {
    
    return findByIdAndSource(id, source, exceptionIfNull);
    
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
   * @deprecated use id or id/source
   */
  @Deprecated
  public static Subject findByIdentifier(String id, String type, boolean exceptionIfNull) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException {
    try {
      return getResolver().findByIdentifier(id);
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
   * @Deprecated
   */
  @Deprecated
  public static Subject findByIdentifier(String id, String type, String source, boolean exceptionIfNull) 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    return findByIdentifierAndSource(id, source, exceptionIfNull);
  }

  /**
   * convert a set of subjects to a set of subject that are in a group
   * @param grouperSession 
   * @param subjects to convert to members
   * @param group that subjects must be in
   * @param field that they must be in in the group (null will default to eh members list
   * @param membershipType that they must be in in the group or null for any
   * @return the subjects in the group (never null)
   */
  public static Set<Subject> findBySubjectsInGroup(GrouperSession grouperSession,
      Set<Subject> subjects, Group group, Field field, MembershipType membershipType) {

    Set<Member> members = MemberFinder.findBySubjectsInGroup(grouperSession, subjects, group, field, membershipType);

    Set<Subject> subjectResults = new LinkedHashSet<Subject>();
    
    //convert members to subjects
    if (GrouperUtil.length(members) != 0) {
      
      for (Member member : members) {
        subjectResults.add(member.getSubject());
      }
      
    }
    return subjectResults;
  }

  /**
   * Get a subject by a well-known identifier, and source.
   * <p>
   * <b>NOTE:</b> This method does not perform any caching.
   * </p>
   * <pre class="eg">
   * try {
   *   Subject subj = SubjectFinder.findByIdentifierAndSource(id, source, true);
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *  </pre>
   * @param   identifier      Well-known identifier.
   * @param   source  {@link Source} adapter to search.
   * @param exceptionIfNull 
   * @return  A {@link Subject} object
   * @throws  SourceUnavailableException
   * @throws  SubjectNotFoundException
   * @throws  SubjectNotUniqueException
   */
  public static Subject findByIdentifierAndSource(String identifier, String source, boolean exceptionIfNull) 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException {
    try {
      return getResolver().findByIdentifier(identifier, source);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }
  }

  /**
   * <pre>
   * Find a subject by packed subject string.  This could be a four colons then subjectId or six colons then a subjectIdentifier, or
   * a source then four colons, then subjectId, or a source then six colons then a subjectIdentifier. 
   * or a subjectIdOrIdentifier, or a source, then eight colons, then a subjectIdentifier e.g.
   * subjectIdOrIdentifier
   * sourceId::::subjectId
   * ::::subjectId
   * sourceId::::::subjectIdentifier
   * ::::::subjectIdentifier
   * sourceId::::::::subjectIdOrIdentifier
   * ::::::::subjectIdOrIdentifier
   * </pre>
   * @param subjectString
   * @param exceptionIfNotFound
   * @return the subject
   */
  public static Subject findByPackedSubjectString(String subjectString, boolean exceptionIfNotFound) {
    if (StringUtils.isBlank(subjectString)) {
      throw new RuntimeException("Why is subjectString blank? ");
    }
    String sourceId = null;
    String subjectIdentifier = null;
    String subjectId = null;
    if (subjectString.contains("::::::::")) {
      String[] subjectParts = GrouperUtil.splitTrim(subjectString, "::::::::");
      
      if (subjectParts.length > 1) {
        sourceId = subjectParts[0];
        String subjectIdOrIdentifier = subjectParts[1];
        return findByIdOrIdentifierAndSource(subjectIdOrIdentifier, sourceId, exceptionIfNotFound);
      }
      String subjectIdOrIdentifier = subjectParts[0];
      return findByIdOrIdentifier(subjectIdOrIdentifier, exceptionIfNotFound);

    } else if (subjectString.contains("::::::")) {
      String[] subjectParts = GrouperUtil.splitTrim(subjectString, "::::::");
      if (subjectParts.length > 1) {
        sourceId = subjectParts[0];
        subjectIdentifier = subjectParts[1];
      } else {
        subjectIdentifier = subjectParts[0];
      }

    } else if (subjectString.contains("::::")) {
      String[] subjectParts = GrouperUtil.splitTrim(subjectString, "::::");
      sourceId = subjectParts[0];
      if (subjectParts.length > 1) {
        sourceId = subjectParts[0];
        subjectId = subjectParts[1];
      } else {
        subjectId = subjectParts[0];
      }
      
    } else {
      return findByIdOrIdentifier(subjectString, exceptionIfNotFound);
    }
    return findByOptionalArgs(sourceId, subjectId, subjectIdentifier, exceptionIfNotFound);
  }
  
  /**
   * result to see if source if restricted by group
   */
  public static class RestrictSourceForGroup {
    
    /** if restricted */
    private boolean restrict;
    
    /** group to restrict to, null means restrict to all */
    private Group group;

    /**
     * @param restrict
     * @param group
     */
    public RestrictSourceForGroup(boolean restrict, Group group) {
      this.restrict = restrict;
      this.group = group;
    }

    /**
     * if restricted
     * @return the restrict
     */
    public boolean isRestrict() {
      return this.restrict;
    }

    
    /**
     * if restricted
     * @param restrict1 the restrict to set
     */
    public void setRestrict(boolean restrict1) {
      this.restrict = restrict1;
    }

    
    /**
     * group to restrict to, null means restrict to all
     * @return the group
     */
    public Group getGroup() {
      return this.group;
    }

    
    /**
     * group to restrict to, null means restrict to all
     * @param group1 the group to set
     */
    public void setGroup(Group group1) {
      this.group = group1;
    }
    
  }

  /**
   * @param sourceId
   * @param stemName
   * @return if restricted and to what extent
   */
  public static RestrictSourceForGroup restrictSourceForGroup(String stemName, String sourceId) {
    
    if (!GrouperConfig.getPropertyBoolean("rules.enable", true)) {
      LOG.debug("rules.enable is false, do not check to see if stem is restricted");
      return new RestrictSourceForGroup(false, null);
    }
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    
    //somtimes root is blank, so convert
    if (StringUtils.isBlank(stemName)) {
      stemName = ":";
    }
    
    if (LOG.isDebugEnabled()) {
      debugMap.put("operation", "restrictSourceForGroup");
      debugMap.put("stemName", stemName);
      debugMap.put("sourceId", sourceId);
    }
    
    //lets see if the source is restricted for this folder
    RuleCheck ruleCheck = new RuleCheck(RuleCheckType.subjectAssignInStem.name(), null, stemName, null, sourceId, null);
    Set<RuleDefinition> ruleDefinitions = RuleEngine.ruleEngine().ruleCheckIndexDefinitionsByNameOrIdInFolderPickOneArgOptional(ruleCheck);

    if (LOG.isDebugEnabled()) {
      debugMap.put("ruleDefinitionsSize", GrouperUtil.length(ruleDefinitions));
    }
    
    if (GrouperUtil.length(ruleDefinitions) == 1) {
      RuleDefinition ruleDefinition = ruleDefinitions.iterator().next();
      final RuleIfCondition ruleIfCondition = ruleDefinition.getIfCondition();
      if (ruleIfCondition != null) {
        RuleIfConditionEnum ruleIfConditionEnum = RuleIfConditionEnum.valueOfIgnoreCase(ruleIfCondition.getIfConditionEnum(), false);
        
        if (LOG.isDebugEnabled()) {
          debugMap.put("ruleIfConditionEnum", ruleIfConditionEnum);
        }
        
        //never means allow all
        if (ruleIfConditionEnum != RuleIfConditionEnum.never) {
          
          RuleThen ruleThen = ruleDefinition.getThen();
          RuleThenEnum ruleThenEnum = ruleThen.thenEnum();
          
          if (LOG.isDebugEnabled()) {
            debugMap.put("ruleIfConditionEnum", ruleThenEnum);
            debugMap.put("ruleIfOwnerName", ruleIfCondition.getIfOwnerName());
            debugMap.put("ruleIfOwnerId", ruleIfCondition.getIfOwnerId());
          }
          //if veto, this is the right type of rule
          if (ruleThenEnum == RuleThenEnum.veto) {
            if (StringUtils.isBlank(ruleIfCondition.getIfOwnerId()) 
                && StringUtils.isBlank(ruleIfCondition.getIfOwnerName())) {
              if (LOG.isDebugEnabled()) {
                debugMap.put("restrict all", Boolean.TRUE);
                LOG.debug(GrouperUtil.mapToString(debugMap));
              }
              return new RestrictSourceForGroup(true, null);
            } 
              
            Group group = null;
            if (!StringUtils.isBlank(ruleIfCondition.getIfOwnerId())) {
              group = GrouperDAOFactory.getFactory().getGroup().findByUuid(ruleIfCondition.getIfOwnerId(), true);
            } else {
              group = GrouperDAOFactory.getFactory().getGroup().findByName(ruleIfCondition.getIfOwnerName(), true);
            }
            if (LOG.isDebugEnabled()) {
              debugMap.put("restrict group", group.getName());
              LOG.debug(GrouperUtil.mapToString(debugMap));
            }

            return new RestrictSourceForGroup(true, group);
          }
        }
      }
    }
    if (LOG.isDebugEnabled()) {
      debugMap.put("restrict none", Boolean.TRUE);
      LOG.debug(GrouperUtil.mapToString(debugMap));
    }
    return new RestrictSourceForGroup(false, null);
  }

}

