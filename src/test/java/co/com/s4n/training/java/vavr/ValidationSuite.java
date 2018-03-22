package co.com.s4n.training.java.vavr;

import io.vavr.collection.CharSeq;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.Test;
import io.vavr.Function1;
import io.vavr.control.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;

public class ValidationSuite
{
    class TestValidation {

        public String name;
        public Integer age;
        public Option<String> address;
        public String phone;
        public String alt1;
        public String alt2;
        public String alt3;
        public String alt4;

        public TestValidation(String name, Integer age, Option<String> address, String phone, String alt1, String alt2, String alt3, String alt4) {
            this.name = name;
            this.age = age;
            this.address = address;
            this.phone = phone;
            this.alt1 = alt1;
            this.alt2 = alt2;
            this.alt3 = alt3;
            this.alt4 = alt4;
        }

        @Override
        public String toString() {
            return name + "," + age + "," + address.getOrElse("none") + "," +
                    phone + "," + alt1 + "," + alt2 + "," + alt3 + "," + alt4;
        }
    }

    class MyClass {
        public String age;
        public String amount;

        public MyClass(String age,String amount) {
            this.amount = amount;
            this.age = age;
        }}


    private Validation<String, String> validateAge(Integer age) {
        if(age<14)return Validation.invalid("Age must be at least " + 14);
        else return Validation.valid(age.toString());
    }

    private Validation<String, String> validateAmount(Integer monto) {
        if (monto < 14000) return Validation.invalid("Amount must be at least " + 1400);
        else return Validation.valid(monto.toString());
    }

    /**
     * Validation con los dos casos válidos. Se ejecuta satisfactoriamente la lambda entregada a ap
     */

    @Test
    public void testValidation1() {

        Validation<Seq<String>, MyClass> res =  Validation
                .combine(validateAge(15),
                        validateAmount(15000))
                .ap(MyClass::new);

        MyClass myClass = res.get();

        assertTrue("ap should be valid",res.isValid());
        assertEquals("La edad debe ser 15 en la clase instanciada ", myClass.age, "15");

    }

    /**
     * Validation con un solo caso exitoso y el otro fallido. No se debe ejecutar la lambda entregada a ap
     * y sin embargo todas las funciones se deben ejecutar.
     */

    @Test(expected = java.util.NoSuchElementException.class)
    public void testValidation2() {

        Validation<Seq<String>, MyClass> res=  Validation
                .combine(validateAge(13),
                        validateAmount(15000))
                .ap(MyClass::new);

        // Este acceso es inseguro porque no se sabe si fue valid o invalid.
        // en este caso esto lanza una excepción. Esto significa que el accesor get sobre un Validation es INSEGURO!
        MyClass myClass = res.get();

        assertTrue("ap should be invalid",res.isInvalid());
    }

    public void testValidation3() {

        Validation<Seq<String>, MyClass> res=  Validation
                .combine(validateAge(13),
                        validateAmount(15000))
                .ap(MyClass::new);

        Integer fold = res.fold(s -> 1, c -> 2);

        assertTrue("ap should be invalid",res.isInvalid());
        assertEquals(fold.intValue(), 1);
    }

    @Test
    public void testValidation4() {

        Validation<Seq<String>, MyClass> res=  Validation
                .combine(validateAge(13),
                        validateAmount(10000))
                .ap(MyClass::new);


                res.fold(
                        s -> {
                            assertTrue(s.size()==2);
                            assertTrue(s.contains("Age must be at least " + 14));
                            assertTrue(s.contains("Amount must be at least " + 1400));
                            return s.size();
                        },
                        
                        c -> 2);

    }

    /**
     * Combinar multiples validations con una invalida
     */
    @Test
    public void testCombineWithAnInvalid(){

        Validation<Error,String> valid = Validation.valid("Lets");
        Validation<Error,String> valid2 = Validation.valid("Go!");
        Validation<Error, String> invalid = Validation.invalid(new Error("Stop!"));

        Validation<Seq<Error>, String> finalValidation = Validation.combine(valid, invalid , valid2).ap((v1,v2,v3) -> v1 + v2 + v3);

        assertEquals("Failure - Combine with an invalid Validation didn't return the error",
                "Stop!",
                finalValidation.getError().get(0).getMessage());

        // Cambialo para que verifiques con fold! :D
    }

