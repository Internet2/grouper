/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.preferences;


/**
 * bean that goes to json and back, very simple
 */
public class UiV2Preference {

  /**
   * 
   */
  public UiV2Preference() {
  }

  /**
   * what defaults in widget cols (0,1,2): [IndexContainer.IndexPanel enum]: 
   * GroupsImanage, MyFavorites, MyMemberships, MyServices, RecentlyUsed, StemsImanage
   */
  private String indexCol0;
  
  /**
   * what defaults in widget cols (0,1,2): [IndexContainer.IndexPanel enum]: 
   * GroupsImanage, MyFavorites, MyMemberships, MyServices, RecentlyUsed, StemsImanage
   * @return the indexCol0
   */
  public String getIndexCol0() {
    return this.indexCol0;
  }
  
  /**
   * what defaults in widget cols (0,1,2): [IndexContainer.IndexPanel enum]: 
   * GroupsImanage, MyFavorites, MyMemberships, MyServices, RecentlyUsed, StemsImanage
   * @param indexCol0_ the indexCol0 to set
   */
  public void setIndexCol0(String indexCol0_) {
    this.indexCol0 = indexCol0_;
  }
  
  /**
   * what defaults in widget cols (0,1,2): [IndexContainer.IndexPanel enum]: 
   * GroupsImanage, MyFavorites, MyMemberships, MyServices, RecentlyUsed, StemsImanage
   * @return the indexCol1
   */
  public String getIndexCol1() {
    return this.indexCol1;
  }
  
  /**
   * what defaults in widget cols (0,1,2): [IndexContainer.IndexPanel enum]: 
   * GroupsImanage, MyFavorites, MyMemberships, MyServices, RecentlyUsed, StemsImanage
   * @param indexCol1_ the indexCol1 to set
   */
  public void setIndexCol1(String indexCol1_) {
    this.indexCol1 = indexCol1_;
  }
  
  /**
   * what defaults in widget cols (0,1,2): [IndexContainer.IndexPanel enum]: 
   * GroupsImanage, MyFavorites, MyMemberships, MyServices, RecentlyUsed, StemsImanage
   * @return the indexCol2
   */
  public String getIndexCol2() {
    return this.indexCol2;
  }
  
  /**
   * what defaults in widget cols (0,1,2): [IndexContainer.IndexPanel enum]: 
   * GroupsImanage, MyFavorites, MyMemberships, MyServices, RecentlyUsed, StemsImanage
   * @param indexCol2_ the indexCol2 to set
   */
  public void setIndexCol2(String indexCol2_) {
    this.indexCol2 = indexCol2_;
  }

  /**
   * what defaults in widget cols (0,1,2): [IndexContainer.IndexPanel enum]: 
   * GroupsImanage, MyFavorites, MyMemberships, MyServices, RecentlyUsed, StemsImanage
   */
  private String indexCol1;
  
  /**
   * what defaults in widget cols (0,1,2): [IndexContainer.IndexPanel enum]: 
   * GroupsImanage, MyFavorites, MyMemberships, MyServices, RecentlyUsed, StemsImanage
   */
  private String indexCol2;
  
}
