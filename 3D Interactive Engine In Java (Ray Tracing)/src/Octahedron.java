import java.io.Serializable;

public class Octahedron extends Shape implements Serializable {

     Point3D[] vertices;
     Material material;
     Color color;
     double fuzz;

    public Octahedron(Point3D center, double size, Color color, Material material, double fuzz) {

        this.color = color;
        this.material = material;
        this.fuzz = fuzz;

        vertices = new Point3D[6];
        vertices[0] = center.add(new Point3D(0, size, 0));     // Top
        vertices[1] = center.add(new Point3D(0, -size, 0));    // Bottom
        vertices[2] = center.add(new Point3D(0, 0, size));     // Front
        vertices[3] = center.add(new Point3D(0, 0, -size));    // Back
        vertices[4] = center.add(new Point3D(-size, 0, 0));    // Left
        vertices[5] = center.add(new Point3D(size, 0, 0));     // Right
    }

    private double intersect(Ray ray, Point3D v0, Point3D v1, Point3D v2) {
        Point3D edge1 = v1.sub(v0);
        Point3D edge2 = v2.sub(v0);
        Point3D h = ray.direction.cross(edge2);
        double a = edge1.dot(h);
        if (Math.abs(a) < 1e-8) return 0;

        double f = 1.0 / a;
        Point3D s = ray.origin.sub(v0);
        double u = f * s.dot(h);
        if (u < 0.0 || u > 1.0) return 0;

        Point3D q = s.cross(edge1);
        double v = f * ray.direction.dot(q);
        if (v < 0.0 || u + v > 1.0) return 0;

        double t = f * edge2.dot(q);
        return t > 1e-8 ? t : 0;
    }

    public double hit(Ray ray) {
        Point3D[][] faces = {
                {vertices[0], vertices[2], vertices[4]},
                {vertices[0], vertices[4], vertices[3]},
                {vertices[0], vertices[3], vertices[5]},
                {vertices[0], vertices[5], vertices[2]},
                {vertices[1], vertices[4], vertices[2]},
                {vertices[1], vertices[3], vertices[4]},
                {vertices[1], vertices[5], vertices[3]},
                {vertices[1], vertices[2], vertices[5]},
        };

        double closest = Double.MAX_VALUE;
        for (Point3D[] tri : faces) {
            double t = intersect(ray, tri[0], tri[1], tri[2]);
            if (t > 0 && t < closest) {
                closest = t;
            }
        }

        return (closest == Double.MAX_VALUE) ? 0 : closest;
    }

    public Point3D getNormal() {
        Point3D edge1 = vertices[2].sub(vertices[0]);
        Point3D edge2 = vertices[4].sub(vertices[0]);
        return edge1.cross(edge2).normalize();
    }

    public Point3D getCenter() {
        double x = 0, y = 0, z = 0;
        for (Point3D v : vertices) {
            x += v.x;
            y += v.y;
            z += v.z;
        }
        return new Point3D(x / 6.0, y / 6.0, z / 6.0);
    }

    public void moveX(double newX) {
        double dx = newX - getCenter().x;
        for (Point3D v : vertices) v.x += dx;
    }

    public void moveY(double newY) {
        double dy = newY - getCenter().y;
        for (Point3D v : vertices) v.y += dy;
    }

    public void moveZ(double newZ) {
        double dz = newZ - getCenter().z;
        for (Point3D v : vertices) v.z += dz;
    }

    public double getCentroidX() { return getCenter().x; }
    public double getCentroidY() { return getCenter().y; }
    public double getCentroidZ() { return getCenter().z; }

    public double getRadius() {
        Point3D center = getCenter();
        double max = 0;
        for (Point3D v : vertices) {
            max = Math.max(max, center.distanceTo(v));
        }
        return max;
    }

    public void setRadius(double newRadius) {
        Point3D center = getCenter();
        double currentRadius = getRadius();
        if (currentRadius == 0) return;
        double scale = newRadius / currentRadius;

        for (int i = 0; i < vertices.length; i++) {
            Point3D dir = vertices[i].sub(center);
            vertices[i] = center.add(dir.mul(scale));
        }
    }

    public void setCenter(Point3D newCenter) {
        Point3D offset = newCenter.sub(getCenter());
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = vertices[i].add(offset);
        }
    }

    public double distanceTo(Point3D p) {
        return getCenter().sub(p).length();
    }

    public double getSize() {
        Point3D center = getCenter();
        double total = 0;
        for (Point3D v : vertices) {
            total += center.distanceTo(v);
        }
        return total / vertices.length;
    }
}
