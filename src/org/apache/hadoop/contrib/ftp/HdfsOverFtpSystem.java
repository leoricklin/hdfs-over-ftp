package org.apache.hadoop.contrib.ftp;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;

/**
 * Class to store DFS connection
 */
public class HdfsOverFtpSystem {

    private static FileSystem dfs = null;

    public static String HDFS_URI = "";

    private static String superuser = "superuser";
    private static String supergroup = "supergroup";

    private final static Logger log = Logger.getLogger(HdfsOverFtpSystem.class);

    private static void hdfsInit() {
        Path p = new Path(HDFS_URI);

        Configuration conf = new Configuration();
        // conf.set("hadoop.job.ugi", superuser + "," + supergroup);
        try {
            dfs = p.getFileSystem(conf);
        } catch (Exception e) {
            log.error("DFS Initialization error", e);
            throw new RuntimeException("DFS Initialization error");
        }
    }

    public static void setHDFS_URI(String HDFS_URI) {
        HDFS_URI = HDFS_URI;
    }

    /**
     * Get dfs
     *
     * @return dfs
     * @throws IOException
     */
    public static FileSystem getDfs() {
        if (dfs == null) {
            hdfsInit();
        }
        return dfs;
    }

    /**
     * Set superuser. and we connect to DFS as a superuser
     *
     * @param superuser
     */
    public static void setSuperuser(String superuser) {
        superuser = superuser;
    }
}
