package com.rxcode.rxdownload;

import com.rxcode.rxdownload.api.DownloadFun;
import com.rxcode.rxdownload.api.RxCarrier;
import com.rxcode.rxdownload.obervables.*;
import com.rxcode.rxdownload.util.DTaskUtil;
import io.reactivex.*;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.*;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.*;

public final class RxDownload {

    private Flowable<RxCarrier> flowable;
    static Subscription subscription;

    //example
    public static void main(String[] args) throws IOException, InterruptedException {

        DTask dTask2 = DTask.create("http://dg.101.hk/1.rar","download2.dl");
        DTask dTask3 = DTask.create("http://dg.101.hk/1.rar","download3.dl");
        DTask dTask4 = DTask.create("http://dg.101.hk/1.rar");

        List<DTask> list = new ArrayList<>();
        list.add(dTask2);
        list.add(dTask3);
        list.add(dTask4);
        RxDownload download = new RxDownload();

        download.build1(list)
                .compose(new DownloadFun())
                .subscribe(new Subscriber<RxCarrier>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        System.out.println("subscribed");
                    }

                    @Override
                    public void onNext(RxCarrier rxCarrier) {
                        DownloadInfo downloadInfo = (DownloadInfo) rxCarrier;
                        System.out.println(downloadInfo.getUuid());

                        if(downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOAD_FINISHED)
                            System.out.println(downloadInfo.getRealFileName() + "saved");
                        else if(downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOADING)
                            System.out.println((downloadInfo.getProgress()/100d)+", "+downloadInfo.getDownloadSpeed()+"kb/s");
                        else if(downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOAD_FAILED)
                            System.out.println("failed");
                        else if(downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOAD_FORCE_STOP)
                            System.out.println("unsubscribe");
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println(t.toString());
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("complete");
                    }
                });
        Thread.sleep(1000000);
    }

    private Flowable<RxCarrier> config(String url,String fileName){
        return Flowable.just(DownloadInfo.create(DownloadInfo.DownloadStatus.DOWNLOAD_CONFIG))
                .map(new Function<DownloadInfo, DownloadInfo>() {
                    @Override
                    public DownloadInfo apply(DownloadInfo downloadInfo) throws Exception {
                        downloadInfo.setUrl(url);
                        downloadInfo.setRealFileName(fileName);
                        return downloadInfo;
                    }
                });
    }

    public Flowable<RxCarrier> build1(Collection<DTask> task){
        return Flowable.fromIterable(task)
                .filter(new Predicate<DTask>() {
                    @Override
                    public boolean test(DTask task) throws Exception {
                        String fileName = task.getDownloadInfo().getRealFileName();
                        String tempName = fileName + DownloadConfig.getTempSuffix();
                        task.getDownloadInfo().setTempFileName(tempName);
                        return DTaskUtil.checkFileAndDelete(DownloadConfig.getAbsolutePath(tempName));
                    }
                })
                .flatMap(new Function<DTask, Publisher<RxCarrier>>() {
                    @Override
                    public Publisher<RxCarrier> apply(DTask task) throws Exception {
                        return task.start();
                    }
                });
    }

    public RxDownload build(Collection<DTask> tasks){
        flowable = build1(tasks);
        return this;
    }

    public Flowable<RxCarrier> build1(@NonNull DTask task) {
        return Flowable.just(task)
                .filter(new Predicate<DTask>() {
                    @Override
                    public boolean test(DTask task) throws Exception {
                        String fileName = task.getDownloadInfo().getRealFileName();
                        String tempName = fileName + DownloadConfig.TEMP_SUFFIX;
                        task.getDownloadInfo().setTempFileName(tempName);

                        return DTaskUtil.checkFileAndDelete(DTaskUtil.makeAbsolutePath(task.getDownloadInfo().getDownloadPath(),tempName));
                    }
                })
                .flatMap(new Function<DTask, Publisher<RxCarrier>>() {
                    @Override
                    public Publisher<RxCarrier> apply(DTask task) throws Exception {
                        return task.start();
                    }
                });
    }

    public RxDownload build(@NonNull DTask task){
        flowable = build1(task);
        return this;
    }

    public Flowable<RxCarrier> start(){
        return flowable.compose(new DownloadFun());
    }

    public void start(Subscriber<RxCarrier> subscriber, Scheduler scheduler){
        if(flowable == null)
            throw new RuntimeException("not init RxDownloadConfig");

        start().subscribeOn(scheduler)
                .subscribe(subscriber);
    }

    public RxDownload setMaxTaskNum(int num){
        DownloadConfig.MAX_TASK_NUM = num;
        return this;
    }

    public RxDownload setDownloadPath(String path){
        if(!path.endsWith("\\") && !path.endsWith("/")){
            path = path.concat("\\");
        }
        DownloadConfig.DEFAULT_DOWNLOAD_PATH = path;
        return this;
    }
}
