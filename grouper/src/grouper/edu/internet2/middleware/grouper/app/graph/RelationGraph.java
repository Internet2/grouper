/**
 * Copyright 2018 Internet2
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
 */

package edu.internet2.middleware.grouper.app.graph;

import java.util.*;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;

import org.apache.commons.lang3.StringUtils;
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
import edu.internet2.middleware.grouper.abac.AbacReference;
import edu.internet2.middleware.grouper.abac.GrouperAbac;
import edu.internet2.middleware.grouper.abac.GrouperJexlScriptAnalysis;
import edu.internet2.middleware.grouper.abac.GrouperJexlScriptPart;
import edu.internet2.middleware.grouper.abac.GrouperLoaderJexlScriptFullSync;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

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

  // Class parameters for finding immediate g:gsa members and provisioners
  private static Set<Source> grouperGSASources;
  private static Field grouperMemberField;
  private static AttributeDefName provisionToPspngAttributeDefName;
  private static String loaderGroupIdAttrDefNameId;
  private static AttributeDefName sqlLoaderAttributeDefName; // used by graph nodes to determine if loader job
  private static AttributeDefName abacAttributeDefName; // used by graph nodes to determine if ABAC/jexl scripted group
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
  private boolean viewProvisionersAllowed = false;

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
  
  private Subject subjectForIsMemberCheck;

  private String overrideAbacScript;
  private Boolean overrideAbacContainsSubject;

  /** lower-case data field alias -> configured friendly name, for terse ABAC visualization labels */
  private Map<String, String> abacFieldFriendlyNames = new HashMap<String, String>();

  /** lower-case data row alias -> configured friendly name, for terse ABAC visualization labels */
  private Map<String, String> abacRowFriendlyNames = new HashMap<String, String>();

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
   * If we're checking whether a subject is a member of each group
   * @param theSearchSubject
   * @return
   */
  public RelationGraph assignSubjectForIsMemberCheck(Subject theSubject) {
    this.subjectForIsMemberCheck = theSubject;
    return this;
  }

  /**
   * flags whether to show provisioner targets. If set, this requires that the attribute
   * definition for etc:pspng:provision_to be created, otherwise an exception in the build will occur
   *
   * @param theShowProvisionTargets whether to include provisioner targets
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

  public RelationGraph assignOverrideAbacScript(String theOverrideAbacScript) {
    this.overrideAbacScript = theOverrideAbacScript;
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
   * returns whether provisioner targets should be included as graph nodes
   *
   * @see #assignShowProvisionTargets(boolean)
   * @return if provisioners should be included in the graph
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
   * after building, returns how many distinct provisioners were encountered
   *
   * @return the number of provisioning targets in the graph
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
   * If we're checking whether a subject is a member of each group
   * 
   * @return the subject
   */
  public Subject getSubjectForIsMemberCheck() {
    return this.subjectForIsMemberCheck;
  }

  /**
   * after building, returns how many groups have one or more provisioner targets
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
  private List<GrouperObjectProvisionerWrapper> fetchPspngProvisioners(Group g) {
    if (provisionToPspngAttributeDefName == null) {
      return Collections.emptyList();
    }

    Set<AttributeAssign> attrAssigns = null;
    try {
      attrAssigns = g.getAttributeDelegate().retrieveAssignments(provisionToPspngAttributeDefName);
    } catch (AttributeDefNotFoundException e) {
      LOG.debug("Failed to get PSPNG provisioner attribute of group " + g.getName() + " (" + e.getMessage() + ")");
    } catch (InsufficientPrivilegeException e) {
      LOG.debug("Failed to get PSPNG provisioner attribute of group " + g.getName() + " (insufficient privilege)");
    }

    if (attrAssigns == null || attrAssigns.size() == 0) {
      return Collections.emptyList();
    }

    List<GrouperObjectProvisionerWrapper> ret = new LinkedList<GrouperObjectProvisionerWrapper>();

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
//        // parentGroup = (Group)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
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
        // In 2.5, handle both PSPNG and new provisioner targets
        Set<GrouperObjectProvisionerWrapper> provTargets = new HashSet<>();

        // new provisioners
        if (viewProvisionersAllowed) {
          try {
            List<GrouperProvisioningAttributeValue> provisionerAttrValues = GrouperProvisioningService.getProvisioningAttributeValues(theGroup);
            for (GrouperProvisioningAttributeValue v : provisionerAttrValues) {
              if (!StringUtils.isEmpty(v.getDoProvision())) {
                provTargets.add(new GrouperObjectProvisionerWrapper(v.getTargetName()));
              }
            }
          } catch (Exception e) {
            LOG.warn("Failed to get provisioner targets for group " + theGroup.getName());
          }
        }

        // PSPNG
        List<GrouperObjectProvisionerWrapper> pspsngProvTargets = fetchPspngProvisioners(theGroup);
        provTargets.addAll(pspsngProvTargets);

        if (!provTargets.isEmpty()) {
          ++numGroupsToProvisioners;
        }

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

      // Show groups, data attributes, and data rows referenced by ABAC/jexl script (only for the start group)
      if (fromNode.isAbacGroup() && fromNode.isStartNode()) {
        try {
          if (!StringUtils.isEmpty(fromNode.getAbacScript())) {
            // Single analysis call gets both tree structure and population counts
            GrouperJexlScriptAnalysis analysis = runAbacAnalysis(fromNode.getAbacScript());

            if (!StringUtils.isEmpty(overrideAbacScript) && analysis != null
                && analysis.getGrouperJexlScriptParts() != null
                && analysis.getGrouperJexlScriptParts().size() > 0) {
              GrouperJexlScriptPart overallPart = analysis.getGrouperJexlScriptParts().get(0);
              int overallCount = overallPart.getPopulationCount();
              if (overallCount >= 0) {
                fromNode.setPopulationCount((long) overallCount);
              }
              if (subjectForIsMemberCheck != null) {
                overrideAbacContainsSubject = overallPart.isContainsSubject();
              }
            }

            List<AbacReference> references = analysis != null && analysis.getVisualizationReferences() != null
                ? analysis.getVisualizationReferences() : new ArrayList<AbacReference>();

            for (AbacReference ref : references) {
              if (ref.getRefType() == AbacReference.RefType.COMPOUND) {
                // Create a compound pseudo-node and add edges from it to its children
                boolean isCompoundAnd = "and".equals(ref.getName());
                // the compound shape shows a terse rendering of its whole sub-expression
                GrouperObjectCompoundWrapper compoundWrapper = new GrouperObjectCompoundWrapper(
                    ref.computeId(), terseNodeLabel(ref), isCompoundAnd);
                GraphNode compoundNode = fetchOrCreateNode(compoundWrapper);
                if (ref.getPopulationCount() >= 0) {
                  compoundNode.setPopulationCount((long) ref.getPopulationCount());
                }
                if (subjectForIsMemberCheck != null) {
                  compoundNode.setSubjectIsMember(ref.isContainsSubject());
                }
                nodesToVisit.add(compoundNode);
                compositeStyleTypes.put(compoundNode, determineAbacEdgeStyle(ref));

                // Add child nodes under the compound (recursively for nested compounds)
                processAbacCompoundChildren(compoundNode, ref, theGroup, level + 1, isRecursive);
              } else {
                // Simple leaf reference - direct child of ABAC group
                GraphNode refNode = createAbacLeafNode(ref, theGroup);
                if (refNode != null) {
                  if (ref.getPopulationCount() >= 0) {
                    refNode.setPopulationCount((long) ref.getPopulationCount());
                  }
                  if (subjectForIsMemberCheck != null) {
                    refNode.setSubjectIsMember(ref.isContainsSubject());
                  }
                  nodesToVisit.add(refNode);
                  compositeStyleTypes.put(refNode, determineAbacEdgeStyle(ref));
                  // hasRow leaves carry inner per-attribute children built from the predicate
                  // AST; expose them as sub-nodes under the row.
                  if (ref.getChildren() != null && !ref.getChildren().isEmpty()) {
                    processAbacCompoundChildren(refNode, ref, theGroup, level + 1, isRecursive);
                  }
                }
              }
            }
          }
        } catch (Exception e) {
          LOG.warn("Error processing ABAC script for group " + theGroup.getName() + " during visualization", e);
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

    Subject currentSubject = GrouperSession.staticGrouperSession().getSubject();

    LOG.info("Starting graph build: "
      + "caller=Subject[" + currentSubject + "], "
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

    // TODO until there is real provisioner access control, just allow read from wheel
    if (PrivilegeHelper.isWheelOrRoot(currentSubject)) {
      viewProvisionersAllowed = true;
    } else {
      LOG.info("Note: user not allowed to view provisioners, so will not be included");
    }

    startNode = fetchOrCreateNode(startObject);
    startNode.setStartNode(true);

    if (!StringUtils.isEmpty(overrideAbacScript)) {
      startNode.setAbacGroup(true);
      startNode.setAbacScript(overrideAbacScript);
    }

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

    queryGroupMemberships();

    if (overrideAbacContainsSubject != null) {
      startNode.setSubjectIsMember(overrideAbacContainsSubject);
    }

    // take care of the styles now
    for (GraphNode node : getNodes()) {
      node.determineStyles();
    }
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
  
  // once the graph is built, query if subject is a member of the groups
  private void queryGroupMemberships() {
    if (this.subjectForIsMemberCheck == null) {
      return;
    }

    //collect all eligible group nodes
    Map<String, GraphNode> groupNodesByUuid = new HashMap<String, GraphNode>();

    for (GraphNode node: getNodes()) {
      if (node.isGroup() && (!node.isLoaderGroup() || node.isSimpleLoaderGroup())) {
        groupNodesByUuid.put(node.getGrouperObjectId(), node);
        
        // default to false
        node.setSubjectIsMember(false);
      }
    }

    // no groups to count, don't need to continue
    if (groupNodesByUuid.size() == 0) {
      return;
    }
    
    Set<Object[]> membershipDataSet = new MembershipFinder().addSubject(this.subjectForIsMemberCheck).assignGroupIds(groupNodesByUuid.keySet()).assignField(Group.getDefaultList()).findMembershipsMembers();
    for (Object[] membershipData : membershipDataSet) {
      Group group = (Group)membershipData[1];
      GraphNode node = groupNodesByUuid.get(group.getId());
      if (node != null) {
        node.setSubjectIsMember(true);
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
      provisionToPspngAttributeDefName = AttributeDefNameFinder.findByNameAsRoot(prov_to, true);
    } catch (AttributeDefNameNotFoundException e) {
      // this is not auto created
      LOG.info("Unable to retrieve PSPNG provision_to attribute; results will not include provisioning relationships", e);
    }

    try {
      // is GroupTypeFinder using root session by default?
      sqlLoaderAttributeDefName = GroupTypeFinder.find("grouperLoader").getAttributeDefName();
    } catch (Exception e) {
      LOG.warn("Unable to retrieve attribute for sql loader jobs; groups might not be detected as loader jobs", e);
    }

    try {
      abacAttributeDefName = AttributeDefNameFinder.findByNameAsRoot(
          GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, false);
    } catch (Exception e) {
      LOG.info("Unable to retrieve ABAC jexl script marker attribute; ABAC groups will not be detected in visualization", e);
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

  /**
   * should be only useful for {@link GraphNode} nodes needing the ABAC/jexl script marker attribute
   * within the context of the user session
   *
   * @return the ABAC jexl script marker attribute def name, or null if not found
   */
  public static AttributeDefName getAbacAttributeDefName() {
    if (!attemptedInitLookupFields) {
      initLookupFields();
    }
    return abacAttributeDefName;
  }

  /**
   * Creates a graph node for a leaf ABAC reference (group, attribute, or row).
   *
   * @param ref the leaf reference
   * @param theGroup the ABAC group (for logging)
   * @return the graph node, or null if the reference could not be resolved
   */
  /**
   * Recursively adds children of an ABAC compound reference under the given parent compound node.
   * Nested compound children become nested compound nodes with their own children attached.
   */
  private void processAbacCompoundChildren(GraphNode parentCompoundNode, AbacReference parentRef,
                                           Group theGroup, long level, boolean isRecursive) {
    if (parentRef.getChildren() == null) {
      return;
    }
    for (AbacReference childRef : parentRef.getChildren()) {
      GraphNode childNode;
      if (childRef.getRefType() == AbacReference.RefType.COMPOUND) {
        boolean isCompoundAnd = "and".equals(childRef.getName());
        GrouperObjectCompoundWrapper compoundWrapper = new GrouperObjectCompoundWrapper(
            childRef.computeId(), terseNodeLabel(childRef), isCompoundAnd);
        childNode = fetchOrCreateNode(compoundWrapper);
      } else {
        childNode = createAbacLeafNode(childRef, theGroup);
      }
      if (childNode == null) {
        continue;
      }
      if (childRef.getPopulationCount() >= 0) {
        childNode.setPopulationCount((long) childRef.getPopulationCount());
      }
      if (subjectForIsMemberCheck != null) {
        childNode.setSubjectIsMember(childRef.isContainsSubject());
      }
      GraphEdge childEdge = new GraphEdge(parentCompoundNode, childNode, determineAbacEdgeStyle(childRef));
      if (!edges.contains(childEdge)) {
        edges.add(childEdge);
        childNode.setDistanceFromStartNode(level);
        visitNode(childNode, level, false, isRecursive);
        if (childRef.getChildren() != null && !childRef.getChildren().isEmpty()) {
          processAbacCompoundChildren(childNode, childRef, theGroup, level + 1, isRecursive);
        }
      }
    }
  }

  private GraphNode createAbacLeafNode(AbacReference ref, Group theGroup) {
    if (ref.getRefType() == AbacReference.RefType.GROUP) {
      if (ref.isMemberOfAny()) {
        // memberOfAny: multiple groups as a single leaf pseudo-node; let the terse renderer build the label
        GrouperObjectDataAttributeWrapper wrapper = new GrouperObjectDataAttributeWrapper(
            ref.computeId(), terseNodeLabel(ref));
        return fetchOrCreateNode(wrapper);
      }
      try {
        Group referencedGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), ref.getName(), false);
        if (referencedGroup != null && !matchesFilter(referencedGroup)) {
          return fetchOrCreateNode(referencedGroup);
        }
      } catch (Exception e) {
        LOG.debug("Failed to find ABAC referenced group " + ref.getName() + " for group " + theGroup.getName());
      }
    } else if (ref.getRefType() == AbacReference.RefType.ATTRIBUTE) {
      GrouperObjectDataAttributeWrapper wrapper = new GrouperObjectDataAttributeWrapper(
          ref.computeId(), terseNodeLabel(ref));
      return fetchOrCreateNode(wrapper);
    } else if (ref.getRefType() == AbacReference.RefType.ROW) {
      GrouperObjectDataRowWrapper wrapper = new GrouperObjectDataRowWrapper(
          ref.computeId(), terseNodeLabel(ref));
      return fetchOrCreateNode(wrapper);
    }
    return null;
  }

  /**
   * Runs the ABAC analysis on a script using a root session. Returns the analysis result
   * which contains both the flat parts list (with population counts and descriptions) and
   * the visualization reference tree (built from the same parsed AST).
   *
   * @param script the JEXL script
   * @return the analysis, or null if an error occurred
   */
  private GrouperJexlScriptAnalysis runAbacAnalysis(String script) {
    try {
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
      loadAbacFriendlyNames(grouperDataEngine);

      GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(grouperDataEngine, script, subjectForIsMemberCheck, GrouperSession.staticGrouperSession().getSubject(), true, null, true);

      if (analysis != null && analysis.getErrorMessage() != null) {
        LOG.warn("ABAC analysis returned error: " + analysis.getErrorMessage());
      }
      return analysis;
    } catch (Exception e) {
      LOG.warn("Error in root session for ABAC analysis: " + e.getMessage());
      return null;
    }
  }

  /**
   * Determines the appropriate edge StyleObjectType for an ABAC reference based on
   * its connective context (AND/OR) and negation state.
   */
  private static StyleObjectType determineAbacEdgeStyle(AbacReference ref) {
    if (ref.getConnective() == AbacReference.Connective.OR) {
      return ref.isNegated() ? StyleObjectType.EDGE_ABAC_OR_NOT : StyleObjectType.EDGE_ABAC_OR;
    } else {
      return ref.isNegated() ? StyleObjectType.EDGE_ABAC_AND_NOT : StyleObjectType.EDGE_ABAC_AND;
    }
  }

  // ===========================================================================
  // Terse, plain-language labels shown inside ABAC visualization nodes. Each node
  // shows a complete-but-compact rendering of its slice of the script. Anything the
  // renderer cannot express tersely falls back to the verbose analysis description,
  // so a node is never left blank.
  // ===========================================================================

  /**
   * Label shown inside the node's own box: a terse rendering of this reference and
   * everything beneath it.
   */
  private String terseNodeLabel(AbacReference ref) {
    if (ref == null) {
      return "";
    }
    String label;
    switch (ref.getRefType()) {
      case COMPOUND:
        label = terseCompoundLabel(ref, true);
        break;
      case ROW:
        label = terseRowLabel(ref, true);
        break;
      case ATTRIBUTE:
        label = terseAttributeLabel(ref, true);
        break;
      case GROUP:
        label = ref.isMemberOfAny() ? terseMemberOfAnyLabel(ref, true) : ref.computeDisplayLabel();
        break;
      default:
        label = ref.computeDisplayLabel();
    }
    if (StringUtils.isBlank(label)) {
      label = ref.computeDisplayLabel();
    }
    return capitalizeFirst(label);
  }

  /**
   * How a reference reads when folded into a parent box's text. A value list is capped at 5
   * with ", etc" here, but shows every value when it is drawn as its own node.
   */
  private String terseRefSummary(AbacReference ref) {
    if (ref == null) {
      return "";
    }
    switch (ref.getRefType()) {
      case COMPOUND:
        return "(" + terseCompoundLabel(ref, false) + ")";
      case ROW:
        return terseRowLabel(ref, false);
      case ATTRIBUTE:
        return terseAttributeLabel(ref, false);
      case GROUP:
        if (ref.isMemberOfAny()) {
          return terseMemberOfAnyLabel(ref, false);
        }
        return (ref.isNegated() ? "must not be in group " : "must be in group ") + StringUtils.defaultString(ref.getName());
      default:
        return ref.computeDisplayLabel();
    }
  }

  /**
   * Terse rendering of an AND/OR compound: children joined by "and" / "or".
   *
   * @param asNode true for the compound's own box. The incoming edge style already encodes the
   *   negation (e.g. "must not be in" / "any of these (not)"), so the node text drops the outer
   *   "not (...)" wrap to avoid a double-negative read of edge-plus-node. False for inline use
   *   in a parent's text where no edge carries the polarity, so the wrap stays.
   */
  private String terseCompoundLabel(AbacReference ref, boolean asNode) {
    List<AbacReference> children = ref.getChildren();
    if (children == null || children.isEmpty()) {
      return ref.computeDisplayLabel();
    }
    String joiner = "or".equals(ref.getName()) ? " or " : " and ";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < children.size(); i++) {
      if (i > 0) {
        sb.append(joiner);
      }
      sb.append(terseRefSummary(children.get(i)));
    }
    String text = sb.toString();
    if (ref.isNegated() && !asNode) {
      return "not (" + text + ")";
    }
    return text;
  }

  /**
   * Terse rendering of a data row, and of a single row-column leaf. Built by parsing the verbose
   * analysis description, which has a fixed shape; on any mismatch this returns the verbose text
   * unchanged so a node is never left garbled.
   */
  private String terseRowLabel(AbacReference ref, boolean asNode) {
    List<AbacReference> children = ref.getChildren();
    String result;
    if (children != null && !children.isEmpty()) {
      // the row node: assemble from each column child's single-attribute description
      result = terseRowFromColumns(ref.getName(), children, ref.isRowInnerOr());
    } else {
      // a single row-column leaf, or a bare row with no conditions
      result = terseSingleRowColumn(ref.computeDisplayLabel(), asNode);
    }
    if (result == null) {
      return ref.computeDisplayLabel();
    }
    // When the row has its own box (asNode), the incoming edge style already carries the
    // negation ("must not be in"), so the node text should NOT also prefix "no" — that would
    // be a double-negative read of edge-plus-node.
    if (ref.isNegated() && !asNode) {
      // "no affiliation with code staff" for inline summaries; lowercase the inner first
      // char so the prefix joins cleanly
      String inner = result.length() > 0
          ? Character.toLowerCase(result.charAt(0)) + result.substring(1) : result;
      return "no " + inner;
    }
    return result;
  }

  /**
   * Assembles the row-node label from its children: bare-presence columns become adjectives
   * before the row name, value lists render "any &lt;field&gt; in: v1, v2, ..., etc" capped at
   * 5, a single value reads "&lt;field&gt; &lt;value&gt;", and a nested AND/OR predicate is
   * rendered in parentheses. Field and row aliases are swapped for their configured friendly
   * names. Returns null if a plain column cannot be parsed.
   */
  private String terseRowFromColumns(String rowAlias, List<AbacReference> children, boolean innerIsOr) {
    List<String> flags = new ArrayList<String>();
    List<String> conditions = new ArrayList<String>();
    for (AbacReference child : children) {
      if (child.getRefType() == AbacReference.RefType.COMPOUND) {
        // a nested AND/OR inside the row predicate -- render it in parentheses
        conditions.add(terseRefSummary(child));
        continue;
      }
      String[] column = parseRowColumn(child.computeDisplayLabel());
      if (column == null) {
        return null;
      }
      if (column[1].length() == 0 && !"other".equals(column[2])) {
        continue;   // bare row with no condition; skip
      }
      String field = friendlyField(column[1]);
      if ("flag".equals(column[2])) {
        // a negated bare-flag is no longer adjective-shaped — route it through conditions as
        // "not <field>" so it reads alongside the other clauses
        if (child.isNegated()) {
          conditions.add("not " + field);
        } else {
          flags.add(field);
        }
      } else if ("list".equals(column[2])) {
        String cond = "any " + field + " in: " + cappedValueList(column[3], 5);
        conditions.add(negateAttributePhrase(cond, child.isNegated()));
      } else if ("other".equals(column[2])) {
        // operator text like "greater than 5", "matches '1.*'", or attributeCompare body —
        // use the field-is-X / field-matches-X pattern so negation reads naturally
        // ("dept is not greater than 7", not "not dept greater than 7")
        String value = applyFriendlyFieldNames(column[3]);
        conditions.add(operatorPhrase(field, value, child.isNegated()));
      } else {
        // "equals": the field followed by its value. Helper falls to a "no " prefix for the
        // negated case (e.g. "no code 'staff'") since there is no verb to negate
        String cond = field.length() == 0 ? column[3] : (field + " " + column[3]);
        conditions.add(negateAttributePhrase(cond, child.isNegated()));
      }
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < flags.size(); i++) {
      sb.append(i == 0 ? "" : ", ").append(flags.get(i));
    }
    if (!flags.isEmpty()) {
      sb.append(' ');
    }
    sb.append(friendlyRow(rowAlias));
    String siblingJoiner = innerIsOr ? " or " : " and ";
    for (int i = 0; i < conditions.size(); i++) {
      // first condition reads as "with <clause>"; subsequent conditions are siblings joined
      // with the row's inner connective (" and " for the default AND-row, " or " when the
      // inner predicate's top-level connective is OR)
      sb.append(i == 0 ? " with " : siblingJoiner).append(conditions.get(i));
    }
    return sb.toString();
  }

  /**
   * Renders a single row-column leaf. When asNode is true the leaf's own box is rendered -- a
   * bare-presence column reads as an adjective before the row name ("active affiliation"), and
   * any other column reads "&lt;row&gt; with &lt;condition&gt;". When false only the bare
   * condition is returned, for folding into a parent. Null on mismatch.
   */
  private String terseSingleRowColumn(String description, boolean asNode) {
    String[] column = parseRowColumn(description);
    if (column == null) {
      return null;
    }
    String rowName = friendlyRow(column[0]);
    if (column[1].length() == 0 && !"other".equals(column[2])) {
      return rowName;   // bare row with no condition
    }
    String field = friendlyField(column[1]);
    if ("flag".equals(column[2])) {
      // a bare-presence column reads as an adjective: "active affiliation"
      return asNode ? (field + " " + rowName) : field;
    }
    String condition;
    if ("list".equals(column[2])) {
      condition = "any " + field + " in: " + cappedValueList(column[3], Integer.MAX_VALUE);
    } else {
      // "equals" or "other": the field followed by its value / operator text. For "other" the
      // value may mention field aliases (e.g. attributeCompare) -- swap them for friendly names.
      String value = "other".equals(column[2]) ? applyFriendlyFieldNames(column[3]) : column[3];
      condition = field.length() == 0 ? value : (field + " " + value);
    }
    return asNode ? (rowName + " with " + condition) : condition;
  }

  /**
   * Parses a single-attribute "Has row 'X' with attribute 'F' ..." analysis description.
   *
   * @return {rowAlias, field, kind, value} where kind is "flag" / "list" / "equals" / "other"
   *   (operators the renderer does not special-case fall through to "other" with a lightly
   *   cleaned value), field is "" for a bare row with no condition or for an attributeCompare-
   *   style description that has no "with attribute" clause; or null only on a structural parse
   *   failure (no "Has row" prefix, missing quote, more than one attribute clause)
   */
  private static String[] parseRowColumn(String description) {
    if (StringUtils.isBlank(description)) {
      return null;
    }
    String hasRowText = textOrFallback("jexlAnalysisHasRow", "has row");
    String withAttrText = textOrFallback("jexlAnalysisHasRowAttributeValue1", "with attribute");
    String anyValueText = textOrFallback("jexlAnalysisHasRowAttributeAnyValue", "with any value:");
    String valueText = textOrFallback("jexlAnalysisHasRowAttributeValue2", "value");

    String trimmed = description.trim();
    if (trimmed.length() <= hasRowText.length()
        || !trimmed.regionMatches(true, 0, hasRowText, 0, hasRowText.length())) {
      return null;
    }
    String afterRow = trimmed.substring(hasRowText.length()).trim();
    if (!afterRow.startsWith("'")) {
      return null;
    }
    int quoteEnd = afterRow.indexOf('\'', 1);
    if (quoteEnd < 0) {
      return null;
    }
    String rowAlias = afterRow.substring(1, quoteEnd);
    String rest = afterRow.substring(quoteEnd + 1).trim();
    if (rest.length() == 0) {
      return new String[] {rowAlias, "", "", ""};
    }
    String attrMarker = withAttrText + " '";
    if (!rest.regionMatches(true, 0, attrMarker, 0, attrMarker.length())) {
      // no "with attribute" clause (e.g. attributeCompare's "row columns compare: ..."): strip
      // the known compare prefix when present, then render the cleaned remainder with no field
      String compareText = textOrFallback("jexlAnalysisAttributeCompare", "row columns compare:");
      String body = rest.regionMatches(true, 0, compareText, 0, compareText.length())
          ? rest.substring(compareText.length()).trim() : rest;
      return new String[] {rowAlias, "", "other", cleanClauseTerse(body)};
    }
    int fieldEnd = rest.indexOf('\'', attrMarker.length());
    if (fieldEnd < 0) {
      return null;
    }
    String field = rest.substring(attrMarker.length(), fieldEnd);
    String clause = rest.substring(fieldEnd + 1).trim();
    if (clause.toLowerCase().contains(attrMarker.toLowerCase())) {
      return null;   // more than one attribute -- not a single column
    }
    if (clause.length() == 0) {
      return new String[] {rowAlias, field, "flag", ""};
    }
    if (clause.regionMatches(true, 0, anyValueText, 0, anyValueText.length())) {
      // keep the surrounding quotes so cappedValueList can split robustly on "', '" -- values
      // that themselves contain a comma stay grouped correctly
      String list = clause.substring(anyValueText.length()).trim();
      return new String[] {rowAlias, field, "list", list};
    }
    if (clause.length() > valueText.length()
        && clause.regionMatches(true, 0, valueText, 0, valueText.length())
        && clause.charAt(valueText.length()) == ' ') {
      String value = clause.substring(valueText.length()).trim();
      if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
        value = value.substring(1, value.length() - 1);
      }
      return new String[] {rowAlias, field, "equals", value};
    }
    // an operator the renderer does not special-case: keep the field and a cleaned clause
    return new String[] {rowAlias, field, "other", cleanClauseTerse(clause)};
  }

  /** Returns the configured text for a key, or the supplied English fallback when blank. */
  private static String textOrFallback(String key, String fallback) {
    String text = GrouperTextContainer.textOrNull(key);
    return StringUtils.isBlank(text) ? fallback : text;
  }

  /**
   * Loads the optional friendly-name overrides for data fields and rows so the terse
   * visualization labels can show them in place of the raw aliases.
   */
  private void loadAbacFriendlyNames(GrouperDataEngine grouperDataEngine) {
    if (grouperDataEngine == null) {
      return;
    }
    for (Map.Entry<String, GrouperDataFieldConfig> entry : grouperDataEngine.getFieldConfigByAlias().entrySet()) {
      String friendlyName = entry.getValue() == null ? null : entry.getValue().getFieldFriendlyName();
      if (!StringUtils.isBlank(friendlyName)) {
        this.abacFieldFriendlyNames.put(entry.getKey(), friendlyName);
      }
    }
    for (Map.Entry<String, GrouperDataRowConfig> entry : grouperDataEngine.getRowConfigByAlias().entrySet()) {
      String friendlyName = entry.getValue() == null ? null : entry.getValue().getRowFriendlyName();
      if (!StringUtils.isBlank(friendlyName)) {
        this.abacRowFriendlyNames.put(entry.getKey(), friendlyName);
      }
    }
  }

  /** Maps a data field alias to its configured friendly name, or returns the alias unchanged. */
  private String friendlyField(String alias) {
    if (alias == null) {
      return "";
    }
    String friendlyName = this.abacFieldFriendlyNames.get(alias.toLowerCase());
    return StringUtils.isBlank(friendlyName) ? alias : friendlyName;
  }

  /** Maps a data row alias to its configured friendly name, or returns the alias unchanged. */
  private String friendlyRow(String alias) {
    if (alias == null) {
      return "";
    }
    String friendlyName = this.abacRowFriendlyNames.get(alias.toLowerCase());
    return StringUtils.isBlank(friendlyName) ? alias : friendlyName;
  }

  /**
   * Replaces every known field alias in a free-text clause with its configured friendly name.
   * Uses word boundaries so e.g. "affiliationDeptNumber" is not swapped inside the longer
   * "affiliationDeptNumberPrimary". (Comparison-operator spelling now lives in cleanClauseTerse
   * so it can be done quote-aware -- a value like '0 &lt; x' is left intact.)
   */
  private String applyFriendlyFieldNames(String text) {
    if (StringUtils.isBlank(text)) {
      return text;
    }
    String result = text;
    for (Map.Entry<String, String> entry : abacFieldFriendlyNames.entrySet()) {
      String regex = "(?i)\\b" + java.util.regex.Pattern.quote(entry.getKey()) + "\\b";
      result = result.replaceAll(regex, java.util.regex.Matcher.quoteReplacement(entry.getValue()));
    }
    return result;
  }

  /**
   * Terse rendering of an attribute condition.
   *
   * @param ownNode true for the attribute's own box (a value list shows every value); false when
   *   folded into a parent box, where a value list caps at 5 with ", etc"
   */
  private String terseAttributeLabel(AbacReference ref, boolean ownNode) {
    String field = friendlyField(ref.getName());
    if (ref.isTerseUnsupported()) {
      // an operator the structured path does not cover (like / regex / between / comparison):
      // drop the "Has attribute '<alias>'" boilerplate and lightly clean what is left
      String desc = ref.computeDisplayLabel();
      String rawAlias = ref.getName();
      if (desc != null && rawAlias != null) {
        int aliasAt = desc.indexOf("'" + rawAlias + "'");
        if (aliasAt >= 0) {
          String clause = desc.substring(aliasAt + rawAlias.length() + 2).trim();
          if (!StringUtils.isBlank(clause)) {
            // "field is greater than 7" / "field is not greater than 7" — the verb sits between
            // the field and the predicate rather than dangling "not" in front of the field.
            // When the attribute is its own box (ownNode), the edge style carries the negation,
            // so we render positively to avoid a double-negative read of edge-plus-node.
            boolean negate = ref.isNegated() && !ownNode;
            return operatorPhrase(field, cleanClauseTerse(clause), negate);
          }
        }
      }
      return desc;
    }
    List<String> values = ref.getAttributeValues();
    String text;
    if (ref.isAttributeNullCheck()) {
      text = field + " is empty";
    } else if (values == null || values.isEmpty()) {
      // a bare presence check reads as just the attribute name, e.g. "active"
      text = field;
    } else if (values.size() == 1) {
      text = field + " is " + terseValue(values.get(0));
    } else {
      // multiple values: list them -- the leaf box shows all, a non-leaf summary caps at 5 with "etc"
      int max = ownNode ? values.size() : 5;
      int n = Math.min(values.size(), max);
      StringBuilder sb = new StringBuilder("any ").append(field).append(" in: ");
      for (int i = 0; i < n; i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(terseValue(values.get(i)));
      }
      if (values.size() > n) {
        sb.append(", etc");
      }
      text = sb.toString();
    }
    // when this is the attribute's own box, the edge style already carries the negation —
    // pass false so the phrase reads positively and avoids a double-negative read
    return negateAttributePhrase(text, ref.isNegated() && !ownNode);
  }

  /**
   * English-friendly negation for an attribute phrase. Negates the verb when the phrase
   * contains " is " ("field is X" -> "field is not X", "field is empty" -> "field is not
   * empty"); prefixes "no " for a bare-presence noun ("MFA" -> "no MFA") or a value-list
   * ("any field in: ..." -> "no field in: ...").
   */
  private static String negateAttributePhrase(String text, boolean negated) {
    if (!negated || text == null || text.length() == 0) {
      return text;
    }
    int isAt = text.indexOf(" is ");
    if (isAt >= 0) {
      return text.substring(0, isAt) + " is not " + text.substring(isAt + " is ".length());
    }
    if (text.startsWith("any ")) {
      return "no " + text.substring("any ".length());
    }
    return "no " + text;
  }

  /**
   * Joins a field name with an operator-shaped clause from cleanClauseTerse. Uses the
   * "field is X" pattern for adjective/preposition clauses (greater than, less than,
   * between, like, ...) — negation becomes "field is not X". Switches to "field matches X"
   * / "field does not match X" when the clause already starts with a verb (the regex case,
   * which cleanClauseTerse maps from "regex" to "matches"). When the field is empty
   * (attributeCompare-style clauses) the clause stands alone with an optional "not " prefix.
   */
  private static String operatorPhrase(String field, String clause, boolean negated) {
    if (clause == null || clause.isEmpty()) {
      return field == null ? "" : field;
    }
    if (field == null || field.isEmpty()) {
      return negated ? ("not " + clause) : clause;
    }
    if (clause.startsWith("matches")) {
      // verb-shaped clause: "matches '<pattern>'" -> negation is "does not match '<pattern>'"
      return negated
          ? field + " does not match" + clause.substring("matches".length())
          : field + " " + clause;
    }
    return field + " is " + (negated ? "not " : "") + clause;
  }

  /**
   * Renders a memberOfAny GROUP ref. The leaf box lists every group; a non-leaf summary caps at
   * 5 with ", etc". For other group operators (recentMemberOf, etc.) the verbose description is
   * lightly cleaned and returned instead.
   */
  private String terseMemberOfAnyLabel(AbacReference ref, boolean asNode) {
    if (ref.isTerseUnsupported()) {
      // e.g. recentMemberOf -- the verbose description already names the group and the time period
      String text = ref.computeDisplayLabel();
      if (StringUtils.isBlank(text)) {
        return text;
      }
      text = stripBoundaryQuotes(text);
      if (Character.isUpperCase(text.charAt(0))) {
        text = Character.toLowerCase(text.charAt(0)) + text.substring(1);
      }
      return text;
    }
    List<String> groups = ref.getAttributeValues();
    if (groups == null || groups.isEmpty()) {
      return ref.computeDisplayLabel();
    }
    int max = asNode ? groups.size() : 5;
    int n = Math.min(groups.size(), max);
    // when this memberOfAny is its own box (asNode), the incoming edge already says
    // "must (not) be in any of these", so the box drops the verb-phrase prefix entirely
    // and just lists the groups. For inline summaries (asNode=false) the prefix stays
    // because no edge carries the relation.
    StringBuilder sb = new StringBuilder();
    if (!asNode) {
      sb.append(ref.isNegated() ? "must not be in any group: " : "must be in any group: ");
    }
    for (int i = 0; i < n; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(groups.get(i));
    }
    if (groups.size() > n) {
      sb.append(", etc");
    }
    return sb.toString();
  }

  /** Quotes a value only when it contains a space or comma. */
  private static String terseValue(String value) {
    if (value == null) {
      return "";
    }
    return (value.indexOf(' ') >= 0 || value.indexOf(',') >= 0) ? ("'" + value + "'") : value;
  }

  /**
   * Strips single-quote chars that wrap values -- i.e. quotes adjacent to a boundary
   * (whitespace, comma, start-of-string, or end-of-string). An inner quote sitting between
   * two non-boundary chars (like the apostrophe in "O'Brien") is preserved.
   */
  private static String stripBoundaryQuotes(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    StringBuilder sb = new StringBuilder(text.length());
    int last = text.length() - 1;
    for (int i = 0; i <= last; i++) {
      char c = text.charAt(i);
      if (c == '\'') {
        boolean leftBoundary = (i == 0) || Character.isWhitespace(text.charAt(i - 1)) || text.charAt(i - 1) == ',';
        boolean rightBoundary = (i == last) || Character.isWhitespace(text.charAt(i + 1)) || text.charAt(i + 1) == ',';
        if (leftBoundary || rightBoundary) {
          continue;
        }
      }
      sb.append(c);
    }
    return sb.toString();
  }

  /** Upper-cases the first character of a visualization label. */
  private static String capitalizeFirst(String text) {
    if (StringUtils.isBlank(text)) {
      return text;
    }
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }

  /**
   * Spells comparison operators in English (" &lt; " -&gt; " less than ", etc.). Preserves the
   * caller's leading and trailing whitespace by padding with one space on each side before
   * substitution and stripping exactly that padding back off.
   */
  private static String spellOperators(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    String substituted = (" " + text + " ")
        .replace(" >= ", " greater than or equal to ")
        .replace(" <= ", " less than or equal to ")
        .replace(" > ", " greater than ")
        .replace(" < ", " less than ")
        .replace(" == ", " equals ")
        .replace(" != ", " not equal to ");
    return substituted.substring(1, substituted.length() - 1);
  }

  /**
   * Spells comparison operators in English only in the regions outside single-quoted values, so
   * a literal value like '0 &lt; x' is left intact.
   */
  private static String spellOperatorsOutsideQuotes(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    StringBuilder result = new StringBuilder();
    StringBuilder outside = new StringBuilder();
    boolean inQuote = false;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '\'') {
        if (!inQuote) {
          result.append(spellOperators(outside.toString()));
          outside.setLength(0);
        }
        result.append(c);
        inQuote = !inQuote;
      } else if (inQuote) {
        result.append(c);
      } else {
        outside.append(c);
      }
    }
    if (outside.length() > 0) {
      result.append(spellOperators(outside.toString()));
    }
    return result.toString();
  }

  /**
   * Lightly cleans an operator clause (the text after the field) for an operator the renderer
   * does not special-case. Spells comparison operators in English outside any quoted values (so
   * a value containing "&lt;" or "&gt;" survives), drops the "with"/"value"/"values" filler
   * words and any ":" and surrounding quotes, and wraps any structural "and"/"or" in
   * non-breaking spaces so the JS label-wrapper does not split inside e.g. a between's
   * "10 and 20". Examples: "less than value '300'" -&gt; "less than 300",
   * "with value like: '%2%'" -&gt; "like %2%", "between values '10' and '20'" -&gt; "between 10 and 20".
   */
  private static String cleanClauseTerse(String clause) {
    if (StringUtils.isBlank(clause)) {
      return "";
    }
    // spell operators before stripping quotes so a value like '0 < x' is not mangled
    String spelled = spellOperatorsOutsideQuotes(clause);
    int quoteAt = spelled.indexOf('\'');
    String phrase = quoteAt < 0 ? spelled : spelled.substring(0, quoteAt);
    String values = quoteAt < 0 ? "" : stripBoundaryQuotes(spelled.substring(quoteAt)).trim();
    StringBuilder sb = new StringBuilder();
    for (String token : phrase.trim().split("\\s+")) {
      String word = token.replace(":", "");
      if (word.length() == 0 || word.equalsIgnoreCase("with")
          || word.equalsIgnoreCase("value") || word.equalsIgnoreCase("values")) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(word);
    }
    if (values.length() > 0) {
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(values);
    }
    // rewrite "regex" to the verb "matches" so operatorPhrase can use the natural English
    // negation ("does not match") rather than dangling "is" in front of a noun ("is regex")
    String cleaned = sb.toString().replaceAll("(?i)\\bregex\\b", "matches");
    // non-breaking spaces around any structural "and"/"or" so the JS hardSplit does not break a
    // line inside e.g. a between's "10 and 20" -- compound joiners still use plain spaces
    return cleaned.replace(" and ", "\u00A0and\u00A0").replace(" or ", "\u00A0or\u00A0");
  }

  /**
   * Splits a value list into up to max values, joins them with ", " and appends ", etc" when
   * more follow. Quoted lists ("'a', 'b', 'c, d'") split on the quoted boundary "', '" so a
   * value that itself contains a comma stays whole; unquoted lists (numeric / timestamp values
   * the analyzer renders without quotes, e.g. "100, 200, 300") split on plain ", ". Values
   * containing a space or comma are re-quoted in the output so the list reads unambiguously.
   */
  private static String cappedValueList(String valueList, int max) {
    if (valueList == null || valueList.isEmpty()) {
      return "";
    }
    String[] parts;
    if (valueList.length() >= 2 && valueList.startsWith("'") && valueList.endsWith("'")) {
      // quoted list -- strip outer quotes, split on the quoted boundary so commas inside values stay
      parts = valueList.substring(1, valueList.length() - 1).split("', '", -1);
    } else {
      // unquoted list (e.g. numeric values) -- split on the plain comma separator
      parts = valueList.split(", ", -1);
    }
    StringBuilder sb = new StringBuilder();
    int n = Math.min(parts.length, max);
    for (int i = 0; i < n; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(terseValue(parts[i]));
    }
    if (parts.length > n) {
      sb.append(", etc");
    }
    return sb.toString();
  }



}
