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
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 * The experimental ESB XMPP listener has been removed for security.
 * <p>
 * It used to log in to an XMPP (Jabber) server as esb.listeners.xmpp.username and pass every
 * chat message whose sender address CONTAINED esb.listeners.xmpp.sendername (a substring
 * match, so other accounts matched too) to {@link EsbListener}, which added and removed
 * memberships of any group as GrouperSystem.  There was no other authorization.
 * <p>
 * This replacement class is shipped in patches because a drop-in cannot delete a class from
 * the Grouper jar, and GrouperLoader still schedules this class by name when
 * esb.listeners.xmpp.enable is true.  It does not connect to anything; it logs an error and
 * returns.  Set esb.listeners.xmpp.enable to false (the default) to stop the error.
 *
 */
@DisallowConcurrentExecution
public class EsbXmppListener implements Job {

  /** */
  private static final Log LOG = GrouperUtil.getLog(EsbXmppListener.class);

  /** error logged when the job is started */
  static final String REMOVED_MESSAGE = "The experimental ESB XMPP listener (esb.listeners.xmpp.enable) "
      + "has been removed for security and will not start: it let any XMPP sender whose address "
      + "contains esb.listeners.xmpp.sendername change memberships as GrouperSystem.  "
      + "Set esb.listeners.xmpp.enable = false in grouper-loader.properties.";

  /**
   * Method invoked by Quartz to start the client.  Refuses to start: logs an error and returns.
   */
  public void execute(JobExecutionContext context)
      throws JobExecutionException {
    LOG.error(REMOVED_MESSAGE);
  }

}
