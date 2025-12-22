package com.taha.openrayui.core;

import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

public class Camera {
    private Vec3 origin;
    private Vec3 lowerLeftCorner;
    private Vec3 horizontal;
    private Vec3 vertical;

    // Artık kamerayı oluştururken nereden nereye baktığını söyleyebileceğiz
    public Camera(Vec3 lookFrom, Vec3 lookAt, double vfov, double aspectRatio) {
        double theta = Math.toRadians(vfov);
        double h = Math.tan(theta / 2);
        double viewportHeight = 2.0 * h;
        double viewportWidth = aspectRatio * viewportHeight;

        Vec3 w = lookFrom.sub(lookAt).unitVector(); // Geriye doğru vektör (Kamera Z)
        Vec3 u = new Vec3(0, 1, 0).cross(w).unitVector(); // Sağ vektör (Kamera X)
        Vec3 v = w.cross(u); // Yukarı vektör (Kamera Y)

        origin = lookFrom;
        horizontal = u.mul(viewportWidth);
        vertical = v.mul(viewportHeight);

        // Sol alt köşe hesabı
        lowerLeftCorner = origin
                .sub(horizontal.div(2))
                .sub(vertical.div(2))
                .sub(w);
    }

    public Ray getRay(double s, double t) {
        return new Ray(origin, lowerLeftCorner
                .add(horizontal.mul(s))
                .add(vertical.mul(t))
                .sub(origin));
    }
}