package edu.internet2.middleware.grouper.dataField;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.dictionary.GrouperDictionary;
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
        Object value) {
      String valueString = GrouperUtil.stringValue(value);
      Long dictionaryId = GrouperDictionaryDao.findOrAdd(valueString);
      grouperDataFieldAssign.setValueDictionaryInternalId(dictionaryId);
    }
    
    @Override
    public void assignValueHelper(GrouperDataRowFieldAssign grouperDataRowFieldAssign,
        Object value) {
      String valueString = GrouperUtil.stringValue(value);
      Long dictionaryId = GrouperDictionaryDao.findOrAdd(valueString);
      grouperDataRowFieldAssign.setValueDictionaryInternalId(dictionaryId);
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
        Object value) {
      Long valueLong = GrouperUtil.longObjectValue(value, true);
      grouperDataFieldAssign.setValueInteger(valueLong);
    }
    @Override
    public void assignValueHelper(GrouperDataRowFieldAssign grouperDataRowFieldAssign,
        Object value) {
      Long valueLong = GrouperUtil.longObjectValue(value, true);
      grouperDataRowFieldAssign.setValueInteger(valueLong);
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
        Object value) {
      Long valueLong = GrouperUtil.longObjectValue(value, true);
      grouperDataFieldAssign.setValueInteger(valueLong);
    }
    @Override
    public void assignValueHelper(GrouperDataRowFieldAssign grouperDataRowFieldAssign,
        Object value) {
      Long valueLong = GrouperUtil.longObjectValue(value, true);
      grouperDataRowFieldAssign.setValueInteger(valueLong);
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
        Object value) {
      Boolean valueBoolean = GrouperUtil.booleanObjectValue(value);
      grouperDataFieldAssign.setValueInteger(valueBoolean == null ? null : (valueBoolean ? 1L : 0L));
    }
    @Override
    public void assignValueHelper(GrouperDataRowFieldAssign grouperDataRowFieldAssign,
        Object value) {
      Boolean valueBoolean = GrouperUtil.booleanObjectValue(value);
      grouperDataRowFieldAssign.setValueInteger(valueBoolean == null ? null : (valueBoolean ? 1L : 0L));
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
  
  public void assignValue(GrouperDataFieldAssign grouperDataFieldAssign, Object value) {
    grouperDataFieldAssign.setValueDictionaryInternalId(null);
    grouperDataFieldAssign.setValueInteger(null);
    if (value == Void.TYPE) {
      return;
    }
    assignValueHelper(grouperDataFieldAssign, value);
  }

  
  public void assignValue(GrouperDataRowFieldAssign grouperDataRowFieldAssign, Object value) {
    grouperDataRowFieldAssign.setValueDictionaryInternalId(null);
    grouperDataRowFieldAssign.setValueInteger(null);
    if (value == Void.TYPE) {
      return;
    }
    assignValueHelper(grouperDataRowFieldAssign, value);
  }

  public abstract void assignValueHelper(GrouperDataFieldAssign grouperDataFieldAssign,
      Object value);

  public abstract void assignValueHelper(GrouperDataRowFieldAssign grouperDataRowFieldAssign,
      Object value);


}
