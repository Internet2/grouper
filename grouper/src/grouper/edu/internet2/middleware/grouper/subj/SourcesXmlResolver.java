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

package edu.internet2.middleware.grouper.subj;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.SubjectFinder.RestrictSourceForGroup;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.subj.cache.SubjectSourceCache;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectTooManyResults;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.provider.SourceManager;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;


/**
 * Wrapper around Subject sources configured in <code>subject.properties</code>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SourcesXmlResolver.java,v 1.13 2009-10-19 19:01:55 mchyzer Exp $
 * @since   1.2.1
 */
public class SourcesXmlResolver implements SubjectResolver {

  /**
   * flush the cache (e.g. for testing)
   */
  public void flushCache() {
  }

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(SourcesXmlResolver.class);
  
  /**
   * Initialize a new <i>SourcesXmlResolver</i>.
   * @since   1.2.1
   */
  public SourcesXmlResolver() {
  }


  /**
   * @see     SubjectResolver#find(String)
   * @since   1.2.1
   */
  public Subject find(final String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException {
    List<Subject> subjects = new ArrayList();
    
    List<LogLabelCallable<Subject>> callables = new ArrayList<LogLabelCallable<Subject>>();
    
    Set<Source> sources = this.getSources();
    
    boolean needsThreads = needsThreads(sources, false);
    
    for ( Source sa : sources ) {
      final Source SOURCE = sa;
      callables.add(new LogLabelCallable<Subject>("find on source: " + sa.getId() + ", '" + id + "'") {

        public Subject callLogic() throws Exception {
          return SubjectSourceCache.getSubjectFromCacheOrSource(SOURCE, id, false);
        }
        
      });
    }    
    
    List<Subject> subjectResults = executeCallables(callables, needsThreads);
    
    for (Subject subject : subjectResults) {
      if (subject != null) {
        subjects.add(subject);
      }
    }
    
    Subject subject = this.thereCanOnlyBeOne(subjects, id);
    
    //filter if necessary
    subject = SubjectFinder.filterSubject(GrouperSession.staticGrouperSession(), subject, null);
    
    return subject;
  }            
  

  /**
   * 
   * TODO merge this with GrouperCallable
   * @param <T>
   */
  public static abstract class LogLabelCallable<T> implements Callable<T> {
    
    /** loglabel */
    private String logLabel;
    
    /**
     * @return the logLabel
     */
    public String getLogLabel() {
      return this.logLabel;
    }
    
    /**
     * @see java.util.concurrent.Callable#call()
     */
    public final T call() throws Exception {
      
      long subStartNanos = -1;
      if (LOG.isDebugEnabled()) {
        subStartNanos = System.nanoTime();
      }
      try {
        return this.callLogic();
      } finally {
        if (LOG.isDebugEnabled()) {
          long nanos = System.nanoTime() - subStartNanos;
          long millis = nanos / 1000000;
          LOG.debug(this.getLogLabel() + ", time in millis: " + millis);
        }
      }
      
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    public abstract T callLogic() throws Exception;

    
    /**
     * @param logLabel the logLabel to set
     */
    public void setLogLabel(String logLabel) {
      this.logLabel = logLabel;
    }
    
    /**
     * 
     * @param theLogLabel
     */
    public LogLabelCallable(String theLogLabel) {
      this.logLabel = theLogLabel;
    }
  }
  
  /**
   * execute callables either in threads or not
   * @param <T>
   * @param callables
   * @param useThreads
   * @return the results of each
   */
  private static <T> List<T> executeCallables(List<LogLabelCallable<T>> callables, boolean useThreads) {
    
    //if threadlocal says not to, dont
    if (!SubjectFinder.isUseThreadsBasedOnThreadLocal()) {
      useThreads = false;
    }
    
    List<T> results = new ArrayList<T>();
    long startNanos = -1;
    if (LOG.isDebugEnabled()) {
      startNanos = System.nanoTime();
    }
    RuntimeException re = null;
    try {
      //maybe dont use threads
      if (!useThreads) {
        for (LogLabelCallable<T> callable : callables) {
          try {
            results.add(callable.call());
            
          } catch (RuntimeException runtimeException1) {
            throw runtimeException1;
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
        
      } else {
        List<Future<T>> futures = new ArrayList<Future<T>>();
  
        final GrouperSession GROUPER_SESSION = GrouperSession.staticGrouperSession();
        
        for (Callable<T> callable : callables) {
          final Callable<T> CALLABLE = callable;
          Future<T> future = GrouperUtil.retrieveExecutorService().submit(new Callable<T>() {
  
            public T call() throws Exception {
              
              //propagate the grouper session...  note, dont do an inverse of control, not
              //sure if grouper session is thread safe...
              GrouperSession grouperSession = GrouperSession.start(GROUPER_SESSION.getSubject());
              try {
              
                return CALLABLE.call();
  
              } finally {
                GrouperSession.stopQuietly(grouperSession);
              }
            }
          });
          futures.add(future);
          
        }
        
        //wait for each and add results
        for (Future<T> future : futures) {
      try {
            results.add(future.get());
          } catch (ExecutionException executionException) {
            //the underlying exception is here... might be runtime
            Throwable throwable = executionException.getCause();
            if (throwable instanceof RuntimeException) {
              throw (RuntimeException)throwable;
            }
            throw new RuntimeException(throwable);
          } catch (InterruptedException interruptedException) {
            throw new RuntimeException(interruptedException);
          }
        }
        
      }
    } catch (RuntimeException runtimeException) {
      re = runtimeException;
    } finally {
      if (LOG.isDebugEnabled()) {
        long nanos = System.nanoTime() - startNanos;
        long millis = nanos / 1000000;
        LOG.debug("SourcesXmlResolver: Using threads: " + useThreads + ", time in millis: " + millis);
      }
      }
    if (re != null) {
      throw re;
    }    
    return results;
    
  }            

  /**
   * @see     SubjectResolver#find(String, String, String)
   * @since   1.2.1
   */
  public Subject find(String id, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = SubjectSourceCache.getSubjectFromCacheOrSource(getSource(source), id, true);
    updateMemberAttributes(subj);
    
    //before we have started up, it wont find the session :)
    if (GrouperStartup.isFinishedStartupSuccessfully() && (!(StringUtils.equals( GrouperConfig.ROOT, id) 
        && StringUtils.equals(InternalSourceAdapter.ID, source)))) {
      subj = SubjectFinder.filterSubject(GrouperSession.staticGrouperSession(), subj, null);
    }
    return subj;
  }

  /**
   * @see     SubjectResolver#findAll(String)
   * @since   1.2.1
   */
  public Set<Subject> findAll(String query)
    throws  IllegalArgumentException
  {
    return findAll(query, this.getSources());
  }

  /**
   * @see     SubjectResolver#findAll(String, String)
   * @since   1.2.1
   */
  public Set<Subject> findAll(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    Source sourceObject = this.getSource(source);
    try {
      Set<Subject> subjects = sourceObject.search(query);
      
      
      subjects = SubjectFinder.filterSubjects(GrouperSession.staticGrouperSession(), subjects, null);
      
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.sort.subjectSets.exactOnTop", true)) {
        subjects = SubjectHelper.sortSetForSearch(subjects, query);
      }
      return subjects;
    } catch (RuntimeException re) {
      if (re instanceof SubjectTooManyResults) {
        throw (SubjectTooManyResults)re;
      }
      String throwErrorOnFindAllFailureString = sourceObject.getInitParam("throwErrorOnFindAllFailure");
      boolean throwErrorOnFindAllFailure = SubjectUtils.booleanValue(throwErrorOnFindAllFailureString, true);

      if (!throwErrorOnFindAllFailure) {
        LOG.error("Exception with source: " + sourceObject.getId() + ", on query: '" + query + "'", re);
        return new HashSet<Subject>();
      } 
      throw new SourceUnavailableException(
          "Exception with source: " + sourceObject.getId() + ", on query: '" + query + "'", re);
    }
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(final String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    List<Subject> subjects = new ArrayList();
    
    List<LogLabelCallable<Subject>> callables = new ArrayList<LogLabelCallable<Subject>>();
    
    Set<Source> sources = this.getSources();
    
    boolean needsThreads = needsThreads(sources, false);
    
    for ( Source sa : sources ) {
      final Source SOURCE = sa;
      callables.add(new LogLabelCallable<Subject>("findByIdentifier on source: " + sa.getId() + ", '" + id + "'") {

        public Subject callLogic() throws Exception {

          return SubjectSourceCache.getSubjectByIdentifierFromCacheOrSource(SOURCE, id, false);
        }
        
      });
      }
    
    List<Subject> subjectResults = executeCallables(callables, needsThreads);
    
    for (Subject subject : subjectResults) {
      if (subject != null) {
        subjects.add(subject);
      }
    }    
    Subject subject = this.thereCanOnlyBeOne(subjects, id);
    subject = SubjectFinder.filterSubject(GrouperSession.staticGrouperSession(), subject, null);
    return subject;
  }            

  /**
   * @see     SubjectResolver#findByIdentifier(String, String, String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(String id, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = SubjectSourceCache.getSubjectByIdentifierFromCacheOrSource(getSource(source), id, true);
    updateMemberAttributes(subj);
    subj = SubjectFinder.filterSubject(GrouperSession.staticGrouperSession(), subj, null);
    return subj;
  }

  /**
   * @see     SubjectResolver#getSource(String)
   * @since   1.2.1
   */
  public Source getSource(String id) 
    throws  IllegalArgumentException,
            SourceUnavailableException { 
    try {
      return SourceManager.getInstance().getSource(id);
    } catch (SourceUnavailableException sue) {
      throw new SourceUnavailableException("Cant find source with id: '" + id + "', " 
          + this.sourceIdErrorSafe(), sue);
    }
  }
  
  /**
   * @see     SubjectResolver#getSources()
   * @since   1.2.1
   */
  public Set<Source> getSources() {
    return new LinkedHashSet( SourceManager.getInstance().getSources() );
  }

  /**
   * return the error string related to source id's, dont fail
   * @return the error string related to source id's, dont fail
   */
  private String sourceIdErrorSafe() {
    try {
      StringBuilder result = new StringBuilder();
      result.append("Possible source id's: ");
      for (Source source : this.getSources()) {
        result.append("'").append(source.getId()).append("', ");
      }
      return result.toString();
    } catch (Exception e) {
      LOG.error("Error calculating source id error message: ", e);
    }
    return "";
  }
  
  /**
   * @param subjectType 
   * @return the sources
   * @throws IllegalArgumentException 
   * @see     SubjectResolver#getSources(String)
   * @since   1.2.1
   */
  public Set<Source> getSources(String subjectType) 
    throws  IllegalArgumentException { 
    return new LinkedHashSet( SourceManager.getInstance().getSources( SubjectTypeEnum.valueOf(subjectType) ) );
  }

  /**
   * @param   subjects  List of found subjects.
   * @param   id        Subject identifier used in query.
   * @return  Matching subject if there is only one.
   * @throws  SubjectNotFoundException if less than 1 matching subjects found.
   * @throws  SubjectNotUniqueException if more than 1 matching subjects found.
   * @since   1.2.1
   */
  private Subject thereCanOnlyBeOne(List<Subject> subjects, String id) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException {
    if (subjects.size() == 0) {
      throw new SubjectNotFoundException("subject not found: " + id);
    } else if (subjects.size() == 1) {
      Subject subj = subjects.get(0);
      updateMemberAttributes(subj);
      return subj;
    }
    throw new SubjectNotUniqueException( "found multiple matching subjects: " + subjects.size() + ", '" + id + "'" );
  }

  /**
   * @see SubjectResolver#findByIdOrIdentifier(String)
   */
  public Subject findByIdOrIdentifier(final String idOrIdentifier) throws IllegalArgumentException,
      SubjectNotFoundException, SubjectNotUniqueException {
    
    List<Subject> subjects = new ArrayList();
    
    List<LogLabelCallable<Subject>> callables = new ArrayList<LogLabelCallable<Subject>>();
    
    Set<Source> sources = this.getSources();
    
    boolean needsThreads = needsThreads(sources, false);
    
    for ( Source sa : sources ) {
      final Source SOURCE = sa;
      callables.add(new LogLabelCallable<Subject>("findByIdOrIdentifier on source: " + sa.getId() + ", '" + idOrIdentifier + "'") {

        public Subject callLogic() throws Exception {
          
          return SubjectSourceCache.getSubjectByIdOrIdentifierFromCacheOrSource(SOURCE, idOrIdentifier, false);
        }
        
      });
      }
    
    List<Subject> subjectResults = executeCallables(callables, needsThreads);
    
    for (Subject subject : subjectResults) {
      if (subject != null) {
        subjects.add(subject);
      }
    }    
    Subject subject = this.thereCanOnlyBeOne(subjects, idOrIdentifier);
    subject = SubjectFinder.filterSubject(GrouperSession.staticGrouperSession(), subject, null);
    return subject;
  }

  /**
   * @see SubjectResolver#findByIdOrIdentifier(String, String)
   */
  public Subject findByIdOrIdentifier(String id, String source)
      throws IllegalArgumentException, SourceUnavailableException,
      SubjectNotFoundException, SubjectNotUniqueException {
    
    Subject subj = SubjectSourceCache.getSubjectByIdOrIdentifierFromCacheOrSource(getSource(source), id, true);
    updateMemberAttributes(subj);
    subj = SubjectFinder.filterSubject(GrouperSession.staticGrouperSession(), subj, null);
    return subj;
  }
  
  /**
   * note if stem name is blank, it means root
   * @see edu.internet2.middleware.grouper.subj.SubjectResolver#findAllInStem(java.lang.String, java.lang.String)
   */
  public Set<Subject> findAllInStem(String stemName, String query)
      throws IllegalArgumentException {
    
    Set<Subject> subjects = new LinkedHashSet();
    
    Set<Source> sourcesToLookIn = new LinkedHashSet<Source>();
    
    
    //if stem name is blank, they mean root
    if (StringUtils.isBlank(stemName)) {
      stemName = ":";
    }
    
    //loop through sources
    for ( Source sa : this.getSources() ) {
      
      //see if it is restricted
      RestrictSourceForGroup restrictSourceForGroup = SubjectFinder.restrictSourceForGroup(stemName, sa.getId());
      if (!restrictSourceForGroup.isRestrict() || restrictSourceForGroup.getGroup() != null) {
        sourcesToLookIn.add(sa);
      }
    }
    
    //if zero it will look in all
    if (GrouperUtil.length(sourcesToLookIn) > 0) {
      subjects = findAllHelper(query, sourcesToLookIn, stemName);
    }
    
    return subjects;
  }

  /**
   * @param subj
   */
  private void updateMemberAttributes(Subject subj) {
    
    //if not started, then maybe not initted...
    if (GrouperStartup.isFinishedStartupSuccessfully() && !"g:gsa".equals(subj.getSourceId())) {
      // update member attributes
      Member member = MemberFinder.internal_findBySubject(subj, null, false);
  
      if (member != null) {
        member.updateMemberAttributes(subj, true);
      }
    }
  }


  /**
   * @see     SubjectResolver#findPage(String)
   * @since   1.2.1
   */
  public SearchPageResult findPage(String query)
    throws  IllegalArgumentException {
    return findPage(query, this.getSources());
  }


  /**
   * @see     SubjectResolver#findAll(String, String)
   * @since   1.2.1
   */
  public SearchPageResult findPage(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    Source sourceObject = this.getSource(source);
    SearchPageResult searchPage = null; 
    final Set<Subject> subjectsMatchIdentifier = Collections.synchronizedSet(new HashSet<Subject>());

    try {
      searchPage = sourceObject.searchPage(query);
      Set<Subject> subjects = searchPage.getResults();
      if (searchPage.isTooManyResults()) {
        
        //if there are too many, then add the idOrIdentifier too to make sure that is there
        //note, this might mean there is more than the page size... oh well
        Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(query, source, false);
        //lets make sure it is not already there
        if (subject != null && !SubjectHelper.inList(searchPage.getResults(), subject)) {
          subjects.add(subject);
        }
        if (subject != null) {
          subjectsMatchIdentifier.add(subject);
        }
      }
      
      //filter if necessary
      subjects = SubjectFinder.filterSubjects(GrouperSession.staticGrouperSession(), subjects, null);
      
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.sort.subjectSets.exactOnTop", true)) {
        subjects = SubjectHelper.sortSetForSearch(subjects, query, subjectsMatchIdentifier);
        searchPage.setResults(subjects);
      }
      return searchPage;
    } catch (RuntimeException re) {
      String throwErrorOnFindAllFailureString = sourceObject.getInitParam("throwErrorOnFindAllFailure");
      boolean throwErrorOnFindAllFailure = SubjectUtils.booleanValue(throwErrorOnFindAllFailureString, true);
  
      if (!throwErrorOnFindAllFailure) {
        LOG.error("Exception with source: " + sourceObject.getId() + ", on query: '" + query + "'", re);
        return new SearchPageResult(false, new HashSet<Subject>());
      } 
      throw new SourceUnavailableException(
          "Exception with source: " + sourceObject.getId() + ", on query: '" + query + "'", re);
    }
  }

  /**
   * 
   * @param sources
   * @param isSearchPage is the call for a general or paged search
   * @return if we need threads
   */
  private boolean needsThreads(Set<Source> sources, boolean isSearchPage) {
    
    //default to false since threading doesnt really help
    boolean useThreadsFromConfig = GrouperConfig.retrieveConfig().propertyValueBoolean(
        isSearchPage ? "subjects.allPage.useThreadForkJoin" : "subjects.idOrIdentifier.useThreadForkJoin", false);
    
    if (!useThreadsFromConfig) {
      return false;
    }
    
    
    int sourcesNeedThreads = 0;
    for (Source source : sources) {
      
      //this one doesnt hit a db/ldap or whatever
      if (!(StringUtils.equals(InternalSourceAdapter.ID, source.getId()))) {
        sourcesNeedThreads++;
      }
      
    }
    
    return sourcesNeedThreads > 1;
    
  }

  /**
   * note if stem name is blank, it means root
   * @see edu.internet2.middleware.grouper.subj.SubjectResolver#findAllInStem(java.lang.String, java.lang.String)
   */
  public SearchPageResult findPageInStem(String stemName, String query)
      throws IllegalArgumentException {
    Set<Source> sources = new LinkedHashSet<Source>(SourceManager.getInstance().getSources());
    return findPageInStem(stemName, query, sources);
  }

  /**
   * note if stem name is blank, it means root
   * @see edu.internet2.middleware.grouper.subj.SubjectResolver#findAllInStem(java.lang.String, java.lang.String, Set)
   */
  public SearchPageResult findPageInStem(String stemName, String query, Set<Source> sources)
      throws IllegalArgumentException {
    
    SearchPageResult searchPageResult = new SearchPageResult();
    searchPageResult.setTooManyResults(false);
    
    if (GrouperUtil.length(sources) == 0) {
      sources = this.getSources();
    }
    
    Set<Source> sourcesToLookIn = new LinkedHashSet<Source>();
    
    
    //if stem name is blank, they mean root
    if (StringUtils.isBlank(stemName)) {
      stemName = ":";
    }
    
    //loop through sources
    for ( Source sa : sources ) {
      
        //see if it is restricted
        RestrictSourceForGroup restrictSourceForGroup = SubjectFinder.restrictSourceForGroup(stemName, sa.getId());
        if (!restrictSourceForGroup.isRestrict() || restrictSourceForGroup.getGroup() != null) {
        sourcesToLookIn.add(sa);
      }
    }
    
    //if zero it will look in all
    if (GrouperUtil.length(sourcesToLookIn) > 0) {
      searchPageResult = findPageHelper(query, sourcesToLookIn, stemName);
    }
    
    return searchPageResult;
  }

  /**
   * @see SubjectResolver#findAll(String, Set)
   */
  public Set<Subject> findAll(final String query, Set<Source> sources) {
    return findAllHelper(query, sources, null);
  }

  /**
   * @param query 
   * @param sources 
   * @param stemName 
   * @return  subjects
   * @throws IllegalArgumentException 
   */
  private Set<Subject> findAllHelper(final String query, Set<Source> sources, String stemName)
      throws IllegalArgumentException {
    
    if (GrouperUtil.length(sources) == 0) {
      return findAll(query);
    }
    
    Set<Subject> subjects = null;
    
    //can configure not separating search string by commas
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperQuerySubjectsMultipleQueriesCommaSeparated", true)) {
      
      Set<String> queries = GrouperUtil.splitTrimToSet(query, ",");
      
      subjects = new LinkedHashSet<Subject>();
      
      for (String individualQuery : GrouperUtil.nonNull(queries)) {
        
        subjects.addAll(GrouperUtil.nonNull(findAllHelperNonMultiple(individualQuery, sources, stemName)));
        
      }

      //take out dupes
      SubjectHelper.removeDuplicates(subjects);
      
    } else {
      subjects = findAllHelperNonMultiple(query, sources, stemName);

    }
    
    
    subjects = SubjectFinder.filterSubjects(GrouperSession.staticGrouperSession(), subjects, stemName);
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.sort.subjectSets.exactOnTop", true)) {
      subjects = SubjectHelper.sortSetForSearch(subjects, query);
    }

    return subjects;
  }

  /**
   * run a subject search without checking for multiple queries comma separated
   * @param query
   * @param sources
   * @param stemName
   * @return the results
   */
  private Set<Subject> findAllHelperNonMultiple(final String query, final Set<Source> sources, final String stemName) {
    Set<Subject> subjects = new LinkedHashSet<Subject>();
    
    List<LogLabelCallable<Set<Subject>>> callables = new ArrayList<LogLabelCallable<Set<Subject>>>();
    
    boolean needsThreads = needsThreads(sources, false);
    
    //get all the jobs ready to go
    for ( Source sa : sources ) {
      final Source SOURCE = sa;
      callables.add(new LogLabelCallable<Set<Subject>>("findAll on source: " + sa.getId() + ", '" + query + "'") {

        public Set<Subject> callLogic() throws Exception {
          try {
            return SOURCE.search(query);
      } catch (RuntimeException re) {
            if (re instanceof SubjectTooManyResults) {
              throw (SubjectTooManyResults)re;
            }
            String throwErrorOnFindAllFailureString = SOURCE.getInitParam("throwErrorOnFindAllFailure");
        boolean throwErrorOnFindAllFailure = SubjectUtils.booleanValue(throwErrorOnFindAllFailureString, true);

        if (!throwErrorOnFindAllFailure) {
              LOG.error("Exception with source: " + SOURCE.getId() + ", on query: '" + query + "'", re);
        } else {
          throw new SourceUnavailableException(
                  "Exception with source: " + SOURCE.getId() + ", on query: '" + query + "'", re);
            }
          }
          return null;
        }

      });
      }
    
    //run the jobs
    List<Set<Subject>> subjectResults = executeCallables(callables, needsThreads);
    
    for (Set<Subject> subjectSet : subjectResults) {
      if (subjectSet != null) {
        subjects.addAll(subjectSet);
      }
    }
    
    return subjects;
  }
  
  /**
   * @see SubjectResolver#findPage(String, Set)
   */
  public SearchPageResult findPage(final String query, Set<Source> sources)
      throws SourceUnavailableException {
    
    return findPageHelper(query, sources, null);
    
  }
  
  
  /**
   * @param query 
   * @param sources 
   * @param stemName 
   * @return search page result
   * @throws SourceUnavailableException 
   */
  private SearchPageResult findPageHelper(final String query, Set<Source> sources, String stemName)
      throws SourceUnavailableException {
    
    if (GrouperUtil.length(sources) == 0) {
      return findPage(query);
    }
    Set<Subject> subjects = null;
    
    //can configure not separating search string by commas
    boolean tooManyResults = false;

    Set<Subject> subjectsMatchIdentifier = Collections.synchronizedSet(new HashSet<Subject>());

    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperQuerySubjectsMultipleQueriesCommaSeparated", true)) {
      
      Set<String> queries = GrouperUtil.splitTrimToSet(query, ",");
      
      subjects = new LinkedHashSet<Subject>();
      for (String individualQuery : GrouperUtil.nonNull(queries)) {
        
        SearchPageResult searchPageResult = findPageHelperNonMultiple(individualQuery, sources, stemName, subjectsMatchIdentifier);
        subjects.addAll(GrouperUtil.nonNull(searchPageResult.getResults()));
        tooManyResults = tooManyResults || searchPageResult.isTooManyResults();
      }
  
      //take out dupes
      SubjectHelper.removeDuplicates(subjects);
      
    } else {
      SearchPageResult searchPageResult = findPageHelperNonMultiple(query, sources, stemName, subjectsMatchIdentifier);
      subjects = searchPageResult.getResults();
      tooManyResults = searchPageResult.isTooManyResults();
  
    }
    
    //filter if necessary
    subjects = SubjectFinder.filterSubjects(GrouperSession.staticGrouperSession(), subjects, stemName);
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.sort.subjectSets.exactOnTop", true)) {
      subjects = SubjectHelper.sortSetForSearch(subjects, query, subjectsMatchIdentifier);
    }
    
