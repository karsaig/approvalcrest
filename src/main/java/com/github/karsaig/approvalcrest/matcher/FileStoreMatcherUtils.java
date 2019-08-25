package com.github.karsaig.approvalcrest.matcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Utility class with methods for creating the JSON files for
 * {@link JsonMatcher}.
 *
 * @author Andras_Gyuro
 */
public class FileStoreMatcherUtils {

    public static final Object SEPARATOR = "-";
    private static final String SRC_TEST_JAVA_PATH = "src" + File.separator + "test" + File.separator + "java"
            + File.separator;
    private static final String APPROVED_NAME_PART = "approved";
    private static final String NOT_APPROVED_NAME_PART = "not-approved";
    private static final Pattern DOT_LITERAL_PATTERN = Pattern.compile(".", Pattern.LITERAL);
    private final String fileExtension;

    public FileStoreMatcherUtils(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Creates file with '-not-approved' suffix and .json extension and writes
     * the jsonObject in it.
     *
     * @param fileNameWithPath specifies the name of the file with full path (relative to
     *                         project root)
     * @param jsonObject       the file's content
     * @throws IOException exception thrown when failed to create the file
     */
    public String createNotApproved(String fileNameWithPath, String jsonObject, String comment)
            throws IOException {
        File file = new File(getFullFileName(fileNameWithPath, false));
        File parent = file.getParentFile();
        parent.mkdirs();
        parent.setExecutable(true, false);
        parent.setReadable(true, false);
        parent.setWritable(true, false);
        return writeToFile(file, jsonObject, comment);
    }

    public String overwriteApprovedFile(String fileNameWithPath, String jsonObject, String comment) throws IOException {
        File file = new File(getFullFileName(fileNameWithPath, true));
        return writeToFile(file, jsonObject, comment);
    }

    private String writeToFile(File file, String jsonObject, String comment) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = Files.newWriter(file, Charsets.UTF_8);
            writer.append("/*" + comment + "*/");
            writer.append("\n");
            writer.append(jsonObject);
            writer.close();
            file.setReadable(true, false);
            file.setWritable(true, false);
            return file.getName();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public String readFile(File file) throws IOException {
        String fileContent = Files.toString(file, Charsets.UTF_8);

        if (fileContent.startsWith("/*")) {
            int index = fileContent.indexOf("*/\n");
            if (-1 < index) {
                return fileContent.substring(index + 3);
            }
        }
        return fileContent;
    }

    /**
     * Gets file with '-approved' suffix and .json extension and returns it.
     *
     * @param fileNameWithPath the name of the file with full path (relative to project root)
     * @return the {@link File} object
     */
    public File getApproved(String fileNameWithPath) {
        File file = new File(getFullFileName(fileNameWithPath, true));
        return file;
    }

    /**
     * Returns the name of the test method, in which the call was originated
     * from.
     *
     * @return test method name in String
     */
    public String getCallerTestMethodName() {
        StackTraceElement testStackTraceElement = getTestStackTraceElement(Thread.currentThread().getStackTrace());
        return testStackTraceElement != null ? testStackTraceElement.getMethodName() : null;
    }

    /**
     * Returns the name of the test class file which the call was originated
     * from.
     *
     * @return test method's class name
     */
    public String getCallerTestClassName() {
        StackTraceElement testStackTraceElement = getTestStackTraceElement(Thread.currentThread().getStackTrace());
        return testStackTraceElement != null ? testStackTraceElement.getClassName() : null;
    }

    /**
     * Returns the absolute path of the test class in which the call was
     * originated from.
     *
     * @return test method name in String
     */
    public String getCallerTestClassPath() {
        StackTraceElement testStackTraceElement = getTestStackTraceElement(Thread.currentThread().getStackTrace());
        String fileName = testStackTraceElement.getFileName().substring(0,
                testStackTraceElement.getFileName().lastIndexOf("."));
        return SRC_TEST_JAVA_PATH
                + DOT_LITERAL_PATTERN.matcher(testStackTraceElement.getClassName()).replaceAll(Matcher.quoteReplacement(File.separator)).replace(fileName, "");
    }

    private StackTraceElement getTestStackTraceElement(StackTraceElement[] stackTrace) {
        StackTraceElement result = null;
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement s = stackTrace[i];
            if (isTestMethod(s)) {
                result = s;
                break;
            }
        }
        return result;
    }

    private boolean isTestMethod(StackTraceElement element) {
        boolean isTest;

        String fullClassName = element.getClassName();
        Class<?> clazz;
        try {
            clazz = Class.forName(fullClassName);
            Method method = findMethod(clazz, element.getMethodName());
            isTest = method != null && method.isAnnotationPresent(Test.class);
        } catch (Throwable e) {
            isTest = false;
        }

        return isTest;
    }

    private Method findMethod(Class clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    public String getFullFileName(String fileName, boolean approved) {
        return getFileNameWithExtension(fileName, approved);
    }

    private String getFileNameWithExtension(String fileName, boolean approved) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(fileName);
        stringBuilder.append(SEPARATOR);
        if (approved) {
            stringBuilder.append(APPROVED_NAME_PART);
        } else {
            stringBuilder.append(NOT_APPROVED_NAME_PART);
        }
        stringBuilder.append(fileExtension);

        return stringBuilder.toString();
    }
}
