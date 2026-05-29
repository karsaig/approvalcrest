package com.github.karsaig.approvalcrest.kotlin.matcher

import com.github.karsaig.approvalcrest.matcher.ContentMatcher
import com.github.karsaig.approvalcrest.matcher.file.AbstractDiagnosingFileMatcher
import com.github.karsaig.approvalcrest.matcher.file.ApprovedFileMatcher
import java.nio.file.Path

/**
 * Kotlin extension functions for [ContentMatcher] that work around Kotlin's inability
 * to infer F-bounded (recursive) generic return types (KT-5464).
 *
 * Without these, chained calls such as:
 *   `sameContentAsApproved<String>().withUniqueId("id")`
 * fail to compile because Kotlin infers the return type as `ApprovedFileMatcher<*>`.
 */

@Suppress("UNCHECKED_CAST")
fun <T> ContentMatcher<T>.withUniqueId(uniqueId: String): ContentMatcher<T> =
    (this as ApprovedFileMatcher<ContentMatcher<T>>).withUniqueId(uniqueId)

@Suppress("UNCHECKED_CAST")
fun <T> ContentMatcher<T>.withFileName(customFileName: String): ContentMatcher<T> =
    (this as ApprovedFileMatcher<ContentMatcher<T>>).withFileName(customFileName)

@Suppress("UNCHECKED_CAST")
fun <T> ContentMatcher<T>.withPathName(pathName: String): ContentMatcher<T> =
    (this as ApprovedFileMatcher<ContentMatcher<T>>).withPathName(pathName)

@Suppress("UNCHECKED_CAST")
fun <T> ContentMatcher<T>.withRelativePathName(pathName: String): ContentMatcher<T> =
    (this as ApprovedFileMatcher<ContentMatcher<T>>).withRelativePathName(pathName)

@Suppress("UNCHECKED_CAST")
fun <T> ContentMatcher<T>.withPath(path: Path): ContentMatcher<T> =
    (this as ApprovedFileMatcher<ContentMatcher<T>>).withPath(path)

@Suppress("UNCHECKED_CAST")
fun <T> ContentMatcher<T>.withMachineReadableOutput(): ContentMatcher<T> =
    (this as AbstractDiagnosingFileMatcher<T, ContentMatcher<T>>).withMachineReadableOutput()
