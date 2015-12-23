/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

import org.apache.commons.logging.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xmpp.XmppConnectionBean;

/**
 * 
 * Class to start a persistent XMMP client that will remain connected to XMPP
 * server using an instance of {@link EsbXmppPacketListener} to receive events
 * sent to it to process as changes in the Grouper registry. Messages are 
 * filtered on sender name. All configuration in grouper-loader.properties
 *
 */
public class EsbXmppListener implements Job {

  /** */
  private static final Log LOG = GrouperUtil.getLog(EsbXmppListener.class);

  /**
   * method to start the client
   * @param jobDataMap
   */
  private void startListenerClient(JobDataMap jobDataMap) {
    String server = jobDataMap.getString("server");
    int port = Integer.parseInt(jobDataMap.getString("port"));
    String username = jobDataMap.getString("username");
    String password = jobDataMap.getString("password");
    String sendername = jobDataMap.getString("sendername");
    String resource = jobDataMap.getString("resource");

    XmppConnectionBean xmppConnectionBean = new XmppConnectionBean(server, port, username, resource, password);
    
    XMPPConnection xmppConnection = xmppConnectionBean.xmppConnection();
    
    LOG.info("XMPP listener connected to " + xmppConnectionBean.xmppServer() + " on port " + xmppConnectionBean.xmppPort());

    Chat newChat = xmppConnectionBean.chat(sendername, null);
    if (LOG.isDebugEnabled()) {
      try {
        newChat.sendMessage("Grouper listener online");
      } catch (XMPPException e) {
        LOG.debug("Error Delivering block " + e.getMessage());
      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(".. creating packet listener with sender filter " + sendername);
    }
    PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class),
            new FromContainsFilter(sendername));

    // Next, create a packet listener
    PacketListener listener = new EsbXmppPacketListener();
    // Register the listener.
    xmppConnection.addPacketListener(listener, filter);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Packet listener created and added to connection");
    }
  }

  /**
   * Method invoked by Quartz to start the client
   */
  public void execute(JobExecutionContext context)
      throws JobExecutionException {
    // TODO Auto-generated method stub
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    this.startListenerClient(jobDataMap);
  }

}
