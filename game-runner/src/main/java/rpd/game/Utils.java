package rpd.game;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Utils {
    public static <A, B, C> Stream<C> zip(Stream<A> as, Stream<B> bs, BiFunction<A, B, C> merger) {
        Spliterator<A> aSpliterator = as.spliterator();
        Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
        Spliterator<B> bSpliterator = bs.spliterator();
        Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
        Iterator<C> cIterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return aIterator.hasNext() & bIterator.hasNext();
            }

            @Override
            public C next() {
                return merger.apply(aIterator.next(),
                                    bIterator.next());
            }
        };
        Spliterator<C> cSpliterator = Spliterators.spliterator(cIterator,
                                                               Math.min(aSpliterator.estimateSize(),
                                                                        bSpliterator.estimateSize()),
                                                               aSpliterator.characteristics() &
                                                               bSpliterator.characteristics() &
                                                               ~Spliterator.SUBSIZED);
        return StreamSupport.stream(cSpliterator, false);
    }
}
