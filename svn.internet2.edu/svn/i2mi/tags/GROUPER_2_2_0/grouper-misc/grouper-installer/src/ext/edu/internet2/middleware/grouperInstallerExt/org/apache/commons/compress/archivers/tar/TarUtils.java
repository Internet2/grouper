/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.compress.archivers.tar;

/**
 * This class provides static utility methods to work with byte streams.
 *
 * @Immutable
 */
// CheckStyle:HideUtilityClassConstructorCheck OFF (bc)
public class TarUtils {

    private static final int BYTE_MASK = 255;

    /** Private constructor to prevent instantiation of this utility class. */
    private TarUtils(){    
    }

    /**
     * Parse an octal string from a buffer.
     * Leading spaces are ignored.
     * The buffer must contain a trailing space or NUL,
     * and may contain an additional trailing space or NUL.
     *
     * The input buffer is allowed to contain all NULs,
     * in which case the method returns 0L
     * (this allows for missing fields).
     *
     * @param buffer The buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The maximum number of bytes to parse - must be at least 2 bytes.
     * @return The long value of the octal string.
     * @throws IllegalArgumentException if the trailing space/NUL is missing or if a invalid byte is detected.
     */
    public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long    result = 0;
        int     end = offset + length;
        int     start = offset;

        if (length < 2){
            throw new IllegalArgumentException("Length "+length+" must be at least 2");
        }

        boolean allNUL = true;
        for (int i = start; i < end; i++){
            if (buffer[i] != 0){
                allNUL = false;
                break;
            }
        }
        if (allNUL) {
            return 0L;
        }

        // Skip leading spaces
        while (start < end){
            if (buffer[start] == ' '){
                start++;
            } else {
                break;
            }
        }

