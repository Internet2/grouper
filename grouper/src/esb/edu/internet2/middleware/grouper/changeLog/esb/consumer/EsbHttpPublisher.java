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

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.mortbay.jetty.HttpException;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
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
    // TODO Auto-generated method stub

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
    int retries = GrouperLoaderConfig.retrieveConfig().propertyValueInt("changeLog.consumer." + consumerName
        + ".publisher.retries", 0);
    int timeout = GrouperLoaderConfig.retrieveConfig().propertyValueInt("changeLog.consumer." + consumerName
        + ".publisher.timeout", 60000);
    PostMethod post = new PostMethod(urlString);
    post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
        new DefaultHttpMethodRetryHandler(retries, false));
    post.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, new Integer(timeout));
    //post.setRequestHeader("Content-Type", "application/json; charset=utf-8");
    post.setRequestHeader("Content-Type", GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
        + consumerName + ".publisher.contentTypeHeader", "application/x-www-form-urlencoded"));
    RequestEntity requestEntity;
    try {
      //requestEntity = new StringRequestEntity(eventJsonString, "application/json", "utf-8");
      
      String stringRequestEntityPrefix = "";
      
      if (GrouperLoaderConfig.retrieveConfig().containsKey("changeLog.consumer."
          + consumerName + ".publisher.stringRequestEntityPrefix")) {
        stringRequestEntityPrefix = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
            + consumerName + ".publisher.stringRequestEntityPrefix");
        
      }
      
      requestEntity = new StringRequestEntity(StringUtils.defaultString(stringRequestEntityPrefix) + eventJsonString, 
          GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
              + consumerName + ".publisher.stringRequestEntityContentType", "application/x-www-form-urlencoded"), "utf-8"); 
      
      post.setRequestEntity(requestEntity);
      HttpClient httpClient = new HttpClient();
      if (!(username.equals(""))) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Authenticating using basic auth");
        }
        URL url = new URL(urlString);
        httpClient.getState().setCredentials(new AuthScope(null, url.getPort(), null),
            new UsernamePasswordCredentials(username, password));
        httpClient.getParams().setAuthenticationPreemptive(true);
        post.setDoAuthentication(true);
      }
      int statusCode = httpClient.executeMethod(post);
      if (statusCode == 200) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Status code 200 recieved, event sent OK");
        }
        return true;
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Status code " + statusCode + " recieved, event send failed");
        }
        return false;
      }
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (HttpException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public void disconnect() {
    // Unused, client does not maintain a persistent connection in this version

  }

}
