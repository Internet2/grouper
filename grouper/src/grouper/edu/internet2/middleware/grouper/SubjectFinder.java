/**
 * Copyright 2014 Internet2
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
 */
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.entity.EntitySourceAdapter;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SessionException;
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
import edu.internet2.middleware.grouper.subj.SubjectBean;
import edu.internet2.middleware.grouper.subj.SubjectCustomizer;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.subj.SubjectResolver;
import edu.internet2.middleware.grouper.subj.SubjectResolverFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.NotNullValidator;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.SearchPageResult;
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

  /**
   * subject id to search for
   */
  private String subjectId;
  
  /**
   * assign a subjectId to search for
   * @param theSubjectId
   * @return this for chaining
   */
  public SubjectFinder assignSubjectId(String theSubjectId) {
    this.subjectId = theSubjectId;
    return this;
  }
  
  /**
   * source id to search for
   */
  private String sourceId;

  /**
   * assign the source id to search in
   * @param theSourceId
   * @return this for chaining
   */
  public SubjectFinder assignSourceId(String theSourceId) {
    this.sourceId = theSourceId;
    return this;
  }
  
  /**
   * subject identifier to search for
   */
  private String subjectIdentifier;
  
  /**
   * assign a subject identifier to search for
   * @param theSubjectIdentifier1
   * @return this for chaining
   */
  public SubjectFinder assignSubjectIdentifier(String theSubjectIdentifier1) {
    this.subjectIdentifier = theSubjectIdentifier1;
    return this;
  }
  
  /**
   * subjectIdOrIdentifier to search for
   */
  private String subjectIdOrIdentifier;

  /**
   * assign subject id or identifier to search for
   * @param theSubjectIdOrIdentifier
   * @return this for chaining
   */
  public SubjectFinder assignSubjectIdOrIdentifier(String theSubjectIdOrIdentifier) {
    this.subjectIdOrIdentifier = theSubjectIdOrIdentifier;
    return this;
  }
  
  /**
   * memberId of the subject to search for
   */
  private String memberId;
  
  /**
   * assign a member id to search for
   * @param theMemberId
   * @return this for chaining
   */
  public SubjectFinder assignMemberId(String theMemberId) {
    this.memberId = theMemberId;
    return this;
  }

  /**
   * if there should be an exception if not found
   */
  private boolean exceptionIfNotFound;
  
  /**
   * if there should be an exception if not found on one subject to query
   * @param theExceptionIfNotFound
   * @return this for chaining
   */
  public SubjectFinder assignExceptionIfNotFound(boolean theExceptionIfNotFound) {
    this.exceptionIfNotFound = theExceptionIfNotFound;
    return this;
  }
  
  public Subject findSubject() {
    
    //can query by subjectId, subjectIdentifier, or subjectIdOrIdentifier
    int countSelectionCritieria = 0;
    
    if (!StringUtils.isBlank(this.subjectId)) {
      countSelectionCritieria++;
    }

    if (!StringUtils.isBlank(this.subjectIdentifier)) {
      countSelectionCritieria++;
    }

    if (!StringUtils.isBlank(this.subjectIdOrIdentifier)) {
      countSelectionCritieria++;
    }

    if (!StringUtils.isBlank(this.memberId)) {
      countSelectionCritieria++;
    }
    
    if (countSelectionCritieria != 1) {
      throw new RuntimeException("You need to pass in 1 criteria, either id, identifier, idOrIdentifier, or memberId: '" + this.subjectId + "', '"
          + this.subjectIdentifier + "', '" + this.subjectIdOrIdentifier +  "', '" + this.memberId + "'");
    }

    if (!StringUtils.isBlank(this.subjectId)) {
      if (!StringUtils.isBlank(this.sourceId)) {
        return SubjectFinder.findByIdAndSource(this.subjectId, this.sourceId, this.exceptionIfNotFound);
      }
      return SubjectFinder.findById(this.subjectId, this.exceptionIfNotFound);
    }
    
    if (!StringUtils.isBlank(this.subjectIdentifier)) {
      if (!StringUtils.isBlank(this.sourceId)) {
        return SubjectFinder.findByIdentifierAndSource(this.subjectIdentifier, this.sourceId, this.exceptionIfNotFound);
      }
      return SubjectFinder.findByIdentifier(this.subjectId, this.exceptionIfNotFound);
    }
    
    if (!StringUtils.isBlank(this.subjectIdOrIdentifier)) {
      if (!StringUtils.isBlank(this.sourceId)) {
        return SubjectFinder.findByIdOrIdentifierAndSource(this.subjectIdOrIdentifier, this.sourceId, this.exceptionIfNotFound);
      }
      return SubjectFinder.findByIdOrIdentifier(this.subjectIdOrIdentifier, this.exceptionIfNotFound);
    }

    if (!StringUtils.isBlank(this.memberId)) {
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), this.memberId, this.exceptionIfNotFound);
      if (!StringUtils.isBlank(this.sourceId)) {
        if (member != null && !StringUtils.equals(member.getSubjectSourceId(), this.sourceId)) {
          throw new RuntimeException("Member id: " + this.memberId + ", which is source: " + member.getSubjectSourceId()
              + ", and subjectId: " + member.getSubjectId() + ", but was queried with source id: " + this.sourceId);
        }
      }
      
      //this seems weird, why would a memberId not be found???
      if (member == null) {
        return null;
      }
      return member.getSubject();
    }

    throw new RuntimeException("Why are we here?");
  }
  
  /** */
  private static GrouperSession  rootSession;

  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  /**
   * @return session
   */
  public static GrouperSession grouperSessionOrRootForSubjectFinder() {
    //If we have a thread local session then let's use it to ensure 
    //proper VIEW privilege enforcement
    GrouperSession activeSession = GrouperSession.staticGrouperSession(false);
    if(activeSession !=null) {
      return activeSession;
    }
    if (rootSession == null) {
      try {
        //dont replace the currently active session
        rootSession = GrouperSession.start( SubjectFinder.findRootSubject(), false );
      }
      catch (SessionException eS) {
        throw new GrouperException(E.S_NOSTARTROOT + eS.getMessage(), eS);
      }
    }
    return rootSession;
  } // private GrouperSession _getSession()

  /** if we should use threads when doing searches (if grouper.properties allows), this must be used in a try/finally */
  private static ThreadLocal<Boolean> useThreads = new ThreadLocal<Boolean>();

  /**
   * if we should use threads when doing searches (if grouper.properties allows), this must be used in a try/finally
   * @param ifUseThreads
   */
  public static void useThreads(boolean ifUseThreads) {
    useThreads.set(ifUseThreads);
  }

  /**
   * if we should use threads when doing searches (if grouper.properties allows)
   * @return isUseThreadsBasedOnThreadLocal
   */
  public static boolean isUseThreadsBasedOnThreadLocal() {
    Boolean isUseThreads = useThreads.get();
    return GrouperUtil.booleanValue(isUseThreads, true);
  }
  
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
   * find by id or identifier in certain sources
   * @param idOrIdentifier
   * @param sources
   * @param exceptionIfNull if SubjectNotFoundException or null
   * @return the subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  public static Subject findByIdOrIdentifierAndSource(String idOrIdentifier, Set<Source> sources, boolean exceptionIfNull) 
      throws SubjectNotFoundException, SubjectNotUniqueException {

    Subject subject = null;
    for (Source source : GrouperUtil.nonNull(sources)) {

      Subject tempSubject = findByIdOrIdentifierAndSource(idOrIdentifier, source.getId(), false);
      
      //found multiple, thats not good
      if (subject != null && tempSubject != null) {
        throw new RuntimeException("Found multiple subjects in " + subject.getSourceId() + " and " + tempSubject.getSourceId());
      }
      
      //found one, thats good
      if (subject == null && tempSubject != null) {
        subject = tempSubject;
        //dont break...
      }
      
    }
    
    //didnt find one and expecting one
    if (exceptionIfNull && subject == null) {
      throw new SubjectNotFoundException("Cant find subject by id or identifier and sources: '" 
          + idOrIdentifier + "', " + SubjectHelper.sourcesToIdsString(sources));
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
   * find by subject beans
   * @param subjectBeans
   * @return the subjects
   */
  public static Map<SubjectBean, Subject> findBySubjectBeans(Collection<SubjectBean> subjectBeans) {
    
    if (subjectBeans == null) {
      return null;
    }
    
    Map<SubjectBean, Subject> result = new HashMap<SubjectBean, Subject>();
    
    Map<String, Set<String>> mapOfSourceToSubjectIds = new HashMap<String, Set<String>>();
    Map<String, Set<SubjectBean>> mapOfSourceToSubjectBeans = new HashMap<String, Set<SubjectBean>>();
    
    //separated out by source
    for (SubjectBean subjectBean : subjectBeans) {
      
      Set<String> subjectIds = mapOfSourceToSubjectIds.get(subjectBean.getSourceId());
      Set<SubjectBean> subjectBeanSet = mapOfSourceToSubjectBeans.get(subjectBean.getSourceId());
      if (subjectIds == null) {
        
        subjectIds = new HashSet<String>();
        subjectBeanSet = new HashSet<SubjectBean>();
        mapOfSourceToSubjectIds.put(subjectBean.getSourceId(), subjectIds);
        mapOfSourceToSubjectBeans.put(subjectBean.getSourceId(), subjectBeanSet);
        
      }
      
      subjectIds.add(subjectBean.getId());
      subjectBeanSet.add(subjectBean);
    }
    
    //loop through sources and get results
    for (String sourceId : mapOfSourceToSubjectIds.keySet()) {
      Set<String> subjectIds = mapOfSourceToSubjectIds.get(sourceId);
      Set<SubjectBean> subjectBeanSet = mapOfSourceToSubjectBeans.get(sourceId);
      Map<String, Subject> subjectsForSource = GrouperUtil.nonNull(findByIds(subjectIds, sourceId));
      for (SubjectBean subjectBean : subjectBeanSet) {
        
        Subject subject = subjectsForSource.get(subjectBean.getId());
        if (subject != null) {
          result.put(subjectBean, subject);
        }
        
      }
    }
    return result;
    
  }
  
  /**
   * find subjects by ids
   * @param ids
   * @return the map of id to subject.  If a subject is not found, it will
   * not be in the result
   */
  public static Map<String, Subject> findByIds(Collection<String> ids) {
    return getResolver().findByIds(ids);
  }
  
  /**
   * find subjects by idsOrIdentifiers
   * @param idsOrIdentifiers
   * @return the map of id or identifier to subject.  If a subject is not found, it will
   * not be in the result
   */
  public static Map<String, Subject> findByIdsOrIdentifiers(Collection<String> idsOrIdentifiers) {
    return getResolver().findByIdsOrIdentifiers(idsOrIdentifiers);
  }
  
  /**
   * find subjects by idsOrIdentifiers
   * @param idsOrIdentifiers
   * @param source
   * @return the map of id or identifier to subject.  If a subject is not found, it will
   * not be in the result
   */
  public static Map<String, Subject> findByIdsOrIdentifiers(Collection<String> idsOrIdentifiers, String source) {
    return getResolver().findByIdsOrIdentifiers(idsOrIdentifiers, source);
  }
  
  /**
   * find subjects by identifiers
   * @param identifiers
   * @return the map of identifier to subject.  If a subject is not found, it will
   * not be in the result
   */
  public static Map<String, Subject> findByIdentifiers(Collection<String> identifiers) {
    return getResolver().findByIdentifiers(identifiers);
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
    return getResolver().findAll(query, sources);
    
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
        throw new GrouperException( "unable to retrieve GrouperAll: " + e.getMessage(), e );
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
        throw new GrouperException( "unable to retrieve GrouperSystem: " + e.getMessage(), e );
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
      NotNullValidator v = NotNullValidator.validate(gsa);
      if (v.isInvalid()) {
        throw new RuntimeException("Why cant we find the group source adapter???");
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
    resolver = null;  
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
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("rules.enable", true)) {
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

  /**
   * Find a page of subjects matching the query.
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
   * SearchPageResult subjects = SubjectFinder.findPage(query);
   * </pre>
   * @param   query     Subject query string.
   * @return  A {@link Set} of {@link Subject} objects and if there are too many.
   * @throws SubjectTooManyResults if more results than configured
   */
  public static SearchPageResult findPage(String query) {
    return getResolver().findPage(query);
  }

  /**
   * Find a page of subjects matching the query within the specified {@link Source}s.
   * <p>
   * <b>NOTE:</b> This method does not perform any caching.
   * </p>
   * <pre class="eg">
   * try {
   *   SearchPageResult subjects = SubjectFinder.findPage(query, sources);
   * }
   * catch (SourceUnavailableException eSU) {
   *   // unable to query source
   * }
   *  </pre>
   * @param   query   Subject query string.
   * @param   sources  {@link Source} adapters to search.
   * @return  A {@link Set} of {@link Subject}s and if there are too many.
   * @throws  SourceUnavailableException
   */
  public static SearchPageResult findPage(String query, Set<Source> sources)
      throws  SourceUnavailableException {

    if (sources == null || sources.isEmpty()) {
      return findPage(query);
    }
    return getResolver().findPage(query, sources);

  }

  /**
   * Find a page of subjects matching the query within the specified {@link Source}.
   * <p>
   * <b>NOTE:</b> This method does not perform any caching.
   * </p>
   * <pre class="eg">
   * try {
   *   Set subjects = SubjectFinder.findPage(query, source);
   * }
   * catch (SourceUnavailableException eSU) {
   *   // unable to query source
   * }
   *  </pre>
   * @param   query   Subject query string.r.
   * @param   source  {@link Source} adapter to search.
   * @return  A {@link Set} of {@link Subject}s and if too many.
   * @throws  SourceUnavailableException
   */
  public static SearchPageResult findPage(String query, String source)
    throws  SourceUnavailableException
  {
    return getResolver().findPage(query, source);
  }

  /**
   * Find a page of subjects matching the query, in a certain folder.  If there are
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
  public static SearchPageResult findPageInStem(String stemName, String query) {
    
    return getResolver().findPageInStem(stemName, query);
    
  }

  /**
   * Find a page of subjects matching the query, in a certain folder.  If there are
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
  public static SearchPageResult findPageInStem(String stemName, String query, Set<Source> sources) {
    
    return getResolver().findPageInStem(stemName, query, sources);
    
  }

  /**
   * find subjects by identifiers
   * @param identifiers
   * @param source
   * @return the map of identifier to subject.  If a subject is not found, it will
   * not be in the result
   */
  public static Map<String, Subject> findByIdentifiers(Collection<String> identifiers, String source) {
    return getResolver().findByIdentifiers(identifiers, source);
  }

  /**
   * find subjects by ids
   * @param ids
   * @param source
   * @return the map of id to subject.  If a subject is not found, it will
   * not be in the result
   */
  public static Map<String, Subject> findByIds(Collection<String> ids, String source) {
    return getResolver().findByIds(ids, source);
  }

  /**
   * hold a customizer
   * @author mchyzer
   *
   */
  private static class SubjectCustomizerCacheBean {
    
    /** an instance of it */
    private SubjectCustomizer subjectCustomizer;

    /**
     * an instance of it
     * @return the instance
     */
    public SubjectCustomizer getSubjectCustomizer() {
      return this.subjectCustomizer;
    }

    /**
     * an instance of it
     * @param subjectCustomizer1
     */
    public void setSubjectCustomizer(SubjectCustomizer subjectCustomizer1) {
      this.subjectCustomizer = subjectCustomizer1;
    }
    
  }
  
  /**
   * cache the instance so we dont have to keep looking it up
   */
  private static ExpirableCache<Boolean, SubjectCustomizerCacheBean> subjectCustomizerClassCache = new ExpirableCache<Boolean, SubjectCustomizerCacheBean>(5);

  /**
   * decorate subjects based on subject customizer in grouper.properties
   * @param grouperSession
   * @param subjects
   * @param attributeNamesRequested 
   */
  public static void decorateSubjects(GrouperSession grouperSession, Set<Subject> subjects, Collection<String> attributeNamesRequested) {
    
    SubjectCustomizer subjectCustomizer = subjectCustomizer();
    if (subjectCustomizer != null) {
      subjectCustomizer.decorateSubjects(grouperSession, subjects, attributeNamesRequested );
    }    
  }

  
  /**
   * filter subjects based on subject customizer in grouper.properties
   * @param grouperSession
   * @param subjectMap is map os subject id to subject
   * @param filterSubjectsInStemName 
   * @param attributeNamesRequested 
   * @return subject map
   */
  public static Map<String,Subject> filterSubjects(GrouperSession grouperSession, Map<String,Subject> subjectMap, String filterSubjectsInStemName) {
    
    //if nothing to do
    if (GrouperUtil.length(subjectMap) == 0) {
      return subjectMap;
    }
    
    SubjectCustomizer subjectCustomizer = subjectCustomizer();
    if (subjectCustomizer != null) {
      
      Set<Subject> subjectSet = new LinkedHashSet<Subject>(subjectMap.values());
      
      subjectSet = subjectCustomizer.filterSubjects(grouperSession, subjectSet, filterSubjectsInStemName);
      
      //make a new map
      subjectMap = new LinkedHashMap<String, Subject>();
      
      for (Subject subject : subjectSet) {
        subjectMap.put(subject.getId(), subject);
      }
      
    }    
    return subjectMap;
  }
  
  /**
   * filter subjects based on subject customizer in grouper.properties
   * @param grouperSession
   * @param subjects
   * @param filterSubjectsInStemName 
   * @param attributeNamesRequested 
   * @return subjects
   */
  public static Set<Subject> filterSubjects(GrouperSession grouperSession, Set<Subject> subjects, String filterSubjectsInStemName) {
    
    //if nothing to do
    if (GrouperUtil.length(subjects) == 0) {
      return subjects;
    }
    
    SubjectCustomizer subjectCustomizer = subjectCustomizer();
    if (subjectCustomizer != null) {
      return subjectCustomizer.filterSubjects(grouperSession, subjects, filterSubjectsInStemName);
    }    
    return subjects;
  }

  /**
   * filter subjects based on subject customizer in grouper.properties
   * @param grouperSession
   * @param subject
   * @param filterSubjectsInStemName 
   * @param attributeNamesRequested 
   * @return subjects
   */
  public static Subject filterSubject(GrouperSession grouperSession, Subject subject, String filterSubjectsInStemName) {
    
    if (subject == null) {
      return null;
    }
    
    SubjectCustomizer subjectCustomizer = subjectCustomizer();
    if (subjectCustomizer == null) {
      return subject;
    }
    
    Set<Subject> subjects = new HashSet<Subject>();
    subjects.add(subject);
     
    subjects = subjectCustomizer.filterSubjects(grouperSession, subjects, filterSubjectsInStemName);
    
    int subjectsLength = GrouperUtil.length(subjects);
    if (subjectsLength == 0) {
      return null;
    }
    
    if (subjectsLength > 1) {
      throw new RuntimeException("Why would number of subjects be greater than 1??? " + subjectsLength);
    }
    
    return subjects.iterator().next();
  }

  /**
   * clea the subject customizer cache
   */
  public static void internalClearSubjectCustomizerCache() {
    subjectCustomizerClassCache.clear();
  }
  
  /**
   * get the subject customizer
   * @return subject customizer or null
   */
  public static SubjectCustomizer subjectCustomizer() {
    SubjectCustomizerCacheBean subjectCustomizerCacheBean = subjectCustomizerClassCache.get(Boolean.TRUE);
    
    if (subjectCustomizerCacheBean == null) {
      
      SubjectCustomizerCacheBean newBean = new SubjectCustomizerCacheBean();
      
      String subjectCustomizerClassName = GrouperConfig.retrieveConfig().propertyValueString("subjects.customizer.className");
      
      if (!StringUtils.isBlank(subjectCustomizerClassName)) {
        Class<SubjectCustomizer> theClass = GrouperUtil.forName(subjectCustomizerClassName);
        SubjectCustomizer subjectCustomizer = GrouperUtil.newInstance(theClass);
        newBean.setSubjectCustomizer(subjectCustomizer);
      }
      subjectCustomizerClassCache.put(Boolean.TRUE, newBean);
      subjectCustomizerCacheBean = newBean;
    }
    return subjectCustomizerCacheBean.getSubjectCustomizer();
  }
  
}

