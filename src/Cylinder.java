import java.io.Serializable;

public class Cylinder extends Shape implements Serializable {
    private static final long serialVersionUID = 1L;

    Point3D center;
    double radius;
    double height;
    Color color;
    Material material;
    double fuzz;

    Cylinder(Point3D center, double radius, double height, Color color, Material material, double fuzz) {
        this.center = new Point3D(center);
        this.radius = radius;
        this.height = height;
        this.color = new Color(color);
        this.material = material;
        this.fuzz = fuzz;
    }

    public Point3D getCenter() {
        return this.center;
    }

    public double getHeight() {
        return this.height;
    }

    public double hit(Ray r) {
        Point3D oc = r.origin.sub(center);
        double a = r.direction.x * r.direction.x + r.direction.z * r.direction.z;
        double b = 2.0 * (oc.x * r.direction.x + oc.z * r.direction.z);
        double c = oc.x * oc.x + oc.z * oc.z - radius * radius;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return 0.0;

        double sqrtD = Math.sqrt(discriminant);
        double t0 = (-b - sqrtD) / (2.0 * a);
        double t1 = (-b + sqrtD) / (2.0 * a);

        for (double t : new double[]{t0, t1}) {
            if (t > 10E-9) {
                double y = r.origin.y + t * r.direction.y;
                if (y >= center.y && y <= center.y + height) {
                    return t;
                }
            }
        }

        return 0.0;
    }

    public void setCenter(Point3D newCenter) {
        this.center = newCenter;
    }

    public double distanceTo(Point3D other) {
        double dx = center.x - other.x;
        double dy = center.y - other.y;
        double dz = center.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public double getRadius() {
        return radius;
    }

    public void moveX(double val) {
        center.x += val;
    }

    public void moveY(double val) {
        center.y += val;
    }

    public void moveZ(double val) {
        center.z += val;
    }

    public void setRadius(double r) {
        double current = getRadius();
        double scale = r / current;

        radius *= scale;
        height *= scale;
    }

}
