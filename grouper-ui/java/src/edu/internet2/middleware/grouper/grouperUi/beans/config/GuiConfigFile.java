package edu.internet2.middleware.grouper.grouperUi.beans.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * gui config file bean
 * @author mchyzer
 *
 */
public class GuiConfigFile {

  private ConfigPropertiesCascadeBase configPropertiesCascadeBase;
  
  public ConfigPropertiesCascadeBase getConfigPropertiesCascadeBase() {
    return configPropertiesCascadeBase;
  }
  
  public void setConfigPropertiesCascadeBase(
      ConfigPropertiesCascadeBase configPropertiesCascadeBase) {
    this.configPropertiesCascadeBase = configPropertiesCascadeBase;
  }

  /**
   * find gui property 
   * @return gui config property
   */
  public GuiConfigProperty findGuiConfigProperty(String propertyName, boolean exceptionIfNotFound) {
    for (GuiConfigSection guiConfigSection : GrouperUtil.nonNull(this.guiConfigSections)) {
      for (GuiConfigProperty guiConfigProperty : GrouperUtil.nonNull(guiConfigSection.getGuiConfigProperties())) {
        if (StringUtils.equals(GrouperUtil.stripEnd(guiConfigProperty.getConfigItemMetadata().getKey(), ".elConfig"), 
            GrouperUtil.stripEnd(propertyName, ".elConfig"))) {
          return guiConfigProperty;
        }
      }
    }
    if (exceptionIfNotFound) {
      throw new RuntimeException("Cant find property: '" + propertyName + "'");
    }
    return null;
  }
  
  /**
   * 
   */
  public GuiConfigFile() {
    
  }

  /**
   * config file name
   */
  private ConfigFileName configFileName;

  /**
   * config file name
   * @return the config file name
   */
  public ConfigFileName getConfigFileName() {
    return configFileName;
  }

  /**
   * config file name
   * @param configFileName1
   */
  public void setConfigFileName(ConfigFileName configFileName1) {
    this.configFileName = configFileName1;
  }

  /**
   * gui config sections
   */
  private List<GuiConfigSection> guiConfigSections = new ArrayList<GuiConfigSection>();

  /**
   * 
   * @return gui config sections
   */
  public List<GuiConfigSection> getGuiConfigSections() {
    return this.guiConfigSections;
  }

  /**
   * gui cofnig sections
   * @param guiConfigSections1
   */
  public void setGuiConfigSections(List<GuiConfigSection> guiConfigSections1) {
    this.guiConfigSections = guiConfigSections1;
  }
  
  
  
}
