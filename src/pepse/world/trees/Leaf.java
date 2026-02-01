package pepse.world.trees;

import danogl.GameObject;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.world.Block;

import java.awt.*;
import java.util.Random;

/**
 * A single leaf GameObject that move in the wind
 *
 * each leaf is a square the size of a {@link Block} and uses scheduled and transition components
 * to create a continuous move animation. The animation starts after a small randomized delay
 * so neighboring leaves are out of phase
 */
public class Leaf extends GameObject {
    private static final Color LEAF_COLOR = new Color(50, 200, 30);
    private static final float MAX_INITIAL_DELAY = 0.5f;

    /**
     * duration (seconds) of a full wind move cycle.
     */
    private static final float WIND_CYCLE_SECONDS = 2.0f;

    private static final float MAX_ANGLE_DEG = 6f;
    private static final float MIN_WIDTH_FACTOR = 0.995f;
    private static final float MAX_WIDTH_FACTOR = 1.005f;

    /**
     * The original leaf dimensions used as a baseline when scaling
     */
    private final Vector2 baseDimensions;

    /**
     * Creates a new Leaf.
     *
     * the leaf is positioned at the given top-left corner and will start a wind animation after a
     * small random delay so adjacent leaves are not synchronized.
     *
     * @param topLeftCorner The top-left corner position of the leaf in world coordinates.
     * @param random random instance used to generate the initial delay and slight variation
     */
    public Leaf(Vector2 topLeftCorner, Random random) {
        super(topLeftCorner,
                Vector2.ONES.mult(Block.SIZE),
                new RectangleRenderable(LEAF_COLOR));

        setTag("leaf");
        baseDimensions = getDimensions();

        float delay = random.nextFloat() * MAX_INITIAL_DELAY;
        new ScheduledTask(this, delay, false, () -> startWind(random));
    }

    /**
     * Starts the wind movement animation for leaf
     *
     * The animation consists of two transitions running in parallel:
     * - rotation between -MAX_ANGLE_DEG and +MAX_ANGLE_DEG
     * - horizontal scaling between MIN_WIDTH_FACTOR and MAX_WIDTH_FACTOR
     *
     * @param random Random instance used to introduce a slight variation in the scaling cycle duration.
     */
    private void startWind(Random random) {
        float angleA = -MAX_ANGLE_DEG;
        float angleB = MAX_ANGLE_DEG;

        new Transition<>(
                this,
                this.renderer()::setRenderableAngle,
                angleA,
                angleB,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                WIND_CYCLE_SECONDS,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );

        new Transition<>(
                this,
                (Float factor) -> setDimensions(new Vector2(baseDimensions.x() * factor, baseDimensions.y())),
                MIN_WIDTH_FACTOR,
                MAX_WIDTH_FACTOR,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                WIND_CYCLE_SECONDS + random.nextFloat(),
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
    }
}
