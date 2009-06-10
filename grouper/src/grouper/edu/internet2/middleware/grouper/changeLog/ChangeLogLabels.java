/*
 * @author mchyzer
 * $Id: ChangeLogLabels.java,v 1.1 2009-06-10 05:31:35 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;


/**
 *
 */
public class ChangeLogLabels {

  /**
   * label for group type add
   */
  public static enum GROUP_TYPE_ADD implements ChangeLogLabel {

    /** id of the group type */
    id, 
    
    /** name */
    name;
  };
  
  /** labels for a group type update */
  public static enum GROUP_TYPE_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,

    /** new value of the property */
    propertyNewValue;
  }
      
  /**
   * labels for group type delete
   */
  public static enum GROUP_TYPE_DELETE implements ChangeLogLabel {
    
    /** id of the group type */
    id,
    
    /** name */
    name;
  };

  /**
   * label for group add
   */
  public static enum GROUP_ADD implements ChangeLogLabel {

    /** id of the group type */
    id, 
    
    /** name */
    name,
    
    /** parent stem id */
    parentStemId,
    
    /** display name */
    displayName,
    
    /** description */
    description;
  };
  
  /** labels for a group update */
  public static enum GROUP_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** parent stem id */
    parentStemId,
    
    /** display name */
    displayName,
    
    /** display extension (not stored in col, but yes stored in diffs on update */
    displayExtension,
    
    /** description */
    description,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  }
      
  /**
   * labels for group delete
   */
  public static enum GROUP_DELETE implements ChangeLogLabel {
    
    /** id of the group */
    id,
    
    /** name */
    name,
    
    /** parent stem id */
    parentStemId, 

    /** display name */
    displayName, 
    
    /** description */
    description;
  };

}
