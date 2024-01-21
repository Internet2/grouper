package edu.internet2.middleware.grouper.attr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class AttributeDefSaveBatch {

  
  private boolean makeChangesIfExist = true;

  public AttributeDefSaveBatch assignMakeChangesIfExist(boolean theMakeChangesIfExist) {
    this.makeChangesIfExist = theMakeChangesIfExist;
    return this;
  }
  
  private List<AttributeDefSave> attributeDefSaves = new ArrayList<AttributeDefSave>();
  
  public AttributeDefSaveBatch addAttributeDefSaves(Collection<AttributeDefSave> theAttributeDefSaves) {
    if (theAttributeDefSaves != null) {
      this.attributeDefSaves.addAll(theAttributeDefSaves);
    }
    return this;
  }

  public Map<String, AttributeDef> save() {
    
    Set<String> attributeDefNames = new HashSet<String>();

    for (AttributeDefSave attributeDefSave : attributeDefSaves) {
      if (!StringUtils.isBlank(attributeDefSave.getName())) {
        attributeDefNames.add(attributeDefSave.getName());
      }
      if (!StringUtils.isBlank(attributeDefSave.getAttributeDefNameToEdit())) {
        attributeDefNames.add(attributeDefSave.getAttributeDefNameToEdit());
      }
    }
    
    Set<AttributeDef> attributeDefs = new AttributeDefFinder().assignNamesOfAttributeDefs(attributeDefNames).findAttributes();
    
    Map<String, AttributeDef> attributeDefNameToAttributeDef = new HashMap<String, AttributeDef>();
    
    for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefs)) {
      attributeDefNameToAttributeDef.put(attributeDef.getName(), attributeDef);
    }
    
    for (AttributeDefSave attributeDefSave : attributeDefSaves) {
      if (!StringUtils.isBlank(attributeDefSave.getName())) {
        AttributeDef attributeDef = attributeDefNameToAttributeDef.get(attributeDefSave.getName());
        if (attributeDef != null) {
          if (this.makeChangesIfExist) {
            attributeDef = attributeDefSave.save();
          }
        } else {
          attributeDef = attributeDefSave.save();
        }
        if (attributeDef != null) {
          attributeDefNameToAttributeDef.put(attributeDef.getName(), attributeDef);
        }
      }
    }
    return attributeDefNameToAttributeDef;
  }
}
