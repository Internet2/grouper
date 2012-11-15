/*
 * @author mchyzer $Id: AsasHttpServletRequestst.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiServer.j2ee;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import edu.internet2.middleware.authzStandardApiServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.authzStandardApiServer.rest.AsasRestHttpMethod;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerConfig;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.collections.IteratorUtils;

/**
 * wrap request so that no nulls are given to axis (since it handles badly)
 */
public class AsasHttpServletRequest extends HttpServletRequestWrapper {
  
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
      "indent", "method");

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
        String paramsToIgnoreString = StandardApiServerConfig.retrieveConfig().propertyValueString("authzStandardApiServer.httpParamsToIgnore");
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
                + ", when only one is expected");
      }
    }
    return this.parameterMap;
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterNames()
   */
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
