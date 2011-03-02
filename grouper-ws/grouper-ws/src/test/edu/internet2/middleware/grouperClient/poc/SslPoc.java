/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.poc;

import edu.internet2.middleware.grouperClient.ssl.EasySslSocketFactory;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.GetMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.protocol.Protocol;


/**
 *
 */
public class SslPoc {

  /**
   * @param args
   * @throws Exception 
   */
  @SuppressWarnings("deprecation")
  public static void main(String[] args) throws Exception {
    
    Protocol easyhttps = new Protocol("https", new EasySslSocketFactory(), 443);
    Protocol.registerProtocol("https", easyhttps);

    
    HttpClient httpClient = new HttpClient();

    GetMethod method = new GetMethod(
        "https://cosign-test-1.net.isc.upenn.edu/~jorj/file1.html");

    int resultCode = httpClient.executeMethod(method);
    
    System.out.println("resultCode: " + resultCode);
    
    
  }

}
