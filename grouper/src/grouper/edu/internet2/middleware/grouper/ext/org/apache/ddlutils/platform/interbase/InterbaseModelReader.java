package edu.internet2.middleware.grouper.ext.org.apache.ddlutils.platform.interbase;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.Platform;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Column;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.ForeignKey;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Index;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.TypeMap;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.platform.JdbcModelReader;

/**
 * The Jdbc Model Reader for Interbase.
 *
 * @version $Revision: $
 */
public class InterbaseModelReader extends JdbcModelReader
{
    /**
     * Creates a new model reader for Interbase databases.
     * 
     * @param platform The platform that this model reader belongs to
     */
    public InterbaseModelReader(Platform platform)
    {
        super(platform);
        setDefaultCatalogPattern(null);
        setDefaultSchemaPattern(null);
        setDefaultTablePattern("%");
        setDefaultColumnPattern("%");
    }

    /**
     * {@inheritDoc}
     */
    protected Table readTable(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
    {
        Table table = super.readTable(metaData, values);

        if (table != null)
        {
            determineExtraColumnInfo(table);
            determineAutoIncrementColumns(table);
            adjustColumns(table);
        }

        return table;
    }

    /**
     * {@inheritDoc}
     */
    protected Collection readColumns(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        ResultSet columnData = null;

        try
        {
            List columns = new ArrayList();

            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                // Jaybird has a problem when delimited identifiers are used as
                // it is not able to find the columns for the table
                // So we have to filter manually below
                columnData = metaData.getColumns(getDefaultTablePattern(), getDefaultColumnPattern());

                while (columnData.next())
                {
                    Map values = readColumns(columnData, getColumnsForColumn());

                    if (tableName.equals(values.get("TABLE_NAME")))
                    {
                        columns.add(readColumn(metaData, values));
                    }
                }
            }
            else
            {
                columnData = metaData.getColumns(tableName, getDefaultColumnPattern());

                while (columnData.next())
                {
                    Map values = readColumns(columnData, getColumnsForColumn());

                    columns.add(readColumn(metaData, values));
                }
            }

            return columns;
        }
        finally
        {
            if (columnData != null)
            {
                columnData.close();
            }
        }
    }

    /**
     * Helper method that determines extra column info from the system tables: default value, precision, scale.
     *
     * @param table The table
     */
    protected void determineExtraColumnInfo(Table table) throws SQLException
    {
        StringBuffer query   = new StringBuffer();
        
        query.append("SELECT a.RDB$FIELD_NAME, a.RDB$DEFAULT_SOURCE, b.RDB$FIELD_PRECISION, b.RDB$FIELD_SCALE,");
        query.append(" b.RDB$FIELD_TYPE, b.RDB$FIELD_SUB_TYPE FROM RDB$RELATION_FIELDS a, RDB$FIELDS b");
        query.append(" WHERE a.RDB$RELATION_NAME=? AND a.RDB$FIELD_SOURCE=b.RDB$FIELD_NAME");

        PreparedStatement prepStmt = getConnection().prepareStatement(query.toString());

        try
        {
            prepStmt.setString(1, getPlatform().isDelimitedIdentifierModeOn() ? table.getName() : table.getName().toUpperCase());

            ResultSet rs = prepStmt.executeQuery();

            while (rs.next())
            {
                String columnName = rs.getString(1).trim();
                Column column     = table.findColumn(columnName, getPlatform().isDelimitedIdentifierModeOn());

                if (column != null)
                {
                    String defaultValue = rs.getString(2);

                    if (!rs.wasNull() && (defaultValue != null))
                    {
                        defaultValue = defaultValue.trim();
                        if (defaultValue.startsWith("DEFAULT "))
                        {
                            defaultValue = defaultValue.substring("DEFAULT ".length());
                        }
                        column.setDefaultValue(defaultValue);
                    }
                    
                    short   precision          = rs.getShort(3);
                    boolean precisionSpecified = !rs.wasNull();
                    short   scale              = rs.getShort(4);
                    boolean scaleSpecified     = !rs.wasNull();

                    if (precisionSpecified)
                    {
                        // for some reason, Interbase stores the negative scale
                        column.setSizeAndScale(precision, scaleSpecified ? -scale : 0);
                    }

                    short dbType      = rs.getShort(5);
                    short blobSubType = rs.getShort(6);

                    // CLOBs are returned by the driver as VARCHAR
                    if (!rs.wasNull() && (dbType == 261) && (blobSubType == 1))
                    {
                        column.setTypeCode(Types.CLOB);
                    }
                }
            }
            rs.close();
        }
        finally
        {
            prepStmt.close();
        }
    }

