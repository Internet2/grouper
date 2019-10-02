/*******************************************************************************
 * Copyright 2014 Internet2
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
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.dojo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * does logic for a combobox
 * @author mchyzer
 *
 */
public class DojoComboLogic {

  /**
   * do the logic for a dojo combobox.  Note, this will open and close a grouperSession too
   * @param dojoQueryLogic
   * @param request
   * @param response
   */
  public static <T> void logic(HttpServletRequest request, 
      HttpServletResponse response, DojoComboQueryLogic<T> dojoComboQueryLogic) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    DojoComboDataResponse dojoComboDataResponse = null;


    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      boolean done = false;
      
      {
        String validationError = dojoComboQueryLogic.initialValidationError(request, grouperSession);
        if (!StringUtils.isBlank(validationError)) {
          dojoComboDataResponse = new DojoComboDataResponse(
              new DojoComboDataResponseItem[]{new DojoComboDataResponseItem("", validationError, validationError)});
          done = true;
        }
      }
    
      if (!done) {
        //{
        //  query: {name: "A*"},
        //  queryOptions: {ignoreCase: true},
        //  sort: [{attribute:"name", descending:false}],
        //    start: 0,
        //    count: 10
        //}
        
        //https://server.url/twoFactorMchyzer/twoFactorUi/app/UiMain.personPicker?name=ab*&start=0&count=Infinity
  
        //Utils.printToScreen("{\"label\":\"name\", \"identifier\":\"id\",\"items\":[{\"id\":\"10021368\",\"name\":\"Chris Hyzer (mchyzer, 10021368) (active) Staff - Astt And Information Security - Application Architect (also: Alumni)\"},{\"id\":\"10193029\",\"name\":\"Chyze-Whee Ang (angcw, 10193029) (active) Alumni\"}]}", "application/json", false, false);
  
        String query = StringUtils.defaultString(request.getParameter("id"));
        
        boolean isLookup = true;
        
        if (StringUtils.isBlank(query)) {
  
          isLookup = false;
          
          query = StringUtils.trimToEmpty(request.getParameter("name"));
  
        }
  
        //if there is no *, then looking for a specific name, return nothing since someone just typed something in and left...
        if (!query.contains("*")) {
          
          isLookup = true;
          
        }

        boolean allowAutocompleteById = true;
        String allowedAutocompleteByIdPaths = GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.search.autocompleteById.allowedPaths");
        if (!StringUtils.isBlank(allowedAutocompleteByIdPaths)) {
          allowAutocompleteById = false;
          List<String> allowedPaths = GrouperUtil.splitTrimToList(allowedAutocompleteByIdPaths, ",");
          if (allowedPaths.contains(request.getPathInfo())) {
            allowAutocompleteById = true;
          }
        }

        Set<T> objects = new LinkedHashSet<T>();
        boolean enterMoreChars = false;
        
        {
          String groupIdOrName = query.endsWith("*") ? query.substring(0, query.length()-1) : query;
          if (!StringUtils.isBlank(groupIdOrName) && (allowAutocompleteById || !query.endsWith("*"))) {

            T t = dojoComboQueryLogic.lookup(request, grouperSession, groupIdOrName);
            if (t != null) {
              objects.add(t);
            }
          }
        }

        if (!isLookup) {

          boolean allowAutocompleteSearch = true;
          String allowedAutocompleteSearchPaths = GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.search.autocompleteSearch.allowedPaths");
          if (!StringUtils.isBlank(allowedAutocompleteSearchPaths)) {
            allowAutocompleteSearch = false;
            List<String> allowedPaths = GrouperUtil.splitTrimToList(allowedAutocompleteSearchPaths, ",");
            if (allowedPaths.contains(request.getPathInfo())) {
              allowAutocompleteSearch = true;
            }
          }

          if (allowAutocompleteSearch) {
            //take out the asterisk
            query = StringUtils.replace(query, "*", "");

            //if its a blank query, then dont return anything...
            if (query.length() > 1 || dojoComboQueryLogic.validQueryOverride(grouperSession, query)) {

              Collection<T> results = dojoComboQueryLogic.search(request, grouperSession, query);

              if (results != null) {
                objects.addAll(results);
              }
            }
          } else {
            // prevent response "enter 2 or more characters if skipping autocompletion
            enterMoreChars = allowAutocompleteSearch ? true : false; //yes, can logically shorten it, but then the code is less clear
          }
        }
  
        if (enterMoreChars) {
          String label = TextContainer.retrieveFromRequest().getText().get("comboNotEnoughChars");
          DojoComboDataResponseItem dojoComboDataResponseItem = new DojoComboDataResponseItem(null, 
              label, label);
          dojoComboDataResponse = new DojoComboDataResponse(GrouperUtil.toList(dojoComboDataResponseItem));
        } else {
  
          if (objects.size() == 0) {
            dojoComboDataResponse = new DojoComboDataResponse();
          } else {
            
            List<DojoComboDataResponseItem> items = new ArrayList<DojoComboDataResponseItem>();
      
            //convert stem to item
            for (T t : objects) {
              
              //description could be null?
              String id = GrouperUiUtils.escapeHtml(dojoComboQueryLogic.retrieveId(grouperSession, t), true);
              String label = GrouperUiUtils.escapeHtml(dojoComboQueryLogic.retrieveLabel(grouperSession, t), true);
              String htmlLabel = dojoComboQueryLogic.retrieveHtmlLabel(grouperSession, t);
              
              DojoComboDataResponseItem item = new DojoComboDataResponseItem(id, label, htmlLabel);
              items.add(item);
              
            }
            
            dojoComboDataResponse = new DojoComboDataResponse(
              GrouperUtil.toArray(items, DojoComboDataResponseItem.class));
      
          }  
        }
      }      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    String json = GrouperUtil.jsonConvertTo(dojoComboDataResponse, false);
    
    //write json to screen
    GrouperUiUtils.printToScreen(json, HttpContentType.APPLICATION_JSON, false, false);

    //dont print the regular JSON
    throw new ControllerDone();
    
  }
  
}
