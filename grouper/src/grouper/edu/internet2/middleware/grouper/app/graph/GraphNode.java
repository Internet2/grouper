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

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.app.visualization.StyleObjectType;
import edu.internet2.middleware.grouper.app.visualization.VisualStyle;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectSubjectWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * A GraphNode is a vertex of the directed graph. It holds a single {link GrouperObject}
 * entity, and exposes that object's hashCode and equals methods, so that GraphNodes can
 * be added to Sets without duplicate objects. This class has facilities for keeping track
 * of ancillary data, such as parent nodes and child nodes, the node's distance from the
 * start node, member count, and flags for whether the node's parents and children
 * have been visited. Maintaining this data is optional, and is up to the caller to
 * set them meaningfully.
 */
public class GraphNode {

  //todo ?? add a getName here so the root folder can default to something besides blank
  private static AttributeDefName sqlLoaderAttributeDefName;
  private static AttributeDefName ldapLoaderAttributeDefName;

  private GrouperObject grouperObject;
  private long memberCount;

  private boolean stem;
  private boolean group;
  private boolean subject;
  private boolean loaderGroup;
  private boolean provisionerTarget;
  private boolean intersectGroup;
  private boolean complementGroup;
  private boolean startNode;
  private StyleObjectType styleObjectType;

  private boolean visitedParents;
  private boolean visitedChildren;

  private long distanceFromStartNode;
  private Set<GraphNode> parentNodes;
  private Set<GraphNode> childNodes;


  /**
   * Constructor that also marks the node as the starting node for the graph
   *
   * @param grouperObject
   * @param isStartNode
   */
  public GraphNode(GrouperObject grouperObject, boolean isStartNode) {
    startNode = isStartNode;
    this.grouperObject = grouperObject;

    parentNodes = new HashSet<GraphNode>();
    childNodes = new HashSet<GraphNode>();

    determineObjectTypes();
  }

  /**
   * General constructor for a GraphNode containing a grouper Object.
   *
   * @param grouperObject
   */
  public GraphNode(GrouperObject grouperObject) {
    this(grouperObject, false);
  }

  /**
   * Compares two nodes and considers them equal if they both hold the same GrouperObject.
   *
   * @param obj GraphNode to compare
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GraphNode other = (GraphNode) obj;

    return new EqualsBuilder()
      .append(this.grouperObject, other.grouperObject)
      .isEquals();
  }

  /**
   * Computes the hashCode based on the contained GrouperObject
   *
   * @return
   */
  @Override
  public int hashCode() {
      return new HashCodeBuilder()
              .append( this.grouperObject)
              .toHashCode();
  }

  private void determineObjectTypes() {

    // reset the statuses and recalculate
    this.stem = false;
    this.group = false;
    this.subject = false;
    this.loaderGroup = false;
    this.intersectGroup = false;
    this.complementGroup = false;
    this.provisionerTarget = false;

    if (grouperObject instanceof Group) {
      Group theGroup = (Group)grouperObject;
      this.group = true;

      // on first run, initialize the singletons for the AttributeDefName lookups
      if (sqlLoaderAttributeDefName == null) {
        sqlLoaderAttributeDefName = GroupTypeFinder.find("grouperLoader").getAttributeDefName();
      }
      if (ldapLoaderAttributeDefName == null) {
        ldapLoaderAttributeDefName = LoaderLdapUtils.grouperLoaderLdapAttributeDefName(true);
      }

      // is it a sql loader job?
      if (theGroup.getAttributeDelegate().retrieveAssignment(null, sqlLoaderAttributeDefName, false, false)!=null) {
        this.loaderGroup = true;
      }
      // is an ldap loader job?
      else if (theGroup.getAttributeDelegate().retrieveAssignment(null, ldapLoaderAttributeDefName, false, false)!=null) {
        this.loaderGroup = true;
      }

      // is it an intersect/complement group?
      if (theGroup.hasComposite()) {
        Composite composite = theGroup.getComposite(true);
        if (composite.getType().equals(CompositeType.COMPLEMENT)) {
          this.complementGroup = true;
        } else if (composite.getType().equals(CompositeType.INTERSECTION)) {
          this.intersectGroup = true;
        }
      }

    }

    if (grouperObject instanceof Stem) {
      this.stem = true;
    }

    if (grouperObject instanceof GrouperObjectSubjectWrapper) {
      this.subject = true;
    }

    if (grouperObject instanceof GrouperObjectProvisionerWrapper) {
      this.provisionerTarget = true;
    }

    // determine the VisualStyle object type this maps to
    if (loaderGroup) {
      styleObjectType = isStartNode() ? StyleObjectType.START_LOADER_GROUP : StyleObjectType.LOADER_GROUP;
    } else if (provisionerTarget) {
      styleObjectType = StyleObjectType.PROVISIONER;
    } else if (intersectGroup) {
      styleObjectType = StyleObjectType.INTERSECT_GROUP;
    } else if (complementGroup) {
      styleObjectType = StyleObjectType.COMPLEMENT_GROUP;
    } else if (group) {
      styleObjectType = isStartNode() ? StyleObjectType.START_GROUP : StyleObjectType.GROUP;
    } else if (stem) {
      styleObjectType = isStartNode() ? StyleObjectType.START_STEM : StyleObjectType.STEM;
    } else if (subject) {
      styleObjectType = isStartNode() ? StyleObjectType.START_SUBJECT : StyleObjectType.SUBJECT;
    } else if (provisionerTarget) {
      styleObjectType = StyleObjectType.PROVISIONER;
    } else {
      styleObjectType = StyleObjectType.DEFAULT;
    }

  }

