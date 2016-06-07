package org.apache.hadoop.contrib.ftp;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.Logger;

/**
 * Implemented FileSystemView to use HdfsFileObject
 */
public class HdfsFileSystemView implements FileSystemView {

    // the first and the last character will always be '/'
    // It is always with respect to the root directory.
    private String currDir = "/";

    private User user;
    private final FileSystem dfs;
    // <- 2060607, leo
    public UserGroupInformation proxyUgi;
    private final Logger log = Logger.getLogger(HdfsFtpFile.class);
    // ->
    /**
     * Constructor - set the user object.
     */
    protected HdfsFileSystemView(User user, FileSystem fs) throws FtpException {
        if (user == null) {
            throw new IllegalArgumentException("user can not be null");
        }
        /* <- 20160607 leo: we will use the HDFS user home prefix
        if (user.getHomeDirectory() == null) {
            throw new IllegalArgumentException("User home directory can not be null");
        }
        -> */
        if(fs == null){
            throw new IllegalArgumentException("File system can not be null");
        }
        this.user = user;
        this.dfs = fs;
        // <- 2060607, leo
        this.currDir = this.getUserHomeDirectory();
        try {
            this.proxyUgi = UserGroupInformation.createProxyUser(user.getName(), UserGroupInformation.getLoginUser());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        // ->
    }

    /**
     * Get the user home directory. It would be the file system root for the
     * user.
     */
    public FtpFile getHomeDirectory() {
        // <- 20160607, leo
        return new HdfsFtpFile(this.getUserHomeDirectory(), user, dfs);
        // ->
    }

    /**
     *  20160607, leo: in HDFS, the user home directory begins with /user directory
     */
    public String getUserHomeDirectory() {
        return "/user/" + user.getName();
    }

    /**
     * Get the current directory.
     */
    public FtpFile getWorkingDirectory() {
        return new HdfsFtpFile(currDir, user, dfs);
    }

    /**
     * Get file object.
     */
    public FtpFile getFile(String file) {
        String path;
        if (file.startsWith("/")) {
            path = file;
        } else if (currDir.length() > 1) {
            path = currDir + "/" + file;
        } else {
            path = "/" + file;
        }
        // <- 20160607, leo
        return new HdfsFtpFile(path, this.user, this.dfs, this.proxyUgi);
        // ->
    }

    /**
     * Change directory.
     */
    public boolean changeWorkingDirectory(String dest) {
        // <- 20160607, leo
        FtpFile dir = this.getFile(dest);
        if (dir.isDirectory() && dir.isReadable()) {
            currDir =  dir.getAbsolutePath();
            return true;
        } else {
            return false;
        }
        // ->
    }

    /**
     * Is the file content random accessible?
     */
    public boolean isRandomAccessible() {
        return true;
    }

    /**
     * Dispose file system view - does nothing.
     */
    public void dispose() {
    }
}
