package com.taha.openrayui.material;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.core.ScatterResult;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.texture.SolidColor;
import com.taha.openrayui.texture.Texture;

/**
 * A material that simulates a matte surface (diffuse reflection).
 * <p>
 * UPDATED ARCHITECTURE:
 * This material now supports both a {@link Texture} and a {@link Vec3} color tint.
 * The final color output is calculated as: {@code FinalColor = TextureColor * TintColor}.
 * This allows users to apply color filters to textures (e.g., making a brick wall redder)
 * or simply use a solid color if the texture is white.
 * </p>
 */
public class Lambertian implements Material {

    private Texture texture;   // The surface pattern (image, checker, or solid color)
    private Vec3 colorTint;    // The color filter multiplier (Default is White: 1,1,1)

    /**
     * Constructs a Lambertian material with a solid color.
     * The underlying texture is set to White so the tint color controls the appearance.
     * @param color The albedo color.
     */
    public Lambertian(Vec3 color) {
        this.colorTint = color;
        this.texture = new SolidColor(new Vec3(1, 1, 1)); // White texture (neutral)
    }

    /**
     * Constructs a Lambertian material with a specific texture.
     * The tint color is set to White (1,1,1) to show the texture's original colors.
     * @param texture The texture to apply.
     */
    public Lambertian(Texture texture) {
        this.texture = texture;
        this.colorTint = new Vec3(1, 1, 1); // White tint (neutral)
    }

    // --- Accessors for UI Binding ---

    public Vec3 getColor() {
        return colorTint;
    }

    public void setColor(Vec3 color) {
        this.colorTint = color;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public ScatterResult scatter(Ray rIn, HitRecord rec) {
        // Calculate a random reflection direction for diffuse scattering
        Vec3 scatterDirection = rec.normal.add(Vec3.randomUnitVector());

        // Catch degenerate scatter direction (if random vector is opposite to normal)
        if (nearZero(scatterDirection)) {
            scatterDirection = rec.normal;
        }

        Ray scattered = new Ray(rec.p, scatterDirection);

        // --- COLOR CALCULATION ---
        // 1. Sample the texture at the UV coordinates.
        Vec3 textureColor = texture.value(rec.u, rec.v, rec.p);

        // 2. Multiply by the tint color.
        // If tint is White (1,1,1), the result is the original texture color.
        Vec3 attenuation = textureColor.mul(colorTint);

        return new ScatterResult(scattered, attenuation);
    }

    /**
     * Checks if a vector is very close to zero to prevent NaN errors.
     */
    private boolean nearZero(Vec3 v) {
        double s = 1e-8;
        return (Math.abs(v.x) < s) && (Math.abs(v.y) < s) && (Math.abs(v.z) < s);
    }
}
