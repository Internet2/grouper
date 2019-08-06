package edu.internet2.middleware.grouper.grouperUi.beans.config;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ConfigurationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Configure;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * utils for config stuff
 * @author mchyzer
 *
 */
public class ConfigUtils {

  /**
   * 
   */
  protected static Log LOG = LogFactory.getLog(ConfigUtils.class);

  public ConfigUtils() {
  }

  /**
   * return if the edit should continue
   * @return true if ok, false if should stop now
   */
  public static boolean validateConfigEdit(ConfigurationContainer configurationContainer, ConfigFileName configFileName, 
      String propertyNameString, String valueString, boolean isExpressionLanguage, StringBuilder message, GrouperConfigHibernate[] grouperConfigHibernateToReturn) {

    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(null, null, propertyNameString);
    Set<GrouperConfigHibernate> grouperConfigHibernatesEl = GrouperDAOFactory.getFactory().getConfig().findAll(null, null, propertyNameString + ".elConfig");
    
    GrouperConfigHibernate grouperConfigHibernate = null;
    GrouperConfigHibernate grouperConfigHibernateEl = null;
    
    for (GrouperConfigHibernate current : GrouperUtil.nonNull(grouperConfigHibernates)) {
      if (configFileName.getConfigFileName().equals(current.getConfigFileNameDb()) && "INSTITUTION".equals(current.getConfigFileHierarchyDb())) {
        if (grouperConfigHibernate != null) {
          // why are there two???
          LOG.error("Why are there two configs in db with same key and config file???? " + current.getConfigFileNameDb() 
            + ", " + current.getConfigKey() + ", " + current.getConfigValue());
          current.delete();
        }
        grouperConfigHibernate = current;
      }
    }
    
    for (GrouperConfigHibernate current : GrouperUtil.nonNull(grouperConfigHibernatesEl)) {
      if (configFileName.getConfigFileName().equals(current.getConfigFileNameDb()) && "INSTITUTION".equals(current.getConfigFileHierarchyDb())) {
        if (grouperConfigHibernate != null) {
          // why are there two???
          LOG.error("Why are there two configs in db with same key and config file???? " + current.getConfigFileNameDb() 
            + ", " + current.getConfigKey() + ", " + current.getConfigValue());
          current.delete();
        }
        grouperConfigHibernateEl = current;
      }
    }
    
    //why would both be there?
    if (grouperConfigHibernate != null && grouperConfigHibernateEl != null) {
      
      if (isExpressionLanguage) {
        grouperConfigHibernate.delete();
        grouperConfigHibernate = null;
      } else {
        grouperConfigHibernateEl.delete();
        grouperConfigHibernateEl = null;
      }
      
    }
    
    // get down to one grouper config hibernate
    if (grouperConfigHibernate == null) {
      grouperConfigHibernate = grouperConfigHibernateEl;
      grouperConfigHibernateEl = null;
    }

    
    for (ConfigFileName current : ConfigFileName.values()) {
      
      if (current == configFileName) {
        continue;
      }
      
      ConfigPropertiesCascadeBase configPropertiesCascadeBase = current.getConfig();
      
      if (configPropertiesCascadeBase.containsKey(GrouperClientUtils.stripEnd(propertyNameString, ".elConfig"))) {
        
        configurationContainer.setCurrentConfigPropertyName(propertyNameString);
        configurationContainer.setCurrentConfigFileName(current.getConfigFileName());
        
        message.append(TextContainer.retrieveFromRequest().getText().get("configurationFilesPropertyExistsInAnother")).append("<br />");
        
      }
      
    }

    
    return true;
  }  

}
