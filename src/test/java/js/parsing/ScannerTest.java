package js.parsing;


import static js.base.Tools.*;

import js.base.BasePrinter;
import js.file.Files;
import org.junit.Test;

import js.testutil.MyTestCase;
import js.json.JSMap;

public class ScannerTest extends MyTestCase {

  public Scanner sc() {
    return sc("alpha bravo; charlie; delta echo foxtrot;");
  }

  public Scanner sc(String text) {
    if (mScanner == null) {
      if (sDFA == null) {
        var m = new JSMap("{\"final\":2,\"tokens\":[\"WS\",\"WORD\",\"SEMI\"],\"version\":4.0,\"states\":[[[9,11,12,14,32,33],4,[65,91,97,123],3,[59,60],1],[[-4,-3]],[],[[-3,-2],2,[65,91,97,123],3],[[9,11,12,14,32,33],4,[-2,-1]]]}");
        sDFA = new DFA(m);
      }
      mScanner = new Scanner(sDFA, text);
    }
    return mScanner;
  }

  private static DFA sDFA;
  private Scanner mScanner;

  @Test
  public void peekDistances() {
    var s = sc();
    var p = new BasePrinter();
    while (s.hasNext()) {
      for (int i = 0; i < 5; i++) {
        p.pr(i, ":", s.peek(i));
      }
      p.pr("read:", s.read());
    }
    generateMessage(p.toString());
//    assertMessage(p.toString());
  }

}
