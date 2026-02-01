package pepse.world.trees;

import danogl.GameObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A container bundling the GameObjects that make up flora created in a range.
 *
 * A {@link FloraPack} contains separate lists for tree trunks, leaves and fruits so callers
 * can add each group to the correct game layer
 */
public class FloraPack {
    private final List<GameObject> trunks;
    private final List<GameObject> leaves;
    private final List<GameObject> fruits;

    /**
     * Creates a new FloraPack.
     * Creates a new FloraPack.
     *
     * @param trunks The trunk GameObjects
     * @param leaves The leaf GameObjects.
     * @param fruits The fruit GameObjects.
     */
    public FloraPack(List<GameObject> trunks,
                     List<GameObject> leaves,
                     List<GameObject> fruits) {
        this.trunks = trunks;
        this.leaves = leaves;
        this.fruits = fruits;
    }

    /**
     * @return A copy of the trunks list
     */
    public List<GameObject> getTrunks() {
        return new ArrayList<>(trunks);
    }

    /**
     * @return A copy of the leaves list
     */
    public List<GameObject> getLeaves() {
        return new ArrayList<>(leaves);
    }

    /**
     * @return A copy of the fruits list
     */
    public List<GameObject> getFruits() {
        return new ArrayList<>(fruits);
    }
}
