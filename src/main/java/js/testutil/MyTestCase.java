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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import js.base.*;
import js.data.AbstractData;
import js.data.DataUtil;
import js.file.DirWalk;
import js.file.FileException;
import js.file.Files;
import js.json.JSMap;
import js.json.JSObject;
import js.system.SystemUtil;

/**
 * Base class for our unit tests.
 * 
 * Will support regression testing, logging, etc.
 */
@RunWith(MyTestRunner.class)
public abstract class MyTestCase extends BaseObject implements LoggerInterface {

  public MyTestCase() {
    // When running a unit test that uses Java's BufferedImage class,
    // OSX adds an icon to the menu bar (temporarily);
    //
    // new BufferedImage(320,256,BufferedImage.TYPE_INT_ARGB);
    //
    // ...to prevent this, ensure we're in 'headless' mode
    //
    SystemUtil.setConsoleAppFlag(true);
  }

  @Before
  public void setup() {
    prepareExecutionContext();
    // We leave verbosity false unless there is a single unit test being invoked
    if (MyTestRunner.runCount() == 1)
      setVerbose();
    log("------------ setup");
    logger(this);
  }

  @After
  public void tearDown() throws Exception {
    mExecutionContext.shutdown();
    log("------------ tearDown");
  }

  @Rule
  public TestName mTestName = new TestName();

  // ------------------------------------------------------------------
  // BaseObject methods
  // ------------------------------------------------------------------

  @Override
  protected final String supplyName() {
    return mTestName.getMethodName();
  }

  // ------------------------------------------------------------------
  // Logging
  // ------------------------------------------------------------------

  public void println(String message) {
    System.out.println(message);
  }

  // ------------------------------------------------------------------
  // Random numbers
  // ------------------------------------------------------------------

  /**
   * Get random number generator; constructs if necessary
   */
  public final Random random() {
    if (mRandom == null)
      resetSeed(1942);
    return mRandom;
  }

  /**
   * Create a new random number generator, with a particular seed
   */
  public final Random resetSeed(int seed) {
    mRandom = new Random(seed);
    return random();
  }

  private Random mRandom;

  // ------------------------------------------------------------------
  // Regression test hash code manipulation (e.g. replace old with new)
  // ------------------------------------------------------------------

  /**
   * Replace any old hash code for this unit test with a new one, without
   * failing the test
   * 
   * Marked deprecated for easy deletion within IDE of calls to this method
   */
  @Deprecated
  public final MyTestCase rv() {
    alertWithSkip(1, "invalidating old hash");
    fileManager().invalidateOldHash();
    return this;
  }

  // ------------------------------------------------------------------
  // Execution context
  // ------------------------------------------------------------------

  private void prepareExecutionContext() {
    mExecutionContext = new ExecutionContext();
    mExecutionContext.setName(name());
  }

  public final ExecutionContext executionContext() {
    checkState(mExecutionContext != null, "no ExecutionContext defined");
    return mExecutionContext;
  }

  private ExecutionContext mExecutionContext;

  // ------------------------------------------------------------------
  // Unit test data directories
  // ------------------------------------------------------------------

  /**
   * Get name of directory containing data files; default uses derivation of
   * class name
   */
  public String testDataName() {
    String result = convertCamelToUnderscore(getClass().getSimpleName());
    result = chomp(result, "_test") + "_test_data";
    return result;
  }

  /**
   * Set the name of the directory for this unit test's data files.
   */
  public final void setTestDataName(String name) {
    mCachedTestDataName = name;
  }

  private String cachedTestDataName() {
    if (mCachedTestDataName == null)
      setTestDataName(testDataName());
    return mCachedTestDataName;
  }

  /**
   * Get the directory where test data files for this test case can be found
   */
  public final File testDataDir() {
    if (mCachedTestDataDir == null)
      mCachedTestDataDir = dataDir(cachedTestDataName()).getAbsoluteFile();
    return mCachedTestDataDir;
  }

  /**
   * Get a particular unit test file (or subdirectory) within the unit test data
   * directory
   */
  public final File testFile(String name) {
    return new File(testDataDir(), name);
  }

  /**
   * Get data directory name from cache, adding if necessary
   */
  private static File dataDir(String baseName) {
    File result = sCachedDataDirectoryMap.get(baseName);
    if (result == null) {
      result = Files.assertDirectoryExists(new File(sUnitTestDir, baseName), "data directory");
      sCachedDataDirectoryMap.put(baseName, result);
    }
    return result;
  }

  private static File sUnitTestDir = new File("unit_test");

  private static Map<String, File> sCachedDataDirectoryMap = concurrentHashMap();

