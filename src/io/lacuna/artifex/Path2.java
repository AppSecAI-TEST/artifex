package io.lacuna.artifex;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.lacuna.artifex.Box.box;
import static io.lacuna.artifex.Vec.vec;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.arraycopy;

/**
 * @author ztellman
 */
public class Path2 {

  private final List<Curve2> curves;
  private final Box2 bounds;
  private final boolean isRing;

  public Path2(List<Curve2> curves) {
    this.curves = curves.stream()
        .flatMap(c -> Arrays.stream(c.split(c.inflections())))
        .collect(Collectors.toList());

    bounds = curves.stream().map(c -> box(c.start(), c.end())).reduce(Box2::union).get();

    Curve2 first = curves.get(0);
    Curve2 last = curves.get(curves.size() - 1);
    isRing = first.start().equals(last.end());
  }

  public Path2 reverse() {
    return new Path2(curves.stream().map(Curve2::reverse).collect(Collectors.toList()));
  }

  public List<Curve2> curves() {
    return curves;
  }

  public boolean isRing() {
    return isRing;
  }

  public boolean inside(Vec2 p) {
    if (!isRing) {
      throw new IllegalStateException("path is not a ring");
    }

    LinearSegment2 ray = LinearSegment2.from(p, vec(bounds.ux + 1, p.y));

    return curves.stream().filter(c -> c.intersections(ray).length > 0).count() % 2 == 1;
  }

  public Box2 bounds() {
    return bounds;
  }

  public Vec2[] subdivide(double error) {
    List<Vec2[]> segments = curves.stream().map(c -> c.subdivide(error)).collect(Collectors.toList());
    int length = segments.stream().mapToInt(c -> c.length).sum() - (segments.size() - 1);

    Vec2[] result = new Vec2[length];
    int idx = 0;
    for (int i = 0; i < segments.size(); i++) {
      int offset = i == 0 ? 0 : 1;
      int len = segments.get(i).length - offset;
      arraycopy(segments.get(i), offset, result, idx, len);
      idx += len;
    }

    return result;
  }
}
