/**
 * Copyright 2018 Internet2
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.graph.GraphEdge;
import edu.internet2.middleware.grouper.app.graph.GraphNode;
import edu.internet2.middleware.grouper.app.graph.RelationGraph;
import edu.internet2.middleware.grouper.app.visualization.StyleObjectType;
import edu.internet2.middleware.grouper.app.visualization.VisualSettings;
import edu.internet2.middleware.grouper.app.visualization.VisualStyleSet;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.preferences.UiV2VisualizationPreference;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.*;
import edu.internet2.middleware.grouper.misc.GrouperObjectSubjectWrapper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Operations to display a graph of object relationships
 */
public class UiV2Visualization {

  /**
   * Internal class used for json-lib serialization; needs to be public for
   * introspection to work
   */
  public class VisualizationGraph {
    private Map<String, Object> settings;
    private Map<String, String> statistics;
    private Map<String, Map<String, String>> styles;

    public VisualizationGraph() {
      settings = new HashMap<String, Object>();
      statistics = new HashMap<String, String>();
      styles = new HashMap<String, Map<String, String>>();
    }

    protected void addSetting(String name, Object value) {
      this.settings.put(name, value);
    }

    protected void addStatistic(String name, String value) {
      this.statistics.put(name, value);
    }

    protected void addStyleProperty(String styleName, String propertyName, String propertyValue) {
      if (!styles.containsKey(styleName)) {
        styles.put(styleName, new HashMap<String, String>());
      }
      styles.get(styleName).put(propertyName, propertyValue);
    }

    public Map<String, Object> getSettings() {
      return settings;
    }

    public Map<String, String> getStatistics() {
      return statistics;
    }

    public Map<String, Map<String, String>> getStyles() {
      return styles;
    }
  }

  public class TextGraph extends VisualizationGraph { //note need to be public for json-lib to parse them
    private List<TextNode> nodes;

    private TextGraph() {
      super();
      nodes = new LinkedList<TextNode>();
    }

    private void addNode(TextNode node) {
      this.nodes.add(node);
    }

    public List<TextNode> getNodes() {
      return nodes;
    }
  }

  /**
   * Internal class used for json-lib serialization; needs to be public for
   * introspection to work
   */
  public class D3Graph extends VisualizationGraph {
    private Map<String, Node> nodes;
    private List<D3Link> links;

    private D3Graph() {
      super();
      nodes = new HashMap<String, Node>();
      links = new LinkedList<D3Link>();
    }

    private void addNode(String id, Node node) {
      this.nodes.put(id, node);
    }

    private void addLink(D3Link link) {
      this.links.add(link);
    }

    public Map<String, Node> getNodes() {
      return nodes;
    }

    public List<D3Link> getLinks() {
      return links;
    }

  }

  /**
   * Internal class used for json-lib serialization; needs to be public for
   * introspection to work
   */
  public class Node {
    private String id;
    private String name;
    private String displayExtension;
    private String description;
    private String type;
    private String baseType;
    private long memberCount = 0;

    private Node(String id, String name, String displayExtension, String description, String type,
                 String baseType, long memberCount) {
      this.id = id;
      this.name = name;
      this.displayExtension = displayExtension;
      this.description = description;
      this.type = type;
      this.baseType = baseType;
      this.memberCount = memberCount;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getDisplayExtension() {
      return displayExtension;
    }

    public String getDescription() {
      return description;
    }

    public String getType() {
      return type;
    }

    public String getBaseType() {
      return baseType;
    }

    public long getMemberCount() {
      return memberCount;
    }
  }

  /**
   * Internal class used for json-lib serialization; needs to be public for
   * introspection to work
   */
  public class TextNode extends Node {
    private long indent;

    private TextNode(String id, String name, String displayExtension, String description, String type,
                     String linkType, long memberCount, long indent) {
      super(id, name, displayExtension, description, type, linkType, memberCount);
      this.indent = indent;
    }

