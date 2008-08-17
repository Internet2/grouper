/*
 * $Id: RijnMaker.java,v 1.1 2008-08-17 15:33:02 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
package edu.internet2.middleware.grouper.util.rijndael;

/**
 * This class and package is not well documented. It was written by a third
 * party. It encrypts and decrypted text based on text and a key using the
 * rijndael argoithm. See class peopleFinderPackage.password which is the only
 * class that uses this package, and it is a facade for rijndael.
 * 
 * @author unknown
 * @version ???
 */
class RijnMaker {

  /**
   * Field keyArray.
   */
  int[] keyArray;

  /**
   * Field stateSize.
   */
  private int stateSize; //size of text

  /**
   * Field keySize.
   */
  private int keySize;

  /**
   * Field r.
   */
  StartRijndael r;

  //constructor,sets up state and key size
  /**
   * Constructor for RijnMaker.
   * @param sSize int
   * @param kSize int
   */
  public RijnMaker(int sSize, int kSize) {
    this.stateSize = sSize;
    this.keySize = kSize;
    this.keyArray = new int[this.keySize];
  }

  //takes in an array of ints and returns a String of chars (ASCII)
  /**
   * Method intArrayToString.
   * @param t int[]
   * @return String
   */
  public String intArrayToString(int[] t) {
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < t.length; i++) {
      sb.append((char) t[i]);
    }

    return sb.toString();
  }

  //converts a string of chars to an arrayof int values
  /**
   * Method stringToIntArray.
   * @param s String
   * @return int[]
   */
  public int[] stringToIntArray(String s) {
    int[] temp = new int[s.length()];

    for (int i = 0; i < s.length(); i++) {
      temp[i] = s.charAt(i);
    }

    return temp;
  }

  //takes in a string of hex (2 digit) values and returns an int array
  /**
   * Method hexStringToIntArray.
   * @param s String
   * @return int[]
   */
  public int[] hexStringToIntArray(String s) {
    int[] temp = new int[s.length() / 2];

    for (int i = 0; i < s.length(); i = i + 2) {
      temp[i / 2] = Integer.valueOf(s.substring(i, i + 2), 16).intValue();
    }

    return temp;
  }

  /**
   * @param args
   *          $objectType$
   */
  public static void main(String[] args) {
    RijnMaker r = new RijnMaker(32, 32);
    r.encrypt("a", "a");

    new RijnMaker(32, 32);
  }

  //takes in 2 strings of any size representing the text and the key
  //returns a string representing the ciphertext in hex values(2 digits)
  /**
   * Method encrypt.
   * @param t String
   * @param k String
   * @return String
   */
  public String encrypt(String t, String k) {
    String text = cleanText(t);
    String cipher = "";
    int[] temp = new int[this.stateSize];
    setKeyLength(k);

    //for each chunk of text size sSize, we perform encryption on it and store
    // the result
    for (int i = 0; i < text.length(); i = i + this.stateSize) {
      temp = stringToIntArray(text.substring(i, i + this.stateSize));

      this.r = new StartRijndael(temp, this.keyArray);
      this.r.encrypt();

      cipher = cipher + (this.r.stateToHex());
    }

    return cipher;
  }

  //takes in 2 strings, one representing the ciphertext as a string of hex
  // values
  //the other, the key as a normal string.
  //returns a string containing the plain text. works with any size of key and
  // text
  /**
   * Method decrypt.
   * @param t String
   * @param k String
   * @return String
   */
  public String decrypt(String t, String k) {
    String text = t;
    String plain = "";
    setKeyLength(k);

    int[] temp; // new int[stateSize];

    //for each chunk of text size sSize, we perform decryption on it and store
    // the result
    for (int i = 0; i < text.length(); i = i + (this.stateSize * 2)) {
      temp = hexStringToIntArray(text.substring(i, i + (this.stateSize * 2)));

      //Printed out by Chris Hyzer
      this.r = new StartRijndael(temp, this.keyArray);
      this.r.decrypt();

      plain = plain + (intArrayToString(this.r.toInt()));
    }

    return plain;
  }

  //sets the keyArray to the correct size and fills it with the correct ints
  /**
   * Method setKeyLength.
   * @param k String
   */
  public void setKeyLength(String k) {
    String key = k;

    if (key.length() > this.keySize) {
      key = key.substring(0, this.keySize);
    }

    while (key.length() < this.keySize) {
      key = key + " ";
    }

    for (int i = 0; i < key.length(); i++) {
      this.keyArray[i] = key.charAt(i);
    }
  }

  //sets the text to the correct length by adding on spaces if required
  /**
   * Method cleanText.
   * @param t String
   * @return String
   */
  public String cleanText(String t) {
    String text = t;

    while (text.length() < this.stateSize) {
      text = text + " ";
    }

    while ((text.length() % this.stateSize) != 0) {
      text = text + " ";
    }

    return text;
  }
}