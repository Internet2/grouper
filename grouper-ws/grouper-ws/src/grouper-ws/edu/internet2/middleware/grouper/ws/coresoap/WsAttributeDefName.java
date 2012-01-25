/**
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Result of one attribute def name being retrieved.  The number of
 * attribute def names will equal the number of attribute def names related to the result
 * 
 * @author mchyzer
 */
public class WsAttributeDefName implements Comparable<WsAttributeDefName> {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /** extension of attributeDefName, the part to the right of last colon in name */
  private String extension;

  /** display extension, the part to the right of the last colon in display name */
  private String displayExtension;

  /**
   * convert a set of attribute def names to results
   * @param attributeDefNameSet
   * @return the attributeDefNames (null if none or null)
   */
  public static WsAttributeDefName[] convertAttributeDefNames(Set<AttributeDefName> attributeDefNameSet) {
    if (attributeDefNameSet == null || attributeDefNameSet.size() == 0) {
      return null;
    }
    int attributeDefNameSetSize = attributeDefNameSet.size();
    WsAttributeDefName[] wsAttributeDefNameResults = new WsAttributeDefName[attributeDefNameSetSize];
    int index = 0;
    for (AttributeDefName attributeDefName : attributeDefNameSet) {
      WsAttributeDefName wsAttributeDefName = new WsAttributeDefName(attributeDefName, null);
      wsAttributeDefNameResults[index] = wsAttributeDefName;
      index++;
    }
    return wsAttributeDefNameResults;

  }

  /**
   * friendly description of this attributeDefName
   */
  private String description;

  /**
   * friendly extensions of attributeDefName and parent stems
   */
  private String displayName;

  /**
   * Full name of the attributeDefName (all extensions of parent stems, separated by colons,  and the extention of this attributeDefName
   */
  private String name;

  /**
   * universally unique identifier of this attributeDefName
   */
  private String uuid;

  /** id of the attribute definition */
  private String attributeDefId;

  /** name of the attribute definition */
  private String attributeDefName;

  
  /**
   * name of the attribute definition
   * @return name of attribute def
   */
  public String getAttributeDefName() {
    return this.attributeDefName;
  }

  /**
   * name of the attribute definition
   * @param attributeDefName1
   */
  public void setAttributeDefName(String attributeDefName1) {
    this.attributeDefName = attributeDefName1;
  }

  /**
   * id of the attribute definition
   * @return id of the attribute definition
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  /**
   * id of the attribute definition
   * @param attributeDefId1
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  /**
   * no arg constructor
   */
  public WsAttributeDefName() {
    //blank

  }

  /**
   * construct based on attribute def name, assign all fields
   * @param theAttributeDefName 
   * @param wsAttributeDefNameLookup is the lookup to set looked up values
   */
  public WsAttributeDefName(AttributeDefName theAttributeDefName, WsAttributeDefNameLookup wsAttributeDefNameLookup) {
    if (theAttributeDefName != null) {
      this.setDescription(StringUtils.trimToNull(theAttributeDefName.getDescription()));
      this.setDisplayName(theAttributeDefName.getDisplayName());
      this.setName(theAttributeDefName.getName());
      this.setUuid(theAttributeDefName.getId());
      this.setExtension(theAttributeDefName.getExtension());
      this.setDisplayExtension(theAttributeDefName.getDisplayExtension());
      this.setAttributeDefId(theAttributeDefName.getAttributeDefId());
      this.setAttributeDefName(theAttributeDefName.getAttributeDef().getName());
      
    } else {
      if (wsAttributeDefNameLookup != null) {
        //no attributeDefName, set the look values so the caller can keep things in sync
        this.setName(wsAttributeDefNameLookup.getName());
        this.setUuid(wsAttributeDefNameLookup.getUuid());
        this.setExtension(GrouperUtil.extensionFromName(wsAttributeDefNameLookup.getName()));
      }
    }
  }
  
  /**
   * construct based on pit attribute def name, assign all fields
   * @param theAttributeDefName 
   * @param wsAttributeDefNameLookup is the lookup to set looked up values
   */
  public WsAttributeDefName(PITAttributeDefName theAttributeDefName, WsAttributeDefNameLookup wsAttributeDefNameLookup) {
    if (theAttributeDefName != null) {
      this.setName(theAttributeDefName.getName());
      this.setUuid(theAttributeDefName.getSourceId());
      this.setExtension(GrouperUtil.extensionFromName(theAttributeDefName.getName()));
      
      PITAttributeDef theAttributeDef = PITAttributeDefFinder.findById(theAttributeDefName.getAttributeDefId(), false);
      
      if (theAttributeDef != null) {
        this.setAttributeDefId(theAttributeDef.getSourceId());
        this.setAttributeDefName(theAttributeDef.getName());
      }
      
    } else {
      if (wsAttributeDefNameLookup != null) {
        //no attributeDefName, set the look values so the caller can keep things in sync
        this.setName(wsAttributeDefNameLookup.getName());
        this.setUuid(wsAttributeDefNameLookup.getUuid());
        this.setExtension(GrouperUtil.extensionFromName(wsAttributeDefNameLookup.getName()));
      }
    }
  }

  /**
   * friendly description of this attributeDefName
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * friendly extensions of attributeDefName and parent stems
   * @return the displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * Full name of the attributeDefName (all extensions of parent stems, separated by colons, 
   * and the extention of this attributeDefName
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * universally unique identifier of this attributeDefName
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * friendly description of this attributeDefName
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * friendly extensions of attributeDefName and parent stems
   * @param displayName1 the displayName to set
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * Full name of the attributeDefName (all extensions of parent stems, separated by colons, 
   * and the extention of this attributeDefName
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * universally unique identifier of this attributeDefName
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * extension of attributeDefName, the part to the right of last colon in name
   * @return the extension
   */
  public String getExtension() {
    return this.extension;
  }

  /**
   * extension of attributeDefName, the part to the right of last colon in name
   * @param extension1 the extension to set
   */
  public void setExtension(String extension1) {
    this.extension = extension1;
  }

  /**
   * display extension, the part to the right of the last colon in display name
   * @return the displayExtension
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }

  /**
   * display extension, the part to the right of the last colon in display name
   * @param displayExtension1 the displayExtension to set
   */
  public void setDisplayExtension(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsAttributeDefName o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (this == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }
    return GrouperUtil.compare(this.getName(), o2.getName());
  }
}
