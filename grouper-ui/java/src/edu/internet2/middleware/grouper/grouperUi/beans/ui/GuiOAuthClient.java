/*******************************************************************************
 * Copyright 2024 Internet2
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
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.internet2.middleware.grouper.authentication.GrouperOAuthClient;

/**
 * GUI wrapper for GrouperOAuthClient that provides formatted fields
 * for display in JSP pages. Does NOT expose the client secret.
 *
 * @author mchyzer
 */
public class GuiOAuthClient {

  /** the underlying OAuth client object */
  private GrouperOAuthClient grouperOAuthClient;

  /**
   * constructor
   * @param grouperOAuthClient1
   */
  public GuiOAuthClient(GrouperOAuthClient grouperOAuthClient1) {
    this.grouperOAuthClient = grouperOAuthClient1;
  }

  /**
   * @return the underlying OAuth client object
   */
  public GrouperOAuthClient getGrouperOAuthClient() {
    return this.grouperOAuthClient;
  }

  /**
   * @param grouperOAuthClient1 the OAuth client object
   */
  public void setGrouperOAuthClient(GrouperOAuthClient grouperOAuthClient1) {
    this.grouperOAuthClient = grouperOAuthClient1;
  }

  /**
   * get the registered time formatted as yyyy-MM-dd HH:mm:ss
   * @return the formatted date string
   */
  public String getRegisteredTimeFormatted() {
    if (this.grouperOAuthClient == null
        || this.grouperOAuthClient.getRegisteredMicros() == null) {
      return "";
    }
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
        new Date(this.grouperOAuthClient.getRegisteredMicros() / 1000));
  }

  /**
   * convert a list of GrouperOAuthClient to a list of GuiOAuthClient
   * @param grouperOAuthClients
   * @return the list of gui wrappers
   */
  public static List<GuiOAuthClient> convertFromGrouperOAuthClients(
      List<GrouperOAuthClient> grouperOAuthClients) {
    List<GuiOAuthClient> result = new ArrayList<GuiOAuthClient>();
    if (grouperOAuthClients != null) {
      for (GrouperOAuthClient client : grouperOAuthClients) {
        result.add(new GuiOAuthClient(client));
      }
    }
    return result;
  }
}
