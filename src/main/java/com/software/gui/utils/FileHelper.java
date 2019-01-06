package com.software.gui.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.Traverser;
import com.sun.deploy.trace.FileTraceListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

public class FileHelper {

    public static <T> void write(List<T> objects, Path file) {
        try {
            // write object to file
            OutputStream fos = Files.newOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new ArrayList<>(objects));
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> ArrayList<T> read2ArrayList(Path file) {
        try {
            InputStream in = Files.newInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(in);
            List<T> list = (List<T>) ois.readObject();
            return list == null ? Lists.newArrayList() : Lists.newArrayList(list);
        }catch (NoSuchFileException e) {
            System.out.println("not create "+file.getFileName()+",skip.");
        }catch (IOException|ClassNotFoundException e){
            e.printStackTrace();
        }
        return Lists.newArrayList();
    }

    public static <T> void write(ObservableList<T> mods, Path file) {
        try {
            // write object to file
            OutputStream fos = Files.newOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new ArrayList<>(mods));
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static <T> ObservableList<T> read2ObservableList(Path file) {
        try {
            InputStream in = Files.newInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(in);
            List<T> list = (List<T>) ois.readObject();
            return list == null ? FXCollections.observableArrayList() : FXCollections.observableArrayList(list);
        }catch (NoSuchFileException e) {
            System.out.println("not create "+file.getFileName()+",skip.");
        }catch (IOException|ClassNotFoundException e){
            e.printStackTrace();
        }
        return FXCollections.emptyObservableList();
    }

    public static <T> void write(Set<T> mods, Path file) {
        try {
            // write object to file
            OutputStream fos = Files.newOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mods);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static <T> Set<T> read2Set(Path file) {
        try {
            InputStream in = Files.newInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(in);
            return (Set<T>) ois.readObject();
        }catch (NoSuchFileException e) {
            System.out.println("not create "+file.getFileName()+",skip.");
        }catch (IOException|ClassNotFoundException e){
            e.printStackTrace();
        }
        return Sets.newHashSet();
    }

    public static Set<File> getFilesFrom(File file){
        Set<File> allFiles = new HashSet<>();
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<File>();
            list.add(file);
            while (!list.isEmpty()){
                File temp = list.remove();
                if (temp.isDirectory()) {
                    File[] files = temp.listFiles();
                    if(files != null)
                        Collections.addAll(list, files);
                } else {
                    if(!temp.isHidden()){
                        if (temp.canRead() && temp.length() > 0) {
                            allFiles.add(temp);
                        }
                    }
                }
            }
        }
        return allFiles;
    }

    public static String getJarPath(){
        String jarWholePath = FileHelper.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            jarWholePath = java.net.URLDecoder.decode(jarWholePath, "UTF-8");
        } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        return new File(jarWholePath).getAbsolutePath();
    }

    public static String getJarDir(){
        return new File(getJarPath()).getParent();
    }
}
