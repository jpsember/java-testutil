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

import static js.base.Tools.*;

import java.io.File;
import java.util.Map;

import js.file.Files;
import js.base.BaseObject;
import js.json.JSMap;

/**
 * Records the hash codes associated with a particular unit test class's tests,
 * and saves them
 */
final class HashCodeRegistry extends BaseObject {

  /**
   * Get registry for a test case, constructing one if necessary; must be thread
   * safe
   */
  public static HashCodeRegistry registryFor(MyTestCase testCase) {
    String key = testCase.getClass().getSimpleName();
    HashCodeRegistry registry = sClassesMap.get(key);
    if (registry == null) {
      synchronized (HashCodeRegistry.class) {
        registry = new HashCodeRegistry(key);
        sClassesMap.put(key, registry);
      }
    }
    return registry;
  }

  private static Map<String, HashCodeRegistry> sClassesMap = concurrentHashMap();

  // ------------------------------------------------------------------

  private HashCodeRegistry(String key) {
    mKey = key;
    mFile = new File(FileManager.UNIT_TEST_DIRECTORY, mKey.replace('.', '_') + ".json");
    mMap = JSMap.fromFileIfExists(mFile);
  }

  @Override
  public JSMap toJson() {
    return map().put("key", mKey).put("map", mMap);
  }

  @Override
  protected String supplyName() {
    return mKey;
  }

  public boolean verifyHash(String unitTestName, int hash, boolean replaceExistingHash) {
    int expectedHash = mMap.opt(unitTestName, -1);

    log("verifyHash", unitTestName, "expected:", expectedHash, "got:", hash);

    if (expectedHash < 0 || replaceExistingHash) {
      synchronized (mMap) {
        pr("Updating unit test hash", mKey + "." + unitTestName, "=>", hash);
        mMap.put(unitTestName, hash);
        write();
        expectedHash = hash;
      }
    }
    return expectedHash == hash;
  }

  private void write() {
    log("write registry", INDENT, mMap);
    File dir = Files.parent(mFile);
    if (!dir.exists())
      Files.S.mkdirs(dir);
    Files.S.writeString(mFile, mMap.prettyPrint());
  }

  private final String mKey;
  private final File mFile;
  private final JSMap mMap;
}
