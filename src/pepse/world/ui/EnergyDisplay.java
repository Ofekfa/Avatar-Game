package pepse.world.ui;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

/**
 * A UI that shows the avatar's current energy on the screen
 *
 * This object is rendered in camera coordinates, so it stays fixed in the top-left corner.
 * the display is being updated via a callback from the avatar, only when energy changes.
 */
public class EnergyDisplay extends GameObject {
    private static final String TAG = "energyDisplay";
    private static final Vector2 TOP_LEFT = new Vector2(10f, 10f);
    private static final Vector2 DIMENSIONS = new Vector2(220f, 30f);

    private final TextRenderable textRenderable;
    private float lastEnergy = Float.NaN;

    /**
     * Creates the energy display
     */
    public EnergyDisplay() {
        super(TOP_LEFT, DIMENSIONS, new TextRenderable("Energy: "));
        setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        setTag(TAG);

        this.textRenderable = (TextRenderable) renderer().getRenderable();
    }

    /**
     * Updates the displayed energy value.
     * this method designed to be used as a callback
     *
     * @param newEnergy the new energy value to show.
     */
    public void updateEnergy(float newEnergy) {
        if (newEnergy == lastEnergy) {
            return;
        }
        lastEnergy = newEnergy;
        textRenderable.setString("Energy: " + (int) newEnergy);
    }
}
