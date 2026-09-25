/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.coresoap.GrouperService;
import edu.internet2.middleware.grouper.ws.coresoap.WsResultMeta;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestServlet;
import edu.internet2.middleware.grouper.ws.rest.WsRestClassLookup;

/**
 * the parts of GrouperServiceUtils which need the servlet API or the web service endpoint class.
 *
 * <p>GrouperServiceUtils is otherwise ordinary logic which any caller can use, so it belongs with
 * the rest of the logic.  These three members are what kept it tied to the war, and every caller
 * of them is itself part of the HTTP edge, so nothing outside grouper-ws loses anything by their
 * being here.</p>
 */
public class GrouperWsHttpUtils {

  /** lite object type param name */
  private static final String WS_LITE_OBJECT_TYPE = GrouperServiceUtils.WS_LITE_OBJECT_TYPE;

  /**
   * the web service endpoint class
   * @return the class
   */
  public static Class<?> currentServiceClass() {
    return GrouperService.class;
  }

  /**
   * take the http params and marshal them into an object
   * @param paramMap
   * @param httpServletRequest if not null, make sure no dupes.  if null, forget it
   * @param warnings is the warnings, if null, just throw exception
   * @return the object or null if nothing in query params
   */
  public static Object marshalHttpParamsToObject(Map<String, String[]> paramMap,
      HttpServletRequest httpServletRequest, StringBuilder warnings) {

    String objectTypeSimpleName = GrouperServiceJ2ee.parameterValue(paramMap, httpServletRequest, WS_LITE_OBJECT_TYPE);
    Set<String> namesForWarnings = new HashSet<String>();
    //if no object, then set warnings
    boolean hasObjectType = !StringUtils.isBlank(objectTypeSimpleName);
    Object object = null;
    if (!hasObjectType) {
      namesForWarnings.addAll(paramMap.keySet());
    } else {

      Class<?> objectClass = WsRestClassLookup.retrieveClassBySimpleName(objectTypeSimpleName);
      object = GrouperUtil.newInstance(objectClass);

      //lets assign other params
      for (String key: paramMap.keySet()) {
        if (StringUtils.equals(key, WS_LITE_OBJECT_TYPE)) {
          continue;
        }
        String value = GrouperServiceJ2ee.parameterValue(paramMap, httpServletRequest, key);
        Method method = GrouperUtil.setter(objectClass, key, true, false);
        if (method == null) {
          namesForWarnings.add(key);
        } else {
          //should we see if scalar, or try to assign?  hmmmm... hopefully the
          //scalar list is up to date
          if (!GrouperUtil.isScalar(method.getParameterTypes()[0])) {
            namesForWarnings.add(key);
          } else {
            //if null or blank, then forget it
            if (StringUtils.isBlank(value)) {
              continue;
            }
            try {
              GrouperUtil.assignSetter(object, key, value, true);
            } catch (RuntimeException re) {
              throw new WsInvalidQueryException("Problem assigning object: " + objectClass
                  + ", property: " + key + ", value: " + value, re);
            }
          }
        }
      }
    }
    StringBuilder theWarnings = new StringBuilder();
    if (namesForWarnings.size() > 0) {
      theWarnings.append("Cant find properties to assign HTTP params: ");
      for (String property: namesForWarnings) {
        theWarnings.append(property).append(", ");
      }
      theWarnings.append("\n");
      if (warnings == null) {
        throw new RuntimeException(theWarnings.toString());
      }
      //append the warnings to the stringbuilder passed in
      warnings.append(theWarnings);
    }
    return object;

  }

  /**
   * add response headers for a success and response code
   * @param response
   * @param success T or F
   * @param resultCode
   * @param resultCode2
   */
  public static void addResponseHeaders(HttpServletResponse response, String success,
      String resultCode, String resultCode2) {
    if (!response.containsHeader(GrouperRestServlet.X_GROUPER_RESULT_CODE)) {
      //default to NONE if not set for some reason (NONE will give clue that something wrong, why isnt it set???)
      response.addHeader(GrouperRestServlet.X_GROUPER_RESULT_CODE, StringUtils.defaultIfEmpty(resultCode, "NONE"));
    }
    if (!response.containsHeader(GrouperRestServlet.X_GROUPER_SUCCESS)) {
      //default to F if not set for some reason
      response.addHeader(GrouperRestServlet.X_GROUPER_SUCCESS, StringUtils.defaultIfEmpty(success, "F"));
    }
    if (!response.containsHeader(GrouperRestServlet.X_GROUPER_RESULT_CODE2)) {
      //default to NONE if not set for some reason (NONE will give clue that something wrong, why isnt it set???)
      response.addHeader(GrouperRestServlet.X_GROUPER_RESULT_CODE2, StringUtils.defaultIfEmpty(resultCode2, "NONE"));
    }
  }

  /**
   * add response headers for a success and response code
   * will retrieve the response object from threadlocal
   * @param wsResultMeta result metadata
   * @param isSoap if soap
   */
  public static void addResponseHeaders(WsResultMeta wsResultMeta, boolean isSoap) {
    HttpServletResponse httpServletResponse = GrouperServiceJ2ee
        .retrieveHttpServletResponse();
    addResponseHeaders(httpServletResponse, wsResultMeta.getSuccess(), wsResultMeta
        .getResultCode(), wsResultMeta.getResultCode2());
  }

}
