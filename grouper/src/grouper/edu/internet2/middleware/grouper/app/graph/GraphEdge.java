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

import edu.internet2.middleware.grouper.app.visualization.StyleObjectType;
import edu.internet2.middleware.grouper.app.visualization.VisualStyle;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * A GraphEdge represents a directional connection between two GraphNodes
 */
public class GraphEdge {
  private GraphNode fromNode;
  private GraphNode toNode;

  private StyleObjectType styleObjectType;

  /**
   * Constructor defining a directional edge, from one node to another. Depending on the
   * object type of either node, this will also set its VisualStyle object type enum.
   *
   * @param fromNode
   * @param toNode
   */
  public GraphEdge(GraphNode fromNode, GraphNode toNode) {
    this.fromNode = fromNode;
    this.toNode = toNode;

    if (fromNode.isLoaderGroup()) {
      styleObjectType = StyleObjectType.EDGE_FROM_LOADER;
    } else if (toNode.isProvisionerTarget()) {
      styleObjectType = StyleObjectType.EDGE_TO_PROVISIONER;
    } else if (fromNode.isStem()) {
      styleObjectType = StyleObjectType.EDGE_FROM_STEM;
    } else if (toNode.isComplementGroup()) {
      styleObjectType = StyleObjectType.EDGE_COMPLEMENT;
    } else if (toNode.isIntersectGroup()) {
      styleObjectType = StyleObjectType.EDGE_INTERSECT;
    } else if (fromNode.isGroup()) {
      styleObjectType = StyleObjectType.EDGE_MEMBERSHIP;
    } else {
      styleObjectType = StyleObjectType.EDGE;
    }
  }


  /**
   * Compares two edges and considers them equal if the target and destination
   * nodes are the same
   *
   * @param obj GraphEdge to compare
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
    GraphEdge other = (GraphEdge) obj;

    return new EqualsBuilder()
      .append(this.fromNode, other.fromNode)
      .append(this.toNode, other.toNode)
      .isEquals();
  }

  /**
   * Computes the hash code based on the origin and destination nodes
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
      return new HashCodeBuilder()
              .append( this.fromNode)
              .append( this.toNode)
              .toHashCode();
  }

  /**
   * gets the origin node for the edge
   *
    * @return origin node
   */
  public GraphNode getFromNode() {
    return fromNode;
  }

  /**
   * gets the destination node for the edge
   *
   * @return destination node
   */
  public GraphNode getToNode() {
    return toNode;
  }

  /**
   * gets the VisualStyle object type enum
   *
   * @return VisualStyle object type
   */
  public StyleObjectType getStyleObjectType() {
    return styleObjectType;
  }
}
