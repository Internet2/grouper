/*
 * @author mchyzer $Id: WsHttpServletRequest.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;

/**
 * wrap request so that no nulls are given to axis (since it handles badly)
 */
@SuppressWarnings("deprecation")
public class WsHttpServletRequest extends HttpServletRequestWrapper {

  /**
   * construct with underlying request
   * @param theHttpServletRequest
   */
  public WsHttpServletRequest(HttpServletRequest theHttpServletRequest) {
    super(theHttpServletRequest);
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  @Override
  public String getParameter(String name) {
    //return empty not null
    return StringUtils.defaultString(super.getParameter(name));
  }

  /** param map which doesnt return null */
  private Map<String, String> parameterMap = null;

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterMap()
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, String> getParameterMap() {

    if (this.parameterMap == null) {

      Map<String, String> existingMap = super.getParameterMap();
      Map<String, String> newMap = new LinkedHashMap<String, String>(existingMap);
      for (String key : newMap.keySet()) {
        if (newMap.get(key) == null) {
          newMap.put(key, "");
        }
      }
      this.parameterMap = newMap;
    }
    return this.parameterMap;
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
   */
  @Override
  public String[] getParameterValues(String name) {
    String[] defaultValue = super.getParameterValues(name);
    if ((defaultValue == null || (defaultValue.length == 1 && defaultValue[0] == null))
        && this.getParameterMap().containsKey(name)) {
      return new String[] { "" };
    }
    return defaultValue;
  }

}
