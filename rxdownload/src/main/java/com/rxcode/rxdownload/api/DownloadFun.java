package com.rxcode.rxdownload.api;

import com.rxcode.rxdownload.DownloadConfig;
import com.rxcode.rxdownload.RxDownload;
import com.rxcode.rxdownload.obervables.AbstractDownload;
import com.rxcode.rxdownload.obervables.DownloadInfo;
import com.rxcode.rxdownload.obervables.NormalDownload;
import com.rxcode.rxdownload.obervables.RetrofitClient;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.functions.*;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import retrofit2.Response;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DownloadFun implements FlowableTransformer<RxCarrier, RxCarrier> {

    @Override
    public Publisher<RxCarrier> apply(Flowable<RxCarrier> upstream) {
        return upstream
                .flatMap(new Function<RxCarrier,Flowable<RxCarrier>>() {
                    HttpGetService service = RetrofitClient.INSTANCE.create(HttpGetService.class);
                    @Override
                    public Flowable<RxCarrier> apply(RxCarrier o) throws Exception {
                        DownloadInfo downloadInfo = (DownloadInfo) o;
                        AbstractDownload download = new NormalDownload(downloadInfo,service);
                        File file = new File(DownloadConfig.getDownloadPath() + downloadInfo.getTempFileName());

                        return service.getFile(downloadInfo.getUrl())
                                .retry(throwable -> {
                                    downloadInfo.setDownloadStatus(DownloadInfo.DownloadStatus.DOWNLOAD_FAILED);
                                    downloadInfo.setThrowable(throwable);
                                    return false;
                                })
                                .observeOn(Schedulers.io())
                                .flatMap((Function<Response<ResponseBody>, Flowable<RxCarrier>>) responseBodyResponse -> {

                                    Flowable<RxCarrier> downloadEmitter = download.download(file, responseBodyResponse);

                                    return downloadEmitter
                                            .buffer(download.getSampleInterval() * 2,TimeUnit.MILLISECONDS,1)
                                            .flatMap(new Function<List<RxCarrier>, Publisher<RxCarrier>>() {
                                                private DownloadInfo temp = null;
                                                @Override
                                                public Publisher<RxCarrier> apply(List<RxCarrier> rxCarriers) throws Exception {
                                                    if(rxCarriers.isEmpty()){
                                                        if(temp != null)
                                                            return Flowable.just(temp);
                                                        else
                                                            return Flowable.empty();
                                                    }else {
                                                        temp = (DownloadInfo) rxCarriers.get(0);
                                                        return Flowable.just(temp);
                                                    }
                                                }
                                            })
                                            .sample(download.getSampleInterval(),TimeUnit.MILLISECONDS,true)
                                            .onErrorReturn(throwable -> {
                                                ((DownloadInfo) o).setDownloadStatus(DownloadInfo.DownloadStatus.DOWNLOAD_FAILED);
                                                ((DownloadInfo) o).setThrowable(throwable);
                                                return o;
                                            })
                                            .scan((downloadInfo1, rxCarrier) -> {
                                                DownloadInfo currentDownloadInfo = (DownloadInfo) rxCarrier;
                                                DownloadInfo lastDownloadInfo = (DownloadInfo) downloadInfo1;
                                                double speed;
                                                speed = (currentDownloadInfo.getProgress() - lastDownloadInfo.getProgress())/10000d /
                                                        DownloadConfig.getSampleInterval() * currentDownloadInfo.getTotalLength();

                                                currentDownloadInfo.setDownloadSpeed(speed);
                                                return (RxCarrier) currentDownloadInfo.clone();
                                            });
                                });
                    }
                },true,DownloadConfig.getMaxTaskNum());
    }
}
