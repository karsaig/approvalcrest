package com.github.karsaig.approvalcrest.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;
import com.github.karsaig.approvalcrest.util.InMemoryFsUtil;
import com.google.common.jimfs.Jimfs;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

/**
 * Reproducing tests for the review findings in approvalcrest-core.
 *
 * <p>Each test asserts the CURRENT behaviour. Where that behaviour is the bug, the assertion
 * message says so, and the test flips to red when the bug is fixed.
 */
public class CoreBugReproTest {

    private static final Set<Class<?>> NO_CIRCULAR = Collections.emptySet();

    private static Gson gson() {
        return GsonProvider.gson(new MatcherConfiguration(), NO_CIRCULAR);
    }

    /** No equals()/hashCode() override - two instances with identical fields are NOT equal. */
    static class NoEquals {
        final String v;

        NoEquals(String v) {
            this.v = v;
        }
    }

    /**
     * Finding 1.1 - Set serialisation uses a TreeSet keyed on the JSON representation, so two
     * elements that are not equals() but serialise identically collapse into one.
     *
     * <p>Correct behaviour: a 2-element Set must serialise as a 2-element JSON array.
     */
    @Test
    public void setSerialisationCollapsesDistinctElements() {
        Set<NoEquals> set = new LinkedHashSet<>();
        set.add(new NoEquals("same"));
        set.add(new NoEquals("same"));
        assertEquals(2, set.size(), "setup: the two beans are genuinely distinct Set members");

        JsonArray json = JsonParser.parseString(gson().toJson(set)).getAsJsonArray();

        // BUG: serialises as a single-element array, silently losing one member.
        assertEquals(1, json.size(),
                "BUG 1.1: 2-element Set serialised to a " + json.size()
                        + "-element array - cardinality lost");
    }

    /**
     * Finding 1.1, consequence - because the Set collapses, a 2-element actual and a 1-element
     * expected produce identical JSON, so a real difference cannot be detected.
     */
    @Test
    public void twoElementSetAndOneElementSetProduceIdenticalJson() {
        Set<NoEquals> actual = new LinkedHashSet<>();
        actual.add(new NoEquals("same"));
        actual.add(new NoEquals("same"));

        Set<NoEquals> expected = new LinkedHashSet<>();
        expected.add(new NoEquals("same"));

        assertNotEquals(actual.size(), expected.size(), "setup: the sets genuinely differ in size");

        // BUG: the difference is invisible after serialisation.
        assertEquals(gson().toJson(expected), gson().toJson(actual),
                "BUG 1.1: sets of different sizes serialise identically - a real diff cannot fail a test");
    }

    /**
     * Finding 2.5 - OffsetDateTime is normalised to UTC before formatting, so the offset is lost
     * and two values with different offsets but the same instant serialise identically.
     *
     * <p>Correct behaviour: the offset is part of the value and must survive serialisation.
     */
    @Test
    public void offsetDateTimeLosesItsOffset() {
        OffsetDateTime plusFive = OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.ofHours(5));
        OffsetDateTime utc = OffsetDateTime.of(2024, 1, 1, 7, 0, 0, 0, ZoneOffset.UTC);
        assertNotEquals(plusFive, utc, "setup: the two values are different OffsetDateTimes");
        assertEquals(plusFive.toInstant(), utc.toInstant(), "setup: but they are the same instant");