    return new SearchPageResult(tooManyResults, subjects);
  }  
  /**
   * @param query 
   * @param sources 
   * @param stemName 
   * @return search page result
   * @throws SourceUnavailableException 
   */
  private SearchPageResult findPageHelperNonMultiple(final String query, Set<Source> sources, String stemName, final Set<Subject> subjectsMatchIdentifier)
      throws SourceUnavailableException {
    
    if (GrouperUtil.length(sources) == 0) {
      return findPage(query);
    }
    
    SearchPageResult searchPageResult = new SearchPageResult();
    searchPageResult.setTooManyResults(false);
    Set<Subject> subjects = new LinkedHashSet<Subject>();
    searchPageResult.setResults(subjects);
    
    List<LogLabelCallable<SearchPageResult>> callables = new ArrayList<LogLabelCallable<SearchPageResult>>();
    
    boolean needsThreads = needsThreads(sources, false);
        
    //get all the jobs ready to go
    for ( Source sa : sources ) {
      final Source SOURCE = sa;
      callables.add(new LogLabelCallable<SearchPageResult>("findPage on source: " + sa.getId() + ", '" + query + "'") {

        public SearchPageResult callLogic() throws Exception {
          try {
            SearchPageResult searchPage = SOURCE.searchPage(query);

            if (searchPage.isTooManyResults()) {
              
              //if there are too many, then add the idOrIdentifier too to make sure that is there
              //note, this might mean there is more than the page size... oh well
              Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(query, SOURCE.getId(), false);
              //lets make sure it is not already there
              if (subject != null && !SubjectHelper.inList(searchPage.getResults(), subject)) {
                //assume this is not unmodifiable...
                searchPage.getResults().add(subject);
              }
              if (subject != null) {
                subjectsMatchIdentifier.add(subject);
              }

            }
            return searchPage;
          } catch (RuntimeException re) {
            String throwErrorOnFindAllFailureString = SOURCE.getInitParam("throwErrorOnFindAllFailure");
            boolean throwErrorOnFindAllFailure = SubjectUtils.booleanValue(throwErrorOnFindAllFailureString, true);
      
            if (!throwErrorOnFindAllFailure) {
              LOG.error("Exception with source: " + SOURCE.getId() + ", on query: '" + query + "'", re);
            } else {
              throw new SourceUnavailableException(
                  "Exception with source: " + SOURCE.getId() + ", on query: '" + query + "'", re);
            }
          }
          return null;
        }
        
      });
    }    
    
    //run the jobs
    List<SearchPageResult> subjectResults = executeCallables(callables, needsThreads);
    
    for (SearchPageResult searchPage : subjectResults) {
      subjects.addAll( GrouperUtil.nonNull(searchPage.getResults()) );
      if (searchPage.isTooManyResults()) {
        searchPageResult.setTooManyResults(true);
      }
    }
    
    searchPageResult.setResults(subjects);

    return searchPageResult;

  }
  
  /**
   * @see SubjectResolver#findByIdentifiers(Collection)
   */
  public Map<String, Subject> findByIdentifiers(final Collection<String> identifiers)
      throws IllegalArgumentException {
    
    Map<String, Subject> subjectMapResults = new HashMap<String, Subject>();
    
    List<LogLabelCallable<Map<String, Subject>>> callables = new ArrayList<LogLabelCallable<Map<String, Subject>>>();
    
    Set<Source> sources = this.getSources();
    
    boolean needsThreads = needsThreads(sources, false);
    
    for ( Source sa : sources ) {
      final Source SOURCE = sa;
      callables.add(new LogLabelCallable<Map<String, Subject>>(
          "find by identifiers on source: " + sa.getId() + ", '" + GrouperUtil.toStringForLog(identifiers, 100) + "'") {

        public Map<String, Subject> callLogic() throws Exception {
          
          return SubjectSourceCache.getSubjectsByIdentifiersFromCacheOrSource(SOURCE, identifiers);
        }
        
      });
    }    
    
    List<Map<String, Subject>> subjectResults = executeCallables(callables, needsThreads);
    
    for (Map<String, Subject> subjectMap : subjectResults) {
      if (subjectMap != null) {
        subjectMapResults.putAll(subjectMap);
      }
    }

    subjectMapResults = SubjectFinder.filterSubjects(GrouperSession.staticGrouperSession(), subjectMapResults, null);
    
    return subjectMapResults;

    
  }

  /**
   * @see SubjectResolver#findByIdentifiers(Collection, String)
   */
  public Map<String, Subject> findByIdentifiers(Collection<String> identifiers, String source)
      throws IllegalArgumentException, SourceUnavailableException {
    
    Map<String, Subject> subjectMap = SubjectSourceCache.getSubjectsByIdentifiersFromCacheOrSource(this.getSource(source), identifiers);
//if you are going to do this, do it in a batch!!!!
//    if (subjectMap != null) {
//      for (Subject subject : subjectMap.values()) {
//        updateMemberAttributes(subject);
//      }
//    }
    
    subjectMap = SubjectFinder.filterSubjects(GrouperSession.staticGrouperSession(), subjectMap, null);

    return subjectMap;
  }

  /**
   * @see SubjectResolver#findByIds(Collection)
   */
  public Map<String, Subject> findByIds(final Collection<String> ids)
      throws IllegalArgumentException {

    Map<String, Subject> subjectMapResults = new HashMap<String, Subject>();
    
    List<LogLabelCallable<Map<String, Subject>>> callables = new ArrayList<LogLabelCallable<Map<String, Subject>>>();
    
    Set<Source> sources = this.getSources();
    
    boolean needsThreads = needsThreads(sources, false);
    
    for ( Source sa : sources ) {
      final Source SOURCE = sa;
      callables.add(new LogLabelCallable<Map<String, Subject>>(
          "find on source: " + sa.getId() + ", '" + GrouperUtil.toStringForLog(ids, 100) + "'") {

        public Map<String, Subject> callLogic() throws Exception {
          
          return SubjectSourceCache.getSubjectsByIdsFromCacheOrSource(SOURCE, ids);
          
        }
        
      });
    }    
    
    List<Map<String, Subject>> subjectResults = executeCallables(callables, needsThreads);
    
    for (Map<String, Subject> subjectMap : subjectResults) {
      if (subjectMap != null) {
        subjectMapResults.putAll(subjectMap);
      }
    }
    
    subjectMapResults = SubjectFinder.filterSubjects(GrouperSession.staticGrouperSession(), subjectMapResults, null);

    return subjectMapResults;
    
  }
  
  /**
   * @see SubjectResolver#findByIds(Collection, String)
   */
  public Map<String, Subject> findByIds(Collection<String> ids, String source)
      throws IllegalArgumentException, SourceUnavailableException {

    Map<String, Subject> subjectMap = SubjectSourceCache.getSubjectsByIdsFromCacheOrSource(this.getSource(source), ids);
    if (subjectMap != null) {
//if you are going to do this, do it in a batch!!!!
//      for (Subject subject : subjectMap.values()) {
//        updateMemberAttributes(subject);
//      }
      
    }
    subjectMap = SubjectFinder.filterSubjects(GrouperSession.staticGrouperSession(), subjectMap, null);
    return subjectMap;

  }

  /**
   * @see SubjectResolver#findByIdsOrIdentifiers(Collection)
   */
  public Map<String, Subject> findByIdsOrIdentifiers(final Collection<String> idsOrIdentifiers)
      throws IllegalArgumentException {
    Set<Source> sources = this.getSources();
    return findByIdsOrIdentifiers(idsOrIdentifiers, sources);
  }


  /**
   * @see SubjectResolver#findByIdsOrIdentifiers(Collection)
   */
  public Map<String, Subject> findByIdsOrIdentifiers(final Collection<String> idsOrIdentifiers, Set<Source> sources)
      throws IllegalArgumentException {

    Map<String, Subject> subjectMapResults = new HashMap<String, Subject>();
    
    List<LogLabelCallable<Map<String, Subject>>> callables = new ArrayList<LogLabelCallable<Map<String, Subject>>>();
    
    boolean needsThreads = needsThreads(sources, false);
    
    for ( Source sa : sources ) {
      final Source SOURCE = sa;
      callables.add(new LogLabelCallable<Map<String, Subject>>(
          "find by id or identifier on source: " + sa.getId() + ", '" + GrouperUtil.toStringForLog(idsOrIdentifiers, 100) + "'") {

        public Map<String, Subject> callLogic() throws Exception {
          
          return SubjectSourceCache.getSubjectsByIdsOrIdentifiersFromCacheOrSource(SOURCE, idsOrIdentifiers);
        }
        
      });
    }    
    
    List<Map<String, Subject>> subjectResults = executeCallables(callables, needsThreads);
    
    for (Map<String, Subject> subjectMap : subjectResults) {
      if (subjectMap != null) {
        subjectMapResults.putAll(subjectMap);
      }
    }
    
    subjectMapResults = SubjectFinder.filterSubjects(GrouperSession.staticGrouperSession(), subjectMapResults, null);

    return subjectMapResults;

    
  }
  
  /**
   * @see SubjectResolver#findByIdsOrIdentifiers(Collection, String)
   */
  public Map<String, Subject> findByIdsOrIdentifiers(Collection<String> idsOrIdentifiers, String source)
      throws IllegalArgumentException, SourceUnavailableException {

    Map<String, Subject> subjectMap = SubjectSourceCache.getSubjectsByIdsOrIdentifiersFromCacheOrSource(this.getSource(source), idsOrIdentifiers);

//if you are going to do this, do it in a batch!!!!
//    if (subjectMap != null) {
//      for (Subject subject : subjectMap.values()) {
//        updateMemberAttributes(subject);
//      }
//    }
    subjectMap = SubjectFinder.filterSubjects(GrouperSession.staticGrouperSession(), subjectMap, null);

    return subjectMap;

  }



  
}

