/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * config item metadata
 */
public enum ConfigItemMetadataType {
  
  /** true/false */
  BOOLEAN {

    @Override
    public String getStringForUi() {
      return "boolean (true or false)";
    }

    @Override
    public Object convertValue(String valueString, boolean requireValidValue) {
      try {
        return GrouperUtil.booleanValue(valueString);
      } catch (RuntimeException e) {
        if (!requireValidValue) {
          return false;
        }
        throw e;
      }
    }

    @Override
    public String validate(String value) {
      try {
        GrouperUtil.booleanValue(value, false);
        return null;
      } catch (Exception e) {
        return "grouperConfigurationValidationInvalidBoolean";
      }
    }
    
    
  }, 
  
  /** any string */
  STRING {

    @Override
    public String getStringForUi() {
      return "text";
    }
    
    @Override
    public String validate(String value) {
      // everything is a valid string
      return null;
    }

  }, 

  /** group name in system */
  GROUP {

    @Override
    public String getStringForUi() {
      return "group";
    }
    
    @Override
    public String validate(final String value) {
      try {
        
        GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            if (GroupFinder.findByUuid(grouperSession, value, false) != null) {
              return null;
            }

            if (GroupFinder.findByName(grouperSession, value, false) != null) {
              return null;
            }
            
            throw new RuntimeException();
          }
        });
        
        return null;
      } catch (Exception e) {
        return "grouperConfigurationValidationInvalidGroup";
      }
    }

  }, 

  /** folder in system */
  STEM {

    @Override
    public String getStringForUi() {
      return "folder";
    }
    
    @Override
    public String validate(final String value) {
      try {
        
        GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            if (StemFinder.findByUuid(grouperSession, value, false) != null) {
              return null;
            }

            if (StemFinder.findByName(grouperSession, value, false) != null) {
              return null;
            }
            
            throw new RuntimeException();
          }
        });
        
        return null;
      } catch (Exception e) {
        return "grouperConfigurationValidationInvalidStem";
      }
    }

  }, 
  
  /** name of attribute def in system */
  ATTRIBUTEDEF {

    @Override
    public String getStringForUi() {
      return "attribute definition";
    }
    
    @Override
    public String validate(final String value) {
      try {
        
        GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            if (AttributeDefFinder.findById(value, false) != null) {
              return null;
            }

            if (AttributeDefFinder.findByName(value, false) != null) {
              return null;
            }
            
            throw new RuntimeException();
          }
        });
        
        return null;
      } catch (Exception e) {
        return "grouperConfigurationValidationInvalidAttributeDef";
      }
    }

  }, 

  /** name of attribute def name in system */
  ATTRIBUTEDEFNAME {

    @Override
    public String getStringForUi() {
      return "attribute definition name";
    }
    
    @Override
    public String validate(final String value) {
      try {
        
        GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            if (AttributeDefNameFinder.findById(value, false) != null) {
              return null;
            }

            if (AttributeDefNameFinder.findByName(value, false) != null) {
              return null;
            }
            
            throw new RuntimeException();
          }
        });
        
        return null;
      } catch (Exception e) {
        return "grouperConfigurationValidationInvalidAttributeDefName";
      }
    }

  }, 

  /** subject id or identifier in system */
  SUBJECT {

    @Override
    public String getStringForUi() {
      return "entity (e.g. subject)";
    }
    
    @Override
    public String validate(final String value) {
      try {
        
        //  #auto-create group subject ids or identifiers, comma separated, to bootstrap the registry on startup
        //  #  (increment the integer index)
        //  # {valueType: "subject", multiple: true, regex: "^configuration\\.autocreate\\.group\\.subjects\\.[0-9]+$"}
        //  #configuration.autocreate.group.subjects.0 = johnsmith
        //
        //  # subjectIdOrIdentifier  or  sourceId::::subjectId  or  ::::subjectId  or  sourceId::::::subjectIdentifier  or  ::::::subjectIdentifier
        //  # sourceId::::::::subjectIdOrIdentifier  or  ::::::::subjectIdOrIdentifier

        GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            SubjectFinder.findByPackedSubjectString(value, true);
            
            return null;
          }
        });
        
        return null;
      } catch (Exception e) {
        return "grouperConfigurationValidationInvalidSubject";
      }
    }

  }, 
  
  /** any integer or long datatype */
  INTEGER {

    @Override
    public String getStringForUi() {
      return "integer";
    }
    
    @Override
    public Object convertValue(String valueString, boolean requireValidValue) {
      try {
        return GrouperUtil.intValue(valueString, -1);
      } catch (RuntimeException e) {
        if (!requireValidValue) {
          return -1;
        }
        throw e;
      }

    }

    @Override
    public String validate(String value) {
      try {
        GrouperUtil.intValue(value);
        return null;
      } catch (Exception e) {
        return "grouperConfigurationValidationInvalidInteger";
      }
    }

  }, 
  
  /** floating point number in system */
  FLOATING {

    @Override
    public String getStringForUi() {
      return "decimal";
    }
    
    @Override
    public Object convertValue(String valueString, boolean requireValidValue) {
      try {
        return GrouperUtil.defaultIfNull(GrouperUtil.doubleObjectValue(valueString, true), -1.0);
      } catch (RuntimeException e) {
        if (!requireValidValue) {
          return -1.0;
        }
        throw e;
      }

    }

    @Override
    public String validate(String value) {
      try {
        GrouperUtil.doubleValue(value);
        return null;
      } catch (Exception e) {
        return "grouperConfigurationValidationInvalidFloating";
      }
    }

  }, 
  
  /** password, or encrypted, or file name, or script */
  PASSWORD {

    @Override
    public String getStringForUi() {
      return "password";
    }
    
    @Override
    public String validate(String value) {
      return null;
    }

  }, 
  
  /** fully qualified class in system */
  CLASS {

    @Override
    public String getStringForUi() {
      return "Java class";
    }
    
    @Override
    public String validate(String value) {
      try {
        GrouperUtil.forName(value);
        return null;
      } catch (Exception e) {
        return "grouperConfigurationValidationInvalidClass";
      }
    }

  };
  
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static ConfigItemMetadataType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(ConfigItemMetadataType.class, 
        string, exceptionOnNull);

  }
  
  /**
   * 
   */
  public abstract String getStringForUi();

  /**
   * for EL types, convert to boolean or integers
   * @param valueString
   * @return object
   */
  public Object convertValue(String valueString, boolean requireValidValue) {
    return valueString;
  }

  /**
   * 
   * @param value
   * @return externalized text key
   */
  public abstract String validate(String value);
}
