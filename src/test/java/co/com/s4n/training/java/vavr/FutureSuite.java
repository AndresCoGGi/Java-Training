package co.com.s4n.training.java.vavr;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.concurrent.Promise;
import org.junit.Test;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.Patterns.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import static io.vavr.API.*;
import static org.junit.Assert.assertNotEquals;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;


public class FutureSuite {
    // Max wait time for results = WAIT_MILLIS * WAIT_COUNT (however, most probably it will take only WAIT_MILLIS * 1)
    private static final long WAIT_MILLIS = 50;
    private static final int WAIT_COUNT = 100;
    //se puede llamar 100 veces - sleep durante 50 milisegundos
    private static void waitUntil(Supplier<Boolean> condition) {
        int count = 0;
        while (!condition.get()) {
            if (++count > WAIT_COUNT) {
                //condicion no cumplida
                fail("Condition not met.");
            } else {
                Try.run(() -> Thread.sleep(WAIT_MILLIS));
            }
        }
    }

    /**
     * Se prueba que pasa cuando se crea un futuro con error.
     */
    @Test(expected = Error.class)
    public void testFutureWithError() {
        Future<String> future = Future.of(() -> {
            throw new Error("Failure");
        });
        //no hacerle get a un Futuro excepto si es condigo de pruebas
        future.get();
    }

    /**
     * El resultado de un futuro se puede esperar con onComplete
     */
    @Test
    public void testOnCompleteSuccess() {
        Future<String[]> futureSplit = Future.of(() -> "TEXT_TO_SPLIT".split("_"));
        //la lambda del onComplete se realiza cuando se ejecuta el hilo asi sea bien o mal
        //se realizan las modificaciones al futuro sin capturarlo en una variable
        //res valor del futuro -> un try del resultado
        futureSplit.onComplete(res -> {
            System.out.println("res "+res.get());
            //si el res tiene valor -> pregunta isSucces al try -> el get es del Try- ya que es Success
            if (res.isSuccess()) {
                for (int i = 0; i < res.get().length; i++) {
                    //covertimos en minusculas
                    res.get()[i] = res.get()[i].toLowerCase();
                }
            }
        });
        //bloquea el hilo principal hasta que el futuro termine
        futureSplit.await();
        String[] expected = {"text", "to", "split"};
        //Wait until we are sure that the second thread (onComplete) is done.
        //espera hasta que se acabe el hilo que esta en el onComplete
        waitUntil(() -> futureSplit.get()[2].equals("split"));
        assertArrayEquals("The arrays are different", expected, futureSplit.get());
    }


   @Test
   public void testOnCompleteSuccess2() {
       Future<String[]> futureSplit = Future.of(() -> "TEXT_TO_SPLIT".split("_"));
       //la lambda del onComplete se realiza cuando se ejecuta el hilo asi sea bien o mal
       //se realizan las modificaciones al futuro sin capturarlo en una variable
       //res valor del futuro -> un try del resultado
       futureSplit.onComplete(res -> {
           System.out.println("res "+res.get());
           //si el res tiene valor(arreglo de elementos) -> pregunta isSucces al try -> el get es del Try- ya que es Success
           if (res.isSuccess()) {
               for (int i = 0; i < res.get().length; i++) {
                   //covertimos en minusculas
                   res.get()[i] = res.get()[i].toLowerCase();
               }
           }
       });
       //bloquea el hilo principal hasta que el futuro termine - pero no se si la lambda ya termino.
       futureSplit.await();
       String[] expected = {"text", "to", "split"};
       //Wait until we are sure that the second thread (onComplete) is done.
       //espera hasta que se acabe el hilo que esta en el onComplete
       //esepra hasta que se complete el hilo principal y hasta que se cumpla la condicion
       waitUntil(() -> futureSplit.get()[2].equals("split"));
       assertArrayEquals("The arrays are different", expected, futureSplit.get());
   }

