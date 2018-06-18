package co.com.s4n.training.java.vavr;

import io.vavr.Lazy;
import io.vavr.concurrent.Future;
import org.junit.Test;

public class LazySuite {

    @Test
    public void EjercicioLazy(){
        Lazy<Future<String>> f1 = Lazy.of(()-> Future.of(() ->{
            Thread.sleep(500);
            return "Andres";
        }));
        Lazy<Future<String>> f2 = Lazy.of(()-> Future.of(() ->{
            Thread.sleep(800);
            return "Correa";
        }));
        Lazy<Future<String>> f3 = Lazy.of(()-> Future.of(() ->{
            Thread.sleep(300);
            return "Giraldo";
        }));

        long inicio = System.nanoTime();

        Future<String> result =
                f1.get().flatMap(s -> f2.get()
                        .flatMap(s1 -> f3.get()
                                .flatMap(s2 -> Future.of(() -> s+s1+s2))));


        result.await().get();

        long fin = System.nanoTime();
        Double elapsed = (fin-inicio)*Math.pow(10 ,- 6);
        System.out.println("Diferencia : "+elapsed);



        //Memoizing - se demora menos , porque ya esta en memoria
        long inicio2 = System.nanoTime();

        Future<String> result2 =
                f1.get().flatMap(s -> f2.get()
                        .flatMap(s1 -> f3.get()
                                .flatMap(s2 -> Future.of(() -> s+s1+s2))));
        result2.await().get();

        long fin2 = System.nanoTime();
        elapsed = (fin2-inicio2)*Math.pow(10 ,- 6);
        System.out.printf("Diferencia Segundo : "+elapsed);

    }




}
