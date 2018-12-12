/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.util.List;

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
    TestRunner.run(new ConfigFileMetadataTest("testGenerateMetadataForConfigFile"));
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
  public void testGetLinesFromFile() {
    String contents = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/cfg/dbConfig/grouperTest.base.properties", false);

    List<String> configFileLinesList = GrouperUtil.splitFileLines(contents);
    
//    int i=1;
//    for (String line : configFileLinesList) {
//      System.out.println(i++ + ": " + line);
//    }
    
    assertEquals(113, GrouperUtil.length(configFileLinesList));
    
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
    assertEquals("Config chaining hierarchy", configFileMetadata.getConfigSectionMetadataList().get(0).getComment());
    assertEquals("General settings", configFileMetadata.getConfigSectionMetadataList().get(1).getComment());
    assertEquals("inititalization and configuration settings", configFileMetadata.getConfigSectionMetadataList().get(2).getComment());

    int currentSection = 0;
    int currentItem = -1;
    ConfigSectionMetadata currentConfigSection = null;
    ConfigItemMetadata currentConfigItem = null;
    
    //  first section
    //  ########################################
    //  ## Config chaining hierarchy
    //  ########################################
    //  # comma separated config files that override each other (files on the right override the left)
    //  # each should start with file: or classpath:
    //  # e.g. classpath:grouper.example.properties, file:c:/something/myconfig.properties
    //  # {valueType: "string", required: true, multiple: true, requiresRestart: true}
    //  grouper.config.hierarchy = classpath:grouper.base.properties, classpath:grouper.properties

    currentSection = 0;
    currentConfigSection = configFileMetadata.getConfigSectionMetadataList().get(currentSection);
    assertEquals("Correct number of items", 2, GrouperUtil.length(currentConfigSection.getConfigItemMetadataList()));
    
    currentItem = 0;
    currentConfigItem = currentConfigSection.getConfigItemMetadataList().get(0);
    
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
    //  # {valueType: "stem", required: true, default: "true"}
    //  grouper.rootStemForBuiltinObjects = etc

    currentSection++;
    currentConfigSection = configFileMetadata.getConfigSectionMetadataList().get(currentSection);
    assertEquals("Correct number of items", 2, GrouperUtil.length(currentConfigSection.getConfigItemMetadataList()));

    
    //  #######################################
    //  ## inititalization and
    //  ## configuration settings
    //  #######################################
    //
    //  #if grouper should auto init the registry if not initted (i.e. insert the root stem, built in fields, etc)
    //  #defaults to true
    //  # {valueType: "boolean", required: true}
    //  registry.autoinit = true
    //
    //  #auto-create groups (increment the integer index), and auto-populate with users 
    //  #(comma separated subject ids) to bootstrap the registry on startup
    //  #(note: check config needs to be on)
    //  # {regex: "configuration.autocreate.group.name.[0-9]+", valueType: "group", required: true}
    //  #configuration.autocreate.group.name.0 = $$grouper.rootStemForBuiltinObjects$$:uiUsers
    //
    //  # {regex: "configuration.autocreate.group.description.[0-9]+", valueType: "string"}
    //  #configuration.autocreate.group.description.0 = users allowed to log in to the UI
    //
    //  # {regex: "configuration.autocreate.group.subjects.[0-9]+", valueType: "subject"}
    //  #configuration.autocreate.group.subjects.0 = 
    //
    //  # some attribute def
    //  # {valueType: "attributeDef"}
    //  someAttrDef =
    //
    //  # some attribute def name
    //  # {valueType: "attributeDefName"}
    //  someAttrDefName =
    //
    //  # some class extends another class
    //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfigAbstractExample"}
    //  someClassExtends =
    //
    //  # some class implements another class
    //  # {valueType: "class", mustImplementInterface: "edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfigInterfaceExample"}
    //  someClassImplements =
    //
    //  # some pass
    //  # {valueType: "password", sensitive: true}
    //  somePass = 
    //
    //  # some floatin
    //  # {valueType: "floating"}
    //  someFloating = 
    //
    //  # some group
    //  # {valueType: "group"}
    //  someGroup = 
    //
    //  # some integer
    //  # {valueType: "integer"}
    //  someInteger = 
    //
    //  # some integer
    //  # {valueType: "integer"}
    //  someInteger = 
    currentSection++;
    currentConfigSection = configFileMetadata.getConfigSectionMetadataList().get(currentSection);

    assertEquals("Correct number of items", 13, GrouperUtil.length(currentConfigSection.getConfigItemMetadataList()));
    
    
  }
  
}
