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
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * Decorator that provides caching for {@link SubjectResolver}.
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
   * cache of multikey, to subject
   */
  static GrouperCache<MultiKey, Subject> findByIdentifierCache = new GrouperCache<MultiKey, Subject>(CachingResolver.class.getName() + ".FindByIdentifier", 5000, false, 30, 120, false);

  /**
   * cache of multikey, to subject
   */
  static GrouperCache<MultiKey, Subject> findByIdOrIdentifierCache = new GrouperCache<MultiKey, Subject>(CachingResolver.class.getName() + ".FindByIdentifier", 5000, false, 30, 120, false);

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
    Set<Subject> subjects = this.getFromFindAllCache(null, query, null);
    if (subjects == null) {
      subjects = super.getDecoratedResolver().findAll(query);
      this.putInFindAllCache(null, query, null, subjects);
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
    return findAllCache.get( new MultiKey(stemName, query, source) );
  }

  /**
   * Retrieve subject from cache for <code>findByIdentifier(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindByIdentifierCache(String id, String source) {
    return findByIdentifierCache.get( new MultiKey(id, source) );
  }

  /**
   * Retrieve subject from cache for <code>find(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindCache(String id, String source) {
    return findCache.get(new MultiKey(id, source));
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
    findAllCache.put( new MultiKey(stemName, query, source), subjects );
  }

  /**
   * Put subject into cache for <code>findByIdentifier(...)</code>.
   * @since   1.2.1
   */
  private void putInFindByIdentifierCache(String idfr, Subject subj) {
    findByIdentifierCache.put( 
      new MultiKey(idfr, null), subj  
    );
    findByIdentifierCache.put( 
        new MultiKey( idfr, subj.getSource().getId() ), subj
    );
    this.putInFindCache(subj);
  }

  /**
   * Put subject into cache for <code>find(...)</code>.
   * @since   1.2.1
   */
  private void putInFindCache(Subject subj) {
    findCache.put( 
        new MultiKey( subj.getId(), null), subj  
    );
    findCache.put( 
        new MultiKey( subj.getId(), subj.getSource().getId() ), subj
    );
  }

  /**
   * Retrieve subject from cache for <code>findByIdentifier(...)</code>.
   * @return  Cached subject or null.
   * @since   1.2.1
   */
  private Subject getFromFindByIdOrIdentifierCache(String identifier, String source) {
    return findByIdOrIdentifierCache.get( new MultiKey(identifier, source) );
  }

  /**
   * Put subject into cache for <code>findByIdentifier(...)</code>.
   * @since   1.2.1
   */
  private void putInFindByIdOrIdentifierCache(String idfr, Subject subj) {
    findByIdOrIdentifierCache.put( 
      new MultiKey(idfr, null), subj  
    );
    findByIdOrIdentifierCache.put( 
        new MultiKey( idfr, subj.getSource().getId() ), subj
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

  public Set<Subject> findAllInStem(String stemName, String query)
      throws IllegalArgumentException {
    // TODO Auto-generated method stub
    return null;
  }

}

