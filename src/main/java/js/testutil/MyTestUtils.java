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

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import js.base.DateTimeTools;
import js.data.DataUtil;
import js.file.Files;
import js.json.JSMap;
import junit.framework.TestCase;
import static js.base.Tools.*;

/**
 * Various utility methods to help writing unit tests
 */
public final class MyTestUtils {

  /**
   * Generate a list of distinct random positive integers 0 <= n < upperBound
   */
  public static int[] randomIntegers(Random random, int count, int upperBound) {
    Set<Integer> idSet = hashSet();
    int[] values = new int[count];
    for (int i = 0; i < count; i++) {
      int id;
      while (true) {
        id = random.nextInt(upperBound);
        if (idSet.add(id))
          break;
      }
      values[i] = id;
    }
    return values;
  }

  public static String randomText(Random random, int numWords) {
    StringBuilder sb = new StringBuilder();
    int words = Math.max(1, random.nextInt(numWords) + (numWords / 15));
    for (int i = 0; i < words; i++) {
      if (i != 0)
        sb.append(' ');
      sb.append(sRandomText.get(random.nextInt(sRandomText.size())));
    }
    return sb.toString();
  }

  // Courtesy of hipsum.co
  //
  private static List<String> sRandomText = split(
      "I'm baby normcore sustainable gluten-free post-ironic fixie tousled, "
          + "whatever vaporware seitan tilde cornhole food truck venmo. Crucifix "
          + "banjo organic pop-up cold-pressed neutra selfies. Direct trade waistcoat "
          + "artisan hell of mustache bicycle rights neutra man braid tumblr selfies "
          + "gentrify small batch hot chicken. Stumptown fixie woke tote bag subway "
          + "tile truffaut street art fanny pack meggings venmo.",
      ' ');

  /**
   * Verify that a regular expression matches a sequence of strings
   * 
   * @param patternText
   *          pattern of regex
   * @param text
   *          zero or more strings to match; each can be prefixed with "!",
   *          which indicates no match should occur
   */
  public static void verifyRegEx(String patternText, String... textList) {

    Pattern pattern = Pattern.compile(patternText);
    StringBuilder result = null;

    for (String text : textList) {
      boolean expectMatch = true;
      if (text.startsWith("!")) {
        expectMatch = false;
        text = chompPrefix(text, "!");
      }
      Matcher match = pattern.matcher(text);
      boolean found = match.find();
      if (found != expectMatch) {
        if (result == null)
          result = new StringBuilder("\n\n*** RegEx failure, pattern '" + pattern.pattern() + "':\n");
        result.append(" --> " + found + " '" + text + "'\n");
      }
    }

    if (result != null) {
      result.append("\n\n");
      pr(result);
      TestCase.fail(result.toString());
    }
  }

  /**
   * Figure out where a snapshot should be written. We have to figure out where
   * the test resources folder is (or should be); it must lie in the SOURCE
   * directory, not the BUILD (or 'target') directory
   * 
   * @return directory where snapshot should be placed
   */
  public static File inferTestResourceLocation(TestCase testCase) {
    StringBuilder sb = new StringBuilder("src/test/resources");
    String packageName = testCase.getClass().getPackage().getName();
    List<String> components = split(packageName, '.');
    for (String comp : components)
      sb.append("/" + comp);
    return Files.S.mkdirs(new File(sb.toString()));
  }

  /**
   * Generate an array of all permutations of integers 0...n-1
   */
  public static List<int[]> generatePermutations(int n) {
    List<int[]> output = arrayList();

    int[] array = new int[n];
    for (int i = 0; i < n; i++)
      array[i] = i;
    int[] c = new int[n];

    output.add(array.clone());
    int i = 1;
    while (i < n) {
      if (c[i] < i) {
        if ((i & 1) == 0)
          swap(array, 0, i);
        else
          swap(array, c[i], i);
        output.add(array.clone());
        c[i]++;
        i = 1;
      } else {
        c[i] = 0;
        i++;
      }
    }
    return output;
  }

