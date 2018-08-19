package com.software.scan;

import com.software.beans.AbstractMod;
import com.software.beans.WrapperMod;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by 陆正威 on 2018/7/11.
 */
@SuppressWarnings("UnusedReturnValue")
public class JarsView {
    public HashSet<String> filter;
    private JarScan jarScan = new JarScan();
    private Subscriber<WrapperMod> subscriber;
    private Logger log;

    private Flowable<WrapperMod> modFlowable;
    private Subscription subscription;
    private final int proNum = Runtime.getRuntime().availableProcessors();
    private ExecutorService executor;

    public static JarsView create(String... filters){
        return new JarsView();
    }

    private JarsView(String... filters){

        log = Logger.getLogger(this.getClass().getSimpleName());
        log.setLevel(Level.CONFIG);

        filter = new HashSet<>();
        filter.addAll(Arrays.asList(filters));
        filter.add("jar");
        filter.add("litemod");
    }

    public JarsView setJarScan(@Nonnull JarScan jarScan) {
        this.jarScan = jarScan;
        return this;
    }

    /**
     * @param process the process you want to add
     * @return instance of this
     *  add your Custom ScanProcess here ,or just want to reset the order of the processes
     */
    public JarsView addScanProcess(@Nonnull AbstractScanProcess process){
        if(jarScan == null)
            jarScan = new JarScan();
        jarScan.addNewScanProcess(process);
        return this;
    }

    /**
     * @param time  millisecond
     * @return
     *  Set the sleep time of a thread that balance the CPU utilization
     */
    public JarsView setSleepTime(long time){
        if(jarScan == null)
            jarScan = new JarScan();
        jarScan.setSleepTime(time < 0 ? 0:time);
        return this;
    }

    public JarsView setFiles(Collection<File> files){

        Flowable<File> flowable = Flowable.fromIterable(files);

        modFlowable = flowable.filter(file -> {
            for (String end :
                    filter) {
                if (file.getName().endsWith(end))
                    return true;
            }
            return false;
        }).flatMap((Function<File, Flowable<WrapperMod>>) file -> {
            Flowable<WrapperMod> flowable1;
            try {
                flowable1 =  Flowable.just(file)
                        .observeOn(Schedulers.io())
                        .map(file1 -> {
                            WrapperMod wm = JarsView.this.meetFile(file1);
                            wm.setFilePath(file.getAbsolutePath());
                            return wm;
                        });

                return flowable1;
            }catch (Exception e){
                return Flowable.error(e);
            }
        }
        ,proNum + 1);

        return this;
    }

