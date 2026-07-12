---
title: "Amazon SNS SQS POC"
space: GrIntDev
pageId: 48795848
version: 6
lastUpdated: 2026-07-12T07:02:38.284Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48795848/Amazon+SNS+SQS+POC
---

This is an SNS POC and load test. I sent 1000 messages in 42 seconds (42ms per send), and received in batches of 10 in a different thread.

Door to door it took less than half a second to be processed.

**Note: you need a recent JRE to run the AWS SNS client.**

```
Sent 1000 in 42002ms
Total records: 3000
Mean: 337.05 ms
Min: 83 ms
Max: 2745 ms
Standard deviation: 287.51 ms
Total took 43148ms

```

Note: messages sent to SNS are wrapped in a JSON envelope, in this case, the message body "test" is sent

```
{
  "Type" : "Notification",
  "MessageId" : "b070a413-79dc-587d-84ec-ed7929fa6544",
  "TopicArn" : "arn:aws:sns:us-east-1:992702096659:chrisTestSns",
  "Message" : "test",
  "Timestamp" : "2013-01-11T17:37:42.645Z",
  "SignatureVersion" : "1",
  "Signature" : "nNEfxsvLg2ISD8bp94HxkGv3MCSA19MPEcEvwZ5i0hOip3oEDVLrArSqa/OCkwOJEnYvVZrF81u2t4HCz/Wf3cQMm9Wmi/hEQ8tdfuoN3LLp6zdPq8/YZRBEg/gpBcCWwoIR4fv15q90k8RWkOkezHwJWyOm/49AXYnK1wJxxhA=",
  "SigningCertURL" : "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem",
  "UnsubscribeURL" : "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:992702096659:chrisTestSns:fb9de8dc-102c-428d-bb4a-44ed896b21a3"
}

```

### Create a topic

### Create 3 queues and subscribe to the topic

Note the queues can be sent to by SNS topic, and the connection user can read (and/or write) from the queue

### Permissions on topic about who can subscribe

### Run test with AWS client jar

You need these jars:

- aws-java-sdk.jar
- commons-codec-1.3.jar
- commons-codec.jar
- commons-lang.jar
- commons-logging-1.1.1.jar
- commons-math.jar
- httpclient-4.1.1.jar
- httpcore-4.1.jar
- log4j.jar

Here is the code

