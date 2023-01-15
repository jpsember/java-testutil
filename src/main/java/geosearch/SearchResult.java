package geosearch;

import js.geometry.MyMath;

public final class SearchResult {

  SearchResult(GeoObject obj, float dist) {
    mGeoObject = obj;
    mSquaredDistance = dist;
  }

  private final GeoObject mGeoObject;
  private final float mSquaredDistance;

  public String toString() {
    StringBuilder sb = new StringBuilder("<");
    if (mGeoObject == null)
      sb.append("null");
    else
      sb.append(mGeoObject.location());
    sb.append(" dist:");
    sb.append((int) MyMath.sqrtf(mSquaredDistance));
    sb.append(">");
    return sb.toString();
  }

  public GeoObject geoObject() {
    return mGeoObject;
  }

  public float squaredDistance() {
    return mSquaredDistance;
  }

}