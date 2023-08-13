package com.mall.search.thread;

import lombok.SneakyThrows;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName: ThreadTest
 * @Description:
 * @Author: L
 * @Create: 2023-08-11 14:15
 * @Version: 1.0
 */
public class ThreadTest {
    /**
     * 创建多线程的集中方式
     * 1 继承 Tread
     * 2 实现 Runnable 接口
     * 3 实现Callable 接口 + FutureTask (可以拿到返回结果，可以处理异常)
     * 4 线程池
     * 区别
     *  1,2 不能获取到返回值 3 可以获取返回值
     *  1,2,3 都不能控制系统的资源
     *  4 可以控制系统的资源，系统性能稳定
     */
    public static ExecutorService executorService = Executors.newFixedThreadPool(10);

//    @SneakyThrows
//    public static void main(String[] args) {
//        System.out.println("-----main-----start:"+Thread.currentThread().getName());
//        //1 继承 Tread
////        Thread01 thread01 = new Thread01();
////        thread01.start();
//        //实现 Runnable 接口
////        Thread02 thread02 = new Thread02();
////        new Thread(thread02).start();
//        //3 实现Callable 接口 + FutureTask (可以拿到返回结果，可以处理异常)
////        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
////        new Thread(futureTask).start();
////        //FutureTask.get() 等待线程执行完成获取返回结果
////        Integer integer = futureTask.get();
////        System.out.println("线程执行结果："+integer);
//        //4 线程池  直接给线程池提交任务
//        //当前系统的线程池 有一个或者几个，每个异步任务都提交给线程池
//        ThreadPoolExecutor executor = new ThreadPoolExecutor(
//                5,
//                200,
//                10,
//                TimeUnit.SECONDS,
//                new LinkedBlockingDeque<>(100000),
//                Executors.defaultThreadFactory(),
//                new ThreadPoolExecutor.AbortPolicy());
//        /**
//         * 七大参数
//         * corePoolSize:[5] 核心线程数[一直存在除非（allowCoreThreadTimeOut）]; 线程池，创建好以后就准备就绪的线程数量，就等待来接受异步任务去执行。
//         *        5个  Thread thread = new Thread();  thread.start();
//         * maximumPoolSize:[200] 最大线程数量;  控制资源
//         * keepAliveTime:存活时间。如果当前的线程数量大于core数量。
//         *      释放空闲的线程（maximumPoolSize-corePoolSize）。只要线程空闲大于指定的keepAliveTime；
//         * unit:时间单位
//         * BlockingQueue<Runnable> workQueue:阻塞队列。如果任务有很多，就会将目前多的任务放在队列里面。
//         *              只要有线程空闲，就会去队列里面取出新的任务继续执行。
//         * threadFactory:线程的创建工厂。
//         * RejectedExecutionHandler handler:如果队列满了，按照我们指定的拒绝策略拒绝执行任务
//         * 工作顺序:
//         * 1)、线程池创建，准备好core数量的核心线程，准备接受任务
//         * 1.1、core满了，就将再进来的任务放入阻塞队列中。空闲的core就会自己去阻塞队列获取任务执行
//         * 1.2、阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量
//         * 1.3、max满了就用RejectedExecutionHandler拒绝任务
//         * 1.4、max都执行完成，有很多空闲.在指定的时间keepAliveTime以后，释放max-core这些线程
//         *      new LinkedBlockingDeque<>()：默认是Integer的最大值。内存不够
//         * 问题：一个线程池 core 7； max 20 ，queue：50，100并发进来怎么分配的；
//         *      7个会立即得到执行，50个会进入队列，再开(maximumPoolSize - corePoolSize)13个进行执行。剩下的30个就使用拒绝策略。
//         * 如果不想抛弃还要执行。CallerRunsPolicy；
//         */
////        Executors.newCachedThreadPool() 核心core是0，所有都可回收
////        Executors.newFixedThreadPool() 固定大小，核心core=max；都不可回收
////        Executors.newScheduledThreadPool() 定时任务的线程池
////        Executors.newSingleThreadExecutor() 单线程的线程池，后台从队列里面获取任务，挨个执行
//
//        System.out.println("-----main-----end:"+Thread.currentThread().getId());
//    }
    public static class Thread01 extends Thread {
        @Override
        public void run(){
            System.out.println("当前线程："+Thread.currentThread().getName()+"="+Thread.currentThread().getId());
            int i = 10/2;
            System.out.println("运行结果："+i);
        }

    }