    /**
     *Valida la funcion de find aplicando un predicado que viene de una implementacion de la clase Iterable que contenga Futuros
     * Tener encuenta el primero que cumpla con el predicado y sea Oncomplete es el que entrega
     */
    @Test
    public void testFutureToFind() {
        //lista de vavr con un futuro de Integer
        List<Future<Integer>> myLista = List.of( Future.of(() -> 5+4), Future.of(() -> 6+9), Future.of(() -> 31+1),Future.of(() -> 20+9));

        //los primeros que cumplan la condicion
        Future<Option<Integer>> futureSome = Future.find(myLista, v -> v < 10);
        Future<Option<Integer>> futureSomeM = Future.find(myLista, v -> v > 31);
        Future<Option<Integer>> futureNone = Future.find(myLista, v -> v > 40);

        assertEquals("Valide find in the List with Future", Some(9), futureSome.get());
        assertEquals("Valide find in the List with Future", Some(32), futureSomeM.get());
        assertEquals("Valide find in the List with Future", None(), futureNone.get());
    }

    /**
     *Valida la funcion de find aplicando un predicado que viene de una implementacion de la clase Iterable que contenga Futuros
     */
    @Test
    public void testFutureToTransform() {
        Integer futuretransform = Future.of( () -> 9).transform(v -> v.getOrElse(12) + 80);
        Future<Integer> myResult= Future.of(() -> 9).transformValue(v -> Try.of(()-> v.get()+12));
        assertEquals("Valide transform in a Future",new Integer(89) ,futuretransform);
        assertEquals("Valide transform in a Future",new Integer (21) ,myResult.get());
    }

    /**
     *Valida la funcion de find aplicando un predicado que viene de una implementacion de la clase Iterable que contenga Futuros
     */
    @Test
    public void testFutureToOnFails() {
        //final para asignarle valor luego
        final String[] valor = {"default","pedro"};

        Consumer<Object> funcion = element -> {
            valor[1] = "fallo";
        };

        Future<Object> myFuture = Future.of(() -> {
            throw new Error("No implemented");
        });

        //hace algo cuando un futuro falla
        myFuture.onFailure(funcion);
        assertEquals("Validete Onfailure in Future", "pedro",valor[1]);
        //bloquea el hilo principal, pero no se si ya acabo el consumer
        myFuture.await();

        assertTrue("Validete Onfailure in Future",myFuture.isFailure());

        waitUntil(() -> valor[1].toString()=="fallo");

        assertEquals("Validete Onfailure in Future", "fallo",valor[1]);
    }

    /**
     *Se valida el uso de Map obteniendo la longitu de un String
     * Se valida el uso Flatmap obteniendo el resultado apartir de una suma
     */
    @Test
    public void testFutureToMap() {
        Future<Integer> myMap = Future.of( () -> "pedro")
                //se ejecuta en un hilo de la misma caja del futuro
                .map(v -> v.length());
        assertEquals("validate map with future",new Integer(5),myMap.get());
    }

    @Test
    public void testFutureToFlatMap() {

        Future<Integer> myFlatMap = Future
                .of( () ->Future.of(() -> 5+9))
                //v.await -> hasta que se complete el fututo - bloquea el hilo
                .flatMap(v -> Future.of(()->v.await().getOrElse(15)));

        assertEquals("validate map with future",new Integer(14),myFlatMap.get());
    }

    public Future<String> convertirMayus(String nombre){
        String nombreMayus;
        nombreMayus = nombre.toUpperCase();
        System.out.println("nombre es "+nombreMayus);
        return Future.of(() -> nombreMayus);
    }

    public Future<String> obtenerPrimeraLetra(String name){
        char primerachar = name.charAt(0);
        String primera = Character.toString(primerachar);
        System.out.println("Primera letra "+primera);
        return Future.of(() ->primera);
    }

    public Future<String> convertirMinusyjuntar(String letra,String letra2){
        String letraMinus = letra.toLowerCase()+letra2;
        System.out.println("Letra Minuscula "+letraMinus);
        return Future.of(() ->letraMinus);
    }

    public static Future<String> filtro(String letra, int edad){
        System.out.println("Edad "+edad);
        return edad>24 ? Future.of(() -> letra) : Future.of(() -> {throw new Error("No implemented");});

    }


