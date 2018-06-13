package co.com.s4n.training.java.jdk;

import static org.junit.Assert.*;

import co.com.s4n.training.java.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamsSuite {
    @Test
    public void smokeTest() {
        assertTrue(true);
    }

    @Test
    public void testStreams1(){
        List<String> myList = Arrays.asList("a1", "a2", "b1", "c2", "c1");

        //converir coleccion a streams
        Stream<String> resultadoStream = myList
                .stream()
                //la s -> son los elementos
                .filter(s -> s.startsWith("c"))
                .map(String::toUpperCase)
                //.map(s -> s.toUpperCase())
                .sorted();

        //convertir stream a coleccion

        List<String> resCollect = resultadoStream.collect(Collectors.toList());

        assertTrue(resCollect.size()==2);
        assertTrue(resCollect.contains("C1"));
        assertTrue(resCollect.contains("C2"));

    }

    @Test
    public void testStreams2(){
        Optional<String> first = Arrays.asList("a1", "a2", "a3")
                                        .stream()
                                        .findFirst();

        //compara con a1 lo primero de la lista y si no hay nada "NONE"
        assertEquals("a1",first.orElseGet(()->"NONE"));

    }

    @Test
    public void testStreams3(){
        Optional<String> first = Stream.of("a1", "a2", "a3")
                .findFirst();

        assertEquals("a1",first.orElseGet(()->"NONE"));

    }

    @Test
    public void testStreams4(){
        OptionalInt first = IntStream.range(1, 4)
                .findFirst();

        assertEquals(1,first.orElseGet(()->666));

    }


    @Test
    public void testStreams6(){
        OptionalDouble average = Arrays.stream(new int[]{1, 2, 3})
                .map(n -> 2 * n + 1)
                .average();

        assertEquals(5D,average.orElseGet(()->666),0D);

    }

    @Test
    public void testStreams7(){
        OptionalInt max = Stream.of("a1", "a2", "a3")
                .map(s -> s.substring(1))
                .mapToInt(Integer::parseInt)
                .max();

        assertEquals(3,max.orElseGet(()->666));


    }

    @Test
    public void testStreams8(){
        Optional<String> first = IntStream.range(1, 4)
                .mapToObj(i -> "a" + i)
                .findFirst();

        assertEquals("a1",first.orElseGet(()->"NONE"));

    }

    @Test
    public void testStreams9() {
        List<String> collect = Stream.of(1.0, 2.0, 3.0)
                .mapToInt(Double::intValue)
                .mapToObj(i -> "a" + i)
                .collect(Collectors.toList());

        assertEquals(collect.size(),3);
        assertTrue(collect.contains("a2"));
    }

    @Test
    public void stramsContienenObjetos(){
        class MyClass{
            int i;
            public MyClass(int i){
                this.i = i;
            }

            public MyClass(Integer i){
                this.i = i.intValue();
            }

            @Override
            public String toString(){
                return String.valueOf(i);
            }
        }

        // Esta conversion no funciona :(
        /*
        List<MyClass> nuevaLista = Stream.of(1, 2, 0, 3, 4).map(MyClass::new)
                .collect(Collectors.toList());
        */

        List<MyClass> nuevaLista = Stream.of(1, 2, 0, 3, 4)
                .map(x -> new MyClass(x.intValue()))
                .collect(Collectors.toList());


        assertTrue(nuevaLista.size()==5);
        assertTrue(nuevaLista.get(0).toString().equals("1"));

    }

    @Test
    public void stramsContienenObjetos3(){


        // Esta conversion no funciona :(
        List<MyClassWithInt> nuevaLista = Stream
                .of(1, 2, 0, 3, 4)
                .map(MyClassWithInt::new)
                .collect(Collectors.toList());


        assertTrue(nuevaLista.size()==5);
        assertTrue(nuevaLista.get(0).toString().equals("1"));

    }

    @Test
    public void stramsContienenObjetos2(){

        // Qué súper vuelta hay que dar para lograr lo que queríamos :(

        List<MyClass> nuevaLista = Stream.of(1, 2, 0, 3, 4)
                .mapToInt(Integer::intValue)
                .mapToObj(MyClass::new)
                .collect(Collectors.toList());


        assertTrue(nuevaLista.size()==5);
        assertTrue(nuevaLista.get(0).toString().equals("1"));

    }

    @Test
    public void testStreams10() {

        System.out.println("--------------- REVISA EL ORDEN DE LA SALIDA ----------------");
        Stream.of("d2", "a2", "b1", "b3", "c")
                .filter(s -> {
                    System.out.println("filter: " + s);
                    return true;
                })
                .forEach(s -> System.out.println("forEach: " + s));

        System.out.println("--------------------------------------------------------------");
    }

    @Test
    public void testStream11(){
        // Cuántos elementos pasan por el stream?
        boolean b = Stream.of("d2", "a2", "b1", "b3", "c")
                .map(s -> {
                    return s.toUpperCase();
                })
                .anyMatch(s -> {
                    return s.startsWith("A");
                });

        assertTrue(b);

    }

    @Test
    public void testStreams12(){
        List<String> collect = Stream.of("d2", "a2", "b1", "b3", "c")
                .map(s -> {
                    return s.toUpperCase();
                })
                .filter(s -> {
                    return s.startsWith("A");
                }).collect(Collectors.toList());

        assertTrue(collect.size()==1);
        assertTrue(collect.contains("A2"));
    }


    @Test
    public void testStreams13() {
        //TODO: cambia el orden de map y filter
        assertTrue(true);
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void testStreams14() {
        Stream<String> stream =
                Stream.of("d2", "a2", "b1", "b3", "c")
                        .filter(s -> s.startsWith("a"));

        boolean b = stream.anyMatch(s -> true);
        assertTrue(b);

        //Un stream no se puede volver a usar despues de haberse ejecutado una operacion final sobre el :(
        stream.noneMatch(s -> true);
    }

    @Test
    public void testStreams15() {

        Supplier<Stream<String>> streamSupplier =
                () -> Stream.of("d2", "a2", "b1", "b3", "c")
                        .filter(s -> s.startsWith("a"));

        boolean b = streamSupplier.get().anyMatch(s -> true);
        boolean b1 = streamSupplier.get().noneMatch(s -> true);

        assert(b);
        assert(b1);

    }


    //Collect

    // Collect accepts a Collector which consists of four different operations: a supplier, an accumulator, a combiner and a finisher.

    class Person {
        String name;
        int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Test
    public void testStreams16() {
        List<Person> persons =
                Arrays.asList(
                        new Person("Max", 18),
                        new Person("Peter", 23),
                        new Person("Pamela", 23),
                        new Person("David", 12));

    }


    @Test
    public void testStreams17() {
        List<Person> persons =
                Arrays.asList(
                        new Person("Max", 18),
                        new Person("Peter", 23),
                        new Person("Pamela", 23),
                        new Person("David", 12));

        List<Person> filtered =
                persons
                        .stream()
                        .filter(p -> p.name.startsWith("P"))
                        .collect(Collectors.toList());

        assertTrue(filtered.size()==2);

    }

    @Test
    public void testStreams18() {
        List<Person> persons =
                Arrays.asList(
                        new Person("Max", 18),
                        new Person("Peter", 23),
                        new Person("Pamela", 23),
                        new Person("David", 12));

        Map<Integer, List<Person>> personsByAge = persons
                .stream()
                .collect(Collectors.groupingBy(p -> p.age));

        assertTrue((personsByAge.get(new Integer(23)).size()==2));

        // Como verificamos que las dos personas de edad 23

    }

    @Test
    public void testStreams19() {
        List<Person> persons =
                Arrays.asList(
                        new Person("Max", 18),
                        new Person("Peter", 23),
                        new Person("Pamela", 23),
                        new Person("David", 12));

        Double averageAge = persons
                .stream()
                .collect(Collectors.averagingInt(p -> p.age));

        assertEquals(averageAge, 19D, 0D);

    }


    //FlatMap

    @Test
    public void testStreams20(){

        List<Integer> collect = Stream
                .of(1, 2, 3, 4)
                .flatMap(x -> Stream.of(x, x + 1))
                .collect(Collectors.toList());

        assertTrue(collect.size()==8);

    }

    @Test
    public void collectingPersons(){
        Stream<CollectablePerson> personas = Stream.of(new CollectablePerson("Juan", 10), new CollectablePerson("Felipe", 20));

        CollectablePerson persona = personas.collect(new PersonCollector());

        System.out.println("persona:" + persona.name);
    }
    //Reduce
    //ParalellStreams
    //https://dzone.com/articles/think-twice-using-java-8




}
