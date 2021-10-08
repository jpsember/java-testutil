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
package js.json;

import static js.base.Tools.*;
import static org.junit.Assert.*;

import org.junit.Test;

import js.geometry.Matrix;
import js.testutil.MyTestCase;

public class JSListTest extends MyTestCase {

  @Test(expected = UnsupportedOperationException.class)
  public void modifyLockedList() {
    JSList lst = new JSList().add("a");
    lst.lock();
    lst.add("b");
  }

  @Test
  public void putDataObjects() {
    JSList list = new JSList();
    Matrix mx = Matrix.getScale(2f, 1f);
    list.add(mx);
    assertMessage(list.prettyPrint());
  }

  @Test
  public void equalityIntegers() {
    JSList m1 = list().add(100L).add(100);
    JSList m2 = list().add(100).add(100);
    assertEquals(m1, m2);
  }

  @Test
  public void inequalityIntegers() {
    JSList m1 = list().add(100L).add(100);
    JSList m2 = list().add(101L).add(100);
    assertNotEquals(m1, m2);
  }

  @Test
  public void equalityIntegersNested() {
    JSList m1 = list().add(100L).add(100);
    JSList m2 = list().add(100).add(100);
    JSList m3 = list().add(m1);
    JSList m4 = list().add(m2);
    assertEquals(m3, m4);
  }

  @Test
  public void equalityFloats() {
    JSList m1 = list().add(100.0).add(100f);
    JSList m2 = list().add(100f).add(100f);
    assertEquals(m1, m2);

  }

  @Test
  public void equalityFloatsNested() {
    JSList m1 = list().add(100.0).add(100f);
    JSList m2 = list().add(100f).add(100f);
    JSList m3 = list().add(m1);
    JSList m4 = list().add(m2);
    assertEquals(m3, m4);
  }
}
