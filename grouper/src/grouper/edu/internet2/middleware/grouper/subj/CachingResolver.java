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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * Decorator that provides caching for {@link SubjectResolver}.
 * 
 * TODO the caching of findAll should work based on group restricted to, not stem name
 * <p/>
 * @author  blair christensen.
 * @version $Id: CachingResolver.java,v 1.8 2008-08-26 21:11:51 mchyzer Exp $
 * @since   1.2.1
 */
public class CachingResolver extends SubjectResolverDecorator {

  /**
   * cache of multikey, to subject
   */
  static GrouperCache<MultiKey, Subject> findCache = new GrouperCache<MultiKey, Subject>(CachingResolver.class.getName() + ".Find", 5000, false, 30, 120, false);

  /**
   * cache of multikey including query to subjects
   */
  static GrouperCache<MultiKey, Set<Subject>> findAllCache = new GrouperCache<MultiKey, Set<Subject>>(CachingResolver.class.getName() + ".FindAll", 5000, false, 30, 120, false);
  
  /**
   * cache of multikey including query to subjects, and source ids
   */
  static GrouperCache<MultiKey, SearchPageResult> findPageCache = new GrouperCache<MultiKey, SearchPageResult>(CachingResolver.class.getName() + ".FindPage", 5000, false, 30, 120, false);
  
  /**
   * cache of multikey, to subject
   */
  static GrouperCache<MultiKey, Subject> findByIdentifierCache = new GrouperCache<MultiKey, Subject>(CachingResolver.class.getName() + ".FindByIdentifier", 5000, false, 30, 120, false);

  /**
   * cache of multikey, to subject
   */
  static GrouperCache<MultiKey, Subject> findByIdOrIdentifierCache = new GrouperCache<MultiKey, Subject>(CachingResolver.class.getName() + ".FindByIdOrIdentifier", 5000, false, 30, 120, false);

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(CachingResolver.class);

  /**
   * flush the cache (e.g. for testing)
   */
  public void flushCache() {
    findCache.clear();
    findAllCache.clear();
    findByIdentifierCache.clear();
  }

  /**
   * @since   1.2.1
   */
  public CachingResolver(SubjectResolver resolver) {
    super(resolver);
  }


  /**
   * @see     SubjectResolver#find(String)
   * @since   1.2.1
   */
  public Subject find(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindCache(id, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().find(id);
      this.putInFindCache(subj);
    }
    return subj;
  }

