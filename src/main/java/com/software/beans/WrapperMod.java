package com.software.beans;

import com.google.common.hash.HashCode;
import com.sun.istack.internal.NotNull;

import javax.annotation.Nonnull;

/**
 * Created by 陆正威 on 2018/7/14.
 * To use the wrapperMod ,it's necessary to check if it's empty,or the instance would throw a NullException
 * However,because of requirement to send the information of a File that can not be scan as a mod ,the method
 * get/setFilePath will check the var 'actual' is null or not,to set the value or get the value form super-class
 */
public class WrapperMod<T extends AbstractMod> extends AbstractMod {
    private T actual;
    private static final long serialVersionUID = 4797998094119389433L;

    private WrapperMod(){
        super();
        actual = null;
    }

    public static <T extends AbstractMod>WrapperMod<T> createEmpty(){
        return new WrapperMod<T>();
    }

    public static <T extends AbstractMod> WrapperMod<T> create(@NotNull T t){
        return new WrapperMod<T>(t);
    }

    public WrapperMod(@NotNull T wrap){
        actual = wrap;
    }

    public void wrap(@NotNull T w){
        actual = w;
    }

    public T get(){
        return actual;
    }

    public boolean isEmpty(){
        return actual == null;
    }

    public void clear(){
        actual = null;
    }

    @Override
    public String toString() {
        return actual.toString();
    }

    @Override
    public String getFilePath() {
        if(actual == null)
            return super.getFilePath();
        return actual.getFilePath();
    }

    @Override
    public void setId(@Nonnull String id) {
        actual.setId(id);
    }

    @Override
    public void setVersion(String version) {
        actual.setVersion(version);
    }

    @Override
    public void setMode(short mode) {
        actual.setMode(mode);
    }

    @Override
    public String getModeDescribe(short mode) {
        return actual.getModeDescribe(mode);
    }

    @Override
    public String getSimpleDescribe() {
        return actual.getSimpleDescribe();
    }

    //for empty wrap mod to store filepath
    @Override
    public void setFilePath(String filePath) {
        if(actual == null)
            super.setFilePath(filePath);
        else
            actual.setFilePath(filePath);
    }

    @Override
    public HashCode getFileMD5() {
        return actual.getFileMD5();
    }

    @Override
    public void setFileMD5(HashCode fileMD5) {
        actual.setFileMD5(fileMD5);
    }

    @Override
    public String getId() {
        return actual.getId();
    }

    @Override
    public String getVersion() {
        return actual.getVersion();
    }

    @Override
    public short getMode() {
        return actual.getMode();
    }

    @Override
    public boolean equals(Object o) {
        if(actual != null)
            return actual.equals(o);
        return false;
    }

    @Override
    public boolean isSameAs(@Nonnull AbstractMod mod) {
        return actual.isSameAs(mod);
    }

    @Override
    public boolean isTrulySame(@Nonnull AbstractMod mod) {
        return actual.isTrulySame(mod);
    }

    @Override
    public int hashCode() {
        return actual.hashCode();
    }

    @Override
    public boolean isFileDataSameAs(@Nonnull AbstractMod mod) {
        return actual.isFileDataSameAs(mod);
    }

    @Override
    public boolean isFromServer() {
        return actual.isFromServer();
    }

    @Override
    public void setFromServer(boolean fromServer) {
        actual.setFromServer(fromServer);
    }
}
