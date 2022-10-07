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
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class TierResourceTypesProviderIntegrationTest {

  private static final String RESOURCE_TYPES_ENDPOINT = "http://localhost:8080/grouper-ws-scim/v2/ResourceTypes";

  private void addScimHeaders(HttpUriRequest request) {
    request.setHeader("Content-Type", "application/scim+json");
    request.setHeader("Accept", "application/scim+json");
    request.setHeader("Authorization", "Basic R3JvdXBlclN5c3RlbTpwYXNz");
  }

  @Test
  public void getResourceTypesConfig() throws IOException {

    HttpUriRequest request = new HttpGet(RESOURCE_TYPES_ENDPOINT);
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));

    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);

    assertThat(json.get("totalResults").asInt(), equalTo(3));
    assertThat(json.get("startIndex").asInt(), equalTo(1));
    assertThat(json.get("itemsPerPage").asInt(), equalTo(3));
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:ListResponse"));


    for (Iterator<JsonNode> it = json.get("resources").elements(); it.hasNext(); ) {
      JsonNode node = it.next();

      assertThat(node.get("meta").get("resourceType").asText(), equalTo("ResourceType"));
      assertEquals(objectMapper.readerForListOf(String.class).readValue(node.get("schemas")),
              Arrays.asList("urn:ietf:params:scim:schemas:core:2.0:ResourceType"));
      assertThat(node.get("baseUrn").asText(), equalTo("urn:ietf:params:scim:schemas:core:2.0:ResourceType"));
      assertThat(node.get("resourceType").asText(), equalTo("ResourceType"));

      switch (node.get("schemaUrn").asText()) {
        case "urn:ietf:params:scim:schemas:core:2.0:Group":
          assertThat(node.get("endpoint").asText(), equalTo("/Groups"));
          /* todo PSU the meta location is incorrect, it should be plural */
          assertThat(node.get("meta").get("location").asText(), equalTo("http://localhost:8080/grouper-ws-scim/v2/ResourceTypes/Group"));
          assertThat(node.get("name").asText(), equalTo("Group"));
          assertThat(node.get("description").asText(), equalTo("Top level ScimGroup"));
          assertThat(node.get("schemaExtensions").get(0).get("schemaUrn").asText(), equalTo("urn:grouper:params:scim:schemas:extension:TierGroupExtension"));
          assertThat(node.get("schemaExtensions").get(0).get("required").asBoolean(), equalTo(Boolean.FALSE));
          assertThat(node.get("schemaExtensions").get(1).get("schemaUrn").asText(), equalTo("urn:tier:params:scim:schemas:extension:TierMetaExtension"));
          assertThat(node.get("schemaExtensions").get(1).get("required").asBoolean(), equalTo(Boolean.FALSE));

          assertThat(node.get("id").asText(), equalTo("Group"));
          break;
        case "urn:ietf:params:scim:schemas:core:2.0:User":
          assertThat(node.get("endpoint").asText(), equalTo("/Users"));
          /* todo PSU the meta location is incorrect, it should be plural */
          assertThat(node.get("meta").get("location").asText(), equalTo("http://localhost:8080/grouper-ws-scim/v2/ResourceTypes/User"));
          assertThat(node.get("name").asText(), equalTo("User"));
          assertThat(node.get("description").asText(), equalTo("Top level ScimUser"));
          assertThat(node.get("schemaExtensions").get(0).get("schemaUrn").asText(), equalTo("urn:tier:params:scim:schemas:extension:TierMetaExtension"));
          assertThat(node.get("schemaExtensions").get(0).get("required").asBoolean(), equalTo(Boolean.FALSE));
          assertThat(node.get("id").asText(), equalTo("User"));
          break;
        case "urn:tier:params:scim:schemas:Membership":
          assertThat(node.get("endpoint").asText(), equalTo("/Memberships"));
          /* todo PSU the meta location is incorrect, it should be plural */
          assertThat(node.get("meta").get("location").asText(), equalTo("http://localhost:8080/grouper-ws-scim/v2/ResourceTypes/Membership"));
          assertThat(node.get("name").asText(), equalTo("Membership"));
          assertThat(node.get("description").asText(), equalTo("Resource for representing Membership schema data"));
          assertThat(node.get("id").asText(), equalTo("urn:tier:params:scim:schemas:Membership"));
          break;
        default:
          throw new RuntimeException("Unexpected resource type: " + node.get("schemaUrn"));
      }
    }
  }
}
