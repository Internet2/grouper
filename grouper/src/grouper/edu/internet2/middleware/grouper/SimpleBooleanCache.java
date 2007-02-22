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

package edu.internet2.middleware.grouper;
import  java.util.HashMap;
import  java.util.Map;

/** 
 * Simple cache implementation for holding boolean values.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SimpleBooleanCache.java,v 1.1 2007-02-22 17:40:30 blair Exp $
 * @since   1.2.0     
 */
class SimpleBooleanCache extends SimpleCache {

  // PUBLIC INSTANCE METHODS //

  /**
   * Retrieve a cached {@link Object}.
   * <p/>
   * @since   1.2.0
   */
  protected Boolean get(Object key) {
    return (Boolean) super.get(key);
  } // protected boolean get(key)

  /**
   * Cache an {@link Object}.
   * <p/>
   * @since   1.2.0
   */
  protected void put(Object key, boolean value) {
    super.put( key, Boolean.valueOf(value) );
  } // protected void put(key, value)

} // class SimpleBooleanCache extends SimpleCache

