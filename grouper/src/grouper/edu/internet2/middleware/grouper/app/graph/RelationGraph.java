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

package edu.internet2.middleware.grouper.app.graph;

import java.util.*;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.subj.SubjectHelper;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.visualization.StyleObjectType;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectSubjectWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

import org.hibernate.type.StringType;
import org.hibernate.type.Type;

/**
 * Class to build a directed graph from Grouper relationships. The graph is
 * initialized from a single starting node. From there it will branch to the
 * node's parents and children recursively until exhausted. Nodes can be of a
 * subset of GrouperObject types -- Group, Stem, Subject (as GrouperObjectSubjectWrapper).
 * A pseudo-object for provisioners is also implemented in this package so that PSPNG
 * provisioning targets can be represented as nodes. For stems, it will get the parents
 * and children of all its child groups.
 *
 * Each node contains an underlying GrouperObject type. Edges contain the directed relationship
 * from parent to child. The methods involved in building the graph are influenced by setup
 * parameters. For example, a build can include or exclude showing stems, or can filter certain
 * stems based on regular expressions. The build can also optionally count group memberships
 * and include the results as extra data within the nodes. As the build recursively follows parents
 * and children, it stored the distanec (number of hops) from each node to the start node.
 * After the build, The full set of nodes, edges, the starting node, and other information can be
 * retrieved from the graph object.
 *
 * There is a hard limit of 100 levels, as an emergency stop against unforeseen cycles that miss
 * detection.
 */
public class RelationGraph {

  private static final String KLASS = RelationGraph.class.getName();

  private static final Log LOG = GrouperUtil.getLog(RelationGraph.class);
  public static final int RECURSIVE_LEVEL_LIMIT = 100;

  // Class parameters for finding immediate g:gsa members and PSPNG provisioners
  private static Set<Source> grouperGSASources;
  private static Field grouperMemberField;
  private static AttributeDefName provisionToAttributeDefName;
  private static String loaderGroupIdAttrDefNameId;
  private static AttributeDefName sqlLoaderAttributeDefName; // used by graph nodes to determine if loader job
  private static Set<Source> nonGroupSourcesCache = null;
  private static String objectTypeAttributeId = null;
  private static String objectTypeAttributeValueId = null;
  private static boolean attemptedInitLookupFields = false;

  /* assignX settings for graph construction */
  private GrouperObject startObject;
  private long parentLevels = -1;
  private long childLevels = -1;
  private boolean showAllMemberCounts = true;
  private boolean showDirectMemberCounts = true;
  private boolean showObjectTypes = false;
  private boolean showLoaderJobs = true;
  private boolean showProvisionTargets = true;
  private boolean showStems = true;
  private boolean includeGroupsInMemberCounts = false;
  private Set<String> skipFolderNamePatterns = new HashSet<String>();  // Arrays.asList("^etc:.*", "^$") -> default to skip etc:* and root object
  private Set<String> skipGroupNamePatterns = new HashSet<String>();  // don't skip etc:* groups since they may be loader jobs
  private List<Pattern> skipFolderPatterns;
  private List<Pattern> skipGroupPatterns;
  private long maxSiblings = -1;

  private GraphNode startNode;
  private Map<GrouperObject, GraphNode> objectToNodeMap;
  private Set<GraphEdge> edges;
  private Set<Stem> skippedFolders;
  private Set<Group> skippedGroups;

  // convenience calculations that get set during or after building the graph
  private long numLoaders;
  private long numGroupsFromLoaders;
  private long numProvisioners;
  private long numGroupsToProvisioners;
  private long maxParentDistance;
  private long maxChildDistance;
  private long totalMemberCount;
  private long directMemberCount;
  private Set<GraphNode> leafParentNodes;
  private Set<GraphNode> leafChildNodes;

  /**
   * Create a new graph with default settings. Caller should call the various
   * assign methods to set build parameters, and then call build() to construct
   * the graph.
   */
  public RelationGraph() {
  }

  /**
   * sets the {@link GrouperObject} object to serve as the starting point of the graph
   *
   * @param theStartObject Group, Stem, or GrouperObjectSubjectWrapper object to start the tree from
   * @return
   */
  public RelationGraph assignStartObject(GrouperObject theStartObject) {
    if (theStartObject instanceof Group
             || theStartObject instanceof Stem
             || theStartObject instanceof GrouperObjectSubjectWrapper) {
      this.startObject = theStartObject;
      return this;
    }

    throw new RuntimeException("Only groups, stems, or subject wrapper objects can be used as the starting node for a graph");
  }

  /**
   * sets the start object from a subject, by converting to a {@link GrouperObjectSubjectWrapper}
   *
   * @param theStartSubject subject to start the tree from
   * @return
   */
  public RelationGraph assignStartObject(Subject theStartSubject) {
    return assignStartObject(new GrouperObjectSubjectWrapper(theStartSubject));
  }


  /**
   * sets the maximum number of parent levels to include in the graph
   *
   * @param theParentLevels number of parent steps to include, or -1 to include all levels
   * @return
   */
  public RelationGraph assignParentLevels(long theParentLevels) {
    this.parentLevels = theParentLevels;
    return this;
  }

  /**
   * sets the maximum number of child levels to include in the graph
   *
   * @param theChildLevels number of child steps to include, or -1 to include all levels
   * @return
   */
  public RelationGraph assignChildLevels(long theChildLevels) {
    this.childLevels = theChildLevels;
    return this;
  }

  /**
   * flags whether to count memberships (direct and indirect) for groups
   *
   * @param theShowAllMemberCounts whether to count memberships for groups
   * @return
   */
  public RelationGraph assignShowAllMemberCounts(boolean theShowAllMemberCounts) {
    this.showAllMemberCounts = theShowAllMemberCounts;
    return this;
  }

  /**
   * flags whether to count direct memberships for groups
   *
   * @param theShowDirectMemberCounts whether to count direct memberships for groups
   * @return
   */
  public RelationGraph assignShowDirectMemberCounts(boolean theShowDirectMemberCounts) {
    this.showDirectMemberCounts = theShowDirectMemberCounts;
    return this;
  }

