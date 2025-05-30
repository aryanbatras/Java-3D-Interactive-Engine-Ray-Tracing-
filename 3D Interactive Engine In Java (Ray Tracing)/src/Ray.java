public class Ray {

    Point3D origin;
    Point3D direction;

    /*

    A 3D point P = origin + (t * direction)

    Where t is a scalar that determines the distance between the ray origin and the object being hit
    The distance is along one direction starting from the origin point

     */

    Ray(Point3D origin, Point3D direction){
        this.origin = new Point3D(origin);
        this.direction = new Point3D(direction);
    }

    Point3D getOrigin(){ return origin; }
    Point3D getDirection(){ return direction; }

    Point3D at(double t) {
        // point = origin + scalar * direction
        return origin.add(direction.mul(t));
    }

}
