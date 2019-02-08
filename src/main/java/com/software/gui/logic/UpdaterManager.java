package com.software.gui.logic;

import com.rxcode.rxdownload.RxDownload;
import com.rxcode.rxdownload.api.DownloadFun;
import com.rxcode.rxdownload.api.RxCarrier;
import com.rxcode.rxdownload.obervables.DTask;
import com.rxcode.rxdownload.obervables.RetrofitClient;
import com.software.beans.jsonbean.VersionData;
import com.software.gui.utils.AppInfo;
import com.software.gui.utils.FileHelper;
import com.software.gui.utils.VersionCompareHelper;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import java.io.File;

public enum  UpdaterManager {
    INSTANCE;
    private static final String CHECK_UPDATE_URL = "https://raw.githubusercontent.com/LuZWCHA/download/master/sync/latest_version.json";
    private static final String UPDATE_URL = "https://raw.githubusercontent.com/LuZWCHA/download/master/sync/latest/";
    private HttpRequest request;
    RxDownload rxDownload;

    UpdaterManager(){
        request = RetrofitClient.INSTANCE.create(HttpRequest.class);
        rxDownload = new RxDownload();
    }

    public Single<VersionData> checkUpdate(){
        return request.getVersion(CHECK_UPDATE_URL)
                .subscribeOn(Schedulers.io())
                .singleOrError()
                .flatMap(new Function<VersionData, SingleSource<VersionData>>() {
                    @Override
                    public SingleSource<VersionData> apply(VersionData appImfo) throws Exception {
                        if(VersionCompareHelper.compareVersion(appImfo.getVersion(),AppInfo.VERSION) > 0 )
                            return Single.just(appImfo);
                        return Single.just(VersionData.createEmpty());
                    }
                });
    }

    public Flowable<RxCarrier> downloadNewVersion(String version){
        return rxDownload.build1(DTask.create(UPDATE_URL + "ModSyncToolClient-" + version + ".jar")
                .setDownloadPath(FileHelper.getJarDir()))
                .compose(new DownloadFun());
    }

    public void deleteOldVersion(){
        Thread runnable = new Thread() {
            @Override
            public void run() {
                File oldFile = new File(FileHelper.getJarPath());
                oldFile.deleteOnExit();
            }
        };
        runnable.start();
    }

    public void restart(String path){

    }
}
