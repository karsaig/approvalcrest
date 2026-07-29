package com.github.karsaig.approvalcrest.dedup;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The kinds of approved file the dedup tooling can process.
 *
 * <p>Both kinds share the same bucket directories in the shared approvals directory, distinguished
 * only by their extension, which is why selecting a subset has to be handled carefully — see
 * {@link ApprovedFileScanner}.
 */
enum ApprovedFileType {

    JSON("json"),
    CONTENT("content");

    /** Accepted in a type list to mean every type, so {@code --types all} reads naturally. */
    private static final String ALL_KEYWORD = "all";

    private final String extension;

    ApprovedFileType(String extension) {
        this.extension = extension;
    }

    /**
     * @return the file extension without the leading dot, as {@code FileStoreMatcherUtils} expects it
     */
    String extension() {
        return extension;
    }

    /**
     * Returns every type, used as the default so a run without an explicit selection behaves as it
     * always has.
     *
     * @return all approved file types
     */
    static Set<ApprovedFileType> all() {
        return EnumSet.allOf(ApprovedFileType.class);
    }

    /**
     * Resolves the type whose extension matches the given approved file name.
     *
     * @param fileName the file name to inspect
     * @return the matching type, or null when the name is neither json nor content
     */
    static ApprovedFileType fromFileName(String fileName) {
        for (ApprovedFileType type : values()) {
            if (fileName.endsWith("." + type.extension)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Parses a comma-separated type list such as {@code json}, {@code content}, {@code json,content}
     * or {@code all}.
     *
     * <p>An unrecognised value is rejected rather than silently ignored: quietly dropping it would
     * leave the caller believing a type was selected when nothing would be processed.
     *
     * @param value the list to parse; null or blank means every type
     * @return the selected types, never empty
     * @throws IllegalArgumentException if a value is not a known type
     */
    static Set<ApprovedFileType> parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return all();
        }
        Set<ApprovedFileType> selected = new LinkedHashSet<>();
        for (String raw : value.split(",")) {
            String token = raw.trim();
            if (token.isEmpty()) {
                continue;
            }
            if (ALL_KEYWORD.equalsIgnoreCase(token)) {
                return all();
            }
            selected.add(parseSingle(token));
        }
        return selected.isEmpty() ? all() : Collections.unmodifiableSet(selected);
    }

    private static ApprovedFileType parseSingle(String token) {
        for (ApprovedFileType type : values()) {
            if (type.extension.equalsIgnoreCase(token)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown approved file type: '" + token + "'. Valid values are "
                + Arrays.stream(values()).map(ApprovedFileType::extension).collect(Collectors.joining(", "))
                + " and " + ALL_KEYWORD + ".");
    }
}
