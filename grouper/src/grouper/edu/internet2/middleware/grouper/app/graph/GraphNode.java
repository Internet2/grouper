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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.app.visualization.StyleObjectType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectSubjectWrapper;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
  private GrouperObject grouperObject;
  private long allMemberCount;
  private long directMemberCount;
  private List<String> objectTypeNames;

  private boolean stem;
  private boolean group;
  private boolean subject;
  private boolean loaderGroup;
  private boolean simpleLoaderGroup;
  private boolean provisionerTarget;
  private boolean intersectGroup;
  private boolean complementGroup;
  private boolean startNode;
  private StyleObjectType styleObjectType;

  private boolean visitedParents;
  private boolean visitedChildren;
  private boolean startedProcessingParentPaths;
  private boolean startedProcessingChildPaths;

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
    this.simpleLoaderGroup = false;
    this.intersectGroup = false;
    this.complementGroup = false;
    this.provisionerTarget = false;

    if (grouperObject instanceof Group) {
      final Group theGroup = (Group)grouperObject;
      this.group = true;

      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          // is it a sql loader job?
          if (RelationGraph.getSqlLoaderAttributeDefName() != null
              && theGroup.getAttributeDelegate().retrieveAssignment(null, RelationGraph.getSqlLoaderAttributeDefName(), false, false)!=null) {
            GraphNode.this.loaderGroup = true;

            if ("SQL_SIMPLE".equals(theGroup.getAttribute(GrouperLoader.GROUPER_LOADER_TYPE))) {
              GraphNode.this.simpleLoaderGroup = true;
            }
          }
          // is an ldap loader job?
          else {
            AttributeAssign ldapAttributeAssign = theGroup.getAttributeDelegate().retrieveAssignment(null, LoaderLdapUtils.grouperLoaderLdapAttributeDefName(), false, false);
            if (ldapAttributeAssign != null) {
              GraphNode.this.loaderGroup = true;

              if ("LDAP_SIMPLE".equals(ldapAttributeAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapTypeName()))) {
                GraphNode.this.simpleLoaderGroup = true;
              }

            }
          }

          return null;
        }
      });

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
    if (simpleLoaderGroup) {
      styleObjectType = isStartNode() ? StyleObjectType.START_SIMPLE_LOADER_GROUP : StyleObjectType.SIMPLE_LOADER_GROUP;
    } else if (loaderGroup) {
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
   * returns the underlying GrouperObject value
   *
   * @return the internal GrouperObject value
   */
  public GrouperObject getGrouperObject() {
    return grouperObject;
  }

  /**
   * The total member count as set by the caller.
   *
   * @return
   */
  public long getAllMemberCount() {
    return allMemberCount;
  }

  /**
   * the grouper object types for a group or stem, as set by the creator of this node
   *
   * @return the object type names
   */
  public List<String> getObjectTypeNames() {
    return objectTypeNames;
  }

  /**
   * the direct member count as set by the caller
   *
   * @return
   */
  public long getDirectMemberCount() {
    return directMemberCount;
  }

  /**
   * sets the total member count for this node
   *
   * @param allMemberCount member count
   */
  public void setAllMemberCount(long allMemberCount) {
    this.allMemberCount = allMemberCount;
  }

  /**
   * sets the direct member count for this node.
   *
   * @param directMemberCount member count
   */
  public void setDirectMemberCount(long directMemberCount) {
    this.directMemberCount = directMemberCount;
  }

  /**
   * sets the list of grouper object type names
   *
   * @param objectTypeNames
   */
  public void setObjectTypeNames(List<String> objectTypeNames) {
    this.objectTypeNames = objectTypeNames;
  }

  /**
   * Adds one object type name to the node's current list.
   *
   * @param objectTypeName
   */
  public void addObjectTypeName(String objectTypeName) {
    if (this.objectTypeNames == null) {
      this.objectTypeNames = new LinkedList<String>();
    }
    this.objectTypeNames.add(objectTypeName);
  }

  /**
   * virtual method that returns true if both parents and children have been visited
   *
   * @return
   */
  public boolean isVisited() {
    return visitedParents && visitedChildren;
  }

  /**
   * returns whether the parent nodes have been visited. Set by the caller
   *
   * @return true if parent nodes have been visited
   */
  public boolean isVisitedParents() {
    return visitedParents;
  }

  /**
   * marks this node as having visited all its parent nodes
   *
   * @param visitedParents
   */
  public void setVisitedParents(boolean visitedParents) {
    this.visitedParents = visitedParents;
  }

  /**
   * Returns whether the child nodes have been visited. Set by the caller
   *
   * @return true if child nodes have been visited
   */
  public boolean isVisitedChildren() {
    return visitedChildren;
  }

  /**
   * marks this node as having visited all its child nodes
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
   * adds a parent node to the collection
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
   * True if the underlying Grouper object is a group set up
   * as a SQL_SIMPLE or LDAP_SIMPLE Grouper Loader job
   *
   * @return whether the Grouper object is a group with loader settings
   */
  public boolean isSimpleLoaderGroup() {
    return simpleLoaderGroup;
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

  /**
   * helper method to get the id of the underlying GrouperObject
   *
   * @return the id of the underlying GrouperObject
   */
  public String getGrouperObjectId() {
//    if (isSubject()) {
//      // GrouperObjectSubjectWrapper constructs a weird source||||id string as the id; dig into the underlying subject to
//      // get just the id
//      return ((GrouperObjectSubjectWrapper)this.getGrouperObject()).getSubject().getId();
//    } else {
      return this.getGrouperObject().getId();
//    }
  }

  /**
   * Helper method to get the id of the underlying GrouperObject
   *
   * @return the name of the underlying GrouperObject
   */
  public String getGrouperObjectName() {
    return this.getGrouperObject().getName();
  }
}
