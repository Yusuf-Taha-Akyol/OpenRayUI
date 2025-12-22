package com.taha.openrayui.core;

import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * A data carrier class that holds the result of a light scattering event.
 */
public class ScatterResult {
    public final Ray scattered;     // The new ray traveling after the bounce
    public final Vec3 attenuation;  // How much the color is absorbed/attenuated

    public ScatterResult(Ray scattered, Vec3 attenuation) {
        this.scattered = scattered;
        this.attenuation = attenuation;
    }
}