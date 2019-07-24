package edu.internet2.middleware.grouper.grouperUi.beans.config;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;

public class GuiConfigSection {

  /**
   * gui config file
   */
  private GuiConfigFile guiConfigFile;
  
  
  
  
  public GuiConfigFile getGuiConfigFile() {
    return guiConfigFile;
  }

  
  public void setGuiConfigFile(GuiConfigFile guiConfigFile) {
    this.guiConfigFile = guiConfigFile;
  }

  public GuiConfigSection() {
  }

  /**
   * gui config properties
   */
  private List<GuiConfigProperty> guiConfigProperties = new ArrayList<GuiConfigProperty>();
  
  /**
   * gui config properties
   */
  public List<GuiConfigProperty> getGuiConfigProperties() {
    return guiConfigProperties;
  }
  
  /**
   * gui config properties
   */
  public void setGuiConfigProperties(List<GuiConfigProperty> guiConfigProperties) {
    this.guiConfigProperties = guiConfigProperties;
  }

  /**
   * metadata for section
   */
  private ConfigSectionMetadata configSectionMetadata;


  /**
   * metadata for section
   * @return the config section metadata
   */
  public ConfigSectionMetadata getConfigSectionMetadata() {
    return this.configSectionMetadata;
  }

  /**
   * metadata for section
   * @param configSectionMetadata
   */
  public void setConfigSectionMetadata(ConfigSectionMetadata configSectionMetadata) {
    this.configSectionMetadata = configSectionMetadata;
  }
  
  
  
}
