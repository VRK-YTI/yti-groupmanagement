package fi.vm.yti.groupmanagement.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CollectionUtil {

    private CollectionUtil() {
    }

    public static <T> @NotNull T requireSingle(@NotNull final Collection<T> items) {

        if (items.size() != 1) {
            throw new RuntimeException("Expecting single item, was: " + items.size());
        }

        return items.iterator().next();
    }

    public static @Nullable <T> T requireSingleOrNone(@NotNull final Collection<T> items) {

        if (items.size() > 1) {
            throw new RuntimeException("Expecting single item, was: " + items.size());
        }

        Iterator<T> iterator = items.iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public static <T, R> @NotNull List<R> mapToList(@NotNull final Collection<T> collection,
                                                    final Function<T, R> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    public static <T> @NotNull List<T> filterToList(@NotNull final Collection<T> collection,
                                                    final Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(Collectors.toList());
    }
}
