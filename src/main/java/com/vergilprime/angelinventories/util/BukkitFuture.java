package com.vergilprime.angelinventories.util;

import com.vergilprime.angelinventories.AngelInventories;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class BukkitFuture<V> {

    private Object lock = new Object();
    private V value;

    public BukkitFuture(boolean sync, Supplier<V> supplier) {
        this(sync, false, supplier);
    }

    public BukkitFuture(boolean sync, boolean instantly, Supplier<V> supplier) {
        Runnable task = () -> {
            value = supplier.get();
            synchronized (lock) {
                lock.notifyAll();
            }
        };
        if (instantly && sync == Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            run(sync, task);
        }
    }

    private void wait(Object obj) {
        try {
            synchronized (obj) {
                obj.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public <T> BukkitFuture<T> then(boolean sync, Function<V, T> func) {
        return new BukkitFuture(false, () -> {
            if (value == null) {
                wait(lock);
            }
            if (sync) {
                List<T> next = new ArrayList<>(1);
                run(true, () -> {
                    next.add(func.apply(value));
                    next.notify();
                });
                wait(next);
                return next.get(0);
            } else {
                return func.apply(value);
            }
        });
    }

    public <T> BukkitFuture<T> thenSync(Function<V, T> func) {
        return then(true, func);
    }

    public <T> BukkitFuture<T> thenAsync(Function<V, T> func) {
        return then(false, func);
    }

    public static <V> BukkitFuture<V> sync(Supplier<V> s) {
        return new BukkitFuture(true, s);
    }

    public static <V> BukkitFuture<V> async(Supplier<V> s) {
        return new BukkitFuture(false, s);
    }

    public static void run(boolean sync, Runnable task) {
        if (AngelInventories.isDisabling()) {
            task.run();
        } else {
            if (sync) {
                Bukkit.getScheduler().runTask(AngelInventories.getInstance(), task);
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(AngelInventories.getInstance(), task);
            }
        }
    }

}
