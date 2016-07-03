/*
 * @author mchyzer $Id: AsasRestHttpMethodva,v 1.5 2008-03-29 10:50:43 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzClient.ws;

import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientUtils;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

/**
 * types of http methods accepted by grouper rest
 */
public enum AsacRestHttpMethod {

  /** GET */
  GET {

    /**
     * @see AsacRestHttpMethod#httpMethod(String)
     * make a get method
     */
    @Override
    public HttpMethodBase httpMethod(String url) {
      return new GetMethod(url);
    }


  },

  /** POST */
  POST {

    /**
     * @see AsacRestHttpMethod#httpMethod(String)
     * make a get method
     */
    @Override
    public HttpMethodBase httpMethod(String url) {
      return new PostMethod(url);
    }

  },

  /** PUT */
  PUT {

    /**
     * @see AsacRestHttpMethod#httpMethod(String)
     * make a get method
     */
    @Override
    public HttpMethodBase httpMethod(String url) {
      return new PutMethod(url);
    }

  },

  /** DELETE */
  DELETE {

    /**
     * @see AsacRestHttpMethod#httpMethod(String)
     * make a get method
     */
    @Override
    public HttpMethodBase httpMethod(String url) {
      return new DeleteMethod(url);
    }
  };

  /**
   * make the method for httpClient
   * @param url
   * @return the method
   */
  public abstract HttpMethodBase httpMethod(String url);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static AsacRestHttpMethod valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) {
    return StandardApiClientUtils.enumValueOfIgnoreCase(AsacRestHttpMethod.class, string, exceptionOnNotFound);
  }
}
