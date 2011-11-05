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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
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
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return null;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    return findAllCache.get( new MultiKey(grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), stemName, query, source) );
  }

  /**
   * Retrieve subject from cache for <code>findByIdentifier(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindByIdentifierCache(String id, String source) {
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return null;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    return findByIdentifierCache.get( new MultiKey(grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), id, source) );
  }

  /**
   * Retrieve subject from cache for <code>find(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindCache(String id, String source) {
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return null;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    return findCache.get(new MultiKey(grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), id, source));
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
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    findAllCache.put( new MultiKey(grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), stemName, query, source), subjects );
  }

  /**
   * Put subject into cache for <code>findByIdentifier(...)</code>.
   * @since   1.2.1
   */
  private void putInFindByIdentifierCache(String idfr, Subject subj) {
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    findByIdentifierCache.put( 
      new MultiKey(grouperSessionSubject.getSourceId(), 
          grouperSessionSubject.getId(), idfr, null), subj  
    );
    findByIdentifierCache.put( 
        new MultiKey(grouperSessionSubject.getSourceId(), 
            grouperSessionSubject.getId(),  idfr, subj.getSource().getId() ), subj
    );
    this.putInFindCache(subj);
  }

  /**
   * Put subject into cache for <code>find(...)</code>.
   * @since   1.2.1
   */
  private void putInFindCache(Subject subj) {
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    findCache.put( 
        new MultiKey(grouperSessionSubject.getSourceId(), 
            grouperSessionSubject.getId(),  subj.getId(), null), subj  
    );
    findCache.put( 
        new MultiKey(grouperSessionSubject.getSourceId(), 
            grouperSessionSubject.getId(),  subj.getId(), subj.getSource().getId() ), subj
    );
  }

  /**
   * Retrieve subject from cache for <code>findByIdentifier(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindByIdOrIdentifierCache(String identifier, String source) {
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return null;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    return findByIdOrIdentifierCache.get( new MultiKey(grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), identifier, source) );
  }

  /**
   * Put subject into cache for <code>findByIdentifier(...)</code>.
   * @since   1.2.1
   */
  private void putInFindByIdOrIdentifierCache(String idfr, Subject subj) {
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    findByIdOrIdentifierCache.put( 
      new MultiKey(grouperSessionSubject.getSourceId(), 
          grouperSessionSubject.getId(), idfr, null), subj  
    );
    findByIdOrIdentifierCache.put( 
        new MultiKey(grouperSessionSubject.getSourceId(), 
            grouperSessionSubject.getId(),  idfr, subj.getSource().getId() ), subj
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
  
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    if (LOG.isDebugEnabled()) {
      debugMap.put("operation", "findAllInStem");
      debugMap.put("stemName", stemName);
      debugMap.put("query", query);
    }
  
    SearchPageResult subjects = this.getFromFindPageCache(stemName, query, (String)null);
    
    //TODO do this caching better... need to clear when group changes???
    //for now dont cache if finding in stem name
    if (subjects == null || !StringUtils.isBlank(stemName)) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("foundInCache", Boolean.FALSE);
      }
      subjects = super.getDecoratedResolver().findPageInStem(stemName, query);
      this.putInFindPageCache(stemName, query, (String)null, subjects);
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
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return null;
    }
    MultiKey multiKey = sourcesMultiKey(stemName, query, sources);
    
    return findPageCache.get(multiKey);
  }

  /**
   * Put set of subjects into cache for <code>findPage(...)</code>.
   * @since   2.0.2
   */
  private void putInFindPageCache(String stemName, String query, Set<Source> sources, SearchPageResult searchPageResult) {
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return;
    }
    MultiKey multiKey = sourcesMultiKey(stemName, query, sources);
    findPageCache.put(multiKey, searchPageResult );
  }

  /**
   * Retrieve set of subjects from cache for <code>findAll(...)</code>.
   * @return  Cached set of subjects or null.
   * @since   2.0.2
   */
  private Set<Subject> getFromFindAllCache(String stemName, String query, Set<Source> sources) {
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
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
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
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
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      throw new RuntimeException("If there is no grouper session you should not call this method!");
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    if (GrouperUtil.length(sources) == 0) {
      return new MultiKey(grouperSessionSubject.getSourceId(), 
          grouperSessionSubject.getId(), stemName, query, null);
    }
    Object[] sourcesArray = sources.toArray();
    //convert to ids
    for (int i=0;i<sourcesArray.length;i++) {
      sourcesArray[i] = ((Source)sourcesArray[i]).getId();
    }
    Arrays.sort(sourcesArray);
    Object[] fullKey = new Object[sourcesArray.length+4];
    fullKey[0] = grouperSessionSubject.getSourceId();
    fullKey[1] = grouperSessionSubject.getId(); 
    fullKey[2] = stemName;
    fullKey[3] = query;
    System.arraycopy(sourcesArray, 0, fullKey, 2, sourcesArray.length);
    MultiKey multiKey = new MultiKey(fullKey);
    return multiKey;
  }

  /**
   * Retrieve set of subjects from cache for <code>findPage(...)</code>.
   * @return  Cached set of subjects or null.
   * @since   2.0.2
   */
  private SearchPageResult getFromFindPageCache(String stemName, String query, String source) {
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return null;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    return findPageCache.get( new MultiKey(grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), stemName, query, source) );
  }

  /**
   * Put set of subjects into cache for <code>findAll(...)</code>.
   * @since   2.0.2
   */
  private void putInFindPageCache(String stemName, String query, String source, SearchPageResult searchPageResult) {
    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession(false);
    if (staticGrouperSession == null) {
      return;
    }
    Subject grouperSessionSubject = staticGrouperSession.getSubject();
    findPageCache.put( new MultiKey(grouperSessionSubject.getSourceId(), 
        grouperSessionSubject.getId(), stemName, query, source), searchPageResult );
  }

}