  /**
   * flags whether to show the object type strings (e.g. ref, basis ...) for stems and groups
   *
   * @param theShowObjectTypes whether to count direct memberships for groups
   * @return
   */
  public RelationGraph assignShowObjectTypes(boolean theShowObjectTypes) {
    this.showObjectTypes = theShowObjectTypes;
    return this;
  }

  /**
   * flags whether to show the loader jobs that populate groups
   *
   * @param theShowLoaderJobs whether to include loader jobs
   * @return
   */
  public RelationGraph assignShowLoaderJobs(boolean theShowLoaderJobs) {
    this.showLoaderJobs = theShowLoaderJobs;
    return this;
  }

  /**
   * flags whether to show PSPNG provisioner targets. If set, this requires that the attribute
   * definition for etc:pspng:provision_to be created, otherwise an exception in the build will occur
   *
   * @param theShowProvisionTargets whether to include PSPNG provisioner targets
   * @return
   */
  public RelationGraph assignShowProvisionTargets(boolean theShowProvisionTargets) {
    this.showProvisionTargets = theShowProvisionTargets;
    return this;
  }

  /**
   * flags whether to show stems
   *
   * @param theShowStems whether to include stems
   * @return
   */
  public RelationGraph assignShowStems(boolean theShowStems) {
    this.showStems = theShowStems;
    return this;
  }

  /**
   * flags whether to include groups in the count of group members
   *
   * @param includeGroupsInMemberCounts whether to consider groups when counting members
   * @return
   */
  public RelationGraph assignIncludeGroupsInMemberCounts(boolean includeGroupsInMemberCounts) {
    this.includeGroupsInMemberCounts = includeGroupsInMemberCounts;
    return this;
  }

  /**
   * Assigns patterns for stem names to be filtered out. Will not skip the starting node even
   * if it matches.
   *
   * @param theSkipFolderNamePatterns the set of regular expressions to filter out matching stem names
   */
  public RelationGraph assignSkipFolderNamePatterns(Set<String> theSkipFolderNamePatterns) {
    this.skipFolderNamePatterns = theSkipFolderNamePatterns;
    return this;
  }

  /**
   * Assigns patterns for group names to be filtered out. Will not skip the starting node even
   * if it matches.
   *
   * @param theSkipGroupNamePatterns the set of regular expressions to filter out matching group names
   */
  public RelationGraph assignSkipGroupNamePatterns(Set<String> theSkipGroupNamePatterns) {
    this.skipGroupNamePatterns = theSkipGroupNamePatterns;
    return this;
  }

  /**
   * The maximum number of objects of the same type to add as parents/children, or
   * a value zero or less to include all objects. Any more than this will be excluded
   * from the graph. The same "type" refers to the role; e.g., both loader jobs and
   * members will be parents of a group, but are different types.
   *
   * @param theMaxSiblings the maximum number of sibling objects before filtering out additional ones
   */
  public RelationGraph assignMaxSiblings(long theMaxSiblings) {
    this.maxSiblings = theMaxSiblings;
    return this;
  }

  /**** Getters for the associated assignX methods ****/

  /**
   * returns the number of parent levels to include in the graph
   *
   * @see #assignParentLevels(long)
   * @return the maximum number of parent levels to include in the graph
   */
  public long getParentLevels() {
    return parentLevels;
  }

  /**
   * returns the number of child levels to include in the graph
   *
   * @see #assignChildLevels(long)
   * @return the maximum number of child levels to include in the graph
   */
  public long getChildLevels() {
    return childLevels;
  }

  /**
   * returns whether memberships are counted for Group nodes
   *
   * @see #assignShowAllMemberCounts(boolean)
   * @return if memberships are counted for groups
   */
  public boolean isShowAllMemberCounts() {
    return showAllMemberCounts;
  }

  /**
   * returns whether direct memberships are counted for Group nodes
   *
   * @see #assignShowDirectMemberCounts(boolean)
   * @return if direct memberships are counted for groups
   */
  public boolean isShowDirectMemberCounts() {
    return showDirectMemberCounts;
  }

  /**
   * returns whether to show object types for stems and groups
   *
   * @see #assignShowObjectTypes(boolean)
   * @return if showing object types
   */
  public boolean isShowObjectTypes() {
    return showObjectTypes;
  }

  /**
   * returns whether loader jobs should be included as graph nodes
   *
   * @see #assignShowLoaderJobs(boolean)
   * @return if loader jobs should be included in the graph
   */
  public boolean isShowLoaderJobs() {
    return showLoaderJobs;
  }

  /**
   * returns whether PSPNG provisioner targets should be included as graph nodes
   *
   * @see #assignShowProvisionTargets(boolean)
   * @return if PSPNG provisioners should be included in the graph
   */
  public boolean isShowProvisionTargets() {
    return showProvisionTargets;
  }

  /**
   * returns whether stems should be included as graph nodes
   *
   * @see #assignShowStems(boolean)
   * @return if stems should be included in the graph
   */
  public boolean isShowStems() {
    return showStems;
  }

  /**
   * returns whether to include groups in the count of group members
   *
   * @see #assignIncludeGroupsInMemberCounts(boolean)
   * @return if groups are considered in the count of group members
   */
  public boolean isIncludeGroupsInMemberCounts() {
    return includeGroupsInMemberCounts;
  }

  /**
   * returns the filters for stems when building the graph
   *
   * @see #assignSkipFolderNamePatterns(Set)
   * @return the set of regular expressions to filter out matching stem names
   */
  public Set<String> getSkipFolderNamePatterns() {
    return skipFolderNamePatterns;
  }

  /**
   * returns the filters for stems when building the graph
   *
   * @see #assignSkipGroupNamePatterns(Set)
   * @return the set of regular expressions to filter out matching stem names
   */
  public Set<String> getSkipGroupNamePatterns() {
    return skipGroupNamePatterns;
  }

  /**
   * returns the maximum number of objects of the same type to be included as
   * parents or children of an object
   *
   * @see #assignMaxSiblings(long)
   * @return the maximum number of objects of the same type to be included in relations
   */
  public long getMaxSiblings() {
    return maxSiblings;
  }

  /**** Methods generally meaningful after the build is complete ****/

  /**
   * The initializing object for the object, wrapped in a node
   *
   * @return the starting node
   */
  public GraphNode getStartNode() {
    return startNode;
  }

