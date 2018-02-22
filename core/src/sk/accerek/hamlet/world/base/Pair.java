package sk.accerek.hamlet.world.base;

import lombok.Data;

@Data
public class Pair<T, E> {
    private final T x;
    private final E y;

    public Pair(T x, E y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o.getClass() != Pair.class) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return x.equals(pair.x) && y.equals(pair.y);
    }

    @Override
    public String toString() {
        return String.format("{%s; %s}", x, y);
    }
}
