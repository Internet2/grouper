package edu.internet2.middleware.grouper.app.azure;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import junit.framework.TestCase;

public class GrouperAzureExchangeOnlineTest extends TestCase {

  public void testExchangeWritesDefaultToDisabled() {
    assertFalse(new GrouperAzureConfiguration().isExchangeOnlineEnabled());
  }

  public void testMockExchangeGroupPersistenceDefaults() {
    for (String type : new String[] {"Security", "Distribution"}) {
      GrouperAzureGroup group = AzureMockServiceHandler.newExchangeGroup(
          GrouperUtil.jsonJacksonNode("{\"Type\":\"" + type + "\"}"));
      assertNotNull(group.getId());
      assertEquals("F", group.getAssignableToRoleDb());
      assertEquals(GrouperAzureGroup.defaultVisibility, group.getVisibilityDb());
      assertEquals("F", group.getGroupTypeDynamicDb());
      assertEquals("F", group.getGroupTypeUnifiedDb());
      assertEquals("T", group.getMailEnabledDb());
      assertEquals("Security".equals(type), group.isSecurityEnabled());
    }
  }

  public void testIndividualMembershipCommandsAndSwitches() {
    final List<String> commands = new ArrayList<String>();
    final List<Map<String, Object>> requests = new ArrayList<Map<String, Object>>();
    GrouperAzureExchangeOnline exchange = new GrouperAzureExchangeOnline(null) {
      @Override
      public JsonNode exoInvokeCommand(String cmdletName, Map<String, Object> parameters) {
        commands.add(cmdletName);
        requests.add(new LinkedHashMap<String, Object>(parameters));
        return null;
      }
    };
    exchange.membership("group-id", "member-id", true);
    exchange.membership("group-id", "member-id", false);
    assertEquals(GrouperUtil.toList("Add-DistributionGroupMember", "Remove-DistributionGroupMember"), commands);
    for (Map<String, Object> parameters : requests) {
      assertEquals("group-id", parameters.get("Identity"));
      assertEquals("member-id", parameters.get("Member"));
      assertEquals(Boolean.TRUE, parameters.get("BypassSecurityGroupManagerCheck"));
    }
    assertEquals(Boolean.FALSE, requests.get(1).get("Confirm"));
  }

  public void testGroupTypeRouting() {
    for (String type : new String[] {"distributionGroup", "securityMailEnabled", "security", "unified", "unifiedSecurityEnabled"}) {
      ProvisioningGroup group = new ProvisioningGroup(false);
      group.assignAttributeValue("groupType", type);
      GrouperAzureGroup azure = GrouperAzureGroup.fromProvisioningGroup(group, null);
      assertEquals(type, type.equals("distributionGroup") || type.equals("securityMailEnabled"), azure.isExchangeOnlineGroup());
      if (type.equals("distributionGroup")) {
        assertFalse(azure.isSecurityEnabled());
      }
    }
  }

  public void testGraphReadCarriesExchangeRoutingAndAddress() {
    GrouperAzureGroup group = GrouperAzureGroup.fromJson(GrouperUtil.jsonJacksonNode(
        "{\"id\":\"group-id\",\"displayName\":\"Mail group\",\"mailEnabled\":true,\"securityEnabled\":true,"
        + "\"groupTypes\":[],\"mail\":\"group@contoso.com\",\"onPremisesSyncEnabled\":true}"));
    GrouperAzureGroup roundTrip = GrouperAzureGroup.fromProvisioningGroup(group.toProvisioningGroup(), null);
    assertTrue(roundTrip.isExchangeOnlineGroup());
    assertTrue(roundTrip.isOnPremisesSyncEnabled());
    assertEquals("group@contoso.com", roundTrip.getPrimarySmtpAddress());
  }

  public void testCreateSecurityGroupParameters() {
    GrouperAzureGroup group = mailGroup();
    group.setSecurityEnabled(true);
    Map<String, Object> parameters = GrouperAzureExchangeOnline.groupParameters(group,
        GrouperUtil.toSet("displayName", "mailNickname", "mailEnabled", "securityEnabled", "primarySmtpAddress"), true);
    assertEquals("Security", parameters.get("Type"));
    assertEquals("mail-group", parameters.get("Name"));
    assertEquals("mail-group", parameters.get("Alias"));
    assertEquals("mail-group@contoso.com", parameters.get("PrimarySmtpAddress"));
    assertEquals("Closed", parameters.get("MemberJoinRestriction"));
    assertEquals("Closed", parameters.get("MemberDepartRestriction"));
    assertFalse(parameters.containsKey("BypassSecurityGroupManagerCheck"));
  }

