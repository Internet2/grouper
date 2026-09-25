/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAddMember;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAdminExternalSystemGet;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAdminGetDaemonJobMessage;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAdminGetDaemonJobs;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAdminRunDaemonJob;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAdminSearchConfigs;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAdminSearchDaemons;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAssignAttributes;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAssignGrouperPrivilegesLite;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAuthUser;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpDeleteMember;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpDocSearch;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpFindAttributeDefNames;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpFindGroups;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpFindStems;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpFolderDelete;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpGetAttributeAssignmentsLite;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpGetAuditEntries;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpGetGrouperPrivilegesLite;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpGetGroups;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpGetMembersLite;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpGetMemberships;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpGetSubjects;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpGroupDelete;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpGroupSave;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpHasMember;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpInstitutionalTools;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpLdapSearch;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpRecipeTool;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpSqlGetSchema;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpSqlSelect;

/**
 * puts every tool into {@link GrouperToolRegistry}.
 *
 * <p>this is the list the MCP servlet's dispatch switch used to be, with the category each tool
 * needs alongside it, which used to be a separate switch in {@link GrouperMcpToolLog}, and the
 * conditions under which a tool is worth offering, which used to be scattered through the
 * servlet's tools/list method.  keeping them together is the point: a tool cannot be added now
 * without saying what it is allowed to do, where before an unlisted name quietly counted as a
 * read.</p>
 *
 * <p>the order here is the order tools are advertised in, and is deliberately the order the
 * servlet listed them in before this moved, so that a client sees the same list it saw
 * before.</p>
 */
public class GrouperToolRegistration {

  /** whether registration has run */
  private static boolean registered = false;

  /**
   * register every tool, once
   */
  public static synchronized void registerIfNeeded() {

    if (registered) {
      return;
    }

    // set before registering so that anything reached from a tool class cannot recurse into
    // here, but put back if registration does not finish.  otherwise a tool which failed to
    // register would leave a half filled registry which every later call quietly accepted, and
    // the missing tools would look like tools which do not exist
    registered = true;

    try {
      registerReadonlyTools();
      registerReadwriteTools();
      registerSqlTools();
      registerAdminTools();

    } catch (RuntimeException re) {
      registered = false;
      GrouperToolRegistry.clear();
      throw re;
    }
  }

