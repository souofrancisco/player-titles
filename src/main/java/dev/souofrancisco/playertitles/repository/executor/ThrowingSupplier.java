package dev.souofrancisco.playertitles.repository.executor;

@FunctionalInterface
public interface ThrowingSupplier<T> {

    T get() throws Exception;
}