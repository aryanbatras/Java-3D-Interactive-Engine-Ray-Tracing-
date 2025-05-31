import java.io.Serializable;

public class Triangle extends Shape implements Serializable {

    Point3D v0, v1, v2;
    Point3D normal;

    Material material;
    Color color;
    double fuzz;

    public Triangle(Point3D v0, Point3D v1, Point3D v2) {

        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;

    }

    public Triangle(Point3D a, Point3D b, Point3D c, Color color, Material material, double fuzz) {

        this.v0 = a;
        this.v1 = b;
        this.v2 = c;
        this.color = color;
        this.material = material;
        this.fuzz = fuzz;
        this.normal = (b.sub(a)).cross(c.sub(a)).normalize();

    }

    public double hit(Ray ray) {

        Point3D edge1 = v1.sub(v0);
        Point3D edge2 = v2.sub(v0);

        Point3D h = ray.direction.cross(edge2);
        double a = edge1.dot(h);

        if (a > -1e-8 && a < 1e-8) return 0;

        double f = 1.0 / a;
        Point3D s = ray.origin.sub(v0);
        double u = f * s.dot(h);
        if (u < 0.0 || u > 1.0) return 0;

        Point3D q = s.cross(edge1);
        double v = f * ray.direction.dot(q);
        if (v < 0.0 || u + v > 1.0) return 0;

        double t = f * edge2.dot(q);

        return (t > 1e-8) ? t : 0;
    }

    public Point3D getNormal() {
        return normal;
    }

    public Point3D getCenter() {
        return v0.add(v1).add(v2).div(3.0);
    }

    public void moveX(double newX) {
        double cx = (v0.x + v1.x + v2.x) / 3.0;
        double dx = newX - cx;
        v0.x += dx;
        v1.x += dx;
        v2.x += dx;
    }

    public void moveY(double newY) {
        double cy = (v0.y + v1.y + v2.y) / 3.0;
        double dy = newY - cy;
        v0.y += dy;
        v1.y += dy;
        v2.y += dy;
    }

    public void moveZ(double newZ) {
        double cz = (v0.z + v1.z + v2.z) / 3.0;
        double dz = newZ - cz;
        v0.z += dz;
        v1.z += dz;
        v2.z += dz;
    }

    public double getCentroidX() {
        return (v0.x + v1.x + v2.x) / 3.0;
    }

    public double getCentroidY() {
        return (v0.y + v1.y + v2.y) / 3.0;
    }

    public double getCentroidZ() {
        return (v0.z + v1.z + v2.z) / 3.0;
    }

    public double getRadius() {
        Point3D center = new Point3D(getCentroidX(), getCentroidY(), getCentroidZ());
        double d0 = v0.sub(center).length();
        double d1 = v1.sub(center).length();
        double d2 = v2.sub(center).length();
        return Math.max(d0, Math.max(d1, d2));
    }

    public void setRadius(double newRadius) {

        Point3D center = new Point3D(getCentroidX(), getCentroidY(), getCentroidZ());
        double currentRadius = getRadius();
        if (currentRadius == 0) return; // Avoid divide-by-zero

        double scale = newRadius / currentRadius;

        v0 = center.add(v0.sub(center).mul(scale));
        v1 = center.add(v1.sub(center).mul(scale));
        v2 = center.add(v2.sub(center).mul(scale));
    }

    public double distanceTo(Point3D p) {
        // Approximate distance to triangle center
        Point3D center = new Point3D(
                (v0.x + v1.x + v2.x) / 3.0,
                (v0.y + v1.y + v2.y) / 3.0,
                (v0.z + v1.z + v2.z) / 3.0
        );
        return center.sub(p).length();
    }

    public void setCenter(Point3D newCenter) {
        Point3D currentCenter = v0.add(v1).add(v2).div(3.0);
        Point3D offset = newCenter.sub(currentCenter);
        v0 = v0.add(offset);
        v1 = v1.add(offset);
        v2 = v2.add(offset);
    }

}
