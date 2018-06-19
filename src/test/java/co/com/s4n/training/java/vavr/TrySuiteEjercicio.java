package co.com.s4n.training.java.vavr;

import co.com.s4n.training.java.classEjercicioTry;
import io.vavr.control.Try;
import org.junit.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class TrySuiteEjercicio {

    @Test
    public void flatMapInOptionEjercicio(){
        Try<String> resultado =
                classEjercicioTry.convertirMayus("andres")
                                .flatMap(a -> classEjercicioTry.obtenerPrimeraLetra(a)
                                        .flatMap(d -> classEjercicioTry.filtro(d,25)
                                        .flatMap(b -> classEjercicioTry.convertirMinusyjuntar(b,"c")
                                        )));

        assertEquals(Try.of(() -> "ac"),resultado);
    }

    @Test
    public void flatMapInOptionEjercicio2(){
        Try<String> resultado =
                classEjercicioTry.convertirMayus("andres")
                        .flatMap(a -> classEjercicioTry.obtenerPrimeraLetra(a)
                                .flatMap(d -> classEjercicioTry.filtro(d,22)
                                        .flatMap(b -> classEjercicioTry.convertirMinusyjuntar(b,"c")
                                        )));

        assertTrue(resultado.isFailure());
    }


    @Test
    public void flatMapInOptionEjercicioWithRecover(){
        Try<String> resultado =
                classEjercicioTry.convertirMayus("andres")
                        .flatMap(a -> classEjercicioTry.obtenerPrimeraLetra(a)
                                .flatMap(d -> classEjercicioTry.filtro(d,22).recover(Exception.class,"a")
                                    .flatMap(b -> classEjercicioTry.convertirMinusyjuntar(b,"c")
                                )));

        assertEquals(Try.of(() -> "ac"),resultado);
    }

    @Test
    public void flatMapInOptionEjercicioWithRecoverWth(){
        Try<String> resultado =
                classEjercicioTry.convertirMayus("andres")
                        .flatMap(a -> classEjercicioTry.obtenerPrimeraLetra(a)
                                .flatMap(d -> classEjercicioTry.filtro(d,22).recoverWith(Exception.class,Try.of(() ->"a"))
                                        .flatMap(b -> classEjercicioTry.convertirMinusyjuntar(b,"c")
                                        )));

        assertEquals(Try.of(() -> "ac"),resultado);
    }

}
