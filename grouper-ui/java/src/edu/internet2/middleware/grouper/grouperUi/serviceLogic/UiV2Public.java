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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * public 
 */
public class UiV2Public extends UiServiceLogicBase {

  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(UiV2Public.class);

  /**
   * index page of application, or public operation used for posts.  since we only have one
   * request path that is a servlet in the public space, then this will be
   * the gatekeeper for all public operations either the index or ajax operations
   * @param request
   * @param response
   */
  public void postIndex(HttpServletRequest request, HttpServletResponse response) {

    //since this is the only public servlet, tunnel the operation through
    String function = request.getParameter("function");
    
    //dont use reflection here for security reasons
    if (StringUtils.equals(function, UiV2Public.class.getSimpleName() + ".error")) {
      error(request, response);
    } else if (StringUtils.equals(function, UiV2Public.class.getSimpleName() + ".help")) {
      help(request, response);
    } else {
      throw new RuntimeException("Invalid function: " + function);
    }


  }

  /**
   * index page of application, or public operation.  since we only have one
   * request path that is a servlet in the public space, then this will be
   * the gatekeeper for all public operations either the index or ajax operations
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();

    //if there is an operation, then we need the main page
    String operation = request.getParameter("operation");
    
    if (!StringUtils.isBlank(operation)) {
      //if this is an error, it could be ajax, so ajax will need to redirect to right place.
      String url = request.getRequestURL().toString() + "?" + request.getQueryString();
      
      //strip off the http / https
      url = url.substring(8);
      //go to the next slash
      url = url.substring(url.indexOf('/')+1);
      //the next slash is the end of the contextroot
      url = url.substring(url.indexOf('/')+1);
      url = "../../" + url;
      
      response.addHeader("X-Grouper-path", GrouperUtil.escapeUrlEncode(url));
  
      //just show a jsp
      showJsp("/WEB-INF/grouperUi2/public/index.jsp");
  
      throw new ControllerDone();
    }  
  }

  public void help(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
            "/WEB-INF/grouperUi2/public/help.jsp"));
  }

  /**
   * ajax operation if there is an error
   * @param request
   * @param response
   */
  public void error(HttpServletRequest request, HttpServletResponse response) {
    String code = request.getParameter("code");

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#preMessaging", 
        TextContainer.retrieveFromRequest().getText().get("guiErrorHeader")));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
        "/WEB-INF/grouperUi2/public/startOver.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
        TextContainer.retrieveFromRequest().getText().get("errorCode_" + code)));

  }

}
