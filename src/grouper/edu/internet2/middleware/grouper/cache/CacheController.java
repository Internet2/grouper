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

package edu.internet2.middleware.grouper.cache;
import  edu.internet2.middleware.grouper.Group;
import  edu.internet2.middleware.grouper.Privilege;
import  edu.internet2.middleware.grouper.SubjectFinder;
import  edu.internet2.middleware.grouper.UnableToPerformException;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Set;


/**
 * Interface for common cache operations.
 * @author  blair christensen.
 * @version $Id: CacheController.java,v 1.1 2007-08-24 14:18:15 blair Exp $
 * @since   @HEAD@
 */
public interface CacheController {

 
  /**
   * Flush all caches.
   * @since   @HEAD@
   */
  void flushCache();

  /**
   * @return  ehcache statistics for <i>cache</i>.
   * @since   @HEAD@
   */
  CacheStats getStats(String cache);

  /** 
   * Initialize privilege cache.
   * @since   @HEAD@
   */
  void initialize();
  
}

