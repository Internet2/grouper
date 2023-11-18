package edu.internet2.middleware.grouper.stem;

import java.io.Serializable;

public class StemViewPrivilege implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -8676875678328668860L;

  private String stemUuid;
  
  private String memberUuid;
  
  private String objectType;
  
  
  
  
  public String getStemUuid() {
    return stemUuid;
  }

  
  public void setStemUuid(String stemUuid) {
    this.stemUuid = stemUuid;
  }

  
  public String getMemberUuid() {
    return memberUuid;
  }

  
  public void setMemberUuid(String memberUuid) {
    this.memberUuid = memberUuid;
  }

  
  public String getObjectType() {
    return objectType;
  }

  
  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }


}
