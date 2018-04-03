package co.com.s4n.training.java;

public class CollectablePerson {
    public String name;
    public int age;

    public CollectablePerson(){
        this.name = "";
        this.age = 0;
    }

    public CollectablePerson(String name, int age){
        this.name = name;
        this.age = age;
    }

    public void addName(String name){
        this.name = this.name + " " + name;
    }

    public void addAge(int age){
        this.age = this.age + age;
    }

}