  /**
   * @see     SubjectResolver#find(String, String)
   * @since   1.2.1
   */
  public Subject find(String id, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindCache(id, source);
    if (subj == null) {
      subj = super.getDecoratedResolver().find(id, source);
      this.putInFindCache(subj);
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
    Set<Subject> subjects = this.getFromFindAllCache(null, query, (String)null);
    if (subjects == null) {
      subjects = super.getDecoratedResolver().findAll(query);
      this.putInFindAllCache(null, query, (String)null, subjects);
    }
    return subjects;
  }

  /**
   * @see     SubjectResolver#findAll(String, String)
   * @since   1.2.1
   */
  public Set<Subject> findAll(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    Set<Subject> subjects = this.getFromFindAllCache(null, query, source);
    if (subjects == null) {
      subjects = super.getDecoratedResolver().findAll(query, source);
      this.putInFindAllCache(null, query, source, subjects);
    }
    return subjects;
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindByIdentifierCache(id, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().findByIdentifier(id);
      this.putInFindByIdentifierCache(id, subj);
    }
    return subj;
  }            

  /**
   * @see     SubjectResolver#findByIdentifier(String, String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(String id, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindByIdentifierCache(id, source);
    if (subj == null) {
      subj = super.getDecoratedResolver().findByIdentifier(id, source);
      this.putInFindByIdentifierCache(id, subj);
    }
    return subj;
  }

  /**
   * Retrieve set of subjects from cache for <code>findAll(...)</code>.
   * @return  Cached set of subjects or null.
   * @since   1.2.1
   */
  private Set<Subject> getFromFindAllCache(String stemName, String query, String source) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      return null;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    return findAllCache.get( new MultiKey(new Object[]{grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), stemName, query, source, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()}) );
  }

  /**
   * Retrieve subject from cache for <code>findByIdentifier(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindByIdentifierCache(String id, String source) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      return null;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    return findByIdentifierCache.get( new MultiKey(grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), id, source, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()) );
  }

  /**
   * Retrieve subject from cache for <code>find(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindCache(String id, String source) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      return null;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    return findCache.get(new MultiKey(grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), id, source, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()));
  }

  /**
   * @see     SubjectResolver#getSource(String)
   * @since   1.2.1
   */
  public Source getSource(String id) 
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    return super.getDecoratedResolver().getSource(id);
  }
 
  /**
   * @see     SubjectResolver#getSources()
   * @since   1.2.1
   */
  public Set<Source> getSources() {
    return super.getDecoratedResolver().getSources();
  }

  /**
   * Put set of subjects into cache for <code>findAll(...)</code>.
   * @since   1.2.1
   */
  private void putInFindAllCache(String stemName, String query, String source, Set<Subject> subjects) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      return;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    findAllCache.put( new MultiKey(new Object[]{grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), stemName, query, source, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()}), subjects );
  }

  /**
   * Put subject into cache for <code>findByIdentifier(...)</code>.
   * @since   1.2.1
   */
  private void putInFindByIdentifierCache(String idfr, Subject subj) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null || subj == null) {
      return;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    findByIdentifierCache.put( 
      new MultiKey(grouperSessionSubject.getSourceId(), 
          grouperSessionSubject.getId(), idfr, null, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()), subj  
    );
    findByIdentifierCache.put( 
        new MultiKey(grouperSessionSubject.getSourceId(), 
            grouperSessionSubject.getId(),  idfr, subj.getSource().getId(), GrouperSourceAdapter.searchForGroupsWithReadPrivilege() ), subj
    );
    this.putInFindCache(subj);
  }

  /**
   * Put subject into cache for <code>find(...)</code>.
   * @since   1.2.1
   */
  private void putInFindCache(Subject subj) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null || subj == null) {
      return;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    findCache.put( 
        new MultiKey(grouperSessionSubject.getSourceId(), 
            grouperSessionSubject.getId(),  subj.getId(), null, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()), subj  
    );
    findCache.put( 
        new MultiKey(grouperSessionSubject.getSourceId(), 
            grouperSessionSubject.getId(),  subj.getId(), subj.getSource().getId(), GrouperSourceAdapter.searchForGroupsWithReadPrivilege() ), subj
    );
  }

  /**
   * Retrieve subject from cache for <code>findByIdentifier(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindByIdOrIdentifierCache(String identifier, String source) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      return null;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    return findByIdOrIdentifierCache.get( new MultiKey(grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), identifier, source, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()) );
  }

  /**
   * Put subject into cache for <code>findByIdentifier(...)</code>.
   * @since   1.2.1
   */
  private void putInFindByIdOrIdentifierCache(String idfr, Subject subj) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null || subj == null) {
      return;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    findByIdOrIdentifierCache.put( 
      new MultiKey(grouperSessionSubject.getSourceId(), 
          grouperSessionSubject.getId(), idfr, null, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()), subj  
    );
    findByIdOrIdentifierCache.put( 
        new MultiKey(grouperSessionSubject.getSourceId(), 
            grouperSessionSubject.getId(),  idfr, subj.getSource().getId(), GrouperSourceAdapter.searchForGroupsWithReadPrivilege() ), subj
    );
    this.putInFindCache(subj);
  }

  /**
   * 
   */
  public Subject findByIdOrIdentifier(String id) throws IllegalArgumentException,
      SubjectNotFoundException, SubjectNotUniqueException {
    Subject subj = this.getFromFindByIdOrIdentifierCache(id, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().findByIdOrIdentifier(id);
      this.putInFindByIdOrIdentifierCache(id, subj);
    }
    return subj;
  }

  /**
   * @see SubjectResolver#findByIdOrIdentifier(String, String)
   */
  public Subject findByIdOrIdentifier(String id, String source)
      throws IllegalArgumentException, SourceUnavailableException,
      SubjectNotFoundException, SubjectNotUniqueException {
    Subject subj = this.getFromFindByIdOrIdentifierCache(id, source);
    if (subj == null) {
      subj = super.getDecoratedResolver().findByIdOrIdentifier(id, source);
      this.putInFindByIdOrIdentifierCache(id, subj);
    }
    return subj;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.subj.SubjectResolver#findAllInStem(java.lang.String, java.lang.String)
   */
  public Set<Subject> findAllInStem(String stemName, String query)
      throws IllegalArgumentException {

    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    if (LOG.isDebugEnabled()) {
      debugMap.put("operation", "findAllInStem");
      debugMap.put("stemName", stemName);
      debugMap.put("query", query);
    }

    Set<Subject> subjects = this.getFromFindAllCache(stemName, query, (String)null);
    
    //TODO do this caching better... need to clear when group changes???
    //for now dont cache if finding in stem name
    if (subjects == null || !StringUtils.isBlank(stemName)) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("foundInCache", Boolean.FALSE);
      }
      subjects = super.getDecoratedResolver().findAllInStem(stemName, query);
      this.putInFindAllCache(stemName, query, (String)null, subjects);
    } else {
      if (LOG.isDebugEnabled()) {
        debugMap.put("foundInCache", Boolean.TRUE);
      }

    }
    if (LOG.isDebugEnabled()) {
      debugMap.put("resultSize", GrouperUtil.length(subjects));
      LOG.debug(GrouperUtil.mapToString(debugMap));
    }
    return subjects;
  }

  /**
   * @see     SubjectResolver#findPage(String)
   * @since   1.2.1
   */
  public SearchPageResult findPage(String query)
    throws  IllegalArgumentException
  {
    SearchPageResult searchPageResult = this.getFromFindPageCache(null, query, (String)null);
    if (searchPageResult == null) {
      searchPageResult = super.getDecoratedResolver().findPage(query);
      this.putInFindPageCache(null, query, (String)null, searchPageResult);
    }
    return searchPageResult;
  }

  /**
   * @see SubjectResolver#findAll(String, Set)
   */
  public Set<Subject> findAll(String query, Set<Source> sources)
      throws IllegalArgumentException {

    //search all
    if (GrouperUtil.length(sources) == 0) {
      return findAll(query);
    }

    Set<Subject> subjects = this.getFromFindAllCache(null, query, sources);
    if (subjects == null) {
      subjects = super.getDecoratedResolver().findAll(query, sources);
      this.putInFindAllCache(null, query, sources, subjects);
    }
    return subjects;

  }

  /**
   * @see SubjectResolver#findPage(String)
   */
  public SearchPageResult findPage(String query, Set<Source> sources)
      throws SourceUnavailableException {
    
    //search all
    if (GrouperUtil.length(sources) == 0) {
      return findPage(query);
    }
    
    SearchPageResult searchPageResult = this.getFromFindPageCache(null, query, sources);
    if (searchPageResult == null) {
      searchPageResult = super.getDecoratedResolver().findPage(query, sources);
      this.putInFindPageCache(null, query, sources, searchPageResult);
    }
    return searchPageResult;
  }

  /**
   * @see     SubjectResolver#findPage(String, String)
   * @since   2.0.2
   */
  public SearchPageResult findPage(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    SearchPageResult subjects = this.getFromFindPageCache(null, query, source);
    if (subjects == null) {
      subjects = super.getDecoratedResolver().findPage(query, source);
      this.putInFindPageCache(null, query, source, subjects);
    }
    return subjects;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.subj.SubjectResolver#findAllInStem(java.lang.String, java.lang.String)
   */
  public SearchPageResult findPageInStem(String stemName, String query)
      throws IllegalArgumentException {
    return findPageInStem(stemName, query, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.subj.SubjectResolver#findPageInStem(java.lang.String, java.lang.String, Set)
   */
  @Override
  public SearchPageResult findPageInStem(String stemName, String query, Set<Source> sources)
      throws IllegalArgumentException {
  
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    if (LOG.isDebugEnabled()) {
      debugMap.put("operation", "findPageInStem");
      debugMap.put("stemName", stemName);
      debugMap.put("query", query);
      debugMap.put("sources", GrouperUtil.toStringForLog(sources));
    }
  
    SearchPageResult subjects = this.getFromFindPageCache(stemName, query, sources);
    
    //TODO do this caching better... need to clear when group changes???
    //for now dont cache if finding in stem name
    if (subjects == null || !StringUtils.isBlank(stemName)) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("foundInCache", Boolean.FALSE);
      }
      subjects = super.getDecoratedResolver().findPageInStem(stemName, query, sources);
      this.putInFindPageCache(stemName, query, sources, subjects);
    } else {
      if (LOG.isDebugEnabled()) {
        debugMap.put("foundInCache", Boolean.TRUE);
      }
  
    }
    if (LOG.isDebugEnabled()) {
      debugMap.put("resultSize", GrouperUtil.length(subjects.getResults()));
      LOG.debug(GrouperUtil.mapToString(debugMap));
    }
    return subjects;
  }

  /**
   * Retrieve set of subjects from cache for <code>findPage(...)</code>.
   * @return  Cached set of subjects or null.
   * @since   2.0.2
   */
  private SearchPageResult getFromFindPageCache(String stemName, String query, Set<Source> sources) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

    if (staticGrouperSession == null) {
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("staticGrouperSession is null");
      }
      return null;
    }
    MultiKey multiKey = sourcesMultiKey(stemName, query, sources);
    
    SearchPageResult searchPageResult = findPageCache.get(multiKey);

    if (LOG.isDebugEnabled()) {
      debugMap.put("query", query);
      if (!StringUtils.isBlank(stemName)) {
        debugMap.put("stemName", stemName);
      }
      String sourceIds = SubjectHelper.sourcesToIdsString(sources);
      if (!StringUtils.isBlank(sourceIds)) {
        debugMap.put("sourceIds", sourceIds);
      }
      debugMap.put("multiKey", multiKey.toString());
      
      if (searchPageResult == null) {
        debugMap.put("searchPageResult", "null");
      } else {
        debugMap.put("isTooManyResults", searchPageResult.isTooManyResults());
        
        if (GrouperUtil.length(searchPageResult.getResults()) > 0) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("firstResult", searchPageResult.getResults().iterator().next().getDescription());
          }
        }
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  
    return searchPageResult;
  }

  /**
   * Put set of subjects into cache for <code>findPage(...)</code>.
   * @since   2.0.2
   */
  private void putInFindPageCache(String stemName, String query, Set<Source> sources, SearchPageResult searchPageResult) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      return;
    }
    MultiKey multiKey = sourcesMultiKey(stemName, query, sources);
    
    if (LOG.isDebugEnabled()) {
      Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
      debugMap.put("query", query);
      if (!StringUtils.isBlank(stemName)) {
        debugMap.put("stemName", stemName);
      }
      String sourceIds = SubjectHelper.sourcesToIdsString(sources);
      if (!StringUtils.isBlank(sourceIds)) {
        debugMap.put("sourceIds", sourceIds);
      }
      debugMap.put("multiKey", multiKey.toString());
      
      debugMap.put("isTooManyResults", searchPageResult.isTooManyResults());
      
      if (GrouperUtil.length(searchPageResult.getResults()) > 0) {
        debugMap.put("firstResult", searchPageResult.getResults().iterator().next().getDescription());
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

    findPageCache.put(multiKey, searchPageResult );
  }

  /**
   * Retrieve set of subjects from cache for <code>findAll(...)</code>.
   * @return  Cached set of subjects or null.
   * @since   2.0.2
   */
  private Set<Subject> getFromFindAllCache(String stemName, String query, Set<Source> sources) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      return null;
    }
    MultiKey multiKey = sourcesMultiKey(stemName, query, sources);
    
    return findAllCache.get(multiKey);
  }

  /**
   * Put set of subjects into cache for <code>findAll(...)</code>.
   * @since   2.0.2
   */
  private void putInFindAllCache(String stemName, String query, Set<Source> sources, Set<Subject> subjects) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      return;
    }
    MultiKey multiKey = sourcesMultiKey(stemName, query, sources);
    findAllCache.put(multiKey, subjects );
  }


  /**
   * get a multikey based on all the arguments
   * @param stemName
   * @param query
   * @param sources
   * @return the multikey
   */
  private MultiKey sourcesMultiKey(String stemName, String query, Set<Source> sources) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      throw new RuntimeException("If there is no grouper session you should not call this method!");
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    if (GrouperUtil.length(sources) == 0) {
      return new MultiKey(new Object[]{grouperSessionSubject.getSourceId(), 
          grouperSessionSubject.getId(), stemName, query, null, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()});
    }
    Object[] sourcesArray = sources.toArray();
    //convert to ids
    for (int i=0;i<sourcesArray.length;i++) {
      sourcesArray[i] = ((Source)sourcesArray[i]).getId();
    }
    Arrays.sort(sourcesArray);
    Object[] fullKey = new Object[sourcesArray.length+5];
    fullKey[0] = grouperSessionSubject.getSourceId();
    fullKey[1] = grouperSessionSubject.getId(); 
    fullKey[2] = stemName;
    fullKey[3] = query;
    fullKey[4] = GrouperSourceAdapter.searchForGroupsWithReadPrivilege();
    System.arraycopy(sourcesArray, 0, fullKey, 5, sourcesArray.length);
    MultiKey multiKey = new MultiKey(fullKey);
    return multiKey;
  }

  /**
   * Retrieve set of subjects from cache for <code>findPage(...)</code>.
   * @return  Cached set of subjects or null.
   * @since   2.0.2
   */
  private SearchPageResult getFromFindPageCache(String stemName, String query, String source) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      return null;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    MultiKey multiKey = new MultiKey(new Object[]{grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), stemName, query, source, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()});
    SearchPageResult searchPageResult = findPageCache.get( multiKey );
    
    if (LOG.isDebugEnabled()) {
      Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
      debugMap.put("query", query);
      if (!StringUtils.isBlank(stemName)) {
        debugMap.put("stemName", stemName);
      }

      if (!StringUtils.isBlank(source)) {
        debugMap.put("source", source);
      }
      debugMap.put("multiKey", multiKey.toString());
      
      if (searchPageResult == null) {
        debugMap.put("searchPageResult", "null");
      } else {
        debugMap.put("isTooManyResults", searchPageResult.isTooManyResults());
        
        if (GrouperUtil.length(searchPageResult.getResults()) > 0) {
          debugMap.put("firstResult", searchPageResult.getResults().iterator().next().getDescription());
        }
      }      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

    
    return searchPageResult;
  }

  /**
   * Put set of subjects into cache for <code>findAll(...)</code>.
   * @since   2.0.2
   */
  private void putInFindPageCache(String stemName, String query, String source, SearchPageResult searchPageResult) {
    GrouperSession staticGrouperSession = GrouperSourceAdapter.internal_getSessionOrRootForSubjectFinder();
    if (staticGrouperSession == null) {
      return;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    MultiKey multiKey = new MultiKey(new Object[]{grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), stemName, query, source, GrouperSourceAdapter.searchForGroupsWithReadPrivilege()});
    
    if (LOG.isDebugEnabled()) {
      Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
      debugMap.put("query", query);
      if (!StringUtils.isBlank(stemName)) {
        debugMap.put("stemName", stemName);
      }

      if (!StringUtils.isBlank(source)) {
        debugMap.put("source", source);
      }
      debugMap.put("multiKey", multiKey.toString());
      
      debugMap.put("isTooManyResults", searchPageResult.isTooManyResults());
      
      if (GrouperUtil.length(searchPageResult.getResults()) > 0) {
        debugMap.put("firstResult", searchPageResult.getResults().iterator().next().getDescription());
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

    findPageCache.put( multiKey, searchPageResult );
  }

  /**
   * @see SubjectResolver#findByIdentifiers(Collection)
   */
  public Map<String, Subject> findByIdentifiers(Collection<String> identifiers)
      throws IllegalArgumentException {
    
    Map<String, Subject> result = new HashMap<String, Subject>();
    
    Set<String> identifiersNotFoundInCache = new HashSet<String>();
    
    //lets get from cache
    for (String identifier : identifiers) {
      Subject subject = this.getFromFindByIdentifierCache(identifier, null);
      if (subject == null) {
        //if not found, batch these up
        identifiersNotFoundInCache.add(identifier);
      } else {
        result.put(identifier, subject);
      }
    }

    //if not everything in cache, get the batch
    if (GrouperUtil.length(identifiersNotFoundInCache) > 0) {
      
      Map<String, Subject> nonCachedResult = super.getDecoratedResolver().findByIdentifiers(identifiersNotFoundInCache);
      
      for (String identifier : nonCachedResult.keySet()) {

        //put each of these in the cache
        this.putInFindByIdentifierCache(identifier, nonCachedResult.get(identifier));
        
      }
      
      result.putAll(nonCachedResult);
      
    }
    return result;
  }

  /**
   * @see SubjectResolver#findByIdentifiers(Collection, String)
   */
  public Map<String, Subject> findByIdentifiers(Collection<String> identifiers, String source)
      throws IllegalArgumentException, SourceUnavailableException {

    Map<String, Subject> result = new HashMap<String, Subject>();
    
    Set<String> identifiersNotFoundInCache = new HashSet<String>();
    
    //lets get from cache
    for (String identifier : identifiers) {
      Subject subject = this.getFromFindByIdentifierCache(identifier, source);
      if (subject == null) {
        //if not found, batch these up
        identifiersNotFoundInCache.add(identifier);
      } else {
        result.put(identifier, subject);
      }
    }

    //if not everything in cache, get the batch
    if (GrouperUtil.length(identifiersNotFoundInCache) > 0) {
      
      Map<String, Subject> nonCachedResult = super.getDecoratedResolver().findByIdentifiers(identifiersNotFoundInCache, source);
      
      for (String identifier : nonCachedResult.keySet()) {

        //put each of these in the cache
        this.putInFindByIdentifierCache(identifier, nonCachedResult.get(identifier));
        
      }
      
      result.putAll(nonCachedResult);
      
    }
    return result;

  }

  /**
   * @see SubjectResolver#findByIds(Collection)
   */
  public Map<String, Subject> findByIds(Collection<String> ids)
      throws IllegalArgumentException {
    Map<String, Subject> result = new HashMap<String, Subject>();
    
    Set<String> idsNotFoundInCache = new HashSet<String>();
    
    //lets get from cache
    for (String id : ids) {
      Subject subject = this.getFromFindCache(id, null);
      if (subject == null) {
        //if not found, batch these up
        idsNotFoundInCache.add(id);
      } else {
        result.put(id, subject);
      }
    }

    //if not everything in cache, get the batch
    if (GrouperUtil.length(idsNotFoundInCache) > 0) {

      Map<String, Subject> nonCachedResult = super.getDecoratedResolver().findByIds(idsNotFoundInCache);

      for (Subject subject : nonCachedResult.values()) {

        //put each of these in the cache
        this.putInFindCache(subject);

      }

      result.putAll(nonCachedResult);

    }
    return result;

  }

  /**
   * @see SubjectResolver#findByIds(Collection, String)
   */
  public Map<String, Subject> findByIds(Collection<String> ids, String source)
      throws IllegalArgumentException, SourceUnavailableException {

    Map<String, Subject> result = new HashMap<String, Subject>();
    
    Set<String> idsNotFoundInCache = new HashSet<String>();
    
    //lets get from cache
    for (String id : ids) {
      Subject subject = this.getFromFindCache(id, source);
      if (subject == null) {
        //if not found, batch these up
        idsNotFoundInCache.add(id);
      } else {
        result.put(id, subject);
      }
    }

    //if not everything in cache, get the batch
    if (GrouperUtil.length(idsNotFoundInCache) > 0) {

      Map<String, Subject> nonCachedResult = super.getDecoratedResolver().findByIds(idsNotFoundInCache, source);

      for (Subject subject : nonCachedResult.values()) {

        //put each of these in the cache
        this.putInFindCache(subject);

      }

      result.putAll(nonCachedResult);

    }
    return result;


    
  }
  
  /**
   * @see SubjectResolver#findByIdsOrIdentifiers(Collection)
   */
  public Map<String, Subject> findByIdsOrIdentifiers(Collection<String> idsOrIdentifiers)
      throws IllegalArgumentException {

    Map<String, Subject> result = new HashMap<String, Subject>();

    Set<String> idsOrIdentifiersNotFoundInCache = new HashSet<String>();

    //lets get from cache
    for (String idOrIdentifier : idsOrIdentifiers) {
      Subject subject = this.getFromFindByIdOrIdentifierCache(idOrIdentifier, null);
      if (subject == null) {
        //if not found, batch these up
        idsOrIdentifiersNotFoundInCache.add(idOrIdentifier);
      } else {
        result.put(idOrIdentifier, subject);
      }
    }

    //if not everything in cache, get the batch
    if (GrouperUtil.length(idsOrIdentifiersNotFoundInCache) > 0) {

      Map<String, Subject> nonCachedResult = super.getDecoratedResolver().findByIdsOrIdentifiers(idsOrIdentifiersNotFoundInCache);

      for (String idOrIdentifier : nonCachedResult.keySet()) {

        //put each of these in the cache
        this.putInFindByIdOrIdentifierCache(idOrIdentifier, nonCachedResult.get(idOrIdentifier));

      }

      result.putAll(nonCachedResult);

    }
    return result;

    
  }
  
  /**
   * @see SubjectResolver#findByIdsOrIdentifiers(Collection, String)
   */
  public Map<String, Subject> findByIdsOrIdentifiers(Collection<String> idsOrIdentifiers, String source)
      throws IllegalArgumentException, SourceUnavailableException {

    Map<String, Subject> result = new HashMap<String, Subject>();

    Set<String> idsOrIdentifiersNotFoundInCache = new HashSet<String>();

    //lets get from cache
    for (String idOrIdentifier : idsOrIdentifiers) {
      Subject subject = this.getFromFindByIdOrIdentifierCache(idOrIdentifier, source);
      if (subject == null) {
        //if not found, batch these up
        idsOrIdentifiersNotFoundInCache.add(idOrIdentifier);
      } else {
        result.put(idOrIdentifier, subject);
      }
    }

    //if not everything in cache, get the batch
    if (GrouperUtil.length(idsOrIdentifiersNotFoundInCache) > 0) {

      Map<String, Subject> nonCachedResult = super.getDecoratedResolver().findByIdsOrIdentifiers(idsOrIdentifiersNotFoundInCache, source);

      for (String idOrIdentifier : nonCachedResult.keySet()) {

        //put each of these in the cache
        this.putInFindByIdOrIdentifierCache(idOrIdentifier, nonCachedResult.get(idOrIdentifier));

      }

      result.putAll(nonCachedResult);

    }
    return result;

  }

}

