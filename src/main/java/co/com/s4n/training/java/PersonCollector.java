package co.com.s4n.training.java;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class PersonCollector implements Collector<CollectablePerson, CollectablePerson, CollectablePerson> {
    @Override
    public Supplier<CollectablePerson> supplier() {
        return CollectablePerson::new;
    }

    @Override
    public BiConsumer<CollectablePerson, CollectablePerson> accumulator() {
        return (CollectablePerson p1, CollectablePerson p2) -> {
            System.out.println("accumulator: "+ p1.name+ " "+p2.name);
            p1.addName(p2.name);
            p1.addAge(p2.age);
        };
    }

    @Override
    public BinaryOperator<CollectablePerson> combiner() {
        return (CollectablePerson p1, CollectablePerson p2) -> {
            System.out.println("combiner: "+ p1.name+ " "+p2.name);
            return new CollectablePerson(p1.name+" "+p2.name, p1.age+p2.age);
        };

    }

    @Override
    public Function<CollectablePerson, CollectablePerson> finisher() {
        return (CollectablePerson p1) -> {
            System.out.println("finisher: "+ p1.name+ " "+p1.age);

            return p1;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.UNORDERED);
    }
}