        // Must have trailing NUL or space
        byte trailer;
        trailer = buffer[end-1];
        if (trailer == 0 || trailer == ' '){
            end--;
        } else {
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, end-1, trailer));
        }
        // May have additional NUL or space
        trailer = buffer[end-1];
        if (trailer == 0 || trailer == ' '){
            end--;
        }

        for ( ;start < end; start++) {
            final byte currentByte = buffer[start];
            // CheckStyle:MagicNumber OFF
            if (currentByte < '0' || currentByte > '7'){
                throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, start, currentByte));
            }
            result = (result << 3) + (currentByte - '0'); // convert from ASCII
            // CheckStyle:MagicNumber ON
        }

        return result;
    }

    /**
     * Parse a boolean byte from a buffer.
     * Leading spaces and NUL are ignored.
     * The buffer may contain trailing spaces or NULs.
     *
     * @param buffer The buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @return The boolean value of the bytes.
     * @throws IllegalArgumentException if an invalid byte is detected.
     */
    public static boolean parseBoolean(final byte[] buffer, final int offset) {
        return buffer[offset] == 1;
    }

    // Helper method to generate the exception message
    private static String exceptionMessage(byte[] buffer, final int offset,
            final int length, int current, final byte currentByte) {
        String string = new String(buffer, offset, length);
        string=string.replaceAll("\0", "{NUL}"); // Replace NULs to allow string to be printed
        final String s = "Invalid byte "+currentByte+" at offset "+(current-offset)+" in '"+string+"' len="+length;
        return s;
    }

    /**
     * Parse an entry name from a buffer.
     * Parsing stops when a NUL is found
     * or the buffer length is reached.
     *
     * @param buffer The buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The maximum number of bytes to parse.
     * @return The entry name.
     */
    public static String parseName(byte[] buffer, final int offset, final int length) {
        StringBuffer result = new StringBuffer(length);
        int          end = offset + length;

        for (int i = offset; i < end; ++i) {
            byte b = buffer[i];
            if (b == 0) { // Trailing null
                break;
            }
            result.append((char) (b & 0xFF)); // Allow for sign-extension
        }

        return result.toString();
    }

    /**
     * Copy a name (StringBuffer) into a buffer.
     * Copies characters from the name into the buffer
     * starting at the specified offset. 
     * If the buffer is longer than the name, the buffer
     * is filled with trailing NULs.
     * If the name is longer than the buffer,
     * the output is truncated.
     *
     * @param name The header name from which to copy the characters.
     * @param buf The buffer where the name is to be stored.
     * @param offset The starting offset into the buffer
     * @param length The maximum number of header bytes to copy.
     * @return The updated offset, i.e. offset + length
     */
    public static int formatNameBytes(String name, byte[] buf, final int offset, final int length) {
        int i;

        // copy until end of input or output is reached.
        for (i = 0; i < length && i < name.length(); ++i) {
            buf[offset + i] = (byte) name.charAt(i);
        }

        // Pad any remaining output bytes with NUL
        for (; i < length; ++i) {
            buf[offset + i] = 0;
        }

        return offset + length;
    }

    /**
     * Fill buffer with unsigned octal number, padded with leading zeroes.
     * 
     * @param value number to convert to octal - treated as unsigned
     * @param buffer destination buffer
     * @param offset starting offset in buffer
     * @param length length of buffer to fill
     * @throws IllegalArgumentException if the value will not fit in the buffer
     */
    public static void formatUnsignedOctalString(final long value, byte[] buffer,
            final int offset, final int length) {
        int remaining = length;
        remaining--;
        if (value == 0) {
            buffer[offset + remaining--] = (byte) '0';
        } else {
            long val = value;
            for (; remaining >= 0 && val != 0; --remaining) {
                // CheckStyle:MagicNumber OFF
                buffer[offset + remaining] = (byte) ((byte) '0' + (byte) (val & 7));
                val = val >>> 3;
                // CheckStyle:MagicNumber ON
            }
            if (val != 0){
                throw new IllegalArgumentException
                (value+"="+Long.toOctalString(value)+ " will not fit in octal number buffer of length "+length);
            }
        }

        for (; remaining >= 0; --remaining) { // leading zeros
            buffer[offset + remaining] = (byte) '0';
        }
    }

    /**
     * Write an octal integer into a buffer.
     *
     * Uses {@link #formatUnsignedOctalString} to format
     * the value as an octal string with leading zeros.
     * The converted number is followed by space and NUL
     * 
     * @param value The value to write
     * @param buf The buffer to receive the output
     * @param offset The starting offset into the buffer
     * @param length The size of the output buffer
     * @return The updated offset, i.e offset+length
     * @throws IllegalArgumentException if the value (and trailer) will not fit in the buffer
     */
    public static int formatOctalBytes(final long value, byte[] buf, final int offset, final int length) {

        int idx=length-2; // For space and trailing null
        formatUnsignedOctalString(value, buf, offset, idx);

        buf[offset + idx++] = (byte) ' '; // Trailing space
        buf[offset + idx]   = 0; // Trailing null

        return offset + length;
    }

    /**
     * Write an octal long integer into a buffer.
     * 
     * Uses {@link #formatUnsignedOctalString} to format
     * the value as an octal string with leading zeros.
     * The converted number is followed by a space.
     * 
     * @param value The value to write as octal
     * @param buf The destinationbuffer.
     * @param offset The starting offset into the buffer.
     * @param length The length of the buffer
     * @return The updated offset
     * @throws IllegalArgumentException if the value (and trailer) will not fit in the buffer
     */
    public static int formatLongOctalBytes(final long value, byte[] buf, final int offset, final int length) {

        int idx=length-1; // For space
        
        formatUnsignedOctalString(value, buf, offset, idx);
        buf[offset + idx] = (byte) ' '; // Trailing space

        return offset + length;
    }

    /**
     * Writes an octal value into a buffer.
     * 
     * Uses {@link #formatUnsignedOctalString} to format
     * the value as an octal string with leading zeros.
     * The converted number is followed by NUL and then space.
     *
     * @param value The value to convert
     * @param buf The destination buffer
     * @param offset The starting offset into the buffer.
     * @param length The size of the buffer.
     * @return The updated value of offset, i.e. offset+length
     * @throws IllegalArgumentException if the value (and trailer) will not fit in the buffer
     */
    public static int formatCheckSumOctalBytes(final long value, byte[] buf, final int offset, final int length) {

        int idx=length-2; // for NUL and space
        formatUnsignedOctalString(value, buf, offset, idx);

        buf[offset + idx++]   = 0; // Trailing null
        buf[offset + idx]     = (byte) ' '; // Trailing space

        return offset + length;
    }

    /**
     * Compute the checksum of a tar entry header.
     *
     * @param buf The tar entry's header buffer.
     * @return The computed checksum.
     */
    public static long computeCheckSum(final byte[] buf) {
        long sum = 0;

        for (int i = 0; i < buf.length; ++i) {
            sum += BYTE_MASK & buf[i];
        }

        return sum;
    }
}
