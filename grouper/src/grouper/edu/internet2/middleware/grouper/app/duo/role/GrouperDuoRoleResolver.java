package edu.internet2.middleware.grouper.app.duo.role;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Resolves Duo admin role values (role names or role ids) to Duo role_id values, and picks
 * which role to assign when an administrator is in more than one role group.
 *
 * Duo's legacy "role" field (e.g. "Help Desk") is being deprecated in favor of "role_id"
 * (e.g. help_desk), and only role_id can assign a custom admin role.  The provisioner sends
 * and compares role_id only.  The value in Grouper (e.g. group metadata or group display
 * extension) can be either a role id or a role name:
 *
 * 1. exact built-in role id (or legacy alias like integration_manager) -> that role id
 * 2. built-in role name, case insensitive (e.g. "administrator") -> its role id (service_manager)
 * 3. exact custom role id from GET /admin/v1/admin_roles -> that role id
 * 4. custom role name from GET /admin/v1/admin_roles, case insensitive -> its role id
 * 5. anything else is passed through as a role id, so a custom role id still works if the
 *    Admin API application cannot list roles; Duo rejects it on write if it is not valid
 *
 * Duo enforces unique role names case insensitive, including against the built-in names, so a
 * name maps to exactly one role id.
 *
 * Custom roles are only retrieved from Duo (needs the "Grant resource - Read" Admin API
 * permission) if a value does not resolve to a built-in role, and only once per resolver.
 *
 * Priority: Duo allows one role per administrator, so the role with the highest priority wins.
 * Built-in roles are 100 (read_only) to 800 (owner), custom roles default to 150 (just above
 * read_only), and the provisioner config rolePriority (JSON of role id or name to integer)
 * overrides any of these.  Ties go to the alphabetically first role id so the pick is stable.
 *
 * @author mchyzer
 */