  /**
   * after building, returns the set of all edges
   *
   * @return The set of all edges in the built graph
   */
  public Set<GraphEdge> getEdges() {
    return edges;
  }

  /**
   * after building, returns the set of all nodes
   *
   * @return The set of all nodes in the built graph
   */
  public Collection<GraphNode> getNodes() {
    return objectToNodeMap.values();
  }


  /**
   * after building, returns how many folders were skipped as the result of filters
   *
   * @return the number of folders skipped due to filters
   */
  public long getNumSkippedFolders() {
    return skippedFolders == null ? 0 : skippedFolders.size();
  }

  /**
   * after building, returns how many groups were skipped as the result of filters
   *
   * @return the number of groups skipped due to filters
   */
  public long getNumSkippedGroups() {
    return skippedGroups == null ? 0 : skippedGroups.size();
  }

  /**
   * after building, returns how many loader jobs were encountered
   *
   * @return the number of loader jobs in the graph
   */
  public long getNumLoaders() {
    return numLoaders;
  }

  /**
   * after building, returns how many groups have memberships loaded from loader jobs
   *
   * @return the number of groups loaded from loader jobs
   */
  public long getNumGroupsFromLoaders() {
    return numGroupsFromLoaders;
  }

  /**
   * after building, returns how many distinct PSPNG provisioners were encountered
   *
   * @return the number of PSPNG provisioning targets in the graph
   */
  public long getNumProvisioners() {
    return numProvisioners;
  }

  /**
   * after building, the total of all memberships in all groups
   *
   * @return the total of all group memberships
   */
  public long getTotalMemberCount() {
    return totalMemberCount;
  }

  /**
   * after building, the total of all direct memberships in all groups
   *
   * @return the total of direct group memberships
   */
  public long getDirectMemberCount() {
    return directMemberCount;
  }

  /**
   * after building, returns how many groups have one or more PSPNG provisioner targets
   *
   * @return the number of groups loaded from loader jobs
   */
  public long getNumGroupsToProvisioners() {
    return numGroupsToProvisioners;
  }

  /**
   * After building, returns the highest parent distance from the starting node. This will
   * always be a positive number, even though parents of the start node will have property
   * distanceFromStartNode less than zero.
   *
   * @return the the maximum parent distance from the starting node
   */
  public long getMaxParentDistance() {
    return maxParentDistance;
  }

  /**
   * After building, returns the highest child distance from the starting node. This will
   * always be zero or greater.
   *
   * @return the the maximum child distance from the starting node
   */
  public long getMaxChildDistance() {
    return maxChildDistance;
  }

  /**
   * returns all the top level parent nodes (nodes with no parents)
   *
   * @return all nodes which do not have parent nodes
   */
  public Set<GraphNode> getLeafParentNodes() {
    return leafParentNodes;
  }

  /**
   * returns all the bottom level child nodes (nodes with no children)
   *
   * @return all nodes which do not have child nodes
   */
  public Set<GraphNode> getLeafChildNodes() {
    return leafChildNodes;
  }

  /**
   * retrieve a graph node based on its contained Grouper object
   *
   * @param object the Grouper object to query
   * @return the node containing this object, or null if not found
   */
  public GraphNode getNode(GrouperObject object) {
    return objectToNodeMap.containsKey(object) ? objectToNodeMap.get(object) : null;
  }


  // internal function to get an existing node, or create one and set up stats for it
  private GraphNode fetchOrCreateNode(GrouperObject object) {
    GraphNode node;
    if (objectToNodeMap.containsKey(object)) {
      node = objectToNodeMap.get(object);
    } else {
      node = new GraphNode(object);
      objectToNodeMap.put(object, node);
      if (node.isLoaderGroup()) {
        ++numLoaders;
      }
      if (node.isProvisionerTarget()) {
        ++numProvisioners;
      }

    }

    return node;
  }

  // return the immediate groups that are members for this group
  private Set<Member> fetchImmediateGsaMembers(Group g) {
    //currently just memberships, not privileges
    return g.getImmediateMembers(grouperMemberField, grouperGSASources, null);
  }

  // return the groups having this node's group or subject as a direct member
  private Set<MembershipSubjectContainer> fetchImmediateMemberships(GraphNode fromNode) {
    if (fromNode.isGroup()) {
      Group g = (Group) fromNode.getGrouperObject();
      MembershipFinder membershipFinder = new MembershipFinder()
        .addMemberId(g.toMember().getId())
        .assignCheckSecurity(true)
        .assignHasFieldForGroup(true)
        .assignEnabled(true)
        .assignHasMembershipTypeForGroup(true)
        .assignMembershipType(MembershipType.IMMEDIATE);

      return membershipFinder.findMembershipResult().getMembershipSubjectContainers();
    } else if (fromNode.isSubject()) {
      Subject subject = ((GrouperObjectSubjectWrapper) fromNode.getGrouperObject()).getSubject();

      MembershipFinder membershipFinder = new MembershipFinder()
        .addSubject(subject)
        .assignEnabled(true)
        .assignHasFieldForGroup(true)
        .assignHasMembershipTypeForGroup(true)
        .assignMembershipType(MembershipType.IMMEDIATE);
      return membershipFinder.findMembershipResult().getMembershipSubjectContainers();
    } else {
      throw new RuntimeException("Can only get memberships for groups and subjects");
    }
  }

  // return the loader job(s) for this group
  private List<Group> fetchLoaderJobs(Group g) {
    Set<AttributeAssign> attrAssigns = null;
    try {
      attrAssigns = g.getAttributeDelegate().retrieveAssignmentsByAttributeDef(GrouperCheckConfig.loaderMetadataStemName() + ":loaderMetadataDef");
    } catch (AttributeDefNotFoundException e) {
      LOG.debug("Could not find loaderMetadataDef attribute for group " + g.getName() + " (" + e.getMessage() + ")");
    }

    if (attrAssigns == null || attrAssigns.size() == 0) {
      return Collections.emptyList();
    }

    List<Group> ret = new LinkedList<Group>();

    ++numGroupsFromLoaders;
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    for (AttributeAssign aa: attrAssigns) {
      String jobId = aa.getAttributeValueDelegate().retrieveValueString(GrouperCheckConfig.loaderMetadataStemName() + ":" + GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_GROUP_ID);
      try {
        Group jobGroup = GroupFinder.findByUuid(grouperSession, jobId, true);
        ret.add(jobGroup);
      } catch (Exception e) {
        LOG.error("Failed to find loader job with id " + jobId + "referenced by group " + g.getName());
      }
    }

    return ret;
  }

