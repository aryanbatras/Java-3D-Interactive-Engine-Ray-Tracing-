import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/*

 Initial Setup Screen
 Do the Resolution setup
 Frame should be relative to the W & H of Image
 And It should not change after setup
 RayTracerWindow.getDimensions() -> W & H

 */


public class Main {

    public static Random random;

    public static void main(String[] args) throws IOException {
        random = new Random();
        new Window();
    }

    public static Color rayColor(ArrayList<Shape> WORLD, Ray r, BufferedImage environmentMap, int depth) {
        if(depth <= 0){ return new Color(0,0,0); }
        double nearest = Double.MAX_VALUE;
        Material hitMaterial = null;
        Point3D hitPoint = null;
        Point3D normal = null;
        Shape hitShape = null;
        Color hitColor = null;
        double fuzz = 0f;

        for (Shape currentShape : WORLD) {
            double t = currentShape.hit(r);
            if (t > 0.001 && t < nearest) {
                nearest = t;
                hitPoint = r.at(t);
                hitShape = currentShape;
                if(currentShape instanceof Sphere sphere){
                    normal = hitPoint.sub(sphere.center).normalize( );
                    hitColor = sphere.color;
                    hitMaterial = sphere.material;
                    fuzz = sphere.fuzz;
                } else if(currentShape instanceof Triangle triangle){
                    normal = triangle.getNormal();
                    hitColor = triangle.color;
                    hitMaterial = triangle.material;
                    fuzz = triangle.fuzz;
                } else if (currentShape instanceof Box box) {
                    Point3D center = box.getMin().add(box.getMax()).mul(0.5);
                    Point3D localHit = hitPoint.sub(center);
                    double dx = Math.abs(localHit.x) - Math.abs(box.getMax().x - box.getMin().x) / 2.0;
                    double dy = Math.abs(localHit.y) - Math.abs(box.getMax().y - box.getMin().y) / 2.0;
                    double dz = Math.abs(localHit.z) - Math.abs(box.getMax().z - box.getMin().z) / 2.0;
                    if (dx > dy && dx > dz)
                        normal = new Point3D(Math.signum(localHit.x), 0, 0);
                    else if (dy > dz)
                        normal = new Point3D(0, Math.signum(localHit.y), 0);
                    else
                        normal = new Point3D(0, 0, Math.signum(localHit.z));
                    hitColor = box.color;
                    hitMaterial = box.material;
                    fuzz = box.fuzz;
                } else if (currentShape instanceof Cylinder cylinder) {
                    Point3D base = cylinder.center;
                    Point3D top = new Point3D(base.x, base.y + cylinder.height, base.z);
                    double y = hitPoint.y;
                    if (Math.abs(y - base.y) < 1e-3) {
                        normal = new Point3D(0, -1, 0);
                    } else if (Math.abs(y - top.y) < 1e-3) {
                        normal = new Point3D(0, 1, 0);
                    } else {
                        Point3D radial = new Point3D(hitPoint.x, 0, hitPoint.z).sub(new Point3D(base.x, 0, base.z));
                        normal = radial.normalize( );
                    }
                    hitColor = cylinder.color;
                    hitMaterial = cylinder.material;
                    fuzz = cylinder.fuzz;
                } else if (currentShape instanceof Prism prism ||
                        currentShape instanceof Octahedron octahedron ||
                        currentShape instanceof Cone torus) {
                    if (currentShape instanceof Prism p) {
                        normal = p.getNormal();
                        hitColor = p.color;
                        hitMaterial = p.material;
                        fuzz = p.fuzz;
                    } else if (currentShape instanceof Octahedron o) {
                        normal = o.getNormal();
                        hitColor = o.color;
                        hitMaterial = o.material;
                        fuzz = o.fuzz;
                    } else if (currentShape instanceof Cone tShape) {
                        normal = tShape.getNormal(hitPoint);
                        hitColor = tShape.color;
                        hitMaterial = tShape.material;
                        fuzz = tShape.fuzz;
                    }
                }
            }
        }

        if(hitShape != null && hitMaterial == Material.LAMBERTIAN){
            ArrayList<Point3D> lightsource = new ArrayList<>();
            lightsource.add(new Point3D(1,1,-1).normalize());
            lightsource.add(new Point3D(-1,1,-1).normalize());
            lightsource.add(new Point3D(0,-1,-1).normalize());
            float re = 0, g = 0, b = 0;
            for (Point3D light : lightsource) {
                double intensity = Math.max(0, normal.dot(light));
                re += hitColor.r * (float) intensity;
                g += hitColor.g * (float) intensity;
                b += hitColor.b * (float) intensity;
            }
            re = Math.min(1, re);
            g = Math.min(1, g);
            b = Math.min(1, b);
            return new Color(re, g, b);

        } else if(hitShape != null && hitMaterial == Material.METAL){
            Point3D ref = reflect(r.direction.normalize(), normal);
            ref = ref.add(randomInUnitSphere().mul(fuzz));
            if(ref.dot(normal) > 0){
                Ray refray = new Ray(hitPoint, ref);
                Color refcolor = rayColor(WORLD, refray, environmentMap, depth - 1);
                return new Color(
                        hitColor.r * refcolor.r,
                        hitColor.g * refcolor.g,
                        hitColor.b * refcolor.b
                );
            } else {
                return new Color(0,0,0);
            }

         } else if (hitShape != null && hitMaterial == Material.DIELECTRIC) {
            Color attenuation = hitColor;
            double ir = fuzz;
            Point3D outward_normal;
            double etai_over_etat;
            if (r.direction.dot(normal) > 0) {
                outward_normal = normal.mul(-1);
                etai_over_etat = ir;
            } else {
                outward_normal = normal;
                etai_over_etat = 1.0 / ir;
            }
            Point3D unit_direction = r.direction.normalize();
            double cos_theta = Math.min(unit_direction.mul(-1).dot(outward_normal), 1.0);
            double sin_theta = Math.sqrt(Math.max(0.0, 1.0 - cos_theta * cos_theta));
            Point3D scattered_direction;
            boolean cannot_refract = etai_over_etat * sin_theta > 1.00000001;
            double reflectance_prob;
            if (cannot_refract) {
                reflectance_prob = 1.0;
            } else {
                double r0 = (etai_over_etat - 1.0) / (etai_over_etat + 1.0);
                r0 = r0 * r0;
                reflectance_prob = r0 + (1.0 - r0) * Math.pow((1.0 - cos_theta), 5);
            }
            if (reflectance_prob > random.nextDouble()) {
                scattered_direction = reflect(unit_direction, outward_normal);
            } else {
                scattered_direction = refract(unit_direction, outward_normal, etai_over_etat);
            }
            Ray scattered_ray = new Ray(hitPoint.add(outward_normal.mul(0.0001)), scattered_direction);
            Color scattered_color = rayColor(WORLD, scattered_ray, environmentMap, depth - 1);

            return new Color(
                    attenuation.r * scattered_color.r,
                    attenuation.g * scattered_color.g,
                    attenuation.b * scattered_color.b
            );
        } else if (hitShape != null && hitMaterial == Material.GLOSSY) {
            Point3D reflected = reflect(r.direction.normalize(), normal);
            reflected = reflected.add(randomInUnitSphere().mul(fuzz * 0.5));
            Ray glossyRay = new Ray(hitPoint, reflected);
            Color glossyColor = rayColor(WORLD, glossyRay, environmentMap, depth - 1);
            return new Color(
                    (hitColor.r * glossyColor.r + hitColor.r * 0.3f) / 1.3f,
                    (hitColor.g * glossyColor.g + hitColor.g * 0.3f) / 1.3f,
                    (hitColor.b * glossyColor.b + hitColor.b * 0.3f) / 1.3f
            );
        } else if (hitShape != null && hitMaterial == Material.PLASTIC) {
            Point3D diffuseDir = normal.add(randomInUnitSphere()).normalize();
            Ray diffuseRay = new Ray(hitPoint, diffuseDir);
            Color diffuseColor = rayColor(WORLD, diffuseRay, environmentMap, depth - 1);
            Point3D specular = reflect(r.direction.normalize(), normal).add(randomInUnitSphere().mul(fuzz * 0.3));
            Ray specularRay = new Ray(hitPoint, specular);
            Color specularColor = rayColor(WORLD, specularRay, environmentMap, depth - 1);
            return new Color(
                    0.7f * hitColor.r * diffuseColor.r + 0.3f * specularColor.r,
                    0.7f * hitColor.g * diffuseColor.g + 0.3f * specularColor.g,
                    0.7f * hitColor.b * diffuseColor.b + 0.3f * specularColor.b
            );
        } else if (hitShape != null && hitMaterial == Material.MATTE) {
            ArrayList<Point3D> lightsource = new ArrayList<>();
            lightsource.add(new Point3D(1,1,-1).normalize());
            lightsource.add(new Point3D(-1,1,-1).normalize());
            float re = 0, g = 0, b = 0;
            for (Point3D light : lightsource) {
                double intensity = Math.max(0, normal.dot(light));
                re += hitColor.r * 0.9f * (float) intensity;
                g += hitColor.g * 0.9f * (float) intensity;
                b += hitColor.b * 0.9f * (float) intensity;
            }
            return new Color(Math.min(1, re), Math.min(1, g), Math.min(1, b));
        } else if (hitShape != null && hitMaterial == Material.MIRROR) {
            Point3D reflected = reflect(r.direction.normalize( ), normal);
            Ray reflectedRay = new Ray(hitPoint, reflected);
            Color reflectedColor = rayColor(WORLD, reflectedRay, environmentMap, depth - 1);
            return new Color(
                    reflectedColor.r,
                    reflectedColor.g,
                    reflectedColor.b
            );
        } else if (hitShape != null && hitMaterial == Material.TRANSLUCENT) {
            Point3D scatterDir = normal.add(randomInUnitSphere().mul(0.5)).normalize();
            Ray scatterRay = new Ray(hitPoint, scatterDir);
            Color scatterColor = rayColor(WORLD, scatterRay, environmentMap, depth - 1);
            return new Color(
                    hitColor.r * scatterColor.r * 0.9f,
                    hitColor.g * scatterColor.g * 0.9f,
                    hitColor.b * scatterColor.b * 0.9f
            );
        } else if (hitShape != null && hitMaterial == Material.CHROME) {
            Point3D reflected = reflect(r.direction.normalize( ), normal);
            reflected = reflected.add(randomInUnitSphere( ).mul(fuzz * 0.1));
            Ray rayChrome = new Ray(hitPoint, reflected);
            Color chromeColor = rayColor(WORLD, rayChrome, environmentMap, depth - 1);
            return new Color(
                    hitColor.r * chromeColor.r * 1.2f,
                    hitColor.g * chromeColor.g * 1.1f,
                    hitColor.b * chromeColor.b * 1.2f
            );
        }
        else if (hitShape != null && hitMaterial == Material.ANODIZED_METAL) {
            Point3D refl = reflect(r.direction.normalize(), normal).add(randomInUnitSphere().mul(fuzz));
            Ray rayAnodized = new Ray(hitPoint, refl);
            Color reflColor = rayColor(WORLD, rayAnodized, environmentMap, depth - 1);
            float shift = (float)Math.abs(Math.sin(hitPoint.y * 3));
            Color anodize = new Color(0.5f + shift * 0.5f, 0.3f, 0.7f);
            return new Color(
                    hitColor.r * reflColor.r * anodize.r,
                    hitColor.g * reflColor.g * anodize.g,
                    hitColor.b * reflColor.b * anodize.b
            );
        } else if (hitShape != null && hitMaterial == Material.CRYSTAL) {
            Point3D refr = refract(r.direction.normalize(), normal, 1.6);
            Ray scatteredRay = new Ray(hitPoint, refr.add(randomInUnitSphere().mul(0.1)));
            Color refractedColor = rayColor(WORLD, scatteredRay, environmentMap, depth - 1);
            return new Color(
                    Math.min(1, hitColor.r * refractedColor.r + 0.2f),
                    Math.min(1, hitColor.g * refractedColor.g + 0.2f),
                    Math.min(1, hitColor.b * refractedColor.b + 0.2f)
            );
        } else if (hitShape != null && hitMaterial == Material.MIST) {
            float fade = (float)Math.exp(-0.1 * hitPoint.length());
            fade = Math.max(0.50f, fade);
            return new Color(
                    hitColor.r * fade,
                    hitColor.g * fade,
                    hitColor.b * fade
            );
        } else if (hitMaterial == Material.MAGIC_GOO) {
            Point3D dir = normal.add(randomInUnitSphere().mul(0.7)).normalize();
            Ray rayGoo = new Ray(hitPoint, dir);
            Color scatter = rayColor(WORLD, rayGoo, environmentMap, depth - 1);
            float edgeGlow = (float)Math.pow(1 - Math.abs(normal.dot(r.direction.normalize())), 2);
            return new Color(
                    hitColor.r * scatter.r + edgeGlow * 0.3f,
                    hitColor.g * scatter.g + edgeGlow * 0.3f,
                    hitColor.b * scatter.b + edgeGlow * 0.3f
            );
        }

        if (hitShape == null) {
            Point3D direction = r.getDirection().normalize();
            double u = 0.5 + Math.atan2(direction.z, direction.x) / (2 * Math.PI);
            double v = 0.5 - Math.asin(direction.y) / Math.PI;
            int x = (int)(u * environmentMap.getWidth());
            int y = (int)(v * environmentMap.getHeight());
            x = Math.min(Math.max(x, 0), environmentMap.getWidth() - 1);
            y = Math.min(Math.max(y, 0), environmentMap.getHeight() - 1);
            int rgb = environmentMap.getRGB(x, y);
            float rr = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;
            return new Color(rr, g, b);
        }

        Point3D unitDirection = r.direction.normalize();
        double t = 0.5 * (unitDirection.y + 1.0);
        return new Color(
                (float)((1.0 - t) * 1.0 + t * 0.1),
                (float)((1.0 - t) * 1.0 + t * 0.1),
                (float)((1.0 - t) * 1.0 + t * 0.99)
        );

    }

