package edu.internet2.middleware.grouper.webservicesClient;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;


/**
 * example password handler for client
 */
public class RampartPwHandlerClient implements CallbackHandler {
    /**
     * example handler for client
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    public void handle(Callback[] callbacks)
        throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[i];
            String id = pwcb.getIdentifer();

            if ("GrouperSystem".equals(id)) {
                pwcb.setPassword("mypass");
            } else {
                throw new RuntimeException("Userid not found: " + id);
            }
        }
    }
}