    public static class Thread02 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程："+Thread.currentThread().getName()+"="+Thread.currentThread().getId());
            int i = 20/2;
            System.out.println("运行结果："+i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程："+Thread.currentThread().getName()+"="+Thread.currentThread().getId());
            int i = 30/2;
            System.out.println("运行结果："+i);
            return i;
        }
    }

    //-----------------------------------CompletableFuture以下测试----------------------------------------------------------------------------
    @SneakyThrows
    public static void main(String[] args) {
        System.out.println("-----main-----start:"+Thread.currentThread().getName());

//        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
//            int i = 40 / 2;
//            System.out.println("运行结果：" + i);
//        }, executorService);
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
//            int i = 50 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executorService);
//        Integer integer = future.get();
//        System.out.println("异步执行结果="+integer);

//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
//            int i = 60 / 0;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executorService).whenComplete((res,exception)->{
//            System.out.println("异步执行完成,结果是："+res+" 异常是："+exception);
//        }).exceptionally(throwable -> {
//            return 0;
//        });
//        System.out.println(future.get());

        //方法执行后的处理 handle 可以改变执行结果
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
//            int i = 70 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executorService).handle((res,exception)->{
//            if (res != null) {
//                return res * 2;
//            }
//            if (exception != null) {
//                return 0;
//            }
//            return -1;
//        });
//        System.out.println(future.get());
        /**
         * 不能获取上一步执行结果
         */
//        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
//            int i = 80 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executorService).thenRunAsync(() ->
//                        System.out.println("任务二执行完成")
//                , executorService);

//        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
//            int i = 90 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executorService).thenAcceptAsync(res ->
//                System.out.println("任务二执行完成,获取到上一步执行结果：" + res)
//        );

//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
//            int i = 100 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executorService).thenApplyAsync(res -> {
//            int i = res * 2;
//            System.out.println("任务二执行完成,获取到上一步执行结果并修改：" + i);
//            return i;
//        }, executorService);
//        System.out.println(future.get());

        /**
         * 多个任务组合
         */
/*        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务一线程启动：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
            int i = 110 / 2;
            System.out.println("任务二结束：" + i);
            return i;
        }, executorService);
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务二线程启动：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
            int i = 120 / 2;
            System.out.println("任务二结束：" + i);
            return i;
        }, executorService);*/
//        CompletableFuture<Void> future = future1.runAfterBothAsync(future2, () -> {
//            System.out.println("任务三线程启动" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
//        }, executorService);
//        future1.thenAcceptBothAsync(future2, (f1,f2)->{
//            System.out.println("任务三线程启动，得到之前的两个结果和是："+f1+"<-->"+f2);
//        },executorService);

//        CompletableFuture<Object> future = future1.thenCombineAsync(future2, (f1, f2) -> {
//            System.out.println("任务三线程启动，得到之前的两个结果和是：" + f1 + "<-->" + f2 + "两者之和是：" + (f1 + f2));
//            return f1+f2;
//        }, executorService);
//        System.out.println(future.get());
        /**
         * 两个任务只要有一个完成便执行第三个任务
         */
       /* CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务一线程启动：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
            int i = 130 / 2;
            System.out.println("任务二结束：" + i);
            return i;
        }, executorService);
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务二线程启动：" + Thread.currentThread().getName() + "=" + Thread.currentThread().getId());
            int i = 140 / 2;
            System.out.println("任务二结束：" + i);
            return i;
        }, executorService);*/
        //runAfterEitherAsync  不感知任务结果 没有返回值
//        future1.runAfterEitherAsync(future2, ()->{
//            System.out.println("两个任务已经有一个任务完成");
//        },executorService);
        //只接受执行结果 自己没有返回值
//        future1.acceptEitherAsync(future2, (res)->{
//            System.out.println("两个任务已经有一个任务完成");
//        },executorService);
        //能够感知其它任务结果，并且自身也有返回结果
//        CompletableFuture<Integer> future = future1.applyToEitherAsync(future2, (res) -> {
//            System.out.println("两个任务已经有一个任务完成");
//            return res * 2;
//        }, executorService);
//        System.out.println(future.get());
        /**
         * 多任务组合
         */
        CompletableFuture<String> futureImage = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品图片信息");
            return "p-image";
        }, executorService);
        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品属性信息");
            return "p-attr";
        }, executorService);
        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品介绍信息");
            return "p-desc";
        }, executorService);
//        CompletableFuture<Void> future = CompletableFuture.allOf(futureImage, futureAttr, futureDesc);
//        future.get(); //等待所有任务全部完成
        CompletableFuture<Object> future = CompletableFuture.anyOf(futureImage, futureAttr, futureDesc);
        future.get();
        System.out.println(future.get());
        System.out.println("-----main-----end:"+Thread.currentThread().getId());
    }

}
