/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.messaging;

import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;


/**
 *
 */
public class GrouperMessageUtils {
  
  /**
   * message root stem
   * @return the message root stem
   */
  public static String messageRootStemName() {
    return GrouperCheckConfig.attributeRootStemName() + ":messages";
  }

  /**
   * grouper message topic name of attribute def
   * @return the name
   */
  public static String grouperMessageTopicNameOfDef() {
    return messageRootStemName() + ":grouperMessageTopicDef";
  }

  /**
   * grouper message queue name of attribute def
   * @return the name
   */
  public static String grouperMessageQueueNameOfDef() {
    return messageRootStemName() + ":grouperMessageQueueDef";
  }

  /**
   * topic stem name
   * @return topic stem name
   */
  public static String topicStemName() {
    return messageRootStemName() + ":grouperMessageTopics";
  }
}