    public static Point3D reflect(Point3D v, Point3D n) {
        return v.sub(n.mul(2 * v.dot(n)));
    }

    public static Point3D refract(Point3D uv, Point3D n, double etai_over_etat) {
        double cos_theta = Math.min(uv.mul(-1).dot(n), 1.0);
        Point3D r_out_perp = uv.add(n.mul(cos_theta)).mul(etai_over_etat);
        double r_out_perp_length_sq = r_out_perp.dot(r_out_perp);
        if (r_out_perp_length_sq > 1.0) {}
        Point3D r_out_parallel = n.mul(-Math.sqrt(Math.abs(1.0 - r_out_perp_length_sq)));
        return r_out_perp.add(r_out_parallel);
    }

    public static Point3D randomInUnitSphere() {
        while (true) {
            Point3D p = new Point3D(
                    random.nextDouble() * 2 - 1,
                    random.nextDouble() * 2 - 1,
                    random.nextDouble() * 2 - 1
            );
            if (p.dot(p) < 1) return p;
        }
    }


    public static double intersectRayCone(Ray ray, Cone cone) {
        Point3D apex = cone.getCenter();
        Point3D dir = ray.direction;
        Point3D origin = ray.origin.sub(apex);
        double height = cone.getHeight();
        double radius = cone.getRadius();
        double tanTheta = radius / height;
        double k = tanTheta * tanTheta;
        double dx = dir.x;
        double dy = dir.y;
        double dz = dir.z;
        double ox = origin.x;
        double oy = origin.y;
        double oz = origin.z;
        double a = dx*dx + dz*dz - k * dy*dy;
        double b = 2 * (ox*dx + oz*dz - k * oy*dy);
        double c = ox*ox + oz*oz - k * oy*oy;
        double discriminant = b*b - 4*a*c;
        if (discriminant < 0) return -1;
        double sqrtDisc = Math.sqrt(discriminant);
        double t0 = (-b - sqrtDisc) / (2*a);
        double t1 = (-b + sqrtDisc) / (2*a);
        for (double t : new double[]{t0, t1}) {
            if (t > 0) {
                double y = oy + dy * t;
                if (y >= 0 && y <= height) {
                    return t;
                }
            }
        }
        return -1;
    }

