/*
 * @author mchyzer $Id: GrouperUiRestServlet.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.j2ee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.grouperUi.beans.RequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.AppState;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiSettings;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.InviteExternalSubjects;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.SimpleAttributeUpdateFilter;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.SimpleMembershipUpdateFilter;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.SimpleMembershipUpdateImportExport;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.SimpleMembershipUpdateMenu;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter.UiSection;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

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
      InviteExternalSubjects.class.getSimpleName() + ".groupToAssignFilter",
      SimpleMembershipUpdateFilter.class.getSimpleName() + ".filterUsers", 
      "SimpleMembershipUpdateFilter.filterGroups",
      SimpleMembershipUpdateMenu.class.getSimpleName() + ".advancedMenuStructure", 
      SimpleMembershipUpdateImportExport.class.getSimpleName() + ".exportSubjectIdsCsv",
      "SimpleMembershipUpdateImportExport.exportAllCsv", "SimpleMembershipUpdateMenu.memberMenuStructure",
      "SimpleMembershipUpdateFilter.filterMembers", "SimpleAttributeUpdateFilter.filterAttributeDefs",
      "SimpleAttributeUpdateFilter.filterCreatableNamespace", 
      SimpleAttributeUpdateFilter.class.getSimpleName() + ".filterPrivilegeUsers");

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
    this.doGet(request, response);
    
  }

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @SuppressWarnings({ "unchecked" })
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    GrouperUiFilter.assignHttpServlet(this);
    
    RequestContainer.retrieveFromRequest().setAjaxRequest(true);
    
    List<String> urlStrings = extractUrlStrings(request);
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String appStateString = request.getParameter("appState");
    JSONObject jsonObject = JSONObject.fromObject(appStateString);

    AppState appState = (AppState)JSONObject.toBean( jsonObject, AppState.class ); 

    //app state isnt there e.g. for ajax components
    if (appState == null) {
      appState = new AppState();
    }
    
    appState.initRequest();
    
    try {
      initGui();
    } catch (ControllerDone cd) {
      guiResponseJs.printToScreen();
      return;
    }
    
    boolean printToScreen = true;

    //this is just the filename of the export, for the browser to save correctly if right click
    if (GrouperUtil.length(urlStrings) == 3
        && StringUtils.equals("app", urlStrings.get(0))
        && (StringUtils.equals("SimpleMembershipUpdateImportExport.exportSubjectIdsCsv", urlStrings.get(1))
            || StringUtils.equals("SimpleMembershipUpdateImportExport.exportAllCsv", urlStrings.get(1)))) {
      //strip off the filename
      urlStrings = GrouperUtil.toList(urlStrings.get(0), urlStrings.get(1));
    }
    
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
          String errorMessage = "Cant process method: " + request.getMethod() + " for operation: " + classAndMethodName;
          throw new RuntimeException(errorMessage);
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

        

      } catch (NoSessionException nse) {

        boolean addTextArea = guiResponseJs.isAddTextAreaTag();
        
        //print out error message for user, a new one
        guiResponseJs = new GuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newCloseModal());
        guiResponseJs.setAddTextAreaTag(addTextArea);
        
        UiSection uiSection = GrouperUiFilter.uiSectionForRequest();
        
        String startOverKey = "simpleMembershipUpdate.startOver";
        
        if (uiSection == UiSection.EXTERNAL) {
          startOverKey = "externalSubjectSelfRegister.startOver";
        }
          
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(startOverKey)));

      } catch (ControllerDone cd) {
        printToScreen = cd.isPrintGuiReponseJs();
        //do nothing, this is ok
      } catch (RuntimeException re) {
        String error = "Problem calling reflection from URL: " + className + "." + methodName + "\n\n" + ExceptionUtils.getFullStackTrace(re);
        LOG.error(error);
        GrouperUiUtils.appendErrorToRequest(error);
        
        //if adding text area for form file submits, make sure to do that in errors too
        boolean addTextArea = guiResponseJs.isAddTextAreaTag();
        
        //print out error message for user, a new one
        guiResponseJs = new GuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newCloseModal());
        guiResponseJs.setAddTextAreaTag(addTextArea);
        guiResponseJs.addAction(GuiScreenAction.newAlert("Error: " + GrouperUiUtils.escapeHtml(re.getMessage(), true)));
        
      }
    } else {
      //print out error message for user, a new one
      guiResponseJs = new GuiResponseJs();
      String error = "Cant find logic for URL: " 
        + GrouperUtil.toStringForLog(urlStrings);
      guiResponseJs.addAction(GuiScreenAction.newAlert("Error: " + error));
      LOG.error(error);
      GrouperUiUtils.appendErrorToRequest(error);
    }
    
    if (printToScreen) {
      guiResponseJs.printToScreen();
    }
  }

  /**
   * init stuff on gui
   */
  public static void initGui() {
    GuiSettings guiSettings = new GuiSettings();
    
    guiSettings.storeToRequest();
    
    guiSettings.setAuthnKey(GrouperUuid.getUuid());
    
    Subject loggedInSubject = null;
    
    try {
      loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      guiSettings.setLoggedInSubject(new GuiSubject(loggedInSubject));
    } catch (SubjectNotFoundException snfe) {
      UiSection uiSection = GrouperUiFilter.uiSectionForRequest();
      //if its anonymous, then there might not be a subject logged in
      if (!uiSection.isAnonymous()) {
        throw snfe;
      }
    }
    
    // lets see where the templates are: assume grouperUiText.properties is in WEB-INF/classes,
    // and templates are WEB-INF/templates
    AppState appState = AppState.retrieveFromRequest();
    if (!appState.isInitted()) {

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newAssign("allObjects.guiSettings", guiSettings));  
      
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
