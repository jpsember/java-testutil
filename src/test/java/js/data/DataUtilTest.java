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

import static js.base.Tools.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import js.json.JSList;
import js.testutil.MyTestCase;

public class DataUtilTest extends MyTestCase {

  @Test
  public void constructByteArray() {
    loadTools();
    byte[] SAMPLE_BYTES = { -128, -127, -126, -125, //
        -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 124, 125, 126, 127, };
    ByteArray byteArray = ByteArray.with(SAMPLE_BYTES);
    assertMessage(byteArray);
  }

  @Test
  public void bytesToShortsLittleEndian() {
    byte[] b = { // Should produce - 12,345
        (byte) 0xc7, (byte) 0xcf, };
    assertMessage(ShortArray.with(DataUtil.bytesToShortsLittleEndian(b)));
  }

  @Test
  public void bytesToShortsBigEndian() {
    byte[] b = { // Should produce - 12,345
        (byte) 0xcf, (byte) 0xc7, };
    assertMessage(ShortArray.with(DataUtil.bytesToShortsBigEndian(b)));
  }

  @Test
  public void bytesToIntsLittleEndian() {
    byte[] b = { // Should produce - 1,234,567,890
        0x2E, (byte) 0xFD, 0x69, (byte) 0xB6, };
    assertMessage(IntArray.with(DataUtil.bytesToIntsLittleEndian(b)));
  }

  @Test
  public void bytesToIntsBigEndian() {
    byte[] b = { // Should produce - 1,234,567,890
        (byte) 0xB6, 0x69, (byte) 0xFD, 0x2E };
    assertMessage(IntArray.with(DataUtil.bytesToIntsBigEndian(b)));
  }

  @Test
  public void bytesToLongsBigEndian() {
    byte[] b = { // Should produce -123,456,789,012,345
        (byte) 0xff, (byte) 0xFF, (byte) 0x8F, (byte) 0xB7, (byte) 0x79, (byte) 0xF2, (byte) 0x20,
        (byte) 0x87, };
    assertMessage(LongArray.with(DataUtil.bytesToLongsBigEndian(b)));
  }

  @Test
  public void bytesToLongsBigEndian2() {
    byte[] b = { (byte) 0x40, (byte) 0x0, (byte) 0x0, (byte) 0x0, //
        (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0, //
    };
    LongArray la = LongArray.with(DataUtil.bytesToLongsBigEndian(b));
    long lv = la.get(0);
    assertEquals(0x4000000080000000l, lv);
  }

  @Test
  public void intsToBytesLittleEndian() {
    int[] input = { 1234567890, -1234567890, };
    byte[] b = DataUtil.intsToBytesLittleEndian(input);
    StringBuilder sb = new StringBuilder();
    for (byte x : b) {
      sb.append(DataUtil.hex8(x));
    }
    log(sb);
    assertEquals("d20296492efd69b6", sb.toString());
  }

  private static int[] SAMPLE_INTS = { //
      Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 3, //
      -4, -3, -2, -1, //
      0, 1, 2, 3, //
      Integer.MAX_VALUE - 3, Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE, };

  @Test
  public void intsToBytesLittleEndianAndBack() {
    byte[] b = DataUtil.intsToBytesLittleEndian(SAMPLE_INTS);
    int[] output = DataUtil.bytesToIntsLittleEndian(b);
    assertTrue(Arrays.equals(SAMPLE_INTS, output));
  }

  @Test
  public void longsToBytesBigEndian() {
    long[] longs = { -1234567890123456l };
    byte[] b = DataUtil.longsToBytesBigEndian(longs);
    StringBuilder sb = new StringBuilder();
    for (byte x : b) {
      sb.append(DataUtil.hex8(x));
    }
    assertEquals("fffb9d2ac3754540", sb.toString());
  }

  @Test
  public void repeatedUnmodifiableLists() {
    List<String> a = arrayList();
    a.add("hello");
    List<String> a2 = DataUtil.immutableCopyOf(a);
    assertNotSame(a2, a);
    List<String> a3 = DataUtil.immutableCopyOf(a2);
    assertSame(a2, a3);
  }

  @Test
  public void repeatedUnmodifiableMaps() {
    Map<String, String> a = hashMap();
    a.put("hello", "jim");
    Map<String, String> a2 = DataUtil.immutableCopyOf(a);
    assertNotSame(a2, a);
    Map<String, String> a3 = DataUtil.immutableCopyOf(a2);
    assertSame(a2, a3);
  }

  @Test
  public void repeatedUnmodifiableSets() {
    Set<String> a = hashSet();
    a.add("hello");
    Set<String> a2 = DataUtil.immutableCopyOf(a);
    assertNotSame(a2, a);
    Set<String> a3 = DataUtil.immutableCopyOf(a2);
    assertSame(a2, a3);
  }

  @Test
  public void arrayNumbers() {
    var src = new ArrayList<Double>();
    src.add(1.2);
    src.add(1.7);
    src.add(-3.2);
    var x = DataUtil.intArray(src);
    assertMessage(JSList.with(x));
  }

}