  private String mCachedTestDataName;
  private File mCachedTestDataDir;

  // ------------------------------------------------------------------
  // Generated directory
  // ------------------------------------------------------------------

  public final void setGeneratedDir(File dir) {
    fileManager().setGeneratedDir(dir);
  }

  protected FileManager fileManager() {
    if (mFileManager == null)
      mFileManager = new FileManager(this);
    return mFileManager;
  }

  private FileManager mFileManager;

  /**
   * Get, creating if necessary, directory for test's generated files
   */
  public final File generatedDir() {
    return fileManager().generatedDir();
  }

  /**
   * Construct generated File relative to generatedDir()
   */
  public final File generatedFile(String relativePath) {
    return new File(generatedDir(), relativePath);
  }

  /**
   * Convert an object to a string, and write the string to "message.txt" within
   * the generated directory
   */
  public final void generateMessage(Object messageObject) {
    String text = messageObject.toString();
    if (verbose()) {
      String q = insertLeftRightMargins(text);
      log(CR, "Content:", INDENT, q);
    }
    generateMessage(GENERATED_MESSAGE_NAME, text);
  }

  public final void generateMessage(String filename, Object messageObject) {
    sysFiles().writeString(generatedFile(filename), messageObject.toString());
  }

  private static final String GENERATED_MESSAGE_NAME = "message.txt";

  // ------------------------------------------------------------------
  // Assertions involving generated files' content
  // ------------------------------------------------------------------

  /**
   * Construct hash of generated directory, and verify it has the expected value
   */
  public final void assertGenerated() {
    fileManager().assertGeneratedDirectoryHash();
  }

  public final void assertMessage(Object messageObject) {
    generateMessage(messageObject.toString());
    assertGenerated();
  }

  public final void assertMessage(JSObject messageObject) {
    assertMessage(messageObject.prettyPrint());
  }

  public final void assertMessage(AbstractData messageObject) {
    assertMessage(messageObject.toJson());
  }

  public final void assertDirectoryHash() {
    if (verbose())
      saveHuman();
    assertDirectoryHash(generatedDir());
  }

  public void ignoreFilenames(String... filenames) {
    if (mFilenamesToIgnore == null)
      mFilenamesToIgnore = arrayList();
    for (String x : filenames)
      mFilenamesToIgnore.add(x);
  }

  /**
   * Pretty-print an object if it's a json map or list; otherwise, call its
   * toString() method
   */
  private static String prettyPrint(Object value) {
    String string;
    if (value instanceof JSObject)
      string = ((JSObject) value).prettyPrint();
    else
      string = value.toString();
    // Wicket's StringValue toString() can return null, which is STUPID
    if (string == null)
      string = "<null>";
    return string;
  }

  public final void assertHash(Object object) {
    calculateHash(object);

    HashCodeRegistry registry = HashCodeRegistry.registryFor(this);
    if (registry.verifyHash(name(), mHashReceived, false)) {
      return;
    }

    if (mObjectDescr == null)
      mObjectDescr = prettyPrint(object);

    //    errMsg().pr(//
    //        "Unexpected hash value:", mHashReceived, "for object:", CR, //
    //        mObjectDescr, CR, //
    //        "Unexpected hash value:", mHashReceived, CR);
    fail(BasePrinter.toString("Unexpected hash value:", mHashReceived, "for object:", CR, //
        mObjectDescr, CR, //
        "Unexpected hash value:", mHashReceived, CR));

    //    "\n" + errMsg().toString());
  }

  public final void assertDirectoryHash(File directory) {
    deleteScheduledTestFiles();
    JSMap dirSummary = MyTestUtils.dirSummary(directory, mFilenamesToIgnore);
    mFilenamesToIgnore = null;
    try {
      assertHash(dirSummary);
    } catch (Throwable t) {
      showDiffs(directory, verbose());
      throw t;
    }

    // Create a copy of the directory, if one doesn't exist, to serve as a reference
    // so we can easily see what the expected output should have been if there's an error 
    // at some future point.

    // If reference directory already exists, delete it if its cached hash code
    // disagrees with what we expected.

    writeReferenceCopy(directory, mHashReceived);
  }

  /**
   * Get the set of filenames to be preserved when performing saveHuman(...)
   */
  public Set<String> retainFilenamesSet() {
    return mPreservedFilenamesSet;
  }

  private Set<String> mPreservedFilenamesSet = hashSet();