    public static double intersectRayPrism(Ray ray, Prism prism) {
        double closestT = Double.MAX_VALUE;
        boolean hit = false;
        Point3D[] v = prism.vertices;
        int[][] faces = {
                {0, 1, 2}, {0, 2, 3},
                {4, 5, 6}, {4, 6, 7},
                {0, 1, 5}, {0, 5, 4},
                {2, 3, 7}, {2, 7, 6},
                {1, 2, 6}, {1, 6, 5},
                {0, 3, 7}, {0, 7, 4}
        };
        for (int[] f : faces) {
            double t = intersectRayTriangle(ray, v[f[0]], v[f[1]], v[f[2]]);
            if (t > 1e-6 && t < closestT) {
                closestT = t;
                hit = true;
            }
        }
        return hit ? closestT : -1;
    }

    public static double intersectRayOctahedron(Ray ray, Octahedron octahedron) {
        double closestT = Double.MAX_VALUE;
        boolean hit = false;
        Point3D[] v = octahedron.vertices;
        int[][] faces = {
                {0, 2, 4}, {0, 4, 3}, {0, 3, 5}, {0, 5, 2}, // top
                {1, 4, 2}, {1, 3, 4}, {1, 5, 3}, {1, 2, 5}  // bottom
        };
        for (int[] f : faces) {
            double t = intersectRayTriangle(ray, v[f[0]], v[f[1]], v[f[2]]);
            if (t > 1e-6 && t < closestT) {
                closestT = t;
                hit = true;
            }
        }
        return hit ? closestT : -1;
    }

