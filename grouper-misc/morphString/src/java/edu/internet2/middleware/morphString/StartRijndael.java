/*
 * $Id: StartRijndael.java,v 1.1 2008-09-13 18:51:48 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
package edu.internet2.middleware.morphString;

/**
 */
class StartRijndael {

  /**
   * Field S.
   */
  private int[] S; //the S-Box

  /**
   * Field Sinv.
   */
  private int[] Sinv; //the inverse S-Box

  /**
   * Field log.
   */
  private final int[] log = new int[256]; //these tables are used to allow us

  // to multiply

  /**
   * Field alog.
   */
  private final int[] alog = new int[256]; //in the GF(2^8) field

  /**
   * Field ROOT.
   */
  private final int ROOT = 0x11B; //constant XORed with Byte when 2 bytes being

  // multiplied exceed 8 bits

  /**
   * Field key.
   */
  private RijnByte[] key; //holds the key used for encryption and decryption

  /**
   * Field state.
   */
  private RijnByte[] rinjState; //holds the initial plaintext and it's status at

  // every stage of the encryption

  //ends up holding the ciphertext when encryption is complete.
  /**
   * Field rcon.
   */
  private RijnByte[] rcon; //holds all 30 possible round constants as used in

  // the Key Expansion

  /**
   * Field NB.
   */
  private int rinjNBInt; // block length of state divided by 32 (no. of columns of

  // state)

  /**
   * Field NK.
   */
  private int NK; // block length of key divided by 32 (no. of columns of key)

  /**
   * Field NR.
   */
  private int NR; // no. of rounds to carry out. based on function of NB & NK

  /**
   * Field w.
   */
  private RijnWord[] w; //used to hold the fully expanded key

  //returns the state converted to characters and returns these in a string
  /**
   * Method stateToString.
   * @return String
   */
  public String stateToString() {
    byte[] b = new byte[this.rinjState.length];

    for (int i = 0; i < this.rinjState.length; i++) {
      b[i] = (byte) this.rinjState[i].getVal();
    }

    return new String(b);
  }

  //returns a string representation of the state in Hex values
  //each int is returned as a 2 digit hex number
  /**
   * Method stateToHex.
   * @return String
   */
  public String stateToHex() {
    String temp = "";

    for (int i = 0; i < this.rinjState.length; i++) {
      if (this.rinjState[i].getVal() < 16) {
        temp = temp + "0"; //0 appended if int value would only give us a 1
        // digit hex number
      }

      temp = temp + Integer.toString(this.rinjState[i].getVal(), 16);
    }

    return temp;
  }

  //takes in a string containing hex numbers in 2digits
  //and returns an array of ints
  /**
   * Method hexToInt.
   * @param s String
   * @return int[]
   */
  public static int[] hexToInt(String s) {
    int[] temp = new int[s.length() / 2];

    for (int i = 0; i < s.length(); i = i + 2) {
      temp[i / 2] = Integer.valueOf(s.substring(i, i + 2), 16).intValue();
    }

    return temp;
  }

  //a constructor that takes in an array of ints for message and key
  //arrays must be either of length 16,24 or 32
  /**
   * Constructor for StartRijndael.
   * @param m int[]
   * @param k int[]
   */
  public StartRijndael(int[] m, int[] k) {

    this.rinjNBInt = m.length / 4;
    this.NK = k.length / 4;

    if ((this.rinjNBInt == 4) && (this.NK == 4)) { //work out the correct number of rounds
      this.NR = 10; //based on the value of NB & NR;
    } else if (((this.NK == 6) && (this.rinjNBInt != 14)) || ((this.rinjNBInt == 6) && (this.NK != 8))) {
      this.NR = 12;
    } else if ((this.NK == 8) || (this.rinjNBInt == 8)) {
      this.NR = 14;
    } else {
      throw new RuntimeException("Incorrect value for NK or NB");
    }

    this.key = new RijnByte[4 * this.NK]; //instantiate the key and state arrays to the
    // correct size
    this.rinjState = new RijnByte[4 * this.rinjNBInt];

    //byte messArray[] = message.getBytes(); //create an array of bytes
    // representing the message(ASCII vals)
    for (int i = 0; i < m.length; i++) { //fill the state with RijnByte objects
      // representing the text
      this.rinjState[i] = new RijnByte(m[i]);

    }

    //byte keyArray[] = theKey.getBytes(); //create an array of bytes
    // representing the key(ASCII vals)
    for (int i = 0; i < k.length; i++) { //fill the key with RijnByte objects
      // representing the key
      this.key[i] = new RijnByte(k[i]);
    }

    //create tables needed for calculations
    createSBox(); //used by ByteSub
    createSinvBox(); //used by ByteSub Inverse
    createRcon(); //round constants as used by key expansion
    createLogs(); //logs used for multiplying in GF(2^8)
    keyExpansion(); //same key expansion is used for encrypt or decrypt
  }

