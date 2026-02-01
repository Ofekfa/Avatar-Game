package pepse.world.avatar;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The playable character of the game.
 *
 * The avatar can move left, right, jump and perform a single double-jump while falling
 * it is also manage an energy resource:
 * - Idle on the ground increases energy.
 * - Running on the ground costs energy.
 * - Jumping costs energy .
 *
 * The avatar uses a small states (Idle / Run / Jump)
 */
public class Avatar extends GameObject {

    /**
     * Assets folder path used by the avatar animations.
     */
    private static final String ASSETS_DIR = "assets/";

    /**
     * default image used as the initial renderable
     */
    private static final String IDLE_IMAGE_PATH = ASSETS_DIR + "idle_0.png";

    /**
     * The avatar size (width and height) in pixels
     */
    private static final float AVATAR_SIZE = 50f;

    /**
     * downwards acceleration (gravity) in pixels per second^2
     */
    private static final float GRAVITY = 600f;

    /**
     * Horizontal movement speed in pixels per second
     */
    private static final float VELOCITY_X = 400;

    /**
     * Jump velocity
     */
    private static final float JUMP_VELOCITY_Y = -650;

    /**
     * Energy limits
     */
    private static final float MAX_ENERGY = 100f;
    private static final float MIN_ENERGY = 0f;

    /**
     * Energy change per update:
     * idle gains energy, running on ground costs energy
     */
    private static final float IDLE_GAIN_PER_UPDATE = 1f;
    private static final float RUN_COST_PER_UPDATE = 2f;

    /**
     * Energy thresholds / costs for actions.
     */
    private static final float RUN_THRESHOLD = 2f;
    private static final float JUMP_COST = 20f;
    private static final float DOUBLE_JUMP_COST = 50f;

    /**
     * Animation configuration
     */
    private static final float FRAME_DURATION = 0.12f;
    private static final int IDLE_FRAMES = 4;
    private static final int JUMP_FRAMES = 4;
    private static final int RUN_FRAMES = 6;

    /**
     * Tags used for collision filtering
     */
    private static final String BLOCK_TAG = "block";
    private static final String AVATAR_TAG = "avatar";

    /**
     * energy starting value
     */
    private float energy = MAX_ENERGY;

    /**
     * True after doing a double jump, until the avatar lands again
     */
    private boolean doubleJumpUsed = false;

    /**
     * Used to detect a just pressed space press.
     */
    private boolean spacePressedLastUpdate = false;

    /**
     * used for flipping the renderable when moving left/right
     */
    private boolean facingLeft = false;


    private final UserInputListener inputListener;
    private AvatarState currentState;

    private final List<Consumer<Float>> energyListeners = new ArrayList<>();

    private final AnimationRenderable idleAnimation;
    private final AnimationRenderable runAnimation;
    private final AnimationRenderable jumpAnimation;

    /**
     * Creates a new avatar.
     *
     * @param topLeftCorner The starting top-left position (world coordinates)
     * @param inputListener used for reading keyboard input
     * @param imageReader used for loading avatar animation frames.
     */
    public Avatar(Vector2 topLeftCorner,
                  UserInputListener inputListener,
                  ImageReader imageReader) {

        super(topLeftCorner,
                Vector2.ONES.mult(AVATAR_SIZE),
                imageReader.readImage(IDLE_IMAGE_PATH, true));

        this.inputListener = inputListener;

        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        transform().setAccelerationY(GRAVITY);

        setTag(AVATAR_TAG);

        idleAnimation = loadAnimation(imageReader, "idle_", IDLE_FRAMES);
        runAnimation = loadAnimation(imageReader, "run_", RUN_FRAMES);
        jumpAnimation = loadAnimation(imageReader, "jump_", JUMP_FRAMES);

        this.currentState = new IdleState();
        this.currentState.onEnter(this);


    }

    /**
     * @return the avatar size (width and height)
     */
    public static float getAvatarSize() {
        return AVATAR_SIZE;
    }

