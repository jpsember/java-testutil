package geosearch;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import js.base.BaseObject;
import js.geometry.FPoint3;

public final class GeoTree extends BaseObject {

  public void add(GeoObject object) {
    log("add:", object.location());

    int depth = 0;
    int dimension = 0;
    GeoNode node = mRootNode;

    while (true) {
      log("depth:", depth, "node:", node);
      if (node.isLeaf()) {
        if (node.isFull()) {
          splitNode(node, dimension);
          continue;
        }
        log("adding to node");
        node.add(object);
        log("---------------------------------");
        mSize++;
        break;
      } else {
        float targetC = coordinate(dimension, object.location());
        if (targetC < node.value) {
          log("descending left");
          node = node.left;
        } else {
          log("descending right");
          node = node.right;
        }
        depth++;
        dimension++;
        if (dimension == 3)
          dimension = 0;
      }
    }
  }

  public boolean remove(GeoObject obj) {
    int depth = 0;
    int dimension = 0;
    GeoNode node = mRootNode;

    GeoNode parentNode = null;

    while (true) {
      log("depth:", depth, "node:", node);
      if (node.isLeaf()) {
        int pop = node.population;
        GeoObject[] pts = node.points;

        for (int i = 0; i < pop; i++) {
          GeoObject c = pts[i];
          if (c == obj) {
            node.removeObject(i);
            mSize--;

            // If there is a parent node, and the population of this node has dropped to zero,
            // replace the parent node with this one's sibling
            
            // !!! This won't work, since the bisector property that the sibling points have
            // won't agree with the dimensionality of the parent node that they are moving to.
            //
            
            if (node.population == 0 && parentNode != null) {
              GeoNode sibling = parentNode.left;
              if (sibling == node)
                sibling = parentNode.right;
              
              pr("node is now empty:"
                  ,node);
              pr("parent:",parentNode);
              pr("sibling:",sibling);
              parentNode.left = sibling.left;
              parentNode.right = sibling.right;
              parentNode.population = sibling.population;
              parentNode.points = sibling.points;
              parentNode.value = sibling.value;
              parentNode.debugId = sibling.debugId;
              pr("parent now:",parentNode);
                }
            return true;
          }
        }
        return false;
      }

      parentNode = node;
      float targetC = coordinate(dimension, obj.location());
      if (targetC < node.value) {
        log("descending left");
        node = node.left;
      } else {
        log("descending right");
        node = node.right;
      }
      depth++;
      dimension++;
      if (dimension == 3)
        dimension = 0;
    }
  }

  public int size() {
    return mSize;
  }

  /**
   * Get a particular coordinate from a point
   */
  private static float coordinate(int dimension, FPoint3 location) {
    switch (dimension) {
    default: // 0 
      return location.x;
    case 1:
      return location.y;
    case 2:
      return location.x;
    }
  }

  // Comparators for sorting arrays of GeoObjects in a particular dimension
  //
  private static Comparator sDimensionComparators[] = { //
      new Comparator<GeoObject>() {
        @Override
        public int compare(GeoObject o1, GeoObject o2) {
          return Float.compare(o1.location().x, o2.location().x);
        }
      }, //
      new Comparator<GeoObject>() {
        @Override
        public int compare(GeoObject o1, GeoObject o2) {
          return Float.compare(o1.location().y, o2.location().y);
        }
      }, //
      new Comparator<GeoObject>() {
        @Override
        public int compare(GeoObject o1, GeoObject o2) {
          return Float.compare(o1.location().z, o2.location().z);
        }
      } };

  private void splitNode(GeoNode node, int dimension) {
    log("splitNode");

    GeoObject[] obj = node.points;

    // Choose median as bisector value
    Arrays.sort(obj, sDimensionComparators[dimension]);
    int j = obj.length / 2;

    float bisectLocation = coordinate(dimension, obj[j].location());
    log("bisector location:", bisectLocation);

    GeoNode left = GeoNode.newLeafNode();
    GeoNode right = GeoNode.newLeafNode();

    boolean shared = false;
    for (GeoObject x : obj) {
      float objLocation = coordinate(dimension, x.location());
      float diff = objLocation - bisectLocation;
      log("moving object to child node, location:", objLocation);
      if (diff < 0) {
        log("adding to left");
        left.add(x);
        shared = true;
      } else {
        log("adding to right");
        right.add(x);
      }
    }

    // If all the objects ended up in the same node, this is bad; it means the
    // coordinates were identical (at least in that one dimension)
    if (!shared)
      throw badArg("Multiple points had identical coordinates; perturb the inputs?");

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

  /**
   * Get the squared distance between three points
   */
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

    int dimension = depth % 3;

    if (node.isLeaf()) {
      for (int i = 0; i < node.population; i++) {
        GeoObject c = node.points[i];
        float dist = distanceSq(queryLoc, c.location());
        if (dist <= squaredDistance)
          results.add(new SearchResult(c, dist));
      }
    } else {

      float coord = coordinate(dimension, queryLoc);
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
  private int mSize;
}