  /*
   * Return the set of all groups loaded by a loader group, by its id
   */
  private Set<Group> fetchGroupsLoadedByJob(Group group) {
    if (loaderGroupIdAttrDefNameId != null) {
      return new GroupFinder()
        .assignIdOfAttributeDefName(loaderGroupIdAttrDefNameId)
        .assignAttributeValuesOnAssignment(GrouperUtil.toSetObjectType(group.getId()))
        .findGroups();
    } else {
      // previously unable to init the attributeDef
      return Collections.emptySet();
    }
  }
  /*
   * return the PSPNG provisioning targets (as GrouperObject wrappers) for this group,
   * as seen in the provision_to attribute
   */
  private List<GrouperObjectProvisionerWrapper> fetchProvisioners(Group g) {
    if (provisionToAttributeDefName == null) {
      return Collections.emptyList();
    }

    Set<AttributeAssign> attrAssigns = null;
    try {
      attrAssigns = g.getAttributeDelegate().retrieveAssignments(provisionToAttributeDefName);
    } catch (AttributeDefNotFoundException e) {
      LOG.debug("Failed to get provisioner attribute of group " + g.getName() + " (" + e.getMessage() + ")");
    } catch (InsufficientPrivilegeException e) {
      LOG.debug("Failed to get provisioner attribute of group " + g.getName() + " (insufficient privilege)");
    }

    if (attrAssigns == null || attrAssigns.size() == 0) {
      return Collections.emptyList();
    }

    List<GrouperObjectProvisionerWrapper> ret = new LinkedList<GrouperObjectProvisionerWrapper>();

    ++numGroupsToProvisioners;

    for (AttributeAssign aa: attrAssigns) {
      String provId = aa.getValueDelegate().retrieveValueString();
      ret.add(new GrouperObjectProvisionerWrapper(provId));
    }

    return ret;
  }

  // returns the number of members in this group
  @Deprecated
  private long fetchGroupCount(Group g) {
    QueryOptions q = new QueryOptions().retrieveResults(false).retrieveCount(true);
    if (includeGroupsInMemberCounts) {
      MembershipFinder.findMembers(g, grouperMemberField, q);
    } else {
      MembershipFinder.findMembers(g, grouperMemberField, nonGroupSourcesCache, q);
    }
    return q.getCount();
  }

  // for a stem, does the name match any of the blacklist regular expressions
  private boolean matchesFilter(GrouperObject obj) {
    if (obj instanceof Stem) {
      if (skippedFolders.contains(obj)) {
        return true;
      }

      for (Pattern p: skipFolderPatterns) {
        if (p.matcher(obj.getName()).matches()) {
          skippedFolders.add((Stem) obj);
          return true;
        }
      }
    } else if (obj instanceof Group) {
      if (skippedGroups.contains(obj)) {
        return true;
      }

      for (Pattern p: skipGroupPatterns) {
        if (p.matcher(obj.getName()).matches()) {
          skippedGroups.add((Group) obj);
          return true;
        }
      }
    }

    return false;
  }

  // add a directed edge
  private void addEdge(GraphNode fromNode, GraphNode toNode) {
    edges.add(new GraphEdge(fromNode, toNode));
  }

  // add a directed edge, when the edge type is known
  private void addEdge(GraphNode fromNode, GraphNode toNode, StyleObjectType styleObjecType) {
    edges.add(new GraphEdge(fromNode, toNode, styleObjecType));
  }

