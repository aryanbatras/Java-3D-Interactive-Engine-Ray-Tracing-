import java.awt.*;
import java.io.Serializable;

public class Point3D implements Serializable {

    double x, y, z;

    Point3D() {
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    Point3D(Point3D p) {
        x = p.x;
        y = p.y;
        z = p.z;
    }

    Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Point3D add(Point3D p) {
        return new Point3D(
                x + p.x,
                y + p.y,
                z + p.z
        );
    }

    Point3D sub(Point3D p) {
        return new Point3D(
                x - p.x,
                y - p.y,
                z - p.z
        );
    }

    Point3D div(double scalar) {
        return new Point3D(
                x / scalar,
                y / scalar,
                z / scalar
        );
    }

    Point3D mul(double scalar) {
        return new Point3D(
                this.x * scalar,
                this.y * scalar,
                this.z * scalar
        );
    }

    double dot(Point3D p) {
        return x * p.x + y * p.y + z * p.z;
    }

    Point3D normalize() {
        double len = Math.sqrt(x * x + y * y + z * z);
        if (len == 0) return new Point3D(0, 0, 0); // avoid division by zero
        return new Point3D(x / len, y / len, z / len);
    }

    Point3D cross(Point3D other) {
        double x = this.y * other.z - this.z * other.y;
        double y = this.z * other.x - this.x * other.z;
        double z = this.x * other.y - this.y * other.x;
        return new Point3D(x, y, z);
    }

    double length(){
        return Math.sqrt(x * x + y * y + x * x);
    }

    public static Point3D projectToGround(Ray ray, double preserveY) {
        if (Math.abs(ray.direction.y) < 1e-6) { return ray.origin; }
        double t = -ray.origin.y / ray.direction.y;
        Point3D projected = ray.at(t);
        return new Point3D(projected.x, preserveY, projected.z);
    }

    public double distanceTo(Point3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

}












