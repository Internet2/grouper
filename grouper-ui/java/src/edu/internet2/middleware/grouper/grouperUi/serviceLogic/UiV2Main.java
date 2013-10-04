package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


public class UiV2Main extends UiServiceLogicBase {

  
  /**
   * change a column to my favorites
   * @param request
   * @param response
   */
  public void indexColMyFavorites(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexMyFavorites.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    
  }
  
  /**
   * change a column to my services
   * @param request
   * @param response
   */
  public void indexColMyServices(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexMyServices.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  
  /**
   * change a column to groups I manage
   * @param request
   * @param response
   */
  public void indexColGroupsImanage(HttpServletRequest request, HttpServletResponse response) {
    

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexGroupsImanage.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    
  }
  
  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      //just show a jsp
      showJsp("/WEB-INF/grouperUi2/index/index.jsp");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    throw new ControllerDone();
  }

}
