public class Camera {
    private Point3D origin;
    private Point3D lowerLeftCorner;
    private Point3D horizontal;
    private Point3D vertical;
    private Point3D u, v, w;
    private double lensRadius;

    public Camera(Point3D lookfrom, Point3D lookat, Point3D vup, double vfov, double aspectRatio) {
        double theta = Math.toRadians(vfov);
        double h = Math.tan(theta / 2);
        double viewportHeight = 2.0 * h;
        double viewportWidth = aspectRatio * viewportHeight;

        w = lookfrom.sub(lookat).normalize(); // camera backward
        u = vup.cross(w).normalize();         // camera right
        v = w.cross(u);                       // camera up

        origin = lookfrom;
        horizontal = u.mul(viewportWidth);
        vertical = v.mul(viewportHeight);
        lowerLeftCorner = origin.sub(horizontal.mul(0.5)).sub(vertical.mul(0.5)).sub(w);
    }

    public Ray getRay(double s, double t) {
        Point3D direction = lowerLeftCorner.add(horizontal.mul(s)).add(vertical.mul(t)).sub(origin);
        return new Ray(origin, direction);
    }

    public Point3D getForward() {
        return w.mul(-1); // Forward is -w
    }

    public Point3D getRight() {
        return u;
    }

    public Point3D getUp() {
        return v;
    }

    public Point3D getOrigin() {
        return origin;
    }

    public void setPosition(Point3D newOrigin) {
        this.origin = newOrigin;
        lowerLeftCorner = origin.sub(horizontal.mul(0.5)).sub(vertical.mul(0.5)).sub(w);
    }

    public Ray getRayFromScreen(double screenX, double screenY, int screenWidth, int screenHeight) {
        double u = screenX / (double) screenWidth;
        double v = 1.0 - (screenY / (double) screenHeight);
        return getRay(u, v);
    }

    public Point3D getLookFrom() {
        return origin;
    }

    public Point3D getLookAt() {
        return origin.add(w.mul(-1));
    }

    public Point3D getVUp() {
        return v;
    }

}
