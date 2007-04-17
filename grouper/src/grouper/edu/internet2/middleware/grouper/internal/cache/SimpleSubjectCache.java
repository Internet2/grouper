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

package edu.internet2.middleware.grouper.internal.cache;
import  edu.internet2.middleware.subject.Subject;
import  org.apache.commons.collections.keyvalue.MultiKey;

/** 
 * A simple caching implementation of {@link SubjectCache}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SimpleSubjectCache.java,v 1.1 2007-04-17 17:13:26 blair Exp $
 * @since   1.1.0     
 */
public class SimpleSubjectCache extends BaseSubjectCache {

  // PROTECTED INSTANCE VARIABLES //
  protected SimpleCache cache = new SimpleCache();


  // PUBLIC INSTANCE METHODS //

  /**
   * Retrieve a potentially cached {@link Subject}.
   * <p/>
   * @return  A {@link Subject} or null.
   * @since   1.1.0
   */
  public Subject get(String id, String type, String source) {
    MultiKey k = new MultiKey(id, type, source);
    if ( this.cache.containsKey(k) ) {
      return (Subject) this.cache.get(k);
    }
    return null;
  } // public Subject get(id, source, type)

  /**
   * Cache a {@link Subject}.
   * </p>
   * @throws  SubjectCacheException
   * @since   1.1.0
   */
  public void put(String id, String type, String source, Subject subj)
    throws  SubjectCacheException
  {
    // Store the value without any cache flushing
    this.cache.put( new MultiKey(id, type, source), subj );
  } // public void put(o, subj, p, hasPriv)

  /**
   * Remove all cached {@link Subject}s.
   * </p>
   * @throws  SubjectCacheException
   * @since   1.1.0
   */
  public void removeAll() 
    throws  SubjectCacheException
  {
    this.cache.removeAll();
  } // public void removeAll()

} 

