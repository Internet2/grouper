package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinderResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinderResults;

public class GuiAttributeAssignFinderResults {
  
  private AttributeAssignFinderResults attributeAssignFinderResults;

  public GuiAttributeAssignFinderResults(AttributeAssignFinderResults attributeAssignFinderResults) {
    this.attributeAssignFinderResults = attributeAssignFinderResults;
  }

  public AttributeAssignFinderResults getAttributeAssignFinderResults() {
    
    return attributeAssignFinderResults;
  }

  public Set<GuiAttributeAssignFinderResult> getGuiAttributeAssignFinderResults() {
    
    Set<GuiAttributeAssignFinderResult> guiAttributeAssignFinderResults = new HashSet<GuiAttributeAssignFinderResult>();
    
    for (AttributeAssignFinderResult attributeAssignFinderResult: attributeAssignFinderResults.getAttributeAssignFinderResults()) {
      GuiAttributeAssignFinderResult guiAttributeAssignFinderResult = new GuiAttributeAssignFinderResult(attributeAssignFinderResult);
      guiAttributeAssignFinderResults.add(guiAttributeAssignFinderResult);
    }
    return guiAttributeAssignFinderResults;
  }
  
}
