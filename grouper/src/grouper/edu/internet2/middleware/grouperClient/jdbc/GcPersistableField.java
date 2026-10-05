package edu.internet2.middleware.grouperClient.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata about fields that can be stored to the database.
 * @author harveycg
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

	/**
	 * <pre>Whether this field is the optimistic locking version column (like the hibernate
	 * &lt;version name="hibernateVersionNumber" column="hibernate_version_number"/&gt; mapping).
	 * The field must be a long or Long, and there can be at most one per class.
	 * On insert a null version is set to 0.  On update the version is incremented and the update
	 * has "and version_col = oldVersion" in the where clause, if no rows are updated a
	 * GcStaleObjectException is thrown.  deleteFromDatabase also checks the version
	 * (deleteFromDatabaseMultiple does not, it is a batch delete by primary key).
	 * The version decides insert vs update (like hibernate, no select): null or negative (e.g. -1) means
	 * new so insert, 0 or more means previously persisted so update.
	 * Note: storeBatchToDatabase / storeListToDatabase do not support versioned classes.
	 * The check can be turned off (version still incremented) with GcDbAccess.setOptimisticLocking(false),
	 * Grouper does that based on grouper.properties dao.optimisticLocking</pre>
	 * @return true if this is the version field
	 */
	boolean optimisticLockVersion() default false;
}
