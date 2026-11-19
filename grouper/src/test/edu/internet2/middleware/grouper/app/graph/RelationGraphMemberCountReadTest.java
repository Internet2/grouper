/**
 * Copyright 2026 Internet2
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

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;

/**
 * The relation graph (UI visualization) must only compute membership counts for groups the
 * caller can READ.  VIEW puts a group in the graph, but its total and direct member counts are
 * membership-derived data, like the group summary, which needs READ.
 *
 * Before the fix the VIEW-only group's counts were filled in by a raw HQL aggregate and summed
 * into the graph totals.
 */
public class RelationGraphMemberCountReadTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RelationGraphMemberCountReadTest("testMemberCountsNeedRead"));
  }

  /**
   * @param name
   */
  public RelationGraphMemberCountReadTest(String name) {
    super(name);
  }

  /** group the test subject can only VIEW; 2 direct members (SUBJ2, SUBJ3) */
  private Group viewOnlyGroup;

  /** group the test subject can READ; direct members SUBJ4 and viewOnlyGroup */
  private Group readableGroup;

  // ---------------------------------------------------------------- setup

  /**
   * As root: test:viewOnly (2 members, SUBJ0 has VIEW only) is a member of test:readable
   * (SUBJ4 as the other direct member, SUBJ0 has READ).  GrouperAll gets nothing on either.
   */
  private void setupGroups() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    this.viewOnlyGroup = new GroupSave(rootSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:viewOnly").save();
    this.readableGroup = new GroupSave(rootSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:readable").save();

    this.viewOnlyGroup.addMember(SubjectTestHelper.SUBJ2, false);
    this.viewOnlyGroup.addMember(SubjectTestHelper.SUBJ3, false);

    this.readableGroup.addMember(SubjectTestHelper.SUBJ4, false);
    this.readableGroup.addMember(this.viewOnlyGroup.toSubject(), false);

    // make sure the default GrouperAll grants (config dependent) do not give READ
    for (Group group : new Group[] {this.viewOnlyGroup, this.readableGroup}) {
      group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    }

    this.viewOnlyGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    this.readableGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * build a graph from the given group, with both kinds of counts and groups counted as members
   */
  private RelationGraph buildGraph(Group startGroup) {
    RelationGraph relationGraph = new RelationGraph();
    relationGraph.assignStartObject(startGroup);
    relationGraph.assignParentLevels(1);
    relationGraph.assignChildLevels(1);
    relationGraph.assignShowStems(false);
    relationGraph.assignShowAllMemberCounts(true);
    relationGraph.assignShowDirectMemberCounts(true);
    relationGraph.assignIncludeGroupsInMemberCounts(true);
    relationGraph.build();
    return relationGraph;
  }

  /**
   * @return the node for the group with this name, fail if not in the graph
   */
  private GraphNode findNode(RelationGraph relationGraph, String groupName) {
    for (GraphNode graphNode : relationGraph.getNodes()) {
      if (graphNode.isGroup() && graphNode.getGrouperObject() != null
          && groupName.equals(graphNode.getGrouperObject().getName())) {
        return graphNode;
      }
    }
    fail("group not in graph: " + groupName);
    return null;
  }

  // ---------------------------------------------------------------- tests

  /**
   * readable start group with a VIEW-only child: only the readable group gets counts, and only
   * its counts are in the graph totals
   */
  public void testMemberCountsNeedRead() {

    setupGroups();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);

    try {

      // precondition: SUBJ0 can see but not read the view-only group
      Group theViewOnly = GroupFinder.findByName(session, "test:viewOnly", true);
      assertFalse(theViewOnly.canHavePrivilege(SubjectTestHelper.SUBJ0, AccessPrivilege.READ.getName(), false));

      Group theReadable = GroupFinder.findByName(session, "test:readable", true);
      RelationGraph relationGraph = buildGraph(theReadable);

      // readable group: direct = SUBJ4 + viewOnly group; all = those plus SUBJ2, SUBJ3
      GraphNode readableNode = findNode(relationGraph, "test:readable");
      assertEquals(2, readableNode.getDirectMemberCount());
      assertEquals(4, readableNode.getAllMemberCount());

      // view-only group is in the graph, but no counts (was 2 / 2 before the fix)
      GraphNode viewOnlyNode = findNode(relationGraph, "test:viewOnly");
      assertEquals(0, viewOnlyNode.getDirectMemberCount());
      assertEquals(0, viewOnlyNode.getAllMemberCount());

      // graph totals only include the readable group (were 6 / 4 before the fix)
      assertEquals(4, relationGraph.getTotalMemberCount());
      assertEquals(2, relationGraph.getDirectMemberCount());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * VIEW-only start group: no counts on the start node and none in the totals
   */
  public void testViewOnlyStartGroupHasNoCounts() {

    setupGroups();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);

    try {

      Group theViewOnly = GroupFinder.findByName(session, "test:viewOnly", true);
      RelationGraph relationGraph = buildGraph(theViewOnly);

      GraphNode startNode = relationGraph.getStartNode();
      assertEquals(0, startNode.getDirectMemberCount());
      assertEquals(0, startNode.getAllMemberCount());

      // the readable parent is in the graph and keeps its counts
      GraphNode readableNode = findNode(relationGraph, "test:readable");
      assertEquals(2, readableNode.getDirectMemberCount());
      assertEquals(4, readableNode.getAllMemberCount());

      assertEquals(4, relationGraph.getTotalMemberCount());
      assertEquals(2, relationGraph.getDirectMemberCount());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * control: root sees every count
   */
  public void testRootSeesAllCounts() {

    setupGroups();

    GrouperSession session = GrouperSession.startRootSession();

    try {

      Group theReadable = GroupFinder.findByName(session, "test:readable", true);
      RelationGraph relationGraph = buildGraph(theReadable);

      GraphNode viewOnlyNode = findNode(relationGraph, "test:viewOnly");
      assertEquals(2, viewOnlyNode.getDirectMemberCount());
      assertEquals(2, viewOnlyNode.getAllMemberCount());

      assertEquals(6, relationGraph.getTotalMemberCount());
      assertEquals(4, relationGraph.getDirectMemberCount());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * the helper fails closed with no session and ignores null / empty input
   */
  public void testReadableGroupIdsEdgeCases() {
    assertEquals(0, RelationGraphReadableGroups.readableGroupIds(null).size());
    assertEquals(0, RelationGraphReadableGroups.readableGroupIds(new java.util.ArrayList<GraphNode>()).size());
  }
}
