package com.github.karsaig.approvalcrest.testdata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"varargs"})
public class BeanWithGenericIterable<K, T> {

    private final String dummyString;
    private final Set<T> set;
    private final Map<K, T> map;
    private final HashSet<T> hashSet;
    private final HashMap<K, T> hashMap;
    private final T[] array;

    public BeanWithGenericIterable(String dummyString, Set<T> set, Map<K, T> map, HashSet<T> hashSet, HashMap<K, T> hashMap, T[] array) {
        this.dummyString = dummyString;
        this.set = set;
        this.map = map;
        this.hashSet = hashSet;
        this.hashMap = hashMap;
        this.array = array;
    }

    public String getDummyString() {
        return dummyString;
    }

    public Set<T> getSet() {
        return set;
    }

    public Map<K, T> getMap() {
        return map;
    }

    public HashSet<T> getHashSet() {
        return hashSet;
    }

    public HashMap<K, T> getHashMap() {
        return hashMap;
    }

    public T[] getArray() {
        return array;
    }

    public static class Builder<K, T> {
        private String dummyString;
        private Set<T> set;
        private Map<K, T> map;
        private HashSet<T> hashSet;
        private HashMap<K, T> hashMap;
        private T[] array;

        public static <K, T> Builder<K, T> bean() {
            return new Builder<>();
        }

        public Builder<K, T> dummyString(String string) {
            this.dummyString = string;
            return this;
        }

        public Builder<K, T> set(Set<T> set) {
            this.set = set;
            return this;
        }

        public Builder<K, T> map(Map<K, T> map) {
            this.map = map;
            return this;
        }

        public Builder<K, T> hashSet(HashSet<T> hashSet) {
            this.hashSet = hashSet;
            return this;
        }

        public Builder<K, T> hashMap(HashMap<K, T> hashMap) {
            this.hashMap = hashMap;
            return this;
        }

        @SafeVarargs
        public final Builder<K, T> array(T... array) {
            this.array = array;
            return this;
        }

        public BeanWithGenericIterable<K, T> build() {
            return new BeanWithGenericIterable<>(dummyString, set, map, hashSet, hashMap, array);
        }
    }
}
