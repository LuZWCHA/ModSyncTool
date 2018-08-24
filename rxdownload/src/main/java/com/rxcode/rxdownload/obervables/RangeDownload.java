package com.rxcode.rxdownload.obervables;

import com.rxcode.rxdownload.api.RxCarrier;
import io.reactivex.Flowable;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.File;

public class RangeDownload extends NormalDownload {

    public RangeDownload(DownloadInfo downloadInfo) {
        super(downloadInfo);
    }

    @Override
    public Flowable<RxCarrier> checkTempFile(File file, Response<ResponseBody> response) {
        return Flowable.just(downloadInfo);
    }

    @Override
    public Flowable<RxCarrier> write(File file, Response<ResponseBody> response) {
        return super.write(file, response);
    }
}
