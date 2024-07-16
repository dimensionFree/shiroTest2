package com.shiroTest.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DeleteDataHelper {

    protected static final ExecutorService executorService = Executors.newFixedThreadPool(2);


    static List<Runnable> tasks = new ArrayList<>();


    public static void addTask(Runnable runnable){
        tasks.add(runnable);
    }


    public static void clear(){
        for (Runnable task : tasks) {
            if (task != null) {
                try {
                    // 在finally块中提交并执行任务
                    Future<?> submit = executorService.submit(task);
                    submit.get(); // 等待任务完成
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    executorService.shutdown();
                }
            }
        }
    }



}
