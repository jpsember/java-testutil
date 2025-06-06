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
 **/
package js.json;

import static js.base.Tools.*;
import static org.junit.Assert.*;

import org.junit.Test;

import js.geometry.Matrix;
import js.testutil.MyTestCase;

public class JSMapTest extends MyTestCase {

  @Test
  public void parseMapWithComments() {
    // Apparently my JSON parser doesn't allow comments?
    var s = " # This is a comment\n { \"alpha\" : [1,2,3] } # another comment\n";
    var mp = new JSMap(s);
    generateMessage(mp);
  }

  @Test
  public void parseMapTrailingCommas() {
    var s = " { \"alpha\" : [1,2,3,], } \n";
    var mp = new JSMap(s);
    assertMessage(mp);
  }

  @Test
  public void prettyPrintTest() {
    StringBuilder sb = new StringBuilder();
    JSMap m = new JSMap();
    for (int i = 1; i <= 23; i += 3) {
      m.put(spaces(i), new JSMap().put("x", i).put("y", i * i));
      sb.append(m.prettyPrint());
    }
    assertMessage(sb);
  }

  @Test
  public void putDataObjects() {
    JSMap m = new JSMap();
    Matrix mx = Matrix.getScale(2f, 1f);
    m.put("matrix", mx);
    assertMessage(m);
  }

  @Test
  public void emptyMap() {
    String content = "{}";
    JSMap m = new JSMap(content);
    assertMessage(m);
  }

  @Test
  public void parseNullValueInMap() {
    String content = "{\"x\":[],\"y\":\"hello\",\"z\":null}";
    JSMap m = new JSMap(content);
    assertMessage(m.prettyPrint());
  }

  @Test
  public void equalityIntegers() {
    JSMap m1 = map().put("a", 100L).put("b", 100);
    JSMap m2 = map().put("a", 100).put("b", 100);
    assertEquals(m1, m2);
  }

  @Test
  public void equalityIntegersNested() {
    JSMap m1 = map().put("a", 100L).put("b", 100);
    JSMap m2 = map().put("a", 100).put("b", 100);
    JSMap m3 = map().put("m", m1);
    JSMap m4 = map().put("m", m2);
    assertEquals(m3, m4);
  }

  @Test
  public void equalityFloats() {
    JSMap m1 = map().put("a", 100.0).put("b", 100f);
    JSMap m2 = map().put("a", 100f).put("b", 100f);
    assertEquals(m1, m2);
  }

  @Test
  public void equalityFloatsNested() {
    JSMap m1 = map().put("a", 100.0).put("b", 100f);
    JSMap m2 = map().put("a", 100f).put("b", 100f);
    JSMap m3 = map().put("m", m1);
    JSMap m4 = map().put("m", m2);
    assertEquals(m3, m4);
  }

}
