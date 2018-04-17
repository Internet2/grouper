/*******************************************************************************
 * Copyright 2012 Internet2
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
  private Map<String, String[]> parameterMap = null;

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterMap()
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, String[]> getParameterMap() {

    if (this.parameterMap == null) {

      Map<String, String[]> existingMap = super.getParameterMap();
      Map<String, String[]> newMap = new LinkedHashMap<String, String[]>(existingMap);
      for (String key : newMap.keySet()) {
        if (newMap.get(key) == null) {
          newMap.put(key, new String[] {""});
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