    public long getIndent() {
      return indent;
    }
  }

  /**
   * Internal class used for json-lib serialization; needs to be public for
   * introspection to work
   */
  public class D3Link {
    private String source;
    private String target;
    private String type;

    public D3Link(String source, String target, String type) {
      this.source = source;
      this.target = target;
      this.type = type;
    }

    public String getSource() {
      return source;
    }

    private void setSource(String source) {
      this.source = source;
    }

    public String getTarget() {
      return target;
    }

    private void setTarget(String target) {
      this.target = target;
    }

    public String getType() {
      return type;
    }

    private void setType(String type) {
      this.type = type;
    }
  }

  /* **** End of json-lib serialization classes **** */

  /**
   * Show the visualization form and graph, starting with a {@link Group} object
   *
   * @param request
   * @param response
   */
  public void groupView(HttpServletRequest request, HttpServletResponse response) {

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    VisualizationContainer visualizationContainer = grouperRequestContainer.getVisualizationContainer();

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GrouperSession grouperSession = GrouperSession.start(loggedInSubject);

    visualizationHelper(loggedInSubject, request, visualizationContainer, false);

    Group group = (Group) visualizationContainer.getGrouperObject();
    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
    groupContainer.setGuiGroup(new GuiGroup(group));

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
      "/WEB-INF/grouperUi2/group/groupVisualization.jsp"));
  }

  /**
   * Show the visualization form and graph, starting with a {@link Stem} object
   *
   * @param request
   * @param response
   */
  public void stemView(HttpServletRequest request, HttpServletResponse response) {

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    VisualizationContainer visualizationContainer = grouperRequestContainer.getVisualizationContainer();

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GrouperSession grouperSession = GrouperSession.start(loggedInSubject);

    visualizationHelper(loggedInSubject, request, visualizationContainer, false);

    Stem stem = (Stem) visualizationContainer.getGrouperObject();
    StemContainer stemContainer = grouperRequestContainer.getStemContainer();
    stemContainer.setGuiStem(new GuiStem(stem));

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
      "/WEB-INF/grouperUi2/stem/stemVisualization.jsp"));

  }

  /**
   * Show the visualization form and graph, starting with a {@link GrouperObjectSubjectWrapper} object
   *
   * @param request
   * @param response
   */
  public void subjectView(HttpServletRequest request, HttpServletResponse response) {

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    VisualizationContainer visualizationContainer = grouperRequestContainer.getVisualizationContainer();

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GrouperSession grouperSession = GrouperSession.start(loggedInSubject);

    visualizationHelper(loggedInSubject, request, visualizationContainer, false);

    GrouperObjectSubjectWrapper subjectWrapped = (GrouperObjectSubjectWrapper) visualizationContainer.getGrouperObject();
    Subject subject = subjectWrapped.getSubject();
    SubjectContainer subjectContainer = grouperRequestContainer.getSubjectContainer();
    subjectContainer.setGuiSubject(new GuiSubject(subject));

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
      "/WEB-INF/grouperUi2/subject/subjectVisualization.jsp"));
  }

  /**
   * Internal ajax call to get the graph based on settings. Puts json into the visualizationObject DOM element
   *
   * @param request
   * @param response
   */
  public void buildGraph(HttpServletRequest request, HttpServletResponse response) {
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    VisualizationContainer visualizationContainer = grouperRequestContainer.getVisualizationContainer();

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GrouperSession grouperSession = GrouperSession.start(loggedInSubject);

    visualizationHelper(loggedInSubject, request, visualizationContainer, true);

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    RelationGraph relationGraph = new RelationGraph(grouperSession)
      .assignStartObject(visualizationContainer.getGrouperObject())
      .assignParentLevels(visualizationContainer.getDrawNumParentsLevels())
      .assignChildLevels(visualizationContainer.getDrawNumChildrenLevels())
      .assignShowStems(visualizationContainer.isDrawShowStems())
      .assignShowLoaderJobs(visualizationContainer.isDrawShowLoaders())
      .assignShowProvisionTargets(visualizationContainer.isDrawShowProvisioners())
      .assignShowMemberCounts(visualizationContainer.isDrawShowMemberCounts())
      .assignMaxSiblings(visualizationContainer.getDrawMaxSiblings());

    relationGraph.build();

    VisualizationGraph graph = null;
    String jsDrawFunctionName = null;

    if ("text".equals(visualizationContainer.getDrawModule())) {
      graph = buildToJsonText(relationGraph);
      jsDrawFunctionName = "drawGraphModuleText()";
    } else if ("d3".equals(visualizationContainer.getDrawModule())) {
      graph = buildToJsonD3(relationGraph);
      jsDrawFunctionName = "drawGraphModuleD3()";
    } else {
      throw new RuntimeException("Invalid visualization module: '" + visualizationContainer.getDrawModule() + "'");
    }

    graph.addStatistic("numEdges", String.valueOf(relationGraph.getEdges().size()));
    graph.addStatistic("numMemberships", relationGraph.isShowMemberCounts() ? String.valueOf(relationGraph.getNumMembers()) : "(not included)");
    graph.addStatistic("numNodes", String.valueOf(relationGraph.getNodes().size()));
    graph.addStatistic("numLoaderJobs", String.valueOf(relationGraph.getNumLoaders()));
    graph.addStatistic("numGroupsFromLoaders", String.valueOf(relationGraph.getNumGroupsFromLoaders()));
    graph.addStatistic("numProvisioners", String.valueOf(relationGraph.getNumProvisioners()));
    graph.addStatistic("numGroupsToProvisioners", String.valueOf(relationGraph.getNumGroupsToProvisioners()));
    graph.addStatistic("numSkippedFolders", String.valueOf(relationGraph.getNumSkippedFolders()));
    graph.addStatistic("numSkippedGroups", String.valueOf(relationGraph.getNumSkippedGroups()));

    graph.addSetting("startNode", relationGraph.getStartNode().getGrouperObject().getId());
    graph.addSetting("showMemberCounts", relationGraph.isShowMemberCounts());
    graph.addSetting("showMemberCounts", relationGraph.isShowMemberCounts());
    //graph.addSetting("objectNameField", visualizationHelper;);
    //what is this in the d3 drawing?? graph.addSetting("text", -1);

    JSONObject jsonObject = JSONObject.fromObject(graph);

    guiResponseJs.addAction(GuiScreenAction.newAssign("visualizationObject", jsonObject.toString()));
    guiResponseJs.addAction(GuiScreenAction.newScript(jsDrawFunctionName));
  }

  private String getGrouperObjectDisplayExtension(GraphNode graphNode) {
    String displayExtension = graphNode.getGrouperObjectName();
    if (graphNode.isGroup()) {
      displayExtension = ((Group)graphNode.getGrouperObject()).getDisplayExtension();
    } else if (graphNode.isStem()) {
      displayExtension = ((Stem)graphNode.getGrouperObject()).getDisplayExtension();
    }

    return displayExtension;
  }

  private D3Graph buildToJsonD3(RelationGraph relationGraph) {
    VisualSettings settings = new VisualSettings();
    VisualStyleSet styleSet = settings.getStyleSet("dot");

    D3Graph graph = new D3Graph();

    Set<StyleObjectType> styleTypes = new HashSet();

    for (GraphNode graphNode: relationGraph.getNodes()) {
      Node node = new Node(
        graphNode.getGrouperObjectId(),
        graphNode.getGrouperObjectName(),
        getGrouperObjectDisplayExtension(graphNode),
        graphNode.getGrouperObject().getDescription(),
        graphNode.getStyleObjectType().getName(),
        getNodeBaseType(graphNode),
        graphNode.getMemberCount()
      );

      graph.addNode(graphNode.getGrouperObjectId(), node);
      styleTypes.add(graphNode.getStyleObjectType());
    }

    for (GraphEdge graphEdge: relationGraph.getEdges()) {
      D3Link link = new D3Link(
        graphEdge.getFromNode().getGrouperObject().getId(),
        graphEdge.getToNode().getGrouperObject().getId(),
        graphEdge.getStyleObjectType().getName()
      );

      graph.addLink(link);
      styleTypes.add(graphEdge.getStyleObjectType());
    }

    // In addition to edges and nodes, the graph itself has a style
    styleTypes.add(StyleObjectType.GRAPH);

    for (StyleObjectType styleType: styleTypes) {
      for (String propertyName: new String[]{"shape", "style", "color", "fontcolor", "border", "arrowtail", "dir"}) {
        String propertyValue = styleSet.getStyleProperty(styleType.getName(), propertyName, "");
        if (!"".equals(propertyValue)) {
          graph.addStyleProperty(styleType.getName(), propertyName, propertyValue);
        }
      }

      //Style s = new Style();
      //for (Map.Entry<String, String> property : s.getProperties().entrySet()) {
        //s.addProperty(property.getKey(), property.getValue());
      //}
      //graph.addStyle(styleType.getName(), s);
    }

    return graph;
  }

  private  TextGraph buildToJsonText(RelationGraph relationGraph) {
    VisualSettings settings = new VisualSettings();
    VisualStyleSet styleSet = settings.getStyleSet("text");

    TextGraph graph = new TextGraph();

    //maintain the set of object types in use
    Set<StyleObjectType> styleTypes = new HashSet();

    List<GraphNode> sortedNodes = new ArrayList<GraphNode>(relationGraph.getNodes());
    Collections.sort(sortedNodes, new SortByIndent());

    for (GraphNode graphNode: sortedNodes) {
      long indent = graphNode.getDistanceFromStartNode() + relationGraph.getMaxParentDistance();
      if (indent < 0) {
        indent = 0;
      }

      TextNode node = new TextNode(
        graphNode.getGrouperObjectId(),
        graphNode.getGrouperObjectName(),
        getGrouperObjectDisplayExtension(graphNode),
        graphNode.getGrouperObject().getDescription(),
        graphNode.getStyleObjectType().getName(),
        getNodeBaseType(graphNode),
        graphNode.getMemberCount(),
        indent);

      graph.addNode(node);
      styleTypes.add(graphNode.getStyleObjectType());
    }

    for (StyleObjectType styleType: styleTypes) {
      graph.addStyleProperty(
        styleType.getName(),
        "label",
        styleSet.getStyleProperty(styleType, "label", ""));
    }

    return graph;
  }


  private String getNodeBaseType(GraphNode node) {
    if (node.isStem()) {
      return "stem";
    } else if (node.isGroup()) {
      return "group";
    } else if (node.isSubject()) {
      return "subject";
    } else if (node.isProvisionerTarget()) {
      return "provisioner";
    } else {
      return null;
    }
  }

  /**
   * Custom sort for the text builder, sorts by nodes indentation level
   */
  private class SortByIndent implements Comparator<GraphNode> {
    public int compare(GraphNode a, GraphNode b)
    {
      return (int) (a.getDistanceFromStartNode() - b.getDistanceFromStartNode());
    }
  }


  private void visualizationHelper(Subject loggedInSubject, HttpServletRequest request, VisualizationContainer visualizationContainer,
                                   boolean fromSubmission) {
    //todo filters etc

    if (request.getParameter("groupId")!=null) {
      visualizationContainer.setObjectId(request.getParameter("groupId"));
      visualizationContainer.setObjectType("group");
    } else if (request.getParameter("stemId")!=null) {
      visualizationContainer.setObjectId(request.getParameter("stemId"));
      visualizationContainer.setObjectType("stem");
    } else if (request.getParameter("subjectId")!=null) {
      visualizationContainer.setObjectId(request.getParameter("subjectId"));
      visualizationContainer.setObjectType("subject");
    } else if (request.getParameter("objectId")!=null) {
      visualizationContainer.setObjectId(request.getParameter("objectId"));
      visualizationContainer.setObjectType(request.getParameter("objectType"));
    }

    if ("group".equals(visualizationContainer.getObjectType())) {
      visualizationContainer.setOperation("UiV2Visualization.groupView");
    } else if ("stem".equals(visualizationContainer.getObjectType())) {
      visualizationContainer.setOperation("UiV2Visualization.stemView");
    } else if ("subject".equals(visualizationContainer.getObjectType())) {
      visualizationContainer.setOperation("UiV2Visualization.subjectView");
    }

    // todo this is mixing keyname and text errors
    if (StringUtils.isBlank(visualizationContainer.getObjectId())
      || StringUtils.isBlank(visualizationContainer.getObjectType())) {
      throw new RuntimeException("error.subject-summary.missing-parameter");
    }

    /* For params below, persist differences between the form and preferences */
    boolean prefsHaveChanged = false;

    UiV2VisualizationPreference prefsFromAttribute = GrouperUserDataApi.visualizationPrefs(
        GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject, UiV2VisualizationPreference.class);
    if (prefsFromAttribute == null) {
      prefsFromAttribute = new UiV2VisualizationPreference();
    }

    /* draw module */
    visualizationContainer.setDrawModule(request.getParameter("drawModule"), prefsFromAttribute.getDrawModule());
    if (!visualizationContainer.getDrawModule().equals(prefsFromAttribute.getDrawModule())) {
      prefsFromAttribute.setDrawModule(visualizationContainer.getDrawModule());
      prefsHaveChanged = true;
    }

    /* parent levels */
    if (request.getParameter("drawNumParentsLevels") == null && request.getParameter("drawNumParentsAll") == null) {
      // new form, not from submission, so use the prefs values
      visualizationContainer.setDrawNumParentsLevels(prefsFromAttribute.getDrawNumParentsLevels());
    } else {
      long drawNumParentLevels = GrouperUtil.longValue(request.getParameter("drawNumParentsLevels"), -1);
      if (GrouperUtil.booleanValue(request.getParameter("drawNumParentsAll"), false)) {
        drawNumParentLevels = -1;
      }
      visualizationContainer.setDrawNumParentsLevels(drawNumParentLevels);
      if (drawNumParentLevels != prefsFromAttribute.getDrawNumParentsLevels()) {
        prefsFromAttribute.setDrawNumParentsLevels(drawNumParentLevels);
        prefsHaveChanged = true;
      }
    }


    /* child levels */
    if (request.getParameter("drawNumChildrenLevels") == null && request.getParameter("drawNumChildrenAll") == null) {
      // new form, not from submission, so use the prefs values
      visualizationContainer.setDrawNumChildrenLevels(prefsFromAttribute.getDrawNumChildrenLevels());
    } else {
      long drawNumChildLevels = GrouperUtil.longValue(request.getParameter("drawNumChildrenLevels"), -1);
      if (GrouperUtil.booleanValue(request.getParameter("drawNumChildrenAll"), false)) {
        drawNumChildLevels = -1;
      }
      visualizationContainer.setDrawNumChildrenLevels(drawNumChildLevels);
      if (drawNumChildLevels != prefsFromAttribute.getDrawNumChildrenLevels()) {
        prefsFromAttribute.setDrawNumChildrenLevels(drawNumChildLevels);
        prefsHaveChanged = true;
      }
    }

    /* max siblings */
    if (request.getParameter("drawMaxSiblings") == null && request.getParameter("drawMaxSiblingsAll") == null) {
      // new form, not from submission, so use the prefs values
      visualizationContainer.setDrawMaxSiblings(prefsFromAttribute.getDrawMaxSiblings());
    } else {
      long drawMaxSiblings = GrouperUtil.longValue(request.getParameter("drawMaxSiblings"), -1);
      if (GrouperUtil.booleanValue(request.getParameter("drawMaxSiblingsAll"), false)) {
        drawMaxSiblings = -1;
      }
      visualizationContainer.setDrawMaxSiblings(drawMaxSiblings);
      if (drawMaxSiblings != prefsFromAttribute.getDrawMaxSiblings()) {
        prefsFromAttribute.setDrawMaxSiblings(drawMaxSiblings);
        prefsHaveChanged = true;
      }
    }

    /* name type */
    visualizationContainer.setDrawObjectNameType(request.getParameter("drawObjectNameType"), prefsFromAttribute.getDrawObjectNameType());
    if (!GrouperUtil.equals(visualizationContainer.getDrawObjectNameType(), prefsFromAttribute.getDrawObjectNameType())) {
      prefsFromAttribute.setDrawObjectNameType(visualizationContainer.getDrawObjectNameType());
      prefsHaveChanged = true;
    }

    /* For checkboxes, hard to tell whether it's a new form or a box set to unchecked.
     * Use the fromSubmission flag to tell the difference
     */

    /* show stems */
    if (fromSubmission) {
      visualizationContainer.setDrawShowStems(
        GrouperUtil.booleanValue(request.getParameter("drawShowStems"), false));
      if (visualizationContainer.isDrawShowStems() != prefsFromAttribute.isDrawShowStems()) {
        prefsFromAttribute.setDrawShowStems(visualizationContainer.isDrawShowStems());
        prefsHaveChanged = true;
      }
    } else {
      visualizationContainer.setDrawShowStems(prefsFromAttribute.isDrawShowStems());
    }

    /* show loaders */
    if (fromSubmission) {
      visualizationContainer.setDrawShowLoaders(
        GrouperUtil.booleanValue(request.getParameter("drawShowLoaders"), false));
      if (visualizationContainer.isDrawShowLoaders() != prefsFromAttribute.isDrawShowLoaders()) {
        prefsFromAttribute.setDrawShowLoaders(visualizationContainer.isDrawShowLoaders());
        prefsHaveChanged = true;
      }
    } else {
      visualizationContainer.setDrawShowLoaders(prefsFromAttribute.isDrawShowLoaders());
    }

  /* show provisioners */
  if (fromSubmission) {
    visualizationContainer.setDrawShowProvisioners(
      GrouperUtil.booleanValue(request.getParameter("drawShowProvisioners"), false));
    if (visualizationContainer.isDrawShowProvisioners() != prefsFromAttribute.isDrawShowProvisioners()) {
      prefsFromAttribute.setDrawShowProvisioners(visualizationContainer.isDrawShowProvisioners());
      prefsHaveChanged = true;
    }
  } else {
    visualizationContainer.setDrawShowProvisioners(prefsFromAttribute.isDrawShowProvisioners());
  }

    /* show member counts */
    if (fromSubmission) {
      visualizationContainer.setDrawShowMemberCounts(
        GrouperUtil.booleanValue(request.getParameter("drawShowMemberCounts"), false));
      if (visualizationContainer.isDrawShowMemberCounts() != prefsFromAttribute.isDrawShowMemberCounts()) {
        prefsFromAttribute.setDrawShowMemberCounts(visualizationContainer.isDrawShowMemberCounts());
        prefsHaveChanged = true;
      }
    } else {
      visualizationContainer.setDrawShowMemberCounts(prefsFromAttribute.isDrawShowMemberCounts());
    }

    // force a find of the lazy-loaded based on the id and type
    //visualizationContainer.getGrouperObject();

    if (prefsHaveChanged) {
      GrouperUserDataApi.visualizationPrefsAssign(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject, prefsFromAttribute);
    }
  }



}


