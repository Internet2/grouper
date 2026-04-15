package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl3.parser.JexlNode;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;

public class GrouperJexlScriptAnalysis {

  public GrouperJexlScriptAnalysis() {
  }

  private List<GrouperJexlScriptPart> grouperJexlScriptParts = new ArrayList<GrouperJexlScriptPart>();

  
  public List<GrouperJexlScriptPart> getGrouperJexlScriptParts() {
    return grouperJexlScriptParts;
  }
  
  public void setGrouperJexlScriptParts(
      List<GrouperJexlScriptPart> grouperJexlScriptParts) {
    this.grouperJexlScriptParts = grouperJexlScriptParts;
  }
  
  /**
   * 
   */
  @Override
  public String toString() {

    StringBuilder result = new StringBuilder();

    if (!StringUtils.isBlank(this.getErrorMessage())) {
      result.append("Error: " + this.getErrorMessage() + "\n");
    }
    
    if (!StringUtils.isBlank(this.getWarningMessage())) {
      result.append("Warning: " + this.getWarningMessage() + "\n");
    }
    
    for (int i=0; i<grouperJexlScriptParts.size(); i++) {
      result.append(i).append(": ").append(grouperJexlScriptParts.get(i)).append("\n");
    }
    return result.toString();
  }
  
  private Set<String> recentMemberOfGroupNames = new HashSet<String>();
  
  public Set<String> getRecentMemberOfGroupNames() {
    return recentMemberOfGroupNames;
  }

  
  public void setRecentMemberOfGroupNames(Set<String> recentMemberOfGroupNames) {
    this.recentMemberOfGroupNames = recentMemberOfGroupNames;
  }

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  //TODO should have multiple errors or warnings or should append?
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
  
  private String warningMessage;

  public String getWarningMessage() {
    return warningMessage;
  }

  public void setWarningMessage(String warningMessage) {
    this.warningMessage = warningMessage;
  }

  private GrouperDataEngine grouperDataEngine;

  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }

  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }

  /**
   * tree of ABAC references extracted from the JEXL script for visualization
   */
  private List<AbacReference> visualizationReferences;

  public List<AbacReference> getVisualizationReferences() {
    return visualizationReferences;
  }

  public void setVisualizationReferences(List<AbacReference> visualizationReferences) {
    this.visualizationReferences = visualizationReferences;
  }

  /**
   * map from JEXL AST node to the analysis part that carries its description and population count.
   * Used by the visualization tree builder to attach metadata during a direct AST walk.
   * Keyed by reference identity so different AST nodes with the same content are not merged.
   */
  private Map<JexlNode, GrouperJexlScriptPart> astNodeToPart = new IdentityHashMap<JexlNode, GrouperJexlScriptPart>();

  public Map<JexlNode, GrouperJexlScriptPart> getAstNodeToPart() {
    return astNodeToPart;
  }

  /**
   * root AST node of the parsed JEXL script; retained so the visualization builder can walk
   * the tree directly rather than reconstructing it from a flat parts list.
   */
  private JexlNode rootAstNode;

  public JexlNode getRootAstNode() {
    return rootAstNode;
  }

  public void setRootAstNode(JexlNode rootAstNode) {
    this.rootAstNode = rootAstNode;
  }

}
