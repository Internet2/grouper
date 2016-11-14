package edu.internet2.middleware.grouperInstaller;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils;

public class ConvertEhcacheXmlToProperties {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    if (args.length == 0) {
      throw new RuntimeException("Pass in the grouper.cache.base.properties location, grouper.cache.properties "
          + "location to write to, and optionally (if not from classpath) "
          + "the grouper.ehcache.xml file location!");
    }

    String ehcacheBasePropertiesFileLocation = args[0];
    
    File ehcacheBasePropertiesFile = new File(ehcacheBasePropertiesFileLocation);

    if (!ehcacheBasePropertiesFile.exists()) {
      throw new RuntimeException(ehcacheBasePropertiesFile.getAbsolutePath() + " must exists and does not!");
    }

    String ehcachePropertiesFileLocation = args[1];
    
    File ehcachePropertiesFile = new File(ehcachePropertiesFileLocation);
    
    if (ehcachePropertiesFile.exists()) {
      throw new RuntimeException(ehcachePropertiesFile.getAbsolutePath() + " exists and must not.  Delete the file and run this again!");
    }

    if (!ehcachePropertiesFile.getParentFile().exists() || !ehcachePropertiesFile.getParentFile().isDirectory()) {
      throw new RuntimeException(ehcachePropertiesFile.getParentFile().getAbsolutePath() + " must exist and must be a directory");
    }

    String grouperEhcacheXmlFileLocation = args.length >= 3 ? args[2] : null;
    
    URL ehcacheXmlUrl = null;
    
