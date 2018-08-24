package com.rxcode.rxdownload.util;


import com.rxcode.rxdownload.obervables.DTask;

import java.io.File;
import java.util.UUID;

public class DTaskUtil {

    //check the file if existed,and if existed check can it be deleted so that downstream can make a new one
    public static boolean checkFileAndDelete(String fileName){
        if(fileName != null && !fileName.isEmpty()){
            File file = new File(fileName);
            if(!file.exists())
                return true;
            if(file.isDirectory() || !file.canWrite()) {
                return false;
            }
            return file.delete();
        }
        return false;
    }

    public static boolean checkFileExits(String fileNmae){
        if(fileNmae != null && !fileNmae.isEmpty()){
            File file = new File(fileNmae);
            return file.exists();
        }
        return false;
    }

    public static DTask createFakeDTask(UUID uuid){
        return DTask.create(uuid,"","");
    }
}
