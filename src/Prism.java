import java.io.Serializable;

public class Prism extends Shape implements Serializable {

     Point3D[] vertices;
     Material material;
     Color color;
     double fuzz;

    public Prism(Point3D min, Point3D max, Color color, Material material, double fuzz) {

        this.color = color;
        this.material = material;
        this.fuzz = fuzz;

        vertices = new Point3D[8];
        vertices[0] = new Point3D(min.x, min.y, min.z);
        vertices[1] = new Point3D(max.x, min.y, min.z);
        vertices[2] = new Point3D(max.x, max.y, min.z);
        vertices[3] = new Point3D(min.x, max.y, min.z);
        vertices[4] = new Point3D(min.x, min.y, max.z);
        vertices[5] = new Point3D(max.x, min.y, max.z);
        vertices[6] = new Point3D(max.x, max.y, max.z);
        vertices[7] = new Point3D(min.x, max.y, max.z);
    }

    public double hit(Ray ray) {

        Point3D min = getMin();
        Point3D max = getMax();

        double tMin = (min.x - ray.origin.x) / ray.direction.x;
        double tMax = (max.x - ray.origin.x) / ray.direction.x;
        if (tMin > tMax) { double tmp = tMin; tMin = tMax; tMax = tmp; }

        double tyMin = (min.y - ray.origin.y) / ray.direction.y;
        double tyMax = (max.y - ray.origin.y) / ray.direction.y;
        if (tyMin > tyMax) { double tmp = tyMin; tyMin = tyMax; tyMax = tmp; }

        if ((tMin > tyMax) || (tyMin > tMax)) return 0;
        if (tyMin > tMin) tMin = tyMin;
        if (tyMax < tMax) tMax = tyMax;

        double tzMin = (min.z - ray.origin.z) / ray.direction.z;
        double tzMax = (max.z - ray.origin.z) / ray.direction.z;
        if (tzMin > tzMax) { double tmp = tzMin; tzMin = tzMax; tzMax = tmp; }

        if ((tMin > tzMax) || (tzMin > tMax)) return 0;
        if (tzMin > tMin) tMin = tzMin;
        if (tzMax < tMax) tMax = tzMax;

        return (tMin > 0) ? tMin : ((tMax > 0) ? tMax : 0);
    }

    public Point3D getNormal() { return new Point3D(0, 1, 0); }

    public Point3D getCenter() {
        double x = 0, y = 0, z = 0;
        for (Point3D v : vertices) {
            x += v.x;
            y += v.y;
            z += v.z;
        }
        return new Point3D(x / 8.0, y / 8.0, z / 8.0);
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
        double maxDist = 0;
        for (Point3D v : vertices) {
            double dist = v.sub(center).length();
            if (dist > maxDist) maxDist = dist;
        }
        return maxDist;
    }

    public void setRadius(double newRadius) {
        Point3D center = getCenter();
        double currentRadius = getRadius();
        if (currentRadius == 0) return;
        double scale = newRadius / currentRadius;
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = center.add(vertices[i].sub(center).mul(scale));
        }
    }

    public double distanceTo(Point3D p) {
        return getCenter().sub(p).length();
    }

    public void setCenter(Point3D newCenter) {
        Point3D current = getCenter();
        Point3D offset = newCenter.sub(current);
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = vertices[i].add(offset);
        }
    }

    public Point3D getMin() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        for (Point3D v : vertices) {
            minX = Math.min(minX, v.x);
            minY = Math.min(minY, v.y);
            minZ = Math.min(minZ, v.z);
        }
        return new Point3D(minX, minY, minZ);
    }

    public Point3D getMax() {
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (Point3D v : vertices) {
            maxX = Math.max(maxX, v.x);
            maxY = Math.max(maxY, v.y);
            maxZ = Math.max(maxZ, v.z);
        }
        return new Point3D(maxX, maxY, maxZ);
    }
}