  public void testUpdateOwnersUsesRecipientIdsAndHonorsManageFlag() {
    GrouperAzureGroup group = mailGroup();
    group.setOwners(GrouperUtil.toSet("https://graph.microsoft.com/v1.0/users/owner-id"));
    Map<String, Object> parameters = GrouperAzureExchangeOnline.groupParameters(group, GrouperUtil.toSet("groupOwners"), false);
    assertEquals(GrouperUtil.toSet("owner-id"), parameters.get("ManagedBy"));
    assertEquals(Boolean.TRUE, parameters.get("BypassSecurityGroupManagerCheck"));
    group.setGroupOwnersManage(false);
    assertFalse(GrouperAzureExchangeOnline.groupParameters(group, GrouperUtil.toSet("groupOwners"), false).containsKey("ManagedBy"));
  }

  public void testRejectUnsupportedAttributesBeforeWrite() {
    try {
      GrouperAzureExchangeOnline.groupParameters(mailGroup(), GrouperUtil.toSet("resourceProvisioningOptionsTeam"), true);
      fail("Exchange must not silently accept a Teams setting");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("Unsupported"));
    }
  }

  public void testRejectOnPremisesGroup() {
    GrouperAzureGroup group = mailGroup();
    group.setOnPremisesSyncEnabled(true);
    try {
      GrouperAzureExchangeOnline.groupParameters(group, GrouperUtil.toSet("displayName"), false);
      fail("On-premises groups must not be written");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("on-premises"));
    }
  }

  public void testGraphDoesNotSilentlyDropSmtpAddressWrites() {
    try {
      mailGroup().toJson(GrouperUtil.toSet("primarySmtpAddress"), true);
      fail("SMTP address writes must use Exchange, not Graph");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("Exchange Online"));
    }
  }

  public void testConvergedMembershipErrorsOnly() {
    assertNull(GrouperAzureExchangeOnline.parseResponse("Add-DistributionGroupMember", 400,
        "{\"error\":{\"message\":\"Recipient is already a member of the group\"}}"));
    assertNull(GrouperAzureExchangeOnline.parseResponse("Remove-DistributionGroupMember", 400,
        "{\"error\":{\"message\":\"Recipient is not a member of the group\"}}"));
    assertFalse(GrouperAzureExchangeOnline.isConvergedMembershipError("Add-DistributionGroupMember", 403,
        "Recipient is already a member of the group"));
    assertFalse(GrouperAzureExchangeOnline.isConvergedMembershipError("Remove-DistributionGroupMember", 400,
        "Exchange recipient not found"));
    assertFalse(GrouperAzureExchangeOnline.isConvergedMembershipError("Set-DistributionGroup", 400,
        "Recipient is already a member of the group"));
  }

  public void testCmdletFailureInsideSuccessfulHttpResponse() {
    try {
      GrouperAzureExchangeOnline.parseResponse("New-DistributionGroup", 200, "{\"error\":{\"message\":\"Access denied\"}}");
      fail("Cmdlet errors must not be mistaken for HTTP success");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("Access denied"));
    }
  }

  public void testCreationUsesExternalDirectoryObjectIdNotExchangeGuid() {
    assertEquals("entra-id", GrouperAzureExchangeOnline.createdGroupId(GrouperUtil.jsonJacksonNode(
        "{\"value\":[{\"Guid\":\"exchange-id\",\"ExternalDirectoryObjectId\":\"entra-id\"}]}")));
    try {
      GrouperAzureExchangeOnline.createdGroupId(GrouperUtil.jsonJacksonNode("{\"value\":[{\"Guid\":\"exchange-id\"}]}"));
      fail("Never use the Exchange object Guid as the Graph id");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("ExternalDirectoryObjectId"));
    }
  }

  public void testThrottlingDelay() {
    assertEquals(7000L, GrouperAzureExchangeOnline.retryDelayMillis("7", 0));
    assertEquals(0L, GrouperAzureExchangeOnline.retryDelayMillis("0", 0));
    assertEquals(4000L, GrouperAzureExchangeOnline.retryDelayMillis("invalid", 2));
    assertEquals(0L, GrouperAzureExchangeOnline.retryDelayMillis("Wed, 21 Oct 2015 07:28:00 GMT", 0));
  }

  public void testReplicationGuardUsesPersistableSyncState() {
    GcGrouperSyncGroup sync = new GcGrouperSyncGroup();
    assertFalse(GrouperAzureTargetDao.recentlyInserted(sync));
    sync.setInTargetInsertOrExists(true);
    sync.setInTargetStart(new Timestamp(System.currentTimeMillis()));
    assertTrue(GrouperAzureTargetDao.recentlyInserted(sync));
    sync.setInTargetStart(new Timestamp(System.currentTimeMillis() - 16 * 60 * 1000));
    assertFalse(GrouperAzureTargetDao.recentlyInserted(sync));
  }

  private GrouperAzureGroup mailGroup() {
    GrouperAzureGroup group = new GrouperAzureGroup();
    group.setId("group-id");
    group.setDisplayName("Mail group");
    group.setMailNickname("mail-group");
    group.setMailEnabled(true);
    group.setPrimarySmtpAddress("mail-group@contoso.com");
    return group;
  }
}
