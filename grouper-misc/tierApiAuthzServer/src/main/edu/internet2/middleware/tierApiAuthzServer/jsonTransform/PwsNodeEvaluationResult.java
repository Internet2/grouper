/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.jsonTransform;


/**
 * result of evaluation
 */
public class PwsNodeEvaluationResult {

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("{foundNode: ").append(this.pwsNode != null)
      .append(", createdNode: ").append(this.createdNode)
      .append(", changedArrayType: ").append(this.changedArrayType).append("}");
    return result.toString();
  }
  
  /**
   * if changed array type
   */
  private boolean changedArrayType = false;
  
  /**
   * if changed array type
   * @return the changedArrayType
   */
  public boolean isChangedArrayType() {
    return this.changedArrayType;
  }
  
  /**
   * if changed array type
   * @param changedArrayType1 the changedArrayType to set
   */
  public void setChangedArrayType(boolean changedArrayType1) {
    this.changedArrayType = changedArrayType1;
  }

  /**
   * node that this evaluates to
   */
  private PwsNode pwsNode;

  
  /**
   * node that this evaluates to
   * @return the pwsNode
   */
  public PwsNode getPwsNode() {
    return this.pwsNode;
  }

  
  /**
   * node that this evaluates to
   * @param pwsNode1 the pwsNode to set
   */
  public void setPwsNode(PwsNode pwsNode1) {
    this.pwsNode = pwsNode1;
  }

  /**
   * if the node was created
   */
  private boolean createdNode = false;


  
  /**
   * if the node was created
   * @return the createdNode
   */
  public boolean isCreatedNode() {
    return this.createdNode;
  }


  
  /**
   * if the node was created
   * @param createdNode1 the createdNode to set
   */
  public void setCreatedNode(boolean createdNode1) {
    this.createdNode = createdNode1;
  }

  
  
}
