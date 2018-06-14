package co.com.s4n.training.java.jdk;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class CompletableFutureSuite {

    private void sleep(int milliseconds){
        try{
            Thread.sleep(milliseconds);
        }catch(Exception e){
            System.out.println("Problemas durmiendo hilo");
        }
    }

    private void imprimirMensaje(String mensaje){

        Date date = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SS ");

        System.out.println(sdf.format(date)+mensaje);
    }

    public class persona {

        String name;
        int age;

        public persona(String name, int age) {
            this.name = name;
            this.age = age;
        }

    }

    @Test
    public void t1() {

        CompletableFuture<String> completableFuture
                = new CompletableFuture<>();


        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.submit(() -> {
            Thread.sleep(300);

            completableFuture.complete("Hello");
            return null;
        });
            System.out.println(Thread.currentThread().getName());

        try {
            String s = completableFuture.get(500, TimeUnit.MILLISECONDS);
            assertEquals(s, "Hello");
        }catch(Exception e){
            assertTrue(false);
        }finally{
            executorService.shutdown();

        }

    }

    @Test
    public void t2(){
        CompletableFuture<String> completableFuture
                = new CompletableFuture<>();

        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.submit(() -> {
            Thread.sleep(300);

            completableFuture.complete("Hello");
            return null;
        });

        try {
            String s = completableFuture.get(500, TimeUnit.MILLISECONDS);
            assertEquals(s, "Hello");
        }catch(Exception e){
            assertTrue(false);
        }finally{
            executorService.shutdown();
        }
    }

    @Test
    public void t3(){
        // Se puede construir un CompletableFuture a partir de una lambda Supplier (que no recibe parámetros pero sí tiene retorno)
        // con supplyAsync
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            sleep(300);
            return "Hello";
        });

        try {
            String s = future.get(500, TimeUnit.MILLISECONDS);
            assertEquals(s, "Hello");
        }catch(Exception e){

            assertTrue(false);
        }
    }

    @Test
    public void t4(){

        int i = 0;
        // Se puede construir un CompletableFuture a partir de una lambda (Supplier)
        // con runAsync
        Runnable r = () -> {
            sleep(300);
            System.out.println("Soy impuro y no merezco existir");
        };

        // Note el tipo de retorno de runAsync. Siempre es un CompletableFuture<Void> asi que
        // no tenemos manera de determinar el retorno al completar el computo
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(r);

        try {
            voidCompletableFuture.get(500, TimeUnit.MILLISECONDS);
        }catch(Exception e){
            assertTrue(false);
        }
    }

    @Test
    public void t5(){

        String testName = "t5";

        System.out.println(testName + " - El test (hilo ppal) esta corriendo en: "+Thread.currentThread().getName());

        CompletableFuture<String> completableFuture
                = CompletableFuture.supplyAsync(() -> {
            System.out.println(testName + " - completbleFuture corriendo en el thread: "+Thread.currentThread().getName());
            return "Hello";
        });

        //thenApply acepta lambdas de aridad 1 con retorno
        CompletableFuture<String> future = completableFuture
                .thenApply(s -> {
                    imprimirMensaje(testName + " - future corriendo en el thread: "+Thread.currentThread().getName());
                    sleep(500);
                    return s + " World";
                })
                .thenApply(s -> {
                    imprimirMensaje(testName + " - future corriendo en el thread: "+Thread.currentThread().getName());

                    return s + "!";
                });

        try {
            assertEquals("Hello World!", future.get());
        }catch(Exception e){
            assertTrue(false);
        }
    }


    @Test
    public void t6(){

        String testName = "t6";

        CompletableFuture<String> completableFuture
                = CompletableFuture.supplyAsync(() -> {
            System.out.println(testName + " - completbleFuture corriendo en el thread: "+Thread.currentThread().getName());
            return "Hello";
        });

        // thenAccept solo acepta Consumer (lambdas de aridad 1 que no tienen retorno)
        // analice el segundo thenAccept ¿Tiene sentido?
        CompletableFuture<Void> future = completableFuture
                .thenAccept(s -> {
                    System.out.println(testName + " - future corriendo en el thread: " + Thread.currentThread().getName() + " lo que viene del futuro es: "+s);
                })
                //el thenAccept solo se pude realizar una vez
                .thenAccept(s -> {
                    System.out.println(testName + " - future corriendo en el thread: " + Thread.currentThread().getName() + " lo que viene del futuro es: "+s);
                });

    }

    @Test
    public void t7(){

        String testName = "t7";

        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(testName + " - completbleFuture corriendo en el thread: "+Thread.currentThread().getName());
            return "Hello";
        });

        //thenAccept solo acepta Consumer (lambdas de aridad 1 que no tienen retorno)
        CompletableFuture<Void> future = completableFuture
                .thenRun(() -> {
                    imprimirMensaje(testName + " - future corriendo en el thread: " + Thread.currentThread().getName());
                    sleep(500);
                })
                .thenRun(() -> {
                    imprimirMensaje(testName + " - future corriendo en el thread: " + Thread.currentThread().getName());
                });

    }

    @Test
    public void t8(){

        String testName = "t8";

        CompletableFuture<String> completableFuture = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println(testName + " - future corriendo en el thread: " + Thread.currentThread().getName());
                    return "Hello";
                })
                .thenCompose(s -> {
                    System.out.println(testName + " - compose corriendo en el thread: " + Thread.currentThread().getName());
                    return CompletableFuture.supplyAsync(() ->{
                        System.out.println(testName + " - CompletableFuture interno corriendo en el thread: " + Thread.currentThread().getName());
                        return s + " World"  ;
                    }
                    );
                });

        try {
            assertEquals("Hello World", completableFuture.get());
        }catch(Exception e){
            assertTrue(false);
        }
    }

    @Test
    public void t8Ejemplo(){

        String testName = "t8Ejemplo";

        CompletableFuture<persona> completableFuture = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println(testName + " - future corriendo en el thread: " + Thread.currentThread().getName());
                    return "Andres.24";
                })
                .thenCompose(s -> {

                    System.out.println(testName + " - compose corriendo en el thread: " + Thread.currentThread().getName());
                    return CompletableFuture.supplyAsync(() ->{
                                System.out.println(testName + " - CompletableFuture interno corriendo en el thread: " + Thread.currentThread().getName());

                                String[] name = s.split("\\.");
                                String nombre = name[0];
                                int edad = Integer.parseInt(name[1]);
                                persona pers = new persona(nombre,edad);

                                System.out.println("La personas es "+pers.name+" edad:"+pers.age);
                                return pers;
                            }
                    );
                });

        try {
            //el get solo se puede realizar una vez
            persona futuro = completableFuture.get();

            assertEquals("Andres", futuro.name);
            assertEquals(24, futuro.age);
        }catch(Exception e){
            assertTrue(false);
        }
    }



    @Test
    public void t9(){

        String testName = "t9";


        // El segundo parametro de thenCombina es un BiFunction la cual sí tiene que tener retorno.
        CompletableFuture<String> completableFuture = CompletableFuture
                .supplyAsync(() ->{
                    System.out.println(testName + " - CompletableFuture corriendo en el thread: " + Thread.currentThread().getName());
                    return "Hello ";
                })
                .thenCombine(
                        CompletableFuture.supplyAsync(() ->"World"),

                        (s1, s2) ->{
                            System.out.println(testName + " - lambdaCOmbinado corriendo en el thread: " + Thread.currentThread().getName());

                            return s1 + s2;
                        }
                 );

        try {
            assertEquals("Hello World", completableFuture.get());
        }catch(Exception e){
            assertTrue(false);
        }
    }

    @Test
    public void t10(){

        String testName = "t10";

        // El segundo parametro de thenAcceptBoth debe ser un BiConsumer. No puede tener retorno.
        CompletableFuture future = CompletableFuture.supplyAsync(() -> "Hello")
                .thenAcceptBoth(
                        CompletableFuture.supplyAsync(() -> " World"),
                        (s1, s2) -> System.out.println(testName + " corriendo en thread: "+Thread.currentThread().getName()+ " : " +s1 + s2));

        try{
            Object o = future.get();
        }catch(Exception e){
            assertTrue(false);

        }
    }

    @Test
    public void enlaceConSupplyAsync(){
        ExecutorService es = Executors.newFixedThreadPool(1);

        CompletableFuture f = CompletableFuture.supplyAsync(()->"Hello",es);

        CompletableFuture<String> f2 = f.supplyAsync(()->{
            imprimirMensaje("t11 Ejecutando a");
            sleep(500);
            return "a";
        }).supplyAsync(()->{
            imprimirMensaje("t11 Ejecutando b");
            return "b";
        });


        try {
            assertEquals(f2.get(),"b");
        }catch (Exception e){
            assertFalse(true);
        }
    }

    @Test
    public void enlaceConThenApplyAsync(){

        String testName= "testTheAppyAsync";
        ExecutorService es = Executors.newFixedThreadPool(4);

        CompletableFuture futuro = CompletableFuture.supplyAsync(()->"Hello ",es);

        CompletableFuture<String> f2 = futuro
                .thenApplyAsync(s -> {
                    imprimirMensaje(testName + " - ThenApplyAsinc en el thread (1): "+Thread.currentThread().getName());
                    sleep(500);
                    return s + "World";
                },es)
                .thenApplyAsync(s -> {
                    imprimirMensaje(testName + " - ThenApplyAsinc en el thread: (2)"+Thread.currentThread().getName());

                    return s + "!";
                },es);
        try {
            assertEquals("Hello World!", f2.get());
        }catch(Exception e){
            assertTrue(false);
        }
    }


    @Test
    public void t11(){

        //los hilos se deben decir el numero de hilos
        String testName = "t11";

        //caja de hilos - con el numero de hilos (1)
        ExecutorService es = Executors.newFixedThreadPool(1);
        //se le dice de que caja de hilos ,es
        CompletableFuture f = CompletableFuture.supplyAsync(()->"Hello",es);

        f.supplyAsync(() -> "Hello")
                .thenCombineAsync(
                    CompletableFuture.supplyAsync(() -> {
                        System.out.println(testName + " thenCombineAsync en Thread (1): " + Thread.currentThread().getName());
                        return " World";
                    }),
                    (s1, s2) -> {
                        System.out.println(testName + " thenCombineAsync en Thread (2): " + Thread.currentThread().getName());
                        return s1 + s2;
                    },
                    es
                );



    }

}
