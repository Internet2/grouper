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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiService;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ServiceContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.service.ServiceUtils;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * main logic for ui
 */
public class UiV2Service extends UiServiceLogicBase {

  /**
   * results from retrieving results
   *
   */
  public static class RetrieveServiceHelperResult {

    /**
     * attributeDefName
     */
    private AttributeDefName attributeDefName;

    /**
     * attributeDefName
     * @return attributeDefName
     */
    public AttributeDefName getAttributeDefName() {
      return this.attributeDefName;
    }

    /**
     * attributeDefName
     * @param attributeDefName1
     */
    public void setAttributeDefName(AttributeDefName attributeDefName1) {
      this.attributeDefName = attributeDefName1;
    }
    
    /**
     * if added error to screen
     */
    private boolean addedError;

    /**
     * if added error to screen
     * @return if error
     */
    public boolean isAddedError() {
      return this.addedError;
    }

    /**
     * if added error to screen
     * @param addedError1
     */
    public void setAddedError(boolean addedError1) {
      this.addedError = addedError1;
    }
    
    
    
  }

  /**
   * get the service from the request
   * @param request
   * @param requireService
   * @return the stem finder result
   */
  public static RetrieveServiceHelperResult retrieveServiceHelper(HttpServletRequest request, boolean requireService) {

    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    RetrieveServiceHelperResult result = new RetrieveServiceHelperResult();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String attributeDefNameId = request.getParameter("idOfAttributeDefName");
    if (StringUtils.isBlank(attributeDefNameId)) {
      attributeDefNameId = request.getParameter("attributeDefNameId");
    }
    String attributeDefNameIndex = request.getParameter("attributeDefNameIndex");
    String attributeDefNameName = request.getParameter("nameOfAttributeDefName");
    if (StringUtils.isBlank(attributeDefNameName)) {
      attributeDefNameName = request.getParameter("attrbuteDefNameName");
    }
    
    boolean addedError = false;
    AttributeDefName attributeDefName = null;
    
    if (!StringUtils.isBlank(attributeDefNameId)) {
    
      attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByUuidOrName(attributeDefNameId, null, false);
      
    } else if (!StringUtils.isBlank(attributeDefNameName)) {

      attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByUuidOrName(null, attributeDefNameName, false);
      
    } else if (!StringUtils.isBlank(attributeDefNameIndex)) {

      attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByIdIndex(GrouperUtil.longObjectValue(attributeDefNameIndex, false), false, null);
 
    } else {
      
      if (!requireService) {
        return result;
      }

      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("viewServiceCantFindServiceId")));
      addedError = true;

    }

    if (attributeDefName != null) {

      grouperRequestContainer.getServiceContainer().setGuiService(new GuiService(new GuiAttributeDefName(attributeDefName), null));

      result.setAttributeDefName(attributeDefName);

    } else {

      if (!requireService) {
        return result;
      }

      if (!addedError && (!StringUtils.isBlank(attributeDefNameId) || !StringUtils.isBlank(attributeDefNameName) || !StringUtils.isBlank(attributeDefNameIndex))) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("viewServiceCantFindService")));
        addedError = true;
      }
      
    }
    result.setAddedError(addedError);
    
    //go back to the main screen, cant find stem
    if (addedError) {
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/indexMain.jsp"));
    }
    
    return result;
  }
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(UiV2Service.class);

  /**
   * view folders for one service
   * @param request
   * @param response
   */
  public void viewService(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      RetrieveServiceHelperResult retrieveServiceHelperResult = retrieveServiceHelper(request, true);
      
      if (retrieveServiceHelperResult.isAddedError()) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/service/viewService.jsp"));
  
      
      serviceHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
     * the filter button was pressed on the my services page, or paging or sorting, or something
     * @param request
     * @param response
     */
    private void serviceHelper(HttpServletRequest request, HttpServletResponse response) {
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      ServiceContainer serviceContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getServiceContainer();
  
      GuiPaging guiPaging = serviceContainer.getGuiPaging();

      final QueryOptions queryOptions = QueryOptions.create("displayName", true, null, null);
      
      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

      Set<Stem> results = null;
      
      results = GrouperUtil.nonNull(
          ServiceUtils.retrieveStemsForService(serviceContainer.getGuiService()
              .getGuiAttributeDefName().getAttributeDefName().getId(), queryOptions));

      if (GrouperUtil.length(results) == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("viewServiceNoResultsFound")));
      }     

      serviceContainer.setGuiStemsInService(GuiStem.convertFromStems(results));
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#viewServiceResultsId", 
          "/WEB-INF/grouperUi2/service/viewServiceContents.jsp"));
  }

  /**
   * my services reset button
   * @param request
   * @param response
   */
  public void serviceReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      RetrieveServiceHelperResult retrieveServiceHelperResult = retrieveServiceHelper(request, true);
      
      if (retrieveServiceHelperResult.isAddedError()) {
        return;
      }

      //get the unfiltered stems
      serviceHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * view service page or whatever
   * @param request
   * @param response
   */
  public void viewServiceSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      RetrieveServiceHelperResult retrieveServiceHelperResult = retrieveServiceHelper(request, true);
      
      if (retrieveServiceHelperResult.isAddedError()) {
        return;
      }

      serviceHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
