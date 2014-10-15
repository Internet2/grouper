/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAwsChangelog;

import java.util.List;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouperClient.GrouperClient;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GrouperAwsSqsListener {

  /**
   * @param args
   */
  public static void main(String[] args) {

    String accessKey = GrouperClientUtils.propertiesValue("grouperClient.awsAccessKey", true);
    String secretKey = GrouperClientUtils.propertiesValue("grouperClient.awsSecretKey", true);

    //e.g. https://sqs.us-east-1.amazonaws.com/060107389071/isc_jira_penngroups
    String queueUrl = GrouperClientUtils.propertiesValue("grouperClient.awsSqsQueueUrl", true);

    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

    AmazonSQSClient sqs = new AmazonSQSClient(credentials);
    
    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
    List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
    for (Message message : messages) {
      String json = message.getBody();
      String receiptHandle = message.getReceiptHandle();
      //      Message
      //      MessageId:     255dc371-b828-4295-9428-2e233053808e
      //      ReceiptHandle: v5iiyMGi3b6MIj3gF2FKMtGGDhxUAsGgrwlat43jo+ymvjam0xW08ej9H7xhUBPoh5KTuC7adO1h7NdXT4k8NwFvt5eMI6dR5XKyhDgg0t7trpzT9klFXsfBJJn4rZvj2MjsoBSfX2xLbPQIzqHPoHsAo6Ccucxmj9ATPQqw9XBC5D6SUv/K6GcnEQ0ce04mcS9FIRHeaUxV/I3gbhtmuh0jfcHCs+CwyaJDc1+bDxQ3ICGGPI1Vvi7rL1HaZZsiUDS2ERXObXwRiEDvxQzafDjczY2q+o/81z9OdHsr1+4=
      //      MD5OfBody:     d6f80bbd0025860aa66a2edb85fb36d7
      //      Body:          {
      //    "Type" : "Notification",
      //    "MessageId" : "2aaeb2f7-cd80-5ce8-a667-fe40333d324f",
      //    "TopicArn" : "arn:aws:sns:us-east-1:060107389071:isc_jira_penngroups",
      //    "Message" : "{\"esbEvent\":[{\"eventType\":\"MEMBERSHIP_ADD\",\"fieldName\":\"members\",\"groupId\":\"4854cde794b34948911bfea5b2acb611\",\"groupName\":\"atlassian:jira:jira-users\",\"id\":\"9950174f0c6e4b508a5017f28b18ccfc\",\"membershipType\":\"flattened\",\"sequenceNumber\":\"386\",\"sourceId\":\"jdbc\",\"subjectId\":\"test.subject.1\"}]}",
      //    "Timestamp" : "2014-10-15T04:47:57.010Z",
      //    "SignatureVersion" : "1",
      //    "Signature" : "q1KXeTL8/J8TLPMSURrMAKf26ehBOtpndMBsUQcVrJ8u9JqlcaamQyCeZ0gasKK0hcHsRWbgYVyImxqUlDaaNTq6vjJ+5UentlyYj7SMETVGDblFhMkp4oM7FG0NhAKzb/RK09f7UKwKlJpdHXvRVIVM/Ai8AR9+PvnerWz8WhK7ghQEt+yEDE1TIO4UO9gIH4z12f0DrR/IkqynH4Lq40C05pn4ANL4CAON3O7ML0kMZSjtgSnYx0hR33Lw3Y9Ax0l2shTjhflRNPV2Ne3SZGQyQdmraThNHkBJdvwk6qhcN4oXGf5QWlDFOytjuv6uqJAGpqvAVE5w6OKt/ePDmg==",
      //    "SigningCertURL" : "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-d6d679a1d18e95c2f9ffcf11f4f9e198.pem",
      //    "UnsubscribeURL" : "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:060107389071:isc_jira_penngroups:304c0ea8-5b78-414f-adb9-442785286b16"
      //  }      

      JSONObject jsonObject = JSONObject.fromObject(json);

      {
        //unwrap if not raw message
        JSONObject tempObject = (JSONObject)jsonObject.get("Message");
        if (tempObject != null) {
          jsonObject = tempObject;
        }
      }
      
      JSONArray jsonArray = (JSONArray)(jsonObject).get("esbEvent");
      
      for (int i=0;i<jsonArray.size();i++) {
        jsonObject = (JSONObject) jsonArray.get(i);
      
        System.out.println(jsonObject.get("eventType"));
  
        sqs.deleteMessage(queueUrl, receiptHandle);
        
        //sqs.deleteMessage();
        //        System.out.println("  Message");
  //        System.out.println("    MessageId:     " + message.getMessageId());
  //        System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
  //        System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
  //        System.out.println("    Body:          " + message.getBody());
  //        for (Entry<String, String> entry : message.getAttributes().entrySet()) {
  //            System.out.println("  Attribute");
  //            System.out.println("    Name:  " + entry.getKey());
  //            System.out.println("    Value: " + entry.getValue());
  //        }
      }
    }
    System.out.println();
    
  }

}