    public static double intersectRaySphere(Ray ray, Sphere sphere) {
        Point3D oc = ray.origin.sub(sphere.center);
        double a = ray.direction.dot(ray.direction);
        double b = 2.0 * oc.dot(ray.direction);
        double c = oc.dot(oc) - sphere.radius * sphere.radius;
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return -1;
        double sqrtDiscriminant = Math.sqrt(discriminant);
        double t0 = (-b - sqrtDiscriminant) / (2.0 * a);
        double t1 = (-b + sqrtDiscriminant) / (2.0 * a);
        if (t0 > 1e-6) return t0;
        if (t1 > 1e-6) return t1;
        return -1;
    }

    public static double intersectRayBox(Ray ray, Box box) {

        double txmin = (box.getMin().x - ray.origin.x) / ray.direction.x;
        double txmax = (box.getMax().x - ray.origin.x) / ray.direction.x;
        if (txmin > txmax) { double temp = txmin; txmin = txmax; txmax = temp; }

        double tymin = (box.getMin().y - ray.origin.y) / ray.direction.y;
        double tymax = (box.getMax().y - ray.origin.y) / ray.direction.y;
        if (tymin > tymax) { double temp = tymin; tymin = tymax; tymax = temp; }

        if ((txmin > tymax) || (tymin > txmax)) return -1;

        if (tymin > txmin) txmin = tymin;
        if (tymax < txmax) txmax = tymax;

        double tzmin = (box.getMin().z - ray.origin.z) / ray.direction.z;
        double tzmax = (box.getMax().z - ray.origin.z) / ray.direction.z;
        if (tzmin > tzmax) { double temp = tzmin; tzmin = tzmax; tzmax = temp; }

        if ((txmin > tzmax) || (tzmin > txmax)) return -1;

        if (tzmin > txmin) txmin = tzmin;
        if (tzmax < txmax) txmax = tzmax;

        if (txmin < 1e-6 && txmax < 1e-6) return -1;

        return txmin > 1e-6 ? txmin : txmax;
    }

    public static double intersectRayCylinder(Ray ray, Cylinder cylinder) {
        double dx = ray.origin.x - cylinder.center.x;
        double dz = ray.origin.z - cylinder.center.z;

        double a = ray.direction.x * ray.direction.x + ray.direction.z * ray.direction.z;
        double b = 2 * (dx * ray.direction.x + dz * ray.direction.z);
        double c = dx * dx + dz * dz - cylinder.radius * cylinder.radius;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return -1;

        double sqrtDiscriminant = Math.sqrt(discriminant);
        double t0 = (-b - sqrtDiscriminant) / (2 * a);
        double t1 = (-b + sqrtDiscriminant) / (2 * a);

        double tSide = -1;

        for (double tCandidate : new double[]{t0, t1}) {
            if (tCandidate > 1e-6) {
                double y = ray.origin.y + tCandidate * ray.direction.y;
                if (y >= cylinder.center.y && y <= (cylinder.center.y + cylinder.height)) {
                    tSide = tCandidate;
                    break;
                }
            }
        }

        double tCap = -1;
        if (Math.abs(ray.direction.y) > 1e-6) {
            double tBottom = (cylinder.center.y - ray.origin.y) / ray.direction.y;
            if (tBottom > 1e-6) {
                Point3D p = ray.at(tBottom);
                double distX = p.x - cylinder.center.x;
                double distZ = p.z - cylinder.center.z;
                if (distX * distX + distZ * distZ <= cylinder.radius * cylinder.radius) {
                    tCap = tBottom;
                }
            }

            double tTop = (cylinder.center.y + cylinder.height - ray.origin.y) / ray.direction.y;
            if (tTop > 1e-6) {
                Point3D p = ray.at(tTop);
                double distX = p.x - cylinder.center.x;
                double distZ = p.z - cylinder.center.z;
                if (distX * distX + distZ * distZ <= cylinder.radius * cylinder.radius) {
                    if (tCap < 0 || tTop < tCap) tCap = tTop;
                }
            }
        }

        if (tSide > 0 && tCap > 0) return Math.min(tSide, tCap);
        if (tSide > 0) return tSide;
        if (tCap > 0) return tCap;

        return -1;
    }

