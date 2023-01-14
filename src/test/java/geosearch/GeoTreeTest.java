package geosearch;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import js.geometry.FPoint3;
import js.json.JSMap;
import js.testutil.MyTestCase;
import static js.base.Tools.*;

public class GeoTreeTest extends MyTestCase {

  @Test
  public void a() {
    rv();
    
    
    for (int i = 0; i < 10; i++) {
      t().add(rpt());
      if (verbose())
        pr(t().dumpToString());
    }

    JSMap m = map();
m.putNumbered("","searching for exact stored points");

    // Look for the exact stored points
    for (GeoObject obj : pts) {
      GeoObject found = t().find(obj.location());
      m.putNumbered(obj.location().toString(), asStr(found));
      checkState(found == obj);
    }

    m.putNumbered("","searching for random points");

    // Look for random points
    for (int i = 0; i < pts.size(); i++) {
      FPoint3 obj = randomPoint();
      GeoObject found = t().find(obj);
      m.putNumbered(obj.toString(), asStr(found));
    }

    // Look for points near stored points
    //
    m.putNumbered("","searching for points near stored points");

    float rad = GeoTree.SEARCH_RADIUS/2;

    for (GeoObject obj : pts) {
      FPoint3 loc = obj.location();
      FPoint3 off = new FPoint3((random().nextFloat() - 0.5f) * rad, (random().nextFloat() - 0.5f) * rad,
          (random().nextFloat() - 0.5f) * rad);
      FPoint3 loc2 = FPoint3.sum(loc, off);
      GeoObject found = t().find(loc2);
      m.putNumbered(loc2.toString(), asStr(found));
    }

    assertMessage(m);
  }

  private static String asStr(GeoObject obj) {
    if (obj == null)
      return "<null>";
    return obj.location().toString();
  }

  private GeoTree t() {
    if (mt == null) {
      mt = new GeoTree();
      resetSeed(123);
      mt.setVerbose(verbose());
    }
    return mt;
  }

  private GeoTree mt;

  private GeoObject rpt() {
    FPoint3 p = randomPoint();
    GeoObject obj = new OurObj(p);
    pts.add(obj);
    return obj;
  }

  private FPoint3 randomPoint() {
    Random r = random();
    FPoint3 p = new FPoint3(r.nextInt(1000), r.nextInt(1000), r.nextInt(1000));
    return p;
  }

  private static class OurObj extends GeoObject {

    public OurObj(FPoint3 loc) {
      mLoc = loc;
    }

    @Override
    public FPoint3 location() {
      return mLoc;
    }

    private FPoint3 mLoc;
  }

  private List<GeoObject> pts = arrayList();
}
