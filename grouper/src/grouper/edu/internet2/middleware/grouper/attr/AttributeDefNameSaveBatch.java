package edu.internet2.middleware.grouper.attr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class AttributeDefNameSaveBatch {

  
  private boolean makeChangesIfExist = true;

  public AttributeDefNameSaveBatch assignMakeChangesIfExist(boolean theMakeChangesIfExist) {
    this.makeChangesIfExist = theMakeChangesIfExist;
    return this;
  }
  
  private List<AttributeDefNameSave> attributeDefNameSaves = new ArrayList<AttributeDefNameSave>();
  
  public AttributeDefNameSaveBatch addAttributeDefNameSaves(Collection<AttributeDefNameSave> theAttributeDefNameSaves) {
    if (theAttributeDefNameSaves != null) {
      this.attributeDefNameSaves.addAll(theAttributeDefNameSaves);
    }
    return this;
  }

  public Map<String, AttributeDefName> save() {
    
    Set<String> nameOfAttributeDefNames = new HashSet<String>();

    for (AttributeDefNameSave attributeDefNameSave : attributeDefNameSaves) {
      if (!StringUtils.isBlank(attributeDefNameSave.getName())) {
        nameOfAttributeDefNames.add(attributeDefNameSave.getName());
      }
      if (!StringUtils.isBlank(attributeDefNameSave.getAttributeDefNameNameToEdit())) {
        nameOfAttributeDefNames.add(attributeDefNameSave.getAttributeDefNameNameToEdit());
      }
    }
    
    Set<AttributeDefName> attributeDefNames = new AttributeDefNameFinder().assignNamesOfAttributeDefNames(nameOfAttributeDefNames).findAttributeNames();
    
    Map<String, AttributeDefName> nameOfAttributeDefNameToAttributeDefName = new HashMap<String, AttributeDefName>();
    
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
      nameOfAttributeDefNameToAttributeDefName.put(attributeDefName.getName(), attributeDefName);
    }
    
    for (AttributeDefNameSave attributeDefNameSave : attributeDefNameSaves) {
      if (!StringUtils.isBlank(attributeDefNameSave.getName())) {
        AttributeDefName attributeDefName = nameOfAttributeDefNameToAttributeDefName.get(attributeDefNameSave.getName());
        if (attributeDefName != null) {
          if (this.makeChangesIfExist) {
            attributeDefName = attributeDefNameSave.save();
          }
        } else {
          attributeDefName = attributeDefNameSave.save();
        }
        if (attributeDefName != null) {
          nameOfAttributeDefNameToAttributeDefName.put(attributeDefName.getName(), attributeDefName);
        }
      }
    }
    return nameOfAttributeDefNameToAttributeDefName;
  }
}
