/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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

/** 
 * Simple cache implementation for holding boolean values.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SimpleBooleanCache.java,v 1.1 2007-04-17 17:13:26 blair Exp $
 * @since   1.2.0     
 */
public class SimpleBooleanCache extends SimpleCache {
  // FIXME 20070416 visibility - and methods

  // PUBLIC INSTANCE METHODS //

  // TODO 20070412 merge back into "SimpleCache"?
  
  /**
   * Retrieve a cached {@link Object}.
   * <p/>
   * @since   1.2.0
   */
  public Boolean getBoolean(Object key) {
    return (Boolean) super.get(key);
  }

  /**
   * Cache an {@link Object}.
   * <p/>
   * @since   1.2.0
   */
  public void put(Object key, boolean value) {
    super.put( key, Boolean.valueOf(value) );
  } 

} 

