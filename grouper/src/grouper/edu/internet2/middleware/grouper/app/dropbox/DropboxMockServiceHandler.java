package edu.internet2.middleware.grouper.app.dropbox;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Mock implementation of the Dropbox Business Team API, used by the Dropbox provisioner tests.
 *
 * <p>This handler mirrors {@code TrueFoundryMockServiceHandler}: it stands in for the real Dropbox
 * service behind the {@code /mockServices/dropbox/...} servlet path, backs the API onto three mock
 * tables ({@code mock_dropbox_group}, {@code mock_dropbox_user}, {@code mock_dropbox_membership})
 * via Hibernate/{@code GcDbAccess}, and returns JSON shaped exactly as {@link DropboxApiCommands}
 * sends and expects.</p>
 *
 * <p><b>Protocol notes.</b> Every Dropbox Team API call is an HTTP POST with a JSON object body and
 * a JSON response; unions carry a {@code ".tag"} discriminator. Unlike the real service (which can
 * defer long-running writes behind an {@code async_job_id} and a {@code .../job_status/...} poll),
 * this mock always completes <i>synchronously</i>: deletes/removes return
 * {@code {".tag":"complete"}} and {@code members/add_v2} returns
 * {@code {".tag":"complete","complete":[...]}} with no {@code async_job_id}. That keeps the tests off
 * the polling path in {@link DropboxApiCommands#pollIfAsync}.</p>
 *
 * <p><b>Auth difference from TrueFoundry.</b> TrueFoundry stores a JSON blob of two tokens in
 * {@code accessTokenPassword} and accepts either; Dropbox uses a single plain bearer token, so
 * {@link #checkAuthorization(MockServiceRequest)} compares the {@code Authorization: Bearer} value
 * to the stored {@code accessTokenPassword} string directly, with no JSON parse.</p>
 *
 * <p><b>Admin roles.</b> Dropbox exposes no "list all roles" endpoint, so this mock publishes a
 * fixed catalog of the 8 built-in admin roles, each mapped to a synthetic, deterministic
 * {@code role_id} of {@code "pid_dbtmr:" + lowercase(name)} (see {@link #roleIdForName(String)} /
 * {@link #roleNameForRoleId(String)}). A member's current role is stored as a single name in
 * {@code mock_dropbox_user.admin_role} (null == member_only). The {@code roles[]} array emitted by
 * member reads is harvested by {@link DropboxApiCommands} to learn the name -&gt; role_id mapping it
 * needs for {@code set_admin_permissions_v2}.</p>
 */
public class DropboxMockServiceHandler extends MockServiceHandler {

  public DropboxMockServiceHandler() {
  }

  /** the bearer token header carries the secret, so never log it */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  @Override
  public Set<String> doNotLogParameters() {
    return null;
  }

  /** synthetic role_id prefix; deterministic so the catalog is stable across calls */
  private static final String ROLE_ID_PREFIX = "pid_dbtmr:";

  /** cheap process-local flag so the mock tables are only ensured once per JVM */
  private static boolean mockTablesThere = false;

  /**
   * Lazily create the three Dropbox mock tables (group, user, membership) if any is missing.
   * Mirrors {@code TrueFoundryMockServiceHandler.ensureTrueFoundryMockTables}.
   */
  public static void ensureDropboxMockTables() {
    try {
      new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().sql("select count(*) from mock_dropbox_user").select(int.class);
      new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().sql("select count(*) from mock_dropbox_group").select(int.class);
      new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().sql("select count(*) from mock_dropbox_membership").select(int.class);
    } catch (Exception e) {

      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          DropboxGroup.createTableDropboxGroup(ddlVersionBean, database);
          DropboxUser.createTableDropboxUser(ddlVersionBean, database);
          DropboxMembership.createTableDropboxMembership(ddlVersionBean, database);

        }
      });

    }
  }

  /**
   * Validate the {@code Authorization: Bearer <token>} header against the configured Dropbox bearer
   * token. Dropbox stores a single plain token in {@code accessTokenPassword} (no JSON blob like
   * TrueFoundry), so the comparison is a direct string equals.
   * @param mockServiceRequest the incoming request
   */
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String authHeader = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization: Bearer header is required");
    }
    String bearerToken = authHeader.substring("Bearer ".length()).trim();

    String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired(
        "grouperTest.exampleDropbox.mockExternalSystem.configId");
    String accessTokenPassword = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(
        "grouper.wsBearerToken." + configId + ".accessTokenPassword");

    // Accept either the configured static token, OR the access token the mock /oauth2/token endpoint
    // issues (so the OAuth2 refresh-token flow works against the mock too).
    if (!StringUtils.equals(accessTokenPassword, bearerToken)
        && !StringUtils.equals(MOCK_OAUTH_ACCESS_TOKEN, bearerToken)) {
      throw new RuntimeException("Authorization Bearer token does not match the configured token");
    }
  }

  /** the access token the mock /oauth2/token endpoint issues (and checkAuthorization accepts) */
  public static final String MOCK_OAUTH_ACCESS_TOKEN = "mock-dbx-access-token";

  /**
   * POST /oauth2/token (refresh-token grant). Simulates the Dropbox OAuth2 token endpoint: ignores the
   * form body (the real endpoint validates grant_type/refresh_token/client_id/client_secret) and
   * returns a fixed short-lived access token with a 4h expiry. No bearer auth -- the real token
   * endpoint authenticates via the client credentials in the body.
   */
  public void oauth2Token(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("access_token", MOCK_OAUTH_ACCESS_TOKEN);
    result.put("token_type", "bearer");
    result.put("expires_in", 14400);
    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(result));
  }

  // ============================================================
  // Group endpoints
  // ============================================================

  /**
   * POST /2/team/groups/list (and /list/continue). Return every group as a GroupSummary. Paging is
   * simulated as a single page: {@code has_more} is always false, so the {@code /continue} endpoint
   * (which shares this method) simply returns the same full set with no further pages.
   */
  public void groupsList(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    List<DropboxGroup> groups = HibernateSession.byHqlStatic()
        .createQuery("from DropboxGroup order by id")
        .list(DropboxGroup.class);

    ArrayNode groupsArray = GrouperUtil.jsonJacksonArrayNode();
    for (DropboxGroup group : groups) {
      groupsArray.add(buildGroupSummary(group));
    }

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("groups", groupsArray);
    // single-page mock: no cursor, no more pages
    result.put("cursor", "");
    result.put("has_more", false);

    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(result));
  }

  /**
   * POST /2/team/groups/get_info. Body is a GroupsSelector union
   * {@code {".tag":"group_ids","group_ids":[..]}}. Returns an ARRAY of GroupsGetInfoItem: each known
   * id yields {@code {".tag":"group_info", ...GroupFullInfo with members[]}}, each unknown id yields
   * {@code {".tag":"id_not_found","id_not_found":id}}.
   */
  public void groupsGetInfo(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    ArrayNode groupIds = GrouperUtil.jsonJacksonGetArrayNode(body, "group_ids");

    ArrayNode resultArray = GrouperUtil.jsonJacksonArrayNode();
    if (groupIds != null) {
      for (int i = 0; i < groupIds.size(); i++) {
        String groupId = groupIds.get(i).asText();
        DropboxGroup group = findGroup(groupId);
        if (group == null) {
          // not-found entry in the union array
          ObjectNode notFound = GrouperUtil.jsonJacksonNode();
          notFound.put(".tag", "id_not_found");
          notFound.put("id_not_found", groupId);
          resultArray.add(notFound);
          continue;
        }
        ObjectNode groupInfo = buildGroupFullInfo(group, true);
        // GroupsGetInfoItem union tag wrapping the full info
        groupInfo.put(".tag", "group_info");
        resultArray.add(groupInfo);
      }
    }

    // the get_info response is a bare JSON array, not an object
    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(resultArray));
  }

  /**
   * POST /2/team/groups/create. Body = DropboxGroup.toCreateJson() (group_name, optional
   * group_external_id, group_management_type union). Assign a native group_id of
   * {@code "g:" + uuid}, persist, and return the new group's GroupFullInfo.
   */
  public void groupsCreate(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);

    DropboxGroup group = new DropboxGroup();
    // Dropbox assigns the native group_id; mimic its "g:" prefix convention
    group.setId("g:" + GrouperUuid.getUuid());
    group.setName(GrouperUtil.jsonJacksonGetString(body, "group_name"));
    group.setExternalId(GrouperUtil.jsonJacksonGetString(body, "group_external_id"));
    group.setManagementType(unionTag(body, "group_management_type",
        DropboxGroup.MANAGEMENT_TYPE_COMPANY_MANAGED));
    group.setMemberCount(0);

    HibernateSession.byObjectStatic().save(group);

    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(buildGroupFullInfo(group, true)));
  }

  /**
   * POST /2/team/groups/update. Body carries a GroupSelector by group_id plus the new_* fields. Apply
   * new_group_name / new_group_external_id and return the updated GroupFullInfo.
   */
  public void groupsUpdate(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    String groupId = groupSelectorGroupId(body);

    DropboxGroup group = findGroup(groupId);
    if (group == null) {
      groupNotFound(mockServiceResponse, groupId);
      return;
    }

    String newName = GrouperUtil.jsonJacksonGetString(body, "new_group_name");
    if (newName != null) {
      group.setName(newName);
    }
    String newExternalId = GrouperUtil.jsonJacksonGetString(body, "new_group_external_id");
    if (newExternalId != null) {
      group.setExternalId(newExternalId);
    }
    HibernateSession.byObjectStatic().update(group);

    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(buildGroupFullInfo(group, true)));
  }

  /**
   * POST /2/team/groups/delete. Body {@code {".tag":"group_id","group_id":".."}}. Delete the group
   * and all of its memberships, then return a SYNCHRONOUS LaunchEmptyResult
   * {@code {".tag":"complete"}} (the mock never defers behind an async_job_id).
   */
  public void groupsDelete(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    // for delete the group_id is at the top level of the GroupSelector union (no "group" wrapper)
    String groupId = GrouperUtil.jsonJacksonGetString(body, "group_id");

    DropboxGroup group = findGroup(groupId);
    if (group != null) {
      deleteGroupMemberships(groupId);
      HibernateSession.byObjectStatic().delete(group);
    }

    writeJson(mockServiceResponse, launchComplete());
  }

  // ============================================================
  // Group membership endpoints
  // ============================================================

  /**
   * POST /2/team/groups/members/add. Body carries a GroupSelector plus a members[] array of
   * {@code {"user":UserSelectorArg,"access_type":{".tag":..}}}. Insert a membership row per entry
   * (idempotent on group_id + team_member_id) and return a GroupMembersChangeResult
   * {@code {"group_info":GroupFullInfo}} with no async_job_id.
   */
  public void groupsMembersAdd(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    String groupId = groupSelectorGroupId(body);

    DropboxGroup group = findGroup(groupId);
    if (group == null) {
      groupNotFound(mockServiceResponse, groupId);
      return;
    }

    ArrayNode members = GrouperUtil.jsonJacksonGetArrayNode(body, "members");
    if (members != null) {
      for (int i = 0; i < members.size(); i++) {
        JsonNode memberEntry = members.get(i);
        JsonNode userSelector = GrouperUtil.jsonJacksonGetNode(memberEntry, "user");
        String teamMemberId = userSelectorValue(userSelector);
        if (StringUtils.isBlank(teamMemberId)) {
          continue;
        }
        String accessType = unionTag(memberEntry, "access_type", DropboxMembership.ACCESS_TYPE_MEMBER);

        // idempotent: skip if the membership already exists
        if (findMembership(groupId, teamMemberId) == null) {
          DropboxMembership membership = new DropboxMembership();
          membership.setId(GrouperUuid.getUuid());
          membership.setGroupId(groupId);
          membership.setTeamMemberId(teamMemberId);
          membership.setAccessType(accessType);
          HibernateSession.byObjectStatic().save(membership);
        }
      }
    }

    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(groupMembersChangeResult(group)));
  }

  /**
   * POST /2/team/groups/members/remove. Body carries a GroupSelector plus a users[] array of
   * UserSelectorArg. Delete the matching membership rows and return a GroupMembersChangeResult.
   */
  public void groupsMembersRemove(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    String groupId = groupSelectorGroupId(body);

    DropboxGroup group = findGroup(groupId);
    if (group == null) {
      groupNotFound(mockServiceResponse, groupId);
      return;
    }

    ArrayNode users = GrouperUtil.jsonJacksonGetArrayNode(body, "users");
    if (users != null) {
      for (int i = 0; i < users.size(); i++) {
        String teamMemberId = userSelectorValue(users.get(i));
        if (StringUtils.isBlank(teamMemberId)) {
          continue;
        }
        DropboxMembership membership = findMembership(groupId, teamMemberId);
        if (membership != null) {
          HibernateSession.byObjectStatic().delete(membership);
        }
      }
    }

    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(groupMembersChangeResult(group)));
  }

  /**
   * POST /2/team/groups/members/list (and /list/continue). Return the group's members as
   * GroupMemberInfo objects. Single-page mock: has_more is always false.
   */
  public void groupsMembersList(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    String groupId = groupSelectorGroupId(body);

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("members", buildGroupMemberInfoArray(groupId));
    result.put("cursor", "");
    result.put("has_more", false);

    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(result));
  }

  // ============================================================
  // Member (entity) endpoints
  // ============================================================

  /**
   * POST /2/team/members/list_v2 (and /list/continue_v2). Return every member as
   * {@code {"profile":TeamMemberProfile,"roles":[TeamMemberRole]}}. Single-page mock: has_more false.
   */
  public void membersListV2(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    // include_removed defaults to false: exclude downgraded/removed members (rows kept by a
    // keep_account=true remove) so they read as off the team
    List<DropboxUser> users = HibernateSession.byHqlStatic()
        .createQuery("from DropboxUser where status is null or status <> :removedStatus order by id")
        .setString("removedStatus", DropboxUser.STATUS_REMOVED)
        .list(DropboxUser.class);

    ArrayNode membersArray = GrouperUtil.jsonJacksonArrayNode();
    for (DropboxUser user : users) {
      membersArray.add(buildMemberWithRoles(user));
    }

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("members", membersArray);
    result.put("cursor", "");
    result.put("has_more", false);

    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(result));
  }

  /**
   * POST /2/team/members/get_info_v2. Body {@code {"members":[UserSelectorArg]}} where each selector
   * tag is external_id | email | team_member_id. Return
   * {@code {"members_info":[{".tag":"member_info","profile":..,"roles":..}]}}; an unknown selector
   * yields {@code {".tag":"id_not_found"}}.
   */
  public void membersGetInfoV2(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    ArrayNode selectors = GrouperUtil.jsonJacksonGetArrayNode(body, "members");

    ArrayNode membersInfo = GrouperUtil.jsonJacksonArrayNode();
    if (selectors != null) {
      for (int i = 0; i < selectors.size(); i++) {
        JsonNode selector = selectors.get(i);
        String tag = GrouperUtil.jsonJacksonGetString(selector, ".tag");
        String value = GrouperUtil.jsonJacksonGetString(selector, tag);
        DropboxUser user = findUserBySelector(tag, value);
        if (user == null) {
          ObjectNode notFound = GrouperUtil.jsonJacksonNode();
          notFound.put(".tag", "id_not_found");
          membersInfo.add(notFound);
          continue;
        }
        ObjectNode memberInfo = buildMemberWithRoles(user);
        // MembersGetInfoItemV2 union tag
        memberInfo.put(".tag", "member_info");
        membersInfo.add(memberInfo);
      }
    }

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("members_info", membersInfo);
    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(result));
  }

  /**
   * POST /2/team/members/add_v2. Body {@code {"new_members":[..],"force_async":false}}; each entry
   * carries member_email / member_given_name / member_surname / member_external_id. Assign a native
   * team_member_id of {@code "dbmid:" + uuid} and an account_id, set status active, persist, and
   * return a SYNCHRONOUS {@code {".tag":"complete","complete":[{".tag":"success","success":{"profile":..,"role":..}}]}}.
   */
  public void membersAddV2(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    ArrayNode newMembers = GrouperUtil.jsonJacksonGetArrayNode(body, "new_members");

    ArrayNode completeArray = GrouperUtil.jsonJacksonArrayNode();
    if (newMembers != null) {
      for (int i = 0; i < newMembers.size(); i++) {
        JsonNode newMember = newMembers.get(i);
        String memberEmail = GrouperUtil.jsonJacksonGetString(newMember, "member_email");

        // recoverable conflict: Dropbox returns user_already_on_team when the email is associated with
        // an existing member -- including one removed but still within the 7-day recovery window, or
        // invited. The mock treats an already-known email as that conflict (no duplicate is created),
        // so DropboxApiCommands.createDropboxUser exercises its recover-then-reread path.
        if (findUserBySelector("email", memberEmail) != null) {
          ObjectNode conflict = GrouperUtil.jsonJacksonNode();
          conflict.put(".tag", "user_already_on_team");
          conflict.put("user_already_on_team", memberEmail);
          completeArray.add(conflict);
          continue;
        }

        DropboxUser user = new DropboxUser();
        // Dropbox assigns the native ids; mimic the "dbmid:"/"dbaid:" prefixes
        user.setId("dbmid:" + GrouperUuid.getUuid());
        user.setAccountId("dbaid:" + GrouperUuid.getUuid());
        user.setEmail(memberEmail);
        user.setGivenName(GrouperUtil.jsonJacksonGetString(newMember, "member_given_name"));
        user.setSurname(GrouperUtil.jsonJacksonGetString(newMember, "member_surname"));
        user.setExternalId(GrouperUtil.jsonJacksonGetString(newMember, "member_external_id"));
        // newly added members are active immediately in this mock
        user.setStatus(DropboxUser.STATUS_ACTIVE);
        HibernateSession.byObjectStatic().save(user);

        // MemberAddV2Result success element { ".tag":"success", "success": { "profile":.., "role":.. } }
        ObjectNode success = GrouperUtil.jsonJacksonNode();
        success.put(".tag", "success");
        ObjectNode successPayload = GrouperUtil.jsonJacksonNode();
        successPayload.set("profile", buildTeamMemberProfile(user));
        // brand-new member has no admin role
        ObjectNode roleNode = GrouperUtil.jsonJacksonNode();
        roleNode.put(".tag", "member_only");
        successPayload.set("role", roleNode);
        success.set("success", successPayload);
        completeArray.add(success);
      }
    }

    // synchronous MembersAddLaunchV2Result: tag "complete" carrying the result array
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put(".tag", "complete");
    result.set("complete", completeArray);
    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(result));
  }

  /**
   * POST /2/team/members/set_profile_v2. Body carries a UserSelectorArg plus new_email /
   * new_external_id / new_given_name / new_surname. Apply by team_member_id and return the updated
   * TeamMemberInfoV2 {@code {"profile":TeamMemberProfile}}.
   */
  public void membersSetProfileV2(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    JsonNode userSelector = GrouperUtil.jsonJacksonGetNode(body, "user");
    String teamMemberId = userSelectorValue(userSelector);

    DropboxUser user = findUser(teamMemberId);
    if (user == null) {
      memberNotFound(mockServiceResponse, teamMemberId);
      return;
    }

    String newEmail = GrouperUtil.jsonJacksonGetString(body, "new_email");
    if (newEmail != null) {
      user.setEmail(newEmail);
    }
    String newExternalId = GrouperUtil.jsonJacksonGetString(body, "new_external_id");
    if (newExternalId != null) {
      user.setExternalId(newExternalId);
    }
    String newGivenName = GrouperUtil.jsonJacksonGetString(body, "new_given_name");
    if (newGivenName != null) {
      user.setGivenName(newGivenName);
    }
    String newSurname = GrouperUtil.jsonJacksonGetString(body, "new_surname");
    if (newSurname != null) {
      user.setSurname(newSurname);
    }
    HibernateSession.byObjectStatic().update(user);

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("profile", buildTeamMemberProfile(user));
    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(result));
  }

  /**
   * POST /2/team/members/remove. Body {@code {"user":UserSelectorArg,"wipe_data":..,"keep_account":..}}.
   * Hard-delete the member (and all of their memberships) and return a SYNCHRONOUS LaunchEmptyResult
   * {@code {".tag":"complete"}}.
   */
  public void membersRemove(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    JsonNode userSelector = GrouperUtil.jsonJacksonGetNode(body, "user");
    String teamMemberId = userSelectorValue(userSelector);

    DropboxUser user = findUser(teamMemberId);
    if (user != null) {
      // keep_account (downgrade to a free Basic account) is not allowed for an invited-but-never-
      // accepted member -- Dropbox returns 409 cannot_keep_invited_user_account
      boolean keepAccount = GrouperUtil.jsonJacksonGetBoolean(body, "keep_account", false);
      if (keepAccount && DropboxUser.STATUS_INVITED.equals(user.getStatus())) {
        cannotKeepInvitedUserAccount(mockServiceResponse, user.getEmail());
        return;
      }
      deleteMemberMemberships(teamMemberId);
      if (keepAccount) {
        // downgrade to a free Basic account: the member leaves the team but the account is retained.
        // The mock keeps the row (status=removed, so it no longer lists as a team member) instead of
        // hard-deleting, so the downgrade -- including any pre-downgrade email change -- is observable.
        user.setStatus(DropboxUser.STATUS_REMOVED);
        HibernateSession.byObjectStatic().update(user);
      } else {
        HibernateSession.byObjectStatic().delete(user);
      }
    }

    writeJson(mockServiceResponse, launchComplete());
  }

  /**
   * Write the Dropbox 409 cannot_keep_invited_user_account error (keep_account requested for an
   * invited-but-never-accepted member).
   * @param mockServiceResponse the response
   * @param email the member email (echoed in error_summary)
   */
  private void cannotKeepInvitedUserAccount(MockServiceResponse mockServiceResponse, String email) {
    mockServiceResponse.setResponseCode(409);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(
        "{\"error\":{\".tag\":\"cannot_keep_invited_user_account\"},"
        + "\"error_summary\":\"cannot_keep_invited_user_account/" + email + "\"}");
  }

  /**
   * POST /2/team/members/recover. Body {@code {"user":{".tag":"email","email":..}}}. Restore a
   * removed-but-recoverable member; the mock has no separate removed state, so it simply finds the
   * member by selector and confirms it active, returning an empty object. A member not found returns
   * the not-found error (the real API returns 409 user_unrecoverable / user_not_found).
   */
  public void membersRecover(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    JsonNode userSelector = GrouperUtil.jsonJacksonGetNode(body, "user");
    String tag = GrouperUtil.jsonJacksonGetString(userSelector, ".tag");
    String value = GrouperUtil.jsonJacksonGetString(userSelector, tag);

    DropboxUser user = findUserBySelector(tag, value);
    if (user == null) {
      memberNotFound(mockServiceResponse, value);
      return;
    }

    user.setStatus(DropboxUser.STATUS_ACTIVE);
    HibernateSession.byObjectStatic().update(user);

    writeJson(mockServiceResponse, "{}");
  }

  /**
   * POST /2/team/members/suspend. Body {@code {"user":UserSelectorArg,"wipe_data":..}}. Set the
   * member's status to suspended and return an empty object {@code {}} (the real API replies 200 with
   * no body).
   */
  public void membersSuspend(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    setMemberStatus(mockServiceRequest, DropboxUser.STATUS_SUSPENDED);
    writeJson(mockServiceResponse, "{}");
  }

  /**
   * POST /2/team/members/unsuspend. Body {@code {"user":UserSelectorArg}}. Set the member's status
   * back to active and return an empty object {@code {}}.
   */
  public void membersUnsuspend(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    setMemberStatus(mockServiceRequest, DropboxUser.STATUS_ACTIVE);
    writeJson(mockServiceResponse, "{}");
  }

  /**
   * POST /2/team/members/set_admin_permissions_v2. Body {@code {"user":UserSelectorArg,"new_roles":[role_id,..]}}.
   * Map the (first) role_id back to its admin-role NAME via the fixed catalog and store it in
   * {@code admin_role}; an empty new_roles list clears the role (member_only == null). Return
   * {@code {"team_member_id":..}}.
   */
  public void membersSetAdminPermissionsV2(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    JsonNode userSelector = GrouperUtil.jsonJacksonGetNode(body, "user");
    String teamMemberId = userSelectorValue(userSelector);

    DropboxUser user = findUser(teamMemberId);
    if (user == null) {
      memberNotFound(mockServiceResponse, teamMemberId);
      return;
    }

    ArrayNode newRoles = GrouperUtil.jsonJacksonGetArrayNode(body, "new_roles");
    if (newRoles == null || newRoles.size() == 0) {
      // empty list demotes the member to member_only
      user.setAdminRole(null);
    } else {
      // a member carries a single effective admin role in this mock; take the first role_id
      String roleId = newRoles.get(0).asText();
      user.setAdminRole(roleNameForRoleId(roleId));
    }
    HibernateSession.byObjectStatic().update(user);

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("team_member_id", user.getId());
    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(result));
  }

  /**
   * POST /2/team/members/list_member_roles. Body {@code {"team_member_id":".."}}. Returns the
   * full set of ASSIGNABLE admin roles for the team as {@code {"roles":[TeamMemberRole]}} -- i.e.
   * the complete 8-role catalog, NOT just the member's current role. This mirrors the real Dropbox
   * endpoint (which lists roles a member can be assigned) and is what lets a caller resolve a role
   * NAME to a role_id on a fresh team where no member yet holds a role.
   */
  public void membersListMemberRoles(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode body = parseBody(mockServiceRequest);
    String teamMemberId = GrouperUtil.jsonJacksonGetString(body, "team_member_id");

    DropboxUser user = findUser(teamMemberId);
    if (user == null) {
      memberNotFound(mockServiceResponse, teamMemberId);
      return;
    }

    // return the full assignable catalog (all 8 built-in admin roles), not the member's current role
    ArrayNode rolesArray = GrouperUtil.jsonJacksonArrayNode();
    for (String roleName : DropboxUser.ADMIN_ROLE_HIERARCHY) {
      rolesArray.add(buildTeamMemberRole(roleName));
    }

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("roles", rolesArray);
    writeJson(mockServiceResponse, GrouperUtil.jsonJacksonToString(result));
  }

  // ============================================================
  // JSON builders (shared object shapes)
  // ============================================================

  /**
   * Build a GroupSummary: {@code {group_name, group_id, group_external_id, group_management_type:{".tag"},
   * member_count}}. member_count is computed live from the membership table.
   * @param group the group
   * @return the summary node
   */
  private ObjectNode buildGroupSummary(DropboxGroup group) {
    ObjectNode node = GrouperUtil.jsonJacksonNode();
    node.put("group_name", group.getName());
    node.put("group_id", group.getId());
    if (group.getExternalId() != null) {
      node.put("group_external_id", group.getExternalId());
    }
    ObjectNode managementType = GrouperUtil.jsonJacksonNode();
    managementType.put(".tag",
        GrouperUtil.defaultIfBlank(group.getManagementType(), DropboxGroup.MANAGEMENT_TYPE_COMPANY_MANAGED));
    node.set("group_management_type", managementType);
    node.put("member_count", countGroupMemberships(group.getId()));
    return node;
  }

  /**
   * Build a GroupFullInfo: a GroupSummary optionally extended with a {@code members[]} array of
   * GroupMemberInfo (each {@code {"profile":TeamMemberProfile,"access_type":{".tag":..}}}).
   * @param group the group
   * @param includeMembers whether to attach the members[] array
   * @return the full-info node
   */
  private ObjectNode buildGroupFullInfo(DropboxGroup group, boolean includeMembers) {
    ObjectNode node = buildGroupSummary(group);
    if (includeMembers) {
      node.set("members", buildGroupMemberInfoArray(group.getId()));
    }
    return node;
  }

  /**
   * Build the GroupMemberInfo array for a group: one {@code {"profile":..,"access_type":{".tag":..}}}
   * per membership, joining the membership to its member profile.
   * @param groupId the native group_id
   * @return the members array
   */
  private ArrayNode buildGroupMemberInfoArray(String groupId) {
    ArrayNode membersArray = GrouperUtil.jsonJacksonArrayNode();
    List<DropboxMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from DropboxMembership where groupId = :theGroupId")
        .setString("theGroupId", groupId)
        .list(DropboxMembership.class);
    for (DropboxMembership membership : memberships) {
      DropboxUser user = findUser(membership.getTeamMemberId());
      if (user == null) {
        continue;
      }
      ObjectNode memberInfo = GrouperUtil.jsonJacksonNode();
      memberInfo.set("profile", buildTeamMemberProfile(user));
      ObjectNode accessType = GrouperUtil.jsonJacksonNode();
      accessType.put(".tag",
          GrouperUtil.defaultIfBlank(membership.getAccessType(), DropboxMembership.ACCESS_TYPE_MEMBER));
      memberInfo.set("access_type", accessType);
      membersArray.add(memberInfo);
    }
    return membersArray;
  }

  /**
   * Build a TeamMemberProfile: {@code {team_member_id, email, external_id, account_id,
   * status:{".tag"}, name:{given_name, surname, display_name}}}.
   * @param user the member
   * @return the profile node
   */
  private ObjectNode buildTeamMemberProfile(DropboxUser user) {
    ObjectNode profile = GrouperUtil.jsonJacksonNode();
    profile.put("team_member_id", user.getId());
    profile.put("email", user.getEmail());
    if (user.getExternalId() != null) {
      profile.put("external_id", user.getExternalId());
    }
    if (user.getAccountId() != null) {
      profile.put("account_id", user.getAccountId());
    }
    ObjectNode status = GrouperUtil.jsonJacksonNode();
    status.put(".tag", GrouperUtil.defaultIfBlank(user.getStatus(), DropboxUser.STATUS_ACTIVE));
    profile.set("status", status);

    String givenName = GrouperUtil.defaultString(user.getGivenName());
    String surname = GrouperUtil.defaultString(user.getSurname());
    ObjectNode name = GrouperUtil.jsonJacksonNode();
    name.put("given_name", givenName);
    name.put("surname", surname);
    name.put("display_name", StringUtils.trim(givenName + " " + surname));
    profile.set("name", name);
    return profile;
  }

  /**
   * Build a member node carrying the profile and the roles array:
   * {@code {"profile":TeamMemberProfile,"roles":[TeamMemberRole]}}. Used by members/list_v2 and
   * members/get_info_v2.
   * @param user the member
   * @return the member node
   */
  private ObjectNode buildMemberWithRoles(DropboxUser user) {
    ObjectNode node = GrouperUtil.jsonJacksonNode();
    node.set("profile", buildTeamMemberProfile(user));
    node.set("roles", buildRolesArray(user));
    return node;
  }

  /**
   * Build the TeamMemberRole array for a member from its stored admin_role (empty when null /
   * member_only). Each role is {@code {role_id, name, description}} with a deterministic synthetic id.
   * @param user the member
   * @return the roles array (possibly empty, never null)
   */
  private ArrayNode buildRolesArray(DropboxUser user) {
    ArrayNode rolesArray = GrouperUtil.jsonJacksonArrayNode();
    String adminRole = user.getAdminRole();
    if (!StringUtils.isBlank(adminRole)) {
      rolesArray.add(buildTeamMemberRole(adminRole));
    }
    return rolesArray;
  }

  /**
   * Build a single TeamMemberRole {@code {role_id, name, description}} for an admin-role name, using
   * the fixed synthetic role_id catalog.
   * @param roleName one of the 8 built-in admin-role names
   * @return the role node
   */
  private ObjectNode buildTeamMemberRole(String roleName) {
    ObjectNode role = GrouperUtil.jsonJacksonNode();
    role.put("role_id", roleIdForName(roleName));
    role.put("name", roleName);
    role.put("description", "Dropbox " + roleName.replace('_', ' '));
    return role;
  }

  /**
   * Build a GroupMembersChangeResult {@code {"group_info":GroupFullInfo}} with no async_job_id (the
   * mock completes synchronously).
   * @param group the changed group
   * @return the change-result node
   */
  private ObjectNode groupMembersChangeResult(DropboxGroup group) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("group_info", buildGroupFullInfo(group, true));
    return result;
  }

  /**
   * @return a synchronous LaunchEmptyResult {@code {".tag":"complete"}} JSON string
   */
  private String launchComplete() {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put(".tag", "complete");
    return GrouperUtil.jsonJacksonToString(result);
  }

  // ============================================================
  // Admin-role catalog (fixed mapping name <-> synthetic role_id)
  // ============================================================

  /**
   * Map an admin-role NAME to its fixed synthetic role_id ({@code "pid_dbtmr:" + lowercase(name)}).
   * @param roleName the admin-role name
   * @return the synthetic role_id
   */
  public static String roleIdForName(String roleName) {
    return ROLE_ID_PREFIX + roleName.toLowerCase();
  }

  /**
   * Reverse-map a synthetic role_id back to its admin-role NAME by scanning the fixed 8-role catalog.
   * Returns null when the id matches no known role.
   * @param roleId the synthetic role_id
   * @return the admin-role name, or null
   */
  public static String roleNameForRoleId(String roleId) {
    if (StringUtils.isBlank(roleId)) {
      return null;
    }
    for (String roleName : DropboxUser.ADMIN_ROLE_HIERARCHY) {
      if (roleIdForName(roleName).equals(roleId)) {
        return roleName;
      }
    }
    return null;
  }

  // ============================================================
  // Data-access helpers
  // ============================================================

  /**
   * Find a group by its native group_id.
   * @param groupId the group_id
   * @return the group, or null
   */
  private DropboxGroup findGroup(String groupId) {
    if (StringUtils.isBlank(groupId)) {
      return null;
    }
    return HibernateSession.byHqlStatic()
        .createQuery("from DropboxGroup where id = :theId")
        .setString("theId", groupId)
        .uniqueResult(DropboxGroup.class);
  }

  /**
   * Find a member by its native team_member_id.
   * @param teamMemberId the team_member_id
   * @return the member, or null
   */
  private DropboxUser findUser(String teamMemberId) {
    if (StringUtils.isBlank(teamMemberId)) {
      return null;
    }
    return HibernateSession.byHqlStatic()
        .createQuery("from DropboxUser where id = :theId")
        .setString("theId", teamMemberId)
        .uniqueResult(DropboxUser.class);
  }

  /**
   * Find a member by a UserSelectorArg union tag (external_id | email | team_member_id) and value.
   * @param tag the union discriminator
   * @param value the matching value
   * @return the member, or null
   */
  private DropboxUser findUserBySelector(String tag, String value) {
    if (StringUtils.isBlank(tag) || StringUtils.isBlank(value)) {
      return null;
    }
    if ("team_member_id".equals(tag)) {
      return findUser(value);
    }
    if ("email".equals(tag)) {
      return HibernateSession.byHqlStatic()
          .createQuery("from DropboxUser where email = :theValue")
          .setString("theValue", value)
          .uniqueResult(DropboxUser.class);
    }
    if ("external_id".equals(tag)) {
      return HibernateSession.byHqlStatic()
          .createQuery("from DropboxUser where externalId = :theValue")
          .setString("theValue", value)
          .uniqueResult(DropboxUser.class);
    }
    return null;
  }

  /**
   * Find a single membership by group_id + team_member_id.
   * @param groupId the group_id
   * @param teamMemberId the team_member_id
   * @return the membership, or null
   */
  private DropboxMembership findMembership(String groupId, String teamMemberId) {
    return HibernateSession.byHqlStatic()
        .createQuery("from DropboxMembership where groupId = :theGroupId and teamMemberId = :theMemberId")
        .setString("theGroupId", groupId)
        .setString("theMemberId", teamMemberId)
        .uniqueResult(DropboxMembership.class);
  }

  /**
   * Count the memberships of a group (used for member_count).
   * @param groupId the group_id
   * @return the membership count
   */
  private int countGroupMemberships(String groupId) {
    Long count = HibernateSession.byHqlStatic()
        .createQuery("select count(*) from DropboxMembership where groupId = :theGroupId")
        .setString("theGroupId", groupId)
        .uniqueResult(Long.class);
    return count == null ? 0 : count.intValue();
  }

  /**
   * Delete all memberships of a group (on group delete).
   * @param groupId the group_id
   */
  private void deleteGroupMemberships(String groupId) {
    List<DropboxMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from DropboxMembership where groupId = :theGroupId")
        .setString("theGroupId", groupId)
        .list(DropboxMembership.class);
    for (DropboxMembership membership : memberships) {
      HibernateSession.byObjectStatic().delete(membership);
    }
  }

  /**
   * Delete all memberships of a member (on member remove).
   * @param teamMemberId the team_member_id
   */
  private void deleteMemberMemberships(String teamMemberId) {
    List<DropboxMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from DropboxMembership where teamMemberId = :theMemberId")
        .setString("theMemberId", teamMemberId)
        .list(DropboxMembership.class);
    for (DropboxMembership membership : memberships) {
      HibernateSession.byObjectStatic().delete(membership);
    }
  }

  /**
   * Shared body parse for suspend/unsuspend: resolve the selected member and set its status.
   * @param mockServiceRequest the request
   * @param status the new status
   */
  private void setMemberStatus(MockServiceRequest mockServiceRequest, String status) {
    JsonNode body = parseBody(mockServiceRequest);
    JsonNode userSelector = GrouperUtil.jsonJacksonGetNode(body, "user");
    String teamMemberId = userSelectorValue(userSelector);
    DropboxUser user = findUser(teamMemberId);
    if (user != null) {
      user.setStatus(status);
      HibernateSession.byObjectStatic().update(user);
    }
  }

  // ============================================================
  // JSON / selector parse helpers
  // ============================================================

  /**
   * Parse the request body into a JSON node (empty bodies map to an empty object).
   * @param mockServiceRequest the request
   * @return the parsed body node
   */
  private JsonNode parseBody(MockServiceRequest mockServiceRequest) {
    String requestBody = mockServiceRequest.getRequestBody();
    if (StringUtils.isBlank(requestBody)) {
      return GrouperUtil.jsonJacksonNode();
    }
    return GrouperUtil.jsonJacksonNode(requestBody);
  }

  /**
   * Extract the group_id from a request body that carries a GroupSelector under a "group" wrapper:
   * {@code {"group":{".tag":"group_id","group_id":".."}}}.
   * @param body the request body node
   * @return the group_id, or null
   */
  private String groupSelectorGroupId(JsonNode body) {
    JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(body, "group");
    if (groupNode == null) {
      return null;
    }
    return GrouperUtil.jsonJacksonGetString(groupNode, "group_id");
  }

  /**
   * Extract the value from a UserSelectorArg union {@code {".tag":<tag>,<tag>:<value>}}.
   * @param userSelector the selector node
   * @return the selected value, or null
   */
  private String userSelectorValue(JsonNode userSelector) {
    if (userSelector == null) {
      return null;
    }
    String tag = GrouperUtil.jsonJacksonGetString(userSelector, ".tag");
    if (StringUtils.isBlank(tag)) {
      return null;
    }
    return GrouperUtil.jsonJacksonGetString(userSelector, tag);
  }

  /**
   * Read the {@code ".tag"} of a nested union field (e.g. access_type / group_management_type),
   * falling back to a default when the field or tag is absent.
   * @param parent the parent node
   * @param fieldName the union field name
   * @param defaultTag the fallback tag
   * @return the union tag
   */
  private String unionTag(JsonNode parent, String fieldName, String defaultTag) {
    JsonNode unionNode = GrouperUtil.jsonJacksonGetNode(parent, fieldName);
    if (unionNode == null) {
      return defaultTag;
    }
    return GrouperUtil.defaultIfBlank(GrouperUtil.jsonJacksonGetString(unionNode, ".tag"), defaultTag);
  }

  // ============================================================
  // Response helpers
  // ============================================================

  /**
   * Run the bearer-token check, translating an auth failure into a 401 (Dropbox uses 401 for bad
   * tokens) before rethrowing so the servlet still records the failure.
   * @param mockServiceRequest the request
   * @param mockServiceResponse the response
   */
  private void authorize(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }
  }

  /**
   * Write a 200 JSON response body.
   * @param mockServiceResponse the response
   * @param json the JSON string
   */
  private void writeJson(MockServiceResponse mockServiceResponse, String json) {
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(json);
  }

  /**
   * Emit a 409 group-not-found error (Dropbox returns 409 with an error union for bad group ids).
   * @param mockServiceResponse the response
   * @param groupId the missing group_id
   */
  private void groupNotFound(MockServiceResponse mockServiceResponse, String groupId) {
    mockServiceResponse.setResponseCode(409);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(
        "{\"error\":{\".tag\":\"group_not_found\"},\"error_summary\":\"group_not_found/" + groupId + "\"}");
  }

  /**
   * Emit a 409 member-not-found error.
   * @param mockServiceResponse the response
   * @param teamMemberId the missing team_member_id
   */
  private void memberNotFound(MockServiceResponse mockServiceResponse, String teamMemberId) {
    mockServiceResponse.setResponseCode(409);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(
        "{\"error\":{\".tag\":\"user_not_found\"},\"error_summary\":\"user_not_found/" + teamMemberId + "\"}");
  }

  // ============================================================
  // Request routing
  // ============================================================

  /**
   * Route the incoming mock request. All Dropbox Team API calls are POSTs whose path after
   * {@code /mockServices/dropbox} is a fixed endpoint (e.g. {@code /2/team/groups/list}). The path
   * segments are joined and dispatched to the matching handler method, mirroring the
   * TrueFoundry mock's POST-plus-path routing style.
   * @param mockServiceRequest the request
   * @param mockServiceResponse the response
   */
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureDropboxMockTables();
    }
    mockTablesThere = true;

    // the test harness writes this config asynchronously; wait briefly for it (mirrors TrueFoundry)
    String configId = GrouperConfig.retrieveConfig().propertyValueString(
        "grouperTest.exampleDropbox.mockExternalSystem.configId");
    if (StringUtils.isBlank(configId)) {
      for (int i = 0; i < 40; i++) {
        configId = GrouperConfig.retrieveConfig().propertyValueString(
            "grouperTest.exampleDropbox.mockExternalSystem.configId");
        if (!StringUtils.isBlank(configId)) {
          break;
        }
        if (i == 39) {
          throw new RuntimeException(
              "grouper.properties grouperTest.exampleDropbox.mockExternalSystem.configId must be set!");
        }
        GrouperUtil.sleep(1000);
      }
    }

    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }

    String httpMethod = mockServiceRequest.getHttpServletRequest().getMethod();
    // Dropbox is POST-only; join the path segments into the full endpoint path for dispatch
    String path = "/" + StringUtils.join(mockServiceRequest.getPostMockNamePaths(), "/");

    if (StringUtils.equals("POST", httpMethod)) {

      // --- groups ---
      // OAuth2 token endpoint -- no bearer auth (authenticates via client credentials in the body)
      if ("/oauth2/token".equals(path)) {
        oauth2Token(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/groups/list".equals(path) || "/2/team/groups/list/continue".equals(path)) {
        groupsList(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/groups/get_info".equals(path)) {
        groupsGetInfo(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/groups/create".equals(path)) {
        groupsCreate(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/groups/update".equals(path)) {
        groupsUpdate(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/groups/delete".equals(path)) {
        groupsDelete(mockServiceRequest, mockServiceResponse);
        return;
      }

      // --- group memberships ---
      if ("/2/team/groups/members/add".equals(path)) {
        groupsMembersAdd(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/groups/members/remove".equals(path)) {
        groupsMembersRemove(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/groups/members/list".equals(path)
          || "/2/team/groups/members/list/continue".equals(path)) {
        groupsMembersList(mockServiceRequest, mockServiceResponse);
        return;
      }

      // --- members (entities) ---
      if ("/2/team/members/list_v2".equals(path)
          || "/2/team/members/list/continue_v2".equals(path)) {
        membersListV2(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/members/get_info_v2".equals(path)) {
        membersGetInfoV2(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/members/add_v2".equals(path)) {
        membersAddV2(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/members/set_profile_v2".equals(path)) {
        membersSetProfileV2(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/members/remove".equals(path)) {
        membersRemove(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/members/recover".equals(path)) {
        membersRecover(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/members/suspend".equals(path)) {
        membersSuspend(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/members/unsuspend".equals(path)) {
        membersUnsuspend(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/members/set_admin_permissions_v2".equals(path)) {
        membersSetAdminPermissionsV2(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("/2/team/members/list_member_roles".equals(path)) {
        membersListMemberRoles(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    throw new RuntimeException("Unhandled Dropbox mock request: " + httpMethod + " " + path);
  }

}
