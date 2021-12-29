package ink.ptms.chemdah.util;

public interface Function3<T1, T2, T3, R> {

    R invoke(T1 var1, T2 var2, T3 var3);
}