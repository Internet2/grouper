package edu.internet2.middleware.grouper.app.azure;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * The PowerShell module's internal REST contract is intentionally isolated here.
 * This is not the documented, limited adminapi/v2.0 API.
 */
public class GrouperAzureExchangeOnline {

  private GrouperAzureConfiguration configuration;

  public GrouperAzureExchangeOnline(GrouperAzureConfiguration configuration) {
    this.configuration = configuration;
  }

  public JsonNode exoInvokeCommand(String cmdletName, Map<String, Object> parameters) {
    String configId = this.configuration.getAzureExternalSystemConfigId();
    String prefix = "grouper.azureConnector." + configId + ".";
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    String tenantId = config.propertyValueStringRequired(prefix + "tenantId");
    String endpoint = StringUtils.defaultIfBlank(this.configuration.getExchangeOnlineEndpoint(),
        "https://outlook.office365.com/adminapi/beta");
    String anchor = StringUtils.defaultIfBlank(this.configuration.getExchangeOnlineAnchorMailbox(),
        "APP:SystemMailbox{bb558c35-97f1-4cb9-8ff7-d53741dc928c}@" + tenantId);

    ObjectNode body = GrouperUtil.jsonJacksonNode();
    ObjectNode input = body.putObject("CmdletInput");
    input.put("CmdletName", cmdletName);
    Map<String, Object> commandParameters = new LinkedHashMap<String, Object>(parameters);
    commandParameters.put("ErrorAction", "Stop");
    input.set("Parameters", GrouperUtil.jsonJacksonNode(GrouperUtil.jsonConvertTo(commandParameters)));

    // Retry only explicit throttling. Retrying an ambiguous create/network failure can duplicate groups.
    for (int attempt = 0; attempt < 4; attempt++) {
      GrouperHttpClient client = new GrouperHttpClient();
      client.setRetryForThrottlingOrNetworkIssues(0);
      client.assignDoNotLogHeaders(AzureMockServiceHandler.doNotLogHeaders);
      client.assignDoNotLogParameters(AzureMockServiceHandler.doNotLogParameters);
      client.assignProxyUrl(config.propertyValueString(prefix + "proxyUrl"));
      client.assignProxyType(config.propertyValueString(prefix + "proxyType"));
      client.assignUrl(GrouperUtil.stripLastSlashIfExists(endpoint) + "/"
          + GrouperUtil.escapeUrlEncode(tenantId) + "/InvokeCommand");
      client.assignGrouperHttpMethod("POST");
      client.addHeader("Authorization", "Bearer " + AzureGrouperExternalSystem.retrieveBearerTokenForAzureConfigId(
          new HashMap<String, Object>(), configId, AzureGrouperExternalSystem.SCOPE_EXCHANGE_ONLINE));
      client.addHeader("Content-Type", "application/json");
      client.addHeader("Accept", "application/json");
      client.addHeader("X-ResponseFormat", "json");
      client.addHeader("X-AnchorMailbox", anchor);
      client.addHeader("X-CmdletName", cmdletName);
      client.addHeader("X-ClientApplication", "ExoManagementModule");
      client.assignBody(GrouperUtil.jsonJacksonToString(body));
      long started = System.currentTimeMillis();
      try {
        client.executeRequest();
      } finally {
        GrouperProvisioner.incrementCommandsCallsStats(cmdletName, 1, System.currentTimeMillis() - started);
      }
      if (client.getResponseCode() == 429 && attempt < 3) {
        long delay = retryDelayMillis(client.getResponseHeadersLower().get("retry-after"), attempt);
        // Defer to the next provisioning run rather than sleeping unboundedly or retrying too early.
        if (delay <= 60000) {
          GrouperUtil.sleep(delay);
          continue;
        }
      }
      return parseResponse(cmdletName, client.getResponseCode(), client.getResponseBody());
    }
    throw new RuntimeException("Exchange Online retry limit reached");
  }

  static long retryDelayMillis(String retryAfter, int attempt) {
    if (StringUtils.isNotBlank(retryAfter)) {
      try {
        long seconds = Long.parseLong(retryAfter);
        return seconds > 60 ? 61000 : Math.max(0, seconds * 1000);
      } catch (NumberFormatException e) {
        try {
          return Math.max(0, ZonedDateTime.parse(retryAfter, DateTimeFormatter.RFC_1123_DATE_TIME)
              .toInstant().toEpochMilli() - System.currentTimeMillis());
        } catch (RuntimeException ignored) {
          // Use bounded exponential backoff when the server does not supply a usable delay.
        }
      }
    }
    return 1000L << attempt;
  }

  static JsonNode parseResponse(String cmdletName, int status, String body) {
    JsonNode response = null;
    try {
      response = StringUtils.isBlank(body) ? null : GrouperUtil.jsonJacksonNode(body);
    } catch (RuntimeException e) {
      throw new RuntimeException("Exchange Online " + cmdletName + " returned non-JSON (HTTP " + status + ")", e);
    }
    JsonNode error = response == null ? null : response.get("error");
    if (status < 200 || status >= 300 || (error != null && !error.isNull())) {
      String details = error == null ? body : error.toString();
      if (isConvergedMembershipError(cmdletName, status, details)) {
        return null;
      }
      throw new RuntimeException("Exchange Online " + cmdletName + " failed (HTTP " + status + "): " + details);
    }
    return response;
  }

