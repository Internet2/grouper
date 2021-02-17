/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * metadata about a config file
 */
public class ConfigFileMetadata {

  /**
   * state of config file
   */
  private static enum ConfigFileState {
    
    /** top of file */
    LICENSE,
    
    /** comments of section */
    SECTION_COMMENTS,
    
    /** comment of property */
    IN_SECTION,
    
    /** in section somewhere */
    BLANK_LINE_IN_SECTION,
    
    /** comment of property */
    PROPERTY_COMMENT,
    
    /** comment of property */
    PROPERTY_METADATA,
    
    /** property line */
    PROPERTY_LINE;
    
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ConfigFileMetadata.class);

  /**
   * matches default value commented out
   * #configuration.autocreate.group.name.0 = $$grouper.rootStemForBuiltinObjects$$:uiUsers
   */
  private static Pattern defaultValuePattern = Pattern.compile("^#\\s*([^\\s]+)\\s*=\\s*(.*)\\s*$");
  
  /**
   * if the config is 
   */
  private boolean validConfig = false;
  
  /**
   * if the config is 
   * @return the validConfig
   */
  public boolean isValidConfig() {
    return this.validConfig;
  }

  /**
   * if the config is 
   * @param validConfig the validConfig to set
   */
  public void setValidConfig(boolean validConfig) {
    this.validConfig = validConfig;
  }

