/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Result for finding a group
 * 
 * @author mchyzer
 * 
 */
public class GuiGroupDetail {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /** if this group has a direct composite member, T|F */
  private String hasComposite = null;

  /** left group if composite (note, detail will never be there) */
  private GuiGroup leftGroup = null;

  /** right group if composite (note, detail will never be there) */
  private GuiGroup rightGroup = null;

  /** types of this group */
  private String[] typeNames;

  /** attribute names, not including the ones listed in the group result or detail */
  private String[] attributeNames;

  /** attribute values, not including ones listed in the group result or detail */
  private String[] attributeValues;

  /** should be UNION, COMPLEMENT, INTERSECTION */
  private String compositeType;
  
  /**
   * should be UNION, COMPLEMENT, INTERSECTION
   * @return type
   */
  public String getCompositeType() {
    return this.compositeType;
  }

  /**
   * should be UNION, COMPLEMENT, INTERSECTION
   * @param compositeType1
   */
  public void setCompositeType(String compositeType1) {
    this.compositeType = compositeType1;
  }

  /**
   * types of this gruop
   * @return the typeNames
   */
  public String[] getTypeNames() {
    return this.typeNames;
  }

  /**
   * types of this group
   * @param typeNames1 the typeNames to set
   */
  public void setTypeNames(String[] typeNames1) {
    this.typeNames = typeNames1;
  }

  /**
   * attribute names, not including the ones listed in the group result or detail
   * @return the attributeNames
   */
  public String[] getAttributeNames() {
    return this.attributeNames;
  }

  /**
   * attribute names, not including the ones listed in the group result or detail
   * @param attributeNames1 the attributeNames to set
   */
  public void setAttributeNames(String[] attributeNames1) {
    this.attributeNames = attributeNames1;
  }

  /**
   * attribute names, not including the ones listed in the group result or detail
   * @return the attributeValues
   */
  public String[] getAttributeValues() {
    return this.attributeValues;
  }

  /**
   * attribute names, not including the ones listed in the group result or detail
   * @param attributeValues1 the attributeValues to set
   */
  public void setAttributeValues(String[] attributeValues1) {
    this.attributeValues = attributeValues1;
  }

  /**
   * no arg constructor
   */
  public GuiGroupDetail() {
    // blank

  }

  /**
   * construct based on group, assign all fields
   * 
   * @param group is what to construct from
   */
  @SuppressWarnings("unchecked")
  public GuiGroupDetail(Group group) {
    if (group != null) {
      //this group method isnt implemented, so dont send in web service
      //this.setCreateSourceId(group.getCreateSource());
      String createSubjectIdString = null;
      try {
        Subject createSubject = group.getCreateSubject();
        createSubjectIdString = createSubject == null ? null : createSubject.getId();
      } catch (SubjectNotFoundException e) {
        // dont do anything if not found, null
      }
      this.setCreateSubjectId(createSubjectIdString);
      this.setCreateTime(GuiUtils.dateToString(group.getCreateTime()));
      this.setIsCompositeFactor(GuiUtils.booleanToStringOneChar(group
          .isComposite()));
      boolean groupHasComposite = group.hasComposite();
      this.setHasComposite(GuiUtils.booleanToStringOneChar(groupHasComposite));

      //get the composite factors
      if (groupHasComposite) {
        Composite composite = null;

        try {
          composite = group.getComposite();
        } catch (CompositeNotFoundException cnfe) {
          //this means something bad is happening
          throw new RuntimeException(cnfe);
        }

        this.setCompositeType(composite.getType().getName());
        
        try {
          this.setLeftGroup(new GuiGroup(composite.getLeftGroup(), false));
          this.setRightGroup(new GuiGroup(composite.getRightGroup(), false));
        } catch (GroupNotFoundException gnfe) {
          //this means something bad is happening
          throw new RuntimeException(gnfe);
        }
      }
      //note modify source is not in the grouper api anymore..., since you get the 
      //modify member, then get the source from the member object
      this.setModifySource(null);

      String modifySubjectIdString = null;
      try {
        Subject modifySubject = group.getModifySubject();
        modifySubjectIdString = modifySubject == null ? null : modifySubject.getId();
      } catch (SubjectNotFoundException e) {
        // dont do anything if not found, null
      }

      this.setModifySubjectId(modifySubjectIdString);
      this.setModifyTime(GuiUtils.dateToString(group.getModifyTime()));

      //set the types
      Set<GroupType> groupTypes = new TreeSet<GroupType>(group.getTypes());
      
      try {
        GroupType baseType = GroupTypeFinder.find("base");
        groupTypes.remove(baseType);
      } catch (SchemaException se) {
        throw new RuntimeException(se);
      }

      this.typeNames = new String[GrouperUtil.length(groupTypes)];
      int i = 0;
      for (GroupType groupType : GrouperUtil.nonNull(groupTypes)) {
        this.typeNames[i++] = groupType.getName();
      }

      //set the attributes
      Map<String, String> attributeMap = new TreeMap<String, String>(group.getAttributes());

      //remove common attributes to not take redundant space in response
      attributeMap.remove(GrouperConfig.ATTR_NAME);
      attributeMap.remove(GrouperConfig.ATTR_EXTENSION);
      attributeMap.remove(GrouperConfig.ATTR_DISPLAY_EXTENSION);
      attributeMap.remove(GrouperConfig.ATTR_DISPLAY_NAME);
      attributeMap.remove(GrouperConfig.ATTR_DESCRIPTION);

      //find attributes, set in arrays in order
      if (attributeMap.size() > 0) {
        String[] theAttributeNames = new String[attributeMap.size()];
        String[] theAttributeValues = new String[attributeMap.size()];
        i = 0;
        for (String attributeName : attributeMap.keySet()) {
          theAttributeNames[i] = attributeName;
          theAttributeValues[i] = attributeMap.get(attributeName);
          i++;
        }
        this.setAttributeNames(theAttributeNames);
        this.setAttributeValues(theAttributeValues);
      }

    }
  }

