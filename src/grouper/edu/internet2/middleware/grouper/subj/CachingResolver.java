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
import  edu.internet2.middleware.grouper.cache.CacheStats;
import  edu.internet2.middleware.grouper.cache.EhcacheController;
import  edu.internet2.middleware.subject.Source;
import  edu.internet2.middleware.subject.SourceUnavailableException;
import  edu.internet2.middleware.subject.Subject;
import  edu.internet2.middleware.subject.SubjectNotFoundException;
import  edu.internet2.middleware.subject.SubjectNotUniqueException;
import  java.util.Set;
import  net.sf.ehcache.Element;
import  org.apache.commons.collections.keyvalue.MultiKey;


/**
 * Decorator that provides caching for {@link SubjectResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CachingResolver.java,v 1.8 2008-08-26 21:11:51 mchyzer Exp $
 * @since   1.2.1
 */
public class CachingResolver extends SubjectResolverDecorator {


  public static final String            CACHE_FIND              = CachingResolver.class.getName() + ".Find";
  public static final String            CACHE_FINDALL           = CachingResolver.class.getName() + ".FindAll";
  public static final String            CACHE_FINDBYIDENTIFIER  = CachingResolver.class.getName() + ".FindByIdentifier";
  private             EhcacheController cc;

  /**
   * flush the cache (e.g. for testing)
   */
  public void flushCache() {
    this.cc.flushCache();
    super.getDecoratedResolver().flushCache();
  }

  /**
   * @since   1.2.1
   */
  public CachingResolver(SubjectResolver resolver) {
    super(resolver);
    this.cc = new EhcacheController();
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
    Subject subj = this.getFromFindCache(id, null, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().find(id);
    }
    // TODO 20070816  am i performing excessive puts by place this statement here rather
    //                than in the if clause?
    this.putInFindCache(subj);
    return subj;
  }

  /**
   * @see     SubjectResolver#find(String, String)
   * @since   1.2.1
   */
  public Subject find(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindCache(id, type, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().find(id, type);
    }
    this.putInFindCache(subj);
    return subj;
  }

  /**
   * @see     SubjectResolver#find(String, String, String)
   * @since   1.2.1
   */
  public Subject find(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindCache(id, type, source);
    if (subj == null) {
      subj = super.getDecoratedResolver().find(id, type, source);
    }
    this.putInFindCache(subj);
    return subj;
  }

  /**
   * @see     SubjectResolver#findAll(String)
   * @since   1.2.1
   */
  public Set<Subject> findAll(String query)
    throws  IllegalArgumentException
  {
    Set<Subject> subjects = this.getFromFindAllCache(query, null);
    if (subjects == null) {
      subjects = super.getDecoratedResolver().findAll(query);
    }
    this.putInFindAllCache(query, null, subjects);
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
    Set<Subject> subjects = this.getFromFindAllCache(query, source);
    if (subjects == null) {
      subjects = super.getDecoratedResolver().findAll(query, source);
    }
    this.putInFindAllCache(query, source, subjects);
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
    Subject subj = this.getFromFindByIdentifierCache(id, null, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().findByIdentifier(id);
    }
    this.putInFindByIdentifierCache(id, subj);
    return subj;
  }            

  /**
   * @see     SubjectResolver#findByIdentifier(String, String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindByIdentifierCache(id, type, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().findByIdentifier(id, type);
    }
    this.putInFindByIdentifierCache(id, subj);
    return subj;
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String, String, String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindByIdentifierCache(id, type, source);
    if (subj == null) {
      subj = super.getDecoratedResolver().findByIdentifier(id, type, source);
    }
    this.putInFindByIdentifierCache(id, subj);
    return subj;
  }

  /**
   * Retrieve set of subjects from cache for <code>findAll(...)</code>.
   * @return  Cached set of subjects or null.
   * @since   1.2.1
   */
  private Set<Subject> getFromFindAllCache(String query, String source) {
    Element el = this.cc.getCache(CACHE_FINDALL).get( new MultiKey(query, source) );
    if (el != null) {
      return (Set<Subject>) el.getObjectValue();
    }
    return null;
  }

  /**
   * Retrieve subject from cache for <code>findByIdentifier(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindByIdentifierCache(String id, String type, String source) {
    // TODO 20070807 DRY w/ getFromFindCache(String, String, String)
    Element el = this.cc.getCache(CACHE_FINDBYIDENTIFIER).get( new MultiKey(id, type, source) );
    if (el != null) {
      return (Subject) el.getObjectValue();    
    }
    return null;
  }

  /**
   * Retrieve subject from cache for <code>find(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindCache(String id, String type, String source) {
    // TODO 20070807 DRY w/ getFromFindByIdentifierCache(String, String, String)
    Element el = this.cc.getCache(CACHE_FIND).get( new MultiKey(id, type, source) );
    if (el != null) {
      return (Subject) el.getObjectValue();    
    }
    return null;
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
   * @see     SubjectResolver#getSources(String)
   * @since   1.2.1
   */
  public Set<Source> getSources(String subjectType) 
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getSources(subjectType);
  }

  /**
   * @return  ehcache statistics for <i>cache</i>.
   * @since   1.2.1
   */
  public CacheStats getStats(String cache) {
    return this.cc.getStats(cache);
  }

  /**
   * Put set of subjects into cache for <code>findAll(...)</code>.
   * @since   1.2.1
   */
  private void putInFindAllCache(String query, String source, Set<Subject> subjects) {
    this.cc.getCache(CACHE_FINDALL).put( new Element( new MultiKey(query, source), subjects ) );
  }

  /**
   * Put subject into cache for <code>findByIdentifier(...)</code>.
   * @since   1.2.1
   */
  private void putInFindByIdentifierCache(String idfr, Subject subj) {
    this.cc.getCache(CACHE_FINDBYIDENTIFIER).put( 
      new Element( 
        new MultiKey(idfr, null, null), subj  
      )
    );
    this.cc.getCache(CACHE_FINDBYIDENTIFIER).put( 
      new Element( 
        new MultiKey( idfr, subj.getType().getName(), null ), subj
      )
    );
    this.cc.getCache(CACHE_FINDBYIDENTIFIER).put( 
      new Element(
        new MultiKey( idfr, subj.getType().getName(), subj.getSource().getId() ), subj
      )
    );
    this.putInFindCache(subj);
  }

  /**
   * Put subject into cache for <code>find(...)</code>.
   * @since   1.2.1
   */
  private void putInFindCache(Subject subj) {
    this.cc.getCache(CACHE_FIND).put( 
      new Element( 
        new MultiKey( subj.getId(), null, null ), subj  
      )
    );
    this.cc.getCache(CACHE_FIND).put( 
      new Element( 
        new MultiKey( subj.getId(), subj.getType().getName(), null ), subj
      )
    );
    this.cc.getCache(CACHE_FIND).put( 
      new Element(
        new MultiKey( subj.getId(), subj.getType().getName(), subj.getSource().getId() ), subj
      )
    );
    // TODO 20070807 also put in "findByIdentifier(...)" cache?
  }

}