  // recursively walk parents of this node -- includes parent stem, group loaders, group members, and composites
  private void buildParentNodes(GraphNode toNode, long level, boolean isRecursive) {
    if (this.parentLevels != -1 && level > this.parentLevels) {
      return;
    }

    if (level > RECURSIVE_LEVEL_LIMIT) {
      String msg = "Reached max recursive limit of levels (" + RECURSIVE_LEVEL_LIMIT + ") while building relationship graph";
      LOG.error(msg);
      throw new RuntimeException(msg);
    }

    Set<GraphNode> nodesToVisit = new HashSet<GraphNode>();

    // For complement groups, need to handle factors as a completely different case.
    // For intersect, left and right don't matter much, but still tracking it
    Map<GraphNode, StyleObjectType> compositeStyleTypes = new HashMap<GraphNode, StyleObjectType>();

    // for groups and stems, get the parent stem if including stems.
    // Abort at the root stem since getting its parent will throw an error
    if (this.showStems) {
      if (toNode.isGroup() || (toNode.isStem() && !((Stem)toNode.getGrouperObject()).isRootStem())) {
        GrouperObject parentStem = toNode.getGrouperObject().getParentStem();
        if (!matchesFilter(parentStem)) {
          GraphNode parentNode = fetchOrCreateNode(parentStem);
          //addEdge(parentNode, toNode);
          nodesToVisit.add(parentNode);
        }
      }
    }

    if (toNode.isGroup()) {
      Group theGroup = (Group) (toNode.getGrouperObject());

      // for groups, find groups having this as a direct member
      long numMembershipsAdded = 0;
      for (MembershipSubjectContainer msc : fetchImmediateMemberships(toNode)) {
        Group fromGroup = msc.getGroupOwner();
        if (fromGroup == null) {
          continue;
        }
        if (getMaxSiblings() > 0 && numMembershipsAdded >= getMaxSiblings()) {
          skippedGroups.add(fromGroup);
        } else if (!matchesFilter(fromGroup)) {
          GraphNode fromNode = fetchOrCreateNode(fromGroup);
          nodesToVisit.add(fromNode);
          ++numMembershipsAdded;
        }
      }

      if (showLoaderJobs) {
        List<Group> jobGroups = fetchLoaderJobs(theGroup);
        for (Group jobGroup : jobGroups) {
          if (!matchesFilter(jobGroup)) {
            GraphNode jobNode = fetchOrCreateNode(jobGroup);
            if (jobNode.equals(toNode)) {
              // this is a simple loader self link; add the edge to self but don't visit it to avoid infinite recursion
              addEdge(jobNode, toNode);
            } else {
              nodesToVisit.add(jobNode);
            }
          }
        }
      }

      // get groups where this is a composite factor
      for (Composite composite : CompositeFinder.findAsFactor(theGroup)) {
        // findAsFactor doesn't distinguish left/right, so need to compare current group with both
        Group ownerGroup = composite.getOwnerGroup();
        if (!matchesFilter(ownerGroup)) {
          GraphNode fromNode = fetchOrCreateNode(ownerGroup);
          nodesToVisit.add(fromNode);
          if (composite.getType() == CompositeType.COMPLEMENT) {
            if (theGroup.equals(composite.getLeftGroup())) {
              compositeStyleTypes.put(fromNode, StyleObjectType.EDGE_COMPLEMENT_LEFT);
            } else if (theGroup.equals(composite.getRightGroup())) {
              compositeStyleTypes.put(fromNode, StyleObjectType.EDGE_COMPLEMENT_RIGHT);
            }
          } else if (composite.getType() == CompositeType.INTERSECTION) {
            if (theGroup.equals(composite.getLeftGroup())) {
              compositeStyleTypes.put(fromNode, StyleObjectType.EDGE_INTERSECT_LEFT);
            } else if (theGroup.equals(composite.getRightGroup())) {
              compositeStyleTypes.put(fromNode, StyleObjectType.EDGE_INTERSECT_RIGHT);
            }
          }
        }
      }
    } else if (toNode.isSubject()) {
    Set<MembershipSubjectContainer> memberships = fetchImmediateMemberships(toNode);
    long numSubjectMembershipsAdded = 0;
    for (MembershipSubjectContainer msc : memberships) {
      Group fromGroup = msc.getGroupOwner();
      if (fromGroup == null) {
        continue;
      }
      if (getMaxSiblings() > 0 && numSubjectMembershipsAdded >= getMaxSiblings()) {
        skippedGroups.add(fromGroup);
      } else if (!matchesFilter(fromGroup)) {
        GraphNode fromNode = fetchOrCreateNode(fromGroup);
        nodesToVisit.add(fromNode);
      }
    }
  }

  boolean didAddEdges = false;
    for (GraphNode n : nodesToVisit) {
      GraphEdge edgeCandidate = null;
      if (compositeStyleTypes.containsKey(n)) {
        edgeCandidate = new GraphEdge(n, toNode, compositeStyleTypes.get(n));
      } else {
        edgeCandidate = new GraphEdge(n, toNode);
      }
      if (!edges.contains(edgeCandidate)) {
        edges.add(edgeCandidate);
        didAddEdges = true;
        n.setDistanceFromStartNode(-1 * level);

        // if target is a composite group, it's possible that both factors aren't being included by
        // the normal path following. Getting a composite's only level of children (not recursive)
        // as a special case ensures that both factors will be included
        if (n.isIntersectGroup() || n.isComplementGroup()) {
          visitNode(n, level, isRecursive, false, false, true);
        } else {
          visitNode(n, level, isRecursive, false);
        }
      } else {
        LOG.debug("Loop detected; object " + n.getGrouperObjectName() + " has been seen a second time as a parent (second link was from " + toNode.getGrouperObjectName() + ")");
      }
    }

    if (!didAddEdges) {
      leafChildNodes.add(toNode);
    } else {
      if (level > maxParentDistance) {
        maxParentDistance = level;
      }
    }
  }

