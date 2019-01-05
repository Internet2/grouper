/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.io.File;
import java.util.List;

import org.apache.commons.collections.keyvalue.MultiKey;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class ConfigFileMetadataTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ConfigFileMetadataTest("testParseRealConfigFiles"));
  }
  
  /**
   * @param name
   */
  public ConfigFileMetadataTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testParseRealConfigFiles() {
    
    for (MultiKey configFilePathMultiKey : new MultiKey[] {
        new MultiKey("resource: grouper-loader.base.properties", ConfigFileName.GROUPER_LOADER_PROPERTIES)
        , new MultiKey("resource: grouper.base.properties", ConfigFileName.GROUPER_PROPERTIES)
        , new MultiKey("resource: grouper.cache.base.properties", ConfigFileName.GROUPER_CACHE_PROPERTIES)
        , new MultiKey("resource: grouper.client.base.properties", ConfigFileName.GROUPER_CLIENT_PROPERTIES)
        , new MultiKey("resource: subject.base.properties", ConfigFileName.SUBJECT_PROPERTIES)
        , new MultiKey("file: C:/Users/mchyzer/Documents/GitHub/grouper/grouper-ws/grouper-ws/conf/grouper-ws.base.properties", ConfigFileName.GROUPER_WS_PROPERTIES)
        , new MultiKey("file: C:/Users/mchyzer/Documents/GitHub/grouper/grouper-ui/conf/grouper-ui.base.properties", ConfigFileName.GROUPER_UI_PROPERTIES)
      }) {

      String configFilePath = (String)configFilePathMultiKey.getKey(0);
      ConfigFileName configFileName = (ConfigFileName)configFilePathMultiKey.getKey(1);
      try {
        String contents = null;

        if (configFilePath.startsWith("resource: ")) {
          configFilePath = GrouperUtil.prefixOrSuffix(configFilePath, "resource: ", false);
          contents = GrouperUtil.readResourceIntoString(configFilePath, false);
          
        } else if (configFilePath.startsWith("file: ")) {
          configFilePath = GrouperUtil.prefixOrSuffix(configFilePath, "file: ", false);
          contents = GrouperUtil.readFileIntoString(new File(configFilePath));
        } else {
          throw new RuntimeException("Not expecting prefix: '" + configFilePath + "'");
        }
        
        ConfigFileMetadata configFileMetadata = ConfigFileMetadata.generateMetadataForConfigFile(configFileName, contents);
        assertTrue(configFileMetadata.getConfigFileName().getConfigFileName(), GrouperUtil.length(configFileMetadata.getConfigSectionMetadataList()) > 0);
        assertTrue(configFileMetadata.getConfigFileName().getConfigFileName(), configFileMetadata.isValidConfig());
      } catch (RuntimeException re) {
        GrouperUtil.injectInException(re, "Problem in configFilePath: '" + configFilePath + "'");
        throw re;
      }
    }
    

  }
  
  
  /**
   * 
   */
  public void testGetLinesFromFile() {
    String contents = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/cfg/dbConfig/grouperTest.base.properties", false);

    List<String> configFileLinesList = GrouperUtil.splitFileLines(contents);
    
//    int i=1;
//    for (String line : configFileLinesList) {
//      System.out.println(i++ + ": " + line);
//    }
    
    assertEquals(120, GrouperUtil.length(configFileLinesList));
    
  }
  
  /**
   * generate metadata from config files
   */
  public void testGenerateMetadataForConfigFile() {
    
    String contents = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/cfg/dbConfig/grouperTest.base.properties", false);
        
    ConfigFileMetadata configFileMetadata = ConfigFileMetadata.generateMetadataForConfigFile(ConfigFileName.GROUPER_PROPERTIES, contents);
    
    assertNotNull(configFileMetadata);
    assertEquals(ConfigFileName.GROUPER_PROPERTIES, configFileMetadata.getConfigFileName());
    
    assertEquals("Correct number of sections",3, GrouperUtil.length(configFileMetadata.getConfigSectionMetadataList()));
    
    // section comments
    assertEquals("Config chaining hierarchy", configFileMetadata.getConfigSectionMetadataList().get(0).getTitle());
    assertEquals("General settings", configFileMetadata.getConfigSectionMetadataList().get(1).getTitle());
    assertEquals("inititalization and configuration settings", configFileMetadata.getConfigSectionMetadataList().get(2).getTitle());

    int currentSection = 0;
    int currentItem = -1;
    ConfigSectionMetadata currentConfigSection = null;
    ConfigItemMetadata currentConfigItem = null;
    
    //  first section
    //  ########################################
    //  ## Config chaining hierarchy
    //  ## This section is the configuration
    //  ## for chaining hierarchy
    //  ########################################
    //  # comma separated config files that override each other (files on the right override the left)
    //  # each should start with file: or classpath:
    //  # e.g. classpath:grouper.example.properties, file:c:/something/myconfig.properties
    //  # {valueType: "string", required: true, multiple: true, requiresRestart: true}
    //  grouper.config.hierarchy = classpath:grouper.base.properties, classpath:grouper.properties

    currentSection = 0;
    currentConfigSection = configFileMetadata.getConfigSectionMetadataList().get(currentSection);
    assertEquals("Correct number of items", 2, GrouperUtil.length(currentConfigSection.getConfigItemMetadataList()));
    assertEquals("Config chaining hierarchy", currentConfigSection.getTitle());
    assertEquals("This section is the configuration for chaining hierarchy", currentConfigSection.getComment());

    currentItem = 0;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("comma separated config files that override each other (files on the right override the left) each "
        + "should start with file: or classpath: e.g. classpath:grouper.example.properties, file:c:/something/myconfig.properties", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"string\", required: true, multiple: true, requiresRestart: true}", currentConfigItem.getRawMetadataJson());

    assertEquals("grouper.config.hierarchy", currentConfigItem.getKey());
    assertEquals("classpath:grouper.base.properties, classpath:grouper.properties", currentConfigItem.getValue());
    assertEquals(ConfigItemMetadataType.STRING, currentConfigItem.getValueType());
    assertTrue(currentConfigItem.isRequired());
    assertTrue(currentConfigItem.isMultiple());
    assertTrue(currentConfigItem.isRequiresRestart());

    //
    //  # seconds between checking to see if the config files are updated
    //  # {valueType: "integer", required: true}
    //  grouper.config.secondsBetweenUpdateChecks = 60
    
    //  ########################################
    //  ## General settings
    //  ########################################
    //
    //  # used to identify your institution (e.g. in TIER instrumentation)
    //  # {valueType: "string"}
    //  grouper.institution.name = 
    //
    //  # main stem for grouper built in objects
    //  # Note: there are more locations to change than just this
    //  # {valueType: "stem", required: true, defaultValue: "true"}
    //  grouper.rootStemForBuiltinObjects = etc

    currentSection++;
    currentConfigSection = configFileMetadata.getConfigSectionMetadataList().get(currentSection);
    assertEquals("Correct number of items", 2, GrouperUtil.length(currentConfigSection.getConfigItemMetadataList()));
    assertEquals("General settings", currentConfigSection.getTitle());

    currentItem = 0;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("used to identify your institution (e.g. in TIER instrumentation)", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"string\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.STRING, currentConfigItem.getValueType());
    assertFalse(currentConfigItem.isRequired());
    assertFalse(currentConfigItem.isMultiple());
    assertFalse(currentConfigItem.isRequiresRestart());
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("main stem for grouper built in objects Note: there are more locations to change than just this", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"stem\", required: true, defaultValue: \"true\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.STEM, currentConfigItem.getValueType());
    assertTrue(currentConfigItem.isRequired());
    assertEquals("true", currentConfigItem.getDefaultValue());

    //  #######################################
    //  ## inititalization and configuration settings
    //  #######################################
    //
    
    currentSection++;
    currentConfigSection = configFileMetadata.getConfigSectionMetadataList().get(currentSection);

    assertEquals("Correct number of items", 14, GrouperUtil.length(currentConfigSection.getConfigItemMetadataList()));

    
    assertEquals("inititalization and configuration settings", currentConfigSection.getTitle());

    
    //  #if grouper should auto init the registry if not initted (i.e. insert the root stem, built in fields, etc)
    //  #defaults to true
    //  # {valueType: "boolean", required: true}
    //  registry.autoinit = true
    //
    
    currentItem = 0;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("if grouper should auto init the registry if not initted (i.e. insert the root stem, built in fields, etc) defaults to true", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"boolean\", required: true}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.BOOLEAN, currentConfigItem.getValueType());
    assertTrue(currentConfigItem.isRequired());
    assertFalse(currentConfigItem.isMultiple());
    assertFalse(currentConfigItem.isRequiresRestart());

    
    //  #auto-create groups (increment the integer index), and auto-populate with users 
    //  #(comma separated subject ids) to bootstrap the registry on startup
    //  #(note: check config needs to be on)
    //  # {regex: "configuration.autocreate.group.name.[0-9]+", valueType: "group", required: true}
    //  #configuration.autocreate.group.name.0 = $$grouper.rootStemForBuiltinObjects$$:uiUsers
    //

    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("auto-create groups (increment the integer index), and auto-populate with users (comma separated subject ids) to bootstrap the registry on startup (note: check config needs to be on)", 
        currentConfigItem.getComment());
    
    assertEquals("{regex: \"configuration.autocreate.group.name.[0-9]+\", valueType: \"group\", required: true}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.GROUP, currentConfigItem.getValueType());
    assertTrue(currentConfigItem.isRequired());
    assertEquals("configuration.autocreate.group.name.[0-9]+", currentConfigItem.getRegex());

    //  # {regex: "^configuration.autocreate.group.description.[0-9]+$", valueType: "string"}
    //  #configuration.autocreate.group.description.0 = users allowed to log in to the UI
    //
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("", 
        GrouperUtil.trimToEmpty(currentConfigItem.getComment()));
    
    assertEquals("{regex: \"^configuration.autocreate.group.description.[0-9]+$\", valueType: \"string\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.STRING, currentConfigItem.getValueType());
    assertEquals("^configuration.autocreate.group.description.[0-9]+$", currentConfigItem.getRegex());

    //  # {regex: "configuration.autocreate.group.subjects.[0-9]+", valueType: "subject"}
    //  #configuration.autocreate.group.subjects.0 = 
    //
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("", GrouperUtil.trimToEmpty(currentConfigItem.getComment()));
    
    assertEquals("{regex: \"configuration.autocreate.group.subjects.[0-9]+\", valueType: \"subject\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.SUBJECT, currentConfigItem.getValueType());
    assertEquals("configuration.autocreate.group.subjects.[0-9]+", currentConfigItem.getRegex());

    //  # some attribute def
    //  # {valueType: "attributeDef"}
    //  someAttrDef =
    //
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("some attribute def", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"attributeDef\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.ATTRIBUTEDEF, currentConfigItem.getValueType());

    //  # some attribute def name
    //  # {valueType: "attributeDefName"}
    //  someAttrDefName =
    //
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("some attribute def name", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"attributeDefName\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.ATTRIBUTEDEFNAME, currentConfigItem.getValueType());

    //  # some class extends another class
    //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfigAbstractExample"}
    //  someClassExtends =
    //
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("some class extends another class", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"class\", mustExtendClass: \"edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfigAbstractExample\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.CLASS, currentConfigItem.getValueType());
    assertEquals("edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfigAbstractExample", currentConfigItem.getMustExtendClass());

    //  # some class implements another class
    //  # {valueType: "class", mustImplementInterface: "edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfigInterfaceExample"}
    //  someClassImplements =
    //
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("some class implements another class", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"class\", mustImplementInterface: \"edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfigInterfaceExample\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.CLASS, currentConfigItem.getValueType());
    assertEquals("edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfigInterfaceExample", currentConfigItem.getMustImplementInterface());

    //  # some pass
    //  # {valueType: "password", sensitive: true}
    //  somePass = 
    //
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("some pass", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"password\", sensitive: true}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.PASSWORD, currentConfigItem.getValueType());
    assertTrue(currentConfigItem.isSensitive());

    //  # some floatin
    //  # {valueType: "floating"}
    //  someFloating = 
    //
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("some floatin", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"floating\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.FLOATING, currentConfigItem.getValueType());

    //  # some group
    //  # {valueType: "group"}
    //  someGroup = 
    //
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("some group", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"group\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.GROUP, currentConfigItem.getValueType());

    //  # some integer
    //  # {valueType: "integer"}
    //  someInteger = 
    //
    
    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("some integer", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"integer\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.INTEGER, currentConfigItem.getValueType());

    //  # some stem
    //  # {valueType: "stem"}
    //  someStem = 

    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("some stem", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"stem\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.STEM, currentConfigItem.getValueType());
    
    //  # some subject
    //  # {valueType: "subject"}
    //  someSubject = 

    currentItem++;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(currentItem);
    
    assertEquals("some subject", 
        currentConfigItem.getComment());
    
    assertEquals("{valueType: \"subject\"}", currentConfigItem.getRawMetadataJson());

    assertEquals(ConfigItemMetadataType.SUBJECT, currentConfigItem.getValueType());
  }
  
}
