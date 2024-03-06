package org.apache.eventmesh.dashboard.console.function.client;

public interface ClientCreateOperation<T> {

    public T createClient();

    public void close(T client);
}
