/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package js.data;

import static org.junit.Assert.*;

import org.junit.Test;

import js.data.BitReader;
import js.data.BitWriter;
import js.data.IntArray;
import js.testutil.MyTestCase;

import static js.base.Tools.*;
import static js.data.BitUtil.*;
import static js.data.DataUtil.*;

public class BitWriterTest extends MyTestCase {

  @Test
  public void singleFalse() {
    w().write(false);
    assertResult();
  }

  @Test
  public void singleTrue() {
    w().write(true);
    assertResult();
  }

  @Test
  public void multipleBits() {
    w().write(3, 7);
    w().write(6, 63);
    assertResult();
  }

  @Test
  public void wordExceedsSize() {
    for (int s = 1; s < Integer.SIZE; s++) {
      w().write(false);
      w().write(s, ~0);
    }
    assertResult();
  }

  @Test
  public void wordExceedsSize2() {
    for (int s = 1; s < Integer.SIZE; s++) {
      w().write(s, ~0);
      w().write(Integer.SIZE - s, 0);
    }
    assertResult();
  }

  @Test
  public void crossIntBoundary() {
    w().write(false);
    w().write(23, ~0);
    w().write(false);
    w().write(18, 1492);
    w().write(false);
    assertResult();
  }

  @Test
  public void testMaskOutOfRangeBits() {
    IntArray.Builder out = IntArray.newBuilder();
    for (int i = 0; i <= Integer.SIZE; i++) {
      out.add(maskUpperBits(i, ~0));
    }
    assertMessage(bitString(out.array()));
  }

  @Test
  public void sample1() {
    randomSample(1966, 1);
  }

  @Test
  public void sample2() {
    randomSample(1965, 2);
  }

  @Test
  public void sample3() {
    randomSample(1965, 3);
  }

  @Test
  public void samplex() {
    randomSample(1961, 7);
  }

  @Test
  public void findSmallFailingDataSets() {
    if (true)
      return;
    for (int seed = 1; seed < 10000; seed++) {
      wDiscard();
      pr("seed:", seed);
      randomSample(seed, 2);
    }
  }

  @Test
  public void sample_2() {
    randomSample(5198, 2);
  }

  @Test
  public void sample100() {
    randomSample(1965, 100);
  }

  @Test
  public void explicit_32_ones_31_zeros() {
    explicitWords(Integer.SIZE, ~0, Integer.SIZE - 1, 0);
  }

  @Test
  public void straddle() {
    // Writes sequences of A zeroes followed by B ones, verifies the counts of zeros + ones + zeros... is as expected
    //
    for (int prefixLength = 1; prefixLength < Integer.SIZE; prefixLength++) {
      for (int bodyLength = 1; bodyLength <= Integer.SIZE; bodyLength++) {
        verifyPair(prefixLength, bodyLength, false);
      }
    }
  }

  @Test
  public void straddle_1_32() {
    verifyPair(1, Integer.SIZE, false);
  }

  @Test
  public void straddleInv() {
    // Writes sequences of A ones followed by B zeros, verifies the counts of ones + zeroes + ... is as expected
    //
    for (int prefixLength = 1; prefixLength < Integer.SIZE; prefixLength++) {
      for (int bodyLength = 1; bodyLength <= Integer.SIZE; bodyLength++) {
        verifyPair(prefixLength, bodyLength, true);
      }
    }
  }

  private void verifyPair(int prefixLength, int bodyLength, boolean inverted) {
    log(VERT_SP, "Prefix:", prefixLength, "Body:", bodyLength, inverted ? "(INV)" : "");
    BitWriter w = new BitWriter();
    //w.setVerbose(verbose());
    w.write(prefixLength, inverted ? ~0 : 0);
    w.write(bodyLength, inverted ? 0 : ~0);

    IntArray.Builder exp = IntArray.newBuilder();
    exp.add(prefixLength);

    int prefWithBodyLen = prefixLength + bodyLength;
    int totalLen = ((prefWithBodyLen + Integer.SIZE - 1) / Integer.SIZE) * Integer.SIZE;
    int suffixLen = totalLen - prefWithBodyLen;

    if (!inverted) {
      exp.add(bodyLength);
      if (suffixLen > 0)
        exp.add(suffixLen);
    } else {
      exp.add(bodyLength + suffixLen);
    }
    IntArray ew = exp.build();
    IntArray result = describe(w);
    log("expected:", ew);
    log("received:", result);
    assertEquals(ew, result);
  }

  private void explicitWords(int... countAndPatternPairs) {
    IntArray.Builder ia = IntArray.newBuilder();
    for (int i = 0; i < countAndPatternPairs.length; i += 2) {
      int sz = countAndPatternPairs[i];
      int val = countAndPatternPairs[i + 1];
      val = maskUpperBits(sz, val);
      ia.add(val);
      log("write:", sz, val, bitString(sz, val));
      w().write(sz, val);
    }
    if (verbose())
      log("wrote summary:", INDENT, bitString(w().result()));

    prepareReader(w().result());
    IntArray.Builder ib = IntArray.newBuilder();
    for (int i = 0; i < countAndPatternPairs.length; i += 2) {
      int sz = countAndPatternPairs[i];
      ib.add(r().read(sz));
    }
    assertEquals(ia, ib);
  }

  private void randomSample(int seed, int count) {
    IntArray.Builder ia = IntArray.newBuilder();
    resetSeed(seed);
    for (int i = 0; i < count; i++) {
      int sz = random().nextInt(Integer.SIZE) + 1;
      int val = random().nextInt();
      val = (val & 0xff);
      val = maskUpperBits(sz, val);
      ia.add(val);
      log("write:", sz, val, bitString(sz, val));
      w().write(sz, val);
    }
    if (verbose())
      log("wrote summary:", INDENT, bitString(w().result()));

    prepareReader(w().result());
    IntArray.Builder ib = IntArray.newBuilder();
    resetSeed(seed);
    for (int i = 0; i < count; i++) {
      int sz = random().nextInt(Integer.SIZE) + 1;
      random().nextInt();
      ib.add(r().read(sz));
    }
    assertEquals(ia, ib);
  }

  private void assertResult() {
    assertMessage(bitString(w().result()));
  }

  private BitWriter w() {
    if (mBitWriter == null) {
      mBitWriter = new BitWriter();
      mBitWriter.setVerbose(verbose());
    }
    return mBitWriter;
  }

  private void wDiscard() {
    mBitWriter = null;
  }

  private BitWriter mBitWriter;

  private void prepareReader(int[] source) {
    mBitReader = new BitReader(source);
  }

  private BitReader r() {
    return mBitReader;
  }

  private IntArray describe(BitWriter w) {
    return describe(w.result());
  }

  /**
   * Describe the number of contiguous sequences of zeros and ones that appear
   * in an int array
   */
  private IntArray describe(int[] intArray) {
    IntArray.Builder result = IntArray.newBuilder();

    int sequenceLength = 0;
    int previousBit = -1;

    for (int word : intArray) {
      int mask = 1 << (Integer.SIZE - 1);
      while (mask != 0) {
        int bit = (word & mask) != 0 ? 1 : 0;
        mask >>>= 1;
        if (bit != previousBit) {
          if (sequenceLength > 0) {
            result.add(sequenceLength);
            sequenceLength = 0;
          }
          previousBit = bit;
        }
        sequenceLength++;
      }
    }
    if (sequenceLength > 0) {
      result.add(sequenceLength);
    }

    return result.build();
  }

  private BitReader mBitReader;
}