  // recursively walk child nodes -- includes stem's child groups, group/subject direct memberships, provisioners
  private void buildChildNodes(GraphNode fromNode, long level, boolean isRecursive) {
    if (this.childLevels != -1 && level > this.childLevels) {
      return;
    }

    if (level > RECURSIVE_LEVEL_LIMIT) {
      String msg = "Reached max recursive limit of levels (" + RECURSIVE_LEVEL_LIMIT + ") while building relationship graph";
      LOG.error(msg);
      throw new RuntimeException(msg);
    }

    Set<GraphNode> nodesToVisit = new HashSet<GraphNode>();

    // For complement groups, need to handle factors as a completely different case.
    // For intersect, left and right don't matter much, but still tracking it
    Map<GraphNode, StyleObjectType> compositeStyleTypes = new HashMap<GraphNode, StyleObjectType>();

    // for stems, always get the child groups. Also get the child stems
    // if showing them
    if (fromNode.isStem()) {
      long numGroupsAdded = 0;
      for (Group g : ((Stem) fromNode.getGrouperObject()).getChildGroups(Stem.Scope.ONE)) {
        if (getMaxSiblings() > 0 && numGroupsAdded >= getMaxSiblings()) {
          skippedGroups.add(g);
        } else if (!matchesFilter(g)) {
          GraphNode toNode = fetchOrCreateNode(g);
          //addEdge(fromNode, toNode);
          nodesToVisit.add(toNode);
          ++numGroupsAdded;
        }
      }

      if (this.showStems) {
        long numStemsAdded = 0;
        for (Stem s : ((Stem) fromNode.getGrouperObject()).getChildStems()) {
          if (getMaxSiblings() > 0 && numStemsAdded >= getMaxSiblings()) {
            skippedFolders.add(s);
          } else if (!matchesFilter(s)) {
            GraphNode toNode = fetchOrCreateNode(s);
            //addEdge(fromNode, toNode);
            nodesToVisit.add(toNode);
            ++numStemsAdded;
          }
        }
      }
    } else if (fromNode.isGroup()) {
      Group theGroup = (Group) (fromNode.getGrouperObject());

      // for a group, get immediate g:gsa members as parents
      long numMembersAdded = 0;
      for (final Member m : fetchImmediateGsaMembers(theGroup)) {
        Group childGroup = null;
        try {
          // use this version if the graph should exclude groups that can't be viewed
          childGroup = m.toGroup();
          // use this version to see all the groups.
//        // parentGroup = (Group)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
//        //    public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
//        //    return m.toGroup();
//        //  }
//        // });
        } catch (GroupNotFoundException e) {
          //user does not have permission to view group
          LOG.trace("Session " + GrouperSession.staticGrouperSession().getSubject().toString() + " failed to convert memberId " + m.getId() + " to a group (user does not have permission?) "
            + "-- this group and any connected to it will be skipped");
          continue;
        }

        if (getMaxSiblings() > 0 && numMembersAdded >= getMaxSiblings()) {
          skippedGroups.add(childGroup);
        } else if (!matchesFilter(childGroup)) {
          GraphNode childNode = fetchOrCreateNode(childGroup);
          nodesToVisit.add(childNode);
          ++numMembersAdded;
        }
      }


      // get provisioners
      if (showProvisionTargets) {
        List<GrouperObjectProvisionerWrapper> provTargets = fetchProvisioners(theGroup);
        for (GrouperObjectProvisionerWrapper p : provTargets) {
          if (!matchesFilter(p)) {
            GraphNode provNode = fetchOrCreateNode(p);
            nodesToVisit.add(provNode);
          }
        }
      }

      // if a loader job, get the groups loaded by it
      // for a group, get immediate g:gsa members as parents
      long numLoadedGroupsByJob = 0;
      for (Group childGroup : fetchGroupsLoadedByJob(theGroup)) {
        if (getMaxSiblings() > 0 && numLoadedGroupsByJob >= getMaxSiblings()) {
          skippedGroups.add(childGroup);
        } else if (!matchesFilter(childGroup)) {
          GraphNode childNode = fetchOrCreateNode(childGroup);
          if (childNode.equals(fromNode)) {
            // this is a simple loader self link; add the edge to self but don't visit it to avoid infinite recursion
            addEdge(fromNode, childNode);
          } else {
            nodesToVisit.add(childNode);
            ++numLoadedGroupsByJob;
          }
        }
      }

      // Show composite factors.
      if (theGroup.hasComposite()) {
        Composite composite = theGroup.getComposite(true);

        Group left;
        try {
          left = composite.getLeftGroup();
          if (!matchesFilter(left)) {
            GraphNode nodeLeft = fetchOrCreateNode(left);
            nodesToVisit.add(nodeLeft);
            if (composite.getType().equals(CompositeType.COMPLEMENT)) {
              compositeStyleTypes.put(nodeLeft, StyleObjectType.EDGE_COMPLEMENT_LEFT);
            } else if (composite.getType().equals(CompositeType.INTERSECTION)) {
              compositeStyleTypes.put(nodeLeft, StyleObjectType.EDGE_INTERSECT_LEFT);
            }
          }
        } catch (GroupNotFoundException e) {
          LOG.debug("Failed to find left composite factor of group " + theGroup.getName() + "; maybe no privileges?");
        }
        Group right;
        try {
          right = composite.getRightGroup();
          if (!matchesFilter(right)) {
            GraphNode nodeRight = fetchOrCreateNode(right);
            nodesToVisit.add(nodeRight);
            if (composite.getType().equals(CompositeType.COMPLEMENT)) {
              compositeStyleTypes.put(nodeRight, StyleObjectType.EDGE_COMPLEMENT_RIGHT);
            } else if (composite.getType().equals(CompositeType.INTERSECTION)) {
              compositeStyleTypes.put(nodeRight, StyleObjectType.EDGE_INTERSECT_RIGHT);
            }
          }
        } catch (GroupNotFoundException e) {
          LOG.debug("Failed to find left composite factor of group " + theGroup.getName() + "; maybe no privileges?");
        }
      }
    }

    boolean didAddEdges = false;
    for (GraphNode n : nodesToVisit) {
      GraphEdge edgeCandidate = null;
      if (compositeStyleTypes.containsKey(n)) {
        edgeCandidate = new GraphEdge(fromNode, n, compositeStyleTypes.get(n));
      } else {
        edgeCandidate = new GraphEdge(fromNode, n);
      }
      if (!edges.contains(edgeCandidate)) {
        edges.add(edgeCandidate);
        didAddEdges = true;
        n.setDistanceFromStartNode(level);
        visitNode(n, level, false, isRecursive);
      } else {
        LOG.debug("Loop detected; object " + n.getGrouperObjectName() + " has been seen a second time as a child (second link was from " + fromNode.getGrouperObjectName() + ")");
      }
    }

    if (!didAddEdges) {
      leafChildNodes.add(fromNode);
    } else {
      if (level > maxChildDistance) {
        maxChildDistance = level;
      }
    }
  }

  // Version of visitNode() that can stop after one level of parent/child, rather than continuing
  // recursively. If include*Recursive is false but include*OneLevel is true, only follow the next level and
  // stop. If include*Recursive is true, ignore the value of include*OneLevel
  private void visitNode(GraphNode node, long level, boolean includeParentsRecursive, boolean includeParentOneLevel,
                         boolean includeChildrenRecursive, boolean includeChildOneLevel) {
    if (node.isVisited()) {
      return;
    }

    if (node.isSubject()) {
      //subjects don't have child members, so mark as skip it right away
      node.setVisitedChildren(true);
    }

    if ((includeParentsRecursive || includeParentOneLevel) && !node.isVisitedParents()) {
      //Get parents recursively
      buildParentNodes(node, 1 + level, includeParentsRecursive);
      // only mark truly visited when the walk was fully realized; this means there is the potential
      // to be visited twice in different contexts, once as a one-off and once fully analyzed
      node.setVisitedParents(includeParentsRecursive);
    }

    if ((includeChildrenRecursive || includeChildOneLevel) && !node.isVisitedChildren()) {
      //Get children recursively
      buildChildNodes(node, 1 + level, includeChildrenRecursive);
      // only mark truly visited when the walk was fully realized; this means there is the potential
      // to be visited twice in different contexts, once as a one-off and once fully analyzed
      node.setVisitedChildren(includeChildrenRecursive);
    }
  }

  private void visitNode(GraphNode node, long level, boolean includeParentsRecursive, boolean includeChildrenRecursive) {
    visitNode(node, level, includeParentsRecursive, false, includeChildrenRecursive, false);
  }

