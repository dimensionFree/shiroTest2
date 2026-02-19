package com.shiroTest.utils;

import java.util.ArrayList;
import java.util.List;

public class DeleteDataHelper {

    private static final List<Runnable> tasks = new ArrayList<>();

    public static synchronized void addTask(Runnable runnable) {
        tasks.add(runnable);
    }

    public static synchronized void clear() {
        RuntimeException firstException = null;
        try {
            for (int i = tasks.size() - 1; i >= 0; i--) {
                Runnable task = tasks.get(i);
                if (task == null) {
                    continue;
                }
                try {
                    task.run();
                } catch (Exception e) {
                    if (firstException == null) {
                        firstException = new RuntimeException("DeleteDataHelper clear failed", e);
                    } else {
                        firstException.addSuppressed(e);
                    }
                }
            }
        } finally {
            tasks.clear();
        }

        if (firstException != null) {
            throw firstException;
        }
    }
}
