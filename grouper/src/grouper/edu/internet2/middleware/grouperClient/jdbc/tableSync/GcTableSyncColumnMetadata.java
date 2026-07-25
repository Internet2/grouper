/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 *
 */
public class GcTableSyncColumnMetadata {

  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(this.columnName)
        .append(this.columnType)
        .append(this.precision)
        .append(this.scale).
        append(this.columnDisplaySize).toHashCode();
  }

  /**
   * 
   */
  @Override
  public String toString() {
    return this.columnName;
  }


  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    
    if (obj == null || (!(obj instanceof GcTableSyncColumnMetadata))) {
      return false;
    }
    
    GcTableSyncColumnMetadata other = (GcTableSyncColumnMetadata)obj;
    
    return new EqualsBuilder()
        .append(this.columnName, other.columnName)
        .append(this.columnType, other.columnType)
        .append(this.precision, other.precision)
        .append(this.scale, other.scale)
        .append(this.columnDisplaySize, other.columnDisplaySize).isEquals();

  }


  /**
   * 
   */
  public static enum ColumnType {
    
    /**
     * 
     */
    NUMERIC {

      @Override
      public Object readDataFromResultSet(GcTableSyncColumnMetadata gcTableSyncColumnMetadata, ResultSet resultSet) throws SQLException {
        return resultSet.getBigDecimal(gcTableSyncColumnMetadata.getColumnName());
      }
      
      /**
       * convert to type
       */
      @Override
      public Object convertToType(Object input) {
        
        if (input == null) {
          return null;
        }
        
        if (input instanceof BigDecimal) {
          return normalizeBigDecimal((BigDecimal)input);
        }
        
        // note: java.sql.Timestamp, java.sql.Date, and java.sql.Time are all java.util.Date
        if (input instanceof Date) {
          return normalizeBigDecimal(new BigDecimal(((Date)input).getTime()));
        }
        
        if (input instanceof Boolean) {
          return ((Boolean)input).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        
        // note: go through the string representation so decimals are not truncated (e.g. Float, Double)
        String stringValue = input instanceof Number ? input.toString() : GrouperClientUtils.trim(input.toString());
        
        if (GrouperClientUtils.isBlank(stringValue)) {
          return null;
        }
        
        return normalizeBigDecimal(new BigDecimal(stringValue));
      }

    },
    
    /**
     * 
     */
    UUID {

      @Override
      public Object readDataFromResultSet(GcTableSyncColumnMetadata gcTableSyncColumnMetadata, ResultSet resultSet) throws SQLException {
        return resultSet.getObject(gcTableSyncColumnMetadata.getColumnName());
      }
      
      /**
       * convert to type
       */
      @Override
      public Object convertToType(Object input) {
        
        if (input == null) {
          return null;
        }
        
        if (input instanceof String) {
          
          java.util.UUID uuid = null;
          try {
            uuid = java.util.UUID.fromString((String)input);
          } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error converting to UUID: " + input, e);
          }
          return uuid;
        }
        
        if (input instanceof java.util.UUID) {
          return (java.util.UUID) input;
        }
        
        throw new RuntimeException("Error converting to UUID: " + input.getClass().getName() + ", '" + input + "'");
      }

    },
    
    /**
     * 
     */
    BOOLEAN {

      @Override
      public Object readDataFromResultSet(GcTableSyncColumnMetadata gcTableSyncColumnMetadata, ResultSet resultSet) throws SQLException {
        return resultSet.getBoolean(gcTableSyncColumnMetadata.getColumnName());
      }
      
      /**
       * convert to type
       */
      @Override
      public Object convertToType(Object input) {
        
        if (input instanceof Boolean) {
          return input;
        }
        
        if (GrouperClientUtils.isBlank(input)) {
          return null;
        }
        
        if (input instanceof Number) {
          return ((Number)input).longValue() != 0L;
        }
        
        return GrouperClientUtils.booleanValue(input);
      }

    },
    
    /**
     * 
     */
    STRING {

      @Override
      public Object readDataFromResultSet(GcTableSyncColumnMetadata gcTableSyncColumnMetadata, ResultSet resultSet) throws SQLException {
        return resultSet.getString(gcTableSyncColumnMetadata.getColumnName());
      }

      /**
       * convert to type
       */
      @Override
      public Object convertToType(Object input) {
        
        if (input == null) {
          return null;
        }
        
        if (input instanceof String) {
          return (String) input;
        }
        
        if (input instanceof Number) {
          // normalize so an Integer 5, a Long 5, and a BigDecimal 5.00 all look the same
          return normalizeBigDecimal(new BigDecimal(input.toString())).toPlainString();
        }
        
        if (input instanceof Timestamp) {
          return Long.toString(((Timestamp)input).getTime());
        }
        
        return input.toString();
      }
    },
    
    /**
     * 
     */
    TIMESTAMP {

      @Override
      public Object readDataFromResultSet(GcTableSyncColumnMetadata gcTableSyncColumnMetadata, ResultSet resultSet) throws SQLException {
        return resultSet.getTimestamp(gcTableSyncColumnMetadata.getColumnName());
      }
      
      /**
       * convert to type
       */
      @Override
      public Object convertToType(Object input) {
        
        if (input == null) {
          return null;
        }
        
        if (input instanceof Timestamp) {
          return (Timestamp)input;
        }
        
        // note: this covers java.util.Date, java.sql.Date, and java.sql.Time
        if (input instanceof Date) {
          return new Timestamp(((Date)input).getTime());
        }
        
        if (input instanceof Calendar) {
          return new Timestamp(((Calendar)input).getTimeInMillis());
        }
        
        if (input instanceof String) {
          
          return GrouperClientUtils.toTimestamp(input);
        }
        
        if (input instanceof Number) {
          return new Timestamp(((Number)input).longValue());
        }
        
        return new Timestamp(GrouperClientUtils.longValue(input));
      }


    };

    /**
     * read data from result set based on column index
     * @param columnOneIndexed
     * @param resultSet
     * @return the object
     * @throws SQLException
     */
    public abstract Object readDataFromResultSet(GcTableSyncColumnMetadata gcTableSyncColumnMetadata, ResultSet resultSet) throws SQLException;
    
    /**
     * convert an object to another type
     * @param input
     * @return the object
     */
    public abstract Object convertToType(Object input);
    
  }
  
  /**
   * <p>Normalize a BigDecimal so that numerically equal values are also equal by equals().
   * e.g. 5, 5.0, and 5.00 all become 5.  Note that BigDecimal.equals() and BigDecimal.hashCode() are
   * scale sensitive, and those are what the table sync compares are based on.</p>
   * @param bigDecimal
   * @return the normalized big decimal
   */
  public static BigDecimal normalizeBigDecimal(BigDecimal bigDecimal) {
    
    if (bigDecimal == null) {
      return null;
    }
    
    BigDecimal result = bigDecimal.stripTrailingZeros();
    
    // dont use scientific notation for whole numbers, e.g. 600 should not become 6E+2
    if (result.scale() < 0) {
      result = result.setScale(0);
    }
    
    return result;
  }

  /**
   * remove the time part of a timestamp
   * @param timestamp
   * @return the timestamp at midnight
   */
  private static Timestamp truncateToDay(Timestamp timestamp) {
    
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timestamp.getTime());
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    
    return new Timestamp(calendar.getTimeInMillis());
  }

  /**
   * <p>Convert a value to the canonical java type for this column so that values which come from
   * different places can be compared with equals()/hashCode().  The two sides of a sync can be
   * different database vendors (and therefore different jdbc drivers), or one side can be java
   * objects passed in programmatically (e.g. GcTableSyncFromData), so the same logical value can
   * arrive as an Integer, a Long, a BigDecimal, a String, a java.util.Date, etc.  Without this the
   * compare reports a difference and the row is updated on every run.</p>
   * 
   * <p>This is best effort and never throws.  If the value cannot be converted it is returned
   * unchanged so that behavior does not regress for types which are not handled here.</p>
   * 
   * @param input
   * @return the normalized value
   */
  public Object normalizeValue(Object input) {
    
    if (input == null || this.columnType == null) {
      return input;
    }
    
    Object result = null;
    
    try {
      result = this.columnType.convertToType(input);
    } catch (RuntimeException re) {
      // cant normalize this one, compare it as it came in
      return input;
    }
    
    // a DATE column has no time part, so dont let a time part cause a false difference
    if (this.dateOnly && result instanceof Timestamp) {
      result = truncateToDay((Timestamp)result);
    }
    
    return result;
  }

  /**
   * true if this column is a SQL DATE (i.e. has no time part) as opposed to a SQL TIMESTAMP.
   * Both map to ColumnType.TIMESTAMP.
   */
  private boolean dateOnly;

  /**
   * true if this column is a SQL DATE (i.e. has no time part) as opposed to a SQL TIMESTAMP
   * @return the dateOnly
   */
  public boolean isDateOnly() {
    return this.dateOnly;
  }

  /**
   * true if this column is a SQL DATE (i.e. has no time part) as opposed to a SQL TIMESTAMP
   * @param dateOnly1 the dateOnly to set
   */
  public void setDateOnly(boolean dateOnly1) {
    this.dateOnly = dateOnly1;
  }

  /**
   * column index zero indexed
   */
  private int columnIndexZeroIndexed = -1;
  
  /**
   * column index zero indexed
   * @return the columnIndexZeroIndexed
   */
  public int getColumnIndexZeroIndexed() {
    return this.columnIndexZeroIndexed;
  }
  
  /**
   * @param columnIndexZeroIndexed1 the columnIndexZeroIndexed to set
   */
  public void setColumnIndexZeroIndexed(int columnIndexZeroIndexed1) {
    this.columnIndexZeroIndexed = columnIndexZeroIndexed1;
  }

  /**
   * type of column
   */
  private ColumnType columnType;

  /**
   * precision of number in database
   */
  private int precision;
  
  
  /**
   * precision of number in database
   * @return the precision
   */
  public int getPrecision() {
    return this.precision;
  }

  
  /**
   * precision of number in database
   * @param precision1 the precision to set
   */
  public void setPrecision(int precision1) {
    this.precision = precision1;
  }

  /**
   * scale of number in database
   */
  private int scale;

  
  /**
   * scale of number in database
   * @return the scale
   */
  public int getScale() {
    return this.scale;
  }
  
  /**
   * scale of number in database
   * @param scale1 the scale to set
   */
  public void setScale(int scale1) {
    this.scale = scale1;
  }


  /**
   * length of string cols
   */
  private int columnDisplaySize;
  
  /**
   * length of string cols
   * @return the stringLength
   */
  public int getColumnDisplaySize() {
    return this.columnDisplaySize;
  }

  
  /**
   * length of string cols
   * @param stringLength1 the stringLength to set
   */
  public void setColumnDisplaySize(int stringLength1) {
    this.columnDisplaySize = stringLength1;
  }

  /**
   * type of column
   * @return the columnType
   */
  public ColumnType getColumnType() {
    return this.columnType;
  }


  
  /**
   * type of column
   * @param columnType1 the columnType to set
   */
  public void setColumnType(ColumnType columnType1) {
    this.columnType = columnType1;
  }


  /**
   * name of column
   */
  private String columnName;
  
  /**
   * 
   */
  public GcTableSyncColumnMetadata() {
  }

  
  /**
   * name of column
   * @return the columnName
   */
  public String getColumnName() {
    return this.columnName;
  }

  
  /**
   * name of column
   * @param columnName1 the columnName to set
   */
  public void setColumnName(String columnName1) {
    this.columnName = columnName1;
  }

  
}
