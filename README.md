[ ![Bintray](https://api.bintray.com/packages/jadget/jadget-keyhook/jadget-keyhook/images/download.svg) ](https://bintray.com/jadget/jadget-keyhook/jadget-keyhook/_latestVersion)

# Java Keyboard Hooks for Windows

This library provides a JNA based solution for installing keyboard hooks on windows systems.

Dependency (Gradle)

    compile 'net.jadget:jadget-keyhook:1.0.1'


The library is fairly trivial, just create an instance of the KeyHook class, call start() and add listeners

Example program:

    public static void main(String[] args) throws InterruptedException {
        KeyHook hook = new KeyHook().start().addListener(System.out::println);
        Thread.sleep(10000);
        hook.stop();
    }


For more details please consult the javadocs
