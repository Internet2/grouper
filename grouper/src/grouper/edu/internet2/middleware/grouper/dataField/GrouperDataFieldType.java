package edu.internet2.middleware.grouper.dataField;

import java.sql.Timestamp;
import java.util.Map; 

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.dictionary.GrouperDictionaryDao;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperDataFieldType {

  string {

    @Override
    public Object convertValueHelper(Long theNumber, String theString) {
      return theString;
    }

    @Override
    public Object convertValueHelper(Object value) {
      return GrouperUtil.stringValue(value);
    }

    @Override
    public void assignValueHelper(GrouperDataFieldAssign grouperDataFieldAssign,
         Object value, Map<String, Long> dictionaryTextToInternalId) {
      String valueString = GrouperUtil.stringValue(value);
      Long dictionaryId = dictionaryTextToInternalId == null ? null : dictionaryTextToInternalId.get(valueString);
      if (dictionaryId == null) {
        dictionaryId = GrouperDictionaryDao.findOrAdd(valueString);
      }
      grouperDataFieldAssign.setValueDictionaryInternalId(dictionaryId);
    }
    
    @Override
    public void assignValueHelper(GrouperDataRowFieldAssign grouperDataRowFieldAssign,
        Object value, Map<String, Long> dictionaryTextToInternalId) {
      String valueString = GrouperUtil.stringValue(value);
      Long dictionaryId = dictionaryTextToInternalId == null ? null : dictionaryTextToInternalId.get(valueString);
      if (dictionaryId == null) {
        dictionaryId = GrouperDictionaryDao.findOrAdd(valueString);
      }
      grouperDataRowFieldAssign.setValueDictionaryInternalId(dictionaryId);
    }

    @Override
    public String convertToUiFriendlyString(Long valueInteger, String valueString) {
      return GrouperUtil.trimToEmpty(valueString);
    }
    
    
  },
  
  integer {

    @Override
    public Object convertValueHelper(Long theNumber, String theString) {
      return theNumber;
    }

    @Override
    public Object convertValueHelper(Object value) {
      return GrouperUtil.longObjectValue(value, true);
    }
    @Override
    public void assignValueHelper(GrouperDataFieldAssign grouperDataFieldAssign,
        Object value, Map<String, Long> dictionaryTextToInternalId) {
      Long valueLong = GrouperUtil.longObjectValue(value, true);
      grouperDataFieldAssign.setValueInteger(valueLong);
    }
    @Override
    public void assignValueHelper(GrouperDataRowFieldAssign grouperDataRowFieldAssign,
        Object value, Map<String, Long> dictionaryTextToInternalId) {
      Long valueLong = GrouperUtil.longObjectValue(value, true);
      grouperDataRowFieldAssign.setValueInteger(valueLong);
    }
    
    @Override
    public String convertToUiFriendlyString(Long valueInteger, String valueString) {
      if (valueInteger == null) {
        return "";
      }
      return String.valueOf(valueInteger);
    }
  },
  
  timestamp  {

    @Override
    public Object convertValueHelper(Long theNumber, String theString) {
      return theNumber;
    }
    
    @Override
    public Object convertValueHelper(Object value) {
      Timestamp theTimestamp = GrouperUtil.timestampObjectValue(value, true);
      return theTimestamp;
    }
    @Override
    public void assignValueHelper(GrouperDataFieldAssign grouperDataFieldAssign,
        Object value, Map<String, Long> dictionaryTextToInternalId) {
      Long valueLong = GrouperUtil.longObjectValue(value, true);
      grouperDataFieldAssign.setValueInteger(valueLong);
    }
    @Override
    public void assignValueHelper(GrouperDataRowFieldAssign grouperDataRowFieldAssign,
        Object value, Map<String, Long> dictionaryTextToInternalId) {
      Long valueLong = GrouperUtil.longObjectValue(value, true);
      grouperDataRowFieldAssign.setValueInteger(valueLong);
    }
    
    @Override
    public String convertToUiFriendlyString(Long valueInteger, String valueString) {
      if (valueInteger == null) {
        return "";
      }
      Timestamp theTimestamp = GrouperUtil.timestampObjectValue(valueInteger, true);
      //TODO make sure it's same as enable/disable date time on the UI
      return theTimestamp.toString(); 
    }
    
  },
  
  /**
   * 1 is true 0 is false
   */
  bool {

    @Override
    public Object convertValueHelper(Long theNumber, String theString) {
      return theNumber == null ? null : theNumber.intValue() == 1;
    }
    
    @Override
    public Object convertValueHelper(Object value) {
      return GrouperUtil.booleanObjectValue(value);
    }
    @Override
    public void assignValueHelper(GrouperDataFieldAssign grouperDataFieldAssign,
        Object value, Map<String, Long> dictionaryTextToInternalId) {
      Boolean valueBoolean = GrouperUtil.booleanObjectValue(value);
      grouperDataFieldAssign.setValueInteger(valueBoolean == null ? null : (valueBoolean ? 1L : 0L));
    }
    @Override
    public void assignValueHelper(GrouperDataRowFieldAssign grouperDataRowFieldAssign,
        Object value, Map<String, Long> dictionaryTextToInternalId) {
      Boolean valueBoolean = GrouperUtil.booleanObjectValue(value);
      grouperDataRowFieldAssign.setValueInteger(valueBoolean == null ? null : (valueBoolean ? 1L : 0L));
    }
    
    @Override
    public String convertToUiFriendlyString(Long valueInteger, String valueString) {
      if (valueInteger == null) {
        return "";
      }
      if (valueInteger == 1L) {
        //TODO: externalize text it
        return "True";
      } else if (valueInteger == 0L) {
        return "False";
      }
      
      return "Invalid";
    }

  };
  
  /**
   * 
   * @param theNumber
   * @param theString
   * @return
   */
  public abstract Object convertValueHelper(Long theNumber, String theString);

  public Object convertValue(Long theNumber, String theString) {
    
    Object value = convertValueHelper(theNumber, theString);
    if (value == null) {
      value = Void.TYPE;
    }
    return value;
  }

  public Object convertValue(Object providerValue) {
    Object value = convertValueHelper(providerValue);
    if (value == null) {
      value = Void.TYPE;
    }
    return value;
  }

  /**
   * 
   * @param theNumber
   * @param theString
   * @return
   */
  public abstract Object convertValueHelper(Object providerValue);
  
  /**
   * 
   * @param type
   * @param exceptionOnNull
   * @return field type
   */
  public static GrouperDataFieldType valueOfIgnoreCase(String type, boolean exceptionOnNull) {

    if (StringUtils.equals("boolean", type)) {
      return bool;
    }
    
    GrouperDataFieldType fieldType = GrouperUtil.enumValueOfIgnoreCase(GrouperDataFieldType.class, 
        type, exceptionOnNull);
    
    return fieldType;
  }
  
  /**
   * 
   * @param grouperDataFieldAssign
   * @param value
   * @param dictionaryTextToInternalId optional, if not null, then use this to get the dictionary id
   */
  public void assignValue(GrouperDataFieldAssign grouperDataFieldAssign, Object value, Map<String, Long> dictionaryTextToInternalId ) {
    grouperDataFieldAssign.setValueDictionaryInternalId(null);
    grouperDataFieldAssign.setValueInteger(null);
    if (value == Void.TYPE) {
      return;
    }
    assignValueHelper(grouperDataFieldAssign, value, dictionaryTextToInternalId);
  }

  /**
   * 
   * @param grouperDataRowFieldAssign
   * @param value
   * @param dictionaryTextToInternalId
   */
  public void assignValue(GrouperDataRowFieldAssign grouperDataRowFieldAssign, Object value, Map<String, Long> dictionaryTextToInternalId) {
    grouperDataRowFieldAssign.setValueDictionaryInternalId(null);
    grouperDataRowFieldAssign.setValueInteger(null);
    if (value == Void.TYPE) {
      return;
    }
    assignValueHelper(grouperDataRowFieldAssign, value, dictionaryTextToInternalId);
  }

  public abstract void assignValueHelper(GrouperDataFieldAssign grouperDataFieldAssign,
      Object value, Map<String, Long> dictionaryTextToInternalId);

  public abstract void assignValueHelper(GrouperDataRowFieldAssign grouperDataRowFieldAssign,
      Object value, Map<String, Long> dictionaryTextToInternalId);

  public abstract String convertToUiFriendlyString(Long valueInteger, String valueString);


}