    public static double intersectRayTriangle(Ray ray, Triangle triangle) {
        Point3D v0 = triangle.v0;
        Point3D v1 = triangle.v1;
        Point3D v2 = triangle.v2;

        Point3D edge1 = v1.sub(v0);
        Point3D edge2 = v2.sub(v0);
        Point3D h = ray.direction.cross(edge2);
        double a = edge1.dot(h);

        if (Math.abs(a) < 1e-6) return -1;

        double f = 1.0 / a;
        Point3D s = ray.origin.sub(v0);
        double u = f * s.dot(h);

        if (u < 0.0 || u > 1.0) return -1;

        Point3D q = s.cross(edge1);
        double v = f * ray.direction.dot(q);

        if (v < 0.0 || u + v > 1.0) return -1;

        double t = f * edge2.dot(q);
        return t > 1e-6 ? t : -1;
    }

    public static double intersectRayTriangle(Ray ray, Point3D v0, Point3D v1, Point3D v2) {
        Point3D edge1 = v1.sub(v0);
        Point3D edge2 = v2.sub(v0);
        Point3D h = ray.direction.cross(edge2);
        double a = edge1.dot(h);

        if (Math.abs(a) < 1e-8) return -1;

        double f = 1.0 / a;
        Point3D s = ray.origin.sub(v0);
        double u = f * s.dot(h);

        if (u < 0.0 || u > 1.0) return -1;

        Point3D q = s.cross(edge1);
        double v = f * ray.direction.dot(q);

        if (v < 0.0 || u + v > 1.0) return -1;

        double t = f * edge2.dot(q);
        return (t > 1e-6) ? t : -1;
    }

