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
 * @author mchyzer
 * $Id: RampartHandlerServer.java,v 1.2 2008-07-20 21:18:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSPasswordCallback;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;


/**
 * Grouper rampart handler
 */
public class RampartHandlerServer implements CallbackHandler {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(RampartHandlerServer.class);

  /**
   * retrieve the class for rampart customizer
   * @return the class or null if not configured
   */
  @SuppressWarnings("unchecked")
  public static Class<? extends GrouperWssecAuthentication> retrieveRampartCallbackClass() {
    String className = GrouperWsConfig.retrieveConfig().propertyValueString(GrouperWsConfig.WS_SECURITY_RAMPART_AUTHENTICATION_CLASS);
    if (StringUtils.isBlank(className)) {
      return null;
    }
    return GrouperUtil.forName(className);
  }
  
  /**
   * 
   * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
   */
  public void handle (Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    Class<? extends GrouperWssecAuthentication> wssecClass = retrieveRampartCallbackClass();

    GrouperWssecAuthentication grouperWssecAuthentication = GrouperUtil.newInstance(wssecClass); 
    for (int i = 0; i < callbacks.length; i++) {
      if (callbacks[i] instanceof WSPasswordCallback) {
        WSPasswordCallback wsPasswordCallback = (WSPasswordCallback) callbacks[i];
        LOG.debug("identifier: "+wsPasswordCallback.getIdentifier()+", usage: "+wsPasswordCallback.getUsage());

//        if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN) {
//          // for passwords sent in digest mode we need to provide the password,
//          // because the original one can't be un-digested from the message
//
//          // we can throw either of the two Exception types if authentication fails
//          if (! user.equals(pc.getIdentifer()))
//            throw new IOException("unknown user: "+pc.getIdentifer());
//
//          // this will throw an exception if the passwords don't match
//          pc.setPassword(pwd);
//
//        } else if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN_UNKNOWN) {
//          // for passwords sent in cleartext mode we can compare passwords directly
//
//          if (! user.equals(pc.getIdentifer()))
//            throw new IOException("unknown user: "+pc.getIdentifer());
//
//          // we can throw either of the two Exception types if authentication fails
//          if (! pwd.equals(pc.getPassword()))
//            throw new IOException("password incorrect for user: "+pc.getIdentifer());
//        }
        if (!grouperWssecAuthentication.authenticate(wsPasswordCallback)) {
          throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback: " + callbacks[i]);
        }
      }
    }
  }

  
}