  //constructor which takes in a message or ciphertext and key(must be exact
  // sizes(16,24 or 32 characters)
  //and sets up all tables,variables ready for encryption or decryption
  /**
   * Constructor for StartRijndael.
   * @param message String
   * @param theKey String
   */
  public StartRijndael(String message, String theKey) {

    if (!((message.length() == 16) || (message.length() == 24) || (message.length() == 32))) {
      throw new RuntimeException("Incorrect message size");
    }

    if (!((theKey.length() == 16) || (theKey.length() == 24) || (theKey.length() == 32))) {
      throw new RuntimeException("Incorrect key size");
    }

    this.rinjNBInt = message.length() / 4;
    this.NK = theKey.length() / 4;

    if ((this.rinjNBInt == 4) && (this.NK == 4)) { //work out the correct number of rounds
      this.NR = 10; //based on the value of NB & NR;
    } else if (((this.NK == 6) && (this.rinjNBInt != 14)) || ((this.rinjNBInt == 6) && (this.NK != 8))) {
      this.NR = 12;
    } else if ((this.NK == 8) || (this.rinjNBInt == 8)) {
      this.NR = 14;
    } else {
      throw new RuntimeException("Incorrect value for NK or NB");
    }

    this.key = new RijnByte[4 * this.NK]; //instantiate the key and state arrays to the
    // correct size
    this.rinjState = new RijnByte[4 * this.rinjNBInt];

    byte[] messArray = message.getBytes(); //create an array of bytes
    // representing the message(ASCII
    // vals)

    for (int i = 0; i < messArray.length; i++) { //fill the state with RijnByte
      // objects representing the
      // text
      this.rinjState[i] = new RijnByte(messArray[i]);
    }

    byte[] keyArray = theKey.getBytes(); //create an array of bytes
    // representing the key(ASCII vals)

    for (int i = 0; i < keyArray.length; i++) { //fill the key with RijnByte
      // objects representing the key
      this.key[i] = new RijnByte(keyArray[i]);
    }

    //create tables needed for calculations
    createSBox(); //used by ByteSub
    createSinvBox(); //used by ByteSub Inverse
    createRcon(); //round constants as used by key expansion
    createLogs(); //logs used for multiplying in GF(2^8)
    keyExpansion(); //same key expansion is used for encrypt or decrypt
  }

  /**
   *  
   */
  public void encrypt() {
    AddRoundKey(0);
    doRounds();
    doFinalRound();
  }

  /**
   *  
   */
  public void decrypt() {
    doFinalRoundInv();
    doRoundsInv();
    AddRoundKey(0);
  }

  /**
   * Method doRounds.
   */
  private void doRounds() {
    for (int i = 1; i < this.NR; i++) {
      ByteSub();

      ShiftRow();

      MixColumn();

      AddRoundKey(i);

    }
  }

  /**
   * Method doRoundsInv.
   */
  private void doRoundsInv() {
    for (int i = this.NR - 1; i > 0; i--) {
      AddRoundKey(i);
      MixColumnInv();
      ShiftRowInv();
      ByteSubInv();
    }
  }

  /**
   * Method doFinalRound.
   */
  private void doFinalRound() {
    ByteSub();
    ShiftRow();
    AddRoundKey(this.NR);
  }

  /**
   * Method doFinalRoundInv.
   */
  private void doFinalRoundInv() {
    AddRoundKey(this.NR);
    ShiftRowInv();
    ByteSubInv();
  }

