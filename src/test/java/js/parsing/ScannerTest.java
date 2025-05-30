package js.parsing;


import static js.base.Tools.*;

import js.base.BasePrinter;
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
       // var m = new JSMap("{\"final\":2,\"tokens\":[\"WS\",\"WORD\",\"SEMI\"],\"version\":4.0,\"states\":[[[9,11,12,14,32,33],4,[65,91,97,123],3,[59,60],1],[[-4,-3]],[],[[-3,-2],2,[65,91,97,123],3],[[9,11,12,14,32,33],4,[-2,-1]]]}");
        sDFA = DFA.parse("\"{\\\"final\\\":2,\\\"tokens\\\":[\\\"WS\\\",\\\"WORD\\\",\\\"SEMI\\\"],\\\"version\\\":4.0,\\\"states\\\":[[[9,11,12,14,32,33],4,[65,91,97,123],3,[59,60],1],[[-4,-3]],[],[[-3,-2],2,[65,91,97,123],3],[[9,11,12,14,32,33],4,[-2,-1]]]}");

      }
      mScanner = new Scanner(sDFA, text);
    }
    return mScanner;
  }

  private static DFA sDFA;
  private Scanner mScanner;

  @Test
  public void peekDistances() {
    if (alert("disabled")) return;
    var s = sc();
    var p = new BasePrinter();
    while (s.hasNext()) {
      for (int i = 0; i < 5; i++) {
        p.pr(i, ":", s.peek(i));
      }
      p.pr("read:", s.read());
    }
    generateMessage(p.toString());
  }

//  @Test
//  public void compareOldNewDFAParser() {
//    var sampleDfa = "{\"final\":2,\"tokens\":[\"CR\",\"COMMA\",\"VALUE\"],\"version\":4.0,\"states\":[[[33,34,35,44,45,92,93,128],10,[44,45],9,[34,35],5,[32,33],4,[13,14],3,[10,11],1],[[-2,-1]],[],[[10,11],1],[[33,34,35,44,45,92,93,128],10,[-4,-3],2,[34,35],5,[32,33],4],[[32,34,35,92,93,255],5,[92,93],7,[34,35],6],[[-4,-3],2,[32,33],6],[[32,34,35,92,93,255],5,[92,93],7,[34,35],8],[[-4,-3],2,[33,34,35,92,93,255],5,[92,93],7,[32,33],8,[34,35],6],[[-3,-2]],[[32,34,35,44,45,92,93,128],10,[-4,-3]]]}";
//    var dfa1 = DFA.parseDfaFromJson(sampleDfa);
//    var dfa2 = new DFA(new JSMap(sampleDfa));
//    var out1 = scanSampleText(dfa1);
//    var out2 = scanSampleText(dfa2);
//    checkArgument(out1.equals(out2));
//    assertMessage(out1 + "\n\n" + out2);
//  }

  private String scanSampleText(DFA dfa) {
    var p = new BasePrinter();
    var sampleText = "123,45,\"hello\",\n16";
    var sc = new Scanner(dfa, sampleText, -1);
    while (sc.hasNext()) {
      p.pr("next token:", sc.read());
    }
    return p.toString();
  }

}