  /**
   * reads of groups, folders, members, privileges and attributes
   */
  private static void registerReadonlyTools() {

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("doc_search",
        GrouperToolCategory.readonly) {
      public boolean availableFor(GrouperMcpAuthUser authUser) {
        // nothing to search unless this caller has at least one source
        return GrouperMcpDocSearchIndex.hasAnySourcesForSubject(authUser.getSubject());
      }
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpDocSearch.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpDocSearch.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("attribute_def_name_find",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpFindAttributeDefNames.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpFindAttributeDefNames.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("group_find",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpFindGroups.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpFindGroups.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("folder_find",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpFindStems.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpFindStems.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("attribute_assignment_get",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpGetAttributeAssignmentsLite.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpGetAttributeAssignmentsLite.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("audit_get",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpGetAuditEntries.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpGetAuditEntries.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("privilege_get",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpGetGrouperPrivilegesLite.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpGetGrouperPrivilegesLite.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("entity_get_groups",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpGetGroups.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpGetGroups.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("group_get_members",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpGetMembersLite.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpGetMembersLite.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("memberships_get",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpGetMemberships.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpGetMemberships.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("entity_get",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpGetSubjects.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpGetSubjects.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("group_has_member",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpHasMember.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpHasMember.execute(arguments, authUser);
      }
    });

    // these two describe what this institution has set up, so they say for themselves whether
    // there is anything to offer by returning no definition, which the tool list skips
    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("institutional_tools",
        GrouperToolCategory.readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpInstitutionalTools.toolDefinition(authUser,
            GrouperToolAccess.isAllowedReadwrite(authUser));
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpInstitutionalTools.execute(arguments, authUser,
            GrouperToolAccess.isAllowedReadwrite(authUser));
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("recipe",
        GrouperToolCategory.readonly) {
      public GrouperToolCategory category(JsonNode arguments) {
        // list and get are ordinary reads; update writes standing guidance for everybody who
        // consults it.  an unrecognised or missing action is refused by the tool anyway, so it
        // counts as a read rather than being given the more generous write budget
        String action = GrouperUtil.jsonJacksonGetString(arguments, "action");
        return "update".equals(action) ? GrouperToolCategory.readwrite
            : GrouperToolCategory.readonly;
      }
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpRecipeTool.toolDefinition(authUser,
            GrouperToolAccess.isAllowedReadwrite(authUser));
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpRecipeTool.execute(arguments, authUser,
            GrouperToolAccess.isAllowedReadwrite(authUser));
      }
    });
  }

  /**
   * tools which change groups, folders, memberships, privileges or attributes
   */
  private static void registerReadwriteTools() {

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("group_add_member",
        GrouperToolCategory.readwrite) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpAddMember.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpAddMember.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("attribute_assignment_save",
        GrouperToolCategory.readwrite) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpAssignAttributes.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpAssignAttributes.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("privilege_assign",
        GrouperToolCategory.readwrite) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpAssignGrouperPrivilegesLite.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpAssignGrouperPrivilegesLite.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("group_remove_member",
        GrouperToolCategory.readwrite) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpDeleteMember.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpDeleteMember.execute(arguments, authUser);
      }
    });

    // these three act on whole groups and folders rather than on memberships, so a caller whose
    // readwrite scope does not reach a group or a folder has nothing to point them at
    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("folder_delete",
        GrouperToolCategory.readwrite) {
      public boolean availableFor(GrouperMcpAuthUser authUser) {
        return authUser.hasGroupOrFolderReadwriteScope();
      }
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpFolderDelete.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpFolderDelete.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("group_delete",
        GrouperToolCategory.readwrite) {
      public boolean availableFor(GrouperMcpAuthUser authUser) {
        return authUser.hasGroupOrFolderReadwriteScope();
      }
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpGroupDelete.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpGroupDelete.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("group_save",
        GrouperToolCategory.readwrite) {
      public boolean availableFor(GrouperMcpAuthUser authUser) {
        return authUser.hasGroupOrFolderReadwriteScope();
      }
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpGroupSave.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpGroupSave.execute(arguments, authUser);
      }
    });
  }

  /**
   * tools which read the database directly.  none of them are worth offering unless an
   * administrator has made at least one database available
   */
  private static void registerSqlTools() {

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("sql_get_schema",
        GrouperToolCategory.sql) {
      public boolean availableFor(GrouperMcpAuthUser authUser) {
        return GrouperMcpSqlSelect.anyConfigured();
      }
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpSqlGetSchema.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpSqlGetSchema.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("sql_select",
        GrouperToolCategory.sql) {
      public boolean availableFor(GrouperMcpAuthUser authUser) {
        return GrouperMcpSqlSelect.anyConfigured();
      }
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpSqlSelect.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpSqlSelect.execute(arguments, authUser);
      }
    });

    // kept working for clients which still call it by this name, but not offered in a tool list
    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("sql_select_count",
        GrouperToolCategory.sql, false) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpSqlSelect.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        if (arguments != null && arguments.isObject()) {
          ((ObjectNode) arguments).put("countOnly", true);
        }
        return GrouperMcpSqlSelect.execute(arguments, authUser);
      }
    });
  }

  /**
   * tools which read configuration and daemons, and the one which runs a daemon job
   */
  private static void registerAdminTools() {

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("admin_external_system_get",
        GrouperToolCategory.admin_readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminExternalSystemGet.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("admin_daemon_job_message",
        GrouperToolCategory.admin_readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminGetDaemonJobMessage.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminGetDaemonJobMessage.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("admin_daemon_logs",
        GrouperToolCategory.admin_readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminGetDaemonJobs.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminGetDaemonJobs.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("admin_config_search",
        GrouperToolCategory.admin_readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminSearchConfigs.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminSearchConfigs.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("admin_daemon_names",
        GrouperToolCategory.admin_readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminSearchDaemons.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminSearchDaemons.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("ldap",
        GrouperToolCategory.admin_readonly) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpLdapSearch.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpLdapSearch.execute(arguments, authUser);
      }
    });

    GrouperToolRegistry.register(new GrouperToolLegacyAdapter("admin_daemon_job_run",
        GrouperToolCategory.admin_readwrite) {
      public ObjectNode toolDefinition(GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminRunDaemonJob.toolDefinition();
      }
      public ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {
        return GrouperMcpAdminRunDaemonJob.execute(arguments, authUser);
      }
    });
  }

}