  //performs byte substitution on all bytes of the state
  /**
   * Method ByteSub.
   */
  private void ByteSub() {
    for (int i = 0; i < (this.rinjNBInt * 4); i++) {
      this.rinjState[i] = doSBox(this.rinjState[i]);
    }
  }

  //performs Shift Row
  /**
   * Method ShiftRow.
   */
  private void ShiftRow() {
    //the shift offset is determined by the size of the state
    if ((this.rinjNBInt == 4) || (this.rinjNBInt == 6)) {
      doShift(1, 1);
      doShift(2, 2);
      doShift(3, 3);
    } else if (this.rinjNBInt == 8) {
      doShift(1, 1);
      doShift(2, 3);
      doShift(3, 4);
    }
  }

  /**
   * Method MixColumn.
   */
  private void MixColumn() {
    RijnByte[] col2 = new RijnByte[4]; //holds newly multiplied column

    for (int i = 0; i < this.rinjNBInt; i++) { //for each column of the state
      col2[0] = mul(this.rinjState[i * 4].getVal(), 2).XOR(
          mul(this.rinjState[(i * 4) + 1].getVal(), 3).XOR(
              this.rinjState[(i * 4) + 2].XOR(this.rinjState[(i * 4) + 3])));
      col2[1] = this.rinjState[i * 4].XOR(mul(this.rinjState[(i * 4) + 1].getVal(), 2).XOR(
          mul(this.rinjState[(i * 4) + 2].getVal(), 3).XOR(this.rinjState[(i * 4) + 3])));
      col2[2] = this.rinjState[i * 4].XOR(this.rinjState[(i * 4) + 1].XOR(mul(this.rinjState[(i * 4) + 2].getVal(),
          2).XOR(mul(this.rinjState[(i * 4) + 3].getVal(), 3))));
      col2[3] = mul(this.rinjState[i * 4].getVal(), 3).XOR(
          this.rinjState[(i * 4) + 1].XOR(this.rinjState[(i * 4) + 2].XOR(mul(this.rinjState[(i * 4) + 3].getVal(),
              2))));

      //place the results back into the state.
      this.rinjState[i * 4] = col2[0];
      this.rinjState[(i * 4) + 1] = col2[1];
      this.rinjState[(i * 4) + 2] = col2[2];
      this.rinjState[(i * 4) + 3] = col2[3];
    }
  }

  /**
   * Method AddRoundKey.
   * @param i int
   */
  private void AddRoundKey(int i) {
    //i tells us what round we're in
    for (int j = 0; j < (this.rinjNBInt * 4); j++) {
      //for each byte of the state we must XOR it with the correct byte in the
      // expanded key
      //w[(i*NB) + (j/4)].theWord[3-(j%4)] gives us the right word and right
      // byte in that word for each
      //byte of the state depending what round we're in
      this.rinjState[j] = this.rinjState[j].XOR(this.w[(i * this.rinjNBInt) + (j / 4)].theRinjWord[3 - (j % 4)]);
    }
  }

  //perfroms inverse byte substitution on all bytes of the state
  /**
   * Method ByteSubInv.
   */
  private void ByteSubInv() {
    for (int i = 0; i < (this.rinjNBInt * 4); i++) {
      this.rinjState[i] = doSinvBox(this.rinjState[i]);
    }
  }

  //performs the inverse of Shift row
  /**
   * Method ShiftRowInv.
   */
  private void ShiftRowInv() {
    //the shift offset is determined by the size of the state
    switch (this.rinjNBInt) {
      case 4:
        doShift(1, 3);
        doShift(2, 2);
        doShift(3, 1);

        break;

      case 6:
        doShift(1, 5);
        doShift(2, 4);
        doShift(3, 3);

        break;

      case 8:
        doShift(1, 7);
        doShift(2, 5);
        doShift(3, 4);

        break;
    }
  }

