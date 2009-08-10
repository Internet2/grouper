/*
 * @author mchyzer $Id: GrouperUiRestServlet.java,v 1.6 2009-08-10 03:27:45 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ezmorph.bean.BeanMorpher;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.grouperUi.json.AppState;
import edu.internet2.middleware.grouper.grouperUi.json.GuiHideShow;
import edu.internet2.middleware.grouper.grouperUi.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.json.GuiSettings;
import edu.internet2.middleware.grouper.grouperUi.json.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * servlet for rest ui web services
 */
@SuppressWarnings("serial")
public class GrouperUiRestServlet extends HttpServlet {

  /**
   * response header for if this is a success or not T or F
   */
  public static final String X_GROUPER_SUCCESS = "X-Grouper-success";

  /**
   * response header for the grouper response code
   */
  public static final String X_GROUPER_RESULT_CODE = "X-Grouper-resultCode";

  /**
   * response header for the grouper response code
   */
  public static final String X_GROUPER_RESULT_CODE2 = "X-Grouper-resultCode2";

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperUiRestServlet.class);

  /** uris that it is ok to get (e.g. auto complete and other ajax components */
  private static Set<String> operationsOkGet = GrouperUtil.toSet(
      "SimpleMembershipUpdate.filterUsers", "SimpleMembershipUpdate.filterGroups",
      "SimpleMembershipUpdate.advancedMenuStructure");
  
  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @SuppressWarnings({ "unchecked" })
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    GrouperUiJ2ee.assignHttpServlet(this);
    
    List<String> urlStrings = extractUrlStrings(request);
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String appStateString = request.getParameter("appState");
    JSONObject jsonObject = JSONObject.fromObject(appStateString);

    AppState appState = (AppState)JSONObject.toBean( jsonObject, AppState.class ); 

    //app state isnt there e.g. for ajax components
    if (appState == null) {
      appState = new AppState();
    }
    
    SessionContainer sessionContainer = SessionContainer.retrieveFromSession();
    
    {
      //save screen state of all the hide shows which are in session
      Map<String, GuiHideShow> appStateHideShows = appState.getHideShows();
      
      BeanMorpher beanMorpher = new BeanMorpher(GuiHideShow.class, JSONUtils.getMorpherRegistry());
      
      if (appStateHideShows != null) {
        Map<String, GuiHideShow> newAppStateHideShows = new LinkedHashMap<String, GuiHideShow>();
        
        for (String hideShowName : appStateHideShows.keySet()) {
          
          //morph this
          GuiHideShow appStateHideShow = (GuiHideShow)beanMorpher.morph(appStateHideShows.get(hideShowName));
          newAppStateHideShows.put(hideShowName, appStateHideShow);
        }
  
        appState.setHideShows(newAppStateHideShows);
        appStateHideShows = newAppStateHideShows;
        
        for (String hideShowName : appStateHideShows.keySet()) {
          GuiHideShow sessionHideShow = sessionContainer.getHideShows().get(hideShowName);
          if (sessionHideShow != null) {
            
            //copy over the current state
            GuiHideShow appStateHideShow = appStateHideShows.get(hideShowName);
            sessionHideShow.setShowing( appStateHideShow.isShowing());
            
          }
        }
      }
    }
    
    {
      //convert pagers to real object model
      Map<String, GuiPaging> appStatePagers = appState.getPagers();
      
      BeanMorpher beanMorpher = new BeanMorpher(GuiPaging.class, JSONUtils.getMorpherRegistry());
      
      if (appStatePagers != null) {
        Map<String, GuiPaging> newAppStatePagers = new LinkedHashMap<String, GuiPaging>();
        
        for (String pagerName : appStatePagers.keySet()) {
          
          //morph this
          GuiPaging appStateHideShow = (GuiPaging)beanMorpher.morph(appStatePagers.get(pagerName));
          newAppStatePagers.put(pagerName, appStateHideShow);
        }
  
        appState.setPagers(newAppStatePagers);
      }
    }    
    
    appState.storeToRequest();
    
    initGui();
    
    boolean printToScreen = true;

    //see what operation we are doing
    if (GrouperUtil.length(urlStrings) == 2 
        && StringUtils.equals("app", urlStrings.get(0))) {

      String classAndMethodName = urlStrings.get(1);

      //lets do some simple validation, text dot text
      if (!classAndMethodName.matches("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")) {
        throw new RuntimeException("Invalid class and method name: '" + classAndMethodName + "'");
      }

      //I think we are all post all the time, right?
      if (!StringUtils.equalsIgnoreCase("post", request.getMethod() )) {
        if (!operationsOkGet.contains(classAndMethodName)) {
          throw new RuntimeException("Cant process method: " + request.getMethod() + " for operation: " + classAndMethodName);
        }
      }
      
      String className = GrouperUtil.prefixOrSuffix(classAndMethodName, ".", true);
      String methodName = GrouperUtil.prefixOrSuffix(classAndMethodName, ".", false);
      
      //now lets call some simple reflection, must be public static void and take a request and response
      className = "edu.internet2.middleware.grouper.grouperUi.serviceLogic." + className;
      
      Object instance = GrouperUtil.newInstance(GrouperUtil.forName(className));
      
      try {
        
        GrouperUtil.callMethod(instance.getClass(), instance, methodName, 
            new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class}, 
            new Object[]{request, response}, true, false);

        

      } catch (ControllerDone cd) {
        printToScreen = cd.isPrintGuiReponseJs();
        //do nothing, this is ok
      } catch (RuntimeException re) {
        LOG.error("Problem calling reflection from URL: " + className + "." + methodName, re);
        
        //print out error message for user, a new one
        guiResponseJs = new GuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newAlert("Error: " + GuiUtils.escapeHtml(re.getMessage(), true)));
        
      }
    } else {
      //print out error message for user, a new one
      guiResponseJs = new GuiResponseJs();
      String error = "Cant find logic for URL: " 
        + GrouperUtil.toStringForLog(urlStrings);
      guiResponseJs.addAction(GuiScreenAction.newAlert("Error: " + error));
      LOG.error(error);
    }
    
    if (printToScreen) {
      //take the object to print (bean) and print it
      jsonObject = net.sf.json.JSONObject.fromObject( guiResponseJs );  
      String json = jsonObject.toString();
      
      GuiUtils.printToScreen(json, "application/json", false, false);
    }
    
  }

  /**
   * init stuff on gui
   */
  public static void initGui() {
    GuiSettings guiSettings = new GuiSettings();
    
    guiSettings.storeToRequest();
    
    guiSettings.setAuthnKey(GrouperUuid.getUuid());
    
    Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    guiSettings.setLoggedInSubject(new GuiSubject(loggedInSubject));
    
    // lets see where the templates are: assume grouperUiText.properties is in WEB-INF/classes,
    // and templates are WEB-INF/templates
    AppState appState = AppState.retrieveFromRequest();
    if (!appState.isInitted()) {

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newAssign("allObjects.guiSettings", guiSettings));
      guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
          + GuiUtils.message("simpleMembershipUpdate.title", false) + "'"));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
          "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));
  
      
      guiResponseJs.addAction(GuiScreenAction.newAssign("allObjects.appState.initted", true));
    }
    
  }
  
  /**
   * for error messages, get a detailed report of the request
   * @param request
   * @return the string of descriptive result
   */
  public static String requestDebugInfo(HttpServletRequest request) {
    StringBuilder result = new StringBuilder();
    result.append(" uri: ").append(request.getRequestURI());
    result.append(", method: ").append(request.getMethod());
    result.append(", decoded url strings: ");
    List<String> urlStrings = extractUrlStrings(request);
    int urlStringsLength = GrouperUtil.length(urlStrings);
    if (urlStringsLength == 0) {
      result.append("[none]");
    } else {
      for (int i = 0; i < urlStringsLength; i++) {
        result.append(i).append(": '").append(urlStrings.get(i)).append("'");
        if (i != urlStringsLength - 1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }

  /**
   * TODO change to GrouperRestServlet in next release
   * take a request and get the list of url strings for the rest web service
   * @see #extractUrlStrings(String)
   * @param request is the request to get the url strings out of
   * @return the list of url strings
   */
  private static List<String> extractUrlStrings(HttpServletRequest request) {
    String requestResourceFull = request.getRequestURI();
    return extractUrlStrings(requestResourceFull);
  }

  /**
   * TODO change to GrouperRestServlet in next release
   * <pre>
   * take a request uri and break up the url strings not including the app name or servlet
   * this does not include the url params (if applicable)
   * if the input is: grouper-ws/servicesRest/xhtml/v1_3_000/groups/members
   * then the result is a list of size 2: {"group", "members"}
   * 
   * </pre>
   * @param requestResourceFull
   * @return the url strings
   */
  private static List<String> extractUrlStrings(String requestResourceFull) {
    String[] requestResources = StringUtils.split(requestResourceFull, '/');
    List<String> urlStrings = new ArrayList<String>();
    //loop through and decode
    int index = 0;
    for (String requestResource : requestResources) {
      //skip the app name and lite servlet
      if (index++ < 2) {
        continue;
      }
      //unescape the url encoding
      urlStrings.add(GrouperUtil.escapeUrlDecode(requestResource));
    }
    return urlStrings;
  }

}
