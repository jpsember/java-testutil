package geosearch;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import js.geometry.FPoint3;
import js.geometry.MyMath;
import js.json.JSMap;
import js.testutil.MyTestCase;
import static js.base.Tools.*;

public class GeoTreeTest extends MyTestCase {

  private static final float SEARCH_RADIUS = 20;

  @Test
  public void buildThenFind() {
    for (int i = 0; i < 10; i++) {
      tree().add(randomObj());
      if (verbose())
        pr(tree().dumpToString());
    }

    JSMap m = map();
    m.putNumbered("", "searching for exact stored points");

    // Look for the exact stored points
    for (GeoObject obj : pts) {
      List<SearchResult> found = tree().find(obj.location(), 0);
      m.putNumbered(obj.location().toString(), summary(found));
      checkState(found.size() == 1 && found.get(0).geoObject() == obj);
    }

    m.putNumbered("", "searching for random points");

    // Look for random points
    for (int i = 0; i < pts.size(); i++) {
      FPoint3 obj = randomPoint();
      List<SearchResult> found = tree().find(obj, SEARCH_RADIUS * 6);
      m.putNumbered(obj.toString(), summary(found));
    }

    // Look for points near stored points
    //
    m.putNumbered("", "searching for points near stored points");

    float rad = SEARCH_RADIUS / 2;

    for (GeoObject obj : pts) {
      FPoint3 loc = obj.location();
      FPoint3 off = new FPoint3((random().nextFloat() - 0.5f) * rad, (random().nextFloat() - 0.5f) * rad,
          (random().nextFloat() - 0.5f) * rad);
      FPoint3 loc2 = FPoint3.sum(loc, off);
      List<SearchResult> found = tree().find(loc2, SEARCH_RADIUS);
      m.putNumbered(loc2.toString(), summary(found));
    }

    assertMessage(m);
  }

  private static String summary(Iterable<SearchResult> res) {
    StringBuilder sb = new StringBuilder("[");
    for (SearchResult r : res) {
      if (sb.length() > 1)
        sb.append(' ');
      sb.append(r.geoObject().location());
      sb.append("(" + (int) MyMath.sqrtf(r.squaredDistance()) + ")");
    }
    sb.append(']');
    return sb.toString();
  }

  private GeoTree tree() {
    if (mGeoTree == null) {
      mGeoTree = new GeoTree();
      resetSeed(123);
      mGeoTree.setVerbose(verbose());
    }
    return mGeoTree;
  }

  private GeoObject randomObj() {
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

    private final FPoint3 mLoc;
  }

  private List<GeoObject> pts = arrayList();

  private GeoTree mGeoTree;

}