  static boolean isConvergedMembershipError(String cmdletName, int status, String details) {
    if (status != 400 && status != 500 && status != 200) {
      return false;
    }
    if (StringUtils.equals(cmdletName, "Add-DistributionGroupMember")) {
      return StringUtils.containsIgnoreCase(details, "MemberAlreadyExistsException")
          || StringUtils.containsIgnoreCase(details, "is already a member of the group");
    }
    if (StringUtils.equals(cmdletName, "Remove-DistributionGroupMember")) {
      return StringUtils.containsIgnoreCase(details, "MemberNotFoundException")
          || StringUtils.containsIgnoreCase(details, "is not a member of the group");
    }
    return false;
  }

  static String createdGroupId(JsonNode response) {
    JsonNode values = response == null ? null : response.get("value");
    if (values == null || !values.isArray() || values.size() != 1
        || !values.get(0).path("ExternalDirectoryObjectId").isTextual()
        || StringUtils.isBlank(values.get(0).path("ExternalDirectoryObjectId").asText())) {
      throw new RuntimeException("New-DistributionGroup did not return one ExternalDirectoryObjectId; "
          + "check Exchange before retrying group creation");
    }
    return values.get(0).get("ExternalDirectoryObjectId").asText();
  }

  static Map<String, Object> groupParameters(GrouperAzureGroup group, Set<String> fields, boolean create) {
    if (group.isOnPremisesSyncEnabled()) {
      throw new RuntimeException("Groups synchronized from on-premises must be managed in AD, not Exchange Online");
    }
    Map<String, Object> parameters = new LinkedHashMap<String, Object>();
    if (create) {
      if (StringUtils.isBlank(group.getMailNickname()) || group.getMailNickname().length() > 64) {
        throw new RuntimeException("Exchange Online group creation requires mailNickname (Alias), at most 64 characters");
      }
      parameters.put("Name", group.getMailNickname());
      parameters.put("Alias", group.getMailNickname());
      parameters.put("DisplayName", group.getDisplayName());
      parameters.put("Type", group.isSecurityEnabled() ? "Security" : "Distribution");
      parameters.put("MemberJoinRestriction", "Closed");
      parameters.put("MemberDepartRestriction", "Closed");
    } else {
      parameters.put("Identity", group.getId());
      parameters.put("BypassSecurityGroupManagerCheck", true);
    }
    for (String field : fields) {
      if (StringUtils.equals(field, "displayName")) {
        parameters.put("DisplayName", group.getDisplayName());
      } else if (StringUtils.equals(field, "mailNickname")) {
        parameters.put("Alias", group.getMailNickname());
      } else if (StringUtils.equals(field, "primarySmtpAddress")) {
        parameters.put("PrimarySmtpAddress", group.getPrimarySmtpAddress());
      } else if (StringUtils.equals(field, "description")) {
        parameters.put("Description", group.getDescription());
      } else if (StringUtils.equals(field, "groupOwners")) {
        if (group.isGroupOwnersManage()) {
          parameters.put("ManagedBy", ownerIds(group.getOwners()));
        }
      } else if (StringUtils.equalsAny(field, "groupOwnersManage", "id", "onPremisesSyncEnabled")) {
        continue;
      } else if (!create || !StringUtils.equalsAny(field, "mailEnabled", "securityEnabled", "groupType", "groupTypeUnified")) {
        throw new RuntimeException("Unsupported Exchange Online group attribute: " + field);
      }
    }
    if (create && StringUtils.isNotBlank(group.getPrimarySmtpAddress())) {
      parameters.put("PrimarySmtpAddress", group.getPrimarySmtpAddress());
    }
    return parameters;
  }

  private static Set<String> ownerIds(Set<String> owners) {
    Set<String> result = new LinkedHashSet<String>();
    for (String owner : GrouperUtil.nonNull(owners)) {
      result.add(owner.startsWith("https://") || owner.startsWith("http://")
          ? StringUtils.substringAfterLast(owner, "/") : owner);
    }
    return result;
  }

  public void membership(String groupId, String memberId, boolean insert) {
    if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId)) {
      throw new RuntimeException("Exchange Online membership requires group and member Entra object ids");
    }
    Map<String, Object> parameters = new LinkedHashMap<String, Object>();
    parameters.put("Identity", groupId);
    parameters.put("Member", memberId);
    parameters.put("BypassSecurityGroupManagerCheck", true);
    if (!insert) {
      parameters.put("Confirm", false);
    }
    this.exoInvokeCommand(insert ? "Add-DistributionGroupMember" : "Remove-DistributionGroupMember", parameters);
  }
}