public class GrouperDuoRoleResolver {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDuoRoleResolver.class);

  /**
   * built-in Duo role_id to legacy role name, highest default priority first
   */
  public static final Map<String, String> BUILT_IN_ROLE_ID_TO_NAME;

  /**
   * default priority of built-in role ids, higher wins
   */
  public static final Map<String, Integer> BUILT_IN_ROLE_ID_TO_PRIORITY;

  /**
   * old role ids Duo might still return, to the current role id
   */
  public static final Map<String, String> ROLE_ID_ALIASES;

  /**
   * priority of a custom role not in the rolePriority config, just above read_only so it
   * never outranks a real built-in role other than read_only
   */
  public static final int DEFAULT_CUSTOM_ROLE_PRIORITY = 150;

  static {
    Map<String, String> roleIdToName = new LinkedHashMap<String, String>();
    roleIdToName.put("owner", "Owner");
    // Duo's role_id for the "Administrator" role is service_manager
    roleIdToName.put("service_manager", "Administrator");
    roleIdToName.put("application_manager", "Application Manager");
    roleIdToName.put("user_manager", "User Manager");
    roleIdToName.put("security_analyst", "Security Analyst");
    roleIdToName.put("help_desk", "Help Desk");
    roleIdToName.put("billing", "Billing");
    roleIdToName.put("read_only", "Read-only");
    BUILT_IN_ROLE_ID_TO_NAME = Collections.unmodifiableMap(roleIdToName);

    // 800 down to 100 in steps of 100, so custom roles can be slotted in between
    Map<String, Integer> roleIdToPriority = new LinkedHashMap<String, Integer>();
    int priority = 100 * roleIdToName.size();
    for (String roleId : roleIdToName.keySet()) {
      roleIdToPriority.put(roleId, priority);
      priority -= 100;
    }
    BUILT_IN_ROLE_ID_TO_PRIORITY = Collections.unmodifiableMap(roleIdToPriority);

    Map<String, String> aliases = new HashMap<String, String>();
    // application_manager was formerly integration_manager
    aliases.put("integration_manager", "application_manager");
    ROLE_ID_ALIASES = Collections.unmodifiableMap(aliases);
  }

  /**
   * get the resolver of the current Duo role provisioner, or one with just built-in roles if
   * not running in a Duo role provisioner
   * @return the resolver
   */
  public static GrouperDuoRoleResolver retrieveCurrent() {
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    if (grouperProvisioner instanceof GrouperDuoRoleProvisioner) {
      return ((GrouperDuoRoleProvisioner)grouperProvisioner).retrieveDuoRoleResolver();
    }
    return new GrouperDuoRoleResolver(null, null);
  }

  /**
   * parse and validate the rolePriority config, e.g. {"Tier 2 Support": 350, "RLU6PE8ICYDAWJY30ZJ2": 750}
   * @param rolePriorityJson
   * @return role id or name to priority, empty if blank
   * @throws RuntimeException if the json is not an object of integers
   */
  public static Map<String, Integer> parseRolePriorityJson(String rolePriorityJson) {
    Map<String, Integer> result = new LinkedHashMap<String, Integer>();
    if (StringUtils.isBlank(rolePriorityJson)) {
      return result;
    }
    JsonNode jsonNode = null;
    try {
      jsonNode = GrouperUtil.jsonJacksonNode(rolePriorityJson);
    } catch (Exception e) {
      throw new RuntimeException("rolePriority is not valid JSON: " + rolePriorityJson, e);
    }
    if (jsonNode == null || !jsonNode.isObject()) {
      throw new RuntimeException("rolePriority must be a JSON object of role id or name to integer priority, e.g. {\"Tier 2 Support\": 350}");
    }
    Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      if (StringUtils.isBlank(field.getKey()) || field.getValue() == null || !field.getValue().isIntegralNumber()) {
        throw new RuntimeException("rolePriority entry must have a role id or name and an integer priority: '"
            + field.getKey() + "': " + field.getValue());
      }
      result.put(field.getKey().trim(), field.getValue().asInt());
    }
    return result;
  }

  /**
   * duo external system config id, null if custom roles should not be retrieved
   */
  private String duoExternalSystemConfigId;

  /**
   * rolePriority config json
   */
  private String rolePriorityJson;

  /**
   * custom role id to name, null until retrieved from Duo
   */
  private Map<String, String> customRoleIdToName = null;

  /**
   * lower case custom role name to role id
   */
  private Map<String, String> customRoleLowerNameToId = null;

  /**
   * role id to priority (built-in defaults plus config overrides), null until computed
   */
  private Map<String, Integer> roleIdToPriority = null;

  /**
   * @param duoExternalSystemConfigId null to only resolve built-in roles
   * @param rolePriorityJson rolePriority config, or null
   */
  public GrouperDuoRoleResolver(String duoExternalSystemConfigId, String rolePriorityJson) {
    this.duoExternalSystemConfigId = duoExternalSystemConfigId;
    this.rolePriorityJson = rolePriorityJson;
  }

  /**
   * set the custom roles instead of retrieving them from Duo (for testing)
   * @param theCustomRoleIdToName
   */
  public void assignCustomRoles(Map<String, String> theCustomRoleIdToName) {
    this.customRoleIdToName = new LinkedHashMap<String, String>();
    this.customRoleLowerNameToId = new HashMap<String, String>();
    for (String roleId : GrouperUtil.nonNull(theCustomRoleIdToName).keySet()) {
      String roleName = theCustomRoleIdToName.get(roleId);
      this.customRoleIdToName.put(roleId, roleName);
      if (!StringUtils.isBlank(roleName)) {
        this.customRoleLowerNameToId.put(roleName.toLowerCase(), roleId);
      }
    }
    // priorities may reference custom role names
    this.roleIdToPriority = null;
  }

  /**
   * retrieve custom roles from Duo once.  If that fails (e.g. the Admin API application does
   * not have "Grant resource - Read"), log it and continue with built-in roles only
   */
  private void retrieveCustomRolesIfNeeded() {
    if (this.customRoleIdToName != null) {
      return;
    }
    Map<String, String> customRoles = new LinkedHashMap<String, String>();
    if (!StringUtils.isBlank(this.duoExternalSystemConfigId)) {
      try {
        customRoles = GrouperDuoRoleApiCommands.retrieveDuoCustomAdminRoles(this.duoExternalSystemConfigId);
      } catch (RuntimeException re) {
        LOG.warn("Cannot retrieve Duo admin roles for external system '" + this.duoExternalSystemConfigId
            + "' (the Admin API application needs 'Grant resource - Read'), custom role names will not resolve to role ids", re);
      }
    }
    this.assignCustomRoles(customRoles);
  }

  /**
   * resolve a role id or role name to a Duo role_id
   * @param roleIdOrName e.g. help_desk, Help Desk, Administrator, a custom role name or id
   * @return the role id, or null if blank
   */
  public String resolveRoleId(String roleIdOrName) {
    if (StringUtils.isBlank(roleIdOrName)) {
      return null;
    }
    String value = roleIdOrName.trim();

    // built-in role id
    if (BUILT_IN_ROLE_ID_TO_NAME.containsKey(value)) {
      return value;
    }
    if (ROLE_ID_ALIASES.containsKey(value)) {
      return ROLE_ID_ALIASES.get(value);
    }

    // built-in role name, Duo role names are case insensitive
    for (String roleId : BUILT_IN_ROLE_ID_TO_NAME.keySet()) {
      if (StringUtils.equalsIgnoreCase(BUILT_IN_ROLE_ID_TO_NAME.get(roleId), value)) {
        return roleId;
      }
    }

    // custom role id or name from Duo
    this.retrieveCustomRolesIfNeeded();
    if (this.customRoleIdToName.containsKey(value)) {
      return value;
    }
    String customRoleId = this.customRoleLowerNameToId.get(value.toLowerCase());
    if (customRoleId != null) {
      return customRoleId;
    }

    // not known, assume it is a role id, Duo will reject it on write if not
    if (LOG.isDebugEnabled()) {
      LOG.debug("Duo role '" + value + "' is not a known role id or name, sending it as a role id");
    }
    return value;
  }

  /**
   * priority of a role, higher wins
   * @param roleId resolved role id
   * @return the priority
   */
  public int priority(String roleId) {
    if (this.roleIdToPriority == null) {
      Map<String, Integer> theRoleIdToPriority = new HashMap<String, Integer>(BUILT_IN_ROLE_ID_TO_PRIORITY);
      // config keys can be role ids or names, resolve them the same way as group values
      Map<String, Integer> configPriorities = parseRolePriorityJson(this.rolePriorityJson);
      for (String roleIdOrName : configPriorities.keySet()) {
        theRoleIdToPriority.put(this.resolveRoleId(roleIdOrName), configPriorities.get(roleIdOrName));
      }
      this.roleIdToPriority = theRoleIdToPriority;
    }
    Integer priority = this.roleIdToPriority.get(roleId);
    return priority == null ? DEFAULT_CUSTOM_ROLE_PRIORITY : priority;
  }

  /**
   * resolve the roles and pick the one with the highest priority, ties go to the
   * alphabetically first role id
   * @param roleIdsOrNames
   * @return the role id or null if none
   */
  public String pickHighestPriorityRoleId(Collection<String> roleIdsOrNames) {
    String bestRoleId = null;
    int bestPriority = Integer.MIN_VALUE;
    for (String roleIdOrName : GrouperUtil.nonNull(roleIdsOrNames)) {
      String roleId = this.resolveRoleId(roleIdOrName);
      if (roleId == null) {
        continue;
      }
      int priority = this.priority(roleId);
      if (bestRoleId == null || priority > bestPriority
          || (priority == bestPriority && roleId.compareTo(bestRoleId) < 0)) {
        bestRoleId = roleId;
        bestPriority = priority;
      }
    }
    return bestRoleId;
  }

  /**
   * role name for a role id (built-in or custom already retrieved), for logging and the mock
   * @param roleId
   * @return the name or null if not known
   */
  public String roleName(String roleId) {
    if (BUILT_IN_ROLE_ID_TO_NAME.containsKey(roleId)) {
      return BUILT_IN_ROLE_ID_TO_NAME.get(roleId);
    }
    return this.customRoleIdToName == null ? null : this.customRoleIdToName.get(roleId);
  }

}
