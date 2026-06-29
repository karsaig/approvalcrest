package com.github.karsaig.approvalcrest.dedup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Replaces all pointer files with standalone approved files containing the canonical content,
 * then removes all canonical files from the shared approvals directory.
 *
 * <p>Use this to migrate the shared directory location (reinstate, then dedup with the new config),
 * or to fully opt out of the shared file system.
 *
 * <p>Run with: {@code mvn approvalcrest:reinstate}
 */
@Mojo(name = "reinstate")
public class ReinstateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true, required = true)
    private File projectBaseDir;

    /** Shared approvals directory, relative to the project base directory. */
    @Parameter(property = "fileMatcherSharedDir", defaultValue = "src/test/java/shared-approvals")
    private String sharedDir;

    /** Directory to scan for pointer files, relative to the project base directory. */
    @Parameter(defaultValue = "src/test/java")
    private String dir;

    @Override
    public void execute() throws MojoExecutionException {
        Path workingDirectory = projectBaseDir.toPath();
        Path scanDirPath = workingDirectory.resolve(dir);
        try {
            ApprovalReinstate reinstater = new ApprovalReinstate(workingDirectory, scanDirPath, sharedDir);
            ApprovalReinstate.ReinstateResult result = reinstater.reinstate();
            getLog().info(result.toString());
        } catch (IOException e) {
            throw new MojoExecutionException("Reinstate failed", e);
        }
    }
}
