package cn.zbx1425.scatteredshards;

import java.util.function.Supplier;

public class Lazy<T> {

    private T object;
    private final Supplier<T> supplier;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

	public Lazy(T object) {
		this.object = object;
		this.supplier = null;
	}

    public T get() {
        if (object == null) {
            object = supplier.get();
        }
        return object;
    }

	public static <T> Lazy<T> of(Supplier<T> supplier) {
		return new Lazy<>(supplier);
	}

	public static <T> Lazy<T> of(T object) {
		return new Lazy<>(object);
	}
}
