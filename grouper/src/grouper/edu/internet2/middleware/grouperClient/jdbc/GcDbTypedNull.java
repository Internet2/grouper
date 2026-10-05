package edu.internet2.middleware.grouperClient.jdbc;

/**
 * <pre>A bind variable that is null but knows what kind of column it goes into.
 *
 * A plain null bind variable is bound with setObject(index, null), the driver has to guess the type.
 * Postgres binds it as unknown and lets the server decide, which is fine, but oracle can reject an
 * untyped null going into a BLOB column.  When GcDbAccess stores a GcPersistableClass bean it knows the
 * java type of each field, so a null byte[] field is passed as BINARY and bound with
 * setBytes(index, null), which each driver turns into its own binary null (bytea, RAW/BLOB, BLOB).
 *
 * You can also use this with sql + addBindVar() when the column is binary and the value might be null:
 *
 *   new GcDbAccess().sql("update some_table set some_blob = ? where id = ?")
 *     .addBindVar(bytes == null ? GcDbTypedNull.BINARY : bytes).addBindVar(id).executeSql();
 *
 * A plain addBindVar(null) still works like it always did (setObject(index, null)).
 * </pre>
 */
public final class GcDbTypedNull {

  /**
   * null for a binary column (postgres bytea, oracle BLOB/RAW, mysql BLOB/VARBINARY), from a byte[] field
   */
  public static final GcDbTypedNull BINARY = new GcDbTypedNull(byte[].class);

  /**
   * java type of the field this null came from
   */
  private final Class<?> javaType;

  /**
   * only the constants above
   * @param javaType1
   */
  private GcDbTypedNull(Class<?> javaType1) {
    this.javaType = javaType1;
  }

  /**
   * java type of the field this null came from
   * @return the java type
   */
  public Class<?> getJavaType() {
    return this.javaType;
  }

  /**
   * if this field value is null and its type needs a typed null bind, return the marker, else return the value
   * @param javaType declared type of the field
   * @param value field value
   * @return the value or a typed null marker
   */
  public static Object convertNullForBind(Class<?> javaType, Object value) {
    if (value == null && javaType == byte[].class) {
      return BINARY;
    }
    return value;
  }

  /**
   * so logs of bind variables show null
   */
  @Override
  public String toString() {
    return "null";
  }
}
