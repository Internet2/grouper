/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAwsChangelog;

import org.apache.commons.logging.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * change log consumer that goes to
 */
public class GrouperAwsEsbPublisher extends EsbListenerBase  {

  /** */
  private static final Log LOG = GrouperUtil.getLog(GrouperAwsEsbPublisher.class);

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#dispatchEvent(java.lang.String, java.lang.String)
   */
  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    
    String accessKey = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.awsAccessKey", true);
    String secretKey = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.awsSecretKey", true);
    //e.g. US_EAST_1
    String region = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.awsRegion", true);

    //e.g. arn:aws:sns:us-east-1:060107389071:isc_jira_penngroups
    String topicArn = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.awsSnsTopicArn", true);
    
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    
    AmazonSNSClient snsClient = new AmazonSNSClient(credentials);                               
    Regions regions = null;
    try {
      regions = Regions.fromName(region);
    } catch (RuntimeException re) {
      regions = Regions.valueOf(region);
    }
    snsClient.setRegion(Region.getRegion(regions));
    
    //publish to an SNS topic
    PublishRequest publishRequest = new PublishRequest(topicArn, eventJsonString);
    PublishResult publishResult = snsClient.publish(publishRequest);
    
    if (LOG.isDebugEnabled()) {
      //print MessageId of message published to SNS topic
      LOG.debug("AWS SNS message ID - " + publishResult.getMessageId());
    }
    
    return true;
  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#disconnect()
   */
  @Override
  public void disconnect() {
  }

  
  
  
}
