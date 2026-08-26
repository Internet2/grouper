/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.graph;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;

/**
 * Tests around composite factor groups that the caller is not allowed to VIEW.
 *
 * A user can be allowed to see a composite owner group (and even ADMIN it) while not being
 * allowed to VIEW one of its factor groups.  Composite.getLeftGroup()/getRightGroup()/getOwnerGroup()
 * throw GroupNotFoundException in that case, so every caller has to cope with it.  RelationGraph
 * used to resolve both factor groups just to decide which side the current group was on, which blew
 * up the whole graph for those users.
 */
public class RelationGraphCompositePrivilegeTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RelationGraphCompositePrivilegeTest("testRelationGraphWhenOtherFactorNotViewable"));
  }

  /**
   * @param name
   */
  public RelationGraphCompositePrivilegeTest(String name) {
    super(name);
  }

  /** owner group of the composite, viewable by the test subject */
  private Group ownerGroup;

  /** left factor, NOT viewable by the test subject */
  private Group leftGroup;

  /** right factor, viewable by the test subject */
  private Group rightGroup;

  /**
   * set up an intersection composite where the test subject can see the owner and the right
   * factor, but not the left factor
   */
  private void setupComposite(CompositeType compositeType) {

    GrouperSession rootSession = GrouperSession.startRootSession();

    this.ownerGroup = new GroupSave(rootSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:compositePrivsOwner").save();
    this.leftGroup = new GroupSave(rootSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:compositePrivsLeft").save();
    this.rightGroup = new GroupSave(rootSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:compositePrivsRight").save();

    this.ownerGroup.addCompositeMember(compositeType, this.leftGroup, this.rightGroup);

    // hide the left factor from everyone, so the test subject cannot VIEW it
    this.leftGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    this.leftGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

    // the test subject can see the owner and the right factor
    this.ownerGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    this.rightGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * the left factor really is hidden from the test subject, and resolving it through the composite
   * throws.  this is the behavior every caller of Composite.getLeftGroup() has to handle
   */
  public void testCompositeFactorNotViewableThrows() {

    setupComposite(CompositeType.INTERSECTION);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);

    try {

      assertNull("left factor should not be viewable by the test subject",
          GroupFinder.findByName(session, "test:compositePrivsLeft", false));

      Group theOwnerGroup = GroupFinder.findByName(session, "test:compositePrivsOwner", true);

      try {
        theOwnerGroup.getComposite(true).getLeftGroup();
        fail("should not be able to resolve a factor group the subject cannot VIEW");
      } catch (GroupNotFoundException gnfe) {
        //expected
      }

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * building the relation graph from the right factor should not blow up just because the caller
   * cannot VIEW the left factor.  before the fix this threw GroupNotFoundException out of
   * buildParentNodes()
   */
  public void testRelationGraphWhenOtherFactorNotViewable() {

    setupComposite(CompositeType.INTERSECTION);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);

    try {

      Group theRightGroup = GroupFinder.findByName(session, "test:compositePrivsRight", true);

      RelationGraph relationGraph = new RelationGraph();
      relationGraph.assignStartObject(theRightGroup);
      //the composite owner is a parent of this group, so we need to walk up
      relationGraph.assignParentLevels(1);
      relationGraph.assignChildLevels(0);
      relationGraph.assignShowStems(false);

      relationGraph.build();

      assertNotNull("graph should have been built", relationGraph.getStartNode());

      //the composite owner is viewable, so it should have made it into the graph
      boolean foundOwner = false;
      for (GraphNode graphNode : relationGraph.getNodes()) {
        if (graphNode.getGrouperObject() != null
            && this.ownerGroup.getName().equals(graphNode.getGrouperObject().getName())) {
          foundOwner = true;
        }
      }
      assertTrue("composite owner should be in the graph", foundOwner);

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * same as above but for a complement (minus) composite, which takes the other branch in
   * buildParentNodes()
   */
  public void testRelationGraphComplementWhenOtherFactorNotViewable() {

    setupComposite(CompositeType.COMPLEMENT);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);

    try {

      Group theRightGroup = GroupFinder.findByName(session, "test:compositePrivsRight", true);

      RelationGraph relationGraph = new RelationGraph();
      relationGraph.assignStartObject(theRightGroup);
      relationGraph.assignParentLevels(1);
      relationGraph.assignChildLevels(0);
      relationGraph.assignShowStems(false);

      relationGraph.build();

      assertNotNull("graph should have been built", relationGraph.getStartNode());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

}
