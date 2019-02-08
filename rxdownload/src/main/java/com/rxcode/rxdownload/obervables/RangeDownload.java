package com.rxcode.rxdownload.obervables;

import com.rxcode.rxdownload.DownloadConfig;
import com.rxcode.rxdownload.api.HttpGetService;
import com.rxcode.rxdownload.api.RxCarrier;
import com.rxcode.rxdownload.util.DTaskUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Cancellable;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Response;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RangeDownload extends NormalDownload {

    public RangeDownload(DownloadInfo downloadInfo, HttpGetService service) {
        super(downloadInfo,service);
    }

    @Override
    public Flowable<RxCarrier> checkTempFile(File file, Response<ResponseBody> response) {
        return Flowable.just(downloadInfo);
    }

    @Override
    public Flowable<RxCarrier> write(File file, Response<ResponseBody> response) {
        return Flowable.<RxCarrier>create(new FlowableOnSubscribe<RxCarrier>() {
            @Override
            public void subscribe(FlowableEmitter<RxCarrier> emitter) throws Exception {
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                    }
                });

                ResponseBody body = response.body();
                if(body == null) {
                    throw new RuntimeException("response body should not be null");
                }

                long downloadSize = file.length();
                long byteSize = 8192L;
                long totalSize = body.contentLength() + downloadSize;
                downloadInfo.setTotalLength(totalSize);

                BufferedSink sink = Okio.buffer(Okio.appendingSink(file));
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

    @Override
    public Flowable<Response<ResponseBody>> start(DownloadInfo downloadInfo) {
        final long tempFileLength =  new File(DTaskUtil.makeAbsolutePath(downloadInfo.getDownloadPath(),downloadInfo.getTempFileName())).length();
        return service.getFile(downloadInfo.getUrl(),String.format("bytes=%d-",tempFileLength));
    }
}
