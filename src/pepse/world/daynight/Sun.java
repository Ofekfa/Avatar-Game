package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.world.Terrain;

import java.awt.Color;

/**
 * Creates a sun and animates it along a circul path.
 */
public class Sun {

    /**
     * The diameter of the sun
     */
    private static final float SUN_DIAMETER = 80f;

    /**
     * The starting and ending angles of the sun's orbit
     */
    private static final float INITIAL_ANGLE = 0f;
    private static final float FINAL_ANGLE = 360f;

    /**
     * Creates the sun GameObject and makes it move in a circular path
     * the orbit is centered at the middle of the horizon line
     *
     * @param windowDimensions The window dimensions in pixels.
     * @param cycleLength The length in seconds of a day cycle.
     * @return A {@link GameObject} representing the sun.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        OvalRenderable renderable = new OvalRenderable(Color.YELLOW);
        GameObject sun = new GameObject(Vector2.ZERO,
                new Vector2(SUN_DIAMETER, SUN_DIAMETER),
                renderable);
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag("sun");

        float groundHeightAtX0 = windowDimensions.y() * Terrain.BASE_GROUND_Y_FACTOR;
        Vector2 cycleCenter = new Vector2(windowDimensions.x() / 2f, groundHeightAtX0);

        float orbitRadius = windowDimensions.y() * (1f - Terrain.BASE_GROUND_Y_FACTOR);
        Vector2 initialSunCenter = cycleCenter.add(new Vector2(0f, -orbitRadius));
        sun.setCenter(initialSunCenter);

        new Transition<>(
                sun,
                (Float angle) -> sun.setCenter(
                        initialSunCenter
                                .subtract(cycleCenter)
                                .rotated(angle)
                                .add(cycleCenter)
                ),
                INITIAL_ANGLE,
                FINAL_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );

        return sun;
    }
}
