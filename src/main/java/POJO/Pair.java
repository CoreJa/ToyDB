package POJO;

import java.io.Serializable;

public class Pair<K, V> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final K first;
    private final V second;
    public Pair(K k, V v){
        this.first = k;
        this.second = v;
    }
    public K getFirst() {
        return this.first;
    }
    public V getSecond() {
        return this.second;
    }
}
