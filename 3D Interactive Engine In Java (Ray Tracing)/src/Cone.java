import java.io.Serializable;

public class Cone extends Shape implements Serializable {
     Point3D center;
     double radius;
     double height;
     Material material;
     Color color;
     double fuzz;

    public Cone(Point3D center, double radius, double height, Color color, Material material, double fuzz) {
        this.center = center;
        this.radius = radius;
        this.height = height;
        this.color = color;
        this.material = material;
        this.fuzz = fuzz;
    }

    public double getHeight() {
        return height;
    }

    public double getRadius() {
        return radius;
    }

    public Point3D getCenter() {
        return center;
    }

    public void setRadius(double newRadius) {
        this.radius = newRadius;
    }

    public void setHeight(double newHeight) {
        this.height = newHeight;
    }

    public void setCenter(Point3D newCenter) {
        this.center = newCenter;
    }

    public void moveX(double newX) {
        this.center = new Point3D(newX, center.y, center.z);
    }

    public void moveY(double newY) {
        this.center = new Point3D(center.x, newY, center.z);
    }

    public void moveZ(double newZ) {
        this.center = new Point3D(center.x, center.y, newZ);
    }

    public double getCentroidX() {
        return center.x;
    }

    public double getCentroidY() {
        return center.y;
    }

    public double getCentroidZ() {
        return center.z;
    }

    public double distanceTo(Point3D p) {
        return p.sub(center).length();
    }

    public Point3D getNormal(Point3D hitPoint) {
        // Compute the normal of the cone at the given hit point
        Point3D apex = new Point3D(center.x, center.y, center.z + height);
        Point3D axis = apex.sub(center).normalize();
        Point3D cp = hitPoint.sub(center);
        double projection = cp.dot(axis);
        Point3D projected = axis.mul(projection);
        Point3D radial = cp.sub(projected);
        return radial.normalize().cross(axis).cross(radial.normalize()).normalize();
    }

    public double hit(Ray ray) {
        // Ray-Cone intersection (in Z-up orientation)
        // Cone is aligned with Z-axis and apex at center + height
        Point3D apex = new Point3D(center.x, center.y, center.z + height);
        Point3D co = ray.origin.sub(apex);

        double k = radius / height;
        k = k * k;

        double dx = ray.direction.x;
        double dy = ray.direction.y;
        double dz = ray.direction.z;

        double ox = co.x;
        double oy = co.y;
        double oz = co.z;

        double a = dx*dx + dy*dy - k * dz*dz;
        double b = 2 * (dx*ox + dy*oy - k * dz*oz);
        double c = ox*ox + oy*oy - k * oz*oz;

        double discriminant = b*b - 4*a*c;
        if (discriminant < 0) return 0;

        double sqrtDisc = Math.sqrt(discriminant);
        double t1 = (-b - sqrtDisc) / (2 * a);
        double t2 = (-b + sqrtDisc) / (2 * a);

        double t = Math.min(t1, t2);
        if (t < 0) t = Math.max(t1, t2);
        if (t < 0) return 0;

        Point3D hit = ray.at(t);
        double zRel = hit.z - center.z;

        if (zRel < 0 || zRel > height) return 0; // Outside the finite height cone

        return t;
    }

    public Color getColor() {
        return color;
    }

    public Material getMaterial() {
        return material;
    }

    public double getFuzz() {
        return fuzz;
    }
}
