/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
/*
 * @author mchyzer $Id: AsasHttpServletRequestst.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzServer.j2ee;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.rest.AsasRestHttpMethod;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerConfig;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import org.apache.commons.collections.IteratorUtils;

/**
 * wrap request so that no nulls are given to axis (since it handles badly)
 */
public class AsasHttpServletRequest extends HttpServletRequestWrapper {
  
  /**
   * retrieve from threadlocal
   * @return the request
   */
  public static AsasHttpServletRequest retrieve() {
    return (AsasHttpServletRequest)TaasFilterJ2ee.retrieveHttpServletRequest();
  }
  
  /**
   * method for this request
   */
  private String method = null;
  
  /**
   * @see HttpServletRequest#getMethod()
   */
  @Override
  public String getMethod() {
    if (this.method == null) {
      //get it from the URL if it is there
      String methodString = this.getParameter("method");
      if (StandardApiServerUtils.isBlank(methodString)) {
        methodString = super.getMethod();
      }
      //lets see if it is a valid method
      AsasRestHttpMethod.valueOfIgnoreCase(methodString, true);
      this.method = methodString;
    }
    return this.method;
  }

  /**
   * @return original method from underlying servlet
   * @see HttpServletRequest#getMethod()
   */
  public String getOriginalMethod() {
    return super.getMethod();
  }

  /**
   * valid params that the API knows about
   */
  private static Set<String> validParamNames = StandardApiServerUtils.toSet(
      "ascending", 
      "createParentFoldersIfNotExist", 
      "extraFields", 
      "indent", 
      "limit", 
      "membershipType",
      "method", 
      "offset", 
      "offsetFieldValue", 
      "recursive",
      "recursive",
      "saveMode",
      "sortField" 
      );

  
  
  /**
   * construct with underlying request
   * @param theHttpServletRequest
   */
  public AsasHttpServletRequest(HttpServletRequest theHttpServletRequest) {
    super(theHttpServletRequest);
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  @Override
  public String getParameter(String name) {
    return this.getParameterMap().get(name);
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  public Boolean getParameterBoolean(String name) {
    return StandardApiServerUtils.booleanObjectValue(this.getParameterMap().get(name));
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  public Long getParameterLong(String name) {
    return StandardApiServerUtils.longObjectValue(this.getParameterMap().get(name), true);
  }

  /** param map which doesnt return null */
  private Map<String, String> parameterMap = null;

  /** unused http params */
  private Set<String> unusedParams = null;

  /**
   * return unused params that arent in the list to ignore
   * @return the unused params
   */
  public Set<String> unusedParams() {
    //init stuff
    this.getParameterMap();
    return this.unusedParams;
  }
  
  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterMap()
   */
  @Override
  public Map<String, String> getParameterMap() {

    if (this.parameterMap == null) {
      boolean valuesProblem = false;
      Set<String> valuesProblemName = new LinkedHashSet<String>();
      Map<String, String> newMap = new LinkedHashMap<String, String>();
      Set<String> newUnusedParams = new LinkedHashSet<String>();
      @SuppressWarnings("rawtypes")
      Enumeration enumeration = super.getParameterNames();
      Set<String> paramsToIgnore = new HashSet<String>();
      {
        String paramsToIgnoreString = StandardApiServerConfig.retrieveConfig().propertyValueString("tierApiAuthzServer.httpParamsToIgnore");
        if (!StandardApiServerUtils.isBlank(paramsToIgnoreString)) {
          paramsToIgnore.addAll(StandardApiServerUtils.splitTrimToList(paramsToIgnoreString, ","));
        }
      }
      if (enumeration != null) {
        while(enumeration.hasMoreElements()) {
          String paramName = (String)enumeration.nextElement();
          
          if (!validParamNames.contains(paramName)) {
            if (!paramsToIgnore.contains(paramName)) {
            newUnusedParams.add(paramName);
            }
            continue;
          }
          
          String[] values = super.getParameterValues(paramName);
          String value = null;
          if (values != null && values.length > 0) {
            
            //there is probably something wrong if multiple values detected
            if (values.length > 1) {
              valuesProblem = true;
              valuesProblemName.add(paramName);
            }
            value = values[0];
          }
          newMap.put(paramName, value);
        }
      }
      this.parameterMap = newMap;
      this.unusedParams = newUnusedParams;
      if (valuesProblem) {
        throw new AsasRestInvalidRequest(
            "Multiple request parameter values where detected for key: " + StandardApiServerUtils.toStringForLog(valuesProblemName)
                + ", when only one is expected", "400", "ERROR_MULTIPLE_PARAMS");
      }
    }
    return this.parameterMap;
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterNames()
   */
  @SuppressWarnings("rawtypes")
  @Override
  public Enumeration getParameterNames() {
    return IteratorUtils.asEnumeration(this.getParameterMap().keySet().iterator());
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
   */
  @Override
  public String[] getParameterValues(String name) {
    if (this.getParameterMap().containsKey(name)) {
      return new String[]{this.getParameterMap().get(name)};
    }
    return null;
  }

}
