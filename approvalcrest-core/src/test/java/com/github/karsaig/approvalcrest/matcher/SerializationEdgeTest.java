package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Serialisation edge cases exercised through the real {@link GsonProvider} configuration:
 * the {@code java.util.Optional*} hierarchy serializers and the Throwable adapter's source-object
 * traversal (message, cause, suppressed, stack trace). These run in every reflection mode.
 */
public class SerializationEdgeTest {

    private static final Set<Class<?>> NO_CIRCULAR = Collections.emptySet();

    private Gson gson() {
        return GsonProvider.gson(new MatcherConfiguration(), NO_CIRCULAR);
    }

    static class Holder {
        final Optional<String> present;
        final Optional<String> empty;
        final OptionalInt oi;
        final OptionalInt oiEmpty;
        final OptionalLong ol;
        final OptionalDouble od;

        Holder() {
            this.present = Optional.of("value");
            this.empty = Optional.empty();
            this.oi = OptionalInt.of(7);
            this.oiEmpty = OptionalInt.empty();
            this.ol = OptionalLong.of(9_000_000_000L);
            this.od = OptionalDouble.of(2.5);
        }
    }

    @Test
    void serialisesJavaOptionalVariants() {
        String json = gson().toJson(new Holder());

        assertThat(json, containsString("value"));   // present Optional
        assertThat(json, containsString("7"));        // OptionalInt
        assertThat(json, containsString("9000000000")); // OptionalLong
        assertThat(json, containsString("2.5"));      // OptionalDouble
    }

    @Test
    void serialisesThrowableWithCauseAndSuppressed() {
        RuntimeException error = new RuntimeException("top-level", new IllegalStateException("root cause"));
        error.addSuppressed(new IllegalArgumentException("suppressed one"));

        String json = gson().toJson(error);

        // The throwable adapter walks message, cause and suppressed collections.
        assertThat(json, containsString("top-level"));
        assertThat(json, containsString("root cause"));
        assertThat(json, containsString("suppressed one"));
    }

    @Test
    void serialisesThrowableWithoutCause() {
        RuntimeException error = new RuntimeException("no cause here");

        String json = gson().toJson(error);

        assertThat(json, containsString("no cause here"));
    }
}
