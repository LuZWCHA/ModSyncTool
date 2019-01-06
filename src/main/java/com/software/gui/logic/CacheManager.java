package com.software.gui.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public enum  CacheManager {
    INSTANCE;

    private Map<Key,MyCache> caches;

    CacheManager(){
        caches = new HashMap<>();
    }

    public void registerCache(Key key,MyCache cache){
        if(cache.doInit())
            cache.preInit();
        caches.put(key,cache);
    }

    public void removeCache(Key key){
        MyCache cache = caches.get(key);
        if(cache!= null && cache.doSave()){
            cache.doSave();
        }
        caches.remove(key);
    }

    public <T extends MyCache> T getCache(long id,boolean doInit){
        T t = getCache(id);

        if(doInit){
            if(t != null)
                t.preInit();
        }
        return t;
    }

    public <T extends MyCache> T getCache(Key key,boolean doInit){
        T t = getCache(key);

        if(doInit){
            if(t != null)
                t.preInit();
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    public <T extends MyCache> T getCache(long id){
        return (T)caches.get(new Key(id,null));
    }

    @SuppressWarnings("unchecked")
    public <T extends MyCache> T getCache(Key key){
        return (T)caches.get(key);
    }

    public void saveAll(){
        caches.forEach(new BiConsumer<Key, MyCache>() {
            @Override
            public void accept(Key key, MyCache cache) {
                if(cache!=null)
                cache.save();
            }
        });
    }

    public void initAll(){
        caches.forEach(new BiConsumer<Key, MyCache>() {
            @Override
            public void accept(Key key, MyCache cache) {
                if(cache!=null)
                cache.preInit();
            }
        });
    }

    public Stream stream(){
        return caches.entrySet().stream();
    }

    public static class Key{
        private long id;
        private Class clazz;

        Key(long id,Class clazz){
            this.id = id;
            this.clazz = clazz;
        }

        public static Key createKey(long id,Class clazz){
            return new  Key(id,clazz);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return id == key.id;
        }

        @Override
        public int hashCode() {

            return Objects.hash(id);
        }
    }
}