    /**
     * Helper method that determines the auto increment status using Interbase's system tables.
     *
     * @param table The table
     */
    protected void determineAutoIncrementColumns(Table table) throws SQLException
    {
        // Since for long table and column names, the generator name will be shortened
        // we have to determine for each column whether there is a generator for it
        InterbaseBuilder builder = (InterbaseBuilder)getPlatform().getSqlBuilder();
        Column[]         columns = table.getColumns();
        HashMap          names   = new HashMap();
        String           name;

        for (int idx = 0; idx < columns.length; idx++)
        {
            name = builder.getGeneratorName(table, columns[idx]);
            if (!getPlatform().isDelimitedIdentifierModeOn())
            {
                name = name.toUpperCase();
            }
            names.put(name, columns[idx]);
        }

        Statement stmt = getConnection().createStatement();

        try
        {
            ResultSet rs = stmt.executeQuery("SELECT RDB$GENERATOR_NAME FROM RDB$GENERATORS");

            while (rs.next())
            {
                String generatorName = rs.getString(1).trim();
                Column column        = (Column)names.get(generatorName);

                if (column != null)
                {
                    column.setAutoIncrement(true);
                }
            }
            rs.close();
        }
        finally
        {
            stmt.close();
        }
    }

    /**
     * Adjusts the columns in the table by fixing types and default values.
     * 
     * @param table The table
     */
    protected void adjustColumns(Table table)
    {
        Column[] columns = table.getColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            if (columns[idx].getTypeCode() == Types.FLOAT)
            {
                columns[idx].setTypeCode(Types.REAL);
            }
            else if ((columns[idx].getTypeCode() == Types.NUMERIC) || (columns[idx].getTypeCode() == Types.DECIMAL))
            {
                if ((columns[idx].getTypeCode() == Types.NUMERIC) && (columns[idx].getSizeAsInt() == 18) && (columns[idx].getScale() == 0))
                {
                    columns[idx].setTypeCode(Types.BIGINT);
                }
            }
            else if (TypeMap.isTextType(columns[idx].getTypeCode()))
            {
                columns[idx].setDefaultValue(unescape(columns[idx].getDefaultValue(), "'", "''"));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected Collection readPrimaryKeyNames(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        List      pks   = new ArrayList();
        ResultSet pkData = null;

        try
        {
            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                // Jaybird has a problem when delimited identifiers are used as
                // it is not able to find the primary key info for the table
                // So we have to filter manually below
                pkData = metaData.getPrimaryKeys(getDefaultTablePattern());
                while (pkData.next())
                {
                    Map values = readColumns(pkData, getColumnsForPK());
    
                    if (tableName.equals(values.get("TABLE_NAME")))
                    {
                        pks.add(readPrimaryKeyName(metaData, values));
                    }
                }
            }
            else
            {
                pkData = metaData.getPrimaryKeys(tableName);
                while (pkData.next())
                {
                    Map values = readColumns(pkData, getColumnsForPK());
    
                    pks.add(readPrimaryKeyName(metaData, values));
                }
            }
        }
        finally
        {
            if (pkData != null)
            {
                pkData.close();
            }
        }
        return pks;
    }

    /**
     * {@inheritDoc}
     */
    protected Collection readForeignKeys(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        Map       fks    = new ListOrderedMap();
        ResultSet fkData = null;

        try
        {
            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                // Jaybird has a problem when delimited identifiers are used as
                // it is not able to find the foreign key info for the table
                // So we have to filter manually below
                fkData = metaData.getForeignKeys(getDefaultTablePattern());
                while (fkData.next())
                {
                    Map values = readColumns(fkData, getColumnsForFK());
    
                    if (tableName.equals(values.get("FKTABLE_NAME")))
                    {
                        readForeignKey(metaData, values, fks);
                    }
                }
            }
            else
            {
                fkData = metaData.getForeignKeys(tableName);
                while (fkData.next())
                {
                    Map values = readColumns(fkData, getColumnsForFK());
    
                    readForeignKey(metaData, values, fks);
                }
            }
        }
        finally
        {
            if (fkData != null)
            {
                fkData.close();
            }
        }
        return fks.values();
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isInternalPrimaryKeyIndex(DatabaseMetaDataWrapper metaData, Table table, Index index) throws SQLException
    {
        String       tableName = getPlatform().getSqlBuilder().getTableName(table);
        String       indexName = getPlatform().getSqlBuilder().getIndexName(index);
        StringBuffer query     = new StringBuffer();

        query.append("SELECT RDB$CONSTRAINT_NAME FROM RDB$RELATION_CONSTRAINTS where RDB$RELATION_NAME=? AND RDB$CONSTRAINT_TYPE=? AND RDB$INDEX_NAME=?");

        PreparedStatement stmt = getConnection().prepareStatement(query.toString());

        try 
        {
            stmt.setString(1, getPlatform().isDelimitedIdentifierModeOn() ? tableName : tableName.toUpperCase());
            stmt.setString(2, "PRIMARY KEY");
            stmt.setString(3, indexName);

            ResultSet resultSet = stmt.executeQuery();

            return resultSet.next();
        }
        finally
        {
            if (stmt != null)
            {
                stmt.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isInternalForeignKeyIndex(DatabaseMetaDataWrapper metaData, Table table, ForeignKey fk, Index index) throws SQLException
    {
        String       tableName = getPlatform().getSqlBuilder().getTableName(table);
        String       indexName = getPlatform().getSqlBuilder().getIndexName(index);
        String       fkName    = getPlatform().getSqlBuilder().getForeignKeyName(table, fk);
        StringBuffer query     = new StringBuffer();

        query.append("SELECT RDB$CONSTRAINT_NAME FROM RDB$RELATION_CONSTRAINTS where RDB$RELATION_NAME=? AND RDB$CONSTRAINT_TYPE=? AND RDB$CONSTRAINT_NAME=? AND RDB$INDEX_NAME=?");

        PreparedStatement stmt = getConnection().prepareStatement(query.toString());

        try 
        {
            stmt.setString(1, getPlatform().isDelimitedIdentifierModeOn() ? tableName : tableName.toUpperCase());
            stmt.setString(2, "FOREIGN KEY");
            stmt.setString(3, fkName);
            stmt.setString(4, indexName);

            ResultSet resultSet = stmt.executeQuery();

            return resultSet.next();
        }
        finally
        {
            if (stmt != null)
            {
                stmt.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String determineSchemaOf(Connection connection, String schemaPattern, Table table) throws SQLException
    {
        ResultSet tableData  = null;
        ResultSet columnData = null;

        try
        {
            DatabaseMetaDataWrapper metaData = new DatabaseMetaDataWrapper();

            metaData.setMetaData(connection.getMetaData());
            metaData.setCatalog(getDefaultCatalogPattern());
            metaData.setSchemaPattern(schemaPattern == null ? getDefaultSchemaPattern() : schemaPattern);
            metaData.setTableTypes(getDefaultTableTypes());

            String tablePattern = table.getName();

            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                tablePattern = tablePattern.toUpperCase();
            }

            tableData = metaData.getTables(tablePattern);

            boolean found  = false;
            String  schema = null;

            while (!found && tableData.next())
            {
                Map    values    = readColumns(tableData, getColumnsForTable());
                String tableName = (String)values.get("TABLE_NAME");

                if ((tableName != null) && (tableName.length() > 0))
                {
                    schema = (String)values.get("TABLE_SCHEM");
                    found  = true;

                    if (getPlatform().isDelimitedIdentifierModeOn())
                    {
                        // Jaybird has a problem when delimited identifiers are used as
                        // it is not able to find the columns for the table
                        // So we have to filter manually below
                        columnData = metaData.getColumns(getDefaultTablePattern(), getDefaultColumnPattern());
                    }
                    else
                    {
                        columnData = metaData.getColumns(tableName, getDefaultColumnPattern());
                    }

                    while (found && columnData.next())
                    {
                        values = readColumns(columnData, getColumnsForColumn());

                        if (getPlatform().isDelimitedIdentifierModeOn() &&
                            !tableName.equals(values.get("TABLE_NAME")))
                        {
                            continue;
                        }

                        if (table.findColumn((String)values.get("COLUMN_NAME"),
                                             getPlatform().isDelimitedIdentifierModeOn()) == null)
                        {
                            found = false;
                        }
                    }
                    columnData.close();
                    columnData = null;
                }
            }
            return found ? schema : null;
        }
        finally
        {
            if (columnData != null)
            {
                columnData.close();
            }
            if (tableData != null)
            {
                tableData.close();
            }
        }
    }
}
