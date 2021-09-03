package ink.ptms.chemdah.util

interface Function2<T1, T2, R> {

    operator fun invoke(input1: T1, input2: T2): R
}

interface Function3<T1, T2, T3, R> {

    operator fun invoke(input1: T1, input2: T2, input3: T3): R
}