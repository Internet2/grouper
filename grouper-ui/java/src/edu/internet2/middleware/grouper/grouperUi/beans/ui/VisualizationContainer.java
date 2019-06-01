/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectSubjectWrapper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class VisualizationContainer {

  private String operation;
  private String drawModule;
  private String drawObjectNameType;
  private String objectType;
  private String objectId;
  private long drawNumParentsLevels;
  private long drawNumChildrenLevels;
  private long drawMaxSiblings;
  private boolean drawShowStems;
  private boolean drawShowLoaders;
  private boolean drawShowProvisioners;
  private boolean drawShowAllMemberCounts;
  private boolean drawShowDirectMemberCounts;
  private boolean drawShowObjectTypes;
  private boolean drawIncludeGroupsInMemberCounts;
  private boolean drawShowLegend;
  private GrouperObject grouperObject;


  public VisualizationContainer() {
  }

  /**
   * the current uiV2 operation (goes into the URL query params)
   * @return the UiV2 target operation
   */
  public String getOperation() {
    return operation;
  }

  /**
   * Set the UiV2 operation
   *
   * @param operation
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * which module to use to draw the graph (e.g. text or D3)
   *
   * @return
   */
  public String getDrawModule() {
    return drawModule;
  }

  /**
   * sets the module to use for drawing the graph (e.g. text or D3)
   *
   * @param drawModule
   * @param defaultValue
   */
  public void setDrawModule(String drawModule, String defaultValue) {
    this.drawModule = (!GrouperUtil.isBlank(drawModule)) ? drawModule : defaultValue;
  }

  /**
   * whether to use the display extension or object path in object names
   *
   * @return
   */
  public String getDrawObjectNameType() {
    return drawObjectNameType;
  }

  /**
   * whether to use the display extension or object path in object names
   *
   * @param drawObjectNameType
   */
  public void setDrawObjectNameType(String drawObjectNameType, String defaultValue) {
    this.drawObjectNameType = (!GrouperUtil.isBlank(drawObjectNameType)) ? drawObjectNameType : defaultValue;
  }

  /**
   * the lowercase class name  of the {@link GrouperObject} starting node object
   *
   * @return object class, e.g. "group", "stem", or "subject"
   */
  public String getObjectType() {
    return objectType;
  }

  /**
   * Sets the lowercase class name  of the {@link GrouperObject} starting node object
   *
   * @param objectType object class, e.g. "group", "stem", or "subject"
   */
  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  /**
   * The object id of the starting node
   *
   * @return uuid of starting node
   */
  public String getObjectId() {
    return objectId;
  }

  /**
   * set the object id of the starting node
   *
   * @param objectId uuid of the starting node
   */
  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  /**
   * the max number of parent levels to include (-1 indicates all levels)
   *
   * @return number of parent levels to draw
   */
  public long getDrawNumParentsLevels() {
    return drawNumParentsLevels;
  }

  /**
   * Sets the max number of parent levels to include. A value of -1 indicates all levels
   *
   * @param drawNumParentsLevels number of parent levels to draw
   */
  public void setDrawNumParentsLevels(long drawNumParentsLevels) {
    this.drawNumParentsLevels = drawNumParentsLevels;
  }

  /**
   * the max number of child levels to include (-1 indicates all levels)
   *
   * @return number of child levels to draw
   */
  public long getDrawNumChildrenLevels() {
    return drawNumChildrenLevels;
  }

  /**
   * Sets the max number of child levels to include. A value of -1 indicates all levels
   *
   * @param drawNumChildrenLevels number of child levels to draw
   */
  public void setDrawNumChildrenLevels(long drawNumChildrenLevels) {
    this.drawNumChildrenLevels = drawNumChildrenLevels;
  }

  /**
   * The max number of sibling objects to include. A value of zero or less
   * indicates all levels
   *
   * @return max number of sibling objects to draw
   */
  public long getDrawMaxSiblings() {
    return drawMaxSiblings;
  }

  /**
   * Sets the max number of sibling objects to include. A value of zero or less
   * indicates all levels
   *
   * @param drawMaxSiblings maximum number of sibling objects to draw
   */
  public void setDrawMaxSiblings(long drawMaxSiblings) {
    this.drawMaxSiblings = drawMaxSiblings;
  }

  /**
   * true if graph should include parent and child stems
   *
   * @return true if should include stems
   */
  public boolean isDrawShowStems() {
    return drawShowStems;
  }

  /**
   * true if graph should include parent and child stems
   *
   * @param drawShowStems true if should include stems
   */
  public void setDrawShowStems(boolean drawShowStems) {
    this.drawShowStems = drawShowStems;
  }

  /**
   * true if graph should include loader jobs
   *
   * @return true if should include loader jobs
   */
  public boolean isDrawShowLoaders() {
    return drawShowLoaders;
  }

  /**
   * true if graph should include loader jobs
   *
   * @param drawShowLoaders true if should include loader jobs
   */
  public void setDrawShowLoaders(boolean drawShowLoaders) {
    this.drawShowLoaders = drawShowLoaders;
  }

  /**
   * true if graph should include provisioner objects
   *
   * @return true if should include provisioner objects
   */
  public boolean isDrawShowProvisioners() {
    return drawShowProvisioners;
  }

  /**
   * true if graph should include provisioner objects
   *
   * @param drawShowProvisioners true if should include provisioner objects
   */
  public void setDrawShowProvisioners(boolean drawShowProvisioners) {
    this.drawShowProvisioners = drawShowProvisioners;

  }

  /**
   * true if graph should include member counts for groups
   *
   * @return true if should include member counts for groups
   */
  public boolean isDrawShowAllMemberCounts() {
    return drawShowAllMemberCounts;
  }

  /**
   * true if graph should include total member counts for groups
   *
   * @param drawShowAllMemberCounts true if should include total member counts for groups
   */
  public void setDrawShowAllMemberCounts(boolean drawShowAllMemberCounts) {
    this.drawShowAllMemberCounts = drawShowAllMemberCounts;
  }

  /**
   * true if graph should include direct member counts for groups
   *
   * @return true if graph should include direct member counts
   */
  public boolean isDrawShowDirectMemberCounts() {
    return drawShowDirectMemberCounts;
  }

  /**
   * true if graph should include direct member counts for groups
   *
   * @param drawShowDirectMemberCounts true if should include direct member counts
   */
  public void setDrawShowDirectMemberCounts(boolean drawShowDirectMemberCounts) {
    this.drawShowDirectMemberCounts = drawShowDirectMemberCounts;
  }

  /**
   * true if graph should include Grouper object types
   *
   * @return
   */
  public boolean isDrawShowObjectTypes() {
    return drawShowObjectTypes;
  }

  /**
   * true if graph should include Grouper object types
   *
   * @param drawShowObjectTypes
   */
  public void setDrawShowObjectTypes(boolean drawShowObjectTypes) {
    this.drawShowObjectTypes = drawShowObjectTypes;
  }

  /**
   * returns whether to include groups in the count of group members
   *
   * @return if groups are considered in the count of group members
   */
  public boolean isDrawIncludeGroupsInMemberCounts() {
    return drawIncludeGroupsInMemberCounts;
  }

  /**
   * flags whether to include groups in the count of group members
   *
   * @param drawIncludeGroupsInMemberCounts whether to consider groups when counting members
   * @return
   */
  public void setDrawIncludeGroupsInMemberCounts(boolean drawIncludeGroupsInMemberCounts) {
    this.drawIncludeGroupsInMemberCounts = drawIncludeGroupsInMemberCounts;
  }

  /**
   * returns whether the D3 graph should include a legend as an inset
   *
   * @return
   */
  public boolean isDrawShowLegend() {
    return drawShowLegend;
  }

  /**
   * sets whether the D3 graph should include a legend as an inset
   *
   * @param drawShowLegend
   */
  public void setDrawShowLegend(boolean drawShowLegend) {
    this.drawShowLegend = drawShowLegend;
  }

  /**
   * Get the {@link GrouperObject} starting node object based on its type and id
   * @return the {@link Stem}, {@Link Group}, or {@link GrouperObjectSubjectWrapper} object
   * of the starting node
   */
  public GrouperObject getGrouperObject() {

    if (grouperObject == null) {
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();

      if ("group".equals(getObjectType())) {
        grouperObject = GroupFinder.findByUuid(grouperSession, getObjectId(), true);
      } else if ("stem".equals(getObjectType())) {
        grouperObject = StemFinder.findByUuid(grouperSession, getObjectId(), true);
      } else if ("subject".equals(getObjectType())) {
        // More actions->Visualization sets object id as the subject Id, but
        // GrouperObjectSubjectWrapper returns sourceId||||subjectId. Do the
        // appropriate search either way
        String[] subjectParts = GrouperUtil.split(getObjectId(), "||||");
        Subject subject = null;
        if (subjectParts.length >= 2) {
          subject = SubjectFinder.findByIdAndSource(subjectParts[1], subjectParts[0], true);
        } else {
          subject = SubjectFinder.findById(getObjectId(), true);
        }
        grouperObject = new GrouperObjectSubjectWrapper(subject);
      } else {
        throw new RuntimeException("Unknown object type '" + getObjectType() + "'");
      }
    }

    return grouperObject;
  }
}
