package com.software.gui.controllers;

import com.rxcode.rxdownload.api.ANY;
import com.software.beans.Mod;
import com.software.gui.Config;
import com.software.gui.logic.CacheManager;
import com.software.gui.logic.DirInfoCache;
import com.software.gui.logic.FileWatchServer;
import io.reactivex.Maybe;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class MainController2 implements Initializable {

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    @FXML
    private Pane root;

    private static Image[] images;

    private DirInfoCache cache;

    private static FileWatchServer server;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cache = CacheManager.INSTANCE.getCache(0);
        cache.syncFromDisk();

        startServer(cache);
        initImageSources();
    }

    private static void initImageSources(){
        Maybe.just(ANY.product())
                .observeOn(Schedulers.io())
                .map(any -> {
                    images = new Image[8];
                    images[Mod.MODE.FORGE] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/forge_mark.png"));
                    images[Mod.MODE.SPONGE] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/spongie_mark.png"));
                    images[Mod.MODE.LITE] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/litemod_mark.png"));
                    images[Mod.MODE.BUKKIT] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/bukkit_mark.png"));
                    images[Mod.MODE.TWEAK] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/tweaker_mark.png"));
                    images[Mod.MODE.UNKNOWN_MOD_MODE] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/unknown_mark.png"));

                    return any;
                }).subscribe(new DisposableMaybeObserver<ANY>() {
            @Override
            public void onSuccess(ANY any) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

    }

    public static Image[] getImages(){
        return images;
    }

    public static void stopServer(){
        server.clearCache();
        server.interrupt();
    }

    //setup watching-server
    public static void startServer(DirInfoCache cache){
        server = null;
        server = new FileWatchServer(cache);
        server.setDaemon(true);
        server.start();
    }
}
