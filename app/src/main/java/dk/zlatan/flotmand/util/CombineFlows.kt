package dk.zlatan.flotmand.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Combines 6 flows into a single flow with type-safe parameters.
 * This avoids the array-based casting required by the standard combine operator.
 *
 * @param flow1 First flow
 * @param flow2 Second flow
 * @param flow3 Third flow
 * @param flow4 Fourth flow
 * @param flow5 Fifth flow
 * @param flow6 Sixth flow
 * @param transform Function that combines all 6 flow values into result type R
 * @return Combined flow of type R
 */
fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = combine(
    flow1,
    flow2,
    flow3,
    flow4,
    flow5,
    flow6
) { array ->
    @Suppress("UNCHECKED_CAST")
    transform(
        array[0] as T1,
        array[1] as T2,
        array[2] as T3,
        array[3] as T4,
        array[4] as T5,
        array[5] as T6
    )
}
