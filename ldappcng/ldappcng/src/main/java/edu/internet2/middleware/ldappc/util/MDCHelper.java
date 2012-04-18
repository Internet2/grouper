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
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.util;

import org.apache.log4j.MDC;
import org.openspml.v2.msg.spml.Request;

import edu.internet2.middleware.ldappc.spml.PSPConstants;

/**
 * Help control MDC logging.
 */
public class MDCHelper {

  /** An SPML request. */
  private Request request;

  /** The context of the MDC. */
  private Object o;

  /**
   * 
   * Constructor
   * 
   * @param request
   *          the SPML request
   */
  public MDCHelper(Request request) {
    this.request = request;
  }

  public MDCHelper start() {

    o = MDC.get(PSPConstants.MDC_REQUESTID);

    if (o == null && request != null && request.getRequestID() != null) {
      MDC.put(PSPConstants.MDC_REQUESTID, request.getRequestID());
    }

    return this;
  }

  public void stop() {
    if (o == null) {
      MDC.remove(PSPConstants.MDC_REQUESTID);
    }
  }
}
