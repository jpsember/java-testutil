package geosearch;

import static js.base.Tools.*;

import java.util.List;
import java.util.Random;

import js.base.BaseObject;
import js.geometry.FPoint3;

public final class GeoTree extends BaseObject {

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

  public List<SearchResult> find(FPoint3 queryLoc, float radius) {
    List<SearchResult> resultsList = arrayList();

    if (verbose())
      log("--------------------------------------------------------", CR, "find:", queryLoc);

    auxFind(resultsList, mRootNode, 0, queryLoc, radius * radius);
    return resultsList;
  }

  private static float distanceSq(FPoint3 a, FPoint3 b) {
    float dx = a.x - b.x;
    float dy = a.y - b.y;
    float dz = a.z - b.z;
    return dx * dx + dy * dy + dz * dz;
  }

  private void auxFind(List<SearchResult> results, GeoNode node, int depth, FPoint3 queryLoc,
      float squaredDistance) {
    if (verbose())
      log(spaces(depth * 2), "depth:", depth, "node:", node);

    if (node.isLeaf()) {
      for (int i = 0; i < node.pop; i++) {
        GeoObject c = node.points[i];
        float dist = distanceSq(queryLoc, c.location());
        if (dist <= squaredDistance)
          results.add(new SearchResult(c, dist));
      }
    } else {

      float coord = coord(depth, queryLoc);
      float delta = coord - node.value;

      // If within a minimum distance of the bisector, search both subtrees
      if (delta * delta <= squaredDistance) {
        auxFind(results, node.left, depth + 1, queryLoc, squaredDistance);
        auxFind(results, node.right, depth + 1, queryLoc, squaredDistance);
      } else if (delta < 0) {
        auxFind(results, node.left, depth + 1, queryLoc, squaredDistance);
      } else
        auxFind(results, node.right, depth + 1, queryLoc, squaredDistance);
    }
    if (verbose())
      log(spaces(depth * 2), "results:", results);
  }

  public String dumpToString() {
    return mRootNode.dumpTree();
  }

  private GeoNode mRootNode = GeoNode.newLeafNode();
  private Random mRandom = new Random(1966);
}
