package com.software.gui.utils;

public class VersionCompareHelper {

    /**
     * Compare VERSION,which use '.' or '_' to spilt the VersionNumber.(make true when '.' and '_' spilt the VERSION number,each part needs same
     * standard,for example,"1.8_12" compare with "1.8.0_12" are illegal)
     * Zero means they are equal.
     * @param version1 first VERSION
     * @param version2 second VERSION
     * @return If the first one greater than the second one,return positive number,else return negative number
     */
    public static int compareVersion(String version1, String version2) throws Exception {
        if (version1 == null || version2 == null) {
            throw new RuntimeException("CompareVersion error:illegal params.");
        }
        String[] versionArray1 = version1.split("[._]");
        String[] versionArray2 = version2.split("[._]");

        int idx = 0;
        int minLength = Math.min(versionArray1.length, versionArray2.length);//get the min length
        int diff = 0;
        while (idx < minLength
                && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0//first,compare length
                && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {//second, compare char
            ++idx;
        }

        //if the prefixes are same,the longer one is bigger
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
        return diff;
    }
}