    @Test
    public void futureEjercicio(){
            Future<String> resultado =
                    convertirMayus("andres")
                            .flatMap(a -> obtenerPrimeraLetra(a)
                                    .flatMap(d -> filtro(d,25)
                                        .flatMap(b -> convertirMinusyjuntar(b,"c")
                                    )));
            resultado.await();
            System.out.printf("resultado ejercicio "+resultado.get());
            assertEquals(resultado.get(),"ac");
    }

    //hilos ¿¿¿???
    @Test
    public void futureEjercicioFallo(){
        Future<String> resultado =
                convertirMayus("andres")
                        .flatMap(a -> obtenerPrimeraLetra(a)
                                .flatMap(d -> filtro(d,23)
                                        .flatMap(b -> convertirMinusyjuntar(b,"c")
                                        )));

        resultado.await();
        assertTrue(resultado.isFailure());
    }

    /**
     *Se valida el uso de foreach para el encademaient de futuros
     */
    @Test
    public void testFutureToForEach() {
        //listas de JAVA
        java.util.List<Integer> results = new ArrayList<>();
        java.util.List<Integer> compare = Arrays.asList(9,15,32,29);

        List<Future<Integer>> myLista = List.of(Future.of(() -> 5 + 4), Future.of(() -> 6 + 9), Future.of(() -> 31 + 1), Future.of(() -> 20 + 9));

        //recorremos la lista
        myLista.forEach(v -> {
            results.add(v.get());
        });
        assertEquals("Validate Foreach in Future", compare, results);
    }

    @Test
    public void forEachInFuture(){
        final String[] result = {"666"};
        Future<String> f1 = Future.of(() -> "1");
        f1.forEach(i -> result[0]=i);
        f1.await();
        //assertEquals(f1.get(),"1");

        //espera a que el hilo principal se ejecute y se cumpla
        waitUntil(() -> "1".equals(result[0]));
        assertEquals("1",result[0]);
    }

    @Test
    public void testOnComplete() {
        Future<String> futureSplit = Future.of(() -> "Hello!");

        Future<String> onComplete = futureSplit.onComplete(res -> {
            System.out.printf("Hello!");
        });
        futureSplit.await();
        assertEquals(futureSplit.get(),onComplete.get());
        assertSame("Failure - onComplete did not return the same future", futureSplit, onComplete);
    }

    @Test
    public void FoldOnFuture(){
        //El fold no se le puede aplicar a una instancia ya que es estatico

        Future<String> f1 =  Future.of(() ->  "1");
        Future<String> f2 =  Future.of(() ->  "2");
        Future<String> f3 =  Future.of(() ->  "3");


        Future<String> f4 =  Future
                .fold(List.of(f1,f2,f3),"",(x,y) -> x+y);

        assertEquals(f4.await().get(),"123");

    }

    @Test
    public void FoldOnFutureFail(){
        //El fold no se le puede aplicar a una instancia ya que es estatico

        Future<String> f1 =  Future.of(() ->  "1");
        Future<String> f2 =  Future.of(() ->  {throw new Error("No implemented");});
        Future<String> f3 =  Future.of(() ->  "3");


        //si falla un elemento de la lista, falla el resto
        Future<String> f4 =  Future
                .fold(List.of(f1,f2,f3),"",(x,y) -> x+y);

        f4.await();
        //assertEquals(f4.await().get(),"123");
        assertTrue(f4.isFailure());

    }

    @Test
    public void FoldEqualsFlatMap(){
        //El fold no se le puede aplicar a una instancia ya que es estatico

        Future<String> f1 =  Future.of(() ->  "1");
        Future<String> f2 =  Future.of(() ->  "2");
        Future<String> f3 =  Future.of(() ->  "3");


        Future<String> f4 =  Future
                .fold(List.of(f1,f2,f3),"",(x,y) -> x+y);


        Future<String> resFlatMap = f1.flatMap(a -> f2.flatMap(b -> {
            String p = a+b;
            return f3.flatMap(c -> Future.of(() -> p +c));
        }));

        assertEquals(resFlatMap.await().get(),resFlatMap.await().get());


    }

