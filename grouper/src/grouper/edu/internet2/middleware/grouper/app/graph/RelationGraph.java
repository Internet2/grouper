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

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectSubjectWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import org.apache.commons.logging.Log;

import java.util.*;
import java.util.regex.Pattern;

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

  private static final Log LOG = GrouperUtil.getLog(RelationGraph.class);
  public static final int RECURSIVE_LEVEL_LIMIT = 100;

  // Class parameters for finding immediate g:gsa members and PSPNG provisioners
  private static Set<Source> grouperGSASources;
  private static Field grouperMemberField;
  private static AttributeDefName provisionToAttributeDefName;
  private static String loaderGroupIdAttrDefNameId;

  private GrouperSession session;

  /* assignX settings for graph construction */
  private GrouperObject startObject;
  private long parentLevels = -1;
  private long childLevels = -1;
  private boolean showMemberCounts = true;
  private boolean showLoaderJobs = true;
  private boolean showProvisionTargets = true;
  private boolean showStems = true;
  private Set<String> skipFolderNamePatterns = new HashSet<String>(Arrays.asList("^etc:.*", "^$"));  // default to skip etc:* and root object
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
  private long numMembers;
  private Set<GraphNode> leafParentNodes;
  private Set<GraphNode> leafChildNodes;

  /**
   * Create a new graph with default settings. Caller should call the various
   * assign methods to set build parameters, and then call build() to construct
   * the graph.
   *
   * @param session The Grouper session
   */
  public RelationGraph(GrouperSession session) {
    this.session = session;
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
   * flags whether to count memberships for groups
   *
   * @param theShowMemberCounts whether to count memberships for groups
   * @return
   */
  public RelationGraph assignShowMemberCounts(boolean theShowMemberCounts) {
    this.showMemberCounts = theShowMemberCounts;
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
   * @see #assignShowMemberCounts(boolean)
   * @return if memberships are counted for groups
   */
  public boolean isShowMemberCounts() {
    return showMemberCounts;
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
  public long getNumMembers() {
    return numMembers;
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

      if (isShowMemberCounts() && node.isGroup() && !node.isLoaderGroup()) {
        long count = fetchGroupCount((Group) node.getGrouperObject());
        node.setMemberCount(count);
        numMembers += count;
      }
    }

    return node;
  }

  // return the immediate groups that are members for this group
  private Set<Member> fetchImmediateGsaMembers(Group g) {
    // init the g:gsa source if not set
    if (grouperGSASources == null) {
      grouperGSASources = Collections.singleton(SubjectFinder.internal_getGSA());
    }
    if (grouperMemberField == null) {
      grouperMemberField = FieldFinder.find("members", true);
    }

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
    Set<AttributeAssign> attrAssigns = g.getAttributeDelegate().retrieveAssignmentsByAttributeDef(GrouperCheckConfig.loaderMetadataStemName() + ":loaderMetadataDef");
    if (attrAssigns == null || attrAssigns.size() == 0) {
      return Collections.emptyList();
    }

    List<Group> ret = new LinkedList<Group>();

    ++numGroupsFromLoaders;
    for (AttributeAssign aa: attrAssigns) {
      String jobId = aa.getAttributeValueDelegate().retrieveValueString(GrouperCheckConfig.loaderMetadataStemName() + ":" + GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_GROUP_ID);
      try {
        Group jobGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), jobId, true);
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
    if (loaderGroupIdAttrDefNameId == null) {
      loaderGroupIdAttrDefNameId = AttributeDefNameFinder.findByName(
        GrouperCheckConfig.loaderMetadataStemName() + ":" + GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_GROUP_ID, true
      ).getId();
    }

    return new GroupFinder()
      .assignIdOfAttributeDefName(loaderGroupIdAttrDefNameId)
      .assignAttributeValuesOnAssignment(GrouperUtil.toSetObjectType(group.getId()))
      .findGroups();
  }
  /*
   * return the PSPNG provisioning targets (as GrouperObject wrappers) for this group,
   * as seen in the provision_to attribute
   */
  private List<GrouperObjectProvisionerWrapper> fetchProvisioners(Group g) {
    // Set up static AttributeDefName for provision_to attribute. Will throw error if
    // the provision_to attribute not set up. Caller should unset
    // assignShowProvisionTargets to avoid this so the build can still run
    if (provisionToAttributeDefName == null) {
      String prov_to = GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":pspng:provision_to";
      provisionToAttributeDefName = AttributeDefNameFinder.findByName(prov_to, true);
    }

    Set<AttributeAssign> attrAssigns = g.getAttributeDelegate().retrieveAssignments(provisionToAttributeDefName);
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
  private long fetchGroupCount(Group g) {
    QueryOptions q = new QueryOptions().retrieveResults(false).retrieveCount(true);
    new MembershipFinder().addGroup(g).assignField(grouperMemberField).assignQueryOptionsForMember(q).findMembershipsMembers();
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

  // recursively walk parents of this node -- includes parent stem, group loaders, group members, and composites
  private void buildParentNodes(GraphNode toNode, long level) {
    if (this.parentLevels != -1 && level > this.parentLevels) {
      return;
    }

    if (level > RECURSIVE_LEVEL_LIMIT) {
      String msg = "Reached max recursive limit of levels (" + RECURSIVE_LEVEL_LIMIT + ") while building relationship graph";
      LOG.error(msg);
      throw new RuntimeException(msg);
    }

    Set<GraphNode> nodesToVisit = new HashSet<GraphNode>();

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

    // for a group, get immediate g:gsa members as parents
    if (toNode.isGroup()) {
      Group theGroup = (Group)(toNode.getGrouperObject());
      long numMembersAdded = 0;
      for (Member m : fetchImmediateGsaMembers(theGroup)) {
        Group parentGroup = m.toGroup();
        if (getMaxSiblings() > 0 && numMembersAdded >= getMaxSiblings()) {
          skippedGroups.add(parentGroup);
        } else if (!matchesFilter(parentGroup)) {
          GraphNode parentNode = fetchOrCreateNode(parentGroup);
          nodesToVisit.add(parentNode);
          ++numMembersAdded;
        }
      }

      if (showLoaderJobs) {
        List<Group> jobGroups = fetchLoaderJobs(theGroup);
        for (Group jobGroup: jobGroups) {
          if (!matchesFilter(jobGroup)) {
            GraphNode jobNode = fetchOrCreateNode(jobGroup);
            //addEdge(jobNode, toNode);
            nodesToVisit.add(jobNode);
          }
        }
      }

      // show composite factors
      if (theGroup.hasComposite()) {
        Group left = theGroup.getComposite(true).getLeftGroup();
        if (!matchesFilter(left)) {
          //addEdge(left, toNode);
          nodesToVisit.add(fetchOrCreateNode(left));
        }
        Group right = theGroup.getComposite(true).getRightGroup();
        if (!matchesFilter(right)) {
          //addEdge(right, toNode);
          nodesToVisit.add(fetchOrCreateNode(right));
        }
      }
    }

    if (nodesToVisit.size() == 0) {
      leafParentNodes.add(toNode);
    } else {
      if (level > maxParentDistance) {
        maxParentDistance = level;
      }
      for (GraphNode n : nodesToVisit) {
        addEdge(n, toNode);
        n.setDistanceFromStartNode(-1 * level);
        visitNode(n, level, true, false);
      }
    }
  }

  // recursively walk child nodes -- includes stem's child groups, group/subject direct memberships, provisioners
  private void buildChildNodes(GraphNode fromNode, long level) {
    if (this.childLevels != -1 && level > this.childLevels) {
      return;
    }

    if (level > RECURSIVE_LEVEL_LIMIT) {
      String msg = "Reached max recursive limit of levels (" + RECURSIVE_LEVEL_LIMIT + ") while building relationship graph";
      LOG.error(msg);
      throw new RuntimeException(msg);
    }

    Set<GraphNode> nodesToVisit = new HashSet<GraphNode>();

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
          ++numGroupsFromLoaders;
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
    }
    else if (fromNode.isGroup()) {
      Group theGroup = (Group)(fromNode.getGrouperObject());

      // for groups, find groups having this as a direct member
      Set<MembershipSubjectContainer> memberships = fetchImmediateMemberships(fromNode);
      long numMembershipsAdded = 0;
      for (MembershipSubjectContainer msc: memberships) {
        Group toGroup = msc.getGroupOwner();
        if (toGroup == null) {
          continue;
        }
        if (getMaxSiblings() > 0 && numMembershipsAdded >= getMaxSiblings()) {
          skippedGroups.add(toGroup);
        } else if (!matchesFilter(toGroup)) {
          GraphNode toNode = fetchOrCreateNode(toGroup);
          nodesToVisit.add(toNode);
          ++numMembershipsAdded;
        }
      }

      // get provisioners
      if (showProvisionTargets) {
        List<GrouperObjectProvisionerWrapper> provTargets = fetchProvisioners(theGroup);
        for (GrouperObjectProvisionerWrapper p: provTargets) {
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
          nodesToVisit.add(childNode);
          ++numLoadedGroupsByJob;
        }
      }
    } else if (fromNode.isSubject()) {
      Set<MembershipSubjectContainer> memberships = fetchImmediateMemberships(fromNode);
      long numSubjectMembershipsAdded = 0;
      for (MembershipSubjectContainer msc: memberships) {
        Group toGroup = msc.getGroupOwner();
        if (toGroup == null) {
          continue;
        }
        if (getMaxSiblings() > 0 && numSubjectMembershipsAdded >= getMaxSiblings()) {
          skippedGroups.add(toGroup);
        } else if (!matchesFilter(toGroup)) {
          GraphNode toNode = fetchOrCreateNode(toGroup);
          nodesToVisit.add(toNode);
        }
      }
    }

    if (nodesToVisit.size() == 0) {
      leafChildNodes.add(fromNode);
    } else {
      if (level > maxChildDistance) {
        maxChildDistance = level;
      }

      for (GraphNode n : nodesToVisit) {
        //visitNode(n, level);
        //buildChildNodes(n, 1 + level);
        n.setDistanceFromStartNode(level);
        addEdge(fromNode, n);
        visitNode(n, level, false, true);
      }
    }
  }

  private void visitNode(GraphNode node, long level, boolean includeParents, boolean includeChildren) {
    if (node.isVisited()) {
      return;
    }

    if (node.isSubject()) {
      //subjects don't have parents, so mark as skip it right away
      node.setVisitedParents(true);
    }

    if (includeParents && !node.isVisitedParents()) {
      //Get parents recursively
      buildParentNodes(node, 1 + level);
      node.setVisitedParents(true);
    }

    if (includeChildren && !node.isVisitedChildren()) {
      //Get children recursively
      buildChildNodes(node, 1 + level);
      node.setVisitedChildren(true);
    }
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
    numMembers = 0;
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
      + "show member counts=" + isShowMemberCounts() + ", "
      + "folder pattern filters=" + GrouperUtil.join(skipFolderNamePatterns.toArray(), "; "));

    startNode = fetchOrCreateNode(startObject);
    startNode.setStartNode(true);

    // always put the start node, even if that type is skipped
    objectToNodeMap.put(startObject, startNode);

    visitNode(startNode, 0, true, true);

    // if starting with a stem, also visit the parents of its child groups
    if (startNode.isStem()) {
      for (Group g: ((Stem)startNode.getGrouperObject()).getChildGroups(Stem.Scope.ONE)) {
        visitNode(fetchOrCreateNode(g), -1, true, false);
      }

    }

    LOG.debug("Graph completed build; nodes = " + objectToNodeMap.size() + "; edges = " + edges.size());

    for (GraphEdge e : edges) {
      e.getFromNode().addChildNode(e.getToNode());
      e.getToNode().addParentNode(e.getFromNode());
    }
  }
}