  /**
   * generate a config file metadata from the contents of a config file
   * @param configFileName
   * @param configFileContents
   * @return the config file metadata 
   */
  public static ConfigFileMetadata generateMetadataForConfigFile(ConfigFileName configFileName, String configFileContents) {
    
    ConfigFileMetadata configFileMetadata = new ConfigFileMetadata();
    configFileMetadata.setConfigFileName(configFileName);
    configFileMetadata.setConfigSectionMetadataList(new ArrayList<ConfigSectionMetadata>());
    
    //lets get a list of lines
    List<String> configFileLinesList = GrouperUtil.splitFileLines(configFileContents);

    ConfigFileState configFileState = ConfigFileState.LICENSE;
    
    ConfigSectionMetadata configSectionMetadata = null;
    
    ConfigItemMetadata configItemMetadata = null;

    StringBuilder comment = null;
    StringBuilder rawMetadataJson = null;
    String originalConfigFileLine = null;
       
    boolean localConfigValid = true;
    
    for (int i=0;i<GrouperUtil.length(configFileLinesList);i++) {
      
      try {
        String configFileLine = configFileLinesList.get(i);
        originalConfigFileLine = configFileLine;
        
        // can trim all lines
        configFileLine = configFileLine.trim();
  
        boolean configLineIsBlank = StringUtils.isBlank(configFileLine);
        boolean configLineIsComment = configFileLine.startsWith("#");
        boolean configLineSectionStartEnd = configFileLine.startsWith("##########");

        
        // if start of a json line
        boolean configLineIsMetadataStart = false;
        if (configLineIsComment) {
          String configFileLineTemp = configFileLine;
          
          while(configFileLineTemp.startsWith("#")) {
            configFileLineTemp = configFileLineTemp.substring(1);
          }
  
          configFileLineTemp = configFileLineTemp.trim();
          
          configLineIsMetadataStart = configFileLineTemp.startsWith("{");
        }
        
        boolean configLineIsCommentAndNotMetadataStart = configLineIsComment && !configLineIsMetadataStart;
        
        boolean configLineIsProperty = !configLineIsBlank && !configLineIsComment;
        
        Matcher configLineDefaultValueMatcher = defaultValuePattern.matcher(configFileLine);
        
        boolean configLineIsDefaultValue = configLineDefaultValueMatcher.matches();
        
        // do we need to close out comments?
        if (configFileState == ConfigFileState.PROPERTY_COMMENT && (configLineIsBlank || configLineIsMetadataStart || configLineIsProperty || configLineIsDefaultValue)) {
          
          if (configItemMetadata == null) {
            configItemMetadata = new ConfigItemMetadata();
            configSectionMetadata.getConfigItemMetadataList().add(configItemMetadata);
          }
          
          configItemMetadata.setComment(comment.toString());
          comment = null;
        }
        
        // do we need to close out metadata?
        if (configFileState == ConfigFileState.PROPERTY_METADATA && (configLineIsBlank || configLineIsProperty || configLineIsCommentAndNotMetadataStart || configLineIsDefaultValue)) {
          configItemMetadata.setRawMetadataJson(rawMetadataJson.toString());
          try {
            configItemMetadata.processMetadata();
          } catch (Exception e) {
            localConfigValid = false;
            LOG.error("Config file metadata line invalid, " + configFileName + ", line before this line: " + (i+1) + "  '" + originalConfigFileLine + "'.", e);
          }
          
          if (configItemMetadata.isRequired() && StringUtils.isNotBlank(configItemMetadata.getDefaultValue())) {
            throw new RuntimeException("Invalid config file metadata in "+configFileName + ", "
                + "required = true and defaultValue ("+configItemMetadata.getDefaultValue() + ") is not blank. Only one property can be set.");
          }
          
          if (configItemMetadata.isRequired() && StringUtils.isNotBlank(configItemMetadata.getDefaultValueEl())) {
            throw new RuntimeException("Invalid config file metadata in "+configFileName + ", "
                + "required = true and defaultValueEl ("+configItemMetadata.getDefaultValueEl() + ") is not blank. Only one property can be set.");
          }
          
          if (StringUtils.isNotBlank(configItemMetadata.getDefaultValue()) && StringUtils.isNotBlank(configItemMetadata.getDefaultValueEl())) {
            throw new RuntimeException("Invalid config file metadata in "+configFileName + ", "
                + "defaultValue = "+configItemMetadata.getDefaultValue() +" and defaultValueEl ("+configItemMetadata.getDefaultValueEl() + ") is not blank. Only one property can be set.");
          }
          
          if (!configItemMetadata.isReadOnly() && !configItemMetadata.isSaveToDb()) {
            throw new RuntimeException("Invalid config file metadata in "+configFileName + ", "
                + "readOnly = false and saveToDb = false. saveToDb must be true if readOnly is set to false.");
          }
          
          rawMetadataJson = null;
          
          if (configLineIsComment) {
            // this is a line like a commented out property or something, ignore it
            configFileState = ConfigFileState.IN_SECTION;
            
          }
          
        }
  
        if (configLineIsDefaultValue) {

          if (configItemMetadata == null) {
            configItemMetadata = new ConfigItemMetadata();
            configSectionMetadata.getConfigItemMetadataList().add(configItemMetadata);
          }
          if (GrouperUtil.isBlank(configItemMetadata.getKey())) {
            configItemMetadata.setSampleKey(configLineDefaultValueMatcher.group(1));
          }
          configItemMetadata.setSampleValue(configLineDefaultValueMatcher.group(2));
          configItemMetadata = null;
          configFileState = ConfigFileState.IN_SECTION;
          continue;
        }
        
        //this isnt right
        if (configFileState == ConfigFileState.PROPERTY_METADATA && configLineIsMetadataStart) {
          localConfigValid = false;
          LOG.error("Config file line invalid, " + configFileName + ", line: " + (i+1) + " should not be blank or metadata start '" + originalConfigFileLine + "'.");
        }
        
        // if config file line doesnt start with comment?  hmmm
        if (configLineIsBlank) {
          if (configFileState == ConfigFileState.SECTION_COMMENTS) {
            localConfigValid = false;
            LOG.error("Config file line invalid, " + configFileName + ", line: " + (i+1) + " should not be a blank line.");
          }
                  
          continue;
        }
  
        // if we arent in section comments, then if we get to section, start a new section
        if (configFileState != ConfigFileState.SECTION_COMMENTS) {
          if (configLineSectionStartEnd) {
            configFileState = ConfigFileState.SECTION_COMMENTS;
            configSectionMetadata = new ConfigSectionMetadata();
            configFileMetadata.getConfigSectionMetadataList().add(configSectionMetadata);
            comment = new StringBuilder();
            continue;
          }
        }
  
        if (configFileState == ConfigFileState.SECTION_COMMENTS) {
          if (configLineSectionStartEnd) {
            
            //assign the comment
            if (comment.length() > 0) {
              configSectionMetadata.setComment(comment.toString());
            }
            
            // end the section part
            configFileState = ConfigFileState.IN_SECTION;
            comment = null;
            continue;
          }
          
          // lets process the comment
          if (configLineIsComment) {
            
            if (!configFileLine.startsWith("##")) {
              localConfigValid = false;
              LOG.error("Config file line invalid, " + configFileName + ", line: " + (i+1) + " should be a section comment with 2 hashes but was: '" + originalConfigFileLine + "'");
            }
            
            while(configFileLine.startsWith("#")) {
              configFileLine = configFileLine.substring(1);
            }
            
            // trim after comment
            configFileLine = configFileLine.trim();
            
            // see if title
            if (comment.length() == 0 && StringUtils.isBlank(configSectionMetadata.getTitle())) {
              configSectionMetadata.setTitle(configFileLine);
            } else if (comment.length() != 0) {
              // non first line of comment
              comment.append(" ").append(configFileLine);
            } else {
              // first line of comment
              comment.append(configFileLine);
            }
          }
          
          continue;
        }
  
        // still in license
        if (configFileState == ConfigFileState.LICENSE) {
          if (configLineIsProperty) {
            localConfigValid = false;
            LOG.error("Config file line invalid, " + configFileName + ", line: " + (i+1) + " should be in a section but is in the LICENSE part: '" + originalConfigFileLine + "'");
          }
          continue;
        }
        
        //we are not in a section
        if (configFileLine.startsWith("#")) {
          
          configFileLine = configFileLine.substring(1);
          
          //multiple hashes are weird but whatever
          if (configFileLine.startsWith("#")) {
            localConfigValid = false;
            LOG.error("Config file line invalid, " + configFileName + ", line: " + (i+1) + " should be a comment with one hash: '" + originalConfigFileLine + "'");
  
            while(configFileLine.startsWith("#")) {
              configFileLine = configFileLine.substring(1);
            }
            
          }
          configFileLine = configFileLine.trim();

          //if metadata
          if (configLineIsMetadataStart) {
            
            // if starting metadata
            if (configFileState != ConfigFileState.PROPERTY_METADATA) {
              rawMetadataJson = new StringBuilder();
              rawMetadataJson.append(configFileLine);

              configFileState = ConfigFileState.PROPERTY_METADATA;
              
              if (configItemMetadata == null) {
                configItemMetadata = new ConfigItemMetadata();
                configSectionMetadata.getConfigItemMetadataList().add(configItemMetadata);
              }
            } else {
              if (rawMetadataJson.length() > 0) {
                rawMetadataJson.append(" ");
              }
              rawMetadataJson.append(configFileLine);
            }
            continue;
          }
          
          if (configLineIsComment) {
            
            if (configFileState != ConfigFileState.PROPERTY_COMMENT) {
              comment = new StringBuilder();
              comment.append(configFileLine);

              configFileState = ConfigFileState.PROPERTY_COMMENT;
            } else {
              if (comment == null) {
                localConfigValid = false;
                throw new RuntimeException("Cant have whitespace between comments without a property");
              }
              if (comment.length() > 0) {
                comment.append(" ");
              }
              comment.append(configFileLine);
            }
            
            continue;
          }
          
        }
        
        if (configLineIsProperty) {
          int equalsIndex = configFileLine.indexOf('=');
          if (equalsIndex == -1) {
            localConfigValid = false;
            LOG.error("Config file line invalid, " + configFileName + ", line: " + (i+1) + " should be key=value: '" + originalConfigFileLine + "'");
            
          } else {
            String key = StringUtils.trim(configFileLine.substring(0, equalsIndex));

            if (configItemMetadata == null) {
              configItemMetadata = new ConfigItemMetadata();
              configSectionMetadata.getConfigItemMetadataList().add(configItemMetadata);
            }

            configItemMetadata.setKey(key);
            String value = StringUtils.trim(configFileLine.substring(equalsIndex+1, configFileLine.length()));
            configItemMetadata.setValue(value);
          }
          
          // default to string
          if (configItemMetadata.getValueType() == null) {
            configItemMetadata.setValueType(ConfigItemMetadataType.STRING);
          }
          
          configItemMetadata = null;
          configFileState = ConfigFileState.IN_SECTION;
          continue;
        }
      } catch (RuntimeException re) {
        localConfigValid = false;
        GrouperUtil.injectInException(re, "Config file line invalid, " + configFileName + ", line: " + (i+1) + " '" + originalConfigFileLine + "'");
        throw re;
      }
    }
    
    // go through the individual metadata items and whenever we see repeatGroup and repeatCount, expand as many number of times
    // grouping needs to stay intact
    
    for (ConfigSectionMetadata sectionMetadata: configFileMetadata.getConfigSectionMetadataList()) {

      List<ConfigItemMetadata> expandedConfigItemMetadataListPerSection = new ArrayList<ConfigItemMetadata>();
      
      Set<String> repeatGroupsAlreadySeen = new HashSet<String>();
      
      for (ConfigItemMetadata itemMetadata: sectionMetadata.getConfigItemMetadataList()) {
        
        String repeatGroup = itemMetadata.getRepeatGroup();
        
        if (StringUtils.isBlank(repeatGroup)) {
          expandedConfigItemMetadataListPerSection.add(itemMetadata);
          continue;
        }
        
        if (repeatGroupsAlreadySeen.contains(repeatGroup)) {
          continue;
        }
        
        repeatGroupsAlreadySeen.add(repeatGroup);
        
        int repeatCount = itemMetadata.getRepeatCount();
        
        // loop through all metadata in the section looking for that repeat group
        List<ConfigItemMetadata> metadataItemsBelongingToSameRepeatGroup = new ArrayList<ConfigItemMetadata>();
        
        for (ConfigItemMetadata configMetadata: sectionMetadata.getConfigItemMetadataList()) {
          if (StringUtils.equals(configMetadata.getRepeatGroup(), repeatGroup)) {
            metadataItemsBelongingToSameRepeatGroup.add(configMetadata);
          }
        }
        
        for (int i=0; i<repeatCount; i++) {
          
          for (ConfigItemMetadata configMetadata: metadataItemsBelongingToSameRepeatGroup) {
            ConfigItemMetadata copy = configMetadata.clone(i);
            copy.setSubSection(copy.getRepeatGroup()+"."+i);
            copy.setRepeatGroupIndex(i);
            expandedConfigItemMetadataListPerSection.add(copy);
          }
        }
        
      }
      
      sectionMetadata.setConfigItemMetadataList(expandedConfigItemMetadataListPerSection);
      
    }
    
    configFileMetadata.setValidConfig(localConfigValid);
    return configFileMetadata;
    
  }
  
  /**
   * config file name
   */
  private ConfigFileName configFileName;
  
  /**
   * config file name
   * @return the configFileName
   */
  public ConfigFileName getConfigFileName() {
    return this.configFileName;
  }
  
  /**
   * config file name
   * @param configFileName1 the configFileName to set
   */
  public void setConfigFileName(ConfigFileName configFileName1) {
    this.configFileName = configFileName1;
  }

  /**
   * 
   */
  public ConfigFileMetadata() {
  }

  /**
   * list of sections
   */
  private List<ConfigSectionMetadata> configSectionMetadataList = new ArrayList<ConfigSectionMetadata>();
  
  /**
   * list of sections
   * @return the configSectionMetadataList
   */
  public List<ConfigSectionMetadata> getConfigSectionMetadataList() {
    return this.configSectionMetadataList;
  }
  
  /**
   * list of sections
   * @param configSectionMetadataList1 the configSectionMetadataList to set
   */
  public void setConfigSectionMetadataList(
      List<ConfigSectionMetadata> configSectionMetadataList1) {
    this.configSectionMetadataList = configSectionMetadataList1;
  }
  
}
