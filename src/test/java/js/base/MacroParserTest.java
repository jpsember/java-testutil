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

import static js.base.Tools.*;
import js.json.JSMap;
import js.parsing.MacroParser;
import js.testutil.MyTestCase;

public class MacroParserTest extends MyTestCase {

  @Test
  public void example() {
    parse("abc[!x]\ndef[!y]\nghi");
  }

  @Test
  public void noLinefeeds() {
    parse("abc[!x]def[!y]ghi[!z]klm");
  }

  @Test
  public void multipleLinefeeds() {
    parse("abc[!x]\n\n\ndef[!y]\n\n\nghi[!z]\n\n\nklm\n\n\n");
  }

  private void parse(Object content) {
    String str = content.toString();
    p().withTemplate(str).withMapper(m());
    String result = p().content();
    log("Macros:", INDENT, showLf(m()));
    log("Template:", INDENT, showLf(str));
    log("Result:");
    assertMessage(showLf(result));
  }

  /**
   * The logger will suppress multiple empty linefeeds, so insert extra chars to
   * prevent this
   */
  private static String showLf(Object obj) {
    String s = obj.toString();
    final String prefix = "-->";
    final String suffix = "<--";
    s = "\n" + s;
    s = s.replace("\n", suffix + "\n" + prefix);
    s = s.substring(0, s.length() - prefix.length());
    return s;
  }

  private JSMap m() {
    if (mMacroMap == null) {
      mMacroMap = map().put("x", "alpha").put("y", "beta").put("z", "gamma");
    }
    return mMacroMap;
  }

  private JSMap mMacroMap;

  private MacroParser p() {
    if (mParser == null)
      mParser = new MacroParser();
    return mParser;
  }

  private MacroParser mParser;
}
