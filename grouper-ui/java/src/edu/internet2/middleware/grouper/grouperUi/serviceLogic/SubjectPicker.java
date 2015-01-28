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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.subjectPicker.PickerResultJavascriptSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.subjectPicker.PickerResultSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.subjectPicker.SubjectPickerConfigNotFoundException;
import edu.internet2.middleware.grouper.grouperUi.beans.subjectPicker.SubjectPickerContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.subjectPicker.SubjectPickerJavascriptBean;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.member.SearchStringEnum;
import edu.internet2.middleware.grouper.member.SortStringEnum;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectTooManyResults;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * <pre>
 * http://localhost:8088/grouper/grouperUi/appHtml/grouper.html?operation=SubjectPicker.index&subjectPickerName=kimSupervisorPicker&subjectPickerElementName=supervisorPennId
 * 
 * conf/subjectPicker/kimSupervisorPicker.properties
 * 
 * 
 * resultsMustBeInGroup = penn:community:employeeIncludingUphs
 *
 * # put a URL here where the result (subjectId, sourceId, name, description) will be submitted back
 * # blank if same domain and just call opener directly
 * submitResultToUrl = @kualiRiceUrl@/penn/grouperSubjectPicker.html
 * 
 * nav.properties:
 * subjectPicker.kimSupervisorPicker.title = Find your supervisor
 * subjectPicker.kimSupervisorPicker.header = Find your supervisor
 * subjectPicker.kimSupervisorPicker.searchSectionTitle = Enter search term (e.g. name, PennKey, etc)

 * 
 * logic for subject picker module
 * </pre>
 */
public class SubjectPicker {

  /** subjectPicker source properties, multikey on picker app name and sourceId */
  private static GrouperCache<MultiKey, SubjectPickerSourceProperties> subjectPickerSourcePropertiesCache 
    = new GrouperCache<MultiKey, SubjectPickerSourceProperties>(SubjectPicker.class.getName() 
      + "subjectPickerSourcePropertiesCache", 100, false, 300, 300, false);
  
  /**
   * get the source properties for an app name (current app name)
   * @param sourceId
   * @return properties for source and app name
   */
  public static SubjectPickerSourceProperties subjectPickerSourceProperties(String sourceId) {
    
    String pickerAppName = SubjectPickerContainer.retrieveFromRequest().getSubjectPickerName();
    MultiKey multiKey = new MultiKey(pickerAppName, sourceId);
    
    SubjectPickerSourceProperties subjectPickerSourceProperties = 
      subjectPickerSourcePropertiesCache.get(multiKey);
    if (subjectPickerSourceProperties == null) {
      subjectPickerSourceProperties = new SubjectPickerSourceProperties();
      subjectPickerSourceProperties.setSourceId(sourceId);
      
      //lets get media.properties first
      
      for (int i=0;i<100;i++) {
        
        try {
          String currentSourceId = GrouperUiConfig.retrieveConfig().propertyValueString("subjectPicker.defaultSettings.sourceProperties.sourceId." + i);
          if (StringUtils.isBlank(currentSourceId)) {
            break;
          }
          if (StringUtils.equals(sourceId, currentSourceId)) {
            //we found it
            String subjectElForSource = GrouperUiConfig.retrieveConfig().propertyValueString("subjectPicker.defaultSettings.sourceProperties.subjectElForSource." + i);
            if (!StringUtils.isBlank(subjectElForSource)) {
              subjectPickerSourceProperties.setSubjectElForSource(subjectElForSource);
            }
            break;
          }
        } catch (MissingResourceException mre) {
          //if no source, then we are done
          break;
        }
        
      }

      String subjectPickerName = SubjectPickerContainer.retrieveFromRequest().getSubjectPickerName();
      
      //now lets go through the app property file
      for (int i=0;i<100;i++) {
        
        try {
          String currentSourceId = configFileValue(subjectPickerName, "sourceProperties.sourceId." + i);
          if (StringUtils.equals(sourceId, currentSourceId)) {
            //we found it
            String subjectElForSource = configFileValue(subjectPickerName, "sourceProperties.subjectElForSource." + i);
            subjectPickerSourceProperties.setSubjectElForSource(subjectElForSource);
            break;
          }
        } catch (SubjectPickerConfigNotFoundException spcnfe) {
          //if no source, then we are done
          break;
        }
        
        
      }
      subjectPickerSourcePropertiesCache.put(multiKey, subjectPickerSourceProperties);
    }
    return subjectPickerSourceProperties;
  }
  