```
/**
 * @author mchyzer
 * $Id$
 */
package edu.upenn.isc.sqs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 *
 */
public class SnsPoc {

  /** credentials */
  private static final AWSCredentials awsCredentials = 
    new BasicAWSCredentials("ABC123", "XYZ789");

  /**
   * 
   */
  private static final AmazonSNSClient amazonSNSClient = new AmazonSNSClient(awsCredentials);
  
  /**
   * sns endpoint url
   */
  private static final String snsEndpointUrl = "arn:aws:sns:us-east-1:992702096659:chrisTestSns";
  
  /**
   * 
   */
  private static void sendMessagePoc() {
    
    PublishRequest publishRequest = new PublishRequest(snsEndpointUrl, "This is a test message");
    
    amazonSNSClient.publish(publishRequest);

  }
  
  /**
   * @param args
   * @throws InterruptedException 
   */
  public static void main(String[] args) throws InterruptedException {
    
    clearQueues();
    
    //amazonSQSClient.deleteMessage(new DeleteMessageRequest(ENDPOINT_URL, "YwnarCAvi2xRhqWMz7Uz35cnUOUalgRDWOD3qedNTy7CiBd5+oPobt8m1gELG3A7sx/De/opxe4QSLQUo138UjVho5Lx18ONffp2g4nTl98L+W+IpIVzqAQe9XVCK98L5SSosmPzAhL6tqBVd+GYunxNoCWkSeeUH3bAmh2qee6XrJqDeBM9hu4LwBinKJEd+2Yo/KPl5PZiEEo8grdYDj55YmFwVgOtfxHfzDueuNSfJcOO+iFPflSMzaId7qHWrLckU4iPdMPzy8B9nHWGGghzSrAW8PJz1z9OdHsr1+4="));

    
    //sendMessagePoc();
    
    runLoadTest(1000);
    
    //String message = "{\n  \"Type\" : \"Notification\",\n  \"MessageId\" : \"2e2f5fbd-9199-5238-9013-16d7c8c305f6\",\n  \"TopicArn\" : \"arn:aws:sns:us-east-1:992702096659:chrisTestSns\",\n  \"Message\" : \"message__102__1357928218045\",\n  \"Timestamp\" : \"2013-01-11T18:16:58.050Z\",\n  \"SignatureVersion\" : \"1\",\n  \"Signature\" : \"g3z8A6tgElJ2n0SH0a3qcVU2/AYWeNQry8Uqejk3NN1IGmuHx30QVCviTaU1iOmvUBOuA4DEDR1SrdzgikLH05/7byos5Nwit3x4s0kHqpdFBBquAFB0RGNEzuBOoq2aYG/TC5XpE6Keq1bBv45/BLQRhjbr3MPiLrXrnEG3iOg=\",\n  \"SigningCertURL\" : \"https://sns.us-east-1.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem\",\n  \"UnsubscribeURL\" : \"https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:992702096659:chrisTestSns:dcbd1d9b-2f70-4450-89a0-01ff15eec735\"\n}";
    //Matcher matcher = pattern.matcher(message);
    //System.out.println(matcher.matches());
  }

  /**
   * sqsEndpointUrls
   */
  private static final String[] sqsEndpointUrls = new String[]{"https://sqs.us-east-1.amazonaws.com/992702096659/chrisTestSns1",
    "https://sqs.us-east-1.amazonaws.com/992702096659/chrisTestSns2", "https://sqs.us-east-1.amazonaws.com/992702096659/chrisTestSns3"};
  
  /**
   * 
   */
  private static void clearQueues() {
    
    for (String queue : sqsEndpointUrls) {
      for (int i=0;i<500;i++) {
        List<String> result = receiveMessages(queue, 3);
        if (result == null || result.size() == 0) {
          break;
        }
        System.out.println("Received and deleted: " + result.size() + " messages");
      }
    }
    
  }
  
  /**
   * @param size 
   * @throws InterruptedException
   */
  private static void runLoadTest(final int size) throws InterruptedException {
    long start = System.nanoTime();
    
    //prime the pump
    sendMessage(-1);
    
    for (String sqsEndpoint: sqsEndpointUrls) {

      List<String> messages = receiveMessages(sqsEndpoint);
      
      if (messages.size() != 1) {
        throw new RuntimeException("Why length not 1!!!! " + messages.size());
      }
      
      System.out.println("Received message: " + messages.get(0));

    }
    
    Thread senderThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        sendMessages(size);
      }
    });
    
    final List<Integer> millisForDelivery = Collections.synchronizedList(new ArrayList<Integer>());
    
    Thread[] receiverThreads = new Thread[3];
    
    for (int threadIndex=0; threadIndex<3; threadIndex++) {
      
      final int THREAD_INDEX = threadIndex;
      
      receiverThreads[threadIndex] = new Thread(new Runnable() {

        @Override
        public void run() {
          
          /**
           * map of index to number of messages received
           */
          Map<Integer, Integer> resultMap = new HashMap<Integer, Integer>();
          
          int previousIndex = -1;
          
          for (int i=0;i<size*2;i++) {
            
            if (resultMap.size() == size) {
              break;
            }
            
            if ((i+1)%10 == 0) {
              System.out.println("Thread: " + THREAD_INDEX + " received " + resultMap.size() + " messages");
            }
            
            List<String> messageBodies = receiveMessages(sqsEndpointUrls[THREAD_INDEX]);
            if (messageBodies == null || messageBodies.size() == 0) {
              continue;
            }
            
            for (String messageBody : messageBodies) {
              
              Matcher matcher = pattern.matcher(messageBody);
              if (matcher.matches()) {
                
                String indexString = matcher.group(1);
                String millisString = matcher.group(2);
                
                Integer index = Integer.parseInt(indexString);
                if (resultMap.containsKey(index)) {
                  resultMap.put(index, resultMap.get(index) + 1);
                  System.out.println("Received duplicate: " + index + ", " + resultMap.get(index) + " times.");
                } else {
                  resultMap.put(index, 1);
                }

                if (index != previousIndex + 1) {
                  //System.out.println("Out of order: previous: " + previousIndex + ", " + index);
                  
                }
                
                previousIndex = index;
                
                //calculate the time
                long millis = Long.parseLong(millisString);
                int duration = (int)(System.currentTimeMillis() - millis);
                millisForDelivery.add(duration);
                
              } else {
                System.out.println("Doesnt match: " + messageBody);
              }
              
            }
            
          }
        }
        
      });
      
    }
    
    
    senderThread.start();
    
    //Thread.sleep(20000);
    
    for (Thread receiverThread : receiverThreads) {
      receiverThread.start();
    }
    
    senderThread.join();

    for (Thread receiverThread : receiverThreads) {
      receiverThread.join();
    }
    
    //compute stats
    //analyze
    double[] millis = new double[millisForDelivery.size()];
    int i=0;
    for (Integer milli : millisForDelivery) {
      millis[i] = milli.doubleValue();
      i++;
    }
    
    DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(millis);
    
    DecimalFormat df = new DecimalFormat("#.##");
    
    System.out.println("Total records: " + millis.length);
    System.out.println("Mean: " + df.format(descriptiveStatistics.getMean()) + " ms");
    System.out.println("Min: " + df.format(descriptiveStatistics.getMin()) + " ms");
    System.out.println("Max: " + df.format(descriptiveStatistics.getMax()) + " ms");
    System.out.println("Standard deviation: " + df.format(descriptiveStatistics.getStandardDeviation()) + " ms");

    
    System.out.println("Total took " + ((System.nanoTime()-start) / 1000000) + "ms");
  }

  /**
   * pattern to parse message: 
   * {
   *  "Type" : "Notification",
   *  "MessageId" : "b070a413-79dc-587d-84ec-ed7929fa6544",
   *  "TopicArn" : "arn:aws:sns:us-east-1:992702096659:chrisTestSns",
   *  "Message" : "message__001__1423234",
   *  "Timestamp" : "2013-01-11T17:37:42.645Z",
   */
  private static final Pattern pattern = Pattern.compile(".*\"Message\"\\s*:\\s*\"message__(\\d+)__(\\d+)\",.*", Pattern.DOTALL);
  
  /**
   * send 1000 messages, time it
   * @param size 
   */
  private static void sendMessages(int size) {
    
    long start = System.nanoTime();
    
    for (int i=0;i<size;i++) {
      sendMessage(i);
    }
    
    System.out.println("Sent 1000 in " + ((System.nanoTime()-start) / 1000000) + "ms");
  }

  /**
   * receive a message and return the body
   * @param endpointUrl 
   * @return the bodies
   */
  private static List<String> receiveMessages(String endpointUrl) {
    return receiveMessages(endpointUrl, 20);
  }

  /**
   * receive a message and return the body
   * @param endpointUrl 
   * @param secondsToWaitForMessages 
   * @return the bodies
   */
  private static List<String> receiveMessages(String endpointUrl, int secondsToWaitForMessages) {
    
    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(endpointUrl);
    
    receiveMessageRequest.setWaitTimeSeconds(secondsToWaitForMessages);
    receiveMessageRequest.setMaxNumberOfMessages(10);
    
    AmazonSQSClient amazonSQSClient = new AmazonSQSClient(awsCredentials);

    ReceiveMessageResult receiveMessageResult = amazonSQSClient.receiveMessage(receiveMessageRequest);

    List<Message> messages = receiveMessageResult.getMessages();
    
    if (messages == null || messages.size() == 0) {
      return null;
    }
    
    List<String> result = new ArrayList<String>();
    
    for (Message message : messages) {
      String receiptHandle = message.getReceiptHandle();
      
      result.add(message.getBody());
      
      //System.out.println("Deleting message: " + receiptHandle);
      
      amazonSQSClient.deleteMessage(new DeleteMessageRequest(endpointUrl, receiptHandle));
      
    }
    
    //sort these since we got them at once
    Collections.sort(result);
    
    return result;
    
  }
  
  /**
   * if -1, test message, if not, then real message
   * @param index
   */
  private static void sendMessage(int index) {
    
    
    String messageBody = index == -1 ? "something: " + System.nanoTime() : "message__" 
      + StringUtils.leftPad(Integer.toString(index), 3, '0') + "__" + System.currentTimeMillis();
    
    PublishRequest publishRequest = new PublishRequest(snsEndpointUrl, messageBody);
    
    amazonSNSClient.publish(publishRequest);

  }
  
}

```

sdf
