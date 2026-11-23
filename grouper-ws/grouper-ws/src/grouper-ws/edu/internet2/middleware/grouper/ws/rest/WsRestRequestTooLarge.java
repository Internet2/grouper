/*******************************************************************************
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.ws.rest;

/**
 * <pre>
 * A WS REST request body is larger than ws.restMaxRequestBodyBytes.  GrouperRestServlet turns
 * this into HTTP 413 (a plain GrouperRestInvalidRequest is HTTP 400).  The message holds only
 * the limit and the length, never request content.
 *
 * Subclass of GrouperRestInvalidRequest so the servlet's existing invalid-request handling
 * (small error response, result code INVALID_QUERY) applies.  GrouperRestInvalidRequest is in
 * the grouper-ws module through 7.5.1 and in the grouper module from 7.6.0, same package and
 * constructors, so this one version compiles in every release.
 * (GRP-7585)
 * </pre>
 */
public class WsRestRequestTooLarge extends GrouperRestInvalidRequest {

  /** id */
  private static final long serialVersionUID = 1L;

  /**
   * @param message says the limit and the declared or received length
   */
  public WsRestRequestTooLarge(String message) {
    super(message);
  }

}
