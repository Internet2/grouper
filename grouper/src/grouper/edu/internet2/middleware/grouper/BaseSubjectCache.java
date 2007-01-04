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
 * Base implementation of {@link SubjectCache}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: BaseSubjectCache.java,v 1.2 2007-01-04 17:17:45 blair Exp $
 * @since   1.1.0     
 */
public abstract class BaseSubjectCache implements SubjectCache {

  // CONSTRUCTORS //
  
  // @since   1.1.0
  protected BaseSubjectCache() {
    super();
  } // protected BaseSubjectCache()

 
  // PUBLIC CLASS METHODS //

  /**
   * @param   klass   Name of the implementing class to return.
   * @return  A {@link SubjectCache} implementation.
   * @throws  GrouperRuntimeException
   * @since   1.1.0
  , Subject subj */
  public static SubjectCache getCache(String klass) 
    throws  GrouperRuntimeException
  {
    return (SubjectCache) U.realizeInterface(klass);
  } // public static SubjectCache getCache(klass)


  // PUBLIC ABSTRACT INSTANCE METHODS //

  /**
   * Retrieve a cached {@link Subject}.
   * <p/>
   * @return  A {@link Subject} or null.
   * @since   1.1.0
   */
  public abstract Subject get(String id, String type, String source);

  /**
   * Cache a {@link Subject}.
   * </p>
   * @throws  SubjectCacheException
   * @since   1.1.0
   */
  public abstract void put(String id, String type, String source, Subject subj) throws SubjectCacheException;

  /**
   * Remove all cached {@link Subject}s.
   * </p>
   * @throws  SubjectCacheException
   * @since   1.1.0
   */
  public abstract void removeAll() throws SubjectCacheException;

} // public class BaseSubjectCache implements SubjectCache