    public static boolean doesCollide(Shape newShape, ArrayList<Shape> existingShapes) {

        if (newShape instanceof Sphere newSphere) {

            for (Shape s : existingShapes) {

                if (s instanceof Sphere otherSphere) {
                    double distance = newSphere.distanceTo(otherSphere.center);
                    if (distance < (newSphere.radius + otherSphere.radius)) {
                        return true;
                    }

                } else if (s instanceof Triangle otherTriangle) {
                    double distance = newSphere.distanceTo(otherTriangle.getCenter());
                    if (distance < (newSphere.radius + otherTriangle.getRadius())) {
                        return true;
                    }

                } else if (s instanceof Box otherBox) {
                    Point3D boxCenter = otherBox.getCenter();
                    double boxRadius = otherBox.getBoundingRadius();
                    double distance = newSphere.distanceTo(boxCenter);
                    if (distance < (newSphere.radius + boxRadius)) {
                        return true;
                    }

                } else if (s instanceof Cylinder otherCylinder) {
                    Point3D cylCenter = otherCylinder.center;
                    double cylRadius = otherCylinder.radius;
                    double cylHeight = otherCylinder.height;
                    Point3D cylMid = new Point3D(cylCenter.x, cylCenter.y + cylHeight / 2.0, cylCenter.z);
                    double distance = newSphere.distanceTo(cylMid);
                    if (distance < (newSphere.radius + cylRadius)) {
                        return true;
                    }
                }
                else if (s instanceof Cone otherCone) {
                    Point3D coneBase = otherCone.getCenter();
                    double coneHeight = otherCone.getHeight();
                    double coneRadius = otherCone.getRadius();

                    Point3D coneTip = new Point3D(coneBase.x, coneBase.y, coneBase.z + coneHeight);
                    Point3D coneMid = coneBase.add(coneTip).div(2);

                    double dx = newSphere.center.x - coneMid.x;
                    double dy = newSphere.center.y - coneMid.y;
                    double horizontalDistance = Math.sqrt(dx*dx + dy*dy);

                    if (horizontalDistance < (newSphere.getRadius() + coneRadius)) {

                        double minZ1 = newSphere.center.z - newSphere.getRadius();
                        double maxZ1 = newSphere.center.z + newSphere.getRadius();
                        double minZ2 = Math.min(coneBase.z, coneTip.z);
                        double maxZ2 = Math.max(coneBase.z, coneTip.z);

                        boolean zOverlap = (minZ1 <= maxZ2 && maxZ1 >= minZ2);
                        if (zOverlap) return true;
                    }
                }
                if (s instanceof Prism otherPrism) {
                    double distance = newSphere.distanceTo(otherPrism.getCenter());
                    if (distance < (newSphere.radius + otherPrism.getRadius())) return true;
                }
                else if (s instanceof Octahedron otherOct) {
                    double distance = newSphere.distanceTo(otherOct.getCenter());
                    if (distance < (newSphere.radius + otherOct.getRadius())) return true;
                }

            }

        } else if (newShape instanceof Triangle newTriangle) {

            for (Shape s : existingShapes) {

                if (s instanceof Sphere otherSphere) {
                    double distance = newTriangle.distanceTo(otherSphere.center);
                    if (distance < (newTriangle.getRadius() + otherSphere.radius)) {
                        return true;
                    }

                } else if (s instanceof Triangle otherTriangle) {
                    double distance = newTriangle.distanceTo(otherTriangle.getCenter());
                    if (distance < (newTriangle.getRadius() + otherTriangle.getRadius())) {
                        return true;
                    }

                } else if (s instanceof Box otherBox) {
                    Point3D boxCenter = otherBox.getCenter();
                    double boxRadius = otherBox.getBoundingRadius();
                    double distance = newTriangle.distanceTo(boxCenter);
                    if (distance < (newTriangle.getRadius() + boxRadius)) {
                        return true;
                    }

                } else if (s instanceof Cylinder otherCylinder) {
                    Point3D cylCenter = otherCylinder.center;
                    double cylRadius = otherCylinder.radius;
                    double cylHeight = otherCylinder.height;
                    Point3D cylMid = new Point3D(cylCenter.x, cylCenter.y + cylHeight / 2.0, cylCenter.z);
                    double distance = newTriangle.distanceTo(cylMid);
                    if (distance < (newTriangle.getRadius() + cylRadius)) {
                        return true;
                    }
                }
                else if (s instanceof Cone otherCone) {
                    Point3D coneBase = otherCone.getCenter();
                    double coneHeight = otherCone.getHeight();
                    double coneRadius = otherCone.getRadius();

                    Point3D coneTip = new Point3D(coneBase.x, coneBase.y, coneBase.z + coneHeight);
                    Point3D coneMid = coneBase.add(coneTip).div(2);

                    double distance = newTriangle.distanceTo(coneMid);

                    double boundingRadius = coneRadius;
                    double boundingHeight = coneHeight;

                    if (distance < (newTriangle.getRadius() + boundingRadius)) {
                        double minZ1 = newTriangle.getCentroidZ() - newTriangle.getRadius();
                        double maxZ1 = newTriangle.getCentroidZ() + newTriangle.getRadius();
                        double minZ2 = Math.min(coneBase.z, coneTip.z);
                        double maxZ2 = Math.max(coneBase.z, coneTip.z);

                        boolean zOverlap = (minZ1 <= maxZ2 && maxZ1 >= minZ2);
                        if (zOverlap) return true;
                    }
                }

                else if (s instanceof Prism otherPrism) {
                    double distance = newTriangle.distanceTo(otherPrism.getCenter());
                    if (distance < (newTriangle.getRadius() + otherPrism.getRadius())) return true;
                }
                else if (s instanceof Octahedron otherOct) {
                    double distance = newTriangle.distanceTo(otherOct.getCenter());
                    if (distance < (newTriangle.getRadius() + otherOct.getRadius())) return true;
                }

            }

        } else if (newShape instanceof Box newBox) {

            for (Shape s : existingShapes) {

                Point3D boxCenter = newBox.getCenter();
                double boxRadius = newBox.getBoundingRadius();

                if (s instanceof Sphere otherSphere) {
                    double distance = boxCenter.distanceTo(otherSphere.center);
                    if (distance < (boxRadius + otherSphere.radius)) {
                        return true;
                    }

                } else if (s instanceof Triangle otherTriangle) {
                    double distance = boxCenter.distanceTo(otherTriangle.getCenter());
                    if (distance < (boxRadius + otherTriangle.getRadius())) {
                        return true;
                    }

                } else if (s instanceof Box otherBox) {
                    double distance = boxCenter.distanceTo(otherBox.getCenter());
                    if (distance < (boxRadius + otherBox.getBoundingRadius())) {
                        return true;
                    }

                } else if (s instanceof Cylinder otherCylinder) {
                    Point3D cylMid = new Point3D(
                            otherCylinder.center.x,
                            otherCylinder.center.y + otherCylinder.height / 2.0,
                            otherCylinder.center.z
                    );
                    double distance = boxCenter.distanceTo(cylMid);
                    if (distance < (boxRadius + otherCylinder.radius)) {
                        return true;
                    }
                }
                else if (s instanceof Cone otherCone) {
                    Point3D coneBase = otherCone.getCenter();
                    double coneHeight = otherCone.getHeight();
                    double coneRadius = otherCone.getRadius();

                    Point3D coneTip = new Point3D(coneBase.x, coneBase.y, coneBase.z + coneHeight);
                    Point3D coneMid = coneBase.add(coneTip).div(2);

                    double dx = boxCenter.x - coneMid.x;
                    double dy = boxCenter.y - coneMid.y;
                    double horizontalDistance = Math.sqrt(dx*dx + dy*dy);

                    if (horizontalDistance < (boxRadius + coneRadius)) {

                        double boxMinZ = boxCenter.z - newBox.getHeight() / 2;
                        double boxMaxZ = boxCenter.z + newBox.getHeight() / 2;

                        double coneMinZ = Math.min(coneBase.z, coneTip.z);
                        double coneMaxZ = Math.max(coneBase.z, coneTip.z);

                        boolean zOverlap = (boxMinZ <= coneMaxZ && boxMaxZ >= coneMinZ);
                        if (zOverlap) return true;
                    }
                }

                else if (s instanceof Prism otherPrism) {
                    double distance = boxCenter.distanceTo(otherPrism.getCenter());
                    if (distance < (boxRadius + otherPrism.getRadius())) return true;
                }
                else if (s instanceof Octahedron otherOct) {
                    double distance = boxCenter.distanceTo(otherOct.getCenter());
                    if (distance < (boxRadius + otherOct.getRadius())) return true;
                }
            }

        } else if (newShape instanceof Cylinder newCylinder) {

            Point3D cylMid = new Point3D(
                    newCylinder.center.x,
                    newCylinder.center.y + newCylinder.height / 2.0,
                    newCylinder.center.z
            );

            for (Shape s : existingShapes) {

                if (s instanceof Sphere otherSphere) {
                    double distance = cylMid.distanceTo(otherSphere.center);
                    if (distance < (newCylinder.radius + otherSphere.radius)) {
                        return true;
                    }

                } else if (s instanceof Triangle otherTriangle) {
                    double distance = cylMid.distanceTo(otherTriangle.getCenter());
                    if (distance < (newCylinder.radius + otherTriangle.getRadius())) {
                        return true;
                    }

                } else if (s instanceof Box otherBox) {
                    double distance = cylMid.distanceTo(otherBox.getCenter());
                    if (distance < (newCylinder.radius + otherBox.getBoundingRadius())) {
                        return true;
                    }

                } else if (s instanceof Cylinder otherCylinder) {
                    Point3D otherCylMid = new Point3D(
                            otherCylinder.center.x,
                            otherCylinder.center.y + otherCylinder.height / 2.0,
                            otherCylinder.center.z
                    );
                    double distance = cylMid.distanceTo(otherCylMid);
                    if (distance < (newCylinder.radius + otherCylinder.radius)) {
                        return true;
                    }
                }

                else if (s instanceof Cone otherCone) {

                    Point3D coneBase = otherCone.getCenter();
                    double coneHeight = otherCone.getHeight();
                    double coneRadius = otherCone.getRadius();
                    Point3D coneTip = new Point3D(coneBase.x, coneBase.y, coneBase.z + coneHeight);
                    Point3D coneMid = coneBase.add(coneTip).div(2);

                    Point3D cylBase = newCylinder.getCenter();
                    double cylHeight = newCylinder.getHeight();
                    double cylRadius = newCylinder.getRadius();
                    Point3D cylTip = new Point3D(cylBase.x, cylBase.y, cylBase.z + cylHeight);

                    double dx = cylMid.x - coneMid.x;
                    double dy = cylMid.y - coneMid.y;
                    double horizontalDistance = Math.sqrt(dx*dx + dy*dy);

                    if (horizontalDistance < (cylRadius + coneRadius)) {
                        double coneMinZ = Math.min(coneBase.z, coneTip.z);
                        double coneMaxZ = Math.max(coneBase.z, coneTip.z);

                        double cylMinZ = Math.min(cylBase.z, cylTip.z);
                        double cylMaxZ = Math.max(cylBase.z, cylTip.z);

                        boolean zOverlap = (cylMinZ <= coneMaxZ && cylMaxZ >= coneMinZ);
                        if (zOverlap) return true;
                    }
                }
                else if (s instanceof Prism otherPrism) {
                    double distance = cylMid.distanceTo(otherPrism.getCenter());
                    if (distance < (newCylinder.radius + otherPrism.getRadius())) return true;
                }
                else if (s instanceof Octahedron otherOct) {
                    double distance = cylMid.distanceTo(otherOct.getCenter());
                    if (distance < (newCylinder.radius + otherOct.getRadius())) return true;
                }

            }
        } else if (newShape instanceof Cone newCone) {

            Point3D coneBase = newCone.getCenter();
            double coneHeight = newCone.getHeight();
            double coneRadius = newCone.getRadius();
            Point3D coneTip = new Point3D(coneBase.x, coneBase.y, coneBase.z + coneHeight);
            Point3D coneMid = coneBase.add(coneTip).div(2);

            for (Shape s : existingShapes) {

                if (s instanceof Sphere otherSphere) {
                    double distance = coneBase.distanceTo(otherSphere.center);
                    if (distance < (coneRadius + otherSphere.radius)) return true;

                } else if (s instanceof Triangle otherTriangle) {
                    double distance = coneMid.distanceTo(otherTriangle.getCenter());
                    if (distance < (coneRadius + otherTriangle.getRadius())) {
                        double minZ1 = otherTriangle.getCentroidZ() - otherTriangle.getRadius();
                        double maxZ1 = otherTriangle.getCentroidZ() + otherTriangle.getRadius();
                        double minZ2 = Math.min(coneBase.z, coneTip.z);
                        double maxZ2 = Math.max(coneBase.z, coneTip.z);
                        boolean zOverlap = (minZ1 <= maxZ2 && maxZ1 >= minZ2);
                        if (zOverlap) return true;
                    }

                } else if (s instanceof Box otherBox) {
                    double distance = coneMid.distanceTo(otherBox.getCenter());
                    double boundingRadius = otherBox.getBoundingRadius();
                    if (distance < (coneRadius + boundingRadius)) return true;

                } else if (s instanceof Cylinder otherCylinder) {
                    Point3D cylMid = new Point3D(
                            otherCylinder.center.x,
                            otherCylinder.center.y + otherCylinder.height / 2.0,
                            otherCylinder.center.z
                    );
                    double distance = coneMid.distanceTo(cylMid);
                    if (distance < (coneRadius + otherCylinder.radius)) return true;

                } else if (s instanceof Cone otherCone) {
                    Point3D otherConeBase = otherCone.getCenter();
                    double otherConeHeight = otherCone.getHeight();
                    double otherConeRadius = otherCone.getRadius();
                    Point3D otherConeTip = new Point3D(otherConeBase.x, otherConeBase.y, otherConeBase.z + otherConeHeight);
                    Point3D otherConeMid = otherConeBase.add(otherConeTip).div(2);

                    double distance = coneMid.distanceTo(otherConeMid);
                    double boundingRadius = coneRadius + otherConeRadius;

                    if (distance < boundingRadius) {
                        double minZ1 = Math.min(coneBase.z, coneTip.z);
                        double maxZ1 = Math.max(coneBase.z, coneTip.z);
                        double minZ2 = Math.min(otherConeBase.z, otherConeTip.z);
                        double maxZ2 = Math.max(otherConeBase.z, otherConeTip.z);
                        boolean zOverlap = (minZ1 <= maxZ2 && maxZ1 >= minZ2);
                        if (zOverlap) return true;
                    }

                } else if (s instanceof Prism otherPrism) {
                    double distance = coneMid.distanceTo(otherPrism.getCenter());
                    if (distance < (coneRadius + otherPrism.getRadius())) return true;

                } else if (s instanceof Octahedron otherOct) {
                    double distance = coneMid.distanceTo(otherOct.getCenter());
                    if (distance < (coneRadius + otherOct.getRadius())) return true;
                }
            }
        }
        else if (newShape instanceof Prism newPrism) {

            Point3D prismCenter = newPrism.getCenter();
            double prismRadius = newPrism.getRadius();

            for (Shape s : existingShapes) {

                if (s instanceof Sphere otherSphere) {
                    double distance = prismCenter.distanceTo(otherSphere.center);
                    if (distance < (prismRadius + otherSphere.radius)) return true;
                } else if (s instanceof Triangle otherTriangle) {
                    double distance = prismCenter.distanceTo(otherTriangle.getCenter());
                    if (distance < (prismRadius + otherTriangle.getRadius())) return true;
                } else if (s instanceof Box otherBox) {
                    double distance = prismCenter.distanceTo(otherBox.getCenter());
                    if (distance < (prismRadius + otherBox.getBoundingRadius())) return true;
                } else if (s instanceof Cylinder otherCylinder) {
                    Point3D cylMid = new Point3D(
                            otherCylinder.center.x,
                            otherCylinder.center.y + otherCylinder.height / 2.0,
                            otherCylinder.center.z
                    );
                    double distance = prismCenter.distanceTo(cylMid);
                    if (distance < (prismRadius + otherCylinder.radius)) return true;
                }else if (s instanceof Cone otherCone) {
                    Point3D coneBase = otherCone.getCenter();
                    double coneHeight = otherCone.getHeight();
                    double coneRadius = otherCone.getRadius();
                    Point3D coneTip = new Point3D(coneBase.x, coneBase.y, coneBase.z + coneHeight);
                    Point3D coneMid = coneBase.add(coneTip).div(2);
                    double distance = prismCenter.distanceTo(coneMid);

                    double boundingRadius = coneRadius;
                    if (distance < (prismRadius + boundingRadius)) {
                        double minZ1 = prismCenter.z - prismRadius;
                        double maxZ1 = prismCenter.z + prismRadius;
                        double minZ2 = Math.min(coneBase.z, coneTip.z);
                        double maxZ2 = Math.max(coneBase.z, coneTip.z);

                        boolean zOverlap = (minZ1 <= maxZ2 && maxZ1 >= minZ2);
                        if (zOverlap) return true;
                    }
                }
                else if (s instanceof Prism otherPrism) {
                    double distance = prismCenter.distanceTo(otherPrism.getCenter());
                    if (distance < (prismRadius + otherPrism.getRadius())) return true;
                } else if (s instanceof Octahedron otherOct) {
                    double distance = prismCenter.distanceTo(otherOct.getCenter());
                    if (distance < (prismRadius + otherOct.getRadius())) return true;
                }
            }

        } else if (newShape instanceof Octahedron newOct) {
            Point3D octCenter = newOct.getCenter();
            double octRadius = newOct.getRadius();

            for (Shape s : existingShapes) {
                if (s instanceof Sphere otherSphere) {
                    double distance = octCenter.distanceTo(otherSphere.center);
                    if (distance < (octRadius + otherSphere.radius)) return true;
                } else if (s instanceof Triangle otherTriangle) {
                    double distance = octCenter.distanceTo(otherTriangle.getCenter());
                    if (distance < (octRadius + otherTriangle.getRadius())) return true;
                } else if (s instanceof Box otherBox) {
                    double distance = octCenter.distanceTo(otherBox.getCenter());
                    if (distance < (octRadius + otherBox.getBoundingRadius())) return true;
                } else if (s instanceof Cylinder otherCylinder) {
                    Point3D cylMid = new Point3D(
                            otherCylinder.center.x,
                            otherCylinder.center.y + otherCylinder.height / 2.0,
                            otherCylinder.center.z
                    );
                    double distance = octCenter.distanceTo(cylMid);
                    if (distance < (octRadius + otherCylinder.radius)) return true;
                }else if (s instanceof Cone otherCone) {
                    Point3D coneBase = otherCone.getCenter();
                    double coneHeight = otherCone.getHeight();
                    double coneRadius = otherCone.getRadius();
                    Point3D coneTip = new Point3D(coneBase.x, coneBase.y, coneBase.z + coneHeight);
                    Point3D coneMid = coneBase.add(coneTip).div(2);
                    double distance = octCenter.distanceTo(coneMid);
                    double boundingRadius = coneRadius;
                    if (distance < (octRadius + boundingRadius)) {
                        double minZ1 = octCenter.z - octRadius;
                        double maxZ1 = octCenter.z + octRadius;
                        double minZ2 = Math.min(coneBase.z, coneTip.z);
                        double maxZ2 = Math.max(coneBase.z, coneTip.z);
                        boolean zOverlap = (minZ1 <= maxZ2 && maxZ1 >= minZ2);
                        if (zOverlap) return true;
                    }
                }
                else if (s instanceof Prism otherPrism) {
                    double distance = octCenter.distanceTo(otherPrism.getCenter());
                    if (distance < (octRadius + otherPrism.getRadius())) return true;
                } else if (s instanceof Octahedron otherOcta) {
                    double distance = octCenter.distanceTo(otherOcta.getCenter());
                    if (distance < (octRadius + otherOcta.getRadius())) return true;
                }
            }
        }

        return false;
    }

}




