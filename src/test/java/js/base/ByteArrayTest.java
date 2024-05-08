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

import js.data.ByteArray;
import js.json.JSList;
import js.json.JSObject;
import js.testutil.MyTestCase;


import static js.base.Tools.*;
import static org.junit.Assert.*;

public class ByteArrayTest extends MyTestCase {

  @Test
  public void defaultArray() {
    loadTools();
    //var x = FileUtils.getUserDirectory();
    assertMessage(bytesAsInts(ByteArray.DEFAULT_INSTANCE.array()));
  }

  @Test
  public void builder() {
    ByteArray.Builder b = ByteArray.newBuilder();
    b.add((byte) 72);
    b.add((byte) 66);
    assertEquals(2, b.size());
    assertMessage(bytesAsInts(b.array()));
  }

  @Test
  public void insert() {
    ByteArray.Builder b = ByteArray.newBuilder();
    b.add((byte) 72);
    b.add((byte) 66);
    b.add((byte) 99);
    b.add(1, (byte) 33);
    assertMessage(bytesAsInts(b.array()));
  }

  @Test
  public void get() {
    ByteArray.Builder b = ByteArray.newBuilder();
    b.add((byte) 72);
    b.add((byte) 66);
    b.add((byte) 99);
    assertEquals(66, b.get(1));
  }

  @Test
  public void wrappedArrayInBuilder() {
    ByteArray.Builder b = ByteArray.newBuilder();
    for (int i = 0; i < 67; i++) {
      b.add((byte) i);
    }
    byte[] a = b.array();
    assertEquals(67, a.length);
    a[3] = (byte) 88;
    assertEquals(88, b.get(3));
  }

  @Test
  public void json() {
    ByteArray.Builder b = ByteArray.newBuilder();
    for (int i = 0; i < 300; i++) {
      b.add((byte) (i + 50));
    }
    assertMessage(b);
  }

  @Test
  public void encodeAndDecodeJson() {
    ByteArray.Builder b = ByteArray.newBuilder();
    for (int i = 0; i < 300; i++) {
      b.add((byte) (i + 50));
    }
    byte[] a = b.build().array();
    JSObject obj = b.toJson();
    ByteArray c = ByteArray.DEFAULT_INSTANCE.parse(obj);
    log("parsed:", INDENT, obj, CR, "to:", CR, c);
    assertArrayEquals(a, c.array());
  }

  /**
   * Convert array of bytes to a json list with the individual values, not a
   * base64 string as is usual
   */
  private static JSList bytesAsInts(byte[] bytes) {
    JSList out = list();
    for (byte x : bytes)
      out.add(x);
    return out;
  }

}
