/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cache;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.ehcache.EhCacheRegionFactory;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperEhcacheRegionFactory extends EhCacheRegionFactory {

  /**
   * 
   */
  private static final long serialVersionUID = -5628907054629045714L;
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperEhcacheRegionFactory.class);

  /**
   * @see org.hibernate.cache.ehcache.EhCacheRegionFactory#start(org.hibernate.boot.spi.SessionFactoryOptions, java.util.Properties)
   */
  @Override
  public void start(SessionFactoryOptions sessionFactorOptions, Properties properties) throws CacheException {
    if (EhcacheController.ehcacheController().isConfiguredViaProperties() 
        && GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.ehcache.useGrouperEhcacheRegionFactoryForHibernate", true)) {
      if ( manager != null ) {
          LOG.warn("Attempting to start ehcache region facotry that has already been started");
          return;
      }

      try {
        manager = EhcacheController.ehcacheController().getCacheManager();
        mbeanRegistrationHelper.registerMBean( manager, properties );
      } catch (net.sf.ehcache.CacheException e) {
        throw new RuntimeException("Cache exception", e);
      }
    } else {
      super.start(sessionFactorOptions, properties);
    }
  }

  /**
   * @see org.hibernate.cache.ehcache.EhCacheRegionFactory#stop()
   */
  @Override
  public void stop() {
    if (EhcacheController.ehcacheController().isConfiguredViaProperties()) {
      
    } else {
      super.stop();
    }
  }

  
  
}
