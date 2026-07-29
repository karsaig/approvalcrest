package com.github.karsaig.approvalcrest;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The kinds of approved file approvalcrest writes: one per matcher family.
 *
 * <p>Both kinds live side by side in the shared approvals directory, distinguished only by their
 * extension, so anything that selects a subset of them has to be explicit about whether it is
 * selecting what to <em>write</em> or what to <em>delete</em>.
 */
public enum ApprovedFileType {

    /** Written by {@code sameJsonAsApproved} and {@code sameBeanAs}. */
    JSON("json"),

    /** Written by {@code sameContentAsApproved}. */
    CONTENT("content");

    /** Accepted in a type list to mean every type, so {@code --types all} reads naturally. */
    private static final String ALL_KEYWORD = "all";

    /** Accepted to mean no types, so a boolean-style {@code false} keeps working. */
    private static final Set<String> NONE_KEYWORDS =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList("none", "false")));

    private final String extension;

    ApprovedFileType(String extension) {
        this.extension = extension;
    }

    /**
     * Returns the file extension without the leading dot.
     *
     * @return the extension, as {@code FileStoreMatcherUtils} is constructed with
     */
    public String extension() {
        return extension;
    }

    /**
     * Returns every type, used as the default wherever an explicit selection is absent.
     *
     * @return all approved file types
     */
    public static Set<ApprovedFileType> all() {
        return EnumSet.allOf(ApprovedFileType.class);
    }

    /**
     * Returns no types, meaning a feature is switched off entirely.
     *
     * @return an empty set
     */
    public static Set<ApprovedFileType> none() {
        return EnumSet.noneOf(ApprovedFileType.class);
    }

    /**
     * Resolves the type whose extension matches the given approved file name.
     *
     * @param fileName the file name to inspect
     * @return the matching type, or null when the name is neither json nor content
     */
    public static ApprovedFileType fromFileName(String fileName) {
        for (ApprovedFileType type : values()) {
            if (fileName.endsWith("." + type.extension)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Resolves a type from its extension.
     *
     * @param extension the extension without a leading dot
     * @return the matching type, or null when unrecognised
     */
    public static ApprovedFileType fromExtension(String extension) {
        for (ApprovedFileType type : values()) {
            if (type.extension.equalsIgnoreCase(extension)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Parses a type selection: a comma-separated list such as {@code json}, {@code content} or
     * {@code json,content}, or one of the keywords {@code all}, {@code true}, {@code none},
     * {@code false}.
     *
     * <p>{@code true} and {@code all} both mean every type, so a value that used to be read as a
     * boolean keeps its meaning. An unrecognised value is rejected rather than silently ignored:
     * quietly dropping it would leave the caller believing a type was selected when nothing would
     * happen.
     *
     * @param value the selection to parse; null or blank means every type
     * @return the selected types, possibly empty
     * @throws IllegalArgumentException if a value is not a known type or keyword
     */
    public static Set<ApprovedFileType> parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return all();
        }
        Set<ApprovedFileType> selected = new LinkedHashSet<>();
        for (String raw : value.split(",")) {
            String token = raw.trim();
            if (token.isEmpty()) {
                continue;
            }
            if (ALL_KEYWORD.equalsIgnoreCase(token) || Boolean.parseBoolean(token)) {
                return all();
            }
            if (NONE_KEYWORDS.contains(token.toLowerCase())) {
                return none();
            }
            selected.add(parseSingle(token));
        }
        return selected.isEmpty() ? all() : Collections.unmodifiableSet(selected);
    }

    private static ApprovedFileType parseSingle(String token) {
        ApprovedFileType type = fromExtension(token);
        if (type == null) {
            throw new IllegalArgumentException("Unknown approved file type: '" + token + "'. Valid values are "
                    + Arrays.stream(values()).map(ApprovedFileType::extension).collect(Collectors.joining(", "))
                    + ", " + ALL_KEYWORD + " and " + String.join(", ", NONE_KEYWORDS) + ".");
        }
        return type;
    }
}
