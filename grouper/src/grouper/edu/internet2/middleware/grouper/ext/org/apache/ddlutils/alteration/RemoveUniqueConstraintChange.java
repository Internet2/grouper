package edu.internet2.middleware.grouper.ext.org.apache.ddlutils.alteration;

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

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.UniqueConstraint;

/**
 * Represents the removal of a unique constraint from a table.
 */
public class RemoveUniqueConstraintChange extends TableChangeImplBase
{
    /** The unique constraint to be removed. */
    private UniqueConstraint _uniqueConstraint;

    /**
     * Creates a new change object.
     *
     * @param table            The table to remove the unique constraint from
     * @param uniqueConstraint The unique constraint
     */
    public RemoveUniqueConstraintChange(Table table, UniqueConstraint uniqueConstraint)
    {
        super(table);
        _uniqueConstraint = uniqueConstraint;
    }

    /**
     * Returns the unique constraint.
     *
     * @return The unique constraint
     */
    public UniqueConstraint getUniqueConstraint()
    {
        return _uniqueConstraint;
    }

    /**
     * {@inheritDoc}
     */
    public void apply(Database database, boolean caseSensitive)
    {
        Table table = database.findTable(getChangedTable().getName(), caseSensitive);
        UniqueConstraint uniqueConstraint = table.findUniqueConstraint(_uniqueConstraint.getName(), caseSensitive);

        table.removeUniqueConstraint(uniqueConstraint);
    }
}
