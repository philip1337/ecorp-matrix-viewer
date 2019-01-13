package util;

public abstract class Thread extends java.lang.Thread {
    public void Start() {
        start();
    }

    public void Interrupt() {
        interrupt();
    }
}
