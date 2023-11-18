/****
 * Copyright 2022 Internet2
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
 ***/

package edu.internet2.middleware.grouper.ws.scim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class TierServiceProviderProviderIntegrationTest {

  private static final String SP_ENDPOINT = "http://localhost:8080/grouper-ws/scim/v2/ServiceProviderConfig";

  private void addScimHeaders(HttpUriRequest request) {
    request.setHeader("Content-Type", "application/scim+json");
    request.setHeader("Accept", "application/scim+json");
    request.setHeader("Authorization", "Basic R3JvdXBlclN5c3RlbTpwYXNz");
  }

  @Test
  public void getServiceProviderConfig() throws IOException {

    HttpUriRequest request = new HttpGet(SP_ENDPOINT);
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));

    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);

    assertThat(json.get("id").asText(), equalTo("spc"));
    assertThat(json.get("externalId").asText(), equalTo("spc"));
    assertThat(json.get("sort").get("supported").asBoolean(), equalTo(Boolean.FALSE));
    assertThat(json.get("changePassword").get("supported").asBoolean(), equalTo(Boolean.FALSE));
    assertThat(json.get("patch").get("supported").asBoolean(), equalTo(Boolean.FALSE));
    assertThat(json.get("filter").get("maxResults").asInt(), equalTo(1000));
    assertThat(json.get("filter").get("supported").asBoolean(), equalTo(Boolean.TRUE));
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("authenticationSchemes")),
            Collections.emptyList());
    assertThat(json.get("meta").get("location").asText(), equalTo("http://localhost:8080/grouper-ws/scim/v2/ServiceProviderConfig"));
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"));
    assertThat(json.get("etag").get("supported").asBoolean(), equalTo(Boolean.TRUE));
    assertThat(json.get("bulk").get("supported").asBoolean(), equalTo(Boolean.FALSE));
    assertThat(json.get("bulk").get("maxPayloadSize").asInt(), equalTo(0));
    assertThat(json.get("bulk").get("maxOperations").asInt(), equalTo(0));
  }
}
