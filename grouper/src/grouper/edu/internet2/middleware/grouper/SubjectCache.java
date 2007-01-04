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
import  edu.internet2.middleware.subject.*;

/** 
 * Subject Cache interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SubjectCache.java,v 1.12 2007-01-04 17:17:45 blair Exp $
 * @since   1.1.0     
 */
public interface SubjectCache {

  // PUBLIC INSTANCE METHODS //

  /**
   * Retrieve a cached {@link Subject}.
   * <p/>
   * @return  A {@link Subject} or null.
   * @since   1.1.0
   */
  Subject get(String id, String type, String source);

  /**
   * Cache a {@link Subject}.
   * </p>
   * @throws  SubjectCacheException
   * @since   1.1.0
   */
  void put(String id, String type, String source, Subject subj) throws SubjectCacheException;

  /**
   * Remove all cached {@link Subject}s.
   * </p>
   * @throws  SubjectCacheException
   * @since   1.1.0
   */
  void removeAll() throws SubjectCacheException;

} // public interface SubjectCache

