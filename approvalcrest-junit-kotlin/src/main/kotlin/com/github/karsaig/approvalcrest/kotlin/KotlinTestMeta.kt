package com.github.karsaig.approvalcrest.kotlin

import com.github.karsaig.approvalcrest.jupiter.JunitJupiterTestMeta
import org.junit.jupiter.api.TestInfo
import java.io.File

class KotlinTestMeta : JunitJupiterTestMeta {

    constructor() : super()

    constructor(testInfo: TestInfo): super(testInfo)

    override fun getSourceRoutePathString(): String = SRC_TEST_KOTLIN_PATH

    companion object {
        private val SRC_TEST_KOTLIN_PATH = "src${File.separator}test${File.separator}java${File.separator}"
    }
}