    public void view(Subscriber<WrapperMod> modSubscriber) {
        if(modFlowable == null)
            throw new RuntimeException("not bind files");

        modFlowable.doOnSubscribe(subscription -> {

            JarsView.this.subscription = subscription;
            subscription.request(proNum+1);
        }).doOnNext(mod -> {
            if(subscription!=null)
                subscription.request(1);
        }).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                subscription = null;
            }
        }).subscribe(modSubscriber);
    }

    public JarsView subscribeOn(Scheduler scheduler){
        if(modFlowable == null)
            throw new RuntimeException("not bind files");

        modFlowable = modFlowable.subscribeOn(scheduler);
        return this;
    }

    public JarsView observeOn(Scheduler scheduler){
        if(modFlowable == null)
            throw new RuntimeException("not bind files");

        modFlowable = modFlowable.observeOn(scheduler);
        return this;
    }

    @Deprecated
    public void view(String path) throws Exception {
        List<File> files = new ArrayList<>();
        if(traverseFolder(path,files)){
            view(files);
        }
    }

    /**
     * @param files the files you want to scan
     * @throws IOException
     * scan the dir and under-files
     */
    @Deprecated
    public void view(List<File> files) throws Exception {

        executor = Executors.newFixedThreadPool(proNum + 1);
        Scheduler scheduler = Schedulers.from(executor);

        if(!files.isEmpty()){
            log.info("traverseFolder Done! "+files.size()+"files to scan...");
            try {
                Flowable<File> flowable = Flowable.fromIterable(files);

                flowable.filter(file -> {
                    for (String end :
                            filter) {
                        if (file.getName().endsWith(end))
                            return true;
                    }
                    return false;
                }).flatMap(new Function<File, Flowable<WrapperMod>>() {
                               @Override
                               public Flowable<WrapperMod> apply(File file) throws Exception {
                                   Flowable flowable;
                                   try {
                                       flowable =  Flowable.just(file)
                                               .observeOn(scheduler)
                                               .map(new Function<File, WrapperMod>() {
                                                   @Override
                                                   public WrapperMod apply(File file) throws Exception {
                                                       return JarsView.this.meetFile(file);
                                                   }
                                               });
                                       return flowable;
                                   }catch (Exception e){
                                       return Flowable.error(e);
                                   }
                               }
                           }
                ,proNum + 1)//to make back
                        .blockingSubscribe(new Subscriber<WrapperMod>() {
                        Subscription ss;
                        long start_time;

                        @Override
                        public void onSubscribe(Subscription s) {
                            subscription = s;
                            ss = s;
                            if (subscriber != null)
                                subscriber.onSubscribe(s);

                            s.request(proNum + 1);
                            start_time = System.currentTimeMillis();
                            log.info("cpu processor num：" +
                                    proNum);
                            log.info("scan start,please wait for a while...");
                        }

                        @Override
                        public void onNext(WrapperMod abstractMod) {
                            if (!((WrapperMod) abstractMod).isEmpty()) {
                                log.info(abstractMod.toString());

                                if (subscriber != null)
                                    subscriber.onNext(abstractMod);
                            }

                            ss.request(1);
                        }

                        @Override
                        public void onError(Throwable t) {
                            if (subscriber != null)
                                subscriber.onError(t);
                            log.warning("catch error:");
                            log.throwing(this.getClass().getSimpleName(), "view()", t);
                        }

                        @Override
                        public void onComplete() {
                            if (subscriber != null)
                                subscriber.onComplete();
                            log.info("complete used " + (System.currentTimeMillis() - start_time) / 1000 +
                                    " seconds");
                        }
                    });

            }finally {
                executor.shutdownNow();
            }

        }else{
            log.info("traverseFolder failed,skip scan...");
        }
    }

    @Deprecated
    public JarsView setCallBack(Subscriber<WrapperMod> subscriber){
        this.subscriber = subscriber;
        return this;
    }


    //the number will include traverseFolder itself
    @SuppressWarnings("unused")
    @Deprecated
    private boolean traverseFolder(String path, List<File> allFiles) throws IOException {
        log.info("traverseFolder...");
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<File>();
            list.add(file);
            while (!list.isEmpty()){
                File temp = list.remove();
                if (temp.isDirectory()) {
                    File[] files = temp.listFiles();
                    if(files != null)
                        Collections.addAll(list, files);
                    folderNum++;
                } else {
                    if(!temp.isHidden()){
                        for (String s :
                                filter) {
                            if (!s.isEmpty() && temp.canRead() && temp.length() > 0) {
                                allFiles.add(temp);
                                break;
                            }
                        }
                    }
                    fileNum++;
                }
            }
            return true;
        } else {
            return  false;
        }
    }

    private WrapperMod meetFile(File file) throws InterruptedException {
        WrapperMod wm = WrapperMod.createEmpty();

        if(file.canRead()){
            try (JarFile jarFile = new JarFile(file)) {
                if (jarFile.size() > 0)
                    wm = jarScan.scan(jarFile);

            } catch (IOException | InstantiationException | IllegalAccessException ignored) {

            }
        }
        return wm;
    }

    public boolean isScanning(){
        return subscription != null;
    }

    @Deprecated
    public void cancel(){
        if(subscription != null) {
            subscription.cancel();
            subscription = null;
        }
        if(executor!=null && !executor.isShutdown())
            executor.shutdownNow();
    }

    public void stop(){
        if(subscription != null) {
            subscription.cancel();
            subscription = null;
        }
    }

}
