package com.software.scan;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.software.beans.AbstractMod;
import com.software.beans.ObjectPool;
import com.software.beans.WrapperMod;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Created by 陆正威 on 2018/7/11.
 */
public abstract class AbstractScanProcess<T extends AbstractMod> extends ObjectPool.RecyclableObject implements ScanProcess{
    private WrapperMod<T> mod;
    private final Logger log = Logger.getLogger(AbstractScanProcess.class.getSimpleName());
    protected long sleepTime = 2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForgeModScan)) return false;
        ForgeModScan that = (ForgeModScan) o;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }

    public boolean scan(JarFile jarFile) throws InterruptedException {
        JarEntry e = getSubInfoFile(jarFile);
        int errorCount = 0;

        if(e != null){
            InputStream ins = null;
            try {
                ins = jarFile.getInputStream(e);
                mod = handle(ins);
            } catch (Exception e1) {
                log.throwing(this.getClass().getSimpleName(),"scan()",e1);
                errorCount++;
            }
            if(ins != null)
                try {
                    ins.close();
                } catch (IOException e1) {
                    log.throwing(this.getClass().getSimpleName(),"scan()",e1);
                    errorCount++;
                }

        }else{
            try {
                mod = handle(jarFile);
            } catch (InterruptedException e1){
                throw new InterruptedException();
            } catch (Exception e1) {
                log.throwing(this.getClass().getSimpleName(),"scan()",e1);
                errorCount++;
            }
        }
        if(mod.isEmpty())
            errorCount++;
        else {
            File file = new File(jarFile.getName());
            //mod.setFilePath(new File(jarFile.getName()).getAbsolutePath()); moved to JarScan so that empty mod will cache the filepath
            try {
                mod.setFileMD5(Files.asByteSource(file).hash(Hashing.goodFastHash(128)));
            } catch (IOException e1) {
                log.throwing(this.getClass().getSimpleName(),"scan()",e1);
                errorCount ++;
            }
        }
        return errorCount <=0;
    }


    //if want to do some tasks which will take too long time,make sure you have add the way to break off a "while"(for
    // example,throw a interrupt exception by Thread.sleep() or any other methods;because executorService need) or
    //nothing will happen after the JarView send the signal to stop the threads


    /**
     * @param jarFile the jarFile to get a MARK file for example(mod.info for forge mods) to send to the handle({@link com.software.scan.AbstractScanProcess handle(InputStream)})
     * @return the MARK file
     */
    protected abstract JarEntry getSubInfoFile(JarFile jarFile);

    /**
     * @param ins the MARK file's InputStream
     * @return if get the info return the WrapperMod instance else return WrapperMod.EMPTY instance
     * @throws Exception always throw a interrupt exception to finish the scan and try to recycle source
     *                      after that.
     * if do some time-consuming task make sure throwing a exception or the JarView.cancel() will not stop the worker thread
     */
    protected abstract WrapperMod<T> handle (InputStream ins) throws Exception;


    /**
     * @param jf the file
     * @return if get the info return the WrapperMod instance else return WrapperMod.EMPTY instance
     * @throws Exception always throw a interrupt exception to finish the scan and try to recycle source
     *                      after that.
     *  If you want to do some time-consuming task in this method, make sure throwing a exception or the JarView.cancel() will not stop the worker thread
     *  this method just invoke after the handle(InputStream) method get a empty WrapperMod instance;so if you want to skip that
     *  method just return WrapperMod.Empty();
     */
    protected abstract WrapperMod<T> handle(JarFile jf) throws Exception;

    @Override
    public WrapperMod<T> get(){
        return mod;
    }

    @Override
    public void setSleepTime(long time){
        sleepTime = time;
    }

}
