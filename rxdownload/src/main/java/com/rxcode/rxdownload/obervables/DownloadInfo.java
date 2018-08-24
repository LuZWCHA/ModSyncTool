package com.rxcode.rxdownload.obervables;

import com.rxcode.rxdownload.api.RxCarrier;

import java.util.UUID;

public final class DownloadInfo<T> implements RxCarrier,Cloneable {
    private DownloadStatus downloadStatus;
    private long progress;
    private long totalLength;
    private String msg;
    private Throwable throwable;
    private int downloadType;

    private String realFileName;
    private String tempFileName;
    private String url;

    private double downloadSpeed;
    private T data;

    private UUID uuid;

    public static DownloadInfo create(){
        return  new DownloadInfo();
    }

    public static DownloadInfo create(DownloadStatus status){
        DownloadInfo downloadInfo = create();
        downloadInfo.setDownloadStatus(status);
        return downloadInfo;
    }

    private DownloadInfo(){
        downloadStatus = DownloadStatus.DOWNLOAD_START;
        progress = 0;
        totalLength = 0;
        downloadSpeed = 0d;
        downloadType = 0;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int getType() {
        return downloadStatus.hashCode();
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public int getDownloadType() {
        return downloadType;
    }

    public void setDownloadType(int downloadType) {
        this.downloadType = downloadType;
    }

    public String getRealFileName() {
        return realFileName;
    }

    public void setRealFileName(String realFileName) {
        this.realFileName = realFileName;
    }

    public String getTempFileName() {
        return tempFileName;
    }

    public void setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(double downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public enum DownloadStatus{
        DOWNLOAD_CONFIG("等待下载"),
        DOWNLOAD_START ("下载即将开始"),
        DOWNLOADING ("正在下载"),
        DOWNLOAD_FAILED ("下载失败"),
        DOWNLOAD_FINISHED("下载完成"),
        DOWNLOAD_FORCE_STOP("暂停");
        private String name;

        DownloadStatus(String s){
            name = s;
        }

        public String getName() {
            return name;
        }
    }
}
