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

import static org.junit.Assert.*;

import org.junit.Test;

public class BasePrinterTest {

  @Test
  public void variousObjectsTest() {
    p().append(true);
    p().append(-7);
    p().append(-3.52e-3);
    p().append(12L);
    p().append(" hello ");
    p().append(5);
    p().cr();
    p().append("hello");
    result("T     -7   -0.0035        12 hello       5\n" + "hello");
  }

  @Test
  public void registerTest() {
    BasePrinter.registerClassHandler(Foo.class, (x, p) -> p.append("wow"));
    p().append("hey");
    p().pr("hey", new Foo(), new Foo(), "xyz");
    result("hey hey wow wow xyz");
  }

  /**
   * A class to test registering
   */
  private static class Foo {
  }

  private void result(Object obj) {
    assertEquals(obj.toString(), p().toString());
  }

  private BasePrinter p() {
    if (mPrinter == null)
      mPrinter = new BasePrinter();
    return mPrinter;
  }

  private BasePrinter mPrinter;

}
