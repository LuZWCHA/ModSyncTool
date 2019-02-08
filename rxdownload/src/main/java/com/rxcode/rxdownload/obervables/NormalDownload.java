package com.rxcode.rxdownload.obervables;

import com.rxcode.rxdownload.api.ANY;
import com.rxcode.rxdownload.api.HttpGetService;
import com.rxcode.rxdownload.api.RxCarrier;
import com.rxcode.rxdownload.DownloadConfig;
import com.rxcode.rxdownload.util.DTaskUtil;
import com.rxcode.rxdownload.util.HttpHelper;
import com.sun.deploy.net.HttpResponse;
import io.reactivex.*;
import io.reactivex.functions.Cancellable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class NormalDownload extends AbstractDownload {

    public NormalDownload(DownloadInfo downloadInfo, HttpGetService service){
        super(downloadInfo,service);
    }

    @Override
    public Flowable<Response<ResponseBody>> start(DownloadInfo downloadInfo) {
        return service.getFile(downloadInfo.getUrl());
    }

    @Override
    public Flowable<ANY> checkFileAndResponse(File file, Response<ResponseBody> response) {
        if(!response.isSuccessful()) {
            return Flowable.error(new Throwable("failed response."));
        }
        return Flowable.just(ANY.product());
    }

    @Override
    public Flowable<RxCarrier> checkCompleted(File file, Response<ResponseBody> response) {
        return Flowable.fromCallable(new Callable<RxCarrier>() {
            @Override
            public RxCarrier call() throws Exception {
                if(DTaskUtil.checkFileExits(DTaskUtil.getAbsolutePath(downloadInfo))) {
                    if(DownloadConfig.isSkipCompletedFile()) {
                        downloadInfo.setDownloadStatus(DownloadInfo.DownloadStatus.DOWNLOAD_FINISHED);
                        downloadInfo.setProgress(10000);
                        return downloadInfo;
                    }
                    //delete and re-download
                    new File(DownloadConfig.getAbsolutePath(downloadInfo.getRealFileName())).deleteOnExit();
                }
                return downloadInfo;
            }
        });
    }

    @Override
    public Flowable<RxCarrier> checkTempFile(File file, Response<ResponseBody> response) {
        return Flowable.fromCallable(new Callable<RxCarrier>() {
            @Override
            public RxCarrier call() throws Exception {
                if(file.exists()){
                    file.delete();
                }
                return downloadInfo;
            }
        });
    }

    @Override
    public Flowable<RxCarrier> write(File file, Response<ResponseBody> response){

        return Flowable.<RxCarrier>create(new FlowableOnSubscribe<RxCarrier>() {
            @Override
            public void subscribe(FlowableEmitter<RxCarrier> emitter) throws Exception {
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        System.out.println(downloadInfo.getRealFileName()+"cancel");
                    }
                });

                ResponseBody body = response.body();
                if(body == null) {
                    throw new RuntimeException("response body should not be null");
                }

                long downloadSize = 0L;
                long byteSize = 8192L;
                long totalSize = body.contentLength();
                downloadInfo.setTotalLength(totalSize);

                BufferedSink sink = Okio.buffer(Okio.sink(file));
                Source source = body.source();
                Buffer buffer = new Buffer();

                try {
                    long readLen = body.source().read(buffer, byteSize);
                    downloadInfo.setDownloadStatus(DownloadInfo.DownloadStatus.DOWNLOADING);
                    while (!emitter.isCancelled() && readLen != -1L) {
                        sink.write(buffer,readLen);
                        downloadSize += readLen;

                        downloadInfo.setProgress((long) (((double)downloadSize/totalSize)*10000));
                        emitter.onNext(downloadInfo);

                        readLen = source.read(buffer, byteSize);
                    }

                }catch (Exception e){
                    if(!emitter.isCancelled()) {
                        emitter.tryOnError(e);
                    }
                }finally {
                    body.close();
                    buffer.close();
                    sink.close();
                    if(!emitter.isCancelled()) {
                        if(file.renameTo(new File(DTaskUtil.getAbsolutePath(downloadInfo)))) {
                            downloadInfo.setDownloadStatus(DownloadInfo.DownloadStatus.DOWNLOAD_FINISHED);
                            emitter.onNext(downloadInfo);
                            emitter.onComplete();
                        }else {
                            emitter.tryOnError(new RuntimeException("rename failed"));
                        }
                    }
                }

            }
        },BackpressureStrategy.LATEST);

    }


}
