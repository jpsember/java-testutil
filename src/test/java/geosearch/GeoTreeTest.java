package geosearch;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import js.geometry.FPoint3;
import js.testutil.MyTestCase;
import static js.base.Tools.*;

public class GeoTreeTest extends MyTestCase {

  @Test
  public void a() {
    for (int i = 0; i < 10; i++)
      t().add(rpt());
    for (GeoObject obj : pts) {
      GeoObject found = t().find(obj.location());
      checkState(found == obj);
    }
  }

  private GeoTree t() {
    if (mt == null)
      mt = new GeoTree();
    return mt;
  }

  private GeoTree mt;

  private GeoObject rpt() {
    Random r = random();
    FPoint3 p = new FPoint3(r.nextInt(100), r.nextInt(100), r.nextInt(100));
    GeoObject obj = new OurObj(p);
    pts.add(obj);
    return obj;
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
