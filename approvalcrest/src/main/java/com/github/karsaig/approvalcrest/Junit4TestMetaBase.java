package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.AbstractTestMetaBase;

import java.nio.file.Path;

abstract class Junit4TestMetaBase extends AbstractTestMetaBase {

    protected Junit4TestMetaBase(String testClassName, String testMethodName) {
        super(testClassName, testMethodName);
    }

    public Junit4TestMetaBase(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
        super(testClassPath, testClassName, testMethodName, approvedDirectory);
    }

    public Junit4TestMetaBase(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory, Path workingDirectory) {
        super(testClassPath, testClassName, testMethodName, approvedDirectory, workingDirectory);
    }
}
