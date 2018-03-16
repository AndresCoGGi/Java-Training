package co.com.s4n.training.java.jdk;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;

public class LambdaSuite {

    @FunctionalInterface
    interface InterfaceDeEjemplo{
        int metodoDeEjemplo(int x, int y);
    }

    class ClaseDeEjemplo{
        public int metodoDeEjemplo1(int z, InterfaceDeEjemplo i){
            return z + i.metodoDeEjemplo(1,2);
        }

        public int metodoDeEjemplo2(int z, BiFunction<Integer, Integer, Integer> fn){
            return z + fn.apply(1,2);
        }
    }

    @Test
    public void smokeTest() {
        assertTrue(true);
    }

    @Test
    public void usarUnaInterfaceFuncional1(){

        InterfaceDeEjemplo i = (x,y)->x+y;

        ClaseDeEjemplo instancia = new ClaseDeEjemplo();

        int resultado = instancia.metodoDeEjemplo1(1,i);

        assertTrue(resultado==4);
    }

    @Test
    public void usarUnaInterfaceFuncional2(){

        BiFunction<Integer, Integer, Integer> f = (x, y) -> new Integer(x.intValue()+y.intValue());

        ClaseDeEjemplo instancia = new ClaseDeEjemplo();

        int resultado = instancia.metodoDeEjemplo2(1,f);

        assertTrue(resultado==4);
    }

    class ClaseDeEjemplo2{

        public int metodoDeEjemplo2(int x, int y, IntBinaryOperator fn){
            return fn.applyAsInt(x,y);
        }
    }
    @Test
    public void usarUnaFuncionConTiposPrimitivos(){
        IntBinaryOperator f = (x, y) -> x + y;

        ClaseDeEjemplo2 instancia = new ClaseDeEjemplo2();

        int resultado = instancia.metodoDeEjemplo2(1,2,f);

        assertEquals(3,resultado);
    }

    class ClaseDeEjemplo3{

        public String operarConSupplier(Supplier<Integer> s){
            return "El int que me han entregado es: " + s.get();
        }
    }

    @Test
    public void usarUnaFuncionConSupplier(){
        Supplier s1 = () -> {
            System.out.println("Cuándo se evalúa esto? (1)");
            return 4;
        };

        Supplier s2 = () -> {
            System.out.println("Cuándo se evalúa esto? (2)");
            return 4;
        };

        ClaseDeEjemplo3 instancia = new ClaseDeEjemplo3();

        String resultado = instancia.operarConSupplier(s2);

        assertEquals("El int que me han entregado es: 4",resultado);
    }

    class ClaseDeEjemplo4{

        private int i = 0;

        public void operarConConsumer(Consumer<Integer> c){
            c.accept(i);
        }
    }

    @Test
    public void usarUnaFuncionConConsumer(){
        Consumer<Integer> c1 = x -> {
            System.out.println("Me han entregado este valor: "+x);
        };

        ClaseDeEjemplo4 instancia = new ClaseDeEjemplo4();

        instancia.operarConConsumer(c1);


    }

}