    /**
     * Combinar multiples validations todas validas
     */
    @Test
    public void testCombineValid() {

        Validation<Error, String> valid = Validation.valid("Lets");
        Validation<Error, String> valid2 = Validation.valid(" Go");
        Validation<Error, String> valid3 = Validation.valid("!");

        Validation<Seq<Error>, String> finalValidation = Validation
                .combine(valid, valid2, valid3)
                .ap((v1, v2, v3) -> v1 + v2 + v3);

        assertEquals("Failure - Combine validation doesn't apply the correspond function with the values",
                "Lets Go!",
                finalValidation.get());
    }

    /**
     * Un validator retorna un resultado exitoso si el valor
     * cumple con los predicados dados
     */
    @Test
    public void testValidValidator(){
        final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)\\.(.+)$";
        String email = "test@test.com";
        Validation<String, String> validateEmail = CharSeq.of(email)
                .matches(EMAIL_REGEX)
                ? Validation.valid(email)
                : Validation.invalid("Email contains invalid characters");
        assertTrue("The validator failed", validateEmail.isValid());
    }

    /**
     * Un validator retorna un resultado fallido si el valor
     * no cumple con los predicados dados
     */
    @Test
    public void testInvalidValidator() {

        final Integer UPPER_BOUND = 100;
        final Integer LOWER_BOUND = 5;

        Integer value = 500;

        Validation<String, Integer> validateBound = (value < UPPER_BOUND && value > LOWER_BOUND)
                ? Validation.valid(value)
                : Validation.invalid("The value is out of the defined bounds");

        assertTrue("The validator was successful", validateBound.isInvalid());
    }

    /**
     * Se prueba el constructor de 8 parametro para Builder
     */
    @Test
    public void testBuilder8() {

        Validation<String, String> v1 = Validation.valid("John Doe");
        Validation<String, Integer> v2 = Validation.valid(39);
        Validation<String, Option<String>> v3 = Validation.valid(Option.of("address"));

        Validation<String, String> v4 = Validation.valid("111-111-1111");
        Validation<String, String> v5 = Validation.valid("alt1");
        Validation<String, String> v6 = Validation.valid("alt2");
        Validation<String, String> v7 = Validation.valid("alt3");
        Validation<String, String> v8 = Validation.valid("alt4");

        Validation.Builder8<String, String, Integer, Option<String>, String, String, String, String, String> result8 =
                Validation.combine(v1,v2,v3,v4,v5,v6,v7,v8);

        assertEquals("Failure - ",
                "Valid(John Doe,39,address,111-111-1111,alt1,alt2,alt3,alt4)",
                result8.ap(TestValidation::new).toString());
    }

    /**
     *  Me permite recorrer una coleccion de Validation y operarlos
     */
    @Test
    public void testValidatorForEach() {
        ArrayList<String> msg = new ArrayList<>();
        List<Validation<Error,String>> validation = List.of(
                Validation.valid("Juan"),
                Validation.valid("Cadavid"),
                Validation.valid("Cubaque"),
                Validation.invalid(new Error("Stop!"))
        );
        Consumer<Validation<Error,String>> consumer = s -> {
            if(s.isValid()) {
                msg.add("Operacion " + msg.size());
            }
        };
        validation.forEach(consumer);
        assertEquals("Failure- Was not operated",
                Arrays.asList("Operacion 0","Operacion 1","Operacion 2"),msg);
    }

    /**
     *  El flatmap retorna otro validation, y los resultados de otros validation se pueden encadenar
     */
    @Test
    public void testValidatorFlatMap() {

        Validation<Error,Integer> validatorValid = Validation.valid(18);
        Validation<Error,String> validatorInvalid = Validation.invalid(new Error("Alert!"));

        Function1<Integer,Validation<Error,String>> ageValidator =  i -> {
            if (i > 17) {
                return Validation.valid("he is an adult");
            } else {
                return Validation.invalid(new Error("Upps!, he is not an adult"));
            }
        };

        assertEquals("Failure - return some invalid",
                Validation.valid("18 this is part of flatmap"),
                validatorValid.flatMap(s -> Validation.valid(s + " this is part of flatmap")));

        assertEquals("Failure - someone is younger",
                Validation.valid("he is an adult"),
                validatorValid.flatMap(s -> ageValidator.apply(s)));

        assertEquals("Failure - the flatmap is Valid",
                Validation.invalid(new Error("Alert!")).toString(),
                validatorInvalid.flatMap(s -> Validation.valid(s + "invalid flatmap")).toString());
    }



}