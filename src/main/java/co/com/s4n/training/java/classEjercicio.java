package co.com.s4n.training.java;

import io.vavr.collection.List;
import io.vavr.control.Option;


import static io.vavr.API.None;

public class classEjercicio {


    public static Option<String> convertirMayus(String nombre){
        String nombreMayus;
        nombreMayus = nombre.toUpperCase();
        System.out.println("nombre es "+nombreMayus);
        return Option.of(nombreMayus);
    }

    public static Option<String> obtenerPrimeraLetra(String name){
        char primerachar = name.charAt(0);
        String primera = Character.toString(primerachar);
        System.out.println("Primera letra "+primera);
        return Option.of(primera);
    }

    public static Option<String> convertirMinusyjuntar(String letra,String letra2){
        String letraMinus = letra.toLowerCase();
        letraMinus = letraMinus+letra2;
        System.out.println("Letra Minuscula "+letraMinus);
        return Option.of(letraMinus);
    }

    public static Option<String> filtro(String letra, int edad){
        System.out.println("Edad "+edad);
        return edad>24 ? Option.of(letra) : None();
    }

    public static Option<List<String>> añadirAlista(List<String> list, String nuevo){
        list = list.push(nuevo);
        return Option.of(list);

    }

}