  /**
   * Builds the directed graph. Beginning with the starting node, will recursively walk its parents
   * and children. For stems, will visit the parents and children of all its child groups. If starting
   * with a subject, will only visit the children.
   *
   */
  public void build() {
    if (startObject == null) {
      throw new RuntimeException("Starting object was not defined");
    }

    // this may be the first time through; attribute to look up the attribute definitions
    initLookupFields();

    skippedFolders = new HashSet<Stem>();
    skipFolderPatterns = new LinkedList<Pattern>();
    if (skipFolderNamePatterns != null) {
      for (String regexp: skipFolderNamePatterns) {
        skipFolderPatterns.add(Pattern.compile(regexp));
      }
    }

    skippedGroups = new HashSet<Group>();
    skipGroupPatterns = new LinkedList<Pattern>();
    if (skipGroupNamePatterns != null) {
      for (String regexp: skipGroupNamePatterns) {
        skipGroupPatterns.add(Pattern.compile(regexp));
      }
    }

    objectToNodeMap = new HashMap<GrouperObject, GraphNode>();
    edges = new HashSet<GraphEdge>();

    // set up calculation fields
    numLoaders = 0;
    numGroupsFromLoaders = 0;
    numProvisioners = 0;
    totalMemberCount = 0;
    directMemberCount = 0;
    maxParentDistance = 0;
    maxChildDistance = 0;
    leafParentNodes = new HashSet<GraphNode>();
    leafChildNodes = new HashSet<GraphNode>();

    LOG.info("Starting graph build: "
      + "start object=" + startObject.toString() + ", "
      + "max parent levels=" + getParentLevels() + ", "
      + "max child levels=" + getChildLevels() + ", "
      + "show stems=" + isShowStems() + ", "
      + "show loader jobs=" + isShowLoaderJobs() + ", "
      + "show PSPNG provisioners=" + isShowProvisionTargets() + ", "
      + "show member counts=" + isShowAllMemberCounts() + ", "
      + "show direct member counts=" + isShowDirectMemberCounts() + ", "
      + "show object types=" + isShowObjectTypes() + ", "
      + "include groups in member counts=" + isIncludeGroupsInMemberCounts() + ", "
      + "folder pattern filters=" + GrouperUtil.join(skipFolderNamePatterns.toArray(), "; "));

    startNode = fetchOrCreateNode(startObject);
    startNode.setStartNode(true);

    // always put the start node, even if that type is skipped
    objectToNodeMap.put(startObject, startNode);

    visitNode(startNode, 0, true, true);

    // if starting with a stem, also visit the parents of its child groups
    if (startNode.isStem()) {
      for (Group g: ((Stem)startNode.getGrouperObject()).getChildGroups(Stem.Scope.ONE)) {
        visitNode(fetchOrCreateNode(g), 1, true, false);
      }

    }

    LOG.debug("Graph completed build; nodes = " + objectToNodeMap.size() + "; edges = " + edges.size());

    for (GraphEdge e : edges) {
      e.getFromNode().addChildNode(e.getToNode());
      e.getToNode().addParentNode(e.getFromNode());
    }

    // do all the group counts in batches
    queryGroupMemberCounts();

    // do all the building of object type strings in batches
    queryObjectTypeNames();

  }

