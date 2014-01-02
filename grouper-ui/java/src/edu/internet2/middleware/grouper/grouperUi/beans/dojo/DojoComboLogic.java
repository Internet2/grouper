
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
  
        Set<T> objects = new LinkedHashSet<T>();
        boolean enterMoreChars = false;
        
        {
          String groupIdOrName = query.endsWith("*") ? query.substring(0, query.length()-1) : query;
          if (!StringUtils.isBlank(groupIdOrName)) {

            T t = dojoComboQueryLogic.lookup(request, grouperSession, query);
            if (t != null) {
              objects.add(t);
            }
          }
        }

        if (!isLookup) {

          //take out the asterisk
          query = StringUtils.replace(query, "*", "");

          //if its a blank query, then dont return anything...
          if (query.length() > 1) {

            Collection<T> results = dojoComboQueryLogic.search(request, grouperSession, query);
            
            if (results != null) {
              objects.addAll(results);
            }
            
          } else {
            enterMoreChars = true;
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
