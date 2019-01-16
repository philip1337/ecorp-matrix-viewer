package util;

public abstract class Thread extends java.lang.Thread {
    public void Start() {
        start();
    }

    public void Interrupt() {
        interrupt();
    }

    public void Wait() throws InterruptedException {
        wait();
    }
}
