package sa.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Provides convenient utility operations for collections.
 */
public class CollectionUtils {

    private CollectionUtils() {}

    public static <K, E> boolean addToMapSet(Map<K, Set<E>> map, K key, E element) {
        return map.computeIfAbsent(key, k -> newSet()).add(element);
    }

    public static <K1, K2, V> void addToMapMap(Map<K1, Map<K2, V>> map,
                                               K1 key1, K2 key2, V value) {
        map.computeIfAbsent(key1, k -> newMap()).put(key2, value);
    }

    public static <E> Set<E> newSet() {
        return new HashSet<>();
    }

    public static <K, V> Map<K, V> newMap() {
        return new HashMap<>();
    }

    /**
     *
     * @return a stream of all values of a map of map.
     */
    public static <K1, K2, V> Stream<V> getAllValues(Map<K1, Map<K2, V>> map) {
        return map.values()
                .stream()
                .flatMap(m -> m.entrySet().stream())
                .map(Map.Entry::getValue);
    }
}
