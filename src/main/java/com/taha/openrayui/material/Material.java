package com.taha.openrayui.material;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.core.ScatterResult;
import com.taha.openrayui.math.Ray;

/**
 * Interface for materials. Defines how rays interact with surfaces.
 */
public interface Material {
    /**
     * Calculates how a ray scatters when hitting this material.
     * @param rIn The incoming ray
     * @param rec The hit record containing geometric details
     * @return A ScatterResult if the ray reflects/refracts, or null if absorbed.
     */
    ScatterResult scatter(Ray rIn, HitRecord rec);
}
