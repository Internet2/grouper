package edu.internet2.middleware.grouper.log;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperLoggingDynamicConfigTest extends GrouperTest {

  
  public void testLogging() {
    
    Log LOG = GrouperUtil.getLog(GrouperLoggingDynamicConfigTest.class);
    
    assertTrue(LOG.isErrorEnabled());
    assertTrue(LOG.isWarnEnabled());
    assertTrue(!LOG.isInfoEnabled());
    assertTrue(!LOG.isDebugEnabled());

//  # {valueType: "string", formElement: "dropdown", optionValues: ["off", "fatal", "error", "warn", "info", "debug", "trace", "all"] }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.logger.myLogger.name", GrouperLoggingDynamicConfigTest.class.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.logger.myLogger.level", "debug");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.logging.dynamicUpdates.checkAfterSeconds", "10");
    
    GrouperUtil.sleep(70000);
    
    assertTrue(LOG.isErrorEnabled());
    assertTrue(LOG.isWarnEnabled());
    assertTrue(LOG.isInfoEnabled());
    assertTrue(LOG.isDebugEnabled());

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.logger.myLogger2.name", GrouperLoggingDynamicConfigTest.class.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.logger.myLogger2.level", "info");

    GrouperUtil.sleep(12000);

    assertTrue(LOG.isErrorEnabled());
    assertTrue(LOG.isWarnEnabled());
    assertTrue(LOG.isInfoEnabled());
    assertTrue(LOG.isDebugEnabled());

    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.logger.myLogger.name");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.logger.myLogger.level");

    GrouperUtil.sleep(12000);

    assertTrue(LOG.isErrorEnabled());
    assertTrue(LOG.isWarnEnabled());
    assertTrue(LOG.isInfoEnabled());
    assertTrue(!LOG.isDebugEnabled());

    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.logger.myLogger2.name");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.logger.myLogger2.level");

    GrouperUtil.sleep(12000);

    assertTrue(LOG.isErrorEnabled());
    assertTrue(LOG.isWarnEnabled());
    assertTrue(!LOG.isInfoEnabled());
    assertTrue(!LOG.isDebugEnabled());

  }
}

