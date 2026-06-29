package com.github.karsaig.approvalcrest.dedup;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.karsaig.approvalcrest.FileMatcherConfig;

/**
 * CLI entry point for the approvalcrest deduplication tool.
 *
 * <p>Usage:
 * <pre>
 *   java -jar approvalcrest-dedup-VERSION-standalone.jar [options]
 *
 *   Options:
 *     --dir &lt;path&gt;          Scan directory (default: src/test/java)
 *     --shared-dir &lt;path&gt;   Shared approvals directory (default: src/test/java/shared-approvals)
 *     --bucket-depth &lt;n&gt;    Bucket prefix depth 1-6 (default: 2)
 *     --dry-run             Print what would be done without writing files
 *     --reinstate           Replace all pointers with standalone content and clear the shared dir
 * </pre>
 *
 * <p>All options also read from system properties ({@code fmSharedDir},
 * {@code fmSharedDirBucketDepth}, etc.) when not provided on the command line.
 */
public class DedupCli {

    public static void main(String[] args) throws IOException {
        run(args, Paths.get("").toAbsolutePath(), System.out);
    }

    static void run(String[] args, Path workingDirectory, PrintStream out) throws IOException {
        String dir = null;
        String sharedDir = null;
        Integer bucketDepth = null;
        boolean dryRun = false;
        boolean reinstate = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--dir".equals(arg) && i + 1 < args.length) {
                dir = args[++i];
            } else if ("--shared-dir".equals(arg) && i + 1 < args.length) {
                sharedDir = args[++i];
            } else if ("--bucket-depth".equals(arg) && i + 1 < args.length) {
                bucketDepth = Integer.parseInt(args[++i]);
            } else if ("--dry-run".equals(arg)) {
                dryRun = true;
            } else if ("--reinstate".equals(arg)) {
                reinstate = true;
            }
        }

        FileMatcherConfig config = new FileMatcherConfig();
        if (sharedDir == null) {
            sharedDir = config.getSharedApprovalDirectory();
        }
        if (bucketDepth == null) {
            bucketDepth = config.getSharedBucketDepth();
        }

        Path scanDirPath = dir != null ? workingDirectory.resolve(dir) : workingDirectory.resolve("src/test/java");

        if (reinstate) {
            ApprovalReinstate reinstater = new ApprovalReinstate(workingDirectory, scanDirPath, sharedDir);
            ApprovalReinstate.ReinstateResult result = reinstater.reinstate();
            out.println(result);
        } else {
            ApprovalDeduplicator deduplicator = new ApprovalDeduplicator(
                    workingDirectory, scanDirPath, sharedDir, bucketDepth, dryRun);
            ApprovalDeduplicator.DeduplicatorResult result = deduplicator.deduplicate();
            out.println(result);
        }
    }
}
