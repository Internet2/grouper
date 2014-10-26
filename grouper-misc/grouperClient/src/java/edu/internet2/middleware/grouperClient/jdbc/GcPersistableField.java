package edu.internet2.middleware.grouperClient.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata about fields that can be stored to the database.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GcPersistableField {
	
	/**
	 * <pre>Whether this field can be persisted to the database or not, if not set explicitly, 
	 * defaults to checking for the defaultFieldPersist setting of the class level annotation PersistableClass, which must exist in that case.</pre>
	 * @return whether to persist or not.
	 */
	GcPersist persist() default GcPersist.defaultToPersistableClassDefaultFieldPersist;
	
	/**
	 * The name of the column that this field matches in the database.
	 * @return the name.
	 */
	String columnName() default "";	
	
	/**
	 * The sequence name to populate the primary key with.
	 * @return true if so.
	 */
	String primaryKeySequenceName() default "";
	
	/**
	 * <pre>Whether this field is the primary key or not. If it is, it must be numeric 
	 * unless primaryKeyManuallyAssigned is set to true, in which case it can be any type.</pre>
	 * @return true if so.
	 */
	boolean primaryKey() default false;
	
	
	 /**
   * Whether this field is part of a compound primary key or not.
   * @return true if so.
   */
  boolean compoundPrimaryKey() default false;
	

	/**
	 * If this is a primary key, whether it is manually assigned, in which case we need to check the database every time to see if we should insert or update.
	 */
	boolean primaryKeyManuallyAssigned() default false;
}
