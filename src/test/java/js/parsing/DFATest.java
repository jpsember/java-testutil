package js.parsing;

import static js.base.Tools.*;

import js.json.JSUtils;
import org.junit.Test;

import js.testutil.MyTestCase;


public class DFATest extends MyTestCase {

  @Test
  public void describe() {
    loadTools();
    var dfa = JSUtils.JSON_DFA;
    generateMessage(dfa.describe().prettyPrint());
  }

}
