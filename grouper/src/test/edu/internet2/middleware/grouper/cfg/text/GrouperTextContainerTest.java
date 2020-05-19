package edu.internet2.middleware.grouper.cfg.text;

import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileHierarchy;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import junit.textui.TestRunner;


public class GrouperTextContainerTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperTextContainerTest("testTextOrNull"));
  }
  
  public GrouperTextContainerTest(String name) {
    super(name);
  }

  public void testTextOrNull() {
    
    String value = GrouperTextContainer.textOrNull("error.title");
    assertEquals("Error", value);
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb("grouper.text.en.us.properties");
    grouperConfigHibernate.setConfigKey("error.title");
    grouperConfigHibernate.setConfigValue("Error2");
    grouperConfigHibernate.saveOrUpdate();

    GrouperCacheUtils.clearAllCaches();
    
    value = GrouperTextContainer.textOrNull("error.title");
    assertEquals("Error2", value);
  }

  
  
}
