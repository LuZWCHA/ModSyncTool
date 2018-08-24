package com.software.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.software.gui.utils.FileHelper;

import java.io.*;

public class Config {
    public static String PATH;
    public static String SERVER_ADDRESS = "http://116.196.86.6:25531";
    public static boolean USE_RELATIVE_PATH = false;
    public static int DOWNLOAD_THREAD_NUM = 1;

    private static String configName = "sync.config";

    public static String getFormatURL(){
        if(!SERVER_ADDRESS.contains("http://") && !SERVER_ADDRESS.contains("https://"))
            SERVER_ADDRESS = "http://" + SERVER_ADDRESS;
        if(SERVER_ADDRESS.endsWith("/") || SERVER_ADDRESS.endsWith("\\")){
            SERVER_ADDRESS = SERVER_ADDRESS.substring(0,SERVER_ADDRESS.length() - 2);
        }
        return SERVER_ADDRESS;
    }



    public static void save() throws Exception{
        File file = new File(configName);// 把json保存项目根目录下无后缀格式的文本
        OutputStream out = new FileOutputStream(file);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));//设计编码

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setPrettyPrinting()
                .create();
        writer.setIndent("      ");
        String realPath = PATH;
        if(USE_RELATIVE_PATH){
            String fixPath = new File(PATH).getAbsolutePath();
            String currentPath = FileHelper.getJarDir();
            if(currentPath.length() <= fixPath.length() && fixPath.startsWith(currentPath)) {
                realPath = fixPath.substring(currentPath.length(), fixPath.length());
            }else {
                USE_RELATIVE_PATH = false;
                throw new PathConvertException();
            }
        }

        gson.toJson(new SyncConfig(realPath,SERVER_ADDRESS,USE_RELATIVE_PATH, DOWNLOAD_THREAD_NUM), SyncConfig.class, writer);
        writer.flush();
        writer.close();
    }

    public static void read() throws Exception{
        Config.PATH = FileHelper.getJarDir();
        File file = new File(configName);
        InputStream in = new FileInputStream(file);
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        SyncConfig syncConfig;
        syncConfig = gson.fromJson(reader,SyncConfig.class);

        PATH = syncConfig.path;
        SERVER_ADDRESS = syncConfig.serverAddress;
        USE_RELATIVE_PATH = syncConfig.useRelativePath;
        DOWNLOAD_THREAD_NUM = syncConfig.downloadThreadNum > 0 ? syncConfig.downloadThreadNum : Runtime.getRuntime().availableProcessors();

        //check path's validity
        String currentPath = FileHelper.getJarDir();
        if(USE_RELATIVE_PATH){
            PATH = currentPath + PATH;
            if(!new File(PATH).isDirectory()) {
                USE_RELATIVE_PATH = false;
                PATH = syncConfig.path;
            }
        }
        if(!new File(PATH).isDirectory()){
            PATH = currentPath;
        }
        reader.close();
    }

    public static class PathConvertException extends Exception{
        @Override
        public String getMessage() {
            return "can't convert to relative path";
        }
    }

    private static class SyncConfig{
        private String path ;
        private String serverAddress;
        private boolean useRelativePath;
        private int downloadThreadNum;

        public SyncConfig(String path, String serverAddress, boolean useRelativePath, int downloadThreadNum) {
            this.path = path;
            this.serverAddress = serverAddress;
            this.useRelativePath = useRelativePath;
            this.downloadThreadNum = downloadThreadNum;
        }

        public SyncConfig() {

        }

        public String getServerAddress() {
            return serverAddress;
        }

        public void setServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isUseRelativePath() {
            return useRelativePath;
        }

        public void setUseRelativePath(boolean useRelativePath) {
            this.useRelativePath = useRelativePath;
        }

        public int getDownloadThreadNum() {
            return downloadThreadNum;
        }

        public void setDownloadThreadNum(int downloadThreadNum) {
            this.downloadThreadNum = downloadThreadNum;
        }
    }
}
