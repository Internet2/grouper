/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

public abstract class EsbListenerBase {

  public abstract boolean dispatchEvent(String eventJsonString, String consumerName);

  public abstract void disconnect();
}
