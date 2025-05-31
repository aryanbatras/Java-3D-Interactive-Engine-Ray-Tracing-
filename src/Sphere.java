import java.io.Serializable;

public class Sphere extends Shape implements Serializable {
    private static final long serialVersionUID = 1L;

    Point3D center;
    double radius;
    Color color;

    Material material;
    double fuzz;

    Sphere(Point3D center, double radius, Color c, Material m, double f){
        this.center = new Point3D(center);
        this.color = new Color(c);
        this.radius = radius;
        material = m;
        fuzz = f;
    }

    public double hit(Ray r) {

        /*

        If you take a point on the sphere,
        And you subtract it from the center of that sphere,
        And you dot it with that same value then you will get the square of the radius

        The point on that sphere is where the ray intersects

        (p-c)*(p-c) = r^2;
        (o+td-c) * (o+td-c) - r^2 = 0;
        (d*d)t^2 + (2(o-c)*d)t + (o-c)*(o-c) - r^2 = 0;

        a(t^2) + b(t) + c = 0

        We formed a quadratic equation that we can solve to get t

        */

        double a = r.direction.dot(r.direction);
        double b = 2 * r.origin.sub(center).dot(r.direction);
        double c = r.origin.sub(center).dot(r.origin.sub(center)) - radius * radius;

        double discriminant = ( b * b ) - 4 * a * c;

        if(discriminant < 0.0 ) { // ray never intersects
            return 0;
        } else {

            // getting roots of quad eqn
            double t0 = ( -b - Math.sqrt(discriminant) ) / ( 2 * a );
            double t1 = ( -b + Math.sqrt(discriminant) ) / ( 2 * a );

            if( t0 > 10E-9){
                return t0; // returns distance
            }

            if(t1 > 10E-9){
                return t1;
            }

            return 0.0; // no distance no hit
        }

    }

    public double distanceTo(Point3D other) {
        double dx = center.x - other.x;
        double dy = center.y - other.y;
        double dz = center.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public void setCenter(Point3D newCenter) {
        this.center = newCenter;
    }

    public double getRadius() {
        return radius;
    }

}