    if (grouperEhcacheXmlFileLocation != null) {
      
      File grouperEhcacheXmlFile = new File(grouperEhcacheXmlFileLocation);
      try {
        ehcacheXmlUrl = grouperEhcacheXmlFile.toURI().toURL();
      } catch (MalformedURLException mue) {
        throw new RuntimeException(mue);
      }
    } else {
      ehcacheXmlUrl = ConvertEhcacheXmlToProperties.class.getResource("/grouper.ehcache.xml");
    }
    convertEhcacheXmlToProperties(ehcacheBasePropertiesFile, ehcachePropertiesFile, ehcacheXmlUrl);
  }

  /**
   * 
   * @param ehcachePropertiesFile
   * @param ehcacheXmlUrl
   */
  public static void convertEhcacheXmlToProperties(File grouperEhcacheBaseFile, File ehcachePropertiesFile, URL ehcacheXmlUrl) {

    if (ConvertEhcacheXmlToProperties.class.getResource("/grouper.cache.base.properties") == null) {
      throw new RuntimeException("Cant find grouper.cache.base.properties on the classpath!");
    }
    
    //look at base properties
    Properties grouperEhcacheBaseProperties = GrouperInstallerUtils.propertiesFromFile(grouperEhcacheBaseFile);
    
    StringBuilder grouperEhcachePropertiesContents = new StringBuilder();
    
    grouperEhcachePropertiesContents.append(
              "# Copyright 2014 Internet2\n"
            + "#\n"
            + "# Licensed under the Apache License, Version 2.0 (the \"License\");\n"
            + "# you may not use this file except in compliance with the License.\n"
            + "# You may obtain a copy of the License at\n"
            + "#\n"
            + "#   http://www.apache.org/licenses/LICENSE-2.0\n"
            + "#\n"
            + "# Unless required by applicable law or agreed to in writing, software\n"
            + "# distributed under the License is distributed on an \"AS IS\" BASIS,\n"
            + "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
            + "# See the License for the specific language governing permissions and\n"
            + "# limitations under the License.\n"
            + "#\n"
            + "\n"
            + "# Grouper loader uses Grouper Configuration Overlays (documented on wiki)\n"
            + "# By default the configuration is read from grouper-loader.base.properties\n"
            + "# (which should not be edited), and the grouper-loader.properties overlays\n"
            + "# the base settings.  See the grouper-loader.base.properties for the possible\n"
            + "# settings that can be applied to the grouper.properties\n"
        );


    
  }
  
  /**
   * 
   * @param ehcacheBaseFile
   */
  public void convertEhcacheBaseToProperties(File ehcacheBaseFile) {
    //File ehcacheBaseBakFile = this.bakFile(ehcacheBaseFile);
    //GrouperInstallerUtils.copyFile(existingFile, bakFile, true);
    //System.out.println("Backing up: " + existingFile.getAbsolutePath() + " to: " + bakFile.getAbsolutePath());
    
    NodeList nodeList = GrouperInstallerUtils.xpathEvaluate(ehcacheBaseFile, "/ehcache/cache");
    
    Set<String> usedKeys = new HashSet<String>();
    
    for (int i=0;i<nodeList.getLength();i++) {
      
      Element element = (Element)nodeList.item(i);

      //  <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO.FindBySubject"
      //      maxElementsInMemory="5000"
      //      eternal="false"
      //      timeToIdleSeconds="5"
      //      timeToLiveSeconds="10"
      //      overflowToDisk="false"  
      //      statistics="false"
      //  />

      
      String name = element.getAttribute("name");
      Integer maxElementsInMemory = GrouperInstallerUtils.intObjectValue(element.getAttribute("maxElementsInMemory"), true);
      Boolean eternal = GrouperInstallerUtils.booleanObjectValue(element.getAttribute("eternal"));
      Integer timeToIdleSeconds = GrouperInstallerUtils.intObjectValue(element.getAttribute("timeToIdleSeconds"), true);
      Integer timeToLiveSeconds = GrouperInstallerUtils.intObjectValue(element.getAttribute("timeToLiveSeconds"), true);
      Boolean overflowToDisk = GrouperInstallerUtils.booleanObjectValue(element.getAttribute("overflowToDisk"));
      Boolean statistics = GrouperInstallerUtils.booleanObjectValue(element.getAttribute("statistics"));

      //any attributes we dont expect?
      NamedNodeMap configuredNamedNodeMap = element.getAttributes();
      //see which attributes are new or changed
      for (int j=0;j<configuredNamedNodeMap.getLength();j++) {
        Node configuredAttribute = configuredNamedNodeMap.item(j);
        if (!configuredAttribute.getNodeName().equals("name")
            && !configuredAttribute.getNodeName().equals("maxElementsInMemory")
            && !configuredAttribute.getNodeName().equals("eternal")
            && !configuredAttribute.getNodeName().equals("timeToIdleSeconds")
            && !configuredAttribute.getNodeName().equals("timeToLiveSeconds")
            && !configuredAttribute.getNodeName().equals("overflowToDisk")
            && !configuredAttribute.getNodeName().equals("statistics")) {
          throw new RuntimeException("Cant process attribute: '" + configuredAttribute.getNodeName() + "'");
        }
      }
      
      String key = convertEhcacheNameToPropertiesKey(name, usedKeys);
      
      //  cache.name.edu_internet2_middleware_grouper_internal_dao_hib3_Hib3GroupDAO.name = edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupDAO
      //  cache.name.edu_internet2_middleware_grouper_internal_dao_hib3_Hib3GroupDAO.maxElementsInMemory = 500
      //  cache.name.edu_internet2_middleware_grouper_internal_dao_hib3_Hib3GroupDAO.eternal = false
      //  cache.name.edu_internet2_middleware_grouper_internal_dao_hib3_Hib3GroupDAO.timeToIdleSeconds = 1
      //  cache.name.edu_internet2_middleware_grouper_internal_dao_hib3_Hib3GroupDAO.timeToLiveSeconds = 1
      //  cache.name.edu_internet2_middleware_grouper_internal_dao_hib3_Hib3GroupDAO.overflowToDisk = false
      //  cache.name.edu_internet2_middleware_grouper_internal_dao_hib3_Hib3GroupDAO.statistics = false
      
      System.out.println("cache.name." + key + ".name = " + name);
      if (maxElementsInMemory != null) {
        System.out.println("cache.name." + key + ".maxElementsInMemory = " + maxElementsInMemory);
      }
      if (eternal != null) {
        System.out.println("cache.name." + key + ".eternal = " + eternal);
      }
      if (timeToIdleSeconds != null) {
        System.out.println("cache.name." + key + ".timeToIdleSeconds = " + timeToIdleSeconds);
      }
      if (timeToLiveSeconds != null) {
        System.out.println("cache.name." + key + ".timeToLiveSeconds = " + timeToLiveSeconds);
      }
      if (overflowToDisk != null) {
        System.out.println("cache.name." + key + ".overflowToDisk = " + overflowToDisk);
      }
      if (statistics != null) {
        System.out.println("cache.name." + key + ".statistics = " + statistics);
      }
      System.out.println("");
    }

  }

  /**
   * convert a name like: edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupDAO
   * to: edu_internet2_middleware_grouper_internal_dao_hib3_Hib3GroupDAO
   * @param usedKeys
   * @return the key
   */
  private static String convertEhcacheNameToPropertiesKey(String ehcacheName, Set<String> usedKeys) {
    
    StringBuilder result = new StringBuilder();

    //strip off this beginning to get the keys a little smaller
    if (ehcacheName.startsWith("edu.internet2.middleware.grouper.")) {
      ehcacheName = ehcacheName.substring("edu.internet2.middleware.grouper.".length());
    }
    
    for (int i=0; i<ehcacheName.length(); i++) {
      
      char curChar = ehcacheName.charAt(i);
      
      if (Character.isAlphabetic(curChar) || Character.isDigit(curChar)) {
        result.append(curChar);
      } else {
        result.append("_");
      }
      
    }

    String resultString = result.toString();
    if (!usedKeys.contains(resultString)) {
      return resultString;
    }
    
    for (int i=2;i<100;i++) {
      String newResult = resultString + "_" + i;
      if (!usedKeys.contains(newResult)) {
        return newResult;
      }
    }
    
    throw new RuntimeException("Cant find name for " + ehcacheName);
  }

  
}