  /**
   * Returns the underlying GrouperObject value
   *
   * @return the internal GrouperObject value
   */
  public GrouperObject getGrouperObject() {
    return grouperObject;
  }

  /**
   * The member count as set by the caller.
   *
   * @return
   */
  public long getMemberCount() {
    return memberCount;
  }

  /**
   * Sets the member count for this node.
   *
   * @param memberCount member count
   */
  public void setMemberCount(long memberCount) {
    this.memberCount = memberCount;
  }

  /**
   * Virtual method that returns true if both parents and children have been visited.
   *
   * @return
   */
  public boolean isVisited() {
    return visitedParents && visitedChildren;
  }

  /**
   * Returns whether the parent nodes have been visited. Set by the caller
   * @return true if parent nodes have been visited
   */
  public boolean isVisitedParents() {
    return visitedParents;
  }

  /**
   * Marks this node as having visited all its parent nodes
   *
   * @param visitedParents
   */
  public void setVisitedParents(boolean visitedParents) {
    this.visitedParents = visitedParents;
  }

  /**
   * Returns whether the child nodes have been visited. Set by the caller
   * @return true if child nodes have been visited
   */
  public boolean isVisitedChildren() {
    return visitedChildren;
  }

  /**
   * Marks this node as having visited all its child nodes
   *
   * @param visitedChildren
   */
  public void setVisitedChildren(boolean visitedChildren) {
    this.visitedChildren = visitedChildren;
  }

  /**
   * Gets the parent nodes linked to this node. Set by the caller
   *
   * @return the set of all parent nodes reachable by this node
   */
  public Set<GraphNode> getParentNodes() {
    return parentNodes;
  }

  /**
   * Adds a parent node to the collection
   *
   * @param parentNode parent node to add
   */
  public void addParentNode(GraphNode parentNode) {
    this.parentNodes.add(parentNode);
  }

  /**
   * Gets the child nodes linked to this node. Set by the caller
   *
   * @return the set of all child nodes reachable by this node
   */
  public Set<GraphNode> getChildNodes() {
    return childNodes;
  }

  /**
   * Adds a child node to the collection
   *
   * @param childNode child node to add
   */
  public void addChildNode(GraphNode childNode) {
    this.childNodes.add(childNode);
  }

  /**
   * Returns the number of hops from the starting node to this node. The value is set by
   * the caller, but by convention parents of the start node will be negative numbers. The
   * value should be zero only for the starting node.
   *
   * @return Distance from the starting node
   */
  public long getDistanceFromStartNode() {
    return distanceFromStartNode;
  }

  /**
   * Sets the number of hops from the starting node to this node. If this is a
   * parent of the start node, will be less than zero
   *
   * @param distanceFromStartNode
   */
  public void setDistanceFromStartNode(long distanceFromStartNode) {
    this.distanceFromStartNode = distanceFromStartNode;
  }

  /**
   * True if the underlying Grouper object is a {@link Stem}
   *
   * @return whether the Grouper object is a stem
   */
  public boolean isStem() {
    return stem;
  }

  /**
   * True if the underlying Grouper object is a {@link Group}
   *
   * @return whether the Grouper object is a group
   */
  public boolean isGroup() {
    return group;
  }

  /**
   * True if the underlying Grouper object is a {@link GrouperObjectSubjectWrapper}
   *
   * @return whether the Grouper object is a subject wrapped as a GrouperObjectSubjectWrapper
   */
  public boolean isSubject() {
    return subject;
  }

  /**
   * True if the underlying Grouper object is a group set up
   * as a Grouper Loader job (either SQL or LDAP).
   *
   * @return whether the Grouper object is a group with loader settings
   */
  public boolean isLoaderGroup() {
    return loaderGroup;
  }

  /**
   * True if the underlying Grouper object is a {@link GrouperObjectProvisionerWrapper}
   *
   * @return whether the Grouper object is a provisioner object wrapped as a GrouperObjectProvisionerWrapper
   */
  public boolean isProvisionerTarget() {
    return provisionerTarget;
  }

  /**
   * True if the underlying Grouper object is group the has a composite type of INTERSECT
   *
   * @return whether the Grouper object has a composite INTERSECT type
   */
  public boolean isIntersectGroup() {
    return intersectGroup;
  }

  /**
   * True if the underlying Grouper object is group the has a composite type of COMPLEMENT
   *
   * @return whether the Grouper object has a composite COMPLEMENT type
   */
  public boolean isComplementGroup() {
    return complementGroup;
  }

  /**
   * True if this is the starting node. Set by the caller
   *
   * @return whether this is the starting node
   */
  public boolean isStartNode() {
    return startNode;
  }

  /**
   * sets whether this is the start node
   * @param startNode flag whether this is the starting node
   */
  public void setStartNode(boolean startNode) {
    boolean oldStartNode = this.startNode;
    this.startNode = startNode;

    // re-determine the object type since it may change
    if (oldStartNode != startNode) {
      determineObjectTypes();
    };
  }

  /**
   * gets the style object type enum
   *
   * @return style object type
   */
  public StyleObjectType getStyleObjectType() {
    return styleObjectType;
  }
}
