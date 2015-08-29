package ir.websearch.algo.helper;

import java.util.Collection;

public class CollectionUtils {
	
	public static <T> boolean isEmpty(Collection<T> list) {
	    return list == null || list.isEmpty();
	}
	
	public static <T> boolean isNotEmpty(Collection<T> list) {
	    return list != null && !list.isEmpty();
	}
	
}