package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Creates a halo around the sun
 * The halo is a semi-transparent circle that follows the sun's every frame.
 */
public class SunHalo {

    /**
     * the halo is twice the sun's dimensions.
     */
    private static final float HALO_SCALE = 2f;

    /**
     * The halo color
     */
    private static final Color HALO_COLOR = new Color(255, 255, 0, 20);

    /**
     * create halo that follows the given sun object.
     *
     * @param sun The sun GameObject to follow
     * @return a {@link GameObject} representing the halo.
     */
    public static GameObject create(GameObject sun) {
        Vector2 haloSize = sun.getDimensions().mult(HALO_SCALE);

        GameObject halo = new GameObject(
                Vector2.ZERO,
                haloSize,
                new OvalRenderable(HALO_COLOR)
        );
        halo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        halo.setTag("sunHalo");

        halo.addComponent(deltaTime -> {
            halo.setCenter(sun.getCenter());
        });
        return halo;
    }
}
