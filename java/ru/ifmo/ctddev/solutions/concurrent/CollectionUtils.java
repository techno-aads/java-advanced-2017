package ru.ifmo.ctddev.solutions.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Nikita Sokeran
 */
public final class CollectionUtils {
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    private CollectionUtils() {
    }

    public static <T> List<List<T>> createBatchesForParallelExecution(final List<T> list, final int threads) {
        final int batchSize = getBatchSizeForParallelExecution(threads, list.size());

        final List<List<T>> batches = new ArrayList<>();
        for (int left = 0; left < list.size(); left += batchSize) {
            final int right = Math.min(left + batchSize, list.size());
            batches.add(list.subList(left, right));
        }

        return batches;
    }

    private static int getBatchSizeForParallelExecution(final int threads, final int listSize) {
        final int batchSize = Collections.min(Arrays.asList(PROCESSORS, threads, listSize));
        return listSize / batchSize;
    }
}
