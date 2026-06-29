package edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Represents a named UNIQUE CONSTRAINT on a table.
 *
 * This is intentionally a first-class concept that is distinct from a
 * {@link UniqueIndex}.  ddlutils historically modeled all uniqueness as a unique
 * <i>index</i> (see {@link UniqueIndex}), but some databases (notably Oracle and
 * PostgreSQL) require the referenced columns of a foreign key to be backed by a
 * unique (or primary key) <i>constraint</i> rather than a bare unique index.  A
 * unique constraint is therefore modeled, emitted, and read back separately so
 * the database-compare can keep it in sync and so foreign keys can reference it.
 *
 * The columns are reused from {@link IndexColumn} so that column comparison logic
 * matches that of indices, but a UniqueConstraint is NOT an {@link Index} and is
 * never stored in the table's index collection.
 */
public class UniqueConstraint implements Serializable {

  /** Unique ID for serialization purposes. */
  private static final long serialVersionUID = -2032214192310201044L;

  /** The name of the constraint. */
  protected String _name;

  /**
   * Optional name of an existing unique index that should back this constraint,
   * emitted as a <code>USING INDEX</code> clause. This is an emit-time detail only
   * (it pins the constraint to a specific, separately-named index, e.g. on Oracle)
   * and is deliberately NOT part of the constraint's identity -- a constraint read
   * back from the catalog has no usingIndexName, so it must still compare equal to
   * a declared constraint that does.
   */
  protected String _usingIndexName;

  /** The columns making up the constraint, as {@link IndexColumn} entries. */
  protected ArrayList _columns = new ArrayList();

  /**
   * Returns the name of this constraint.
   *
   * @return the constraint name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of this constraint.
   *
   * @param name the constraint name
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * Returns the name of the existing index this constraint should be pinned to via
   * a <code>USING INDEX</code> clause, or null to let the database pick/create one.
   *
   * @return the backing index name, or null
   */
  public String getUsingIndexName() {
    return _usingIndexName;
  }

  /**
   * Sets the name of the existing index this constraint should be pinned to via a
   * <code>USING INDEX</code> clause.
   *
   * @param usingIndexName the backing index name
   */
  public void setUsingIndexName(String usingIndexName) {
    _usingIndexName = usingIndexName;
  }

  /**
   * Returns the number of columns in this constraint.
   *
   * @return the number of columns
   */
  public int getColumnCount() {
    return _columns.size();
  }

  /**
   * Returns the column at the given position.
   *
   * @param idx the position
   * @return the column
   */
  public IndexColumn getColumn(int idx) {
    return (IndexColumn) _columns.get(idx);
  }

  /**
   * Returns the columns of this constraint.
   *
   * @return the columns
   */
  public IndexColumn[] getColumns() {
    return (IndexColumn[]) _columns.toArray(new IndexColumn[_columns.size()]);
  }

  /**
   * Determines whether this constraint includes the given column.
   *
   * @param column the column to look for
   * @return <code>true</code> if the column is part of this constraint
   */
  public boolean hasColumn(Column column) {
    for (int idx = 0; idx < _columns.size(); idx++) {
      IndexColumn curColumn = getColumn(idx);

      if (column.equals(curColumn.getColumn())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds a column to this constraint, keeping the columns ordered by ordinal
   * position (mirrors {@link IndexImpBase#addColumn(IndexColumn)}).
   *
   * @param column the column to add
   */
  public void addColumn(IndexColumn column) {
    if (column != null) {
      for (int idx = 0; idx < _columns.size(); idx++) {
        IndexColumn curColumn = getColumn(idx);

        if (curColumn.getOrdinalPosition() > column.getOrdinalPosition()) {
          _columns.add(idx, column);
          return;
        }
      }
      _columns.add(column);
    }
  }

  /**
   * Removes the given column from this constraint.
   *
   * @param column the column to remove
   */
  public void removeColumn(IndexColumn column) {
    _columns.remove(column);
  }

  /**
   * Removes the column at the given position.
   *
   * @param idx the position of the column to remove
   */
  public void removeColumn(int idx) {
    _columns.remove(idx);
  }

  /**
   * {@inheritDoc}
   */
  public Object clone() throws CloneNotSupportedException {
    UniqueConstraint result = new UniqueConstraint();

    result._name = _name;
    result._usingIndexName = _usingIndexName;
    result._columns = (ArrayList) _columns.clone();

    return result;
  }

  /**
   * {@inheritDoc}
   */
  public boolean equals(Object obj) {
    if (obj instanceof UniqueConstraint) {
      UniqueConstraint other = (UniqueConstraint) obj;

      return new EqualsBuilder().append(_name, other._name)
          .append(_columns, other._columns)
          .isEquals();
    }
    return false;
  }

  /**
   * Compares this constraint to another, ignoring the case of the name. The
   * column ordering and names must match. Mirrors
   * {@link UniqueIndex#equalsIgnoreCase(Index)} so the comparator behaves the
   * same way it does for unique indices.
   *
   * @param other the constraint to compare to
   * @return <code>true</code> if they are equal ignoring name case
   */
  public boolean equalsIgnoreCase(UniqueConstraint other) {
    if (other == null) {
      return false;
    }

    boolean checkName = (_name != null) && (_name.length() > 0) &&
        (other._name != null) && (other._name.length() > 0);

    if ((!checkName || _name.equalsIgnoreCase(other._name)) &&
        (getColumnCount() == other.getColumnCount())) {
      for (int idx = 0; idx < getColumnCount(); idx++) {
        if (!getColumn(idx).equalsIgnoreCase(other.getColumn(idx))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public int hashCode() {
    return _columns.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    StringBuffer result = new StringBuffer();

    result.append("Unique constraint [name=");
    result.append(getName());
    result.append("; ");
    result.append(getColumnCount());
    result.append(" columns]");

    return result.toString();
  }

  /**
   * Returns a verbose string representation of this constraint.
   *
   * @return the string representation
   */
  public String toVerboseString() {
    StringBuffer result = new StringBuffer();

    result.append("Unique constraint [");
    result.append(getName());
    result.append("] columns:");
    for (int idx = 0; idx < getColumnCount(); idx++) {
      result.append(" ");
      result.append(getColumn(idx).toString());
    }

    return result.toString();
  }
}
