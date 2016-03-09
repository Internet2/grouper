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
