package net.jadget.keyhook;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HOOKPROC;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


/**
 * The KeyHook class allows receiving keyboard events via a global
 * hook, i.e., even if a window from another application has the keyboard focus.
 * <p>
 *
 * This class is thread safe
 * <p>
 *
 * <b>This class will only work on Windows systems!</b>
 * <p>
 *
 * This class uses a background thread for receiving events. After the thread
 * has been started via {@link #start()}, any listeners registered via
 * {@link #addListener(Consumer)} will be notified of key events. After use,
 * the background thread must be stopped via {@link #stop()} or it will block
 * vm shutdown.
 * <p>
 *
 * Simple demo program:
 * <pre>
 * {@code
 * public static void main(String[] args) throws InterruptedException {
 *     KeyHook hook = new KeyHook().start().addListener(System.out::println);
 *     Thread.sleep(10000);
 *     hook.stop();
 * }
 * </pre>
 */
public class KeyHook {
    private static final WinUser.HHOOK DUMMY = new WinUser.HHOOK();

    public KeyHook() {
        m_thread = new Thread(this::run);
    }


    /**
     * Starts capturing keyboard events.
     * <p>
     *
     * Call {@link #stop()} to stop capturing events. Caution: this method will
     * start a background thread that will block vm shutdown if {@link #stop()}
     * is not called!
     * <p>
     *
     * This method will block until the background thread is started, what is
     * normally a negligible amount of time.
     *
     * @return this instance, never null
     * @throws KeyHookException if an error occurs
     */
    public KeyHook start() throws KeyHookException {
        m_thread.start();
        try {
            m_startupLatch.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new KeyHookException("thread interrupted on startup");
        }

        if (!m_ready.get()) {
            throw new KeyHookException("could not install windows key hook");
        }

        return this;
    }


    /**
     * Adds a new listener for key events.
     * <p>
     *
     * This method may be called from any thread. Listeners are notified from a
     * shared background thread and must return quickly.
     *
     * @param listener the listener to add, must not be null
     * @return this instance, never null
     */
    public KeyHook addListener(Consumer<KeyHookEvent> listener) {
        m_listeners.add(Objects.requireNonNull(listener, "listener must not be null"));

        return this;
    }


    /**
     * Stops capturing key events.
     *
     * @return this instance, never null
     */
    public KeyHook stop() {
        if (m_ready.get()) {
            User32.INSTANCE.PostThreadMessage(m_windowsThreadId.get(), User32.WM_QUIT, null, null);
        }

        return this;
    }


    /**
     * Returns the background thread used by this class.
     * <p>
     *
     * Provided in case the application wants to sync with the thread lifecycle
     * via {@code join()} or similar use cases.
     *
     * @return the thread, never null
     */
    public Thread getThread() {
        return m_thread;
    }


    private void run() {
        User32.HHOOK hHook;

        try {
            HOOKPROC hookProc = new HOOKPROC() {
                public LRESULT callback(int nCode, WPARAM wParam, User32.KBDLLHOOKSTRUCT hookStruct) {
                    KeyHookEvent event = null;

                    switch (wParam.intValue()) {
                        case User32.WM_KEYDOWN:
                            event = new KeyHookEvent(hookStruct.vkCode, hookStruct.scanCode, true);
                            break;
                        case User32.WM_KEYUP:
                            event = new KeyHookEvent(hookStruct.vkCode, hookStruct.scanCode, false);
                            break;
                    }

                    if (null != event) {
                        for (Consumer<KeyHookEvent> listener : m_listeners) {
                            listener.accept(event);
                        }
                    }

                    return User32.INSTANCE.CallNextHookEx(DUMMY, nCode, wParam,
                            new WinDef.LPARAM(Pointer.nativeValue(hookStruct.getPointer())));
                }
            };

            HINSTANCE hInst = Kernel32.INSTANCE.GetModuleHandle(null);
            hHook = User32.INSTANCE.SetWindowsHookEx(User32.WH_KEYBOARD_LL, hookProc, hInst, 0);

            if (hHook == null) {
                return;
            }

            m_windowsThreadId.set(Kernel32.INSTANCE.GetCurrentThreadId());
            m_ready.set(true);
        }
        finally {
            m_startupLatch.countDown();
        }

        User32.MSG msg = new User32.MSG();
        while (User32.INSTANCE.GetMessage(msg, null, 0, 0) > 0) {
        }

        User32.INSTANCE.UnhookWindowsHookEx(hHook);
    }


    private final AtomicInteger m_windowsThreadId = new AtomicInteger();
    private final AtomicBoolean m_ready = new AtomicBoolean(false);
    private final CountDownLatch m_startupLatch = new CountDownLatch(1);
    private final Collection<Consumer<KeyHookEvent>> m_listeners = new CopyOnWriteArrayList<>();
    private final Thread m_thread;
}
