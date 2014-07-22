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

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.mortbay.jetty.HttpException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;

/**
 * Publishes Grouper events to HTTP(S) server as JSON strings
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

        try {

            RequestConfig.Builder requestConfigBuilder  = RequestConfig.custom();
            requestConfigBuilder.setSocketTimeout(timeout).build();

            HttpPost post = new HttpPost(urlString);
            HttpHost host = new HttpHost(InetAddress.getLocalHost());
            HttpClientContext context = HttpClientContext.create();

            post.addHeader("Content-Type", GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
                    + consumerName + ".publisher.contentTypeHeader", "application/json; charset=utf-8"));

            if (!(username.equals(""))) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authenticating using basic auth");
                }
                URL url = new URL(urlString);

                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(null, url.getPort(), null),
                        new UsernamePasswordCredentials(username, password));

                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();
                authCache.put(host, basicAuth);

                context.setCredentialsProvider(credsProvider);
                context.setAuthCache(authCache);

                requestConfigBuilder.setAuthenticationEnabled(true);
            }

            RequestConfig requestConfig = requestConfigBuilder.build();

            post.setConfig(requestConfig);

            String stringRequestEntityPrefix = "";

            if (GrouperLoaderConfig.retrieveConfig().containsKey("changeLog.consumer."
                    + consumerName + ".publisher.stringRequestEntityPrefix")) {
                stringRequestEntityPrefix = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
                        + consumerName + ".publisher.stringRequestEntityPrefix");

            }

            //activemq might require: application/x-www-form-urlencoded
            String content = StringUtils.defaultString(stringRequestEntityPrefix) + eventJsonString;
            String contentType = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
                    + consumerName + ".publisher.stringRequestEntityContentType", "application/json");
            String charset = "utf-8";

            StringEntity entity = new StringEntity(content, ContentType.create(contentType, charset));

            post.setEntity(entity);

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(retries, false))
                    .build();

            CloseableHttpResponse response = httpClient.execute(host, post, context);
            int statusCode = response.getStatusLine().getStatusCode();

            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(httpClient);

            if (statusCode == 200) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Status code 200 received, event sent OK");
                }
                return true;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Status code " + statusCode + " received, event send failed");
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
