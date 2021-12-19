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
import js.json.JSMap;
import js.testutil.MyTestCase;

import java.io.File;
import static js.base.Tools.*;
import static org.junit.Assert.*;

public class BackupManagerTest extends MyTestCase {

  @Test
  public void createGeneratedCopy() {
    loadTools();
    workDirectory();
    assertGenerated();
  }

  @Test
  public void backupToDefault() {
    workDirectory();
    b().makeBackup(sampleFile());
    assertGenerated();
  }

  @Test
  public void multipleCopies() {
    workDirectory();
    for (int i = 0; i < 10; i++) {
      setCurrentTime(i * 1000);
      modify();
      b().makeBackup(sampleFile());
    }
    assertGenerated();
  }

  @Test
  public void sameTimestamp() {
    workDirectory();
    for (int i = 0; i < 3; i++) {
      setCurrentTime(i * 1000);
      if (i != 1)
        modify();
      b().makeBackup(sampleFile());
    }
    assertGenerated();
  }

  @Test
  public void multipleCopiesDir() {
    workDirectory();
    for (int i = 0; i < 10; i++) {
      setCurrentTime(i * 1000);
      modify();
      b().makeBackup(sampleDir());
    }
    assertGenerated();
  }

  @Test
  public void attemptBackupFileOutsideBaseDir() {
    workDirectory();
    File outsideFile = generatedFile("outside.txt");
    files().writeString(outsideFile, "hello");
    try {
      b().makeBackup(outsideFile);
    } catch (Throwable t) {
      log(t);
      assertTrue(t.getMessage().contains("file is not strictly"));
    }
  }

  @Test
  public void trimNumberOfBackups() { 
    workDirectory();
    for (int i = 0; i < 12; i++) {
      setCurrentTime(i * 1000);
      File f = new File(sampleDir(), "h.txt");
      files().writeString(f, "i=" + i);
      b().backupAndDelete(new File(workDirectory(), "source"));
    }
    assertGenerated();
  }

  private BackupManager b() {
    if (mBackupManager == null) {
      mBackupManager = new BackupManager(files(), generatedFile("work"));
      mBackupManager.setVerbose(verbose());
      setCurrentTime(0);
    }
    return mBackupManager;
  }

  private BackupManager mBackupManager;

  /**
   * Create a copy of the test data directory, one we can safely modify for the
   * test.
   * 
   * We will construct a directory named "work", and place the copied within it
   * as a subdirectory named "source". The manager can place its backups within
   * a sibling subdirectory named _SKIP_backups.
   */
  private File workDirectory() {
    if (mWork == null) {
      File source = testDataDir();
      File work = generatedFile("work");
      File gen = new File(work, "source");
      files().copyDirectory(source, gen);
      mWork = work;
    }
    return mWork;
  }

  private File sampleFile() {
    return new File(sampleDir(), "d.json");
  }

  private File sampleDir() {
    return new File(workDirectory(), "source/c");
  }

  private void modify() {
    File bf = sampleFile();
    if (!bf.exists())
      return;
    JSMap m = JSMap.from(bf);
    mVersion++;
    m.put("version", mVersion);
    files().writePretty(bf, m);
  }

  private void setCurrentTime(long offset) {
    final long START_TIME = 1607380000000L;
    b().setCurrentTime(START_TIME + offset);
  }

  private int mVersion;
  private File mWork;
}
