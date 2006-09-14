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

/** 
 * A no-caching implementation of {@link SubjectCache}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: NoCacheSubjectCache.java,v 1.1 2006-09-14 20:04:04 blair Exp $
 * @since   1.1.0     
 */
public class NoCacheSubjectCache extends BaseSubjectCache {

  // PUBLIC INSTANCE METHODS //

  /**
   * Do not return a cached {@link Subject}.
   * <p/>
   * @return  null
   * @since   1.1.0
   */
  public Subject get(String id, String type, String source) {
    return null;
  } // public Subject get(id, source, type)

  /**
   * Cache a {@link Subject} without any side-effects.
   * </p>
   * @throws  SubjectCacheException
   * @since   1.1.0
   */
  public void put(String id, String type, String source, Subject subj)
    throws  SubjectCacheException
  {
    // Nothing
  } // public void put(subj)

  /**
   * Remove all cached {@link Subject}s.
   * </p>
   * @throws  SubjectCacheException
   * @since   1.1.0
   */
  public void removeAll() 
    throws  SubjectCacheException
  {
    // Nothing
  } // public void removeAll()

} // public class NoCacheSubjectCache extends BaseSubjectCache

