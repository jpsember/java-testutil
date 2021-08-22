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

import js.data.LongArray;
import js.json.JSObject;
import js.testutil.MyTestCase;

import static js.base.Tools.*;
import static org.junit.Assert.*;

public class LongArrayTest extends MyTestCase {

  @Test
  public void defaultArray() {
    loadTools();
    assertMessage(LongArray.DEFAULT_INSTANCE);
  }

  @Test
  public void builder() {
    LongArray.Builder b = LongArray.newBuilder();
    b.add(72);
    b.add(66);
    assertEquals(2, b.size());
    assertMessage(b);
  }

  @Test
  public void insert() {
    LongArray.Builder b = LongArray.newBuilder();
    b.add(72);
    b.add(66);
    b.add(99);
    b.add(1, 33);
    assertMessage(b);
  }

  @Test
  public void get() {
    LongArray.Builder b = LongArray.newBuilder();
    b.add(72);
    b.add(66);
    b.add(99);
    assertEquals(66, b.get(1));
  }

  @Test
  public void wrappedArrayInBuilder() {
    LongArray.Builder b = LongArray.newBuilder();
    for (int i = 0; i < 67; i++) {
      b.add(i);
    }
    long[] a = b.array();
    assertEquals(67, a.length);
    a[3] = (byte) 88;
    assertEquals(88, b.get(3));
  }

  @Test
  public void encodeAndDecodeJson() {
    LongArray.Builder b = LongArray.newBuilder();
    for (int i = 0; i < 300; i++) {
      b.add(i + 50);
    }
    long[] a = b.build().array();
    JSObject obj = b.toJson();
    LongArray c = LongArray.DEFAULT_INSTANCE.parse(obj);
    log("parsed:", INDENT, obj, CR, "to:", CR, c);
    assertArrayEquals(a, c.array());
  }

}
