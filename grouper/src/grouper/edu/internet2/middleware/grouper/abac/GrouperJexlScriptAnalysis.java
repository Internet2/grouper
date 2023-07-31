package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

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
    for (int i=0; i<grouperJexlScriptParts.size(); i++) {
      result.append(i).append(": ").append(grouperJexlScriptParts.get(i)).append("\n");
    }
    return result.toString();
  }

  
}
