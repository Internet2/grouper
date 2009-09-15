/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * definition of an attribute name (is linked with an attribute def)
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class AttributeDefName extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned {

  /** name of the groups attribute def name table in the db */
  public static final String TABLE_GROUPER_ATTRIBUTE_DEF_NAME = "grouper_attribute_def_name";

  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_ID = "attribute_def_id";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_DESCRIPTION = "description";

  /** column */
  public static final String COLUMN_EXTENSION = "extension";

  /** column */
  public static final String COLUMN_NAME = "name";

  /** column */
  public static final String COLUMN_DISPLAY_EXTENSION = "display_extension";

  /** column */
  public static final String COLUMN_DISPLAY_NAME = "display_name";

  /** column */
  public static final String COLUMN_STEM_ID = "stem_id";

  /** column */
  public static final String COLUMN_ID = "id";


  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeDefId */
  public static final String FIELD_ATTRIBUTE_DEF_ID = "attributeDefId";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: displayExtension */
  public static final String FIELD_DISPLAY_EXTENSION = "displayExtension";

  /** constant for field name for: displayName */
  public static final String FIELD_DISPLAY_NAME = "displayName";

  /** constant for field name for: extension */
  public static final String FIELD_EXTENSION = "extension";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: stemId */
  public static final String FIELD_STEM_ID = "stemId";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DESCRIPTION, 
      FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME, FIELD_EXTENSION, FIELD_ID, 
      FIELD_LAST_UPDATED_DB, FIELD_NAME, FIELD_STEM_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DESCRIPTION, 
      FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME, FIELD_EXTENSION, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_ID, FIELD_LAST_UPDATED_DB, FIELD_NAME, FIELD_STEM_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public AttributeDefName clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }


  /** id of this attribute def name */
  private String id;

  /** id of this attribute def  */
  private String attributeDefId;

  /** context id of the transaction */
  private String contextId;

  /** stem that this attribute is in */
  private String stemId;

  /**
   * name of attribute, e.g. school:community:students:expireDate 
   */
  private String name;

  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   */
  private String description;

  /**
   * displayExtension of attribute, e.g. Expire Date
   */
  private String displayExtension;

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   */
  private String displayName;

  /**
   * extension of attribute expireTime
   */
  private String extension;

  /**
   * time in millis when this attribute was created
   */
  private Long createdOnDb;

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;

  /**
   * stem that this attribute is in
   * @return the stem id
   */
  public String getStemId() {
    return stemId;
  }

  /**
   * stem that this attribute is in
   * @param stemId1
   */
  public void setStemId(String stemId1) {
    this.stemId = stemId1;
  }

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * id of this attribute def name
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * id of this attribute def name
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * 
   * @param name1
   */
  public void setName(@SuppressWarnings("unused") String name1) {
    throw new RuntimeException("Dont call this method");
  }

  /**
   * 
   * @return the name
   */
  public String getNameDb() {
    return name;
  }

  /**
   * 
   * @param name1
   */
  public void setNameDb(String name1) {
    this.name = name1;
  }

  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * displayExtension of attribute, e.g. Expire Date
   * @return display extension
   */
  public String getDisplayExtension() {
    return displayExtension;
  }

  /**
   * displayExtension of attribute, e.g. Expire Date
   * @param displayExtension1
   */
  public void setDisplayExtension(@SuppressWarnings("unused") String displayExtension1) {
    throw new RuntimeException("Dont call this method");
  }

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @return display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @param displayName1
   */
  public void setDisplayName(@SuppressWarnings("unused") String displayName1) {
    throw new RuntimeException("Dont call this method");
  }

  /**
   * extension of attribute expireTime
   * @return extension
   */
  public String getExtension() {
    return extension;
  }

  /**
   * extension of attribute expireTime
   * @param extension1
   */
  public void setExtension(@SuppressWarnings("unused") String extension1) {
    throw new RuntimeException("Dont call this method");
  }

  /**
   * extension of attribute expireTime
   * @return extension
   */
  public String getExtensionDb() {
    return extension;
  }

  /**
   * extension of attribute expireTime
   * @param extension1
   */
  public void setExtensionDb(String extension1) {
    this.extension = extension1;
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }

  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
  }

  /**
   * when created
   * @return timestamp
   */
  public Long getCreatedOnDb() {
    return this.createdOnDb;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * displayExtension of attribute, e.g. Expire Date
   * @return display extension
   */
  public String getDisplayExtensionDb() {
    return displayExtension;
  }

  /**
   * displayExtension of attribute, e.g. Expire Date
   * @param displayExtension1
   */
  public void setDisplayExtensionDb(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @return display name
   */
  public String getDisplayNameDb() {
    return displayName;
  }

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @param displayName1
   */
  public void setDisplayNameDb(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * attribute definition that this is related to
   * @return the attribute def id
   */
  public String getAttributeDefId() {
    return attributeDefId;
  }

  /**
   * attribute def id that this is related to
   * @param attributeDefId
   */
  public void setAttributeDefId(String attributeDefId) {
    this.attributeDefId = attributeDefId;
  }
  
  /**
   * parent child set for calulcating
   */
  private static class AttributeDefNameSetPair implements Comparable {
    /** parent */
    private AttributeDefNameSet parent;
    /** child */
    private AttributeDefNameSet child;
    /** number of hops from one to another */
    private int depth;

    /**
     * sort these by depth so we create the path as we go
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
      return ((Integer)this.depth).compareTo(((AttributeDefNameSetPair)o).depth);
    }
    
    /**
     * find an attribute def name set, better be here
     * @param attributeDefNameSetPairs
     * @param attributeDefNameSets 
     * @param id to find
     * @return the def name set
     */
    private static AttributeDefNameSet find(List<AttributeDefNameSetPair> attributeDefNameSetPairs, 
        List<AttributeDefNameSet> attributeDefNameSets, String id) {
      for (AttributeDefNameSetPair attributeDefNameSetPair : attributeDefNameSetPairs) {
        if (StringUtils.equals(id, attributeDefNameSetPair.parent.getId())) {
          return attributeDefNameSetPair.parent;
        }
        if (StringUtils.equals(id, attributeDefNameSetPair.child.getId())) {
          return attributeDefNameSetPair.child;
        }
      }
      for (AttributeDefNameSet attributeDefNameSet : GrouperUtil.nonNull(attributeDefNameSets)) {
        if (StringUtils.equals(id, attributeDefNameSet.getId())) {
          return attributeDefNameSet;
        }
      }
      String attributeDefIfName = "<unknown>";
      String attributeDefThenName = "<unknown>";
      try {
        AttributeDefNameSet attributeDefNameSet = GrouperDAOFactory.getFactory()
          .getAttributeDefNameSet().findById(id, true);
        attributeDefIfName = attributeDefNameSet.getIfHasAttributeDefName().getName();
        attributeDefThenName = attributeDefNameSet.getThenHasAttributeDefName().getName();
      } catch (Exception e) {
        attributeDefIfName = "<exception: " + e.getMessage() + ">";
        attributeDefThenName = "<exception: " + e.getMessage() + ">";
      }
      throw new RuntimeException("Cant find attribute def name set with id: " + id
          + " ifName: " + attributeDefIfName + ", thenName: " + attributeDefThenName);
    }
    
    /**
     * find an attribute def name set, better be here
     * @param attributeDefNameSetPairs
     * @param attributeDefNameSets 
     * @param ifHasId 
     * @param thenHasId 
     * @param depth is the depth expecting
     * @return the def name set
     */
    private static AttributeDefNameSet find(List<AttributeDefNameSetPair> attributeDefNameSetPairs,
        List<AttributeDefNameSet> attributeDefNameSets, String ifHasId, String thenHasId, int depth) {
      //are we sure we are getting the right one here???
      for (AttributeDefNameSetPair attributeDefNameSetPair : attributeDefNameSetPairs) {
        if (StringUtils.equals(ifHasId, attributeDefNameSetPair.parent.getIfHasAttributeDefNameId())
            && StringUtils.equals(thenHasId, attributeDefNameSetPair.parent.getThenHasAttributeDefNameId())
            && depth == attributeDefNameSetPair.parent.getDepth()) {
          return attributeDefNameSetPair.parent;
        }
        if (StringUtils.equals(ifHasId, attributeDefNameSetPair.child.getIfHasAttributeDefNameId())
            && StringUtils.equals(thenHasId, attributeDefNameSetPair.child.getThenHasAttributeDefNameId())
            && depth == attributeDefNameSetPair.child.getDepth()) {
          return attributeDefNameSetPair.child;
        }
      }
      for (AttributeDefNameSet attributeDefNameSet : GrouperUtil.nonNull(attributeDefNameSets)) {
        if (StringUtils.equals(ifHasId, attributeDefNameSet.getIfHasAttributeDefNameId())
            && StringUtils.equals(thenHasId, attributeDefNameSet.getThenHasAttributeDefNameId())
            && depth == attributeDefNameSet.getDepth()) {
          return attributeDefNameSet;
        }
      }
      throw new RuntimeException("Cant find attribute def name set with ifHasId: " 
          + ifHasId + ", thenHasId: " + thenHasId + ", depth: " + depth);
    }
    
  }
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeDefName.class);

  /**
   * 
   * @param newAttributeDefName
   * return true if added, false if already there
   */
  public boolean addToAttributeDefNameSet(AttributeDefName newAttributeDefName) {
    
    //TODO check to see if it was already added
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Adding to attribute set " + this.getName() + "\n  (" + this.getId() + ")" 
          + " this attribute: " + newAttributeDefName.getName() + " (" + newAttributeDefName.getId() + ")");
    }
    
    //lets see what implies having this existing def name
    Set<AttributeDefNameSet> existingAttributeDefNameSetList = 
      GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByThenHasAttributeDefNameId(this.getId());

    //lets see what having this new def name implies
    Set<AttributeDefNameSet> newAttributeDefNameSetList = 
      GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(newAttributeDefName.getId());
    
    List<AttributeDefNameSetPair> attributeDefNameSetPairs = new ArrayList<AttributeDefNameSetPair>();
    List<AttributeDefNameSet> newSets = new ArrayList<AttributeDefNameSet>();
    
    //now lets merge the two lists
    //they each must have one member
    for (AttributeDefNameSet parent : existingAttributeDefNameSetList) {
      for (AttributeDefNameSet child : newAttributeDefNameSetList) {
        AttributeDefNameSetPair attributeDefNameSetPair = new AttributeDefNameSetPair();
        
        attributeDefNameSetPair.depth = 1 + parent.getDepth() + child.getDepth();

        attributeDefNameSetPair.parent = parent;
        attributeDefNameSetPair.child = child;
        attributeDefNameSetPairs.add(attributeDefNameSetPair);
        if (LOG.isDebugEnabled()) {
          AttributeDefName ifHasAttributeDefName = parent.getIfHasAttributeDefName();
          AttributeDefName thenHasAttributeDefName = child.getThenHasAttributeDefName();
          LOG.debug("Found pair to manage " + ifHasAttributeDefName.getName() 
              + "\n  (parent set: " + parent.getId() + ", ifHasNameId: " + ifHasAttributeDefName.getId() + ")"
              + "\n  to: " + thenHasAttributeDefName.getName()
              + "(child set: " + child.getId() + ", thenHasNameId: " + thenHasAttributeDefName.getId() + ")"
              + "\n  depth: " + attributeDefNameSetPair.depth
          );
        }
        
      }
    }

    //sort by depth so we process correctly
    Collections.sort(attributeDefNameSetPairs);
    
    //if has circular, then do more queries to be sure
    boolean hasCircularReference = false;
    OUTER: for (AttributeDefNameSetPair attributeDefNameSetPair : attributeDefNameSetPairs) {
      
      //check for circular reference
      if (StringUtils.equals(attributeDefNameSetPair.parent.getIfHasAttributeDefNameId(), 
          attributeDefNameSetPair.child.getThenHasAttributeDefNameId())
          && attributeDefNameSetPair.depth > 0) {
        if (LOG.isDebugEnabled()) {
          AttributeDefName attributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
          LOG.debug("Found circular reference, skipping " + attributeDefName.getName() 
              + "\n  (" + attributeDefNameSetPair.parent.getIfHasAttributeDefNameId() 
              + ", depth: " + attributeDefNameSetPair.depth + ")");
        }
        hasCircularReference = true;
        //dont want to point to ourselves, circular reference, skip it
        continue;
      }
      
      AttributeDefNameSet attributeDefNameSet = new AttributeDefNameSet();
      attributeDefNameSet.setId(GrouperUuid.getUuid());
      attributeDefNameSet.setDepth(attributeDefNameSetPair.depth);
      attributeDefNameSet.setIfHasAttributeDefNameId(attributeDefNameSetPair.parent.getIfHasAttributeDefNameId());
      attributeDefNameSet.setThenHasAttributeDefNameId(attributeDefNameSetPair.child.getThenHasAttributeDefNameId());
      attributeDefNameSet.setType(attributeDefNameSet.getDepth() == 1 ? 
          AttributeDefAssignmentType.immediate : AttributeDefAssignmentType.effective);

      if (LOG.isDebugEnabled()) {
        AttributeDefName ifHasAttributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
        AttributeDefName thenHasAttributeDefName = attributeDefNameSetPair.child.getThenHasAttributeDefName();
        
        LOG.debug("Adding pair " + attributeDefNameSet.getId() + ",\n  ifHas: " 
            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
            + attributeDefNameSetPair.depth);
      }

      //if a->a, parent is a
      //if a->b, parent is a
      //if a->b->c->d, then parent of a->d is a->c
      if (attributeDefNameSetPair.child.getDepth() == 0) {
        attributeDefNameSet.setParentAttrDefNameSetId(attributeDefNameSetPair.parent.getId());
      } else {
        
        //check for same destination circular reference
        if (StringUtils.equals(attributeDefNameSetPair.parent.getThenHasAttributeDefNameId(), 
            attributeDefNameSetPair.child.getThenHasAttributeDefNameId())
            && attributeDefNameSetPair.parent.getDepth() > 0 && attributeDefNameSetPair.child.getDepth() > 0) {
          if (LOG.isDebugEnabled()) {
            AttributeDefName ifHasAttributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
            AttributeDefName thenHasAttributeDefName = attributeDefNameSetPair.child.getThenHasAttributeDefName();
            
            LOG.debug("Found same destination circular reference, skipping " + attributeDefNameSet.getId() + ",\n  ifHas: " 
                + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
                + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
                + attributeDefNameSetPair.depth);
          }
          hasCircularReference = true;
          //dont want to point to ourselves, circular reference, skip it
          continue;
        }

      }
      
      //if we found a circular reference in the lower levels, see if we are circling in on ourselves
      if (hasCircularReference) {
        int timeToLive = 1000;
        //loop through parents and children and look for overlap
        AttributeDefNameSet currentParentParent = attributeDefNameSetPair.parent;
        while(timeToLive-- > 0) {
          
          AttributeDefNameSet currentChildParent = attributeDefNameSetPair.child;
          while(timeToLive-- > 0) {
          
            if (StringUtils.equals(currentChildParent.getThenHasAttributeDefNameId(), 
                currentParentParent.getThenHasAttributeDefNameId())) {
              if (LOG.isDebugEnabled()) {
                AttributeDefName attributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
                LOG.debug("Found inner circular reference, skipping " + attributeDefName.getName() 
                    + "\n  (" + attributeDefNameSetPair.parent.getIfHasAttributeDefNameId() 
                    + ", depth: " + attributeDefNameSetPair.depth + ")");
              }
              //dont want to point to in a circle, circular reference, skip it
              continue OUTER;
              
            }
            
            
            AttributeDefNameSet previousChildParent = currentChildParent;
            currentChildParent = currentChildParent.getParentAttributeDefSet();
            //all the way up the chain
            if (currentChildParent == previousChildParent) {
              break;
            }

          }
          
          AttributeDefNameSet previousParentParent = currentParentParent;
          currentParentParent = currentParentParent.getParentAttributeDefSet();
          //all the way up the chain
          if (currentParentParent == previousParentParent) {
            break;
          }

        }
          
        if (timeToLive <= 0) {
          throw new RuntimeException("TimeToLive too low! " + timeToLive);
        }
      }
      
      //if we still need to do this
      if (StringUtils.isBlank(attributeDefNameSet.getParentAttrDefNameSetId())) {

        //find the parent of the child
        AttributeDefNameSet parentOfChild = AttributeDefNameSetPair.find(attributeDefNameSetPairs,
            newSets,
            attributeDefNameSetPair.child.getParentAttrDefNameSetId());

        //check for circular reference
        if (StringUtils.equals(attributeDefNameSetPair.parent.getIfHasAttributeDefNameId(), 
            parentOfChild.getThenHasAttributeDefNameId())
            && attributeDefNameSetPair.depth > 1) {
          if (LOG.isDebugEnabled()) {
            AttributeDefName attributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
            LOG.debug("Found parent circular reference, skipping " + attributeDefName.getName() 
                + "\n  (" + attributeDefNameSetPair.parent.getIfHasAttributeDefNameId() 
                + ", depth: " + attributeDefNameSetPair.depth + ")");
          }
          hasCircularReference = true;
          //dont want to point to ourselves, circular reference, skip it
          continue;
        }

        //find the set for the parent start to child parent end
        AttributeDefNameSet parent = AttributeDefNameSetPair.find(attributeDefNameSetPairs, newSets,
            attributeDefNameSetPair.parent.getIfHasAttributeDefNameId(), 
            parentOfChild.getThenHasAttributeDefNameId(), attributeDefNameSetPair.depth-1);
        
        attributeDefNameSet.setParentAttrDefNameSetId(parent.getId());
      }
      
      if (LOG.isDebugEnabled()) {
        AttributeDefName ifHasAttributeDefName = attributeDefNameSetPair.parent.getIfHasAttributeDefName();
        AttributeDefName thenHasAttributeDefName = attributeDefNameSetPair.child.getThenHasAttributeDefName();
        AttributeDefNameSet parent = attributeDefNameSet.getParentAttributeDefSet();
        AttributeDefName parentIfHasAttributeDefName = parent.getIfHasAttributeDefName();
        AttributeDefName parentThenHasAttributeDefName = parent.getThenHasAttributeDefName();
        
        LOG.debug("Added pair " + attributeDefNameSet.getId() + ",\n  ifHas: " 
            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + "), parent: "
            + attributeDefNameSet.getParentAttrDefNameSetId() + ",\n  parentIfHas: "
            + parentIfHasAttributeDefName.getName() + "(" + parentIfHasAttributeDefName.getId() + "),\n  parentThenHas: "
            + parentThenHasAttributeDefName.getName() + "(" + parentThenHasAttributeDefName.getId() + ")");
      }

      attributeDefNameSet.saveOrUpdate();
      newSets.add(attributeDefNameSet);
    }
    return true;
  }

  /**
   * 
   * @param newAttributeDefName
   * @return true if removed, false if already removed
   */
  public boolean removeFromAttributeDefNameSet(AttributeDefName newAttributeDefName) {
    
    //TODO test removing already removed
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing from attribute set " + this.getName() + "\n  (" + this.getId() + ")" 
          + " this attribute: " + newAttributeDefName.getName() + " (" + newAttributeDefName.getId() + ")");
    }
    
    //lets see what implies having this existing def name
    Set<AttributeDefNameSet> candidateAttributeDefNameSetToRemove = 
      GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenHasAttributeDefNameId(this.getId(), newAttributeDefName.getId());

    Set<AttributeDefNameSet> attributeDefNameSetWillRemove = new HashSet<AttributeDefNameSet>();
    Set<String> attributeDefNameSetIdsWillRemove = new HashSet<String>();

    AttributeDefNameSet setToRemove = AttributeDefNameSet.findInCollection(
        candidateAttributeDefNameSetToRemove, this.getId(), newAttributeDefName.getId(), 1, false);
    
    if (setToRemove == null) {
      return false;
    }
    
    attributeDefNameSetWillRemove.add(setToRemove);
    attributeDefNameSetIdsWillRemove.add(setToRemove.getId());
    candidateAttributeDefNameSetToRemove.remove(setToRemove);
    
    int setToRemoveSize = attributeDefNameSetWillRemove.size();
    int timeToLive = 100;
    while (timeToLive-- > 0) {
      Iterator<AttributeDefNameSet> iterator = candidateAttributeDefNameSetToRemove.iterator();
      
      //see if any parents destroyed
      while (iterator.hasNext()) {
        AttributeDefNameSet attributeDefNameSet = iterator.next();
        //if the parent is there, it is gone
        if (attributeDefNameSetIdsWillRemove.contains(attributeDefNameSet.getParentAttrDefNameSetId())) {
          attributeDefNameSetWillRemove.add(attributeDefNameSet);
          attributeDefNameSetIdsWillRemove.add(attributeDefNameSet.getId());
          iterator.remove();
        }
      }
      
      //if we didnt make progress, we are done
      if(setToRemoveSize == attributeDefNameSetWillRemove.size()) {
        break;
      }
      
      setToRemoveSize = attributeDefNameSetWillRemove.size();
    }
    
    if (timeToLive <= 0) {
      throw new RuntimeException("TimeToLive is under 0");
    }
    
    //reverse sort by depth
    List<AttributeDefNameSet> setsToRemove = new ArrayList<AttributeDefNameSet>(attributeDefNameSetWillRemove);
    Collections.sort(setsToRemove, new Comparator<AttributeDefNameSet>() {

      public int compare(AttributeDefNameSet o1, AttributeDefNameSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    Collections.reverse(setsToRemove);
    
    for (AttributeDefNameSet attributeDefNameSet : setsToRemove) {
      if (LOG.isDebugEnabled()) {
        AttributeDefName ifHasAttributeDefName = attributeDefNameSet.getIfHasAttributeDefName();
        AttributeDefName thenHasAttributeDefName = attributeDefNameSet.getThenHasAttributeDefName();
        LOG.debug("Deleting set " + attributeDefNameSet.getId() + ",\n  ifHas: " 
            + ifHasAttributeDefName.getName() + "(" + ifHasAttributeDefName.getId() + "),\n  thenHas: "
            + thenHasAttributeDefName.getName() + "(" + thenHasAttributeDefName.getId() + ")\n  depth: " 
            + attributeDefNameSet.getDepth());
      }
      attributeDefNameSet.delete();
    }
    return true;
  }
  
}
