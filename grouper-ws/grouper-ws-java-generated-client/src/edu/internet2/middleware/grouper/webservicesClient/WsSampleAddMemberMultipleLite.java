/**
 * 
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAddMemberLiteResult;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAddMemberMultipleLite {

  /**
   * @param wsSampleGeneratedType if SOAP or XML/HTTP
   */
  public static void addMemberLite(WsSampleGeneratedType wsSampleGeneratedType, String subjectId) {
    try {
      //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
      GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
      Options options = stub._getServiceClient().getOptions();
      HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
      auth.setUsername(GeneratedClientSettings.USER);
      auth.setPassword(GeneratedClientSettings.PASS);
      auth.setPreemptiveAuthentication(true);

      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
      
      options.setProperty(HTTPConstants.AUTHENTICATE, auth);
      options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
      options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, new Integer(3600000));
      
      AddMemberLite addMemberLite = AddMemberLite.class.newInstance();

      //version, e.g. v1_3_000
      addMemberLite.setClientVersion(GeneratedClientSettings.VERSION);

      addMemberLite.setGroupName("test:isc:astt:chris:group");

      addMemberLite.setGroupUuid("");

      addMemberLite.setSubjectId(subjectId);
      addMemberLite.setSubjectSourceId("jdbc");
      addMemberLite.setSubjectIdentifier("");

      // set the act as id
      addMemberLite.setActAsSubjectId("");

      addMemberLite.setActAsSubjectSourceId("");
      addMemberLite.setActAsSubjectIdentifier("");
      addMemberLite.setFieldName("");
      addMemberLite.setIncludeGroupDetail("");
      addMemberLite.setIncludeSubjectDetail("");
      addMemberLite.setSubjectAttributeNames("");
      addMemberLite.setParamName0("");
      addMemberLite.setParamValue0("");
      addMemberLite.setParamName1("");
      addMemberLite.setParamValue1("");

      WsAddMemberLiteResult wsAddMemberLiteResult = stub.addMemberLite(addMemberLite)
          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(wsAddMemberLiteResult,
          ToStringStyle.MULTI_LINE_STYLE));
      System.out.println(ToStringBuilder.reflectionToString(wsAddMemberLiteResult
          .getResultMetadata(), ToStringStyle.MULTI_LINE_STYLE));
      System.out.println(ToStringBuilder.reflectionToString(wsAddMemberLiteResult
          .getSubjectAttributeNames(), ToStringStyle.MULTI_LINE_STYLE));
      System.out.println(ToStringBuilder.reflectionToString(wsAddMemberLiteResult
          .getWsGroupAssigned(), ToStringStyle.MULTI_LINE_STYLE));
      System.out.println(ToStringBuilder.reflectionToString(wsAddMemberLiteResult
          .getWsSubject(), ToStringStyle.MULTI_LINE_STYLE));

      if (!StringUtils
          .equals("T", wsAddMemberLiteResult.getResultMetadata().getSuccess())) {
        throw new RuntimeException("didnt get success! ");
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
      
    //get a rand int string between 0 and 100
    //String randString = Integer.toString((int)(Math.random() * 100));
    
    //note this is oracle specific
//    List<String> subjectIds = HibernateSession.bySqlStatic().listSelect(String.class, 
//          "select penn_id from person_source where penn_id like ? and rownum < 2", 
//          HibUtils.listObject("%" + randString + "%"));
    //set pagesize 1000;
    //select '"' || penn_id || '", ' from person_source where penn_id like '%56%' and rownum < 100;
    
    String[] subjectIds = new String[]{
        "test.subject.0",
        "test.subject.1",
        "test.subject.2",
        "test.subject.3",
        "test.subject.4",
        "test.subject.5",
        "test.subject.6",
        "test.subject.7",
        "test.subject.8",
        "test.subject.9",
        "10000782",             
        "10000784",             
        "10000785",             
        "10000786",             
        "10000787",             
        "10000788",             
        "10000789",             
        "10000878",             
        "10000978",             
        "10001178",             
        "10001278",             
        "10001378",             
        "10001478",             
        "10001578",             
        "10001678",             
        "10001780",             
        "10001781",             
        "10001782",             
        "10001783",             
        "10001785",             
        "10001786",             
        "10001788",             
        "10001978",             
        "10002078",             
        "10002278",             
        "10002378",             
        "10002478",             
        "10002578",             
        "10002678",             
        "10002778",             
        "10002781",             
        "10002783",             
        "10002786",             
        "10002787",             
        "10002878",             
        "10003178",             
        "10003278",             
        "10003378",             
        "10003678",             
        "10003778",             
        "10003780",             
        "10003781",             
        "10003782",             
        "10003783",             
        "10003784",             
        "10003785",             
        "10003787",             
        "10003789",             
        "10003878",             
        "10004278",             
        "10004478",             
        "10004578",             
        "10004678",             
        "10004778",             
        "10004780",             
        "10004781",             
        "10004782",             
        "10004783",             
        "10004784",             
        "10004785",             
        "10004786",             
        "10004787",             
        "10004788",             
        "10004789",             
        "10004978",             
        "10005078",             
        "10005378",             
        "10005478",             
        "10005678",             
        "10005778",             
        "10005780",             
        "10005781",             
        "10005782",             
        "10005783",             
        "10005784",             
        "10005785",             
        "10005786",             
        "10005787",             
        "10005788",             
        "10005789",             
        "10005878",             
        "10006078",             
        "10006178",             
        "10006378",             
        "10006578",             
        "10006678",             
        "10006778",             
        "10006781",             
        "10006782"
        };
    int i=0;
    for (String subjectId : subjectIds) {
      System.out.println("trying: " + i++ + ", " + subjectId);
      addMemberLite(WsSampleGeneratedType.soap, subjectId);
      
    }
  }
}
