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

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 * Publishes Grouper events to HTTP(S) server as JSON strings
 *
 */
public class EsbHttpPublisher extends EsbListenerBase {

    private static final Log LOG = GrouperUtil.getLog(EsbHttpPublisher.class);

    @Override
    public boolean dispatchEvent(String eventJsonString, String consumerName) {

        String urlString = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
                + consumerName + ".publisher.url");
        String username = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
                + consumerName + ".publisher.username", "");
        String password = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
                + consumerName + ".publisher.password", "");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Consumer name: " + consumerName + " sending "
                    + GrouperUtil.indent(eventJsonString, false) + " to " + urlString);
        }
        
        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
        
        int retries = GrouperLoaderConfig.retrieveConfig().propertyValueInt("changeLog.consumer." + consumerName
                + ".publisher.retries", -1);
        if (retries != -1) {
          grouperHttpClient.assignRetries(retries);
        }
        
        int timeout = GrouperLoaderConfig.retrieveConfig().propertyValueInt("changeLog.consumer." + consumerName
                + ".publisher.timeout", -1);
        if (timeout != -1) {
          grouperHttpClient.assignTimeoutMillies(timeout);
        }

        String proxyUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
            + consumerName + ".publisher.proxyUrl");
        String proxyType = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
            + consumerName + ".publisher.proxyType");

        grouperHttpClient.assignProxyUrl(proxyUrl);
        grouperHttpClient.assignProxyType(proxyType);
        
        grouperHttpClient.assignUrl(urlString);
        grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
        
        //post.setRequestHeader("Content-Type", "application/json; charset=utf-8");
        //activemq might require: application/x-www-form-urlencoded
        grouperHttpClient.addHeader("Content-Type", GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
                + consumerName + ".publisher.contentTypeHeader", "application/json; charset=utf-8"));
        
        //requestEntity = new StringRequestEntity(eventJsonString, "application/json", "utf-8");

        String stringRequestEntityPrefix = "";

        if (GrouperLoaderConfig.retrieveConfig().containsKey("changeLog.consumer."
                + consumerName + ".publisher.stringRequestEntityPrefix")) {
            stringRequestEntityPrefix = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
                    + consumerName + ".publisher.stringRequestEntityPrefix");

        }

        grouperHttpClient.assignBody(StringUtils.defaultString(stringRequestEntityPrefix) + eventJsonString);
        
        // NOTE this used to swallow all exceptions
        
        //activemq might require: application/x-www-form-urlencoded
//            requestEntity = new StringRequestEntity(StringUtils.defaultString(stringRequestEntityPrefix) + eventJsonString,
//                    GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
//                            + consumerName + ".publisher.stringRequestEntityContentType", "application/json"), "utf-8");

//            post.setRequestEntity(requestEntity);
        if (!(username.equals(""))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authenticating using basic auth");
            }
            grouperHttpClient.assignUser(username);
            grouperHttpClient.assignPassword(password);
        }
        grouperHttpClient.executeRequest();
        
        int statusCode = grouperHttpClient.getResponseCode();
        if (statusCode == 200) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Status code 200 recieved, event sent OK");
            }
            return true;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Status code " + statusCode + " recieved, event send failed");
            }
        }
        return false;
    }

    @Override
    public void disconnect() {
        // Unused, client does not maintain a persistent connection in this version

    }

}
