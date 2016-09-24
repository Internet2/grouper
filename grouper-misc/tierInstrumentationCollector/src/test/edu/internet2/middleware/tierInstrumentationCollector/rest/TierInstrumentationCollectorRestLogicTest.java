package edu.internet2.middleware.tierInstrumentationCollector.rest;

import java.util.HashMap;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PostMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.StringRequestEntity;
import edu.internet2.middleware.tierInstrumentationCollector.corebeans.TicResponseBeanBase;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import net.sf.json.JSONObject;

/**
 * test the rest logic
 * @author mchyzer
 *
 */
@SuppressWarnings("unused")
public class TierInstrumentationCollectorRestLogicTest extends TestCase {

  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new TierInstrumentationCollectorRestLogicTest("testUploadSave"));
    runClient();
  }

  /**
   * construct
   */
  public TierInstrumentationCollectorRestLogicTest() {
    super();
  }
  
  /**
   * 
   */
  public static void runClient() {
    try {
      HttpClient httpClient = new HttpClient();
      PostMethod postMethod = new PostMethod("http://localhost:8080/tierInstrumentationCollector/tierInstrumentationCollector/v1/upload");
      String json = "{reportFormat: 1, component: \"grouper\", institution: \"Penn\", version: \"2.3.0\", "
          + "patchesInstalled: \"api1, api2, api4, ws2\", wsServerCount: 3, platformLinux: true, uiServerCount: 1, "
          + "pspngCount: 1, provisionToLdap: true, registrySize: 12345678, transactionCountMemberships: 1234567, "
          + "transactionCountPrivileges: 1234, transactionCountPermissions: 123}";
      postMethod.setRequestEntity(new StringRequestEntity(json, "application-json", "UTF-8"));
      int result = httpClient.executeMethod(postMethod);
      System.out.println(result);
    } catch (Exception e) {
      throw new RuntimeException("Error", e);
    }
  }

  /**
   * construct
   * @param name
   */
  public TierInstrumentationCollectorRestLogicTest(String name) {
    super(name);
  }

  /**
   * test upload save
   */
  public void testUploadSave() {

    //lts make a json object like an upload object
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("componentName", "grouper");
    jsonObject.put("entryVersion", "1.0");
    jsonObject.put("institution", "Penn");
    jsonObject.put("environment", "prod");
    jsonObject.put("componentVersion", "2.3.0");
    jsonObject.put("runningPspng", true);

    TicResponseBeanBase ticResponseBeanBase = TierInstrumentationCollectorRestLogic.uploadSave(jsonObject, new HashMap<String, String>());

    System.out.print(ticResponseBeanBase);
    
  }
  
}
