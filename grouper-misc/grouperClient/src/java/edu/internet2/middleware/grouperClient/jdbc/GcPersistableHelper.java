package edu.internet2.middleware.grouperClient.jdbc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;


/**
 *
 * @author harveycg
 */
public class GcPersistableHelper {

  /**
   * cache oracle name conversion
   */
  private static Map<String, String> oracleStandardNameFromJavaCache = new HashMap<String, String>();
  
  
	/**
	 * Get the oracle underscore name e.g. javaNameHere -> JAVA_NAME_HERE.
	 * @param javaName is the java convention name.
	 * @return the oracle underscore name based on the java name.
	 */
	public static String oracleStandardNameFromJava(String javaName) {

	  String resultString = oracleStandardNameFromJavaCache.get(javaName);
	  
	  if (resultString == null) {
	  
  		if ((javaName == null) || (0 == "".compareTo(javaName))) {
  			return javaName;
  		}

  		StringBuilder result = new StringBuilder();
  
  		//if package is specified, only look at class name
  		if (javaName.indexOf(".") > -1){
  			javaName = javaName.substring(javaName.lastIndexOf(".") +1, javaName.length());
  		}
  
  		//dont check the first char
  		result.append(javaName.charAt(0));
  
  		char currChar;
  
  		//loop through the string, looking for uppercase
  		for (int i = 1; i < javaName.length(); i++) {
  			currChar = javaName.charAt(i);
  
  			//if uppcase append an underscore
  			if ((currChar >= 'A') && (currChar <= 'Z')) {
  				result.append("_");
  			}
  
  			result.append(currChar);
  		}
  
  		//this is in upper-case
  		resultString = result.toString().toUpperCase();
  		oracleStandardNameFromJavaCache.put(javaName, resultString);
	  }
	  return resultString;
	}
	
	/**
	 * cache column names from field
	 */
	private static Map<Field, String> columnNameCache = new HashMap<Field, String>();

	/**
	 * Get the name of the column to store the field value in from the Persistable annotation or field name.
	 * @param field is the field to get the name from.
	 * @return the column name.
	 */
	public static String columnName(Field field){
	  
	  String columnName = columnNameCache.get(field);
	  
	  if (columnName == null) {
	  
  		GcPersistableField persistable = GcPersistableHelper.findPersistableAnnotation(field);
  		if (persistable != null && !"".equals(persistable.columnName())){
  			columnName = persistable.columnName();
  		} else {
  		  columnName = oracleStandardNameFromJava(field.getName());
  		}
  		
  		columnNameCache.put(field, columnName);
	  }
	  
	  return columnName;
	}

	/**
	 * Whether the field is a primary key, check Persistable annotation if there is one on the field, default false.
	 * @param field is the field to check.
	 * @return true if so.
	 */
	public static boolean isPrimaryKey(Field field){
		GcPersistableField persistable = GcPersistableHelper.findPersistableAnnotation(field);
		return persistable != null ? persistable.primaryKey() : false;
	}
	
	
	
	 /**
   * Whether the field is a compound primary key, check Persistable annotation if there is one on the field, default false.
   * @param field is the field to check.
   * @return true if so.
   */
  public static boolean isCompoundPrimaryKey(Field field){
    GcPersistableField persistable = GcPersistableHelper.findPersistableAnnotation(field);
    return persistable != null ? persistable.compoundPrimaryKey() : false;
  }
	
	/**
	 * Get the name of the database table to store the object in from the Persistable annotation or the class name.
	 * @param clazz is the field to check.
	 * @return true if so.
	 */
	public static String tableName(Class<? extends Object> clazz){
		GcPersistableClass persistableClass = GcPersistableHelper.findPersistableClassAnnotation(clazz);
		if (persistableClass != null && !"".equals(persistableClass.tableName())){
			return persistableClass.tableName();
		}
		return oracleStandardNameFromJava(clazz.getName());
	}


