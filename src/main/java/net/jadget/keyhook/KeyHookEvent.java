package net.jadget.keyhook;


public class KeyHookEvent {
    /**
     * Windows virtual key code, see {@link KeyHookKeyCodes} for a list of
     * constants.
     */
    public final int vkCode;

    /**
     * Hardware scan code.
     */
    public final int scanCode;

    /**
     * State of the key, {@code true} for key down events,
     * {@code false} for key up events
     */
    public final boolean isDown;



    public KeyHookEvent(int vkCode, int scanCode, boolean isDown) {
        this.vkCode = vkCode;
        this.scanCode = scanCode;
        this.isDown = isDown;
    }


    @Override
    public String toString() {
        return "KeyEvent(code: " + vkCode + ", state: " + (isDown ? "down" : "up") + ")";
    }
}
