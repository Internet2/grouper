package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinderResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinderResults;

/**
 * 
 * result of attribute def name or attribute def's assigned owners
 *
 */
public class GuiAttributeAssignFinderResults {
  
  /**
   * result of attribute def name or attribute def's assigned owners
   */
  private AttributeAssignFinderResults attributeAssignFinderResults;

  public GuiAttributeAssignFinderResults(AttributeAssignFinderResults attributeAssignFinderResults) {
    this.attributeAssignFinderResults = attributeAssignFinderResults;
  }

  /**
   * result of attribute def name or attribute def's assigned owners
   * @return
   */
  public AttributeAssignFinderResults getAttributeAssignFinderResults() {
    return attributeAssignFinderResults;
  }

  /**
   * get back gui version of attribute assign finder results
   * @return
   */
  public List<GuiAttributeAssignFinderResult> getGuiAttributeAssignFinderResults() {
    
    List<GuiAttributeAssignFinderResult> guiAttributeAssignFinderResults = new ArrayList<GuiAttributeAssignFinderResult>();
    
    List<AttributeAssignFinderResult> sortedAttributeAssignFinderResults = sortAttributeAssignFinderResults(attributeAssignFinderResults.getAttributeAssignFinderResults());
    
    for (AttributeAssignFinderResult attributeAssignFinderResult: sortedAttributeAssignFinderResults) {
      GuiAttributeAssignFinderResult guiAttributeAssignFinderResult = new GuiAttributeAssignFinderResult(attributeAssignFinderResult);
      guiAttributeAssignFinderResults.add(guiAttributeAssignFinderResult);
    }
    return guiAttributeAssignFinderResults;
  }
  
  /**
   * sort the results so that on the UI direct and non direct appear together
   * @param attributeAssignFinderResults
   * @return
   */
  private List<AttributeAssignFinderResult> sortAttributeAssignFinderResults(Set<AttributeAssignFinderResult> attributeAssignFinderResults) {
    
    List<AttributeAssignFinderResult> sortedList = new ArrayList<AttributeAssignFinderResult>();
    
    Map<AttributeAssignFinderResult, List<AttributeAssignFinderResult>> parentChildren = new LinkedHashMap<AttributeAssignFinderResult, List<AttributeAssignFinderResult>>();

    Map<String, AttributeAssignFinderResult> attributeAssignIdFinderResult = new HashMap<String, AttributeAssignFinderResult>();
    
    for (AttributeAssignFinderResult attributeAssignFinderResult: attributeAssignFinderResults) {
      
      if (attributeAssignFinderResult.getOwnerAttributeDef() != null || 
          attributeAssignFinderResult.getOwnerGroup() != null ||
          attributeAssignFinderResult.getOwnerMember() != null ||
          attributeAssignFinderResult.getOwnerMembership() != null || 
          attributeAssignFinderResult.getOwnerStem() != null) {        
        attributeAssignIdFinderResult.put(attributeAssignFinderResult.getAttributeAssign().getId(), attributeAssignFinderResult);
        parentChildren.put(attributeAssignFinderResult, new ArrayList<AttributeAssignFinderResult>());
      }
      
    }
    
    for (AttributeAssignFinderResult attributeAssignFinderResult: attributeAssignFinderResults) {
      
      if (attributeAssignFinderResult.getAttributeAssign() != null
          && attributeAssignFinderResult.getAttributeAssign().getOwnerAttributeAssignId() != null 
          && attributeAssignIdFinderResult.containsKey(attributeAssignFinderResult.getAttributeAssign().getOwnerAttributeAssignId())) {        
        
        AttributeAssignFinderResult parent = attributeAssignIdFinderResult.get(attributeAssignFinderResult.getAttributeAssign().getOwnerAttributeAssignId());
        parentChildren.get(parent).add(attributeAssignFinderResult);
        
      }
      
    }
    
    for (Map.Entry<AttributeAssignFinderResult, List<AttributeAssignFinderResult>> entry: parentChildren.entrySet()) {
      sortedList.add(entry.getKey());
      sortedList.addAll(entry.getValue());
    }
    
    
    return sortedList;
  }
  
}
