/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAwsChangelog;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.xmpp.EsbEvents;
import edu.internet2.middleware.grouperClientExt.xmpp.GcDecodeEsbEvents;


/**
 *
 */
public class GrouperAwsSqsListener {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    List<GrouperSqsMessage> grouperSqsMessages = checkMessages(true);
    
    for (GrouperSqsMessage grouperSqsMessage : grouperSqsMessages) {
      //{"esbEvent":[{"changeOccurred":true,"eventType":"MEMBERSHIP_DELETE","sequenceNumber":"392"}]}
      String json = grouperSqsMessage.getMessageBody();
      EsbEvents esbEvents = GcDecodeEsbEvents.decodeEsbEvents(json);
      esbEvents = GcDecodeEsbEvents.unencryptEsbEvents(esbEvents);
      System.out.println(esbEvents.getEsbEvent()[0].getEventType());
      deleteMessage(grouperSqsMessage.getReceiptHandle());
    }
    
  }

  /**
   * delete a message that was processed
   * @param receiptHandle to delete
   */
  public static void deleteMessage(String receiptHandle) {
    String accessKey = GrouperClientUtils.propertiesValue("grouperClient.awsAccessKey", true);
    accessKey = GrouperClientUtils.decryptFromFileIfFileExists(accessKey, null);
    String secretKey = GrouperClientUtils.propertiesValue("grouperClient.awsSecretKey", true);
    secretKey = GrouperClientUtils.decryptFromFileIfFileExists(secretKey, null);
    
    //e.g. https://sqs.us-east-1.amazonaws.com/060107389071/isc_jira_penngroups
    String queueUrl = GrouperClientUtils.propertiesValue("grouperClient.awsSqsQueueUrl", true);
    deleteMessage(accessKey, secretKey, queueUrl, receiptHandle);
  }

  /**
   * get a list of messages based on sqs credentials.  note, this might poll for up to 20 seconds if the queue is so defined.
   * it will loop until there are messages... note, this will unwrap sns messages if applicable
   * @param accessKey for aws
   * @param secretKey for aws
   * @param queueUrl for aws
   * @param receiptHandle to delete
   */
  public static void deleteMessage(String accessKey, String secretKey, String queueUrl, String receiptHandle) {
    
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

    AmazonSQSClient sqs = new AmazonSQSClient(credentials);
    sqs.deleteMessage(queueUrl, receiptHandle);
    
  }

  /**
   * check messages with default credentials in the grouper.client.properties
   * @param waitForMessages if we should wait until there are messages there
   * @return the list of messages
   */
  public static List<GrouperSqsMessage> checkMessages(boolean waitForMessages) {
    String accessKey = GrouperClientUtils.propertiesValue("grouperClient.awsAccessKey", true);
    accessKey = GrouperClientUtils.decryptFromFileIfFileExists(accessKey, null);
    String secretKey = GrouperClientUtils.propertiesValue("grouperClient.awsSecretKey", true);
    secretKey = GrouperClientUtils.decryptFromFileIfFileExists(secretKey, null);
    
    //e.g. https://sqs.us-east-1.amazonaws.com/060107389071/isc_jira_penngroups
    String queueUrl = GrouperClientUtils.propertiesValue("grouperClient.awsSqsQueueUrl", true);
    return checkMessages(accessKey, secretKey, queueUrl, waitForMessages);
  }

  /**
   * get a list of messages based on sqs credentials.  note, this might poll for up to 20 seconds if the queue is so defined.
   * it will loop until there are messages... note, this will unwrap sns messages if applicable
   * @param accessKey for aws
   * @param secretKey for aws
   * @param queueUrl for aws
   * @param waitForMessages if we should wait until there are messages there
   * @return the list of messages or null if none and not wait for messages
   */
  public static List<GrouperSqsMessage> checkMessages(String accessKey, String secretKey, String queueUrl, boolean waitForMessages) {

    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

    AmazonSQSClient sqs = new AmazonSQSClient(credentials);
    
    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
    List<Message> messages = null;

    List<GrouperSqsMessage> result = new ArrayList<GrouperSqsMessage>();
    
    while(true) {
      //dont check more than every 19.5 seconds
      long lastCheck = System.nanoTime();
      messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
      
      //if we have messages, then return them (after processing below)
      if (GrouperClientUtils.length(messages) > 0) {
        break;
      }

      if (!waitForMessages) {
        return null;
      }
      
      //sleep until 19.5 seconds since the last check
      long timeSinceLastCheckMillis = ((System.nanoTime() - lastCheck) / 1000000);
      if (timeSinceLastCheckMillis < 19500) {
        GrouperClientUtils.sleep(19500 - timeSinceLastCheckMillis);
      }
      
    }

    //at this point we have messages
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

      //see if we need to unwrap an sns to sqs that doesnt have the setting about not wrapping
      if (json.contains("\"Message\"")) {

        JSONObject jsonObject = JSONObject.fromObject(json);

        {
          //unwrap if not raw message
          JSONObject tempObject = (JSONObject)jsonObject.get("Message");
          if (tempObject != null) {
            jsonObject = tempObject;
            json = jsonObject.toString();
          }
        }
      }
      
      GrouperSqsMessage grouperSqsMessage = new GrouperSqsMessage();
      grouperSqsMessage.setMessageBody(json);
      grouperSqsMessage.setReceiptHandle(receiptHandle);
      
      result.add(grouperSqsMessage);
      
      //JSONArray jsonArray = (JSONArray)(jsonObject).get("esbEvent");
      
      //for (int i=0;i<jsonArray.size();i++) {
        //jsonObject = (JSONObject) jsonArray.get(i);
      
        //System.out.println(jsonObject.get("eventType"));
  
        //sqs.deleteMessage(queueUrl, receiptHandle);
        
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
    return result;
  }
  
}
