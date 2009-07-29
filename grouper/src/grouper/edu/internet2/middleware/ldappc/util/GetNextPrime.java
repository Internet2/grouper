/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package edu.internet2.middleware.ldappc.util;

/**
 * <p>
 * Utility class to print the smallest prime number equal or greater than the
 * first argument on the command line.
 * </p>
 * <p>
 * This can be used to generate hash estimates for the parameters in the
 * ldappc.xml file.
 * </p>
 */
public class GetNextPrime
{
    /**
     * Main method. Pass an integer and it will print the smallest prime number
     * equal or greater than that integer.
     * 
     * @param args
     *            the integer
     */
    public static void main(String[] args)
    {
        long input = 0;
        long root = 0;
        boolean isPrime = false;

        try
        {
            input = Long.parseLong(args[0]);
        }
        catch (NumberFormatException nfx)
        {
            System.out.println("Invalid integer: '" + args[0] + "'");
            System.exit(1);
        }
        if (input <= 2)
        {
            System.out.println(2);
            System.exit(0);
        }
        for (int k = 3; k < 9; k += 2)
        {
            if (input <= k)
            {
                System.out.println(k);
                System.exit(0);
            }
        }
        if (input == ((input >> 1) << 1))
        {
            input += 1;
        }
        for (long i = input;; i += 2)
        {
            root = (long) Math.sqrt(i);
            for (long j = 3; j <= root; j++)
            {
                if (i == (long) (i / j) * j)
                {
                    isPrime = false;
                    break;
                }
                if (j == root)
                {
                    isPrime = true;
                }
            }
            if (isPrime == true)
            {
                System.out.println(i);
                break;
            }
        }
    }
}
