package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.Color;
import java.util.function.Consumer;

/**
 * A fruit GameObject that gives an energy bonus to the avatar when collected.
 *
 * The fruit becomes inactive when collected and respawns after a fixed delay.
 * it invokes a callback supplied at creation so the owning game
 * component can apply the energy bonus.
 */
public class Fruit extends GameObject {
    /**
     * Radius (in pixels) of the circular fruit.
     */
    private static final float RADIUS = 10f;

    /**
     * Seconds until the fruit respawns after being collected.
     */
    private static final float RESPAWN_SECONDS = 30f;

    private static final float ENERGY_BONUS = 10f;

    private static final Color FRUIT_COLOR = new Color(220, 40, 40);

    /**
     * The callback receives the energy amount
     * (as a Float) awarded to the avatar.
     */
    private final Consumer<Float> onCollected;

    private boolean active = true;

    /**
     * Creates a new Fruit.
     *
     * @param topLeftCorner the top-left corner position of the fruit in world coordinates.
     * @param onCollected Callback invoked when the fruit is collected, receives the energy bonus.
     */
    public Fruit(Vector2 topLeftCorner, Consumer<Float> onCollected) {
        super(topLeftCorner,
                Vector2.ONES.mult(RADIUS * 2),
                new OvalRenderable(FRUIT_COLOR));
        this.onCollected = onCollected;
        setTag("fruit");
    }

    /**
     * Handles collisions - if an active avatar collides with the fruit, the fruit becomes inactive,
     * invokes the energy callback and schedules a respawn task.
     *
     * @param other the other GameObject involved in the collision.
     * @param collision Collision information
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);

        if (!active) return;
        if (!"avatar".equals(other.getTag())) return;

        active = false;
        renderer().setOpaqueness(0f);

        onCollected.accept(ENERGY_BONUS);

        new ScheduledTask(this, RESPAWN_SECONDS, false, this::respawn);
    }

    /**
     * respawns the fruit, Scheduled internally.
     */
    private void respawn() {
        active = true;
        renderer().setOpaqueness(1f);
    }
}
