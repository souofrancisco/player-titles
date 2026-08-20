package dev.souofrancisco.playertitles.repository.executor;

@FunctionalInterface
public interface ThrowingRunnable {

    void run() throws Exception;
}
