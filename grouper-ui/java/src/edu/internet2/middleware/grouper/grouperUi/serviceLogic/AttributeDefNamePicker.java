/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.AttributeDefNameTooManyResults;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeDefNamePicker.AttributeDefNamePickerConfigNotFoundException;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeDefNamePicker.AttributeDefNamePickerContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeDefNamePicker.PickerResultAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * logic for attribute def name picker module
 */
public class AttributeDefNamePicker {

  /**
   * cache of properties
   */
  private static GrouperCache<String, Properties> configCache = new GrouperCache<String, Properties>(
      AttributeDefNamePicker.class.getName() + ".configCache", 1000, true, 120, 120, false);
  
  
  /**
   * get a config from this finder's config file
   * @param attributeDefNamePickerName
   * @param key
   * @return the value
   * @throws AttributeDefNamePickerConfigNotFoundException 
   */
  public static String configFileValue(String attributeDefNamePickerName, String key) throws AttributeDefNamePickerConfigNotFoundException {
    
    Properties properties = configCache.get(attributeDefNamePickerName);
    
    String classpathName = "attributeDefNamePicker/" + attributeDefNamePickerName + ".properties";

    if (properties == null) {
      
      File configFile = null;
      String configFileName = null;
      
      try { 
        configFile = GrouperUtil.fileFromResourceName(classpathName);
      } catch (Exception e) {
        //just ignore
      }
      if (configFile == null) {
        String configDir = TagUtils.mediaResourceString("attributeDefNamePicker.confDir");
        if (!configDir.endsWith("/") && !configDir.endsWith("\\")) {
          configDir += File.separator;
        }
        configFile = new File(configDir + attributeDefNamePickerName + ".properties");
        configFileName = configFile.getAbsolutePath();
        if (!configFile.exists()) {
  
          //you must have a config file for each attributeDefName picker usage
          throw new RuntimeException("Cant find config for: '" + attributeDefNamePickerName + "' in classpath as: " 
              + classpathName + " or on file system in " + configFileName);
  
        }
      }
      properties = GrouperUtil.propertiesFromFile(configFile, true);
      configCache.put(attributeDefNamePickerName, properties);
    }
    String value = properties.getProperty(key);

    if (value == null) {
      throw new AttributeDefNamePickerConfigNotFoundException("Cant find property: " + key + " for config name: " + attributeDefNamePickerName
          + " on classpath: " + classpathName 
          + " or in config file: media.properties[\"attributeDefNamePicker.confDir\"]/" + attributeDefNamePickerName + ".properties");
    }
    return value;
  }
  
  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    AttributeDefNamePickerContainer attributeDefNamePickerContainer = AttributeDefNamePickerContainer.retrieveFromRequest();
    
    guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
        + attributeDefNamePickerContainer.textMessage("title") + "'"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/attributeDefNamePicker/attributeDefNamePickerIndex.jsp"));

