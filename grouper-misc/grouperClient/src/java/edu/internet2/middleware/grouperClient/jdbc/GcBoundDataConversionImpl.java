package edu.internet2.middleware.grouperClient.jdbc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * <pre>This is our implementation of a helper to convert data to and from Oracle. It is externalized because it will likely be 
 * common that editing will need to be done on a per project basis.</pre>
 * @author harveycg
 */
public class GcBoundDataConversionImpl implements GcBoundDataConversion {

  /**
   * Add the value to the prepared statement, doing any casting, etc needed.
   * @param preparedStatement is the statement to add the value to.
   * @param bindVar is the bindvar to add.
   * @param index is the indes of the statement to add it to.
   */
  @Override
  public void addBindVariableToStatement(PreparedStatement preparedStatement, Object bindVar, int index){

    try {
      if (bindVar == null) {
        // It is possible to bind null (e.g. for an update), so just use object to do this.
        preparedStatement.setObject(index, null);
        return;
      }

      // Get the class and try to iterate through most types.
      Class<?> clazz = bindVar.getClass();
      if (clazz == int.class || clazz == Integer.class) {
        preparedStatement.setInt(index, (Integer)bindVar);
      } else if (clazz == String.class) {
        preparedStatement.setString(index, (String)bindVar);
      }else if (clazz == double.class || clazz == Double.class) {
        preparedStatement.setDouble(index, (Double)bindVar);
      } else if (clazz == long.class || clazz == Long.class) {
        preparedStatement.setLong(index, (Long)bindVar);
      } else if (clazz == float.class || clazz == Float.class) {
        preparedStatement.setFloat(index, (Float)bindVar);
      } else if (clazz == byte.class || clazz == Byte.class) {
        preparedStatement.setByte(index, (Byte)bindVar);
      } else if (clazz == byte[].class || clazz == Byte[].class) {
        preparedStatement.setBytes(index, (byte[])bindVar);
      } else if (clazz == char.class || clazz == Character.class) {
        preparedStatement.setString(index, (String)bindVar);
      } else if (clazz == short.class || clazz == Short.class) {
        preparedStatement.setShort(index, (Short)bindVar);
      } else if (clazz == BigDecimal.class) {
        preparedStatement.setDouble(index, ((BigDecimal)bindVar).doubleValue());
      } else if (clazz == Timestamp.class) {
        // Make sure this is above instanceof Date.
        preparedStatement.setTimestamp(index, (Timestamp)bindVar);
      } else if (bindVar instanceof Date) {
        // This will handle util.Date and sql.Date.
        Timestamp timestamp = new Timestamp(((Date)bindVar).getTime());
        preparedStatement.setTimestamp(index, timestamp);
      } else {
        // This is the fallthrough.
        preparedStatement.setObject(index, bindVar);
      }
    } catch (Exception e) {
      throw new RuntimeException("Problem attaching param index: " + index + ", param: '" + bindVar + "'", e);
    }
  }

  /**
   * Set the value of a field, do any casting, etc needed.
   * @param instance is the instance to set the value to.
   * @param field is the field taking the value.
   * @param value is the value to set.
   */
  @Override
  public void setFieldValue(Object instance, Field field, Object value){

    try{

      // Nothing special needed for null values.
      if (value == null){
        field.set(instance, value);
        return;
      }

      // Special stuff needed.
      if (Date.class.isAssignableFrom(field.getType()) || java.sql.Date.class.isAssignableFrom(field.getType())) {
        value = GrouperClientUtils.toTimestamp(value);
      } else if (field.getType() == double.class || field.getType() == Double.class){
        value = Double.parseDouble(String.valueOf(value));
      } else if (field.getType() == BigDecimal .class){
        value = new BigDecimal(String.valueOf(value));
      } else if (field.getType() == long .class || field.getType() == Long .class){
        value = new Long(String.valueOf(value));
      } else if (field.getType() == int .class || field.getType() == Integer .class){
        value = new Integer(String.valueOf(value));
      } else if (field.getType() == String.class){
        if (!(value instanceof String)){
          value = String.valueOf(value);
        }
      }


      // Set the value to the field.
      if (field.getType().isPrimitive()){
        field.set(instance, value);
      } else {
        try {
          field.set(instance, field.getType().cast(value));
        } catch (RuntimeException e) {
          GrouperClientUtils.injectInException(e, instance.getClass() + ", " + field.getName() + ", " + value.getClass());
          throw e;
        }
      }

    } catch (Exception e){
      throw new RuntimeException("Issues converting data from oracle to java for field " + field.getName() + " in class " + field.getDeclaringClass() + " from type " +  value.getClass() + " to type " + field.getType(), e);
    }
  }


  /**
   * Cast and manipulate the value returned from the database to get it into the appropriate value for assignment.
   * @param <T> is the type to return.
   * @param clazz is the type to return.
   * @param value is the value to cast or manipulate.
   * @return the manipulated value.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T getFieldValue(Class<T> clazz, Object value){

    if (value ==  null){
      return null;
    }

    if (clazz.isAssignableFrom(value.getClass())){
      return (T)value;
    }

    try{
      if (value instanceof Number) {
        if (clazz == int.class || clazz == Integer.class){
          value = ((Number)value).intValue();
        } else if (clazz == double.class || clazz == Double.class){
          value = ((Number)value).doubleValue();
        } else if (clazz == long.class || clazz == Long.class){
          value = ((Number)value).longValue();
        } else if (clazz == String.class){
          value = ((Number)value).toString();
        } else {
          throw new RuntimeException("Not expecting value: " + value.getClass());
        }
      } else {
        throw new RuntimeException("Not expecting value: " + value.getClass());
      }
      return (T)value;
    } catch (Exception e){
      throw new RuntimeException(e);
    }
  }



}