    /**
     * Updates the avatar each frame
     * handle state transitions and delegates behavior to the active state.
     *
     * @param deltaTime time passed since last update (seconds)
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (isOnGround()) {
            doubleJumpUsed = false;
        }

        AvatarState nextState = chooseNextState();
        if (!nextState.getClass().equals(currentState.getClass())) {
            currentState.onExit(this);
            currentState = nextState;
            currentState.onEnter(this);
        }

        currentState.update(this, deltaTime);
        spacePressedLastUpdate = inputListener.isKeyPressed(KeyEvent.VK_SPACE);
    }

    /**
     * Chooses the next state based on whether the avatar is on the ground and on the input
     * The decision is - Jump if in air, run if on ground and exactly one arrow pressed,
     * otherwise Idle.
     */
    private AvatarState chooseNextState() {
        if (!isOnGround()) {
            return new JumpState();
        }

        boolean left = isLeftPressed();
        boolean right = isRightPressed();
        boolean exactlyOne = (left != right);

        if (exactlyOne && energy >= RUN_THRESHOLD) {
            return new RunState();
        }
        return new IdleState();
    }


    private boolean isLeftPressed() {
        return inputListener.isKeyPressed(KeyEvent.VK_LEFT);
    }

    private boolean isRightPressed() {
        return inputListener.isKeyPressed(KeyEvent.VK_RIGHT);
    }

    /**
     * returns true only on the transition from not pressed to pressed
     */
    private boolean isSpaceJustPressed() {
        boolean now = inputListener.isKeyPressed(KeyEvent.VK_SPACE);
        return now && !spacePressedLastUpdate;
    }

    /**
     * treat on ground as zero vertical velocity.
     */
    private boolean isOnGround() {
        return getVelocity().y() == 0;
    }

    /**
     * Handles collisions with ground blocks
     * If the avatar is falling onto a block, stop the fall and put the avatar to sit on top
     * of the block (prevents sinking into terrain)
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);

        if (!BLOCK_TAG.equals(other.getTag())) return;
        if (!isFalling()) return;

        if (collision.getNormal().y() < 0) {
            transform().setVelocityY(0);

            float newY = other.getTopLeftCorner().y() - getDimensions().y();
            setTopLeftCorner(new Vector2(getTopLeftCorner().x(), newY));
        }
    }

    private boolean isFalling() {
        return getVelocity().y() > 0;
    }

    private void setEnergy(float newEnergy) {
        float clamped = Math.max(MIN_ENERGY, Math.min(MAX_ENERGY, newEnergy));
        if (clamped != energy) {
            energy = clamped;
            notifyEnergyChanged();
        }
    }

    private void spendEnergy(float amount) {
        setEnergy(energy - amount);
    }

    private void addEnergy(float amount) {
        setEnergy(energy + amount);
    }

    /**
     * entry point used by other parts of the game (like fruits).
     *
     * @param amount amount of energy to add
     */
    public void increaseEnergy(float amount) {
        addEnergy(amount);
    }

    private void notifyEnergyChanged() {
        for (Consumer<Float> listener : energyListeners) {
            listener.accept(energy);
        }
    }

    /**
     * adds a listener that is called when the energy change
     * the listener also receives the current energy immediately
     *
     * @param listener Callback that receives the new energy value.
     */
    public void addEnergyChangedListener(Consumer<Float> listener) {
        energyListeners.add(listener);
        listener.accept(energy);
    }

    private AnimationRenderable loadAnimation(ImageReader imageReader, String prefix, int frameCount) {
        Renderable[] frames = new Renderable[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = imageReader.readImage(ASSETS_DIR + prefix + i + ".png", true);
        }
        return new AnimationRenderable(frames, FRAME_DURATION);
    }

    private void updateFacing(float xVel) {
        if (xVel < 0) {
            facingLeft = true;
        } else if (xVel > 0) {
            facingLeft = false;
        }
        renderer().setIsFlippedHorizontally(facingLeft);
    }

    /**
     * internal interface for the avatar state.
     */
    private interface AvatarState {
        void onEnter(Avatar avatar);
        void onExit(Avatar avatar);
        void update(Avatar avatar, float deltaTime);
    }

    /**
     * Idle state - the avatar stands still, while on ground, energy increases.
     * From here, the avatar can perform a regular jump if he have enough energy
     */
    private static class IdleState implements AvatarState {

        /**
         * switches the avatar renderable to the idle animation
         *
         * @param avatar the avatar object being controlled
         */
        @Override
        public void onEnter(Avatar avatar) {
            avatar.renderer().setRenderable(avatar.idleAnimation);
        }

        /**
         * no cleanup is required when leaving idle
         *
         * @param avatar the avatar object being controlled.
         */
        @Override
        public void onExit(Avatar avatar) { }

