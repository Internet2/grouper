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

/**
 * UI bean container for OAuth consent page data. This container holds fields
 * related to the OAuth 2.1 authorization consent flow and is not specific to MCP.
 * @author mchyzer
 */
public class OAuthContainer {

  /**
   * the request ID for the pending OAuth authorization
   */
  private String requestId;

  /**
   * the client application name requesting access
   */
  private String clientName;

  /**
   * the client ID
   */
  private String clientId;

  /**
   * the requested scope
   */
  private String scope;

  /**
   * the display name of the logged-in user
   */
  private String loggedInUserName;

  /**
   * error message if any
   */
  private String errorMessage;

  /**
   * whether to show the readonly operations checkbox (user is in readonly group)
   */
  private boolean showReadonly;

  /**
   * whether to show the readwrite operations checkbox (user is in readwrite group)
   */
  private boolean showReadwrite;

  /**
   * whether to show the SQL readonly checkbox (user is in SQL readonly group)
   */
  private boolean showSqlReadonly;

  /**
   * whether to show the admin readonly checkbox (user is in admin readonly group)
   */
  private boolean showAdminReadonly;

  /**
   * whether to show the admin readwrite checkbox (user is in admin readwrite group)
   */
  private boolean showAdminReadwrite;

  /**
   * whether readwrite data scope restrictions (folders, groups, subjects) are required
   * on the consent page. comes from grouper.mcp.oauth.requireReadwriteDataScope config.
   */
  private boolean requireReadwriteDataScope = true;

  /**
   * @return the requestId
   */
  public String getRequestId() {
    return this.requestId;
  }

  /**
   * @param requestId the requestId to set
   */
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  /**
   * @return the clientName
   */
  public String getClientName() {
    return this.clientName;
  }

  /**
   * @param clientName the clientName to set
   */
  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  /**
   * @return the clientId
   */
  public String getClientId() {
    return this.clientId;
  }

  /**
   * @param clientId the clientId to set
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * @return the scope
   */
  public String getScope() {
    return this.scope;
  }

  /**
   * @param scope the scope to set
   */
  public void setScope(String scope) {
    this.scope = scope;
  }

  /**
   * @return the loggedInUserName
   */
  public String getLoggedInUserName() {
    return this.loggedInUserName;
  }

  /**
   * @param loggedInUserName the loggedInUserName to set
   */
  public void setLoggedInUserName(String loggedInUserName) {
    this.loggedInUserName = loggedInUserName;
  }

  /**
   * @return the errorMessage
   */
  public String getErrorMessage() {
    return this.errorMessage;
  }

  /**
   * @param errorMessage the errorMessage to set
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * @return the showReadonly
   */
  public boolean isShowReadonly() {
    return this.showReadonly;
  }

  /**
   * @param showReadonly the showReadonly to set
   */
  public void setShowReadonly(boolean showReadonly) {
    this.showReadonly = showReadonly;
  }

  /**
   * @return the showReadwrite
   */
  public boolean isShowReadwrite() {
    return this.showReadwrite;
  }

  /**
   * @param showReadwrite the showReadwrite to set
   */
  public void setShowReadwrite(boolean showReadwrite) {
    this.showReadwrite = showReadwrite;
  }

  /**
   * @return the showSqlReadonly
   */
  public boolean isShowSqlReadonly() {
    return this.showSqlReadonly;
  }

  /**
   * @param showSqlReadonly the showSqlReadonly to set
   */
  public void setShowSqlReadonly(boolean showSqlReadonly) {
    this.showSqlReadonly = showSqlReadonly;
  }

  /**
   * @return the showAdminReadonly
   */
  public boolean isShowAdminReadonly() {
    return this.showAdminReadonly;
  }

  /**
   * @param showAdminReadonly the showAdminReadonly to set
   */
  public void setShowAdminReadonly(boolean showAdminReadonly) {
    this.showAdminReadonly = showAdminReadonly;
  }

  /**
   * @return the showAdminReadwrite
   */
  public boolean isShowAdminReadwrite() {
    return this.showAdminReadwrite;
  }

  /**
   * @param showAdminReadwrite the showAdminReadwrite to set
   */
  public void setShowAdminReadwrite(boolean showAdminReadwrite) {
    this.showAdminReadwrite = showAdminReadwrite;
  }

  /**
   * whether readwrite data scope restrictions are required on the consent page
   * @return true if required
   */
  public boolean isRequireReadwriteDataScope() {
    return this.requireReadwriteDataScope;
  }

  /**
   * set whether readwrite data scope restrictions are required
   * @param requireReadwriteDataScope1
   */
  public void setRequireReadwriteDataScope(boolean requireReadwriteDataScope1) {
    this.requireReadwriteDataScope = requireReadwriteDataScope1;
  }
}
