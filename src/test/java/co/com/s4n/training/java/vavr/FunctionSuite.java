package co.com.s4n.training.java.vavr;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.junit.Test;
import io.vavr.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FunctionSuite {


    private String fun(String name) {
        return "Hello World " + name;
    }

    /**
     *  Se puede crear una funcion utilizando la referencia de un metodo
     */
    @Test
    public void t1() {
        Function1<String, String> function1 = Function1.of(this::fun);
        assertTrue("Failure - compose the funtion from reference", function1.apply("Juan").equals("Hello World Juan"));
    }

    /**
     *Esta funcion se utiliza para testFunctionTolift()
     */
    private int divideNumber (int a){
        if((a/4)<16){
            throw new IllegalArgumentException("Number invalid");
        }
        return a;
    }
    /**
     * En este test se validara el uso de lift y liftTry para obtener una funcion Total a partir de una parcial
     */
    @Test
    public void t2() {

        Function3<Integer,Integer,Integer,Integer> f = (a, b, c) -> a*c/b;
        Function3<Integer,Integer,Integer, Option<Integer>> fOption =  Function3.lift(f);
        Function1<Integer, Option<Integer>> f1Option = Function1.lift(this::divideNumber);
        Function3<Integer,Integer,Integer, Try<Integer>> fTry =  Function3.liftTry(f);
        Function1<Integer, Try<Integer>> f1Try = Function1.liftTry(this::divideNumber);

        assertEquals("Valide None with lift",true,!fOption.apply(1,0,2).isDefined());
        assertEquals("Valide Some with lift",true,f1Option.apply(80).isDefined());
        assertEquals("Valide Try Failure with liftTry",true,fTry.apply(1,0,2).isFailure());
        assertEquals("Valide Try Succes with liftTry",true,f1Try.apply(80).isSuccess());
    }


    /**
    * Validar la funcionalidad de AndThen, se puede usar el andThen entre funciones cuando
    * el parametro de salida de una de ellas es del mismo tipo de entrada del de la siguiente
    *
    * Validar la funcionalidad de Compose, se puede usar el compose entre funciones cuando
    * el parametro de salida de una de ellas es del mismo tipo de entrada del de la siguiente
    */
    @Test
    public void t3() {

        Function1<String, String> f = a -> a + " Primer paso";
        Function1<String, String> g = a -> a + " Segundo Paso";

        Function1<String, String> compositionAndThen = f.andThen(g);

        Function1<String, String> compositionCompose = g.compose(f);

        assertEquals("failure - implementation andThen",
                "Iniciar Primer paso Segundo Paso",
                compositionAndThen.apply("Iniciar"));

        assertEquals("failure - implementation Compose",
                "Iniciar Primer paso Segundo Paso",
                compositionCompose.apply("Iniciar"));
    }

    /**
     * En esta se puede observar mejor el orden como se deben componer los andThen, y los compose.
     */
    @Test
    public void t4() {
        Function1<String, Tuple2> f = a -> Tuple.of(a, 2);
        Function1<Tuple2, Integer> g = a -> ((Integer) a._2 + 10);

        Function1<String, Integer> compositionAndThen = f.andThen(g);
        Function1<String, Integer> compositionCompose = g.compose(f);
        assertTrue("failure - implementation andThen",
                compositionAndThen.apply("Iniciar") == 12);

        assertTrue("failure - implementation Compose",
                compositionCompose.apply("Iniciar") == 12);

    }



    /**
     * Se puede crear una función fijando los parametros de otra funcion
     */
    @Test
    public void t5(){

        Function2<Integer, Integer, Integer> add = (a, b) -> a + b;
        assertEquals("failure - Function add must return 5 for params (2,3) ", new Integer(5), add.apply(3 , 2));

        Function1<Integer , Integer> addTwo = add.apply(2);
        assertEquals("failure - Function addTwo must return the param plus 2", new Integer(5), addTwo.apply(3));
    }


    /**
     * Se puede aplicar parcialmente una función fijando el valor de
     * uno de los parámetros (currying)
     */
    @Test
    public void t6(){
        Function4<String, String, String, String, Integer> totalLength = (a, b, c, d) ->
                a.length() + b.length() + c.length() + d.length();

        Function1<String, Function1<String, Integer>> add2 = totalLength
                .curried()
                .apply("This is a title")
                .apply("This is a subtitle");

        int total = add2.apply("This is a paragraph").apply("This is a footer");

        assertEquals("failure - the total lenght did not match", 68, total);
    }

    /**
     * Se puede crear una función que lance una checked exception
     */
    @Test(expected = FileNotFoundException.class)
    public void t7() throws Throwable {
        CheckedFunction1<String, String> readFile = new CheckedFunction1<String, String>() {
            @Override
            public String apply(String s) throws FileNotFoundException {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(s);
                } catch (FileNotFoundException fnfe) {
                    throw fnfe;
                }
                return "OK";
            }
        };
        readFile.apply("somefile.txt");

        /**
         * En caso de function, este no es capaz de manejar la excepción y requiere
         * realizar un nuevo try si se desea lanzar el error
         */
        Function1<String, String> readFile2 = new Function1<String, String>() {
            @Override
            public String apply(String s) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(s);
                } catch (FileNotFoundException fnfe) {
                    try {
                        throw fnfe;
                    } catch (FileNotFoundException e) {
                        return "ERROR";
                    }
                }
                return "OK";
            }
        };
        assertEquals("failure - the function read the file successfully", "ERROR", readFile2.apply("somefile.txt"));
    }

    /**
     * memorizar en cache un valor que se obtiene de la primera ejecucion
     */
    @Test
    public void t8() {
        Function0<Double> useMemoized =   Function0.of(Math::random).memoized();
        Double val =    useMemoized.apply();
        Double valOne = useMemoized.apply();
        Double valTwo = useMemoized.apply();
        Double valThree = useMemoized.apply();
        assertEquals("Them some result in random",val,valOne);
        assertEquals("Them some result in random",val,valTwo);
        assertEquals("Them some result in random",val,valThree);
    }

}