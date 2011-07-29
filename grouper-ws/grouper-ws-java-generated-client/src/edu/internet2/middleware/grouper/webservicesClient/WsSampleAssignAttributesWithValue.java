/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignAttributes;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAssignAttributesWithValue implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    assignAttributesWithValue(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    assignAttributesWithValue(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void assignAttributesWithValue(
      WsSampleGeneratedType wsSampleGeneratedType) {
    try {
      //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
      GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
      Options options = stub._getServiceClient().getOptions();
      HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
      auth.setUsername(GeneratedClientSettings.USER);
      auth.setPassword(GeneratedClientSettings.PASS);
      auth.setPreemptiveAuthentication(true);

      options.setProperty(HTTPConstants.AUTHENTICATE, auth);
      options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
      options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
          new Integer(3600000));

      AssignAttributes assignAttributes = AssignAttributes.class.newInstance();

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      
      assignAttributes.setActAsSubjectLookup(actAsSubject);
      assignAttributes.setActions(new String[]{null});
      assignAttributes.setAssignmentDisabledTime("");
      assignAttributes.setAssignmentEnabledTime("");
      assignAttributes.setAssignmentNotes("");
      assignAttributes.setAttributeAssignOperation("assign_attr");
      assignAttributes.setAttributeAssignType("group");
      
      //this is from: AttributeAssignValueOperation
      assignAttributes.setAttributeAssignValueOperation("add_value");
      
      //version, e.g. v1_6_000
      assignAttributes.setClientVersion(GeneratedClientSettings.VERSION);
      
      assignAttributes.setDelegatable("");
      
      assignAttributes.setIncludeGroupDetail("");
      assignAttributes.setIncludeSubjectDetail("");
      
      assignAttributes.setParams(new WsParam[]{null});
      
      assignAttributes.setSubjectAttributeNames(new String[]{null});
      
      assignAttributes.setWsAttributeAssignLookups(new WsAttributeAssignLookup[]{null});
      WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
      wsAttributeAssignValue.setValueSystem("29");
      assignAttributes.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue});
      assignAttributes.setWsAttributeAssignLookups(new WsAttributeAssignLookup[]{null});
      
      WsAttributeDefNameLookup wsAttributeDefNameLookup = WsAttributeDefNameLookup.class.newInstance();
      wsAttributeDefNameLookup.setUuid("");
      wsAttributeDefNameLookup.setName("test:testAttributeAssignDefName");
      assignAttributes.setWsAttributeDefNameLookups(new WsAttributeDefNameLookup[]{wsAttributeDefNameLookup});

      assignAttributes.setWsOwnerAttributeAssignLookups(new WsAttributeAssignLookup[]{null});
      assignAttributes.setWsOwnerAttributeDefLookups(new WsAttributeDefLookup[]{null});
      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      wsGroupLookup.setGroupName("test:groupTestAttrAssign");
      wsGroupLookup.setUuid("");
      assignAttributes.setWsOwnerGroupLookups(new WsGroupLookup[]{wsGroupLookup});
      assignAttributes.setWsOwnerMembershipAnyLookups(new WsMembershipAnyLookup[]{null});
      assignAttributes.setWsOwnerMembershipLookups(new WsMembershipLookup[]{null});
      assignAttributes.setWsOwnerStemLookups(new WsStemLookup[]{null});
      assignAttributes.setWsOwnerSubjectLookups(new WsSubjectLookup[]{null});
      
      WsAssignAttributesResults wsAssignAttributesResults = stub.assignAttributes(assignAttributes)
          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
          wsAssignAttributesResults));

      WsAssignAttributeResult[] wsAssignAttributeResultArray = wsAssignAttributesResults.getWsAttributeAssignResults();

      for (WsAssignAttributeResult wsAssignAttributeResult : wsAssignAttributeResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsAssignAttributeResult));
      }
      
      WsGroup[] wsGroupsResultArray = wsAssignAttributesResults.getWsGroups();

      for (WsGroup wsGroup : wsGroupsResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsGroup));
      }
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
