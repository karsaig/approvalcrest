package com.github.karsaig.approvalcrest.matcher;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.karsaig.approvalcrest.util.InMemoryFiles;

/**
 * Unit test for the {@link ContentMatcher}.
 * Verifies creation of not approved files.
 *
 * @author Andras_Gyuro
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class ContentMatcherTest extends AbstractFileMatcherTest {

    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExists() {
        String actual = "Example content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExists");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "183d71-not-approved.content", "183d71-approved.content"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/183d71-not-approved.content", "/*ContentMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExists*/\n" +
                    "Example content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void testRunShouldCreateNotApprovedFileAndPassWhenNotExistsAnsPassOnCreateEnabled() {
        String actual = "Example content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExists");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, enablePassOnCreate());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/183d71-not-approved.content", "/*ContentMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExists*/\n" +
                    "Example content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExistsOnWindows() {
        String actual = "Example content";
        inMemoryWindowsFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExistsOnWindows");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "e6fb22-not-approved.content", "e6fb22-approved.content"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f\\e6fb22-not-approved.content", "/*ContentMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExistsOnWindows*/\n" +
                    "Example content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApproved() {
        String actual = "Example content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApproved");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("39c4dd-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/39c4dd-approved.content", "Example content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentDiffersFromApprovedContent() {
        String actual = "Example content";
        String apprivedFileContent = "Modified content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentDiffersFromApprovedContent");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("0d08c2-approved.content"), apprivedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: Modified content\n" +
                    "     but: Expected file 87668f/0d08c2-approved.content\n" +
                    "Content does not match!", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/0d08c2-approved.content", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }


    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueId() {
        String actual = "Content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueId");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withUniqueId("idTest");

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("3f0945-idTest-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/3f0945-idTest-approved.content", "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueIdAndContentDiffers() {
        String actual = "Content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueIdAndContentDiffers");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withUniqueId("idTest");

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("39e1a0-idTest-approved.content"), "Different content");

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: Different content\n" +
                    "     but: Expected file 87668f/39e1a0-idTest-approved.content\n" +
                    "Content does not match!", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/39e1a0-idTest-approved.content", "Different content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileName() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileName");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withFileName("single-line");

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("single-line-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/single-line-approved.content", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndContentDiffers() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndContentDiffers");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withFileName("single-line");

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("single-line-approved.content"), "Different content");

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: Different content\n" +
                    "     but: Expected file 87668f/single-line-approved.content\n" +
                    "Content does not match!", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/single-line-approved.content", "Different content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathName() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathName");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().getRoot().resolve("/work/test/path/src/test/contents").resolve("single-line-2-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("src/test/contents/single-line-2-approved.content", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameAndContentDiffers() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameAndContentDiffers");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().getRoot().resolve("/work/test/path/src/test/contents").resolve("single-line-2-approved.content"), "Different content");

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: Different content\n" +
                    "     but: Expected file /work/test/path/src/test/contents/single-line-2\n" +
                    "Content does not match!", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("src/test/contents/single-line-2-approved.content", "Different content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathName() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathName");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("/src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().getRoot().resolve("/src/test/contents").resolve("single-line-2-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("/src/test/contents/single-line-2-approved.content", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathNameAndContentDiffers() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathNameAndContentDiffers");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("/src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().getRoot().resolve("/src/test/contents").resolve("single-line-2-approved.content"), "Different content");

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: Different content\n" +
                    "     but: Expected file /src/test/contents/single-line-2\n" +
                    "Content does not match!", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("/src/test/contents/single-line-2-approved.content", "Different content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenMultiLineContentIsSameContentAsApproved() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\nMauris gravida varius dolor, vel imperdiet urna consectetur a.";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenMultiLineContentIsSameContentAsApproved");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("d23053-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/d23053-approved.content", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
                    "Mauris gravida varius dolor, vel imperdiet urna consectetur a.");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenMultiLineContentWithEmptyLineIsSameContentAsApproved() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\nMauris gravida varius dolor, vel imperdiet urna consectetur a.\n";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenMultiLineContentWithEmptyLineIsSameContentAsApproved");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("0438f6-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/0438f6-approved.content", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
                    "Mauris gravida varius dolor, vel imperdiet urna consectetur a.\n");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenManyLineContentWithEmptyLineIsSameContentAsApproved() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris gravida varius dolor, vel imperdiet urna consectetur a. Quisque a massa quis neque imperdiet mattis id nec augue. Praesent vitae odio in orci hendrerit pretium quis eu enim. Maecenas arcu urna, vestibulum at mauris sit amet, cursus mollis quam. Ut laoreet vestibulum nisi, in auctor tellus pharetra nec. Pellentesque vulputate lorem velit, sit amet placerat tortor blandit scelerisque. Ut eget risus ut magna faucibus aliquet. Sed semper lobortis nisi, eu egestas massa malesuada vitae. Aenean sagittis ultrices libero, sit amet tempor arcu eleifend et. Nulla facilisi. Proin sit amet ipsum ut est vestibulum mollis non viverra erat. Nunc libero velit, vestibulum sit amet lorem id, cursus hendrerit risus. Vivamus dignissim lacus sem, a eleifend sapien imperdiet eu. Integer rutrum pulvinar augue a ullamcorper. In lacinia feugiat dignissim. Sed sollicitudin orci sit amet leo ultricies gravida.\n\nNullam nec arcu blandit, lobortis velit a, placerat lorem. Phasellus vitae porttitor felis, in sodales est. Pellentesque venenatis turpis eu tortor lacinia iaculis. Aliquam lacus lectus, laoreet a odio sit amet, lacinia laoreet ex. Nullam tempor sapien vel tortor sollicitudin, eu facilisis tellus consequat. Integer neque arcu, tempus feugiat felis quis, porta consequat augue. Nulla sem lacus, euismod vitae pretium non, sagittis at massa. Fusce laoreet consectetur sapien elementum fringilla."
                + "\n\nIn dignissim nisl enim, quis pellentesque purus tempor quis. Integer varius, sapien a commodo suscipit, libero nisi malesuada justo, in iaculis quam nisl vitae arcu. Quisque non semper leo. Quisque porta placerat finibus. Fusce tempor porta varius. Nullam ullamcorper vitae nisi in faucibus. Vestibulum eget dolor cursus, condimentum massa at, viverra turpis. Sed eu aliquam mauris, sit amet efficitur metus. Mauris ac sodales est."
                + "\n\nInteger nunc neque, semper non nisi nec, vehicula sodales sem. Nullam dictum est eu porta tempus. Pellentesque malesuada convallis neque. Nunc ultricies faucibus leo, et aliquam purus imperdiet vel. Mauris nunc est, dignissim vel mi et, posuere ornare metus. Donec lectus arcu, consectetur sed iaculis sit amet, cursus non nunc. Etiam dictum justo at nisl laoreet, maximus pharetra elit imperdiet. Pellentesque vel diam at leo scelerisque porttitor nec ut purus. Ut cursus lobortis malesuada. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. In dapibus, dui eu posuere egestas, augue orci rutrum ex, non suscipit purus ligula at urna. Nunc vel arcu non mi molestie consectetur. Morbi dignissim magna at urna mattis gravida. Pellentesque placerat est lacus, sed varius orci tempor ac."
                + "\n\nQuisque consectetur, sapien non aliquam luctus, est urna aliquam purus, eu feugiat diam ex in ex. Phasellus ut nibh vitae libero iaculis accumsan. Morbi vitae orci ut eros tempus rhoncus ut eget justo. Vestibulum a accumsan urna. Cras sit amet lectus sed eros sollicitudin maximus. Fusce at vulputate eros, in bibendum ipsum. Etiam vitae malesuada metus, ac mattis felis. Nullam in facilisis justo. Morbi elementum vestibulum eros in vehicula.";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenManyLineContentWithEmptyLineIsSameContentAsApproved");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("020b55-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/020b55-approved.content", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris gravida varius dolor, vel imperdiet urna consectetur a. Quisque a massa quis neque imperdiet mattis id nec augue. Praesent vitae odio in orci hendrerit pretium quis eu enim. Maecenas arcu urna, vestibulum at mauris sit amet, cursus mollis quam. Ut laoreet vestibulum nisi, in auctor tellus pharetra nec. Pellentesque vulputate lorem velit, sit amet placerat tortor blandit scelerisque. Ut eget risus ut magna faucibus aliquet. Sed semper lobortis nisi, eu egestas massa malesuada vitae. Aenean sagittis ultrices libero, sit amet tempor arcu eleifend et. Nulla facilisi. Proin sit amet ipsum ut est vestibulum mollis non viverra erat. Nunc libero velit, vestibulum sit amet lorem id, cursus hendrerit risus. Vivamus dignissim lacus sem, a eleifend sapien imperdiet eu. Integer rutrum pulvinar augue a ullamcorper. In lacinia feugiat dignissim. Sed sollicitudin orci sit amet leo ultricies gravida.\n" +
                    "\n" +
                    "Nullam nec arcu blandit, lobortis velit a, placerat lorem. Phasellus vitae porttitor felis, in sodales est. Pellentesque venenatis turpis eu tortor lacinia iaculis. Aliquam lacus lectus, laoreet a odio sit amet, lacinia laoreet ex. Nullam tempor sapien vel tortor sollicitudin, eu facilisis tellus consequat. Integer neque arcu, tempus feugiat felis quis, porta consequat augue. Nulla sem lacus, euismod vitae pretium non, sagittis at massa. Fusce laoreet consectetur sapien elementum fringilla.\n" +
                    "\n" +
                    "In dignissim nisl enim, quis pellentesque purus tempor quis. Integer varius, sapien a commodo suscipit, libero nisi malesuada justo, in iaculis quam nisl vitae arcu. Quisque non semper leo. Quisque porta placerat finibus. Fusce tempor porta varius. Nullam ullamcorper vitae nisi in faucibus. Vestibulum eget dolor cursus, condimentum massa at, viverra turpis. Sed eu aliquam mauris, sit amet efficitur metus. Mauris ac sodales est.\n" +
                    "\n" +
                    "Integer nunc neque, semper non nisi nec, vehicula sodales sem. Nullam dictum est eu porta tempus. Pellentesque malesuada convallis neque. Nunc ultricies faucibus leo, et aliquam purus imperdiet vel. Mauris nunc est, dignissim vel mi et, posuere ornare metus. Donec lectus arcu, consectetur sed iaculis sit amet, cursus non nunc. Etiam dictum justo at nisl laoreet, maximus pharetra elit imperdiet. Pellentesque vel diam at leo scelerisque porttitor nec ut purus. Ut cursus lobortis malesuada. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. In dapibus, dui eu posuere egestas, augue orci rutrum ex, non suscipit purus ligula at urna. Nunc vel arcu non mi molestie consectetur. Morbi dignissim magna at urna mattis gravida. Pellentesque placerat est lacus, sed varius orci tempor ac.\n" +
                    "\n" +
                    "Quisque consectetur, sapien non aliquam luctus, est urna aliquam purus, eu feugiat diam ex in ex. Phasellus ut nibh vitae libero iaculis accumsan. Morbi vitae orci ut eros tempus rhoncus ut eget justo. Vestibulum a accumsan urna. Cras sit amet lectus sed eros sollicitudin maximus. Fusce at vulputate eros, in bibendum ipsum. Etiam vitae malesuada metus, ac mattis felis. Nullam in facilisis justo. Morbi elementum vestibulum eros in vehicula.");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenMultiLineContentWithEmptyLineAndWindowsNewLineInputSameContentAsApproved() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\r\nMauris gravida varius dolor, vel imperdiet urna consectetur a.\r\n";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenMultiLineContentWithEmptyLineAndWindowsNewLineInputSameContentAsApproved");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("972efc-approved.content"), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\nMauris gravida varius dolor, vel imperdiet urna consectetur a.\n");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/972efc-approved.content", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\nMauris gravida varius dolor, vel imperdiet urna consectetur a.\n");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenMultiLineContentWithEmptyLineAndWindowsNewLineExpectedContentSameContentAsApproved() {
        String actual = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\nMauris gravida varius dolor, vel imperdiet urna consectetur a.\n";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenMultiLineContentWithEmptyLineAndWindowsNewLineInputSameContentAsApproved");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("972efc-approved.content"), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\r\nMauris gravida varius dolor, vel imperdiet urna consectetur a.\r\n");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/972efc-approved.content", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\r\nMauris gravida varius dolor, vel imperdiet urna consectetur a.\r\n");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowExceptionrWhenContentIsNull() {
        String actual = null;
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowExceptionrWhenContentIsNull");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            IllegalArgumentException actualError = assertThrows(IllegalArgumentException.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("Only String content matcher is supported!", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());

            assertIterableEquals(Collections.emptyList(), actualFiles);
        });
    }

    @Test
    public void shouldThrowExceptionrWhenContentIsANumber() {
        Long actual = 1L;
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowExceptionrWhenContentIsANumber");
            ContentMatcher<Long> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            IllegalArgumentException actualError = assertThrows(IllegalArgumentException.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("Only String content matcher is supported!", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());

            assertIterableEquals(Collections.emptyList(), actualFiles);
        });
    }

    @Test
    public void contentMatcherWorkflowVerifierTest() {
        String actual = "Testing workflow of ContentMatcher";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "contentMatcherWorkflowVerifierTest");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "e4bc67-not-approved.content", "e4bc67-approved.content"), actualError.getMessage());

            approveFile(imfsi.getTestPath().resolve("87668f").resolve("e4bc67-not-approved.content"));

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/e4bc67-approved.content", "/*ContentMatcherTest.contentMatcherWorkflowVerifierTest*/\n" +
                    "Testing workflow of ContentMatcher");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void contentMatcherWorkflowWithNonAsciiCharacters() {
        String actual = "Árvízűtűrőtükörfúrógép\\nL’apostrophe 用的名字☺\\nд1@00000☺☹❤\\naA@AA1A猫很可爱";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "contentMatcherWorkflowWithNonAsciiCharacters");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "c5f725-not-approved.content", "c5f725-approved.content"), actualError.getMessage());

            approveFile(imfsi.getTestPath().resolve("87668f").resolve("c5f725-not-approved.content"));

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi.getInMemoryFileSystem());
            InMemoryFiles expected = new InMemoryFiles("87668f/c5f725-approved.content", "/*ContentMatcherTest.contentMatcherWorkflowWithNonAsciiCharacters*/\n" + actual);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }
}
