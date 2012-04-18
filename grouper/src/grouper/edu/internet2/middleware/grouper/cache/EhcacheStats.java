/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
import  net.sf.ehcache.Statistics;


/**
 * Wrapper around ehcache <i>Statistics</i> class.
 * @author  blair christensen.
 * @version $Id: EhcacheStats.java,v 1.2 2007-08-27 15:53:52 blair Exp $
 * @since   1.2.1
 */
public class EhcacheStats implements CacheStats {


  private Statistics stats;



  /**
   * Instantiate new <i>EhcacheStats</i> object.
   * @throws  IllegalArgumentException if <i>stats</i> is null.
   * @since   1.2.1
   */
  public EhcacheStats(Statistics stats) 
    throws  IllegalArgumentException
  {
    if (stats == null) { // TODO 20070809 ParameterHelper
      throw new IllegalArgumentException("null Statistics");
    }
    this.stats = stats;
  }



  /**
   * @return  Number of cache hits.
   * @since   1.2.1
   */
  public long getHits() {
    return this.stats.getCacheHits();
  }

  /**
   * @return  Number of cache misses.
   * @since   1.2.1
   */
  public long getMisses() {
    return this.stats.getCacheMisses();
  }

  /**
   * @return  Number of objects in cache.
   * @since   1.2.1
   */
  public long getSize() {
    return this.stats.getObjectCount();
  }

}

