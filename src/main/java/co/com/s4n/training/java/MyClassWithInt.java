package co.com.s4n.training.java;

public class MyClassWithInt{
    int i;

    public MyClassWithInt(int i){
        this.i = i;
    }

/*    public MyClassWithInt(Integer i){
        this.i = i.intValue();
    }*/

    @Override
    public String toString(){
        return String.valueOf(i);
    }
}