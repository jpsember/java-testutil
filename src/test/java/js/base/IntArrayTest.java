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
package js.base;

import org.junit.Test;

import js.data.IntArray;
import js.json.JSObject;
import js.testutil.MyTestCase;

import static js.base.Tools.*;
import static org.junit.Assert.*;

public class IntArrayTest extends MyTestCase {

  @Test
  public void defaultArray() {
    loadTools();
    assertMessage(IntArray.DEFAULT_INSTANCE);
  }

  @Test
  public void builder() {
    IntArray.Builder b = IntArray.newBuilder();
    b.add(72);
    b.add(66);
    assertEquals(2, b.size());
    assertMessage(b);
  }

  @Test
  public void insert() {
    IntArray.Builder b = IntArray.newBuilder();
    b.add(72);
    b.add(66);
    b.add(99);
    b.add(1, 33);
    assertMessage(b);
  }

  @Test
  public void get() {
    IntArray.Builder b = IntArray.newBuilder();
    b.add(72);
    b.add(66);
    b.add(99);
    assertEquals(66, b.get(1));
  }

  @Test
  public void wrappedArrayInBuilder() {
    IntArray.Builder b = IntArray.newBuilder();
    for (int i = 0; i < 67; i++) {
      b.add(i);
    }
    int[] a = b.array();
    assertEquals(67, a.length);
    a[3] = (byte) 88;
    assertEquals(88, b.get(3));
  }

  @Test
  public void encodeAndDecodeJson() {
    IntArray.Builder b = IntArray.newBuilder();
    for (int i = 0; i < 300; i++) {
      b.add(i + 50);
    }
    int[] a = b.build().array();
    JSObject obj = b.toJson();
    IntArray c = IntArray.DEFAULT_INSTANCE.parse(obj);
    log("parsed:", INDENT, obj, CR, "to:", CR, c);
    assertArrayEquals(a, c.array());
  }

  private static final int[] sSampleInts = { 72, 66, 55, 19, 13 };

  @Test
  public void changesReflectedInWrappedArray() {
    int[] w = { 77, 88, 99 };
    IntArray a = IntArray.with(w);
    a.array()[1] = 55;
    assertEquals(55, w[1]);
  }

  @Test
  public void hashCodeBuilderAgreesWithImmutable() {
    IntArray a = IntArray.with(sSampleInts);
    IntArray.Builder b = IntArray.newBuilder();
    for (int x : sSampleInts)
      b.add(x);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void hashCodeBuilderAfterChanges() {
    IntArray.Builder a = IntArray.newBuilder();
    a.add(72);
    a.add(55);
    int h1 = a.hashCode();
    assertEquals(h1, a.hashCode());
    a.add(66);
    assertFalse(h1 == a.hashCode());
    a.add(32);
    a.remove(2);
    a.remove(2);
    assertEquals(h1, a.hashCode());
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void removeIllegalSlot() {
    IntArray.Builder a = IntArray.newBuilder().add(72).add(55);
    a.remove(-1);
  }

}
