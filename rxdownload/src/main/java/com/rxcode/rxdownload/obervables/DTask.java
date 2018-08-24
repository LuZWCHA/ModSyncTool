package com.rxcode.rxdownload.obervables;

import com.rxcode.rxdownload.api.RxCarrier;
import com.rxcode.rxdownload.DownloadConfig;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.UUID;

public final class DTask{
    private UUID id;
    private DownloadInfo downloadInfo;

    private Subscription subscription;

    public static DTask create(String url){
        UUID id = UUID.randomUUID();
        return create(url,DownloadConfig.getUnknownName() + id);
    }

    public static DTask create(String url, String fileName){
        UUID id = UUID.randomUUID();
        return create(id,url,fileName);
    }

    public static DTask create(UUID id, String url, String fileName){
        return new DTask(id, url, fileName);
    }

    private DTask(UUID id, String url, String fileName) {
        this.id = id;
        downloadInfo = DownloadInfo.create(DownloadInfo.DownloadStatus.DOWNLOAD_CONFIG);
        downloadInfo.setUrl(url);
        downloadInfo.setRealFileName(fileName);
        downloadInfo.setUuid(id);
    }

    public Flowable<RxCarrier> start(){
        return Flowable.<RxCarrier>just(downloadInfo)
        .doOnSubscribe(new Consumer<Subscription>() {
            @Override
            public void accept(Subscription subscription) throws Exception {
                DTask.this.subscription = subscription;
            }
        });
    }

    public void stop(){
        subscription.cancel();
    }

    @Override
    public String toString() {
        return downloadInfo.getRealFileName() +"  "+(downloadInfo.getProgress() / 100d) +
                ", " + String.format("%.2f", downloadInfo.getDownloadSpeed()) + "kb/s";
    }

    public void setDownloadInfo(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTask)) return false;
        DTask dTask = (DTask) o;
        return Objects.equals(id, dTask.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