        // BUG: both serialise to the same UTC string, so the offset difference cannot fail a test.
        assertEquals(gson().toJson(utc), gson().toJson(plusFive),
                "BUG 2.5: OffsetDateTime values with different offsets serialise identically");
    }

    /**
     * Finding 2.5, the inconsistency - OffsetTime does NOT apply the UTC override, so it keeps
     * its offset. The two adapters disagree about whether offset is significant.
     */
    @Test
    public void offsetTimeKeepsItsOffsetUnlikeOffsetDateTime() {
        OffsetTime plusFive = OffsetTime.of(12, 0, 0, 0, ZoneOffset.ofHours(5));
        OffsetTime utc = OffsetTime.of(7, 0, 0, 0, ZoneOffset.UTC);

        assertNotEquals(gson().toJson(utc), gson().toJson(plusFive),
                "OffsetTime preserves the offset - inconsistent with OffsetDateTime above");
    }

    /**
     * Finding 2.4 - bucketDepth comes straight from a system property with no validation and is
     * used as substring(0, bucketDepth) on the content key.
     *
     * <p>Correct behaviour: reject out-of-range values with a clear configuration error.
     */
    @Test
    public void unvalidatedBucketDepthThrowsStringIndexOutOfBounds() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(InMemoryFsUtil.JIMFS_UNIX_CONFIG)) {
            Path workDir = fs.getPath("/work");
            Files.createDirectories(workDir);
            String shared = "shared";

            FileStoreMatcherUtils utils = new FileStoreMatcherUtils("json",
                    new FileMatcherConfig(false, false, false, false, true, shared, true, 2));

            StringIndexOutOfBoundsException negative = assertThrows(StringIndexOutOfBoundsException.class,
                    () -> utils.writeCanonical("{}", "c", workDir, shared, -1),
                    "BUG 2.4: negative bucketDepth escapes as StringIndexOutOfBoundsException");
            assertTrue(negative.getMessage() != null, "exception carries no configuration context");

            assertThrows(StringIndexOutOfBoundsException.class,
                    () -> utils.writeCanonical("{}", "c", workDir, shared, 99),
                    "BUG 2.4: oversized bucketDepth escapes as StringIndexOutOfBoundsException");
        }
    }

    /**
     * Finding 2.2 - the approved-file header is written with a hardcoded LF but parsed with a
     * literal indexOf("*&#47;\n"). A CRLF file (Windows checkout with core.autocrlf=true, and the
     * repo ships no .gitattributes) fails to parse, so the comment header is never stripped.
     *
     * <p>Correct behaviour: the header is stripped regardless of line ending.
     */
    @Test
    public void crlfHeaderIsNotStrippedOnRead() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(InMemoryFsUtil.JIMFS_UNIX_CONFIG)) {
            Path workDir = fs.getPath("/work");
            Files.createDirectories(workDir);
            FileStoreMatcherUtils utils = new FileStoreMatcherUtils("json",
                    new FileMatcherConfig(false, false, false, false, true, "shared", true, 2));

            Path lf = workDir.resolve("lf-approved.json");
            Files.write(lf, "/*comment*/\n{\"v\":1}".getBytes(StandardCharsets.UTF_8));
            assertEquals("{\"v\":1}", utils.readFile(lf, workDir), "LF file: header correctly stripped");

            Path crlf = workDir.resolve("crlf-approved.json");
            Files.write(crlf, "/*comment*/\r\n{\"v\":1}".getBytes(StandardCharsets.UTF_8));

            // BUG: header not stripped, caller gets the raw file including the comment.
            assertEquals("/*comment*/\r\n{\"v\":1}", utils.readFile(crlf, workDir),
                    "BUG 2.2: CRLF file returned with its comment header still attached");
        }
    }

    /**
     * Finding 2.2, consequence - a CRLF pointer file is not recognised as a pointer at all.
     */
    @Test
    public void crlfPointerFileIsNotRecognisedAsAPointer() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(InMemoryFsUtil.JIMFS_UNIX_CONFIG)) {
            Path workDir = fs.getPath("/work");
            Files.createDirectories(workDir);
            FileStoreMatcherUtils utils = new FileStoreMatcherUtils("json",
                    new FileMatcherConfig(false, false, false, false, true, "shared", true, 2));

            Path lf = workDir.resolve("lf-approved.json");
            Files.write(lf, "/*c*/\n/*pointer:shared/ab/x-approved.json*/".getBytes(StandardCharsets.UTF_8));
            assertTrue(utils.isPointerFile(lf), "LF pointer correctly detected");

            Path crlf = workDir.resolve("crlf-approved.json");
            Files.write(crlf, "/*c*/\r\n/*pointer:shared/ab/x-approved.json*/".getBytes(StandardCharsets.UTF_8));

            // BUG: identical pointer with CRLF is invisible to the pointer machinery.
            assertFalse(utils.isPointerFile(crlf),
                    "BUG 2.2: CRLF pointer file not recognised - dedup and reinstate silently skip it");
        }
    }
}
