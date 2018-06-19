package co.com.s4n.training.java;

import io.vavr.control.Try;

public class classEjercicioTry {

    public static Try<String> convertirMayus(String nombre){
        String nombreMayus = nombre.toUpperCase();
        System.out.println("nombre es "+nombreMayus);
        return Try.of(()->nombreMayus);
    }

    public static Try<String> obtenerPrimeraLetra(String name){
        char primerachar = name.charAt(0);
        String primera = Character.toString(primerachar);
        System.out.println("Primera letra "+primera);
        return Try.of(()->primera);
    }

    public static Try<String> convertirMinusyjuntar(String letra,String letra2){
        String letraMinus = letra.toLowerCase()+letra2;
        System.out.println("Letra Minuscula "+letraMinus);

        return Try.of(()->letraMinus);
    }

    public static Try<String> filtro(String letra, int edad){
        System.out.println("Edad "+edad);
        return edad>24 ? Try.of(()->letra) : Try.failure(new Exception("NO"));
    }

}