    /*public Future<String> myFold(List<Future<String>> futures, String zero, BiFunction<String, String, String> b){

        List<String> entrada = futures


        Integer r = l1.fold(0,(acc,el)->acc+el);



        return Future.of(() -> "");
    }

    @Test
    public void TestMyFold(){
        //El fold no se le puede aplicar a una instancia ya que es estatico

        Future<String> f1 =  Future.of(() ->  "1");
        Future<String> f2 =  Future.of(() ->  "2");
        Future<String> f3 =  Future.of(() ->  "3");

        Future<String> resutado = myFold(List.of(f1,f2,f3),"",(x,y) -> x+y);


    }*/



    /**
     * Se puede crear un future utilizando funciones lambda
     */
    @Test
    public void testFromLambda(){
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<String> future = Future.ofSupplier(service, ()-> Thread.currentThread().getName());
        String future_thread = future.get();
        String main_thread = Thread.currentThread().getName();
        assertNotEquals("Failure - the future must to run in another thread", main_thread, future_thread);
        assertTrue("Failure - the future must be completed after call get()", future.isCompleted());
    }

    /**
     * Se puede crear un future utilizando referencias a metodos
     */
    @Test
    public void testFromMethodRef(){
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<Double> future = Future.ofSupplier(service, Math::random);
        future.get();
        assertTrue("Failure - the future must be completed after call get()", future.isCompleted());
    }


    /**
     * Este metodo me permite coger el primero futuro que termine su trabajo, la coleccion de futuros debe
     * extender de la interfaz iterable
     */
    @Test
    public void testFutureFirstCompleteOf() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        ExecutorService service2 = Executors.newSingleThreadExecutor();

        Future<String> future2 = Future.ofSupplier(service, () -> {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ("Hello this is the Future 2");
        });
        Future<String> future = Future.ofSupplier(service2, () -> "Hello this is the Future 1");
        List<Future<String>> futureList = List.of(future,future2);
        Future<String> future3 = Future.firstCompletedOf(service,futureList);

