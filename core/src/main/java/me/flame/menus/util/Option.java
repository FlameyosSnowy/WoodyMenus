package me.flame.menus.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings({ "unchecked", "unused" })
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Option<T> {
    private static final Option<?> NONE = new Option<>(null);

    private final T value;

    public static <E> Option<E> some(E value) {
        return value != null ? new Option<>(value) : none();
    }

    public static <E> Option<E> someIf(E value, boolean condition) {
        return value != null && condition ? new Option<>(value) : none();
    }

    public static <E> Option<E> none() { return (Option<E>) NONE; }

    public void peek(Consumer<T> actionIfPresent) {
        if (this.isNone()) return;
        actionIfPresent.accept(value);
    }

    public T orElse(T defaultValue) {
        return this.isNone() ? defaultValue : this.value;
    }

    public T orElseGet(Supplier<T> value) { return this.isNone() ? value.get() : this.value; }

    public Option<T> or(Option<T> otherValue) {
        return otherValue.isSome() ? otherValue : this;
    }

    public Option<T> filter(Predicate<T> filtering) {
        return isNone() || !filtering.test(value) ? none() : some(value);
    }

    public <E> Option<E> map(Function<T, E> mapper) {
        return this.isNone() ? none() : some(mapper.apply(value));
    }

    public <E> Option<E> flatMap(Function<T, Option<E>> mapper) {
        return this.isNone() ? none() : mapper.apply(value);
    }

    public boolean isSome() { return value != null; }

    public boolean isNone() { return value == null; }


}
