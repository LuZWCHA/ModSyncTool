package com.software.beans;

import java.io.Serializable;

import static com.software.beans.Mod.MODE.NULL_ID_EXCEPTION;

/**
 * Created by 陆正威 on 2018/7/11.
 */
public interface Mod extends Serializable {

    String[] NameMap = {"unknown","BukkitPlugin","SpongePlugin","ForgeMod","LiteMod","TweakMod","extra"};

    final class MODE {
        public static String NULL_ID_EXCEPTION = "id must be defined";
        public static String UNKNOWN_VERSION = "unknown";
        public static short UNKNOWN_MOD_MODE = 0;
        public static short BUKKIT = 1;
        public static short SPONGE = 2;
        public static short FORGE = 3;
        public static short LITE = 4;
        public static short TWEAK = 5;
        public static short EX =6;
    }
}


