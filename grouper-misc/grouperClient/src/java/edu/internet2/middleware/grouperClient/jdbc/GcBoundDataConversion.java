package edu.internet2.middleware.grouperClient.jdbc;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;


/**
 * <pre>This is our helper to convert data to and from Oracle. It is externalized because it will likely be 
 * common that editing will need to be done on a per project basis.</pre>
 */
public interface GcBoundDataConversion {

  
  /**
   * Add the value to the prepared statement, doing any casting, etc needed.
   * @param preparedStatement is the statement to add the value to.
   * @param bindVar is the bindvar to add.
   * @param index is the indes of the statement to add it to.
   */
  public void addBindVariableToStatement(PreparedStatement preparedStatement, Object bindVar, int index);
  
  
  /**
   * Set the value of a field, do any casting, etc needed.
   * @param instance is the instance to set the value to.
   * @param field is the field taking the value.
   * @param value is the value to set.
   */
  public void setFieldValue(Object instance, Field field, Object value);
  
  
  /**
   * Cast and manipulate the value returned from the database to get it into the appropriate value for assignment.
   * @param <T> is the type to return.
   * @param clazz is the type to return.
   * @param value is the value to cast or manipulate.
   * @return the manipulated value.
   */
  public <T> T getFieldValue(Class<T> clazz, Object value);
  

}
