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
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import js.base.BasePrinter;
import js.base.SystemCall;
import js.file.DirWalk;
import js.file.Files;
import js.json.JSMap;

/**
 * Manages unit test data files and generated output
 */
final class FileManager {

  public static final File UNIT_TEST_DIRECTORY = new File("unit_test");

  public FileManager(MyTestCase unitTest) {
    mUnitTest = unitTest;
  }

  public File generatedDir() {
    if (mGeneratedDir == null) {
      File unitTestDir = UNIT_TEST_DIRECTORY.getAbsoluteFile();

      // If no .gitignore file exists, create one (creating the directory as well if necessary);
      // it will have the entry GENERATED_DIR_NAME

      final String GENERATED_DIR_NAME = "generated";

      File gitIgnoreFile = new File(unitTestDir, ".gitignore");
      if (!gitIgnoreFile.exists()) {
        Files.S.mkdirs(unitTestDir);
        Files.S.writeString(gitIgnoreFile, GENERATED_DIR_NAME + "\n");
      }
      File projectDir = new File(unitTestDir, GENERATED_DIR_NAME);
      String className = chomp(mUnitTest.getClass().getSimpleName(), "Test");
      String testName = chomp(mUnitTest.name(), "Test");
      mGeneratedDir = new File(projectDir, className + "/" + testName);
      Files.S.remakeDirs(mGeneratedDir);
    }
    return mGeneratedDir;
  }

  /**
   * Replace any old hash for current unit test with the value we end up with
   */
  public void invalidateOldHash() {
    mInvalidateOldHash = true;
  }

  /**
   * Construct hash of generated directory, and verify it has the expected value
   */
  public void assertGeneratedDirectoryHash() {
    if (mUnitTest.verbose())
      createInspectionDir();
    try {
      JSMap jsonMap = MyTestUtils.dirSummary(generatedDir());
      // Convert hash code to one using exactly four digits
      int currentHash = (jsonMap.hashCode() & 0xffff) % 9000 + 1000;
      HashCodeRegistry registry = HashCodeRegistry.registryFor(mUnitTest);
      if (!registry.verifyHash(mUnitTest.name(), currentHash, mInvalidateOldHash)) {
        fail(BasePrinter.toString("\nUnexpected hash value for directory contents:", CR, DASHES, CR, //
            jsonMap, CR, DASHES, CR));
      }
    } catch (Throwable t) {
      showDiffs();
      throw t;
    }
    saveTestResults();
  }

  /**
   * Display diff of generated directory and its reference version
   */
  private void showDiffs() {

    File refDir = referenceDir();
    if (!refDir.exists())
      return;

    Set<File> relFiles = hashSet();

    DirWalk dirWalk = new DirWalk(refDir).withRecurse(true).omitNames(".DS_Store");
    relFiles.addAll(dirWalk.filesRelative());
    dirWalk = new DirWalk(generatedDir()).withRecurse(true).omitNames(".DS_Store");
    relFiles.addAll(dirWalk.filesRelative());

    for (File fileReceived : relFiles) {
      File fileRecAbs = dirWalk.abs(fileReceived);
      File fileRefAbs = new File(refDir, fileReceived.getPath());

      if (fileRefAbs.exists() && fileRecAbs.exists()
          && Arrays.equals(Files.toByteArray(fileRecAbs, null), Files.toByteArray(fileRefAbs, null)))
        continue;

      pr(CR,
          "------------------------------------------------------------------------------------------------");
      pr(fileReceived);

      if (!fileRefAbs.exists()) {
        pr("...unexpected file");
        continue;
      }
      if (!fileRecAbs.exists()) {
        pr("...file has disappeared");
        continue;
      }

      // If it looks like a text file, call the 'diff' utility to display differences.
      // Otherwise, only do this (using binary mode) if in verbose mode
      //
      String ext = Files.getExtension(fileReceived);

      boolean isTextFile = sTextFileExtensions.contains(ext);
      if (!mUnitTest.verbose())
        continue;

      SystemCall sc = new SystemCall().arg("diff");
      if (isTextFile)
        sc.arg("--text"); // "Treat all files as text."

      if (true) {
        sc.arg("-C", "2");
      } else {
        sc.arg("--side-by-side"); //  "Output in two columns."
      }
      sc.arg(fileRefAbs, fileRecAbs);
      pr();
      pr(sc.systemOut());
      // It is returning 2 if it encounters binary files (e.g. xxx.zip), which is problematic
      if (sc.exitCode() > 2)
        badState("System call failed:", INDENT, sc);
    }
  }

  private static final Set<String> sTextFileExtensions = hashSetWith(Files.EXT_TEXT, Files.EXT_JSON, "java",
      "dat");

  /**
   * Called when the generated directory's hash has been successfully verified.
   * 
   * 1) If a 'reference' copy of the directory doesn't exist, move generated
   * directory as it; otherwise, delete the generated directory (since it is the
   * same as the reference copy)
   * 
   * 2) Update the hash code of the directory, if it differs from the previous
   * value (or no previous value exists).
   */
  private void saveTestResults() {
    // If we're going to replace the hash in any case, delete any existing reference directory,
    // since its old contents may correspond to an older hash code
    if (mInvalidateOldHash)
      Files.S.deleteDirectory(referenceDir());

    if (!referenceDir().exists())
      Files.S.moveDirectory(generatedDir(), referenceDir());
    else
      Files.S.deleteDirectory(generatedDir());
  }

  /**
   * Save a copy of the generated directory to a subdirectory of the user's
   * Desktop, for convenience
   */
  private void createInspectionDir() {
    File destination = Files.getDesktopFile("unit_test_inspections/" + mUnitTest.name());
    Files.S.mkdirs(destination);
    Files.S.copyDirectory(generatedDir(), destination);
  }

  private File referenceDir() {
    if (mReferenceDir == null)
      mReferenceDir = new File(Files.parent(generatedDir()), generatedDir().getName() + "_REF");
    return mReferenceDir;
  }

  private final MyTestCase mUnitTest;
  private boolean mInvalidateOldHash;
  private File mGeneratedDir;
  private File mReferenceDir;
}