        /**
         * keeps the avatar still, gains energy while standing,
         * and allows a regular jump if the space key was just pressed and enough energy exists
         *
         * @param avatar the avatar object being controlled.
         * @param deltaTime time passed since last update (seconds).
         */
        @Override
        public void update(Avatar avatar, float deltaTime) {
            avatar.transform().setVelocityX(0);

            if (avatar.isOnGround()) {
                avatar.addEnergy(IDLE_GAIN_PER_UPDATE);
            }

            if (avatar.isOnGround()
                    && avatar.isSpaceJustPressed()
                    && avatar.energy >= JUMP_COST) {
                avatar.spendEnergy(JUMP_COST);
                avatar.transform().setVelocityY(JUMP_VELOCITY_Y);
            }
        }
    }

    /**
     * Run state - the avatar moves left/right
     * while on ground, running costs energy each update, in the air horizontal control is free
     */
    private static class RunState implements AvatarState {

        /**
         * switches the avatar renderable to the running animation.
         *
         * @param avatar The avatar object being controlled.
         */
        @Override
        public void onEnter(Avatar avatar) {
            avatar.renderer().setRenderable(avatar.runAnimation);
        }

        /**
         * no cleanup is required when leaving run.
         *
         * @param avatar The avatar object being controlled.
         */
        @Override
        public void onExit(Avatar avatar) { }

        /**
         * Moves the avatar left/right.
         * On the ground, movement costs energy per update.in the air, is free
         * Also allows a regular jump when on the ground.
         *
         * @param avatar The avatar object being controlled.
         * @param deltaTime Time passed since last update (seconds).
         */
        @Override
        public void update(Avatar avatar, float deltaTime) {
            boolean left = avatar.isLeftPressed();
            boolean right = avatar.isRightPressed();

            float xVel = 0f;
            if (left) {
                xVel -= VELOCITY_X;
            }
            if (right) {
                xVel += VELOCITY_X;
            }

            if (xVel == 0f) {
                avatar.transform().setVelocityX(0);
            } else {
                if (avatar.isOnGround()) {
                    if (avatar.energy >= RUN_COST_PER_UPDATE) {
                        avatar.transform().setVelocityX(xVel);
                        avatar.updateFacing(xVel);
                        avatar.spendEnergy(RUN_COST_PER_UPDATE);
                    } else {
                        avatar.transform().setVelocityX(0);
                    }
                } else {
                    avatar.transform().setVelocityX(xVel);
                    avatar.updateFacing(xVel);
                }
            }
            if (avatar.isOnGround()
                    && avatar.isSpaceJustPressed()
                    && avatar.energy >= JUMP_COST) {
                avatar.spendEnergy(JUMP_COST);
                avatar.transform().setVelocityY(JUMP_VELOCITY_Y);
            }
        }
    }

    /**
     * Jump stat -: the avatar is in the air
     */
    private static class JumpState implements AvatarState {

        /**
         * Switches the avatar renderable to the jumping animation.
         *
         * @param avatar The avatar object being controlled
         */
        @Override
        public void onEnter(Avatar avatar) {
            avatar.renderer().setRenderable(avatar.jumpAnimation);
        }

        /**
         * no cleanup is required when leaving jump.
         *
         * @param avatar The avatar object being controlled
         */
        @Override
        public void onExit(Avatar avatar) { }

        /**
         * while in the air, allows horizontal control and a single double jump while falling
         * (if enough energy exists)
         *
         * @param avatar The avatar object being controlled.
         * @param deltaTime Time passed since last update (seconds).
         */
        @Override
        public void update(Avatar avatar, float deltaTime) {
            float xVel = 0f;
            if (avatar.isLeftPressed()) {
                xVel -= VELOCITY_X;
            }
            if (avatar.isRightPressed()) {
                xVel += VELOCITY_X;
            }
            avatar.transform().setVelocityX(xVel);

            Renderable current = avatar.renderer().getRenderable();

            if (xVel != 0f && current != avatar.runAnimation) {
                avatar.renderer().setRenderable(avatar.runAnimation);
                avatar.updateFacing(xVel);
            }
            if (xVel == 0f && current != avatar.jumpAnimation) {
                avatar.renderer().setRenderable(avatar.jumpAnimation);
            }

            if (!avatar.isOnGround()
                    && avatar.isFalling()
                    && !avatar.doubleJumpUsed
                    && avatar.isSpaceJustPressed()
                    && avatar.energy >= DOUBLE_JUMP_COST) {

                avatar.spendEnergy(DOUBLE_JUMP_COST);
                avatar.doubleJumpUsed = true;
                avatar.transform().setVelocityY(JUMP_VELOCITY_Y);
            }
        }
    }
}
