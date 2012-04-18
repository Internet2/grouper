/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
