package edu.internet2.middleware.grouperClient.jdbc;


import java.sql.Date;
import java.sql.Timestamp;
import java.util.LinkedHashMap;

/**
 * Map with string key which ignores key case and has some convenience methods.
 * @author harveycg
 */
public class GcCaseIgnoreHashMap extends LinkedHashMap<String, Object> {

  
  /**
   * Version id.
   */
  private static final long serialVersionUID = -8918564071661874921L;


  /**
   * <pre>This is our helper to convert data to and from Oracle. It is externalized because it will likely be 
   * common that editing will need to be done on a per project basis.</pre>
   */
  private static GcBoundDataConversion boundDataConversion = new GcBoundDataConversionImpl();
  
  
  /**
   * This is the helper to convert data to and from Oracle, which has a default of BoundDataConversionImpl. 
   * If you encounter errors getting and setting data from oracle to java, you may need to override the default
   * and set your version here. Otherwise, nothing is needed.
   * @param _boundDataConversion the boundDataConversion to set.
   */
  public static void loadBoundDataConversion(GcBoundDataConversion _boundDataConversion) {
    boundDataConversion = _boundDataConversion;
  }
  
  
  /**
   * Put an object into the map.
   */
   @Override
   public Object put(String key, Object value) {
    return super.put(key.toLowerCase(), value);
   }
   
   
   /**
    * Get an object from the map.
    */
   @Override
   public Object get(Object key) {
     return super.get(key.toString().toLowerCase());
   }
   

   /**
    * Get a string from the map.
    * @param key is the key of the object.
    * @return the object.
    */
   public String getString(Object key) {
     Object value = super.get(key.toString().toLowerCase());
     if (value == null){
       return null;
     }
     return String.valueOf(super.get(key.toString().toLowerCase()));
   }
   

   /**
    * Get a long from the map.
    * @param key is the key of the object.
    * @return the object.
    */
   public Long getLong(Object key) {
     return boundDataConversion.getFieldValue(Long.class, super.get(key.toString().toLowerCase()));
   }
   

   /**
    * Get an integer  from the map.
    * @param key is the key of the object.
    * @return the object.
    */
   public Integer getInteger(Object key) {
     return boundDataConversion.getFieldValue(Integer.class, super.get(key.toString().toLowerCase()));
  }
   
   
   /**
    * Get a timestamp from the map.
    * @param key is the key of the object.
    * @return the object.
    */
   public Timestamp getTimestamp(Object key) {
     return boundDataConversion.getFieldValue(Timestamp.class, super.get(key.toString().toLowerCase()));
   }
   
   
   /**
    * Get a date from the map.
    * @param key is the key of the object.
    * @return the object.
    */
   public Date getDate(Object key) {
     return boundDataConversion.getFieldValue(Date.class, super.get(key.toString().toLowerCase()));
   }
   
   
   /**
    * Get a date from the map.
    * @param key is the key of the object.
    * @return the object.
    */
   public Double getDouble(Object key) {
     return boundDataConversion.getFieldValue(Double.class, super.get(key.toString().toLowerCase()));
   }

}
