/**
 * 
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.Stem;


/**
 * @author mchyzer
 *
 */
public class RulesStemBean extends RulesBean {

  /**
   * 
   */
  public RulesStemBean() {
    
  }
  
  /**
   * @see RulesBean#hasStem()
   */
  @Override
  public boolean hasStem() {
    return true;
  }

  /**
   * 
   * @param stem1
   */
  public RulesStemBean(Stem stem1) {
    super();
    this.stem = stem1;
  }


  /** stem */
  private Stem stem;

  /**
   * stem
   * @return stem
   */
  @Override
  public Stem getStem() {
    return this.stem;
  }

  /**
   * stem
   * @param stem1
   */
  public void setStem(Stem stem1) {
    this.stem = stem1;
  }

  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (this.stem != null) {
      result.append("stem: ").append(this.stem.getName()).append(", ");
    }
    return result.toString();
  }
  
  
}
