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

import java.io.File;

import org.junit.Test;

import js.json.JSMap;
import js.testutil.MyTestCase;

import static js.base.Tools.*;

public class BinaryCodecTest extends MyTestCase {

  @Test
  public void a() {
    doit();
  }

  @Test
  public void b() {
    doit();
  }

  private void doit() {
    doit(name() + ".json");
  }

  private void doit(String filename) {
    loadTools();
    File tf = testFile(filename);
    JSMap m = JSMap.from(tf);
    byte[] encoded = BinaryCodec.encode(m);
    JSMap m2 = BinaryCodec.decode(encoded);

    int origLen = m.toString().length();
    int encodedLen = encoded.length;

    JSMap res = map().put("original len", origLen).put("encoded len", encodedLen).put("ratio",
        encodedLen / (float) origLen);
    generateMessage(res.prettyPrint());
    assertEquals(m.toString(), m2.toString());
    assertGenerated();
  }

}
