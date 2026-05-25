package com.github.karsaig.approvalcrest.jupiter;

import com.github.karsaig.approvalcrest.matcher.AbstractTestMetaBase;

import java.nio.file.Path;

abstract class Junit5TestMetaBase extends AbstractTestMetaBase {

    protected Junit5TestMetaBase(String testClassName, String testMethodName) {
        super(testClassName, testMethodName);
    }

    protected Junit5TestMetaBase(String testClassName, String testMethodName, String sourceRoutePathString) {
        super(testClassName, testMethodName, sourceRoutePathString);
    }

    protected Junit5TestMetaBase(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
        super(testClassPath, testClassName, testMethodName, approvedDirectory);
    }

    protected Junit5TestMetaBase(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory, Path workingDirectory) {
        super(testClassPath, testClassName, testMethodName, approvedDirectory, workingDirectory);
    }
}
