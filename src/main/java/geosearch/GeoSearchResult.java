package geosearch;

import js.geometry.MyMath;

public final class GeoSearchResult {

  GeoSearchResult(GeoObject obj, float dist) {
    mGeoObject = obj;
    mSquaredDistance = dist;
  }

  public GeoObject geoObject() {
    return mGeoObject;
  }

  public float squaredDistance() {
    return mSquaredDistance;
  }

  private final GeoObject mGeoObject;
  private final float mSquaredDistance;

}