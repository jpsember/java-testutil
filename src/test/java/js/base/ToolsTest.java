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

import java.util.List;

import org.junit.Test;

import js.json.JSMap;
import js.testutil.MyTestCase;

import static js.base.Tools.*;

public class ToolsTest extends MyTestCase {

  @Test
  public void todoReport() {
    redirectSystemOut();
    todo("Here is a todo report", 72);
    todo("Here is a second report");
    todo("Here is a second report");
    todo("Here is a third report");
    String output = restoreSystemOut();
    log("output:", INDENT, output);
    assertTrue(output.startsWith("*** TODO: Here is a todo report"));
    assertEquals(3, split(output.trim(), '\n').size());
  }

  @Test
  public void callPr() {
    redirectSystemOut();
    pr("Here", "is", "a", "pr", "call");
    pr("some", 72, 1.5f, "numbers");
    assertSystemOut();
  }

  @Test
  public void spacesTest() {
    assertEquals("          ", spaces(10));
  }

  @Test
  public void tabTest() {
    StringBuilder sb = new StringBuilder();
    tab(sb, 0);
    sb.append("|");
    tab(sb, 8);
    sb.append("|");
    tab(sb, 12);
    sb.append("|");
    tab(sb, -2);
    sb.append("|");
    assertMessage(sb.toString());
  }

  @Test
  public void insertStringToFrontTest() {
    redirectSystemOut();
    Object[] array = { "Here", "is", "an", "array", 17.0, 5 };
    pr(array);
    pr(insertStringToFront("Hello", array));
    assertSystemOut();
  }

  @Test
  public void chompTest() {
    String s = "alphaalphabravo";
    assertEquals(s, chomp(s));
    assertEquals(s, chomp(s + "\n\n"));
    assertEquals("alphaalpha", chomp(s, "bravo"));
    assertEquals("aaa", chomp("aaa", "b"));
    assertEquals("alphabravo", chompPrefix(s, "alpha"));
    assertEquals("a", chompPrefix("a", "b"));
  }

  @Test
  public void camelCaseTest() {
    String s2 = "js.base.tools_test.java";
    String s1 = "js.Base.ToolsTest.java";
    assertEquals(s2, convertCamelToUnderscore(s1));
  }

  @Test
  public void fromCamelCaseTest() {
    String s1 = "alpha.charlie_bravo_delta";
    String s2 = "Alpha.charlieBravoDelta";
    assertEquals(s2, convertUnderscoreToCamel(s1));
  }

  @Test
  public void nullOrEmptyTest() {
    assertFalse(nullOrEmpty("abc"));
    assertTrue(nullOrEmpty((String) null));
    assertTrue(nullOrEmpty(""));
  }

  @Test
  public void nonEmptyTest() {
    assertTrue(nonEmpty("abc"));
    assertFalse(nonEmpty((String) null));
    assertFalse(nonEmpty(""));
  }

  @Test
  public void ifNullOrEmptyTest() {
    assertEquals("abc", ifNullOrEmpty(null, "abc"));
    assertEquals("abc", ifNullOrEmpty("", "abc"));
    assertEquals("xyz", ifNullOrEmpty("xyz", "abc"));
    assertEquals("abc", nullToEmpty("abc"));
    assertEquals("", nullToEmpty(null));
  }

  @Test
  public void debStrTest() {
    assertEquals("<null>", debStr(null));
    assertEquals("\"hello\"", debStr("hello"));
  }

  @Test
  public void toStrTest() {
    assertEquals("    12.3400", toStr(12.34));
    assertEquals("  1234", toStr(1234));
    assertEquals("     1234", toStr(1234, 9));
    assertEquals("1234", toStr(1234, 2));
  }

  @Test
  public void quoteTest() {
    assertEquals("\"1234\"", quote("1234"));
  }

  @Test
  public void splitTest() {
    List<String> result = split("aabcdebbfgh", 'b');
    StringBuilder sb = new StringBuilder();
    for (String word : result) {
      sb.append('|');
      sb.append(word);
    }
    assertMessage(sb);
  }

  @Test
  public void trimTest() {
    JSMap json = map();

    String whiteSpaceChars = "         \n\n\n\t";
    String sampleText = "a ab abc abcd abcdefghijklmno";

    for (int sampleNumber = 0; sampleNumber < 50; sampleNumber++) {
      StringBuilder sb = new StringBuilder();
      for (int part = 0; part < 3; part++) {
        if (part == 1) {
          if (random().nextInt(3) == 0)
            continue;
          int textLength = random().nextInt(8) + 1;
          int textStart = random().nextInt(sampleText.length() - textLength);
          String textExpr = sampleText.substring(textStart, textStart + textLength).trim();
          sb.append(textExpr);
        } else {
          for (int i = random().nextInt(12); i > 0; i--) {
            char wsChar = whiteSpaceChars.charAt(random().nextInt(whiteSpaceChars.length()));
            sb.append(wsChar);
          }
        }
      }
      String expr = sb.toString();
      json.putNumbered("Sample", expr);
      json.putNumbered("Left", trimLeft(expr));
      json.putNumbered("Right", trimRight(expr));
      json.putNumbered("Both", expr.trim());
      json.putNumbered("--------------------------------");
    }
    generateMessage(json.prettyPrint());
    assertGenerated();
  }

}