    //see if we need to add a css
    String extraCssString = attributeDefNamePickerContainer.configValue("extraCss", false);
    if (!StringUtils.isBlank(extraCssString)) {
      String[] extraCssArray = GrouperUtil.splitTrim(extraCssString, ",");
      for (String extraCss : extraCssArray) {
        guiResponseJs.addAction(GuiScreenAction.newScript("guiAddCss('" + GrouperUiUtils.escapeJavascript(extraCss, true) + "');"));
      }
    }
    
  }
  
  /**
   * search for a term
   * @param request
   * @param response
   */
  public void search(HttpServletRequest request, HttpServletResponse response) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    AttributeDefNamePickerContainer attributeDefNamePickerContainer = AttributeDefNamePickerContainer.retrieveFromRequest();

    String searchField = request.getParameter("searchField");
    
    if (StringUtils.isBlank(searchField)) {
      
      String error = attributeDefNamePickerContainer.textMessage("noSearchTerm");
      guiResponseJs.addAction(GuiScreenAction.newAlert(error));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#searchResultsDiv", ""));
      return;
    }
    
    Set<AttributeDefName> attributeDefNames = null;
    
    
    //convert the source ids to strings
    String searchInAttributeDefNamesString = attributeDefNamePickerContainer
      .configValue("searchInAttributeDefNames", true);
    
    Set<String> searchInAttributeDefNames = GrouperUtil.splitTrimToSet(searchInAttributeDefNamesString, ",");
    
    boolean tooManyResults = false;
    
    String actAsSubjectId = attributeDefNamePickerContainer.configValue("actAsSubjectId", true);
    String actAsSource = attributeDefNamePickerContainer.configValue("actAsSourceId", true);
    
    Subject actAsSubject = SourceManager.getInstance().getSource(actAsSource).getSubject(actAsSubjectId, true);
    GrouperSession grouperSession = null;
    
    int maxResults = attributeDefNamePickerContainer.configValueInt("maxAttributeDefNamesResults");
    
    Set<AttributeDefName> attributeDefNamesExact = null;
    
    try {
      grouperSession = GrouperSession.start(actAsSubject);
      Set<String> searchInAttributeDefIds = new HashSet<String>();
      for (String attributeDefName : searchInAttributeDefNames) {
        AttributeDef attributeDef = AttributeDefFinder.findByName(attributeDefName, true);
        searchInAttributeDefIds.add(attributeDef.getId());
      }

      //lets look for exact matches
      attributeDefNamesExact = AttributeDefNameFinder.findAll(searchField, searchInAttributeDefIds, new QueryOptions().paging(QueryPaging.page(maxResults, 1, false)));
      
      //add some wildcards
      if (!StringUtils.isBlank(searchField)) {
        searchField = "%" + searchField + "%";
      }
      
      attributeDefNames = AttributeDefNameFinder.findAll(searchField, searchInAttributeDefIds, new QueryOptions().paging(QueryPaging.page(maxResults, 1, false)));
      
      if (attributeDefNames.size() == maxResults) {
        tooManyResults = true;
      }

    } catch (AttributeDefNameTooManyResults stmr) {
      tooManyResults = true;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    //add in the ids and identifiers if not there already
    if (attributeDefNames == null) {
      attributeDefNames = new LinkedHashSet<AttributeDefName>();
    }

    if (maxResults < GrouperUtil.length(attributeDefNames)) {
      
      tooManyResults = true;
      attributeDefNames = GrouperUtil.setShorten(attributeDefNames, maxResults);
    }

    //insert the exact matches at the beginning
    if (GrouperUtil.length(attributeDefNamesExact) > 0) {
      attributeDefNamesExact.addAll(attributeDefNames);
      attributeDefNames = attributeDefNamesExact;
    }
    
    if (GrouperUtil.length(attributeDefNames) == 0) {
      
      String error = attributeDefNamePickerContainer.textMessage("noResultsFound");
      guiResponseJs.addAction(GuiScreenAction.newAlert(error));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#searchResultsDiv", ""));
      return;
    }

    if (tooManyResults) {
      String error = attributeDefNamePickerContainer.textMessage("tooManyResults");
      guiResponseJs.addAction(GuiScreenAction.newAlert(error));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#searchResultsDiv", ""));
      //dont return, show what we got
    }
    
    List<PickerResultAttributeDefName> pickerResultAttributeDefNameList = new ArrayList<PickerResultAttributeDefName>();
    for (AttributeDefName attributeDefName : attributeDefNames) {
      PickerResultAttributeDefName pickerResultAttributeDefName = new PickerResultAttributeDefName(attributeDefName, attributeDefNamePickerContainer);
      pickerResultAttributeDefNameList.add(pickerResultAttributeDefName);
    }

    //sort these first
    //dont sort, sorted from DB
    //Collections.sort(pickerResultAttributeDefNameList);
    
    PickerResultAttributeDefName[] pickerResultAttributeDefNames = GrouperUtil.toArray(pickerResultAttributeDefNameList, PickerResultAttributeDefName.class);
    
    StringBuilder jsonAttributeDefNames = new StringBuilder("<script>\n");
    
    for (int i=0;i<pickerResultAttributeDefNames.length;i++) {

      pickerResultAttributeDefNames[i].setIndex(i);
      
      i++;
    }

    jsonAttributeDefNames.append("</script>\n");

    attributeDefNamePickerContainer.setPickerResultAttributeDefNames(pickerResultAttributeDefNames);
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#searchResultsDiv", 
      "/WEB-INF/grouperUi/templates/attributeDefNamePicker/attributeDefNamePickerResults.jsp"));
  }

}
