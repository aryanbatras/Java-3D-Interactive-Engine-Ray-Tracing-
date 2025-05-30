import java.io.Serializable;

public class Box extends Shape implements Serializable {
    private static final long serialVersionUID = 1L;

    private Point3D center;
    private double width, height, depth;
    public Color color;
    public Material material;
    public double fuzz;

    public Box(Point3D center, double width, double height, double depth, Color color, Material material, double fuzz) {
        this.center = new Point3D(center);
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.color = new Color(color);
        this.material = material;
        this.fuzz = fuzz;
    }

    public Point3D getMin() {
        return new Point3D(
                center.x - width / 2,
                center.y - height / 2,
                center.z - depth / 2
        );
    }

    public Point3D getMax() {
        return new Point3D(
                center.x + width / 2,
                center.y + height / 2,
                center.z + depth / 2
        );
    }

    public void setCenter(Point3D newCenter) {
        this.center = new Point3D(newCenter);
    }

    public Point3D getCenter() {
        return new Point3D(center);
    }

    public double getBoundingRadius() {
        return Math.sqrt(width * width + height * height + depth * depth) / 2;
    }

    public void moveX(double dx) {
        center.x += dx;
    }

    public void moveY(double dy) {
        center.y += dy;
    }

    public void moveZ(double dz) {
        center.z += dz;
    }

    public void setRadius(double r) {
        double current = getBoundingRadius();
        double scale = r / current;
        width *= scale;
        height *= scale;
        depth *= scale;
    }

    public double hit(Ray ray) {
        Point3D min = getMin();
        Point3D max = getMax();
        double tMin = 0.001;
        double tMax = Double.MAX_VALUE;

        for (int i = 0; i < 3; i++) {
            double origin = (i == 0) ? ray.origin.x : (i == 1) ? ray.origin.y : ray.origin.z;
            double direction = (i == 0) ? ray.direction.x : (i == 1) ? ray.direction.y : ray.direction.z;
            double minBound = (i == 0) ? min.x : (i == 1) ? min.y : min.z;
            double maxBound = (i == 0) ? max.x : (i == 1) ? max.y : max.z;

            double invD = 1.0 / direction;
            double t0 = (minBound - origin) * invD;
            double t1 = (maxBound - origin) * invD;
            if (invD < 0.0) {
                double tmp = t0;
                t0 = t1;
                t1 = tmp;
            }
            tMin = Math.max(t0, tMin);
            tMax = Math.min(t1, tMax);
            if (tMax <= tMin) return -1;
        }

        return tMin;
    }

    public double distanceTo(Point3D point) {
        return center.sub(point).length();
    }

    public double getWidth() {
        return Math.abs(getMin().x - getMin().x);
    }

    public double getHeight() {
        return Math.abs(getMin().y - getMin().y);
    }

    public double getDepth() {
        return Math.abs(getMax().z - getMax().z);
    }

}
