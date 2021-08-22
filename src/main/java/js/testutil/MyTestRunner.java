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
package js.testutil;

import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Our own TestRunner class to support some custom test behavior
 */
public class MyTestRunner extends BlockJUnit4ClassRunner {

  public MyTestRunner(Class<?> theClass) throws InitializationError {
    super(theClass);
  }

  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
    loadTools();
    int runCount = 0;
    List<FrameworkMethod> children = new ArrayList<FrameworkMethod>(getChildren());
    for (Iterator<FrameworkMethod> iter = children.iterator(); iter.hasNext();) {
      FrameworkMethod each = iter.next();
      if (filter.shouldRun(describeChild(each)))
        runCount++;
    }
    adjustRunCount(runCount);
    super.filter(filter);
  }

  public static int runCount() {
    return sRunCount;
  }

  private void adjustRunCount(int amount) {
    synchronized (MyTestRunner.class) {
      sRunCount += amount;
    }
  }

  private static volatile int sRunCount;

}
