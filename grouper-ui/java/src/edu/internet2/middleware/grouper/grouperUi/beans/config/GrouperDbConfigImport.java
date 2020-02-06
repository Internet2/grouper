/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Configure;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperDbConfigImport {

  /**
   * 
   */
  public GrouperDbConfigImport() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    
  }

  /**
   * which config file
   */
  private String configFilePath;
  
  /**
   * 
   * @param theConfigFilePath
   * @return this for chaining
   */
  public GrouperDbConfigImport configFilePath(String theConfigFilePath) {
    this.configFilePath = theConfigFilePath;
    return this;
  }
  
  /**
   * @return message
   */
  public String store() {
    GrouperDbConfig.seeIfAllowed();
    if (this.configFilePath == null) {
      throw new RuntimeException("Config file is required!");
    }
    StringBuilder message = new StringBuilder();
    
    File configFile = new File(this.configFilePath);
    
    if (!configFile.exists() || !configFile.isFile()) {
      throw new RuntimeException("File doesnt exist or is not a file! (maybe give absolute path?) '" + configFile.getAbsolutePath() + "'");
    }
    
    ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFile.getName(), true);
    
    Properties propertiesToImport = new Properties();
    
    // load properties from file
    Reader reader = null;
    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(configFile);
      reader = new InputStreamReader(fileInputStream);
      
      propertiesToImport.load(reader);

    } catch (Exception e) {
      throw new RuntimeException("Cant process config import: '" + configFile.getAbsolutePath() + "'", e);
    } finally {
      GrouperUtil.closeQuietly(reader);
      GrouperUtil.closeQuietly(fileInputStream);
    }

    UiV2Configure.configurationfileImportSubmitHelper(message, configFileName, propertiesToImport, false);
    return message.toString();

  }
  
}
