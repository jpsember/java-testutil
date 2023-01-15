package geosearch;

import static js.base.Tools.*;

public final class GeoNode {

  public static GeoNode newLeafNode() {
    GeoNode n = new GeoNode();
    n.points = new GeoObject[CAPACITY];
    return n;
  }

  private GeoNode() {
  }

  private static final int CAPACITY = 10;

  private static int sNextDebugId = 100;

  int debugId = sNextDebugId++;
  float value;
  GeoNode left;
  GeoNode right;
  int population;
  GeoObject[] points;

  public boolean isLeaf() {
    return points != null;
  }

  public boolean isFull() {
    checkState(isLeaf());
    return population == CAPACITY;
  }

  public void add(GeoObject object) {
    checkState(population < CAPACITY);
    points[population] = object;
    population++;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("#");
    sb.append(debugId);
    sb.append(' ');

    if (isLeaf()) {
      sb.append("leaf     ");
      sb.append("pop:");
      sb.append(population);
      sb.append(" [");
      for (int k = 0; k < population; k++) {
        if (k > 0)
          sb.append(", ");
        GeoObject n = points[k];
        sb.append(n.location());
      }
      sb.append("]");
    } else {
      sb.append("internal ");
      sb.append("bisector:");
      sb.append(value);
      sb.append(" ");
      sb.append("left:");
      sb.append(left.debugId);
      sb.append(" right:");
      sb.append(right.debugId);
    }
    return sb.toString();
  }

  public String dumpTree() {
    StringBuilder sb = new StringBuilder();
    dumpTreeAux(this, sb, 0);
    return sb.toString();
  }

  private static void dumpTreeAux(GeoNode node, StringBuilder sb, int depth) {
    String sp = spaces(depth * 2);
    String result = node.toString();
    sb.append(sp);
    sb.append(result);
    sb.append('\n');
    if (!node.isLeaf()) {
      dumpTreeAux(node.left, sb, depth + 1);
      dumpTreeAux(node.right, sb, depth + 1);
    }
  }

  public void removeObject(int i) {
    checkState(isLeaf());
    while (i + 1 < population) {
      points[i] = points[i + 1];
      i++;
    }
    population--;
    points[population] = null;
  }

}
