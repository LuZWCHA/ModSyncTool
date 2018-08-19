package com.software.util;
import io.reactivex.annotations.NonNull;

/**
 * Created by 陆正威 on 2018/7/14.
 */
public class ScanUtil {
    public static String getLastWorldOf(@NonNull String s, @NonNull String p, boolean contain){
        int index = s.lastIndexOf(p);
        if(index>=0){
            return s.substring(index + (contain ?0 : p.length()),s.length());
        }
        return s;
    }
    public static String getLastWorldOf(@NonNull String s,@NonNull String p){
        return getLastWorldOf(s, p,false);
    }

}
