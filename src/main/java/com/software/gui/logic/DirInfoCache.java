package com.software.gui.logic;

import com.rxcode.rxdownload.api.ANY;
import com.software.beans.AbstractMod;
import com.software.beans.TransMod;
import com.software.beans.WrapperMod;
import com.software.api.MyCache;
import com.software.gui.scheduler.JavaFxScheduler;
import com.software.gui.utils.FileHelper;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

//Note: some methods are thread-safe while others not;
//all method based on java.util.set and javafx.collections.xxx
@SuppressWarnings({"unused", "FieldCanBeLocal", "WeakerAccess"})
public final class DirInfoCache implements MyCache {
    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    private static String infoFileName = "mod_scan.info";
    private static String ignoreFileName = "scan.ignore";

    private Set<String> ignoreFiles;
    //present mods had been scanned,Note:this SERVER_INF_LIST may have same mod,it's just a map from disk to memory
    //care for the add,remove,save... method should should invoke in special thread,for example in javafx-thread
    private ObservableList<TransMod> transMods;

    //file-to-mod's map,speed up search by filePath
    private Map<String,TransMod> fileToModMap;

    //the addedNewFiles ready to scan
    private ObservableSet<File> addedNewFiles;

    private String diskRealPath;

    //for obserablelist when empty(or not) will send a signal
    private BooleanProperty wasEmpty;

    private SetChangeListener<File> innerListener;

    private final Lock syncLock = new ReentrantLock();

    @Override
    public void preInit() {
        syncFromDisk();
    }