  /**
   * id of the subject that created this group
   */
  private String createSubjectId;

  /**
   * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
   */
  private String createTime;

  /**
   * if a composite member of another group "T", else "F".
   * 
   * A composite group is composed of two groups and a set operator (stored in
   * grouper_composites table) (e.g. union, intersection, etc). A composite
   * group has no immediate members. All subjects in a composite group are
   * effective members.
   */
  private String isCompositeFactor;

  /**
   * Get (optional and questionable) modify source for this group.
   */
  private String modifySource;

  /**
   * Get subject that last modified this group.
   */
  private String modifySubjectId;

  /**
   * Get last modified time for this group. yyyy/mm/dd hh24:mi:ss.SSS
   */
  private String modifyTime;

  /**
   * id of the subject that created this group
   * 
   * @return the createSubjectId
   */
  public String getCreateSubjectId() {
    return this.createSubjectId;
  }

  /**
   * id of the subject that created this group
   * 
   * @param createSubjectId1
   *            the createSubjectId to set
   */
  public void setCreateSubjectId(String createSubjectId1) {
    this.createSubjectId = createSubjectId1;
  }

  /**
   * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
   * 
   * @return the createTime
   */
  public String getCreateTime() {
    return this.createTime;
  }

  /**
   * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
   * 
   * @param createTime1
   *            the createTime to set
   */
  public void setCreateTime(String createTime1) {
    this.createTime = createTime1;
  }

  /**
   * if a composite member of another group "T", else "F".
   * 
   * A composite group is composed of two groups and a set operator (stored in
   * grouper_composites table) (e.g. union, intersection, etc). A composite
   * group has no immediate members. All subjects in a composite group are
   * effective members.
   * 
   * @return the isCompositeFactor
   */
  public String getIsCompositeFactor() {
    return this.isCompositeFactor;
  }

  /**
   * if composite "T", else "F".
   * 
   * A composite group is composed of two groups and a set operator (stored in
   * grouper_composites table) (e.g. union, intersection, etc). A composite
   * group has no immediate members. All subjects in a composite group are
   * effective members.
   * 
   * @param isComposite1
   *            the isCompositeFactor to set
   */
  public void setIsCompositeFactor(String isComposite1) {
    this.isCompositeFactor = isComposite1;
  }

  /**
   * Get (optional and questionable) modify source for this group.
   * 
   * @return the modifySource
   */
  public String getModifySource() {
    return this.modifySource;
  }

  /**
   * Get (optional and questionable) modify source for this group.
   * 
   * @param modifySource1
   *            the modifySource to set
   */
  public void setModifySource(String modifySource1) {
    this.modifySource = modifySource1;
  }

  /**
   * Get subject that last modified this group.
   * 
   * @return the modifySubjectId
   */
  public String getModifySubjectId() {
    return this.modifySubjectId;
  }

  /**
   * Get subject that last modified this group.
   * 
   * @param modifySubjectId1
   *            the modifySubjectId to set
   */
  public void setModifySubjectId(String modifySubjectId1) {
    this.modifySubjectId = modifySubjectId1;
  }

  /**
   * Get last modified time for this group. yyyy/mm/dd hh24:mi:ss.SSS
   * 
   * @return the modifyTime
   */
  public String getModifyTime() {
    return this.modifyTime;
  }

  /**
   * Get last modified time for this group. yyyy/mm/dd hh24:mi:ss.SSS
   * 
   * @param modifyTime1
   *            the modifyTime to set
   */
  public void setModifyTime(String modifyTime1) {
    this.modifyTime = modifyTime1;
  }

  /**
   * if this group has a composite member, T|F
   * @return the hasComposite
   */
  public String getHasComposite() {
    return this.hasComposite;
  }

  /**
   * if this group has a composite member, T|F
   * @param hasComposite1 the hasComposite to set
   */
  public void setHasComposite(String hasComposite1) {
    this.hasComposite = hasComposite1;
  }

  /**
   * left group if composite (note, detail will never be there)
   * @return the leftGroup
   */
  public GuiGroup getLeftGroup() {
    return this.leftGroup;
  }

  /**
   * left group if composite (note, detail will never be there)
   * @param leftGroup1 the leftGroup to set
   */
  public void setLeftGroup(GuiGroup leftGroup1) {
    this.leftGroup = leftGroup1;
  }

  /**
   * left group if composite (note, detail will never be there)
   * @return the rightGroup
   */
  public GuiGroup getRightGroup() {
    return this.rightGroup;
  }

  /**
   * left group if composite (note, detail will never be there)
   * @param rightGroup1 the rightGroup to set
   */
  public void setRightGroup(GuiGroup rightGroup1) {
    this.rightGroup = rightGroup1;
  }
}