  private static void swap(int[] array, int i, int j) {
    int tmp = array[i];
    array[i] = array[j];
    array[j] = tmp;
  }

  private static List<String> sFilenamesToIgnore = arrayList(".DS_Store", ".gitignore");

  /**
   * Get a JSMap representing a directory tree
   */
  public static JSMap dirSummary(File dir) {
    return dirSummary(dir, null);
  }

  public static JSMap dirSummary(File dir, List<String> optFilenamesToIgnore) {
    return dirSummary(dir, optFilenamesToIgnore, true);
  }

  public static JSMap dirSummary(File dir, List<String> optFilenamesToIgnore, boolean calculateFileHashes) {
    Set<String> ignored = hashSet();
    ignored.addAll(sFilenamesToIgnore);
    if (optFilenamesToIgnore != null)
      ignored.addAll(optFilenamesToIgnore);
    return auxDirSummary(dir, ignored, calculateFileHashes);
  }

  private static final Comparator<File> COMPARATOR = (File a, File b) -> a.getPath().compareTo(b.getPath());

  private static List<File> files(File directory) {
    List<File> files = arrayList();
    File[] auxFiles = directory.listFiles();
    if (auxFiles != null) {
      for (File name : auxFiles)
        files.add(name);
      files.sort(COMPARATOR);
    }
    return files;
  }

  private static JSMap auxDirSummary(File dir, Set<String> ignored, boolean calculateFileHashes) {
    List<File> files = files(dir);
    JSMap m = map();
    for (File f : files) {
      String s = f.getName();
      if (ignored.contains(s)) {
        continue;
      }
      Object value = "?";
      if (f.isDirectory()) {
        JSMap subdirSummary = auxDirSummary(f, ignored, calculateFileHashes);
        if (subdirSummary.isEmpty())
          continue;
        value = subdirSummary;
      } else if (calculateFileHashes) {
        value = Files.tryHash(f);
        if (value == null)
          value = DataUtil.checksum(f);
      }
      m.putUnsafe(s, value);
    }
    return m;
  }

  public static File unitTestDirectory() {
    return sUnitTestDirectory;
  }

  private static File sUnitTestDirectory = new File("unit_test");

  private static void installHashFunctions() {
    Files.registerFiletypeHashFn("txt", (f) -> Files.readString(f));
    Files.registerFiletypeHashFn(Files.EXT_JSON, (f) -> {
      return JSMap.from(f);
    });
    Files.registerFiletypeHashFn(Files.EXT_ZIP, (f) -> calcHashForZip(f));
    // Attempt to load classes (which may not be available) so they can install 
    // additional hash functions
    try {
      Class.forName("js.graphics.ImgUtil");
    } catch (ClassNotFoundException e) {
    }
  }

  static {
    installHashFunctions();
  }

  /**
   * Wait for a boolean flag to become true
   */
  public static void waitFor(long maxWaitMs, BooleanSupplier supplier) {
    long startTime = DateTimeTools.getRealMs();
    while (!supplier.getAsBoolean()) {
      long currentTime = DateTimeTools.getRealMs();
      long elapsed = currentTime - startTime;
      if (elapsed > maxWaitMs)
        throw badState("waitFor expired after waiting:", DateTimeTools.humanDuration(elapsed));
      DateTimeTools.sleepForRealMs(DateTimeTools.MILLISECONDS(50));
    }
  }

  /**
   * Wait for a boolean flag to become true; waits a maximum of two seconds
   */
  public static void waitFor(BooleanSupplier supplier) {
    waitFor(2000, supplier);
  }

  private static int calcHashForZip(File zipFile) {
    JSMap m = map();
    m.put("", zipFile.getName());
    ZipFile zipFileObj = Files.zipFile(zipFile);
    for (ZipEntry entry : Files.getZipEntries(zipFileObj))
      m.put(entry.getName(), entry.getCrc());
    return m.hashCode();
  }

}
