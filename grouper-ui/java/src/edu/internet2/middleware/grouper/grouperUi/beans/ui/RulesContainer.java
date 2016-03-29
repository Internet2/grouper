/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiRuleDefinition;


/**
 * container to show rules on screen
 */
public class RulesContainer {

  /**
   * 
   */
  public RulesContainer() {
  }

  /**
   * rules to show on screen
   */
  private Set<GuiRuleDefinition> guiRuleDefinitions;

  
  /**
   * @return the guiRules
   */
  public Set<GuiRuleDefinition> getGuiRuleDefinitions() {
    return this.guiRuleDefinitions;
  }

  
  /**
   * @param guiRules1 the guiRules to set
   */
  public void setGuiRuleDefinitions(Set<GuiRuleDefinition> guiRules1) {
    this.guiRuleDefinitions = guiRules1;
  }
  
  
  
}