	/**
	 * Whether the class has at least one Persistable(Field or Class) annnotation on the class itself or on any field.
	 * @param clazz is the field to check.
	 * @return true if so.
	 */
	public static boolean hasPersistableAnnotation(Class<? extends Object> clazz){
		GcPersistableClass persistable = GcPersistableHelper.findPersistableClassAnnotation(clazz);
		if (persistable != null){
			return true;
		}
		for (Field field : heirarchicalFields(clazz)){
			if (field.isAnnotationPresent(GcPersistableField.class)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 */
	private static Map<Class<?>, List<Field>> hierarchicalFieldsCache = new HashMap<Class<?>, List<Field>>(); 

	/**
	 * A list of heirachical fields from the entire object structure.
	 * @param clazz is the child class.
	 * @return the list.
	 */
	public static List<Field> heirarchicalFields(Class<?> clazz){
	  List<Field> result = hierarchicalFieldsCache.get(clazz);
	  if (result == null) {
	    result =  heirarchicalFields(clazz,null);
	    hierarchicalFieldsCache.put(clazz, result);
	  }
	  return result;
	}


	/**
	 * A list of heirachical fields from the entire object structure.
	 * @param clazz is the child class.
	 * @param heirarchicalFields is the list of fields to add to, pass null.
	 * @return the list.
	 */
	private static List<Field> heirarchicalFields(Class<?> clazz, List<Field> heirarchicalFields){
		if (heirarchicalFields == null){
			heirarchicalFields = new ArrayList<Field>();
		}
		for (Field field : clazz.getDeclaredFields()){
			field.setAccessible(true);
			heirarchicalFields.add(field);
		}
		if (clazz.getSuperclass() != null){
			heirarchicalFields(clazz.getSuperclass(), heirarchicalFields);
		}
		return heirarchicalFields;
	}

	/**
	 * isSelect cache
	 */
	private static Map<MultiKey, Boolean> isSelectCache = new HashMap<MultiKey, Boolean>();

	 /**
   * Whether the field should be selected from the database, check Persistable field and class level annotations.
   * @param field is the field to check.
   * @param clazz is teh type of object we are checking.
   * @return true if so.
   */
  public static boolean isSelect(Field field, Class<?> clazz){
    
    MultiKey multiKey = new MultiKey(field, clazz);
    
    Boolean result = isSelectCache.get(multiKey);
    
    if (result == null) {
    
      // We never persist static fields.
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers())){
        result = false;
      }
      
      if (result == null) {
  
        // See if a default persist has been set on the parent class and save it if so.
        GcPersist classLevelPersist = null;
        GcPersistableClass persistableClass = findPersistableClassAnnotation(clazz);
        if (persistableClass != null){
          classLevelPersist = persistableClass.defaultFieldPersist();
        }
    
        // See if there is a persistable annotation on the field - if there is,
        // then we can ask it whether we shouyld persist it or not.
        GcPersistableField persistable = GcPersistableHelper.findPersistableAnnotation(field);
        if (persistable  != null) {
          result = persistable.persist().shouldSelect(classLevelPersist, persistable);
        }
        
        // If there is not, we have to default to the parent if it exists.
        if (result == null && classLevelPersist != null){
          result = classLevelPersist.shouldSelect(null, persistable);
        }
      }
      
      if (result == null) {
        // Else, we are going to be optimistic, someone is trying to store the class to the database or hydrate it...
        result = true;
      }
      isSelectCache.put(multiKey, result);
    }
    
    return result;
  }

  /**
   * isPersist cache
   */
  private static Map<MultiKey, Boolean> isPersistCache = new HashMap<MultiKey, Boolean>();

	/**
	 * Whether the field should be persisted to the database, check Persistable field and class level annotations.
	 * @param field is the field to check.
	 * @param clazz is teh type of object we are checking.
	 * @return true if so.
	 */
	public static boolean isPersist(Field field, Class<?> clazz){

	  MultiKey multiKey = new MultiKey(field, clazz);
	  
	  Boolean result = isPersistCache.get(multiKey);
	  
	  if (result == null) {
	    	  
  		// We never persist static fields.
  		if (java.lang.reflect.Modifier.isStatic(field.getModifiers())){
  			result = false;
  		}

  		if (result == null) {
    		// See if a default persist has been set on the parent class and save it if so.
    		GcPersist classLevelPersist = null;
    		GcPersistableClass persistableClass = findPersistableClassAnnotation(clazz);
    		if (persistableClass != null){
    			classLevelPersist = persistableClass.defaultFieldPersist();
    		}
    
    		// See if there is a persistable annotation on the field - if there is,
    		// then we can ask it whether we shouyld persist it or not.
    		GcPersistableField persistable = GcPersistableHelper.findPersistableAnnotation(field);
    		if (persistable  != null){
    			result = persistable.persist().shouldPersist(classLevelPersist, persistable);
    		}
    		
    		// If there is not, we have to default to the parent if it exists.
    		if (result == null && classLevelPersist != null){
    			result = classLevelPersist.shouldPersist(null, persistable);
    		}
  		}
  		
  		if (result == null) {
    		// Else, we are going to be optimistic, someone is trying to store the class to the database or hydrate it...
    	  result = true;
  		}
  		
  	  isPersistCache.put(multiKey, result);
	  }
	  
	  return result;
	}

	/**
	 * Find the PersistableClass annotation on the class, or parent classes, return null if there is not one.
	 * @param clazz is the class to find it on.
	 * @return the annotation or null.
	 */
	public static GcPersistableClass findPersistableClassAnnotation(Class<? extends Object> clazz){

	  GcPersistableClass result = clazz.getAnnotation(GcPersistableClass.class);
	  
	  if (result != null) {
	    return result;
	  }

		Class<?> superClass = clazz.getSuperclass();
		while (superClass != null){
			result = superClass.getAnnotation(GcPersistableClass.class);
	    if (result != null) {
	      return result;
	    }
			
			superClass = superClass.getSuperclass();
		}
		return null;
	}


	/**
	 * Find the Persistable annotation on the field, return null if there is not one.
	 * @param field is the field to find it on.
	 * @return the annotation or null.
	 */
	public static GcPersistableField findPersistableAnnotation(Field field){
		return field.getAnnotation(GcPersistableField.class);
	}

	/**
	 * cache the class to the field.  if there is no field, array with null entry.  If field, it is in the first place in array
	 */
	private static Map<Class<?>, Field[]> primaryKeyFieldCache = new HashMap<Class<?>, Field[]>(); 
	
	/**
	 * Get the field that has been specified as the primary key - will return null if the class has compound column primary keys.
	 * @param clazz is the class to check for the field on.
	 * @return the primary key field.
	 */
	public static Field primaryKeyField(Class<? extends Object> clazz){
	  
	  Field[] resultArray = primaryKeyFieldCache.get(clazz);
	  
	  if (resultArray == null) {
	  
	    resultArray = new Field[1];
	    boolean foundField = false;
	    
  		for (Field field : heirarchicalFields(clazz)){
  			if (isPrimaryKey(field)){
  				resultArray[0] = field;
  				foundField = true;
  				break;
  			}
  		}
  		
  		if (!foundField && GcPersistableHelper.findPersistableClassAnnotation(clazz).hasNoPrimaryKey()){
  			foundField = true;
  		}
  		
  		if (!foundField && compoundPrimaryKeyFields(clazz).size() > 0){
        foundField = true;
  		}
  		if (!foundField) {
  		  throw new RuntimeException("No field with a " + GcPersistableField.class.getSimpleName() + " annotation with a primary key field can be found!");
  		}
  		primaryKeyFieldCache.put(clazz, resultArray);
	  }
	  return resultArray[0];
	}
	
	/**
	 * compound primary key fields cache
	 */
	private static Map<Class<? extends Object>, List<Field>> compoundPrimaryKeyFieldsCache = new HashMap<Class<? extends Object>, List<Field>>();
	
	 /**
   * Get the fields that have been specified as compound primary key fields.
   * @param clazz is the class to check for the field on.
   * @return the primary key field.
   */
  public static List<Field> compoundPrimaryKeyFields(Class<? extends Object> clazz){
    
    List<Field> result = compoundPrimaryKeyFieldsCache.get(clazz);
    
    if (result == null) {
    
      result = new ArrayList<Field>();
      for (Field field : heirarchicalFields(clazz)){
        if (isCompoundPrimaryKey(field)){
          result.add(field);
        }
      }
      compoundPrimaryKeyFieldsCache.put(clazz, result);
    }
    
    return result;

  }
	
  
	

	/**
	 * Get the sequence name for the primary key field.
	 * @param primaryKeyField is the field with the annotation for primary key and sequence on it.
	 * @return the sequence name or a failure.
	 */
	public static String primaryKeySequenceName(Field primaryKeyField){
		GcPersistableField persistable = findPersistableAnnotation(primaryKeyField);
		if (persistable == null){
			throw new RuntimeException("The field " + primaryKeyField + " does not have the annotation " + GcPersistableField.class.getSimpleName() + " set!");
		}

		if (!persistable.primaryKey()){
			throw new RuntimeException("The field " + primaryKeyField + " does not have the primary key property set on the annotation " + GcPersistableField.class.getSimpleName() + "!");
		}

		if ("".equals(persistable.primaryKeySequenceName())){
			throw new RuntimeException("The field " + primaryKeyField + " does not have the primary key sequence property set on the annotation " + GcPersistableField.class.getSimpleName() + "!");
		}

		return persistable.primaryKeySequenceName();
	}


	/**
	 * Whether the primary key is manually assigned.
	 * @param primaryKeyField is the field with the annotation for primary key and sequence on it.
	 * @return the sequence name or a failure.
	 */
	public static boolean primaryKeyManuallyAssigned(Field primaryKeyField){
		GcPersistableField persistable = findPersistableAnnotation(primaryKeyField);
		if (persistable == null){
			throw new RuntimeException("The field " + primaryKeyField + " does not have the annotation " + GcPersistableField.class.getSimpleName() + " set!");
		}

		if (!persistable.primaryKey()){
			throw new RuntimeException("The field " + primaryKeyField + " does not have the primary key property set on the annotation " + GcPersistableField.class.getSimpleName() + "!");
		}

		return persistable.primaryKeyManuallyAssigned() || persistable.compoundPrimaryKey();

	}

}
