package com.rxcode.rxdownload.obervables;

import com.rxcode.rxdownload.api.ANY;
import com.rxcode.rxdownload.api.HttpGetService;
import com.rxcode.rxdownload.api.RxCarrier;
import com.rxcode.rxdownload.DownloadConfig;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import org.reactivestreams.Publisher;
import retrofit2.Response;

import java.io.File;
import java.util.concurrent.Callable;

public abstract class AbstractDownload {
    protected DownloadInfo downloadInfo;
    private long sampleInterval;
    private HttpGetService service;

    protected AbstractDownload(){
        sampleInterval = DownloadConfig.getSampleInterval();
    }

    protected AbstractDownload(DownloadInfo downloadInfo,HttpGetService service){
        this();
        this.downloadInfo = downloadInfo;
        this.service = service;
    }

    public Flowable<RxCarrier> download(File file, Response<ResponseBody> response){
        return Flowable.fromPublisher(tryToReNameFile(file, response))
                .flatMap(new Function<ANY, Publisher<RxCarrier>>() {
                    @Override
                    public Publisher<RxCarrier> apply(ANY any) throws Exception {
                        return checkCompleted(file,response);
                    }
                }).flatMap(new Function<RxCarrier, Publisher<RxCarrier>>() {
                    @Override
                    public Publisher<RxCarrier> apply(RxCarrier rxCarrier) throws Exception {
                        return checkTempFile(file, response);
                    }
                }).flatMap(new Function<RxCarrier, Publisher<RxCarrier>>() {
                    @Override
                    public Publisher<RxCarrier> apply(RxCarrier rxCarrier) throws Exception {
                        DownloadInfo downloadInfo = (DownloadInfo) rxCarrier;
                        if(downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOAD_FINISHED)
                            return Flowable.just(downloadInfo);

                        return write(file, response);
                    }
                });
    }

    public abstract Flowable<ANY> tryToReNameFile(File file, Response<ResponseBody> response);

    public abstract Flowable<RxCarrier> checkCompleted(File file, Response<ResponseBody> response);

    public abstract Flowable<RxCarrier> checkTempFile(File file, Response<ResponseBody> response);

    public abstract Flowable<RxCarrier> write(File file, Response<ResponseBody> response);

    public long getSampleInterval() {
        return sampleInterval;
    }
}
