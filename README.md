# Java Keyboard Hooks for Windows

This library provides a JNA based solution for installing keyboard hooks on windows systems.

The library is fairly trivial, just create an instance of the KeyHook class, call start() and add listeners

Example program:

    public static void main(String[] args) throws InterruptedException {
        KeyHook hook = new KeyHook().start().addListener(System.out::println);
        Thread.sleep(10000);
        hook.stop();
    }
