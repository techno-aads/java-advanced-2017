package ru.ifmo.ctddev.solutions.concurrent;


        import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
        import ru.ifmo.ctddev.salinskii.iterativeparallelism.Operations.*;
        import java.util.Comparator;
        import java.util.List;
        import java.util.function.Predicate;


public class IterativeParallelism implements ScalarIP{

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        int partSize = Math.max(values.size() / threads, 1);
        int threadsNumb = Math.min(values.size(), threads);
        Threads[] thread = new Threads[threadsNumb];
        ListHandler<T> listHandler = new ListHandler<T>(values, comparator);
        for(int i = 0; i < threadsNumb; i++){
            int endIndex = (i == threadsNumb - 1) ? values.size() : (i + 1) * partSize;
            thread[i] = new Threads(new Maximum<>(comparator), values.subList(i * partSize, endIndex), i * partSize, listHandler);
        }
        for(Threads threadIter: thread){
            threadIter.thread.join();
        }
        return listHandler.getResult();
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        int partSize = Math.max(values.size() / threads, 1);
        int threadsNumb = Math.min(values.size(), threads);
        Threads[] thread = new Threads[threadsNumb];
        ListHandler<T> listHandler = new ListHandler<T>(values, comparator);
        for(int i = 0; i < threadsNumb; i++){
            int endIndex = (i == threadsNumb - 1) ? values.size() : (i + 1) * partSize;
            thread[i] = new Threads(new Minimum<>(comparator), values.subList(i * partSize, endIndex), i * partSize, listHandler);
        }
        for(Threads threads1: thread){
            threads1.thread.join();
        }
        return listHandler.getResult();
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        int partSize = Math.max(values.size() / threads, 1);
        int threadsNumb = Math.min(values.size(), threads);
        Threads[] thread = new Threads[threadsNumb];
        ListHandler<T> listHandler = new ListHandler<T>(values, predicate, true);
        for(int i = 0; i < threadsNumb; i++){
            int endIndex = (i == threadsNumb - 1) ? values.size() : (i + 1) * partSize;
            thread[i] = new Threads(new AllChecker<>(predicate), values.subList(i * partSize, endIndex), i * partSize, listHandler);
        }
        for(Threads threads1: thread){
            threads1.thread.join();
        }
        return listHandler.getPredicateInd();
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        int partSize = Math.max(values.size() / threads, 1);
        int threadsNumb = Math.min(values.size(), threads);
        Threads[] thread = new Threads[threadsNumb];
        ListHandler<T> listHandler = new ListHandler<T>(values, predicate, false);
        for(int i = 0; i < threadsNumb; i++){
            int endIndex = (i == threadsNumb - 1) ? values.size() : (i + 1) * partSize;
            thread[i] = new Threads(new AnyChecker<>(predicate), values.subList(i * partSize, endIndex), i * partSize, listHandler);
        }
        for(Threads threads1: thread){
            threads1.thread.join();
        }
        return listHandler.getPredicateInd();
    }
}
