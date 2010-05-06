/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

import org.apache.commons.logging.Log;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * Packet listener that will monitor the XMMP server for messages
 * matching a filter and process them
 *
 */
public class EsbXmppPacketListener implements PacketListener {

  private GrouperSession grouperSession;

  private EsbListener esbPublisher;

  private static final Log LOG = GrouperUtil.getLog(EsbXmppPacketListener.class);

  /**
   * Method to process incoming packet
   */
  public void processPacket(Packet packet) {
    // TODO Auto-generated method stub
    if (this.grouperSession == null)
      this.grouperSession = GrouperSession.startRootSession();
    if (this.esbPublisher == null)
      this.esbPublisher = new EsbListener();
    Message message = (Message) packet;
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received message " + message.getBody());
    }
    esbPublisher.processEvent(message.getBody(), grouperSession);
  }

}