    @Override
    public void save() {
        try {
            sync2Disk(FILE.BOTH);
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public boolean doInit() {
        return false;
    }

    @Override
    public boolean doSave() {
        return false;
    }

    public enum FILE{
        IGNORE,
        INFORMATION,
        BOTH
    }

    public DirInfoCache(String path){
        if(new File(path).isDirectory()){
            wasEmpty = new ReadOnlyBooleanWrapper(true);
            diskRealPath = path;
            ignoreFiles = new ConcurrentSkipListSet<>();
            transMods = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
            addedNewFiles = FXCollections.observableSet(new ConcurrentSkipListSet<>());
            fileToModMap = new ConcurrentHashMap<>();
            innerListener = change -> {
                if(change.wasAdded()){
                    if(wasEmpty.get()) {
                        wasEmpty.set(false);
                    }
                }else if(change.wasRemoved() ){
                    if(!wasEmpty.get() && addedNewFiles.isEmpty()){
                        wasEmpty.set(true);
                    }
                }
            };
            addedNewFiles.addListener(new WeakSetChangeListener<>(innerListener));

        }else {
            logger.warning("path is " + diskRealPath);
            throw new RuntimeException("folder path should be valid");
        }
    }

    public ObservableList<TransMod> getTransMods(){
        return transMods;
    }

    public ObservableSet<File> getAddedFiles() {
        return addedNewFiles;
    }

    public boolean isAddedFilesEmpty(){
        return addedNewFiles.isEmpty();
    }

    public void addToIgnoreAndCheck(String filePath){
        final File file = new File(filePath);
        addedNewFiles.remove(file);
        addToIgnore(filePath);
    }

    public void addToIgnore(String filePath){
        ignoreFiles.add(filePath);
    }

    public void removeFromIgnore(String path){
        ignoreFiles.remove(path);
    }

    public void addNewFile(File file){
        if(!fileToModMap.containsKey(file.getAbsolutePath()))
            addedNewFiles.add(file);
    }

    synchronized public void autoAddToCache(File f){
        if(!isIgnored(f.getAbsolutePath())) {
            if (!fileToModMap.containsKey(f.getAbsolutePath()))
                addedNewFiles.add(f);
        }
    }

    synchronized public void autoDeleteFromCache(File file){
        if(fileToModMap.containsKey(file.getAbsolutePath())){
            removeMod((AbstractMod) fileToModMap.get(file.getAbsolutePath()));
        }else if(ignoreFiles.contains(file.getAbsolutePath())){
            ignoreFiles.remove(file.getAbsolutePath());
        }else addedNewFiles.remove(file);
    }

    //Allowed same mod to be added
    //Note:this method call ObservableList's methods witch may bind to JavaFx components
    //To run this method in JavaFx-thread carefully
    public void addNewMod(AbstractMod mod){
        syncLock.lock();
        transMods.add(mod);
        fileToModMap.put(mod.getFilePath(), mod);
        syncLock.unlock();
    }

    //Note:this method call ObservableList's methods witch may bind to JavaFx components
    //To run this method in JavaFx-thread carefully
    public void addNewModAndCheck(AbstractMod mod){
        addedNewFiles.remove(new File(mod.getFilePath()));
        addNewMod(mod);
    }

    public void addNewMods(List<AbstractMod> mods){
        mods.forEach(new Consumer<AbstractMod>() {
            @Override
            public void accept(AbstractMod mod) {
                addNewMod(mod);
            }
        });
    }

    //Note:this method call ObservableList's methods witch may bind to JavaFx components
    //To run this method in JavaFx-thread carefully
    public void removeMod(AbstractMod mod){
        try {
            removeMod(mod,false);
        } catch (IOException ignore) { }
    }

    //Note:this method call ObservableList's methods witch may bind to JavaFx components
    //To run this method in JavaFx-thread carefully
    public void removeMod(AbstractMod mod,boolean removeFile) throws IOException {
        syncLock.lock();
        if(removeFile){
            Files.deleteIfExists(Paths.get(mod.getFilePath()));
        }
        transMods.remove(mod);
        fileToModMap.remove(mod.getFilePath());
        syncLock.unlock();
    }

    public boolean addedNewFilesContains(File file){
        return addedNewFiles.contains(file);
    }

    public void removeAddedFile(File file) {
        addedNewFiles.remove(file);
    }

    public Optional<TransMod> getModByPath(String path){
        return Optional.ofNullable(fileToModMap.get(path));
    }

    public boolean isIgnored(String path){
        return ignoreFiles.contains(path);
    }

    public int getModListSize(){
        return transMods.size();
    }

    private Maybe<ANY> readModListFrom(File file) {
        return Maybe.just(ANY.product())
                .observeOn(Schedulers.io())
                .map(new Function<ANY, ObservableList<AbstractMod>>() {
                    @Override
                    public ObservableList<AbstractMod> apply(ANY any) throws Exception {
                        return FileHelper.read2ObservableList(file.toPath());
                    }
                })
                .observeOn(JavaFxScheduler.platform())
                .map(new Function<ObservableList<AbstractMod>, ANY>() {
                    @Override
                    public ANY apply(ObservableList<AbstractMod> list) throws Exception {
                        addNewMods(list);
                        return ANY.product();
                    }
                }).observeOn(JavaFxScheduler.platform());
    }

    private Maybe<ANY> readIgnoreFilesFrom(ANY any,File file) throws IOException {
        return Maybe.just(file)
                .flatMap(new Function<File, MaybeSource<ANY>>() {
                    @Override
                    public MaybeSource<ANY> apply(File file) throws Exception {
                        ignoreFiles.clear();
                        ignoreFiles.addAll(FileHelper.read2Set(file.toPath()));
                        return Maybe.just(any);
                    }
                });
    }

    private void writeModListTo(File file) throws IOException {
        if(!file.exists()){
            file.createNewFile();
        }
        FileHelper.write(transMods,file.toPath());
    }

    private void writeIgnoreFilesTo(File file) throws IOException {
        if(!file.exists()){
            file.createNewFile();
        }
        FileHelper.write(ignoreFiles,file.toPath());
    }

    private boolean isIgnoreFile(File file){
        return file.getAbsolutePath().equals(getIgnoreFilePath());
    }

    private boolean isModsFile(File file){
        return file.getAbsolutePath().equals(getInfoFilePath());
    }

    synchronized public void sync2Disk(FILE file) throws IOException {
        if(file == FILE.IGNORE)
            sync2Disk(new File(getIgnoreFilePath()));
        else if(file == FILE.INFORMATION)
            syncInfo2Disk(new File(getInfoFilePath()));
        else if(file == FILE.BOTH)
            sync2Disk(new File(getIgnoreFilePath()),new File(getInfoFilePath()));

    }

    private void sync2Disk(@Nonnull File ignoreFile,@Nonnull File modsInfoFile) throws IOException {
        sync2Disk1(ignoreFile,modsInfoFile);
    }

    private void sync2Disk(@Nonnull File ignoreFile) throws IOException {
        sync2Disk1(ignoreFile,null);
    }

    private void syncInfo2Disk(@Nonnull File modsInfoFile) throws IOException {
        sync2Disk1(null,modsInfoFile);
    }

    private void sync2Disk1(File ignoreFile,File modsInfoFile) throws IOException {
        if(ignoreFile != null){
            writeIgnoreFilesTo(ignoreFile);
        }
        if(modsInfoFile != null){
            writeModListTo(modsInfoFile);
        }
    }

    public void addAddedFilesListener(ChangeListener<Boolean> changeListener){
        wasEmpty.addListener(changeListener);
    }

    public void removeAddedFilesListener(ChangeListener<Boolean> changeListener){
        wasEmpty.removeListener(changeListener);
    }

    synchronized public void syncFromDisk(){

        readModListFrom(new File(getInfoFilePath()))
                .flatMap((Function<ANY, MaybeSource<ANY>>) any -> readIgnoreFilesFrom(any,new File(getIgnoreFilePath())))
                .observeOn(JavaFxScheduler.platform())
                .map(any -> {
                    //delete the mods that had been removed
                    //Note:guarantee the thread is javafx-thread
                    transMods.removeIf(new Predicate<TransMod>() {
                        @Override
                        public boolean test(TransMod mod) {
                            return !Files.exists(Paths.get(((WrapperMod) mod).getFilePath()));
                        }
                    });
                    return any;
                }).observeOn(Schedulers.computation())
                .map(any -> {
                    File folder = new File(diskRealPath);
                    Set<File> allFiles = FileHelper.getFilesFrom(folder);
                    //.push new file(which one needs scan)
                    allFiles.forEach(file -> {
                        if (!getModByPath(file.getAbsolutePath()).isPresent() && !ignoreFiles.contains(file.getAbsolutePath())
                                && !isIgnoreFile(file) && !isModsFile(file) && (file.getName().endsWith(".jar") ||
                                file.getName().endsWith(".litemod"))) {
                            addNewFile(file);
                        }
                    });
                    return any;
                }).subscribe(new DisposableMaybeObserver<ANY>() {
            @Override
            public void onSuccess(ANY any) {
                logger.info("load to cache success");
            }

            @Override
            public void onError(Throwable e) {
                logger.warning("load to cache failed"+ e.toString());
            }

            @Override
            public void onComplete() {
                logger.info("load to cache finished");
            }
        });

    }

    public String getInfoFilePath(){
        return diskRealPath + "/" + infoFileName;
    }

    public String getIgnoreFilePath(){
        return diskRealPath + "/" + ignoreFileName;
    }

    public String getDiskRealPath() {
        return diskRealPath;
    }

    //Note:this method call ObservableList's methods witch may bind to JavaFx components
    //To run this method in JavaFx-thread carefully
    synchronized public void resetPath(String diskRealPath)throws Exception{
        if(!new File(diskRealPath).isDirectory())
            throw new RuntimeException("folder path should be valid");
        this.diskRealPath = diskRealPath;
        clearCache();
    }


    private void clearCache(){
        ignoreFiles.clear();
        addedNewFiles.clear();
        fileToModMap.clear();
        wasEmpty.setValue(true);
        transMods.clear();
    }
}
