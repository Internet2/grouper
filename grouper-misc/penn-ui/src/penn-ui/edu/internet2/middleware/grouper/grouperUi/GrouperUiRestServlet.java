/*
 * @author mchyzer $Id: GrouperUiRestServlet.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.grouperUi.json.GuiSettings;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.soap.WsGroup;

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

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @SuppressWarnings({ "unchecked", "cast" })
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    GrouperUiJ2ee.assignHttpServlet(this);
    
    //I think we are all post all the time, right?
    if (!StringUtils.equalsIgnoreCase("post", request.getMethod() )) {
      throw new RuntimeException("Cant process method: " + request.getMethod());
    }
    
    List<String> urlStrings = extractUrlStrings(request);
    
    Object objectToPrint = null;

    //see what operation we are doing
    if (GrouperUtil.length(urlStrings) == 3 
        && StringUtils.equals("app", urlStrings.get(0))) {

      String className = urlStrings.get(1);
      
      //lets do some simple validation
      if (!className.matches("^[a-zA-Z0-9]+$")) {
        throw new RuntimeException("Invalid classname: '" + className + "'");
      }
      
      String methodName = urlStrings.get(2);
      
      if (!methodName.matches("^[a-zA-Z0-9]+$")) {
        throw new RuntimeException("Invalid methodName: '" + methodName + "'");
      }
      
      //now lets call some simple reflection, must be public static void and take a request and response
      className = "edu.internet2.middleware.grouper.grouperUi.serviceLogic." + className;
      
      Object instance = GrouperUtil.newInstance(GrouperUtil.forName(className));
      
      try {
        
        objectToPrint = GrouperUtil.callMethod(instance.getClass(), instance, methodName, 
            new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class}, 
            new Object[]{request, response}, true, false);
      } catch (RuntimeException re) {
        GrouperUtil.injectInException(re, "Problem calling reflection from URL: " + className + "." + methodName);
        throw re;
      }
    } else {
      throw new RuntimeException("Cant find logic for URL: " 
          + GrouperUtil.toStringForLog(urlStrings));
    }
    
    if (objectToPrint == null) {
      throw new RuntimeException("Why is objectToPrint null???");
    }
    
    //for some controls we arent even sending json
    if (objectToPrint != null) {
      //take the object to print (bean) and print it
      JSONObject jsonObject = net.sf.json.JSONObject.fromObject( objectToPrint );  
      String json = jsonObject.toString();
      
      PrintWriter printWriter = response.getWriter();
  
      printWriter.write(json);
      printWriter.flush();
    }
    
    HttpSession httpSession = request.getSession(false);
    if (httpSession != null) {
      httpSession.invalidate();
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
