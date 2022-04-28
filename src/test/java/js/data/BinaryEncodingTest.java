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

import js.testutil.MyTestCase;

import static js.base.Tools.*;
import static js.data.BitUtil.*;

public class BinaryEncodingTest extends MyTestCase {

  @Test
  public void range_2() {
    range(2);
  }

  @Test
  public void range_3() {
    range(3);
  }

  @Test
  public void range_4() {
    range(4);
  }

  @Test
  public void range_5() {
    range(5);
  }

  @Test
  public void range_7() {
    range(7);
  }

  @Test
  public void range_10() {
    range(10);
  }

  @Test
  public void range_max() {
    range(MAX_TRUNCATED_BINARY_VALUE);
  }

  private void range(int maxValue) {
    IntArray.Builder original = IntArray.newBuilder();

    int maxPrinted = 50;
    for (int i = 0; i < maxValue; i++) {
      original.add(i);
      w().writeTruncated(maxValue, i);
      if (maxValue > 2 * maxPrinted && i == maxPrinted) {
        i = maxValue - maxPrinted;
      }
    }

    BitReader rd = new BitReader(w().result());
    IntArray.Builder decoded = IntArray.newBuilder();
    for (int j = 0; j < original.size(); j++) {
      int value = rd.readTruncated(maxValue);
      decoded.add(value);
    }
    assertEquals(original.build(), decoded.build());
    assertResult();
  }

  @Test
  public void unary() {
    resetSeed(42);
    int range = 50;
    IntArray.Builder original = IntArray.newBuilder();
    for (int i = 0; i < 20; i++) {
      int val = random().nextInt(range);
      w().writeUnary(val);
      original.add(val);
    }
    IntArray.Builder readback = IntArray.newBuilder();
    BitReader rd = new BitReader(w().result());

    for (int i = 0; i < original.size(); i++) {
      readback.add(rd.readUnary());
    }
    assertEquals(original, readback);
  }

  @Test
  public void golomb() {
    int m = 10;
    int count = 200;
    resetSeed(42);
    int range = 50;
    IntArray.Builder original = IntArray.newBuilder();
    for (int i = 0; i < count; i++) {
      int val = random().nextInt(range);
      w().writeGolomb(m, val);
      original.add(val);
    }
    if (verbose())
      log("wrote golomb; m", m, "count", count, INDENT, DataUtil.bitString(w().result()));

    IntArray.Builder readback = IntArray.newBuilder();
    BitReader rd = new BitReader(w().result());

    for (int i = 0; i < original.size(); i++) {
      int v = rd.readGolomb(m);
      readback.add(v);
    }
    assertEquals(original, readback);
  }

  private void assertResult() {
    assertMessage(DataUtil.bitString(w().result()));
  }

  private BitWriter w() {
    if (mBitWriter == null) {
      loadTools();
      mBitWriter = new BitWriter();
      mBitWriter.setVerbose(verbose());
    }
    return mBitWriter;
  }

  private BitWriter mBitWriter;
}
