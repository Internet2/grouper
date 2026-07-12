---
title: "Amazon SQS POC"
space: GrIntDev
pageId: 48795856
version: 7
lastUpdated: 2026-07-12T07:02:38.701Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48795856/Amazon+SQS+POC
---

Here is an Amazon POC. I sent 1000 messages in 17 seconds (17ms per send), and received in batches of 10 in a different thread. Door to door it took less than a second to be processed. Note, it says received 994 since the initial ones were tossed out. All messages sent were successfully received

```
Sent 1000 in 17221ms
Received 994 messages
Total records: 1000
Mean: 808.87 ms
Min: 40 ms
Max: 3359 ms
Standard deviation: 642.91 ms
Total took 18733ms

```

### Create queue

Create a queue in amazon

### Permissions

Create a user (which is essentially a key and secret) in IAM

Assign permissions (shown above), note, give all 6 for send/receive, for receive assign all but send. For send, assign all but receive and delete.

### Run test

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

Run the code with AWS client jar:

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
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 *
 */
public class SqsPoc {

  /**
   *
   */
  private static final String ENDPOINT_URL = "https://sqs.us-east-1.amazonaws.com/992702096659/chrisTest";
  /** credentials */
  private static final AWSCredentials awsCredentials =
    new BasicAWSCredentials("xxxxxxxx", "xxxabc");

  private static final AmazonSQSClient amazonSQSClient = new AmazonSQSClient(awsCredentials);

  /**
   *
   */
  private static void sendMessagePoc() {

    AmazonSQSClient amazonSQSClient = new AmazonSQSClient(awsCredentials);

    SendMessageRequest sendMessageRequest = new SendMessageRequest(
        ENDPOINT_URL,
        "This is a message body: " + System.nanoTime());

    amazonSQSClient.sendMessage(sendMessageRequest);

  }

  /**
   * @param args
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException {

    //clearQueue();

    //amazonSQSClient.deleteMessage(new DeleteMessageRequest(ENDPOINT_URL, "YwnarCAvi2xRhqWMz7Uz35cnUOUalgRDWOD3qedNTy7CiBd5+oPobt8m1gELG3A7sx/De/opxe4QSLQUo138UjVho5Lx18ONffp2g4nTl98L+W+IpIVzqAQe9XVCK98L5SSosmPzAhL6tqBVd+GYunxNoCWkSeeUH3bAmh2qee6XrJqDeBM9hu4LwBinKJEd+2Yo/KPl5PZiEEo8grdYDj55YmFwVgOtfxHfzDueuNSfJcOO+iFPflSMzaId7qHWrLckU4iPdMPzy8B9nHWGGghzSrAW8PJz1z9OdHsr1+4="));

    //sendMessagePoc();

    runLoadTest();

  }

  /**
   *
   */
  private static void clearQueue() {
    for (int i=0;i<500;i++) {
      List<String> result = receiveMessages();
      if (result == null || result.size() == 0) {
        break;
      }
      System.out.println("Received and deleted: " + result.size() + " messages");
    }
  }

  /**
   * @throws InterruptedException
   */
  private static void runLoadTest() throws InterruptedException {
    long start = System.nanoTime();

    //prime the pump
    sendMessage(-1);

    List<String> messages = receiveMessages();

    if (messages.size() != 1) {
      throw new RuntimeException("Why length not 1!!!! " + messages.size());
    }

    System.out.println("Received message: " + messages.get(0));

    Thread senderThread = new Thread(new Runnable() {

      @Override
      public void run() {
        send1000messages();
      }
    });

    final List<Integer> millisForDelivery = new ArrayList<Integer>();

    Thread receiverThread = new Thread(new Runnable() {

      @Override
      public void run() {

        /**
         * map of index to number of messages received
         */
        Map<Integer, Integer> resultMap = new HashMap<Integer, Integer>();

        int previousIndex = -1;

        for (int i=0;i<2000;i++) {

          if (resultMap.size() == 1000) {
            break;
          }

          if ((i+1)%10 == 0) {
            System.out.println("Received " + resultMap.size() + " messages");
          }

          List<String> messageBodies = receiveMessages();
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

    senderThread.start();

    //Thread.sleep(20000);

    receiverThread.start();

    senderThread.join();
    receiverThread.join();

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
   * pattern to parse message: message__001__1423234
   */
  private static final Pattern pattern = Pattern.compile("^message__(\\d+)__(\\d+)$");

  /**
   * send 1000 messages, time it
   */
  private static void send1000messages() {

    long start = System.nanoTime();

    for (int i=0;i<1000;i++) {
      sendMessage(i);
    }

    System.out.println("Sent 1000 in " + ((System.nanoTime()-start) / 1000000) + "ms");
  }

  /**
   * receive a message and return the body
   * @return the bodies
   */
  private static List<String> receiveMessages() {

    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(ENDPOINT_URL);

    receiveMessageRequest.setWaitTimeSeconds(20);
    receiveMessageRequest.setMaxNumberOfMessages(10);

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

      amazonSQSClient.deleteMessage(new DeleteMessageRequest(ENDPOINT_URL, receiptHandle));

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

    SendMessageRequest sendMessageRequest = new SendMessageRequest(
        ENDPOINT_URL, messageBody
        );

    amazonSQSClient.sendMessage(sendMessageRequest);

  }

}

```

sdf
