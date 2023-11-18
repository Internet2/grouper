/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;



/**
 * base class of service logic for UI
 * @author mchyzer
 *
 */
public class UiServiceLogicBase {

  /**
   * hand over control to a jsp in WEB-INF/jsp
   * @param jspNameInWebInfJspDir
   */
  protected void showJsp(String jspNameInWebInfJspDir) {
    
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    HttpServletResponse httpServletResponse = GrouperUiFilter.retrieveHttpServletResponse();
    
    //e.g. /WEB-INF/grouperUi2/jsp/index.jsp
    RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(jspNameInWebInfJspDir);

    if (!httpServletResponse.isCommitted()) {
      httpServletResponse.setHeader("Connection", "close");
    }
    try {
      dispatcher.forward(httpServletRequest, httpServletResponse);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}
