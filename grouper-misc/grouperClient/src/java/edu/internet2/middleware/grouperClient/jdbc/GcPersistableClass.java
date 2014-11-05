package edu.internet2.middleware.grouperClient.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata about class objects that can be stored to the database.
 * @author harveycg
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GcPersistableClass {
	
	/**
	 * The name of the table that this object maps to in the database.
	 * @return the table name.
	 */
	String tableName() default "";
	
	/**
	 * Whether the default behavior for fields in this class should be to persist or to not persist, default is to persist.
	 * @return true if so.
	 */
	GcPersist defaultFieldPersist() default GcPersist.doPersist;
	
	
	/**
	 * There is no primary key for this object - saving through DbAccess always results in a new row.
	 * @return true if so.
	 */
	boolean hasNoPrimaryKey() default false;

}