  // once the graph is built, query counts for group objects depending on the settings
  private void queryGroupMemberCounts() {
    if (!showAllMemberCounts && !showDirectMemberCounts) {
      return;
    }

    //collect all eligible group nodes
    Map<String, GraphNode> groupNodesByUuid = new HashMap<String, GraphNode>();
    for (GraphNode node: getNodes()) {
      if (node.isGroup() && (!node.isLoaderGroup() || node.isSimpleLoaderGroup())) {
        groupNodesByUuid.put(node.getGrouperObjectId(), node);
      }
    }

    // no groups to count, don't need to continue
    if (groupNodesByUuid.size() == 0) {
      return;
    }

    List<String> groupUuids = GrouperUtil.listFromCollection(groupNodesByUuid.keySet());

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupUuids.size(), 100);
    for (int i = 0; i < numberOfBatches; i++) {
      List<String> currentBatch = GrouperUtil.batchList(groupUuids, 100, i);
      if (currentBatch.size() == 0) {
        continue;
      }

      StringBuilder theHqlQuery = new StringBuilder(
        "select gg.uuid," +
          /* total members */
          " (" +
          "   select count(distinct gms.memberUuid)" +
          "     from MembershipEntry gms, Member gm, Field gfl" +
          "    where gms.memberUuid = gm.uuid and gms.fieldId = gfl.uuid" +
          "      and gms.enabledDb = 'T'" +
          "      and gfl.name = 'members'" +
          (includeGroupsInMemberCounts ? "" : "       and gm.subjectSourceIdDb != 'g:gsa'") +
          "      and gms.ownerGroupId = gg.uuid" +
          " )," +
          /* direct members */
          " (" +
          "   select count(distinct gms.memberUuid)" +
          "     from MembershipEntry gms, Member gm, Field gfl" +
          "    where gms.memberUuid = gm.uuid and gms.fieldId = gfl.uuid" +
          "      and gms.enabledDb = 'T'" +
          "      and gfl.name = 'members'" +
          "      and gms.type = 'immediate'" +
          (includeGroupsInMemberCounts ? "" : "       and gm.subjectSourceIdDb != 'g:gsa'") +
          "      and gms.ownerGroupId = gg.uuid" +
          "  )" +
          " from Group as gg" +
          " where gg.uuid in (");

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      theHqlQuery.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
      theHqlQuery.append(")");
      byHqlStatic.createQuery(theHqlQuery.toString());

      List<Object[]> results = byHqlStatic.list(Object[].class);

      for (Object[] values : results) {
        String groupId = (String) values[0];
        if (groupNodesByUuid.containsKey(groupId)) {
          // not sure why this wouldn't be found
          GraphNode node = groupNodesByUuid.get(groupId);

          long allCountForGroup = GrouperUtil.longValue(values[1]);
          long directCountForGroup = GrouperUtil.longValue(values[2]);
          node.setAllMemberCount(allCountForGroup);
          this.totalMemberCount += allCountForGroup;
          node.setDirectMemberCount(directCountForGroup);
          this.directMemberCount += directCountForGroup;
        }
      }
    }
  }

  // once the graph is built, query counts for group objects depending on the settings
  private void queryObjectTypeNames() {
    
    // not sure why it wouldnt be empty, but empty it anyhow
    this.getObjectTypesUsed().clear();
    
    if (!showObjectTypes) {
      return;
    }

    if (objectTypeAttributeId == null || objectTypeAttributeValueId == null) {
      LOG.info("Graph build requested to show object types, but the attributes could not be found -- skipping object types");
      return;
    }

    //collect all eligible group and stem nodes
    Map<String, GraphNode> nodesByUuid = new HashMap<String, GraphNode>();
    for (GraphNode node: getNodes()) {
      if (node.isGroup() || node.isStem()) {
        nodesByUuid.put(node.getGrouperObjectId(), node);
      }
    }

    // no groups or stems to count, don't need to continue
    if (nodesByUuid.size() == 0) {
      return;
    }

    List<String> uidList = GrouperUtil.listFromCollection(nodesByUuid.keySet());

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(uidList.size(), 98);
    for (int i = 0; i < numberOfBatches; i++) {
      List<String> currentBatch = GrouperUtil.batchList(uidList, 98, i);
      if (currentBatch.size() == 0) {
        continue;
      }

      StringBuilder theHqlQuery = new StringBuilder(
        "SELECT DISTINCT" +
          "  COALESCE(aa.ownerGroupId, aa.ownerStemId), aav.valueString" +
          "  FROM AttributeAssign aa, AttributeAssign aa2, AttributeAssignValue aav" +
          " WHERE aa2.ownerAttributeAssignId = aa.id" +
          "   AND aav.attributeAssignId = aa2.id" +
          "   AND aa.enabledDb = 'T'" +
          "   AND aa.attributeAssignTypeDb IN ('group', 'stem')       " +
          "   AND aa2.enabledDb = 'T'" +
          "   AND aa2.attributeAssignTypeDb IN ('group_asgn', 'stem_asgn') " +
          "   AND aa.attributeDefNameId = :typeMarker" +
          "   AND aa2.attributeDefNameId = :typeValueString" +
          "   AND COALESCE(aa.ownerGroupId, aa.ownerStemId) in (");

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      theHqlQuery.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
      theHqlQuery.append(")");
      byHqlStatic.createQuery(theHqlQuery.toString());
      byHqlStatic.setString("typeMarker", objectTypeAttributeId);
      byHqlStatic.setString("typeValueString", objectTypeAttributeValueId);

      List<Object[]> results = byHqlStatic.list(Object[].class);

      for (Object[] values : results) {
        String objectId = (String) values[0];
        if (nodesByUuid.containsKey(objectId)) {
          // not sure why this wouldn't be found
          GraphNode node = nodesByUuid.get(objectId);

          final String objectTypeName = (String) values[1];
          node.addObjectTypeName(objectTypeName);
          
          this.objectTypesUsed.add(objectTypeName);
        }
      }
    }
  }

  /**
   * keep track of which types are used for legend
   */
  private Set<String> objectTypesUsed = new HashSet<String>();
  
  /**
   * keep track of which types are used for legend
   * @return the objectTypesUsed
   */
  public Set<String> getObjectTypesUsed() {
    return this.objectTypesUsed;
  }

  // If first time called, init the static attributeDef fields, and other class properties. Find these as root user
  private static void initLookupFields() {
    if (attemptedInitLookupFields) {
      return;
    }

    if (grouperMemberField == null) {
      grouperMemberField = FieldFinder.find("members", true);
    }

    // init the g:gsa source if not set
    if (grouperGSASources == null) {
      grouperGSASources = Collections.singleton(SubjectFinder.internal_getGSA());
    }

    // SubjectHelper has a method to get non-group subject sources, but doesn't cache it.
    // Fetch and save it in this class so it doesn't need to be recalculated for every group.
    if (nonGroupSourcesCache == null) {
      nonGroupSourcesCache = SubjectHelper.nonGroupSources();
    }

    String loaderMetadataGroupIdName = GrouperCheckConfig.loaderMetadataStemName() + ":" + GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_GROUP_ID;
    try {
      loaderGroupIdAttrDefNameId = AttributeDefNameFinder.findByNameAsRoot(
        GrouperCheckConfig.loaderMetadataStemName() + ":" + GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_GROUP_ID, true
      ).getId();
    } catch (AttributeDefNameNotFoundException e) {
      LOG.warn("Unable to retrieve attribute " + loaderMetadataGroupIdName + "; results will not include groups loaded by jobs", e);
    }

    try {
      String prov_to = GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":pspng:provision_to";
      provisionToAttributeDefName = AttributeDefNameFinder.findByNameAsRoot(prov_to, true);
    } catch (AttributeDefNameNotFoundException e) {
      LOG.warn("Unable to retrieve PSPNG provision_to attribute; results will not include provisioning relationships", e);
    }

    try {
      // is GroupTypeFinder using root session by default?
      sqlLoaderAttributeDefName = GroupTypeFinder.find("grouperLoader").getAttributeDefName();
    } catch (Exception e) {
      LOG.warn("Unable to retrieve attribute for sql loader jobs; groups might not be detected as loader jobs", e);
    }

    try {
      // get the attribute IDs
      // note, there is a helper function for the marker but not the metadata
      AttributeDefName typeMarkerAttributeDefName = GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase();
      if (typeMarkerAttributeDefName != null) {
        objectTypeAttributeId = typeMarkerAttributeDefName.getId();
      }
      AttributeDefName typeAttributeValueDefName = AttributeDefNameFinder.findByName(
        GrouperObjectTypesSettings.objectTypesStemName() + ":" + GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME,
        false);
      if (typeAttributeValueDefName != null) {
        objectTypeAttributeValueId = typeAttributeValueDefName.getId();
      }
    } catch (Exception e) {
      LOG.warn("Unable to retrieve attribute for Grouper object types", e);
    }

    attemptedInitLookupFields = true;
  }

  /**
   * should be only useful for {@link GraphNode} nodes needing the sql loader attribute within the
   * context of the user session
   *
   * @return
   */
  public static AttributeDefName getSqlLoaderAttributeDefName() {
    if (!attemptedInitLookupFields) {
      initLookupFields();
    }
    return sqlLoaderAttributeDefName;
  }

}
