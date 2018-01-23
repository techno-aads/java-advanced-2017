package ru.ifmo.ctddev.solutions.concurrent;

import ru.ifmo.ctddev.solutions.mapper.ParallelMapperImpl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Starter {

    public static  void main(String... args){



        Function<Integer, String> func = integer -> "№" + integer;

        List<Integer> data = Arrays.asList(12, 213, 23, 12 ,234 ,212 ,241);

        ParallelMapperImpl pmi = new ParallelMapperImpl(4);
        IterativeParallelism iterativeParallelism = new IterativeParallelism(pmi);

        List<String> result = null;
        List<String> result2 = null;
        int min = 0;

        long start = System.currentTimeMillis();

        try {
            result = pmi.map(func, data);
            result2 = pmi.map(func, data);
            min = iterativeParallelism.minimum(1, data, (o1, o2) -> o1 - o2);
            //System.out.println(iterativeParallelism.map(1, data, o1 -> o1 + "koko"));
            pmi.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(min);

        //result.stream().forEach((s) -> System.out.print(s + " "));
        //result2.stream().forEach((s) -> System.out.print(s + " "));


        System.out.println();
        System.out.println(System.currentTimeMillis() - start);

    }

    static double hardFunc(int a){
        double res = a;
        for(long i = 0; i< 1000000; i++){
            res = Math.sqrt(Math.sin(1304234 * 25456454));
        }
        return res;


    }

}
