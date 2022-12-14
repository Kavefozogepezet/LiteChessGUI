package me.lcgui.misc;

import java.io.Serializable;
import java.util.EventListener;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Egy argumentumos eseményt ábrázoló osztály.
 * Az eseményre hallgatózók tájékoztatást kapnak az esemény meghívásakor.
 * @param <Type> Az argumentum típusa.
 */
public class Event<Type> implements Serializable {
    public interface Listener<Type> extends Serializable, EventListener {
        void invoked(Type data);
    }

    private final HashSet<Listener<Type>> listeners = new HashSet<>();
    private boolean iterationLock = false;

    private final LinkedList<Listener<Type>> deferredAdditions = new LinkedList<>();
    private final LinkedList<Listener<Type>> deferredRemovals = new LinkedList<>();

    /**
     * Hozzáad egy hallgatózót az eseményhez.
     * @param listener A hallgatózó objektum.
     */
    public synchronized void addListener(Listener<Type> listener) {
        if(iterationLock)
            deferredAdditions.add(listener);
        else
            listeners.add(listener);
    }

    /**
     * Eltávolít egy hallgatózó objektumot..
     * @param listener A hallgatózó objektum.
     */
    public synchronized void removeListener(Listener<Type> listener) {
        if(iterationLock)
            deferredRemovals.add(listener);
        else
            listeners.remove(listener);
    }

    /**
     * Meghívja az eseményt, értesíti a halldatózó objektumokat.
     * @param data Az esemény argumentuma.
     */
    public synchronized void invoke(Type data) {
        iterationLock = true;
        for(var listener : listeners)
            listener.invoked(data);

        iterationLock = false;
        executeDeferredOperations();
    }

    private void executeDeferredOperations() {
        listeners.addAll(deferredAdditions);
        deferredRemovals.forEach(listeners::remove);

        deferredAdditions.clear();
        deferredRemovals.clear();
    }
}
