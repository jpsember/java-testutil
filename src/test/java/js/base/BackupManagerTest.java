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

import org.junit.Test;

import js.file.BackupManager;
import js.file.DirWalk;
import js.json.JSMap;
import js.testutil.MyTestCase;

import java.io.File;
import static js.base.Tools.*;

public class BackupManagerTest extends MyTestCase {

  @Test
  public void createGeneratedCopy() {
    loadTools();
    workDirectory();
    assertGenerated();
  }

  @Test
  public void backupToDefault() {
    if (alert("disabled test"))
      return;
    workDirectory();
    b().makeBackup(sampleFile());
    assertGenerated();
  }

  @Test
  public void multipleCopies() {
    if (alert("disabled test"))
      return;
    workDirectory();
    for (int i = 0; i < 10; i++) {
      modify();
      b().makeBackup(sampleFile());
    }
    assertGenerated();
  }

  @Test
  public void sameTimestamp() {
    if (alert("disabled test"))
      return;
    workDirectory();
    for (int i = 0; i < 3; i++) {
      if (i != 1)
        modify();
      b().makeBackup(sampleFile());
    }
    assertGenerated();
  }

  @Test
  public void multipleCopiesDir() {
    if (alert("disabled test"))
      return;
    workDirectory();
    for (int i = 0; i < 10; i++) {
      modify();
      b().makeBackup(sampleDir());
    }
    assertGenerated();
  }

  @Test
  public void deletePreserving() {
    if (alert("disabled test"))
      return;
    workDirectory();
    File f = new File(b().getSourceRootDirectory(), "c/h.txt");
    files().writeString(f, "hello");
    setLastModified(f, 3000);
    b().backupAndDelete(sampleDir(), "h.txt");
    assertGenerated();
  }

  private BackupManager b() {
    if (mBackupManager == null) {
      mBackupManager = new BackupManager(files(), generatedFile("source"));
      mBackupManager.setVerbose(verbose());
    }
    return mBackupManager;
  }

  private BackupManager mBackupManager;

  private File workDirectory() {
    if (mWork == null) {
      File source = testDataDir();
      File gen = generatedFile("source");
      files().copyDirectory(source, gen);
      // Set the last modified time for all test files to a fixed value
      for (File f : new DirWalk(gen).files()) {
        setLastModified(f, 0);
      }
      mWork = gen;
    }
    return mWork;
  }

  private File sampleFile() {
    return new File(sampleDir(), "d.json");
  }

  private File sampleDir() {
    return new File("c");
  }

  private void modify() {
    File bf = new File(b().getSourceRootDirectory(), sampleFile().toString());
    if (!bf.exists())
      return;
    JSMap m = JSMap.from(bf);
    mVersion++;
    m.put("version", mVersion);
    files().writePretty(bf, m);
    // Artifically set the modified time two full seconds ahead from its old position
    setLastModified(bf, 2000 * mVersion);
  }

  private void setLastModified(File f, long offset) {
    final long START_TIME = 1607380000000L;
    f.setLastModified(START_TIME + offset);
  }

  private int mVersion;
  private File mWork;
}