        assertEquals("Failure - the future 2 complete his job first",
                "Hello this is the Future 1",future3.get());
    }

    /**
     * Se puede cambiar el valor de un Future.Failure por otro Future utilizando el metodo fallBackTo
     */
    @Test
    public void testFailureFallBackTo(){
        Future<String> failure = Future.of(() -> {throw new Error("No implemented");});
        String rescue_msg = "Everything is Ok!";
        Future<String> rescue_future = Future.of(() -> rescue_msg);
        Future<String> final_future = failure.fallbackTo(rescue_future);
        assertEquals("Failure - The failure must be mapped to the rescue message", rescue_msg, final_future.get());
    }

    /**
     * El metodo fallBackTo no tiene efecto si el future inicial es exitoso
     */
    @Test
    public void testSuccessFallBackTo(){
        String initial_msg = "Hello!";
        Future<String> success = Future.of(() -> initial_msg);
        Future<String> rescue_future = Future.of(() -> "Everything is Ok!");
        Future<String> final_future = success.fallbackTo(rescue_future);
        assertEquals("Failure - The success future must contain the initial value", initial_msg, final_future.get());
    }

    /**
     * al usar el metodo fallBackTo si los dos futures fallan el failure final debe contener el error del futuro inicial
     */
    @Test
    public void testFailureFallBackToFailure(){
        String initial_error = "I failed first!";
        Future<String> initial_future = Future.of(() -> {throw new Error(initial_error);});
        Future<String> rescue_future = Future.of(() -> {TimeUnit.SECONDS.sleep(1);throw new Error("Second failure");});
        Future<String> final_future = initial_future.fallbackTo(rescue_future);
        final_future.await();
        assertEquals("Failure - the result must be the first failure",
                initial_error,
                final_future.getCause().get().getMessage()); //Future -> Some -> Error -> String
    }

    /**
     * Se puede cancelar un futuro si este no ha sido completado aún
     */
    @Test
    public void testCancelFuture(){
        Future<String> future = Future.of(() -> {
            TimeUnit.SECONDS.sleep(2);
            return "End";});
        assertTrue("Failure - The future was not canceled", future.cancel());
        assertTrue("Failure - The future must be completed after cancel it", future.isCompleted());
        assertTrue("Failure - A canceled future must be a Failure",future.isFailure());
    }

    /**
     * No se puede cancelar un futuro completado
     */
    @Test
    public void testCancelAfterComplete(){
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<String> future = Future.of(service,() -> "Hello!");
        future.await();
        assertTrue("Failure - the future was not completed", future.isCompleted());
        assertFalse("Failure - the future was canceled after its ends", future.cancel());
    }

    /**
     * onFail, onSuccess y onComplete devuelven el mismo futuro que invoca los metodos
     */
    @Test
    public void testTriggersReturn() {
        Future<String> futureSplit = Future.of(() -> "Hello!");

        Future<String> onComplete = futureSplit.onComplete(res -> {/*do some side effect*/});
        Future<String> onSuccess = futureSplit.onSuccess(res ->{/*do some side effect*/});
        Future<String> onFail = futureSplit.onFailure(res -> {/*do some side effect*/});
        futureSplit.await();
        assertSame("Failure - onComplete did not return the same future", futureSplit, onComplete);
        assertSame("Failure - onSuccess did not return the same future", futureSplit, onSuccess);
        assertSame("Failure - onFail did not return the same future", futureSplit, onFail);
    }

    /**
     * Se prueba el poder realizar una acción luego de que un futuro finaliza.
     */
    @Test
    public void testOnSuccess() {
        String[] holder = {"Don't take my"};
        Future<String> future = Future.of(() -> "Ghost");
        future.onSuccess(s -> {
            assertTrue("Future is not completed", future.isCompleted());
            holder[0] += " hate personal";
        });
        waitUntil(() -> holder[0].length() > 14);
        assertEquals("Failure - The message wasn't change after success.", "Don't take my hate personal",holder[0]);
    }

    /**
     * Se puede crear un futuro como resultado de aplicar un fold a un objeto iterable compuesto de futuros
     */
    @Test
    public void testFoldOperation(){
        List<Future<Integer>> futureList = List.of(
                Future.of(()->0),
                Future.of(()->1),
                Future.of(()->2),
                Future.of(()->3));
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<String> futureResult = Future.fold(
                service, // Optional executor service
                futureList, // <Iterable>
                "Numbers on the list: ", // Seed
                (acumulator, element) -> acumulator + element); // Fold operation
        assertEquals("Failure - the result of the fold operation is incorrect",
                "Numbers on the list: 0123",
                futureResult.get());
    }

    /**
     * Un futuro se puede filtrar dado un predicado
     * filter retorna una nueva referencia
     */
    @Test
    public void testFilter() {
        Future<String> future = Future.successful("this_is_a_text");
        Future<String> some = future.filter(s -> s.contains("a_text"));
        Future<String> none = future.filter(s -> s.contains("invalid"));
        assertNotSame("Failure - The futures shouldn't be the same",future,some);
        assertNotSame("Failure - The futures shouldn't be the same",future,none);
        assertEquals("Failure - The filter was not successful", "this_is_a_text", some.get());
        assertTrue("Failure - The filter was successful", none.isEmpty());
    }

    /**
     *  Sequence permite cambiar una lista de futuros<T> a un futuro de una lista <T>,
     *  este devuelve por defecto un Futuro<stream>
     */
    @Test
    public void testFutureWithSequence() {
        List<Future<String>> listOfFutures = List.of(
                Future.of(() -> "1 mensaje"),
                Future.of(() -> "2 mensaje")
        );

        Future<Seq<String>> futureList = Future.sequence(listOfFutures);
        assertFalse("The future is already completed",futureList.isCompleted());
        assertTrue("Failure - futureList is not instance of Future",futureList instanceof Future);

        Stream<String> stream = (Stream<String>) futureList.get();
        assertEquals("Stream does not a List",List.of("1 mensaje","2 mensaje").asJava(),stream.asJava());
    }

    /**
     *  El Recover me sirve para recuperar futuros que hayan fallado, y se recupera el resultado con otro
     *  y se crea un futuro nuevo
     */
    @Test
    public void testFutureRecover() {
        //arreglos vacios
        final String[] thread1 = {""};
        final String[] thread2 = {""};

        Future<Integer> aFuture = Future.of(
                () -> {
                    Thread.sleep(1000);
                    System.out.println("testFutureRecover (1) "+Thread.currentThread().getName());
                    thread1[0] = Thread.currentThread().getName().toString();
                    return 2/0;
                }
        );
        //Math(it).of  ->> la excepcion que venga cambiela por cualquier cosa
        Future<Integer> aRecover = aFuture.recover(it -> Match(it).of(
                Case($(),() -> {
                    System.out.println("testFutureRecover (2) "+Thread.currentThread().getName());
                    thread2[0] = Thread.currentThread().getName().toString();
                    return 2;
                })
        ));

        aRecover.await();
        assertTrue("Failure - The future wasn't a success",aRecover.isSuccess());
        assertFalse("Failure - The threads should be different",thread1[0].equals(thread2[0]));
        assertEquals("Failure - It's not two",new Integer(2),aRecover.get());
    }

    @Test
    public void testFutureRecove2() {

        ExecutorService ex = Executors.newFixedThreadPool(1);
        //arreglos vacios
        final String[] thread1 = {""};
        final String[] thread2 = {""};



        Future<Integer> aFuture = Future.of(ex,
                () -> {
                    Thread.sleep(1000);
                    System.out.println("testFutureRecover2 (1) "+Thread.currentThread().getName());
                    thread1[0] = Thread.currentThread().getName().toString();
                    return 2/0;
                }
        );
        //Math(it).of  ->> la excepcion que venga cambiela por cualquier cosa
        Future<Integer> aRecover = aFuture.recover(it -> Match(it).of(
                Case($(),() -> {
                    System.out.println("testFutureRecover2 (2) "+Thread.currentThread().getName());
                    thread2[0] = Thread.currentThread().getName().toString();
                    return 2;
                })
        ));

        aRecover.await();
        assertTrue("Failure - The future wasn't a success",aRecover.isSuccess());
        assertTrue("Failure - The threads should be different",thread1[0].equals(thread2[0]));
        assertEquals("Failure - It's not two",new Integer(2),aRecover.get());
    }


    @Test
    public void testFutureRecove3() {

        ExecutorService ex = Executors.newFixedThreadPool(1);
        //arreglos vacios
        final String[] thread1 = {""};
        final String[] thread2 = {""};



        Future<Integer> aFuture = Future.of(ex,
                () -> {
                    Thread.sleep(1000);
                    System.out.println("testFutureRecover3 (1) "+Thread.currentThread().getName());
                    thread1[0] = Thread.currentThread().getName().toString();
                    return 2/0;
                }
        );
        //Math(it).of  ->> la excepcion que venga cambiela por cualquier cosa
        Future<Integer> aRecover = aFuture.recover(it -> Match(it).of(
                Case($(),() -> {
                    System.out.println("testFutureRecover3 (2) "+Thread.currentThread().getName());
                    thread2[0] = Thread.currentThread().getName().toString();
                    return 2/0;
                })
        ));

        aRecover.await();
        assertFalse("Failure - The future wasn't a success",aRecover.isSuccess());
        //assertTrue("Failure - The threads should be different",thread1[0].equals(thread2[0]));
        //assertEquals("Failure - It's not two",new Integer(2),aRecover.get());
    }


    /**
     *  El Recover me sirve para recuperar futuros que hayan fallado, y se recupera el futuro con otro
     *  y se crea un futuro nuevo
     */
    @Test
    public void testFutureRecoverWith() {
        final String[] thread1 = {""};
        final String[] thread2 = {""};
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<Integer> aFuture = Future.of(service,() -> {
            thread1[0] = Thread.currentThread().getName().toString();
            return 2 / 0;
        });
        Future<Integer> aRecover = aFuture.recoverWith(it -> Match(it).of(
                Case($(), () -> Future.of(() -> {
                    thread2[0] = Thread.currentThread().getName().toString();
                    return 1;
                }))
        ));
        aRecover.await();
        assertTrue("Failure - The future wasn't a success",aRecover.isSuccess());
        assertFalse("Failure - The threads should be different",thread1[0].equals(thread2[0]));
        assertEquals("Failure - It's not one",new Integer(1),aRecover.get());
    }

    /**
     * Validar pattern Matching a un future correcto.
     */
    @Test
    public void testFuturePatternMatchingSuccess() {
        Future<String> future = Future.of(() -> "Glad to help");
        String result = Match(future).of(
                Case($Future($(instanceOf(Error.class))), "Failure!"),
                Case($Future($()), "Success!"),
                Case($(), "Double failure"));
        assertEquals("Failure - The future should be a success", "Success!", result);
    }

    /**
     * Validar pattern Matching a un future correcto.
     */
    @Test
    public void testFuturePatternMatchingError() {

        Future<String> future = Future.of(() -> {
            throw new Error("Failure");
        });

        // Este test algunas veces tiene exito y algunas otras fracasa
        // Por que sera?

        String result = Match(future).of(
                Case($Future($Some($Failure($()))), "Failure!"),
                Case($Future($()), "Success!"),
                Case($(), "Double failure"));

        assertEquals("Failure - The future should be a success",
                "Failure!",
                result);
    }

    /**
     * Crear un futuro a partir de un Try fallido
     */
    @Test
    public void testFromFailedTry(){
        Try<String> tryValue = Try.of(() -> {throw new Error("Try again!");});
        Future<String> future = Future.fromTry(tryValue);
        future.await();
        assertTrue("Failure - A future from a failed Try must be Failure", future.isFailure());
        assertEquals("Failure - The cause of the failure future must be the same of the tryValue",
                tryValue.getCause(),
                future.getCause().get()); //Future -> Option -> Throwable
    }

    /**
     * Crear un futuro a partir de un Try exitoso
     */
    @Test
    public void testFromSuccessTry(){
        Try<String> tryValue = Try.of(() -> "Hi!");
        Future<String> future = Future.fromTry(tryValue);
        future.await();
        assertTrue("Failure - A future from a success Try must be success", future.isSuccess());
        assertEquals("Failure - A future from a success Try must be contain the value", "Hi!",future.get());
    }

    /**
     * Crear un futuro de la libreria vavr a partir de un futuro de java8
     */
    @Test
    public void testFromJavaFuture() {
        Callable<String> task = () -> Thread.currentThread().getName();
        ExecutorService service = Executors.newSingleThreadExecutor();
        java.util.concurrent.Future<String> javaFuture = service.submit(task);
        ExecutorService service2 = Executors.newSingleThreadExecutor();
        Future<String> future = Future.fromJavaFuture(service2, javaFuture);
        try {
            assertEquals("Failure - vavr Future and java Future had different results", javaFuture.get(), future.get());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Un futuro se puede crear a partir de una promesa
     */
    @Test
    public void testFutureFromPromise() {
        Promise<String> promise = Promise.successful("success!");
        //Future can be created from a promise
        Future<String> future = promise.future();
        future.await();
        assertTrue("The future did not complete", future.isCompleted());
        assertTrue("The promise did not complete", promise.isCompleted());
        assertEquals("The future does not have the value from the promise", "success!", future.get());
    }

    /**
     *Se valida la comunicacion de Futuros mediante promesas
     */
    @Test
    public void testComunicateFuturesWithPromise() {
        Promise<Integer> mypromise = Promise.make();
        Future<Object> myFuture = Future.of(()-> {
            mypromise.success(15);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "algo";
        });
        Future<Integer> myFutureOne = mypromise.future();
        myFutureOne.await();
        assertEquals("Failure - Validate Future with Promise",new Integer(15),myFutureOne.get());
        assertFalse("Failure - Validate myFuture is not complete",myFuture.isCompleted());
    }
}