  /**
   * Method MixColumnInv.
   */
  private void MixColumnInv() {
    RijnByte[] temp = new RijnByte[4];
    RijnByte[] temp2 = new RijnByte[4];

    for (int i = 0; i < this.rinjNBInt; i++) { //for each column of the state

      temp[0] = this.rinjState[i * 4];
      temp[1] = this.rinjState[(i * 4) + 1];
      temp[2] = this.rinjState[(i * 4) + 2];
      temp[3] = this.rinjState[(i * 4) + 3];

      //temp now contains the column in question
      temp2 = MixColumnInvInt(temp); //internal method to mix 1 particular
      // column
      this.rinjState[i * 4] = temp2[0];
      this.rinjState[(i * 4) + 1] = temp2[1];
      this.rinjState[(i * 4) + 2] = temp2[2];
      this.rinjState[(i * 4) + 3] = temp2[3];

      //the state row in question is now updated
    }
  }

  //takes in a RijnByte object and returns its substitute from the S-Box
  //used my method SubByte()
  /**
   * Method doSBox.
   * @param m RijnByte
   * @return RijnByte
   */
  private RijnByte doSBox(RijnByte m) {
    return new RijnByte(this.S[m.getVal()]);
  }

  //takes in a RijnByte object and returns its substitute from the Sinv-Box
  //used by method SubByteInv()
  /**
   * Method doSinvBox.
   * @param m RijnByte
   * @return RijnByte
   */
  private RijnByte doSinvBox(RijnByte m) {
    return new RijnByte(this.Sinv[m.getVal()]);
  }

  //method used my both ShiftRow and ShiftRowInv which takes in what
  //row of the state to shift and how many places to shfit it.
  /**
   * Method doShift.
   * @param row int
   * @param shift int
   */
  private void doShift(int row, int shift) {
    RijnByte[] temp = new RijnByte[this.rinjNBInt];
    RijnByte[] temp2 = new RijnByte[this.rinjNBInt];

    for (int i = 0; i < this.rinjNBInt; i++) {
      temp[i] = this.rinjState[(i * 4) + row];
    }

    //temp array now contains row in question from state
    for (int i = 0; i < this.rinjNBInt; i++) {
      temp2[i] = temp[(i + shift) % this.rinjNBInt];
    }

    //temp2 now contains the row having been shifted
    for (int i = 0; i < this.rinjNBInt; i++) {
      this.rinjState[row + (i * 4)] = temp2[i];

      //state is updated with it's new values
    }
  }

  /**
   * Method keyExpansion.
   */
  private void keyExpansion() {
    RijnWord temp = new RijnWord();
    RijnWord rconWord = new RijnWord();
    this.w = new RijnWord[this.rinjNBInt * (this.NR + 1)]; //will hold the fully expanded key as an
    // array of words

    for (int i = 0; i < (this.rinjNBInt * (this.NR + 1)); i++) {
      this.w[i] = new RijnWord();
    }

    //this adds the cipher key to the beginning of the array of words
    for (int i = 0; i < this.NK; i++) {
      this.w[i].addWord(this.key[4 * i], this.key[(4 * i) + 1], this.key[(4 * i) + 2], this.key[(4 * i) + 3]);
    }

    //main key expansion algorithm
    for (int i = this.NK; i < (this.rinjNBInt * (this.NR + 1)); i++) {
      temp = this.w[i - 1];

      if ((i % this.NK) == 0) {
        temp = SubByte(RotByte(temp));
        rconWord.addWord(this.rcon[(i / this.NK)], new RijnByte(0), new RijnByte(0),
            new RijnByte(0)); //**(i/NK)
        temp = temp.XOR(rconWord);
      }

      if ((this.NK > 6) && ((i % this.NK) == 4)) { // a different version is used if NK >
        // 6
        temp = SubByte(temp);
      }

      this.w[i] = this.w[i - this.NK];
      this.w[i] = this.w[i].XOR(temp);
    }

  }

  //used by the key expansion method
  //this takes in a word and returns a word in which each byte
  //has been replaced by it's S-Box equivalent
  /**
   * Method SubByte.
   * @param mw RijnWord
   * @return RijnWord
   */
  private RijnWord SubByte(RijnWord mw) {
    return new RijnWord(doSBox(mw.theRinjWord[3]), doSBox(mw.theRinjWord[2]),
        doSBox(mw.theRinjWord[1]), doSBox(mw.theRinjWord[0]));
  }

  //used by the key expansion method
  //this returns a word in which the bytes have been shifted one place to the
  // left
  /**
   * Method RotByte.
   * @param a RijnWord
   * @return RijnWord
   */
  private RijnWord RotByte(RijnWord a) {
    return new RijnWord(a.theRinjWord[2], a.theRinjWord[1], a.theRinjWord[0], a.theRinjWord[3]);
  }

