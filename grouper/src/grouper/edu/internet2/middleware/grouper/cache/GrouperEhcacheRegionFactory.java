/**
 * Copyright 2018 Internet2
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
 */

package edu.internet2.middleware.grouper.cache;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.ehcache.internal.EhcacheRegionFactory;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import net.sf.ehcache.CacheManager;


/**
 *
 */
public class GrouperEhcacheRegionFactory extends EhcacheRegionFactory {

  /**
   * 
   */
  private static final long serialVersionUID = -5628907054629045714L;
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperEhcacheRegionFactory.class);
  
  /**
   * @see org.hibernate.cache.ehcache.internal.EhcacheRegionFactory#resolveCacheManager(org.hibernate.boot.spi.SessionFactoryOptions, java.util.Map)
   */
  @Override
  public CacheManager resolveCacheManager(SessionFactoryOptions settings, Map properties) throws CacheException {
    if (EhcacheController.ehcacheController().isConfiguredViaProperties() 
        && GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.ehcache.useGrouperEhcacheRegionFactoryForHibernate", true)) {
      if ( super.getCacheManager() != null ) {
          LOG.warn("Attempting to start ehcache region facotry that has already been started");
          return super.getCacheManager();
      }

      try {
        return EhcacheController.ehcacheController().getCacheManager();
      } catch (net.sf.ehcache.CacheException e) {
        throw new RuntimeException("Cache exception", e);
      }
    } else {
      return super.resolveCacheManager(settings, properties);
    }
  }
  
  /**
   * @see org.hibernate.cache.ehcache.internal.EhcacheRegionFactory#releaseFromUse()
   */
  @Override
  public void releaseFromUse() {
    if (EhcacheController.ehcacheController().isConfiguredViaProperties()) {
      
    } else {
      super.releaseFromUse();
    }
  }
}