  /**
   * properties about a source
   */
  public static class SubjectPickerSourceProperties {

    /** source id */
    private String sourceId;
    
    /** expression language for subject representation on screen */
    private String subjectElForSource;

    
    /**
     * source id
     * @return the sourceId
     */
    public String getSourceId() {
      return this.sourceId;
    }

    
    /**
     * source id
     * @param sourceId1 the sourceId to set
     */
    public void setSourceId(String sourceId1) {
      this.sourceId = sourceId1;
    }

    
    /**
     * expression language for subject representation on screen
     * @return the subjectElForSource
     */
    public String getSubjectElForSource() {
      return this.subjectElForSource;
    }

    
    /**
     * expression language for subject representation on screen
     * @param subjectElForSource1 the subjectElForSource to set
     */
    public void setSubjectElForSource(String subjectElForSource1) {
      this.subjectElForSource = subjectElForSource1;
    }
    
    
  }
  
  /**
   * cache of properties
   */
  private static GrouperCache<String, Properties> configCache = new GrouperCache<String, Properties>(
      SubjectPicker.class.getName() + ".configCache", 1000, true, 120, 120, false);
  
  
  /**
   * get a config from this finder's config file
   * @param subjectPickerName
   * @param key
   * @return the value
   * @throws SubjectPickerConfigNotFoundException 
   */
  public static String configFileValue(String subjectPickerName, String key) throws SubjectPickerConfigNotFoundException {
    
    Properties properties = configCache.get(subjectPickerName);
    
    String classpathName = "subjectPicker/" + subjectPickerName + ".properties";

    if (properties == null) {
      
      File configFile = null;
      String configFileName = null;
      
      try { 
        configFile = GrouperUtil.fileFromResourceName(classpathName);
      } catch (Exception e) {
        //just ignore
      }
      if (configFile == null) {
        String configDir = GrouperUiConfig.retrieveConfig().propertyValueStringRequired("subjectPicker.confDir");
        if (!configDir.endsWith("/") && !configDir.endsWith("\\")) {
          configDir += File.separator;
        }
        configFile = new File(configDir + subjectPickerName + ".properties");
        configFileName = configFile.getAbsolutePath();
        if (!configFile.exists()) {
  
          //you must have a config file for each subject picker usage
          throw new RuntimeException("Cant find config for: '" + subjectPickerName + "' in classpath as: " 
              + classpathName + " or on file system in " + configFileName);
  
        }
      }
      properties = GrouperUtil.propertiesFromFile(configFile, true);
      configCache.put(subjectPickerName, properties);
    }
    String value = properties.getProperty(key);

    if (value == null) {
      throw new SubjectPickerConfigNotFoundException("Cant find property: " + key + " for config name: " + subjectPickerName
          + " on classpath: " + classpathName 
          + " or in config file: media.properties[\"subjectPicker.confDir\"]/" + subjectPickerName + ".properties");
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

    SubjectPickerContainer subjectPickerContainer = SubjectPickerContainer.retrieveFromRequest();
    
    guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
        + subjectPickerContainer.textMessage("title") + "'"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/subjectPicker/subjectPickerIndex.jsp"));

    //see if we need to add a css
    String extraCssString = subjectPickerContainer.configValue("extraCss", false);
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
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      SubjectPickerContainer subjectPickerContainer = SubjectPickerContainer.retrieveFromRequest();
  
      String searchField = request.getParameter("searchField");
      
      if (StringUtils.isBlank(searchField)) {
        
        String error = subjectPickerContainer.textMessage("noSearchTerm");
        guiResponseJs.addAction(GuiScreenAction.newAlert(error));
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#searchResultsDiv", ""));
        return;
      }
      
      Set<Subject> subjects = null;
      
      
      //convert the source ids to strings
      String searchInSourceIdsString = subjectPickerContainer.configValue("searchInSourceIds");
      
      boolean restrictingSourcesBoolean = !StringUtils.isBlank(searchInSourceIdsString);
      
      Set<Source> sourcesToSearchInSourceSet = null;
      boolean tooManyResults = false;

      //if filtering by group, do that here
      final String groupFilterName = subjectPickerContainer.configValue("resultsMustBeInGroup");
      
      if (StringUtils.isBlank(groupFilterName)) {
        try {
          
          //if clamping down on sources in config
          if (restrictingSourcesBoolean) {
            sourcesToSearchInSourceSet = GrouperUtil.convertSources(searchInSourceIdsString);
            SearchPageResult findPageResult = SubjectFinder.findPage(searchField, sourcesToSearchInSourceSet);
            subjects = findPageResult.getResults();
            tooManyResults = findPageResult.isTooManyResults();
          } else {
            sourcesToSearchInSourceSet = new HashSet<Source>(SourceManager.getInstance().getSources());
            SearchPageResult findPageResult = SubjectFinder.findPage(searchField);
            subjects = findPageResult.getResults();
            tooManyResults = findPageResult.isTooManyResults();
          }
          
        } catch (SubjectTooManyResults stmr) {
          tooManyResults = true;
        }
      } else {
        
        Subject actAsSubject = null;
        
        String actAsSourceId = subjectPickerContainer.configValue("actAsSourceId");
        String actAsSubjectId = subjectPickerContainer.configValue("actAsSubjectId");
        
        if (!StringUtils.isBlank(actAsSubjectId)) {
          
          if (!StringUtils.isBlank(actAsSourceId)) {
            
            Source source = SourceManager.getInstance().getSource(actAsSourceId);
            actAsSubject = source.getSubject(actAsSubjectId, true);
            
          } else {
            actAsSubject = SubjectFinder.findById(actAsSubjectId, true);
          }
          
        }
       
        if (actAsSubject == null) {
          actAsSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        }
        
        //get the group as system
        Group group = (Group)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {
            return GroupFinder.findByName(grouperSession2, groupFilterName, true);
          }
        });
        
        //get group, and get id
        QueryOptions queryOptions = new QueryOptions();
        int pageSize = 200;
        queryOptions.paging(pageSize, 1, false);
        
        Set<Member> members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
            group.getId(), Group.getDefaultList(), null, null, queryOptions, true, 
            SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_0, searchField);
        
