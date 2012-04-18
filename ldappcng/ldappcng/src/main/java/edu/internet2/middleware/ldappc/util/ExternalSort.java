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
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This package implement an external merge sort based on Collections sorting.
 * 
 * An input file is divided into batches, which are sorted separately using the
 * Collections.sort method. These batches are then merged into a single sorted file.
 * 
 * The resulting file overwrites the initial file. The intermediate batch files are
 * deleted.
 */
public final class ExternalSort {

  /**
   * Default size in lines for a batch to be sorted in memory.
   */
  private static final int DEFAULT_BATCH_SIZE = 200000;

  /**
   * Text to be appended to the original filename, followed by an integer, indicating the
   * batch.
   */
  private static final String BATCH_EXTENSION = "_batch";

  /**
   * Prevent instantiation.
   */
  private ExternalSort() {
  }

  /**
   * Main method to use for external testing of this routine. For some systems, the batch
   * size may need to be adjusted.
   * 
   * @param args
   *          Command line args: filename - the file to be sorted; batchsize - the number
   *          of lines to be sorted in memory.
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      System.err
          .println("Usage: java edu.internet2.middleware.ldappc.util.Sort filename batchsize");
      System.exit(1);
    }

    String filename = args[0];
    int batchSize = Integer.parseInt(args[1]);

    try {
      sort(filename, batchSize);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Sort a file using the default batch size.
   * 
   * @param filename
   *          The file to be sorted.
   * 
   * @throws IOException
   *           Thrown if a file cannot be read or written.
   */
  public static void sort(String filename) throws IOException {
    sort(filename, DEFAULT_BATCH_SIZE);
  }

  /**
   * Sort a file using the batch size provided.
   * 
   * @param filename
   *          the file to be sorted.
   * @param batchSize
   *          the batch size, that is, the number of lines to batch and sort in memory.
   * @throws IOException
   *           Thrown if a file cannot be read or written.
   */
  public static void sort(String filename, int batchSize) throws IOException {
    FileReader intialFileInput = new FileReader(filename);
    BufferedReader initFileReader = new BufferedReader(intialFileInput);
    ArrayList<String> batch = new ArrayList<String>();

    int numFiles = 0;
    boolean atEOF = false;
    while (!atEOF) {
      //
      // Get a batch of lines.
      //
      int numLines = 0;
      for (; numLines < batchSize; numLines++) {
        String line = initFileReader.readLine();
        if (line != null) {
          batch.add(line);
        } else {
          atEOF = true;
          break;
        }
      }

      //
      // If any lines were read into this batch, sort them and write
      // them to a batch file. Close the file and clear the batch.
      //
      if (numLines > 0) {
        Collections.sort(batch);

        FileWriter fw = new FileWriter(filename + BATCH_EXTENSION + numFiles);
        BufferedWriter bw = new BufferedWriter(fw);
        for (int i = 0; i < batch.size(); i++) {
          bw.append(batch.get(i) + "\n");
        }
        bw.close();

        numFiles++;
        batch.clear();
      }
    }

    //
    // Close the original file.
    //
    initFileReader.close();
    intialFileInput.close();

    //
    // If any batch files were written, merge the batches, overwriting
    // the original file. Delete the batch files.
    //
    if (numFiles > 0) {
      mergeFiles(filename, numFiles);
    }
  }

  /**
   * Merge batch files together back into the original file. Delete the batch files when
   * finished.
   * 
   * @param filename
   *          the file to be written
   * @param numFiles
   *          the number of batch files to merge
   * 
   * @throws IOException
   *           Thrown if a file cannot be created or written to.
   */
  private static void mergeFiles(String filename, int numFiles) throws IOException {
    //
    // Create arrays of various batch file classes, and also of current
    // lines to be merged.
    //
    File[] mergeFiles = new File[numFiles];
    FileReader[] mergeFileReaders = new FileReader[numFiles];
    BufferedReader[] mergeBufferedReaders = new BufferedReader[numFiles];
    String[] currentLines = new String[numFiles];

    //
    // Create the buffered writer for the output file.
    //
    FileWriter fw = new FileWriter(filename);
    BufferedWriter bw = new BufferedWriter(fw);

    //
    // Initialize the readers and current lines for each batch.
    //
    for (int i = 0; i < numFiles; i++) {
      mergeFiles[i] = new File(filename + BATCH_EXTENSION + i);
      mergeFileReaders[i] = new FileReader(mergeFiles[i]);
      mergeBufferedReaders[i] = new BufferedReader(mergeFileReaders[i]);

      currentLines[i] = mergeBufferedReaders[i].readLine();
    }

    //
    // Merge the files together using standard file merge algorithm.
    //
    boolean filesExhausted = false;
    while (!filesExhausted) {
      //
      // Find index of batch file whose current line is the minimum.
      //
      int minIndex = findMinimumLine(currentLines);

      if (minIndex < 0) {
        //
        // If there is no minimum line then all files have been
        // exhausted. Our work here is done.
        //
        filesExhausted = true;
      } else {
        //
        // Write minimum line to the sorted file and get another
        // line from the file that had the minimum.
        //
        bw.append(currentLines[minIndex] + "\n");
        currentLines[minIndex] = mergeBufferedReaders[minIndex].readLine();
      }
    }

    //
    // Close the sorted file.
    //
    bw.close();
    fw.close();

    //
    // Close and delete the batch files.
    //
    for (int i = 0; i < numFiles; i++) {
      mergeBufferedReaders[i].close();
      mergeFileReaders[i].close();
      mergeFiles[i].delete();
    }
  }

  /**
   * Given an array of current lines from the batch files, find the batch index of the
   * minimum line, ignoring files that have been exhausted.
   * 
   * Return -1 if all files have been exhausted.
   * 
   * @param currentLines
   *          the array of current lines from the batch files.
   * @return the index of the batch file with the minimum line, or -1 if all files have
   *         been exhausted.
   */
  private static int findMinimumLine(String[] currentLines) {
    int minIndex = -1;
    String minimumLineText = null;

    for (int i = 0; i < currentLines.length; i++) {
      String currentLine = currentLines[i];
      if (currentLine != null) {
        if (minimumLineText == null || currentLine.compareTo(minimumLineText) < 0) {
          minimumLineText = currentLine;
          minIndex = i;
        }
      }
    }

    return minIndex;
  }
}
