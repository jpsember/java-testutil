package geosearch;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.geometry.FPoint3;

public final class GeoTree extends BaseObject {

  public void add(GeoObject object) {
    notSupported();
  }

  public GeoObject find(FPoint3 location) {
 throw notSupported(); }
  
  private GeoNode mRootNode = new GeoNode();
}
