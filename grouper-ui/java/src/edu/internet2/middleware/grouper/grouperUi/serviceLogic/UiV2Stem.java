package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * operations in the stem screen
 * @author mchyzer
 *
 */
public class UiV2Stem {

  /**
   * the filter button was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filter(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      String stemId = request.getParameter("stemId");

      Stem stem = null;
      boolean addedError = false;
      if (!StringUtils.isBlank(stemId)) {
        if (StringUtils.equals("root", stemId)) {
          stem = StemFinder.findRootStem(grouperSession);
        } else {
          stem = StemFinder.findByUuid(grouperSession, stemId, false);
        }
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemCantFindStemId")));
        addedError = true;
      }

      if (stem != null) {
        grouperRequestContainer.getStemContainer().setGuiStem(new GuiStem(stem));      
        
        String filterText = request.getParameter("filterText");
        grouperRequestContainer.getStemContainer().setFilterText(filterText);
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemFilterResultsId", 
            "/WEB-INF/grouperUi2/stem/stemContents.jsp"));
      } else {
        
        if (!addedError && (!StringUtils.isBlank(stemId))) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("stemCantFindStem")));
        }
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/index/indexMain.jsp"));
      }

      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * view stem
   * @param request
   * @param response
   */
  public void viewStem(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      Stem stem = null;

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String stemId = request.getParameter("stemId");
      String stemIndex = request.getParameter("stemIndex");
      String stemName = request.getParameter("stemName");
      
      boolean addedError = false;
      
      if (!StringUtils.isBlank(stemId)) {
        if (StringUtils.equals("root", stemId)) {
          stem = StemFinder.findRootStem(grouperSession);
        } else {
          stem = StemFinder.findByUuid(grouperSession, stemId, false);
        }
      } else if (!StringUtils.isBlank(stemName)) {
        stem = StemFinder.findByName(grouperSession, stemName, false);
      } else if (!StringUtils.isBlank(stemIndex)) {
        long idIndex = GrouperUtil.longValue(stemIndex);
        stem = StemFinder.findByIdIndex(idIndex, false, null);
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemCantFindStemId")));
        addedError = true;
      }

      if (stem != null) {
        grouperRequestContainer.getStemContainer().setGuiStem(new GuiStem(stem));      
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/stem/viewStem.jsp"));
      } else {
        
        if (!addedError && (!StringUtils.isBlank(stemId) || !StringUtils.isBlank(stemName) || !StringUtils.isBlank(stemIndex))) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("stemCantFindStem")));
        }
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/index/indexMain.jsp"));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }


}
