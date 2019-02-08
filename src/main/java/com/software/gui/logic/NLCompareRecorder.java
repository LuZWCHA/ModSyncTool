package com.software.gui.logic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rxcode.rxdownload.RxDownload;
import com.rxcode.rxdownload.api.ANY;
import com.rxcode.rxdownload.api.DownloadFun;
import com.rxcode.rxdownload.api.RxCarrier;
import com.rxcode.rxdownload.obervables.DTask;
import com.rxcode.rxdownload.obervables.RetrofitClient;
import com.software.beans.AbstractMod;
import com.software.beans.TransMod;
import com.software.gui.Config;
import com.software.gui.scheduler.JavaFxScheduler;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

//compare network and local files,and cache the differences
public class NLCompareRecorder {

    private Logger logger = Logger.getLogger(getClass().getSimpleName());
    public interface DownloadTaskCallBack{
        //trigger when obtain the mod SERVER_INF_LIST
        void getList();
        void getTask(List<DTask> tasks);
    }

    private Set<TransMod> lostMods;//server needed mods
    private Map<String, List<TransMod>> redundantMods;//different from server,the mods are redundant for server
    private List<TransMod> conflictMods;//id VERSION are same (delete by code)
    private DirInfoCache cache;
    private RxDownload rxDownload ;

    NetworkApi networkApi =
            RetrofitClient.INSTANCE
                    .create(NetworkApi.class);

    public NLCompareRecorder(){
        lostMods = new HashSet<>();
        redundantMods = new HashMap<String, List<TransMod>>();
        conflictMods = new LinkedList<>();
        rxDownload = new RxDownload();
    }

    public void bindCache(@NonNull DirInfoCache cache){
        if(cache.equals(this.cache))
            return;
        this.cache = cache;
        rxDownload.setDownloadPath(cache.getDiskRealPath());
        rxDownload.setMaxTaskNum(Config.DOWNLOAD_THREAD_NUM);
    }

    private void updateDownloadPath(){
        rxDownload.setDownloadPath(cache.getDiskRealPath());
    }

    @Deprecated
    private Flowable<List<TransMod>> getModList(){
        return Flowable.just(ANY.product())
                .subscribeOn(JavaFxScheduler.platform())
                .observeOn(Schedulers.io())
                .flatMap(new Function<ANY, Publisher<List<TransMod>>>() {
                    @Override
                    public Publisher<List<TransMod>> apply(ANY any) throws Exception {
                        String url = Config.getFormatURL() + "/modList";
                        System.out.println(url);
                        return networkApi.getModList(url);
                    }
                });

    }

    private void compareWithLocalMods(Set<TransMod> serverMods){
        Set<TransMod> localMods = Sets.newHashSet(cache.getTransMods());
        Set<String> serverModIds = Sets.newHashSet();
        conflictMods.clear();
        redundantMods.clear();

        //same id but different versions
        Map<String,List<TransMod>> idTemp = Maps.newLinkedHashMap();

        lostMods = Sets.difference(serverMods,localMods);

        if(lostMods.isEmpty())
            logger.info("lost mods's SERVER_INF_LIST is empty");
        lostMods.forEach(new Consumer<TransMod>() {
            @Override
            public void accept(TransMod mod) {
                logger.info("lost mods:"+ mod.getSimpleDescribe());
            }
        });

        serverMods.forEach(mod -> serverModIds.add(mod.getId()));

        cache.getTransMods().forEach(mod -> {
            if (serverModIds.contains(mod.getId())) {
                mod.setFromServer(true);
                //if VERSION not same
                if (!serverMods.contains(mod)) {
                    conflictMods.add(mod);
                }

                //if has same mod
                if (idTemp.containsKey(mod.getId())) {
                    conflictMods.add(mod);
                }else{
                    idTemp.put(mod.getId(), Lists.newArrayList(mod));
                }

            } else {
                if (mod.isFromServer())
                    conflictMods.add(mod);
                else {
                    if (idTemp.containsKey(mod.getId())) {
                        List<TransMod> transModSet = idTemp.get(mod.getId());
                        //if mod is same
                        if (transModSet.contains(mod)) {
                            //same mod
                            conflictMods.add(mod);
                        } else {
                            //add the mod if VERSION not same
                            transModSet.add(mod);
                        }
                    } else {
                        idTemp.put(mod.getId(), Lists.newArrayList(mod));
                    }
                }
            }
        });

        idTemp.forEach((s, transMods) -> {
            if(transMods.size() > 1){
                redundantMods.put(s,transMods);
            }
        });

    }

    //this method is going to separated from this class with method getModList()(they are network-method not method for record)
    @Deprecated
    public Flowable<RxCarrier> syncMods(@NonNull DownloadTaskCallBack callBack) throws Exception {
        checkNull(cache);
        rxDownload.setMaxTaskNum(Config.DOWNLOAD_THREAD_NUM);
        //cache may be changed
        updateDownloadPath();
        return getModList()
                .flatMap(new Function<List<TransMod>, Publisher<Set<TransMod>>>() {
                    @Override
                    public Publisher<Set<TransMod>> apply(List<TransMod> transModList) throws Exception {
                        callBack.getList();
                        Set<TransMod> transModSet = Sets.newHashSet(transModList);
                        //to download lost mods
                        return Flowable.just(transModSet)
                                .observeOn(Schedulers.computation())
                                //get conflict mods,lost mods and so on
                                .map(transMods -> {
                                    compareWithLocalMods(transModSet);
                                    return conflictMods;
                                }).observeOn(JavaFxScheduler.platform())
                                //remove(delete file as well) unnecessary mods if possible
                                .map(transMods -> {
                                    transMods.removeIf(new Predicate<TransMod>() {
                                        @Override
                                        public boolean test(TransMod mod) {
                                            try {
                                                cache.removeMod((AbstractMod) mod,true);
                                                logger.info("remove "+mod.getId()+" success");
                                            }catch (IOException e){
                                                logger.throwing(getClass().getSimpleName(),"remove conflict mod(delete file)",e);
                                                return false;
                                            }
                                            return true;
                                        }
                                    });
                                    return lostMods;
                                });
                    }
                }).flatMap((Function<Set<TransMod>, Publisher<TransMod>>) Flowable::fromIterable)
                //warp as a task SERVER_INF_LIST
                .collectInto(Lists.newArrayList(), (BiConsumer<List<DTask>, TransMod>) (list, mod) -> {
                    logger.info(Config.getFormatURL() + "/mod/" + mod.getId());
                    DTask dTask =
                            DTask.create(Config.getFormatURL() + "/mod/"+mod.getId(),mod.getId()+".jar");
                    dTask.getDownloadInfo().setData(mod);
                    list.add(dTask);
                }).flatMapPublisher((Function<List<DTask>, Publisher<RxCarrier>>) list -> {
                    callBack.getTask(list);
                    return rxDownload
                            .build1(list)
                            .compose(new DownloadFun());
                }).observeOn(JavaFxScheduler.platform());
    }

    private void checkNull(Object o)throws Exception {
        if(o == null)
            throw new RuntimeException("object should not be null");
    }


}
