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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.authentication.GrouperOAuthStore;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Serves the OAuth 2.0 Authorization Server Metadata at
 * {@code /.well-known/oauth-authorization-server}.
 *
 * <p>This is required by the MCP specification so that MCP clients can
 * discover the authorization, token, and registration endpoints.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpWellKnownServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpWellKnownServlet.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      // the same value the authorization response sends back as its iss parameter, so that a
      // client comparing the two per RFC 9207 sees them agree
      String baseUrl = GrouperOAuthStore.retrieveIssuerIdentifier();
      if (StringUtils.isBlank(baseUrl)) {
        baseUrl = request.getScheme() + "://" + request.getServerName();
        if (("http".equals(request.getScheme()) && request.getServerPort() != 80)
            || ("https".equals(request.getScheme()) && request.getServerPort() != 443)) {
          baseUrl += ":" + request.getServerPort();
        }
        baseUrl += request.getContextPath();
      }

      // authorization endpoint is in the Grouper UI (user authenticates and sees consent page there)
      String uiUrl = GrouperConfig.getGrouperUiUrl(false);
      if (StringUtils.isBlank(uiUrl)) {
        // fallback: assume UI is at same context path
        uiUrl = baseUrl + "/";
      }

      ObjectNode metadata = objectMapper.createObjectNode();
      metadata.put("issuer", baseUrl);
      metadata.put("authorization_endpoint", uiUrl + "grouperUi/app/UiV2OAuth.authorize");
      metadata.put("token_endpoint", baseUrl + "/mcp/oauth/token");
      metadata.put("registration_endpoint", baseUrl + "/mcp/oauth/register");

      ArrayNode responseTypes = objectMapper.createArrayNode();
      responseTypes.add("code");
      metadata.set("response_types_supported", responseTypes);

      ArrayNode grantTypes = objectMapper.createArrayNode();
      grantTypes.add("authorization_code");
      metadata.set("grant_types_supported", grantTypes);

      ArrayNode tokenEndpointAuthMethods = objectMapper.createArrayNode();
      tokenEndpointAuthMethods.add("none");
      metadata.set("token_endpoint_auth_methods_supported", tokenEndpointAuthMethods);

      ArrayNode codeChallengeMethodsSupported = objectMapper.createArrayNode();
      codeChallengeMethodsSupported.add("S256");
      metadata.set("code_challenge_methods_supported", codeChallengeMethodsSupported);

      // An authorization server which sends the iss parameter on authorization responses has to
      // say so here, per RFC 9207.  This tracks whether the issuer identifier is configured,
      // because that is what decides whether the authorization response actually carries iss.
      // Claiming it while leaving iss off would be worse than not claiming it at all: a client
      // which is told to expect iss rejects a response which does not have it.  Note this reads
      // the configured value rather than baseUrl above, which falls back to a request derived
      // URL that the authorization response cannot use.
      metadata.put("authorization_response_iss_parameter_supported",
          StringUtils.isNotBlank(GrouperOAuthStore.retrieveIssuerIdentifier()));

      // when served as /.well-known/openid-configuration, OIDC Discovery requires these fields
      String requestUri = request.getRequestURI();
      if (requestUri != null && requestUri.contains("openid-configuration")) {
        metadata.put("jwks_uri", baseUrl + "/mcp/oauth/jwks");

        ArrayNode subjectTypes = objectMapper.createArrayNode();
        subjectTypes.add("public");
        metadata.set("subject_types_supported", subjectTypes);

        ArrayNode idTokenSigningAlgs = objectMapper.createArrayNode();
        idTokenSigningAlgs.add("RS256");
        metadata.set("id_token_signing_alg_values_supported", idTokenSigningAlgs);
      }

      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write(objectMapper.writeValueAsString(metadata));
      response.getWriter().flush();
    } catch (RuntimeException re) {
      LOG.error("Error in MCP well-known metadata doGet", re);
      throw re;
    }
  }
}
