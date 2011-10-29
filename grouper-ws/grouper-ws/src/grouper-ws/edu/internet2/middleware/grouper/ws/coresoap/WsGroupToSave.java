/**
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * <pre>
 * Class to save a group via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsGroupToSave {

  /** stem lookup (blank if insert) */
  private WsGroupLookup wsGroupLookup;

  /** stem to save */
  private WsGroup wsGroup;

  /** T or F (null if F) */
  private String createParentStemsIfNotExist;
  
  /**
   * if should create parent stems if not exist
   * @return T or F or null (F)
   */
  public String getCreateParentStemsIfNotExist() {
    return this.createParentStemsIfNotExist;
  }

  /**
   * if should create parent stems if not exist
   * @param createParentStemsIfNotExist1 T or F or null (F)
   */
  public void setCreateParentStemsIfNotExist(String createParentStemsIfNotExist1) {
    this.createParentStemsIfNotExist = createParentStemsIfNotExist1;
  }

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsGroupToSave.class);

  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private String saveMode;

  /**
   * what ended up happening
   */
  @XStreamOmitField
  private SaveResultType saveResultType;

  /**
   * get the save type
   * @return save type
   */
  public SaveResultType saveResultType() {
    return this.saveResultType;
  }
  
  /**
   * 
   */
  public WsGroupToSave() {
    // empty constructor
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * validate the settings (e.g. that booleans are set correctly)
   */
  public void validate() {
    try {
      if (!StringUtils.isBlank(this.saveMode)) {
        //make sure it exists
        SaveMode.valueOfIgnoreCase(this.saveMode);
      }
    } catch (RuntimeException e) {
      throw new WsInvalidQueryException("Problem with save mode: " + e.getMessage()
          + ", " + this, e);
    }
  }

  /**
   * save this group
   * 
   * @param grouperSession
   *            to save
   * @return the stem that was inserted or updated
   * @throws StemNotFoundException 
   * @throws GroupNotFoundException
   * @throws GroupNotFoundException
   * @throws StemAddException 
   * @throws GroupAddException
   * @throws InsufficientPrivilegeException
   * @throws GroupModifyException
   * @throws GroupAddException
   * @throws AttributeNotFoundException 
   * @throws MemberDeleteException 
   */
  public Group save(GrouperSession grouperSession) {

    Group group = null;
      
    try {
      SaveMode theSaveMode = SaveMode.valueOfIgnoreCase(this.saveMode);
  
      if (SaveMode.INSERT != theSaveMode && this.getWsGroupLookup() == null) {
        throw new WsInvalidQueryException(
            "wsGroupLookup is required to save a group (probably just put the name in it)");
      }
       
      if (this.getWsGroupLookup() == null) {
        this.setWsGroupLookup(new WsGroupLookup());
      }
       
      this.getWsGroupLookup().retrieveGroupIfNeeded(grouperSession);
  
      Group groupLookedup = this.getWsGroupLookup().retrieveGroup();
  
      String groupNameLookup = groupLookedup == null ? null : groupLookedup.getName();
  
      GroupSave groupSave = new GroupSave(grouperSession);
      groupSave.assignTypeOfGroup(TypeOfGroup.valueOfIgnoreCase(this.getWsGroup().getTypeOfGroup(), false));
      groupSave.assignGroupNameToEdit(groupNameLookup);
      groupSave.assignUuid(this.getWsGroup().getUuid()).assignName(this.getWsGroup().getName());
      groupSave.assignDisplayExtension(this.getWsGroup().getDisplayExtension());
      groupSave.assignDescription(this.getWsGroup().getDescription());
      groupSave.assignSaveMode(theSaveMode);
      groupSave.assignCreateParentStemsIfNotExist(GrouperUtil.booleanValue(this.getCreateParentStemsIfNotExist(), false));
      
      group = groupSave.save();
      
      this.saveResultType = groupSave.getSaveResultType();
      boolean isInsert = this.saveResultType == SaveResultType.INSERT;

      //lets do attributes and types
      WsGroupDetail wsGroupDetail = this.getWsGroup().getDetail();
      
      //see if detail exists
      if (wsGroupDetail != null) {
        
        //first, types
        String[] typeNames = wsGroupDetail.getTypeNames();
        int typeNamesLength = GrouperUtil.length(typeNames);
        Set<GroupType> typesPassedIn = new TreeSet<GroupType>(); 
        Set<GroupType> typesAlreadyInGroup = new TreeSet<GroupType>(group.getTypes());
        StringBuilder typesPassedInBuilder = new StringBuilder();
        for (int i=0;i<typeNamesLength;i++) {
          GroupType groupType = GroupTypeFinder.find(typeNames[i], true);
          typesPassedIn.add(groupType);
          if (i != 0) {
            typesPassedInBuilder.append(", ");
          }
          typesPassedInBuilder.append(groupType.getName());
          if (!group.hasType(groupType)) {
            LOG.debug("Group:" + group.getName() + ": adding type: " + groupType);
            group.addType(groupType);
            this.saveResultType = isInsert ? this.saveResultType : SaveResultType.UPDATE;
          }
        }
        if (LOG.isDebugEnabled()) {
          LOG.debug("Group:" + group.getName() + ": passed in with types: " + typesPassedInBuilder);
        }

        typesPassedInBuilder = new StringBuilder();
        int i=0;
        for (GroupType groupType : typesAlreadyInGroup) {
          if (i != 0) {
            typesPassedInBuilder.append(", ");
          }
          typesPassedInBuilder.append(groupType.getName());
          i++;
        }

        if (LOG.isDebugEnabled()) {
          LOG.debug("Group:" + group.getName() + ": already had types: " + typesPassedInBuilder);
        }
        
        //then, attributes add
        String[] attributeNames = wsGroupDetail.getAttributeNames();
        String[] attributeValues = wsGroupDetail.getAttributeValues();
        
        int attributeNamesLength = GrouperUtil.length(attributeNames);
        int attributeValuesLength = GrouperUtil.length(attributeValues);
        
        if (attributeNamesLength != attributeValuesLength) {
          throw new WsInvalidQueryException("Attribute name length " + attributeNamesLength 
              + " is not equal to attribute value length " + attributeValuesLength);
        }
        
        boolean groupDirty = false;
        Set<String> attributeNamesPassedIn = new HashSet<String>();
        
        //find attributes to add to the group
        for (i=0;i<attributeNamesLength;i++) {
          
          String attributeName = attributeNames[i];
          String attributeValue = attributeValues[i];
          
          if (!StringUtils.equals(group.getAttributeValue(attributeName, false, false), attributeValue)) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Group: " + group.getName() + ": updating attribute: " 
                + attributeName + ": " + attributeValue);
            }
            group.setAttribute(attributeName, attributeValue);
            this.saveResultType = isInsert ? this.saveResultType : SaveResultType.UPDATE;
            groupDirty = true;
          }
          
          attributeNamesPassedIn.add(attributeName);
        }
        //find attributes to remove
        Map<String, Attribute> attributes = group.getAttributesMap(true);
        for (String key : new HashSet<String>(attributes.keySet())) {
          
          //these are built in attributes, dont touch
          if (StringUtils.equals(GrouperConfig.ATTRIBUTE_NAME, key)
            || StringUtils.equals(GrouperConfig.ATTRIBUTE_EXTENSION, key)
            || StringUtils.equals(GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION, key)
            || StringUtils.equals(GrouperConfig.ATTRIBUTE_DESCRIPTION, key)
            || StringUtils.equals(GrouperConfig.ATTRIBUTE_DISPLAY_NAME, key)) {
            continue;
          }
          
          //see if in the passed in set
          if (!attributeNamesPassedIn.contains(key)) {
            groupDirty = true;
            Field field = FieldFinder.find(key, true);
            GroupType groupType = field.getGroupType();
            if (LOG.isDebugEnabled()) {
              LOG.debug("Group: " + group.getName() + ": delete attribute: " + key 
                + ", groupType: " + groupType + ", groupHasType? " + group.hasType(groupType));
            }
            group.deleteAttribute(key);
            this.saveResultType = isInsert ? this.saveResultType : SaveResultType.UPDATE;
          }
          
          
        }
        
        if (groupDirty) {
          group.store();
        }

        //now delete types
        typesAlreadyInGroup = GrouperUtil.nonNull(typesAlreadyInGroup);
        LOG.debug("Group:" + group.getName() + ": already had types: " + GrouperUtil.length(typesAlreadyInGroup));
        int index = 0;
        for (GroupType groupType : typesAlreadyInGroup) {
          
          if (!typesPassedIn.contains(groupType)) {
            if (!groupType.isSystemType()) {
              if (group.hasType(groupType)) {
                if (LOG.isDebugEnabled()) {
                  LOG.debug("Group:" + group.getName() + ": deleting type: " + groupType + " index: " + index);
                }
                this.saveResultType = isInsert ? this.saveResultType : SaveResultType.UPDATE;
                group.deleteType(groupType);
              }
            }
          }
          index++;
        }


        //######################
        //Composites
        String compositeType = wsGroupDetail.getCompositeType();
        boolean hasComposite = GrouperUtil.booleanValue(wsGroupDetail.getHasComposite(), false);
        if (hasComposite) {
          
          if (StringUtils.isBlank(compositeType)) {
            throw new WsInvalidQueryException("compositeType cannot be blank if hasComposite is T");
          }
          CompositeType theCompositeType = null;
          
          if (StringUtils.equalsIgnoreCase("COMPLEMENT", compositeType)) {
            theCompositeType = CompositeType.COMPLEMENT;
          } else if (StringUtils.equalsIgnoreCase("UNION", compositeType)) {
            theCompositeType = CompositeType.UNION;
          } else if (StringUtils.equalsIgnoreCase("INTERSECTION", compositeType)) {
            theCompositeType = CompositeType.INTERSECTION;
          } else {
            throw new WsInvalidQueryException("compositeType must be COMPLEMENT, UNION, or INTERSECTION, not '" 
                + compositeType + "'");
          }
          
          //get left and right
          if (wsGroupDetail.getLeftGroup() == null) {
            throw new WsInvalidQueryException("if has composite, left group cannot be null");
          }
          if (wsGroupDetail.getRightGroup() == null) {
            throw new WsInvalidQueryException("if has composite, right group cannot be null");
          }
          WsGroupLookup leftGroupLookup = new WsGroupLookup(wsGroupDetail.getLeftGroup());
          WsGroupLookup rightGroupLookup = new WsGroupLookup(wsGroupDetail.getRightGroup());
          
          Group leftGroup = leftGroupLookup.retrieveGroupIfNeeded(grouperSession, "left group");
          Group rightGroup = rightGroupLookup.retrieveGroupIfNeeded(grouperSession, "right group");
          
  
          Composite composite = group.getComposite(false);
          boolean needsChange = composite == null;
          if (composite != null) {
            if (!theCompositeType.equals(composite.getType())) {
              needsChange = true;
            }
            if (!StringUtils.equals(composite.getLeftFactorUuid(), leftGroup.getUuid())) {
              needsChange = true;
            }
            if (!StringUtils.equals(composite.getRightFactorUuid(), rightGroup.getUuid())) {
              needsChange = true;
            }
          }
          if (needsChange) {
            this.saveResultType = isInsert ? this.saveResultType : SaveResultType.UPDATE;
            String prefix = composite != null ? "Changing" : "Adding";
            if (LOG.isDebugEnabled()) {
              LOG.debug(prefix + " composite group for group: " + group.getName() + 
                ": " + compositeType + ", " + leftGroup.getName() + ", " + rightGroup.getName());
            }
            if (composite != null) {
              group.deleteCompositeMember();
            }
            group.addCompositeMember(theCompositeType, leftGroup, rightGroup);
          }
          
        } else {
          
          if (!StringUtils.isBlank(compositeType)) {
            throw new WsInvalidQueryException("compositeType must be blank if hasComposite is blank or F");
          }
          if (group.hasComposite()) {
            this.saveResultType = isInsert ? this.saveResultType : SaveResultType.UPDATE;
            group.deleteCompositeMember();
          }
          
        }
          
        
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    return group;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @return the saveMode
   */
  public String getSaveMode() {
    return this.saveMode;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param saveMode1 the saveMode to set
   */
  public void setSaveMode(String saveMode1) {
    this.saveMode = saveMode1;
  }

  /**
   * @return the wsGroupLookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }

  /**
   * @param wsGroupLookup1 the wsGroupLookup to set
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
  }

  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }
}
