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

import js.data.FloatArray;
import js.json.JSObject;
import js.testutil.MyTestCase;

import static js.base.Tools.*;
import static org.junit.Assert.*;

public class FloatArrayTest extends MyTestCase {

  @Test
  public void defaultArray() {
    loadTools();
    assertMessage(FloatArray.DEFAULT_INSTANCE);
  }

  @Test
  public void builder() {
    FloatArray.Builder b = FloatArray.newBuilder();
    b.add(7.2f);
    b.add(6.6f);
    assertEquals(2, b.size());
    assertMessage(b);
  }

  @Test
  public void insert() {
    FloatArray.Builder b = FloatArray.newBuilder();
    b.add(7.2f);
    b.add(6.6f);
    b.add(99);
    b.add(1, 33);
    assertMessage(b);
  }

  @Test
  public void get() {
    FloatArray.Builder b = FloatArray.newBuilder();
    b.add(7.2f);
    b.add(6.6f);
    b.add(9.9f);
    assertEquals(6.6f, b.get(1), 1e-4f);
  }

  @Test
  public void wrappedArrayInBuilder() {
    FloatArray.Builder b = FloatArray.newBuilder();
    for (int i = 0; i < 67; i++) {
      b.add(i);
    }
    float[] a = b.array();
    assertEquals(67, a.length);
    a[3] = (byte) 88;
    assertEquals(88, b.get(3), 1e-4f);
  }

  @Test
  public void encodeAndDecodeJson() {
    FloatArray.Builder b = FloatArray.newBuilder();
    for (int i = 0; i < 300; i++) {
      b.add(i + 50);
    }
    float[] a = b.build().array();
    JSObject obj = b.toJson();
    FloatArray c = FloatArray.DEFAULT_INSTANCE.parse(obj);
    log("parsed:", INDENT, obj, CR, "to:", CR, c);
    assertArrayEquals(a, c.array(), 1e-4f);
  }

}
