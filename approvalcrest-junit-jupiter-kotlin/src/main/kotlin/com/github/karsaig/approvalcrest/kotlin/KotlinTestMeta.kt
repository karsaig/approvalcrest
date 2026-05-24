package com.github.karsaig.approvalcrest.kotlin

import com.github.karsaig.approvalcrest.jupiter.JunitJupiterTestMeta
import java.io.File
import java.nio.file.Path

class KotlinTestMeta : JunitJupiterTestMeta {

    constructor() : super(SRC_TEST_KOTLIN_PATH)

    internal constructor(testClassPath: Path, testClassName: String, testMethodName: String, approvedDirectory: Path) :
            super(testClassPath, testClassName, testMethodName, approvedDirectory)

    companion object {
        internal val SRC_TEST_KOTLIN_PATH = "src${File.separator}test${File.separator}kotlin${File.separator}"
    }
}
