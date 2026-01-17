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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the different categories of jdbc types.
 *
 * @version $Revision: $
 */
public enum JdbcTypeCategoryEnum
{
    /** The enum value for numeric jdbc types. */
    NUMERIC("numeric", 1),
    /** The enum value for date/time jdbc types. */
    DATETIME("datetime", 2),
    /** The enum value for textual jdbc types. */
    TEXTUAL("textual", 3),
    /** The enum value for binary jdbc types. */
    BINARY("binary", 4),
    /** The enum value for special jdbc types. */
    SPECIAL("special", 5),
    /** The enum value for other jdbc types. */
    OTHER("other", 6);

    /** The textual representation */
    private final String name;
    /** The corresponding integer value */
    private final int value;

    /** Static map for lookup by value */
    private static final Map<Integer, JdbcTypeCategoryEnum> valueMap = new HashMap<>();
    /** Static map for lookup by name */
    private static final Map<String, JdbcTypeCategoryEnum> nameMap = new HashMap<>();

    static {
        for (JdbcTypeCategoryEnum e : values()) {
            valueMap.put(e.value, e);
            nameMap.put(e.name, e);
        }
    }

    /**
     * Creates a new enum object.
     *
     * @param name  The textual representation
     * @param value The corresponding integer value
     */
    private JdbcTypeCategoryEnum(String name, int value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the integer value.
     *
     * @return The integer value
     */
    public int getValue()
    {
        return value;
    }

    /**
     * Returns the textual representation.
     *
     * @return The textual representation
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the enum value that corresponds to the given textual
     * representation.
     *
     * @param defaultTextRep The textual representation
     * @return The enum value
     */
    public static JdbcTypeCategoryEnum getEnum(String defaultTextRep)
    {
        return nameMap.get(defaultTextRep);
    }

    /**
     * Returns the enum value that corresponds to the given integer
     * representation.
     *
     * @param intValue The integer value
     * @return The enum value
     */
    public static JdbcTypeCategoryEnum getEnum(int intValue)
    {
        return valueMap.get(intValue);
    }

    /**
     * Returns the map of enum values.
     *
     * @return The map of enum values
     */
    public static Map<String, JdbcTypeCategoryEnum> getEnumMap()
    {
        return new HashMap<>(nameMap);
    }

    /**
     * Returns a list of all enum values.
     *
     * @return The list of enum values
     */
    public static List<JdbcTypeCategoryEnum> getEnumList()
    {
        return Arrays.asList(values());
    }

    @Override
    public String toString()
    {
        return name;
    }
}
