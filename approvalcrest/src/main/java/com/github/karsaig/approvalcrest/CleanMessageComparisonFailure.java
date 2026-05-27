package com.github.karsaig.approvalcrest;

import org.junit.ComparisonFailure;

/**
 * A {@link ComparisonFailure} subclass that overrides {@link #getMessage()} to return the
 * original message without the {@code ComparisonCompactor} suffix appended by the parent class.
 * <p>
 * Used in machine-readable output mode so that AI agents reading CI logs see only the structured
 * machine-readable text, while IDEs still get a proper diff view via {@link #getExpected()} and
 * {@link #getActual()}.
 */
class CleanMessageComparisonFailure extends ComparisonFailure {

    private static final long serialVersionUID = 1L;

    private final String originalMessage;

    CleanMessageComparisonFailure(String message, String expected, String actual) {
        super(message, expected, actual);
        this.originalMessage = message;
    }

    @Override
    public String getMessage() {
        return originalMessage;
    }
}
