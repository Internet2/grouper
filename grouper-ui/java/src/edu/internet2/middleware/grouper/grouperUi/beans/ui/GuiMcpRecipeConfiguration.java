package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.mcp.GrouperMcpRecipeConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Gui wrapper for one MCP recipe configuration, so the recipes table can show the fields
 * without the JSP reaching into the config module for each one.
 */
public class GuiMcpRecipeConfiguration {

  /** how much of the summary the table shows before it is cut short */
  private static final int SUMMARY_MAX_CHARS = 120;

  /** the underlying configuration */
  private GrouperMcpRecipeConfiguration grouperMcpRecipeConfiguration;

  /**
   * the underlying configuration
   * @return the configuration
   */
  public GrouperMcpRecipeConfiguration getGrouperMcpRecipeConfiguration() {
    return this.grouperMcpRecipeConfiguration;
  }

  /**
   * short name the AI client refers to this recipe by
   * @return the name
   */
  public String getName() {
    return this.grouperMcpRecipeConfiguration.retrieveAttributeValueFromConfig("name", false);
  }

  /**
   * the full summary
   * @return the summary
   */
  public String getSummary() {
    return this.grouperMcpRecipeConfiguration.retrieveAttributeValueFromConfig("summary", false);
  }

  /**
   * the summary cut short for the table, with the full text offered in a tooltip when it is cut
   * @return the abbreviated summary
   */
  public String getSummaryAbbreviated() {
    return StringUtils.abbreviate(this.getSummary(), SUMMARY_MAX_CHARS);
  }

  /**
   * whether the summary is long enough that the table shows only part of it.  the recipes screen
   * offers the full text in a tooltip only when this is true, so that a table of short summaries
   * does not add a keyboard tab stop to every row for text which is already fully on screen.
   * note this cannot be called isSummaryAbbreviated, since that is the same bean property as
   * getSummaryAbbreviated above
   * @return true if the summary is longer than the table shows
   */
  public boolean isSummaryTruncated() {
    String summary = this.getSummary();
    if (summary == null) {
      return false;
    }
    return summary.length() > SUMMARY_MAX_CHARS;
  }

  /**
   * group whose members can see and read this recipe
   * @return the group name
   */
  public String getGroupNameCanUse() {
    return this.grouperMcpRecipeConfiguration.retrieveAttributeValueFromConfig("groupNameCanUse", false);
  }

  /**
   * group whose members can edit this recipe over MCP
   * @return the group name
   */
  public String getGroupNameCanEdit() {
    return this.grouperMcpRecipeConfiguration.retrieveAttributeValueFromConfig("groupNameCanEdit", false);
  }

  /**
   * the "group who can use" as a gui group, so the table can show the short name with a link
   * the way group names are shown elsewhere in the UI
   * @return the gui group, or null when it cannot be linked
   */
  public GuiGroup getGuiGroupCanUse() {
    return guiGroupOrNull(this.getGroupNameCanUse());
  }

  /**
   * the "group who can edit" as a gui group
   * @return the gui group, or null when it cannot be linked
   */
  public GuiGroup getGuiGroupCanEdit() {
    return guiGroupOrNull(this.getGroupNameCanEdit());
  }

  /**
   * resolve a group name for display in the recipes table.
   *
   * <p>Deliberately the logged in session rather than a root session.  A recipe resolves its
   * groups as root, so a recipe can name a group the reader cannot see, and linking to one of
   * those would offer a link which errors when followed.  Returning null here leaves the screen
   * showing the plain name, which is what somebody without view on the group should get.</p>
   *
   * @param groupName the configured group name, may be blank
   * @return the gui group, or null when blank, missing, or not viewable by this user
   */
  private static GuiGroup guiGroupOrNull(String groupName) {

    if (StringUtils.isBlank(groupName)) {
      return null;
    }

    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);

    if (grouperSession == null) {
      return null;
    }

    Group group = GroupFinder.findByName(grouperSession, groupName, false);

    if (group == null) {
      return null;
    }

    return new GuiGroup(group);
  }

  /**
   * MCP tools this recipe attaches a pointer to
   * @return the comma separated tool names
   */
  public String getToolNames() {
    return this.grouperMcpRecipeConfiguration.retrieveAttributeValueFromConfig("toolNames", false);
  }

  /**
   * if this recipe is in use.  defaults to true, matching the config default, so a recipe which
   * has never had the box touched does not read as disabled in the table
   * @return true if enabled
   */
  public boolean isEnabled() {
    String enabled = this.grouperMcpRecipeConfiguration.retrieveAttributeValueFromConfig("enabled", false);
    return GrouperUtil.booleanValue(enabled, true);
  }

  /**
   * who last changed this recipe through Grouper and when, for the recipes table
   * @return the attribution, or blank when it has only ever been edited outside Grouper
   */
  public String getLastEdited() {

    String lastEditedBy = this.grouperMcpRecipeConfiguration.retrieveAttributeValueFromConfig("lastEditedBy", false);

    if (StringUtils.isBlank(lastEditedBy)) {
      return "";
    }

    // the stored value is a packed subject string, sourceId::::subjectId.  the name is what
    // somebody reading the table recognises, so it leads, with the subject id behind it
    String lastEditedByName = this.grouperMcpRecipeConfiguration.retrieveAttributeValueFromConfig("lastEditedByName", false);
    String subjectId = GrouperUtil.prefixOrSuffix(lastEditedBy, "::::", false);

    String who = StringUtils.isBlank(lastEditedByName) ? subjectId : lastEditedByName + " (" + subjectId + ")";

    String lastEditedOn = this.grouperMcpRecipeConfiguration.retrieveAttributeValueFromConfig("lastEditedOn", false);

    return StringUtils.isBlank(lastEditedOn) ? who : who + " on " + lastEditedOn;
  }

  /**
   * @param grouperMcpRecipeConfiguration1 the configuration to wrap
   */
  private GuiMcpRecipeConfiguration(GrouperMcpRecipeConfiguration grouperMcpRecipeConfiguration1) {
    this.grouperMcpRecipeConfiguration = grouperMcpRecipeConfiguration1;
  }

  /**
   * @param mcpRecipeConfiguration the configuration to wrap
   * @return the gui wrapper
   */
  public static GuiMcpRecipeConfiguration convertFromMcpRecipeConfiguration(
      GrouperMcpRecipeConfiguration mcpRecipeConfiguration) {
    return new GuiMcpRecipeConfiguration(mcpRecipeConfiguration);
  }

  /**
   * @param mcpRecipeConfigurations the configurations to wrap
   * @return the gui wrappers
   */
  public static List<GuiMcpRecipeConfiguration> convertFromMcpRecipeConfiguration(
      List<GrouperMcpRecipeConfiguration> mcpRecipeConfigurations) {

    List<GuiMcpRecipeConfiguration> guiMcpRecipeConfigurations = new ArrayList<GuiMcpRecipeConfiguration>();

    for (GrouperMcpRecipeConfiguration mcpRecipeConfiguration : mcpRecipeConfigurations) {
      guiMcpRecipeConfigurations.add(convertFromMcpRecipeConfiguration(mcpRecipeConfiguration));
    }

    return guiMcpRecipeConfigurations;
  }

}
