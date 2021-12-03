package hu.webarticum.resourcemanager.common;

public abstract class CastUtil {

    private CastUtil() {
        // preventing instantiation
    }
    
    
    @SuppressWarnings("unchecked")
    public static <T, U> U cast(T object) {
        return (U) object;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> castClass(Class<?> clazz) {
        return (Class<T>) clazz;
    }
    
}