  //internal method used by MixColumnInv
  //takes in an array of 4 RijnByte objects and multiplies them in the GF(2^8)
  //by the array 14 11 13 9, 9 14 11 13, 13 9 14 11, 11 13 9 14
  /**
   * Method MixColumnInvInt.
   * @param temp RijnByte[]
   * @return RijnByte[]
   */
  private RijnByte[] MixColumnInvInt(RijnByte[] temp) {
    RijnByte[] col2 = new RijnByte[4]; //holds newly multiplied column
    col2[0] = mul(temp[0].getVal(), 14).XOR(
        mul(temp[1].getVal(), 11).XOR(
            mul(temp[2].getVal(), 13).XOR(mul(temp[3].getVal(), 9))));
    col2[1] = mul(temp[0].getVal(), 9).XOR(
        mul(temp[1].getVal(), 14).XOR(
            mul(temp[2].getVal(), 11).XOR(mul(temp[3].getVal(), 13))));
    col2[2] = mul(temp[0].getVal(), 13).XOR(
        mul(temp[1].getVal(), 9).XOR(
            mul(temp[2].getVal(), 14).XOR(mul(temp[3].getVal(), 11))));
    col2[3] = mul(temp[0].getVal(), 11).XOR(
        mul(temp[1].getVal(), 13).XOR(
            mul(temp[2].getVal(), 9).XOR(mul(temp[3].getVal(), 14))));

    return col2;
  }

  //this method takes in two values and uses the log and alog tables
  //to return the value of them being multiplied together
  //it must use the tables as multiplication in the GF(2^8) is different
  //from regular multiplication
  /**
   * Method mul.
   * @param a int
   * @param b int
   * @return RijnByte
   */
  private RijnByte mul(int a, int b) {
    if ((a != 0) && (b != 0)) {
      return new RijnByte(this.alog[(this.log[a] + this.log[b]) % 255]);
    } 
      return new RijnByte(0);
  }

  //returns an array of ints(ASCII)representation of the state which will be
  // the ciphertext or plaintext
  //depending on when it's called
  /**
   * Method toInt.
   * @return int[]
   */
  public int[] toInt() {
    int[] t = new int[4 * this.rinjNBInt];

    for (int i = 0; i < (this.rinjNBInt * 4); i++) {
      t[i] = this.rinjState[i].getVal();
    }

    //byte[] temp2 = new byte[NB*4];
    //for(int i=0;i<NB*4;i++){
    //temp2[i] = (byte)state[i].getVal();
    //}
    //return new String(temp2);
    return t;
  }

  /**
   * Method createSBox.
   */
  private void createSBox() {
    this.S = new int[] { 99, 124, 119, 123, 242, 107, 111, 197, 48, 1, 103, 43, 254, 215, 171,
        118, 202, 130, 201, 125, 250, 89, 71, 240, 173, 212, 162, 175, 156, 164, 114,
        192, 183, 253, 147, 38, 54, 63, 247, 204, 52, 165, 229, 241, 113, 216, 49, 21, 4,
        199, 35, 195, 24, 150, 5, 154, 7, 18, 128, 226, 235, 39, 178, 117, 9, 131, 44,
        26, 27, 110, 90, 160, 82, 59, 214, 179, 41, 227, 47, 132, 83, 209, 0, 237, 32,
        252, 177, 91, 106, 203, 190, 57, 74, 76, 88, 207, 208, 239, 170, 251, 67, 77, 51,
        133, 69, 249, 2, 127, 80, 60, 159, 168, 81, 163, 64, 143, 146, 157, 56, 245, 188,
        182, 218, 33, 16, 255, 243, 210, 205, 12, 19, 236, 95, 151, 68, 23, 196, 167,
        126, 61, 100, 93, 25, 115, 96, 129, 79, 220, 34, 42, 144, 136, 70, 238, 184, 20,
        222, 94, 11, 219, 224, 50, 58, 10, 73, 6, 36, 92, 194, 211, 172, 98, 145, 149,
        228, 121, 231, 200, 55, 109, 141, 213, 78, 169, 108, 86, 244, 234, 101, 122, 174,
        8, 186, 120, 37, 46, 28, 166, 180, 198, 232, 221, 116, 31, 75, 189, 139, 138,
        112, 62, 181, 102, 72, 3, 246, 14, 97, 53, 87, 185, 134, 193, 29, 158, 225, 248,
        152, 17, 105, 217, 142, 148, 155, 30, 135, 233, 206, 85, 40, 223, 140, 161, 137,
        13, 191, 230, 66, 104, 65, 153, 45, 15, 176, 84, 187, 22 };
  }

