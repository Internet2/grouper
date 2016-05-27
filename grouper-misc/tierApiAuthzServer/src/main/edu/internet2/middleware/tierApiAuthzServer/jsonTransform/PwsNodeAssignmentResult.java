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
 * result from an assignment
 */
public class PwsNodeAssignmentResult {

  /**
   * if the source location existed
   */
  private boolean foundSourceLocation = false;

  
  /**
   * if the source location existed
   * @return the foundSourceLocation
   */
  public boolean isFoundSourceLocation() {
    return this.foundSourceLocation;
  }

  
  /**
   * if the source location existed
   * @param foundSourceLocation1 the foundSourceLocation to set
   */
  public void setFoundSourceLocation(boolean foundSourceLocation1) {
    this.foundSourceLocation = foundSourceLocation1;
  }
  
  /**
   * if the destination location was created
   */
  private boolean createdDestinationLocation = false;


  
  /**
   * if the destination location was created
   * @return the createdDestinationLocation
   */
  public boolean isCreatedDestinationLocation() {
    return this.createdDestinationLocation;
  }


  
  /**
   * if the destination location was created
   * @param createdDestinationLocation1 the createdDestinationLocation to set
   */
  public void setCreatedDestinationLocation(boolean createdDestinationLocation1) {
    this.createdDestinationLocation = createdDestinationLocation1;
  }

  /**
   * if the type of the destination did not match the source and needed to be changed
   */
  private boolean changedType = false;


  
  /**
   * if the type of the destination did not match the source and needed to be changed
   * @return the changedType
   */
  public boolean isChangedType() {
    return this.changedType;
  }


  
  /**
   * if the type of the destination did not match the source and needed to be changed
   * @param changedType1 the changedType to set
   */
  public void setChangedType(boolean changedType1) {
    this.changedType = changedType1;
  }
  
  
}
