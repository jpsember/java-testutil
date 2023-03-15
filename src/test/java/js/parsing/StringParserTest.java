package js.parsing;

import static js.base.Tools.*;

import org.junit.Test;

import js.testutil.MyTestCase;
import js.json.JSMap;

public class StringParserTest extends MyTestCase {

  @Test
  public void peekIs() {
    p("alpha bravo");
    checkState(!p().peekIs("bravo"));
    p().read("alpha ");
    checkState(p().peekIs("bravo"));
  }

  @Test
  public void readPath() {
    auxReadPath("alpha/bravo/charlie.txt");
  }

  @Test
  public void readPathWithEscaped() {
    // This will fail since we aren't yet attempting to deal with embedded " in paths
    if (false)
      auxReadPath("\"alpha/bravo/charlie\\\"embedded\"");
  }

  @Test
  public void readInts() {
    String inp = "0 1 -0 -1 123 456 - alpha 5.5";
    p(inp);
    JSMap result = map();
    while (!p().done()) {
      if (p().readIf(" "))
        continue;
      String res = "***";
      String key;
      try {
        int x = p().readInteger();
        res = "" + x;
        key = res;
      } catch (NumberFormatException e) {
        StringBuilder z = new StringBuilder();
        while (!p().done() && !p().peekIs(" "))
          z.append(p().readChar());
        key = z.toString();
      }
      result.put(key, res);
    }
    generateMessage(result);
    log(result);
    assertGenerated();
  }

  private StringParser p(String text) {
    if (mStringParser == null)
      mStringParser = new StringParser(text);
    return mStringParser;
  }

  private void auxReadPath(String t) {
    sb().append(p(t).readPath());
    generateMessage(sb());
    log(sb());
    assertGenerated();
  }

  private StringParser p() {
    checkState(mStringParser != null);
    return mStringParser;
  }

  private StringParser mStringParser;
  private StringBuilder msb;

  private StringBuilder sb() {
    if (msb == null)
      msb = new StringBuilder();
    return msb;
  }
}