  //sets up the inverse S-Box
  /**
   * Method createSinvBox.
   */
  private void createSinvBox() {
    this.Sinv = new int[] { 82, 9, 106, 213, 48, 54, 165, 56, 191, 64, 163, 158, 129, 243,
        215, 251, 124, 227, 57, 130, 155, 47, 255, 135, 52, 142, 67, 68, 196, 222, 233,
        203, 84, 123, 148, 50, 166, 194, 35, 61, 238, 76, 149, 11, 66, 250, 195, 78, 8,
        46, 161, 102, 40, 217, 36, 178, 118, 91, 162, 73, 109, 139, 209, 37, 114, 248,
        246, 100, 134, 104, 152, 22, 212, 164, 92, 204, 93, 101, 182, 146, 108, 112, 72,
        80, 253, 237, 185, 218, 94, 21, 70, 87, 167, 141, 157, 132, 144, 216, 171, 0,
        140, 188, 211, 10, 247, 228, 88, 5, 184, 179, 69, 6, 208, 44, 30, 143, 202, 63,
        15, 2, 193, 175, 189, 3, 1, 19, 138, 107, 58, 145, 17, 65, 79, 103, 220, 234,
        151, 242, 207, 206, 240, 180, 230, 115, 150, 172, 116, 34, 231, 173, 53, 133,
        226, 249, 55, 232, 28, 117, 223, 110, 71, 241, 26, 113, 29, 41, 197, 137, 111,
        183, 98, 14, 170, 24, 190, 27, 252, 86, 62, 75, 198, 210, 121, 32, 154, 219, 192,
        254, 120, 205, 90, 244, 31, 221, 168, 51, 136, 7, 199, 49, 177, 18, 16, 89, 39,
        128, 236, 95, 96, 81, 127, 169, 25, 181, 74, 13, 45, 229, 122, 159, 147, 201,
        156, 239, 160, 224, 59, 77, 174, 42, 245, 176, 200, 235, 187, 60, 131, 83, 153,
        97, 23, 43, 4, 126, 186, 119, 214, 38, 225, 105, 20, 99, 85, 33, 12, 125 };
  }

  //sets up the array of round constants as used by the key expansion
  /**
   * Method createRcon.
   */
  private void createRcon() {
    this.rcon = new RijnByte[] { new RijnByte(0), new RijnByte(1), new RijnByte(2),
        new RijnByte(4), new RijnByte(8), new RijnByte(16), new RijnByte(32),
        new RijnByte(64), new RijnByte(128), new RijnByte(27), new RijnByte(54),
        new RijnByte(108), new RijnByte(216), new RijnByte(123), new RijnByte(246),
        new RijnByte(247), new RijnByte(245), new RijnByte(241), new RijnByte(249),
        new RijnByte(233), new RijnByte(201), new RijnByte(151), new RijnByte(53),
        new RijnByte(106), new RijnByte(212), new RijnByte(179), new RijnByte(125),
        new RijnByte(250), new RijnByte(239), new RijnByte(197), new RijnByte(145) };
  }

  //creates alog and log tables as used by mul(tiply) method which allows
  // multiplication in
  //GF(2^8)
  /**
   * Method createLogs.
   */
  private void createLogs() {
    this.alog[0] = 1;

    for (int i = 1; i < 256; i++) {
      int j = (this.alog[i - 1] << 1) ^ this.alog[i - 1];

      if ((j & 0x100) != 0) {
        j ^= this.ROOT;
      }

      this.alog[i] = j;
    }

    for (int i = 1; i < 255; i++) {
      this.log[this.alog[i]] = i;
    }
  }
}