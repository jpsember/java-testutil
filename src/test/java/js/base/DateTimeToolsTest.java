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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

import org.junit.Test;

import js.json.JSMap;
import js.testutil.MyTestCase;

import static js.base.Tools.*;
import static org.junit.Assert.*;
import static js.base.DateTimeTools.*;

public class DateTimeToolsTest extends MyTestCase {

  private static final long MAY19_TIME = 1463680865000L; // 11:01 AM on Thursday, May 19

  // see http://www.epochconverter.com/
  // or  http://www.freeformatter.com/epoch-timestamp-to-date-converter.html

  // ------------------------------------------------------------------
  // Epoch time
  // ------------------------------------------------------------------

  @Test
  public void sane() {
    assertFalse(DateTimeTools.isSane(575412887000L));
    assertTrue(DateTimeTools.isSane(1616792087000L));
    assertFalse(DateTimeTools.isSane(1900788887000L));
  }

  // ------------------------------------------------------------------
  // ZonedDateTime conversion
  // ------------------------------------------------------------------

  @Test
  public void zonedDateTimeToEpochMs() {
    ZonedDateTime zt = zonedDateTime(MAY19_TIME);
    assertEquals(MAY19_TIME, millis(zt));
  }

  // ------------------------------------------------------------------
  // Time zones
  // ------------------------------------------------------------------

  @Test
  public void TimeZones() {
    Set<String> ids = ZoneId.getAvailableZoneIds();
    assertTrue(ids.contains("Canada/Pacific"));
  }

  // ------------------------------------------------------------------
  // Formatting
  // ------------------------------------------------------------------

  @Test
  public void humanTimeStrings() {
    if (true) { // The output of this test varies with the timezone of the machine it's running on
      return;
    }
    JSMap m = map();
    m.putNumbered(DateTimeTools.humanTimeString(MAY19_TIME));
    m.putNumbered(DateTimeTools.humanTimeString(MAY19_TIME + DateTimeTools.DAYS(100)));
    assertHash(m);
  }

  @Test
  public void durations() {
    add(Duration.ofDays(45).plusSeconds(3500));
    add(Duration.ofDays(15).plusSeconds(3500));
    add(Duration.ofHours(17).plusMinutes(37).plusSeconds(45));
    add(Duration.ofMinutes(37).plusSeconds(45).plusMillis(789));
    add(Duration.ofSeconds(45).plusMillis(789));
    add(Duration.ofSeconds(1).plusMillis(789));
    add(Duration.ofMillis(789));

    JSMap m = map();
    for (Duration d : mDurations) {
      m.putNumbered(humanDuration(d.toMillis()));
      m.putNumbered(humanDuration(d.negated().toMillis()));
    }
    assertHash(m);
  }

  private List<Duration> mDurations = arrayList();

  private void add(Duration dur) {
    mDurations.add(dur);
  }

  @Test
  public void formatUTC() {
    Instant zt = Instant.ofEpochMilli(MAY19_TIME);
    JSMap m = map();
    m.putNumbered(DateTimeTools.FMT_ISO_INSTANT.format(zt));
    m.putNumbered(DateTimeTools.FMT_PREFERRED.withZone(ZoneOffset.UTC).format(zt));
    assertHash(m);
  }

  @Test
  public void formatPreferredTimeZone() {
    Instant zt = Instant.ofEpochMilli(MAY19_TIME);
    JSMap m = map();
    m.putNumbered(DateTimeTools.FMT_ISO_INSTANT.withZone(OUR_TIME_ZONE).format(zt));
    m.putNumbered(DateTimeTools.FMT_PREFERRED.format(zt));
    assertHash(m);
  }

  @Test
  public void formatEasternTimeZone() {
    final ZoneId EASTERN = ZoneId.of("America/Toronto");
    Instant zt = Instant.ofEpochMilli(MAY19_TIME);
    JSMap m = map();
    m.putNumbered(DateTimeTools.FMT_ISO_INSTANT.withZone(EASTERN).format(zt));
    m.putNumbered(DateTimeTools.FMT_PREFERRED.withZone(EASTERN).format(zt));
    assertHash(m);
  }

  @Test
  public void dateWithTimeFormat() {
    DateTimeFormatter f = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm").toFormatter()
        .withZone(OUR_TIME_ZONE);
    String input = "2015-07-30T18:50";
    ZonedDateTime zonedDateTime = ZonedDateTime.parse(input, f);
    assertEquals(1438307400000L, millis(zonedDateTime));
  }

  @Test
  public void staticDateWithTimeFormat() {
    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a").withZone(OUR_TIME_ZONE);
    ZonedDateTime dateTime = zonedDateTime(1483355045000L);
    assertEquals("2017-01-02 03:04 AM", f.format(dateTime));
  }

  @Test
  public void InformalDateTime() {
    ZonedDateTime dt = zonedDateTime(MAY19_TIME);
    String str = DateTimeTools.FMT_PREFERRED.format(dt);
    assertEquals("2016/05/19 11:01:05", str);
  }

  @Test
  public void TimeToISOInstant() {
    ZonedDateTime dt = zonedDateTime(MAY19_TIME);
    String expr = DateTimeTools.FMT_ISO_INSTANT.format(dt);
    assertEquals("2016-05-19T18:01:05Z", expr);
  }

  // ------------------------------------------------------------------
  // Parsing
  // ------------------------------------------------------------------

  @Test
  public void parseSloppy() {
    String s = "2015/07/30 15:51:42";
    ZonedDateTime time = DateTimeTools.parseSloppyDateTime(s);
    String s2 = "2015-07-30T15:51:42Z";
    assertEquals(s2, DateTimeTools.FMT_ISO_INSTANT.format(time));
  }

}