  /**
   * Save a copy of the generated directory to the desktop, WITHOUT deleting any
   * existing directories in the destination.
   *
   * At the start of a unit test, any old generated directory gets deleted, so
   * it disappears from the Finder's "bookmark" section, which is annoying.
   * 
   * For convenience, this method copies the generated directory to the desktop
   * (without deleting any existing one), so we can view it in Finder.
   * 
   * It won't delete any existing directories, but will delete any existing
   * files (that were not already copied over).
   */
  public void saveHuman() {
    File sourceDir = generatedDir();

    // Determine target directory; a subdirectory of the Desktop for easier maintenance
    File humanCopy;
    {
      String className = sourceDir.getParentFile().getName();

      // Remove some extraneous suffixes
      className = chomp(className, "Tests");
      className = chomp(className, "Test");

      humanCopy = Files.getDesktopFile("ub_inspections/" + className + "_" + sourceDir.getName());
    }
    sysFiles().mkdirs(humanCopy);

    Set<String> filesCopied = hashSet();
    Set<String> subdirSet = hashSet();
    DirWalk w = new DirWalk(sourceDir).withRecurse(true);
    for (File f : w.filesRelative()) {
      String dirs = nullToEmpty(f.getParent());
      if (subdirSet.add(dirs)) {
        File newDir = new File(humanCopy, dirs);
        sysFiles().mkdirs(newDir);
      }
      String relPath = f.getPath();
      filesCopied.add(relPath);
      try {
        sysFiles().copyFile(w.abs(f), new File(humanCopy, relPath));
      } catch (FileException e) {
        // A background task (e.g. dashboard cache maintenance) may be deleting files while we're copying them
        if (!(e.getCause() instanceof FileNotFoundException)) {
          throw e;
        }
        alert("File(s) disappeared within saveHuman()");
      }
    }

    // Delete any old files that weren't just updated
    w = new DirWalk(humanCopy).withRecurse(true);
    for (File f : w.filesRelative()) {
      String relPath = f.getPath();
      if (!filesCopied.contains(relPath)) {
        if (mPreservedFilenamesSet.contains(f.getName()))
          continue;
        alert("Deleted stale file(s) within saveHuman()");
        sysFiles().deleteFile(w.abs(f));
      }
    }
  }

  /**
   * Schedule deletion of some generated files or directories, to be done before
   * hashes are calculated
   */
  public final void omitTestFiles(String... fileOrDirectoryNames) {
    for (String filename : fileOrDirectoryNames)
      mFilesToDelete.add(filename);
  }

  public final File omitTestFile(File fileOrDirectory) {
    File file = fileOrDirectory;
    if (file.isAbsolute()) {
      File relPath = Files.fileRelativeToDirectory(file, generatedDir());
      if (relPath.isAbsolute())
        throw die("omitted file is not in generated dir:", file);
      file = relPath;
    }
    mFilesToDelete.add(file.toString());
    return fileOrDirectory;
  }

  public final void omitTestFiles(File... fileOrDirectory) {
    for (File file : fileOrDirectory)
      omitTestFile(file);
  }

  private void deleteScheduledTestFiles() {
    for (String filename : mFilesToDelete) {
      File file = generatedFile(filename);
      if (file.isDirectory())
        Files.S.deleteDirectory(file);
      else
        Files.S.deleteFile(file);
    }
    mFilesToDelete.clear();
  }

  private List<String> mFilesToDelete = arrayList();

  private List<String> mFilenamesToIgnore;

  private static String HASHCODE_FILENAME = "_hashcode_.json";

  private void writeReferenceCopy(File directory, int expectedHash) {
    File refDir = referenceDirectory(directory);
    File hashCodeFile = new File(refDir, HASHCODE_FILENAME);

    if (!hashCodeFile.exists() || JSMap.from(hashCodeFile).getInt("hashcode") != expectedHash
        || expectedHash < 0) {
      sysFiles().deleteDirectory(refDir);
      sysFiles().copyDirectory(directory, refDir);
      sysFiles().writeString(hashCodeFile, map().put("hashcode", expectedHash).toString());
    }
  }

  private static File referenceDirectory(File sourceDirectory) {
    checkArgument(sourceDirectory.isAbsolute());
    return new File(sourceDirectory.getParentFile(), sourceDirectory.getName() + "_REF");
  }

  private static final Set<String> sTextFileExtensions = hashSetWith(Files.EXT_TEXT, Files.EXT_JSON);