        //load all subjects in few queries
        Member.resolveSubjects(members, false);
        
        subjects = new HashSet<Subject>();
        for (Member member : members) {
          subjects.add(member.getSubject());
        }
        
        tooManyResults = members.size() == pageSize;
      }  
      
      //see if any match subjectIdentifier, this is the set of all subject identifier matches
      Set<Subject> idOrIdentifierSubjects = new HashSet<Subject>();
      if (GrouperUtil.length(sourcesToSearchInSourceSet) > 0) {
        for (Source source : sourcesToSearchInSourceSet) {
          Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(searchField, source.getId(), false);
          if (subject != null) {
            idOrIdentifierSubjects.add(subject);
          }
        }
      } else {
        Subject subject = SubjectFinder.findByIdOrIdentifier(searchField, false);
        if (subject != null) {
          idOrIdentifierSubjects.add(subject);
        }
      }
        
      
      int maxResults = subjectPickerContainer.configValueInt("maxSubjectsResultsBeforeGroupSearch");
      
      if (maxResults < GrouperUtil.length(subjects)) {
        tooManyResults = true;
        subjects = GrouperUtil.setShorten(subjects, maxResults);
      }
      
      if (GrouperUtil.length(subjects) == 0) {
        
        String error = subjectPickerContainer.textMessage("noResultsFound");
        guiResponseJs.addAction(GuiScreenAction.newAlert(error));
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#searchResultsDiv", ""));
        return;
      }
  
      
      maxResults = subjectPickerContainer.configValueInt("maxSubjectsResults");
      
      if (maxResults < GrouperUtil.length(subjects)) {
        
        tooManyResults = true;
        subjects = GrouperUtil.setShorten(subjects, maxResults);
      }
  
      if (tooManyResults) {
        String error = subjectPickerContainer.textMessage("tooManyResults");
        guiResponseJs.addAction(GuiScreenAction.newAlert(error));
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#searchResultsDiv", ""));
        //dont return, show what we got
      }
      
      subjects = SubjectHelper.sortSetForSearch(subjects, searchField);
      
      List<PickerResultSubject> pickerResultSubjectList = new ArrayList<PickerResultSubject>();
      for (Subject subject : subjects) {
        PickerResultSubject pickerResultSubject = new PickerResultSubject(subject);
        pickerResultSubjectList.add(pickerResultSubject);
        //keep track of id or identifier match for sorting
        if (idOrIdentifierSubjects.size() > 0 && SubjectHelper.inList(idOrIdentifierSubjects, subject)) {
          pickerResultSubject.setMatchesSubjectIdOrIdentifier(true);
        }
      }
  
      //sort these first
      Collections.sort(pickerResultSubjectList);
      
      PickerResultSubject[] pickerResultSubjects = GrouperUtil.toArray(pickerResultSubjectList, PickerResultSubject.class);
      
      StringBuilder jsonSubjects = new StringBuilder("<script>\n");
      
      boolean sendSubjectJsonToCallback = subjectPickerContainer.configValueBoolean("sendSubjectJsonToCallback");
  
      for (int i=0;i<pickerResultSubjects.length;i++) {
  
        pickerResultSubjects[i].setIndex(i);
        
        PickerResultJavascriptSubject subjectForJavascript = convertSubjectToPickerSubjectForJavascript(
            pickerResultSubjects[i].getSubject(), pickerResultSubjects[i]);
        
        //this could be used for EL
        pickerResultSubjects[i].setPickerResultJavascriptSubject(subjectForJavascript);
        
        //if we are configured to send the objects back
        if (sendSubjectJsonToCallback) {
          
          JSONObject jsonObject = net.sf.json.JSONObject.fromObject( subjectForJavascript );  
          String json = jsonObject.toString();
          jsonSubjects.append("  var subject_" + i + " = " + json + ";\n");
          
          pickerResultSubjects[i].setSubjectObjectName("subject_" + i);
        } else {
          pickerResultSubjects[i].setSubjectObjectName("null");
        }
        
        i++;
      }
  
      jsonSubjects.append("</script>\n");
  
      //if we are configured to send the objects back
      if (sendSubjectJsonToCallback) {
        subjectPickerContainer.setSubjectsScript(jsonSubjects.toString());
      } else {
        subjectPickerContainer.setSubjectsScript(null);
      }
      
      subjectPickerContainer.setPickerResultSubjects(pickerResultSubjects);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#searchResultsDiv", 
        "/WEB-INF/grouperUi/templates/subjectPicker/subjectPickerResults.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * 
   * @param subject
   * @param pickerResultSubject
   * @return the picker result subject which is a different object, special for javascript
   */
  @SuppressWarnings("unchecked")
  private static PickerResultJavascriptSubject convertSubjectToPickerSubjectForJavascript(Subject subject, PickerResultSubject pickerResultSubject) {
    SubjectPickerContainer subjectPickerContainer = SubjectPickerContainer.retrieveFromRequest();
    
    String subjectId = subjectPickerContainer.configValueBoolean("subjectObject.include.subjectId") ? subject.getId() : null;
    String sourceId = subjectPickerContainer.configValueBoolean("subjectObject.include.sourceId") ? subject.getSourceId() : null;
    String typeName = subjectPickerContainer.configValueBoolean("subjectObject.include.typeName") ? subject.getTypeName() : null;
    String name = subjectPickerContainer.configValueBoolean("subjectObject.include.name") ? subject.getName() : null;
    String description = subjectPickerContainer.configValueBoolean("subjectObject.include.description") ? subject.getDescription() : null;
    
    Map<String, String[]> attributes = null;
    String attributesConfig = subjectPickerContainer.configValue("subjectObject.include.attributes");
    Map<String, Set<String>> subjectExistingAttributes = subject.getAttributes();
    attributes = new LinkedHashMap<String, String[]>();
    boolean includeAll = StringUtils.equals(attributesConfig, "INCLUDE_ALL_ATTRIBUTES");
    String[] allowedAttributes = includeAll ? null : GrouperUtil.splitTrim(attributesConfig, ",");
    
    Set<String> hasAttributes = subjectExistingAttributes.keySet();
    for (String hasAttributeKey : hasAttributes) {
      if (includeAll || ArrayUtils.contains(allowedAttributes, hasAttributeKey)) {
        String[] values = GrouperUtil.toArray(subjectExistingAttributes.get(hasAttributeKey), String.class);
        attributes.put(hasAttributeKey, values);
      }
    }
    
    //note: this isnt really the right type, but thats ok, Java will forgive
    SubjectPickerJavascriptBean subjectForJavascript = new SubjectPickerJavascriptBean(attributes, description, subjectId, name, sourceId, typeName);
    
    PickerResultJavascriptSubject pickerSubjectForJavascript = new PickerResultJavascriptSubject(subjectForJavascript);
    pickerSubjectForJavascript.setIndex(pickerResultSubject.getIndex());
    return pickerSubjectForJavascript;
  }
  
}
