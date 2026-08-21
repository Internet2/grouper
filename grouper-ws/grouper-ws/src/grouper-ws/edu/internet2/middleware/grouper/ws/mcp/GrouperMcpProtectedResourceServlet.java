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
package edu.internet2.middleware.grouper.ws.mcp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.authentication.GrouperOAuthStore;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Serves the OAuth 2.0 Protected Resource Metadata (RFC 9728) at
 * {@code /.well-known/oauth-protected-resource}.
 *
 * <p>This is the first step in the MCP OAuth discovery flow. MCP clients
 * receive a {@code WWW-Authenticate} header pointing to this endpoint,
 * which in turn tells the client where the OAuth Authorization Server
 * metadata can be found.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpProtectedResourceServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpProtectedResourceServlet.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      // this is the first thing a client fetches, and what it learns here is which resource
      // this is and which authorization server protects it.  both are built from the address
      // clients reach this server at, and taking them from the request instead, as this used to
      // when it was not configured, let whoever set the Host header answer both questions
      String mcpUrlConfigurationError = GrouperOAuthStore.mcpUrlConfigurationError();
      if (mcpUrlConfigurationError != null) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("error", "server_error");
        error.put("error_description", mcpUrlConfigurationError);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
        response.getWriter().flush();
        return;
      }

      // the same values the token is issued with and verified against, and the same issuer the
      // authorization server metadata publishes, so a client which compares them per RFC 9728
      // and RFC 8414 sees them agree.  behind a gateway these are the gateway address, since
      // that is what the client connected to
      String baseUrl = GrouperOAuthStore.retrieveIssuerIdentifier();

      // resource must match the MCP server URL the client connects to
      String mcpUrl = GrouperOAuthStore.retrieveMcpResourceIdentifier();

      ObjectNode metadata = objectMapper.createObjectNode();
      metadata.put("resource", mcpUrl);

      // RFC 9728 defines authorization_servers as a list of issuer identifiers, so this is the
      // issuer and not the resource URL above.  A client builds the URL it fetches authorization
      // server metadata from out of this value, and RFC 8414 section 3.3 tells it to discard that
      // metadata unless the issuer it finds there is identical to the value it started from.
      // Naming the MCP endpoint here rather than the issuer would therefore have a client which
      // makes that check throw away metadata which is otherwise correct.
      ArrayNode authorizationServers = objectMapper.createArrayNode();
      authorizationServers.add(baseUrl);
      metadata.set("authorization_servers", authorizationServers);

      ArrayNode bearerMethods = objectMapper.createArrayNode();
      bearerMethods.add("header");
      metadata.set("bearer_methods_supported", bearerMethods);

      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write(objectMapper.writeValueAsString(metadata));
      response.getWriter().flush();
    } catch (RuntimeException re) {
      LOG.error("Error in MCP protected resource metadata doGet", re);
      throw re;
    }
  }
}
