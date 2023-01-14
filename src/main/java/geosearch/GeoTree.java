package geosearch;

import static js.base.Tools.*;

import java.util.Random;

import js.base.BaseObject;
import js.geometry.FPoint3;
import js.geometry.MyMath;

public final class GeoTree extends BaseObject {

  public static final float SEARCH_RADIUS = 20;

  public void add(GeoObject object) {

    log("add:", object.location());

    int depth = 0;
    GeoNode node = mRootNode;

    while (true) {
      log("depth:", depth, "node:", node);
      if (node.isLeaf()) {
        if (node.isFull()) {
          splitNode(node, depth);
          continue;
        }
        log("adding to node");
        node.add(object);
        log("---------------------------------");
        break;
      } else {

        float targetC = coord(depth, object.location());

        if (targetC < node.value) {
          log("descending left");
          node = node.left;
        } else {
          log("descending right");
          node = node.right;
        }
        depth++;
      }
    }
  }

  private static float coord(int depth, FPoint3 location) {
    switch (depth % 3) {
    default: // 0 
      return location.x;
    case 1:
      return location.y;
    case 2:
      return location.x;
    }
  }

  private void splitNode(GeoNode node, int depth) {

    log("splitNode");
    loadTools();

    GeoObject[] obj = node.points;

    // Choose bisector point
    int j = mRandom.nextInt(obj.length);

    float bisectLocation = coord(depth, obj[j].location());
    log("bisector location:", bisectLocation);
    todo("choose bisector point as one closest to mean");

    GeoNode left = GeoNode.newLeafNode();
    GeoNode right = GeoNode.newLeafNode();

    for (GeoObject x : obj) {
      float objLocation = coord(depth, x.location());

      float diff = objLocation - bisectLocation;
      if (diff == 0) {
        // place in node with smaller population
        if (left.points.length < right.points.length)
          diff = -1;
      }
      log("moving object to child node, location:", objLocation);
      if (diff < 0) {
        log("adding to left");
        left.add(x);
      } else {
        log("adding to right");
        right.add(x);
      }
    }
    node.left = left;
    node.right = right;
    node.points = null;
    node.value = bisectLocation;
  }

  public GeoObject find(FPoint3 queryLoc) {
    SearchResult nullResult = new SearchResult(null, SEARCH_RADIUS);
    if (verbose())
      log("--------------------------------------------------------", CR, "find:", queryLoc);

    SearchResult findResult = auxFind(mRootNode, 0, queryLoc, nullResult);
    return findResult.object;
  }

  private static float distanceSq(FPoint3 a, FPoint3 b) {
    float dx = a.x - b.x;
    float dy = a.y - b.y;
    float dz = a.z - b.z;
    return dx * dx + dy * dy + dz * dz;
  }

  private static class SearchResult {
    SearchResult(GeoObject obj, float dist) {
      object = obj;
      distance = dist;
    }

    final GeoObject object;
    final float distance;

    public String toString() {
      StringBuilder sb = new StringBuilder("<");
      if (object == null)
        sb.append("null");
      else
        sb.append(object.location());
      sb.append(" dist:");
      sb.append((int) MyMath.sqrtf(distance));
      sb.append(">");
      return sb.toString();
    }
  }

  private SearchResult auxFind(GeoNode node, int depth, FPoint3 queryLoc, SearchResult bestResult) {
    if (verbose())
      log(spaces(depth * 2), "depth:", depth, "node:", node, "best:", bestResult);

    if (node.isLeaf()) {
      for (int i = 0; i < node.pop; i++) {
        GeoObject c = node.points[i];
        float dist = distanceSq(queryLoc, c.location());
        if (dist < bestResult.distance) {
          bestResult = new SearchResult(c, dist);
        }
      }
    } else {

      float coord = coord(depth, queryLoc);
      float delta = coord - node.value;

      // If within a minimum distance of the bisector, search both subtrees
      if (Math.abs(delta) < bestResult.distance) {
        bestResult = auxFind(node.left, depth + 1, queryLoc, bestResult);
        bestResult = auxFind(node.right, depth + 1, queryLoc, bestResult);
      } else if (delta < 0) {
        bestResult = auxFind(node.left, depth + 1, queryLoc, bestResult);
      } else
        bestResult = auxFind(node.right, depth + 1, queryLoc, bestResult);
    }
    if (verbose())
      log(spaces(depth * 2), "result:", bestResult);
    return bestResult;
  }

  public String dumpToString() {
    return mRootNode.dumpTree();
  }

  private GeoNode mRootNode = GeoNode.newLeafNode();
  private Random mRandom = new Random(1966);
}