  public static void showDiffs(File directory, boolean extended) {
    Set<File> relFiles = hashSet();

    File referenceDirectory = referenceDirectory(directory);
    if (referenceDirectory.exists()) {
      DirWalk dirWalk = new DirWalk(referenceDirectory).withRecurse(true);
      relFiles.addAll(dirWalk.filesRelative());
    }
    DirWalk dirWalk = new DirWalk(directory).withRecurse(true);
    relFiles.addAll(dirWalk.filesRelative());
    relFiles.remove(new File(HASHCODE_FILENAME));

    List<File> sortedFiles = arrayList();
    sortedFiles.addAll(relFiles);
    sortedFiles.sort(null);

    for (File fileReceived : sortedFiles) {
      File fileRecAbs = dirWalk.abs(fileReceived);
      File fileRefAbs = new File(referenceDirectory, fileReceived.getPath());

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

      // If it looks like a text file, call the 'diff' utility to display differences
      //
      String ext = Files.getExtension(fileReceived);
      if (!sTextFileExtensions.contains(ext)) {
        pr("...files differ");
        if (extended) {
          SystemCall sc = new SystemCall();
          sc.withVerbose(true);
          sc.arg("diff", fileRefAbs, fileRecAbs);
          sc.assertSuccess();
        }
        continue;
      }

      SystemCall sc = new SystemCall();
      sc.directory(directory);
      sc.arg("diff", fileRecAbs, fileRefAbs);
      sc.arg("-c");
      pr();
      String result = sc.systemOut();
      pr(result);
    }
  }

  private String mObjectDescr;
  private int mHashReceived;

  private void calculateHash(Object object) {
    if (object instanceof File) {
      File file = (File) object;
      mObjectDescr = file.getName();
      byte[] fileContents = Files.toByteArray(file, null);
      object = DataUtil.hashOf(fileContents);
    }

    int oldHashReceived = object.hashCode();
    // Curiously, a hash code of zero is sometimes being generated
    if (oldHashReceived == 0)
      oldHashReceived = 999;
    int hashReceived = (oldHashReceived & 0xffff) % 9000 + 1000;
    mHashReceived = hashReceived;
  }

  // ------------------------------------------------------------------
  // Redirecting System.out, System.err
  // ------------------------------------------------------------------

  private boolean isSystemOutRedirected() {
    return mOriginalSystemOut != null;
  }

  private void clearSystemOutRedirection() {
    mOriginalSystemOut = null;
    mRedirectedGenFile = null;
    mRedirectedOutputStream = null;
  }

  /**
   * Redirect System.out to a generated file
   */
  public final void redirectSystemOut() {
    checkState(!isSystemOutRedirected(), "System.out already redirected");
    mOriginalSystemOut = System.out;

    // The client code may delete the entire generated directory 
    // (i.e., 'zap' output dir); so temporarily redirect to a file that is elsewhere

    try {
      mRedirectedGenFile = File.createTempFile(name() + "-", null);
    } catch (Throwable e) {
      throw Files.asFileException(e);
    }
    mRedirectedGenFile.deleteOnExit();
    mRedirectedOutputStream = Files.S.outputStream(mRedirectedGenFile);
    System.setOut(new PrintStream(mRedirectedOutputStream));
  }

  /**
   * Redirect System.out to a generated file, unless verbosity is true
   */
  public final void quietSystemOut() {
    if (!verbose())
      redirectSystemOut();
  }

  public final void assertSystemOut() {
    try {
      String message = restoreSystemOut();
      if (verbose())
        log(message);
      assertGenerated();
    } finally {
      clearSystemOutRedirection();
    }
  }

  /**
   * Restore System.out, and return the redirected output's content. It is also
   * written to the generated file 'message.txt'
   */
  public String restoreSystemOut() {
    checkState(isSystemOutRedirected(), "System.out wasn't redirected");

    String redirectedContent;
    try {
      System.out.flush();
      System.setOut(mOriginalSystemOut);

      // Clear the 'systemOutRedirected' status by discarding the original System.out:
      mOriginalSystemOut = null;

      mRedirectedOutputStream.close();

      redirectedContent = Files.readString(mRedirectedGenFile);
      // Move the redirected file to the generated directory, now that we've got its content;
      // if client code deletes it, that's ok
      File destination = generatedFile(GENERATED_MESSAGE_NAME);
      Files.S.moveFile(mRedirectedGenFile, destination);

      mRedirectedGenFile = destination;
    } catch (IOException e) {
      throw Files.asFileException(e);
    }
    return redirectedContent;
  }

  public final Files files() {
    if (mFiles == null) {
      mFiles = new Files();
      mFiles.setVerbose(verbose());
    }
    return mFiles;
  }

  private Files sysFiles() {
    return Files.S;
  }

  private Files mFiles;
  private PrintStream mOriginalSystemOut;
  private File mRedirectedGenFile;
  private FileOutputStream mRedirectedOutputStream;

  protected TimeManager timeManager() {
    return mExecutionContext.getTimeManager();
  }

}
