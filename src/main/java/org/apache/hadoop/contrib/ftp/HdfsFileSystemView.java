package org.apache.hadoop.contrib.ftp;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.apache.hadoop.fs.FileSystem;

/**
 * Implemented FileSystemView to use HdfsFileObject
 */
public class HdfsFileSystemView implements FileSystemView {

    // the first and the last character will always be '/'
    // It is always with respect to the root directory.
    private String currDir = "/";

    private User user;
    private final FileSystem dfs;

    /**
     * Constructor - set the user object.
     */
    protected HdfsFileSystemView(User user, FileSystem fs) throws FtpException {
        if (user == null) {
            throw new IllegalArgumentException("user can not be null");
        }
        if (user.getHomeDirectory() == null) {
            throw new IllegalArgumentException(
                    "User home directory can not be null");
        }
        if(fs == null){
            throw new IllegalArgumentException(
                    "File system can not be null");
        }
        this.user = user;
        this.dfs = fs;
        // <- 20131113, leo
        this.currDir = user.getHomeDirectory();
        // ->
    }

    /**
     * Get the user home directory. It would be the file system root for the
     * user.
     */
    public FtpFile getHomeDirectory() {
        return new HdfsFtpFile(user.getHomeDirectory(), user, dfs);
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
        return new HdfsFtpFile(path, user, dfs);
    }

    /**
     * Change directory.
     */
    public boolean changeWorkingDirectory(String dir) {
        String path;
        if (dir.startsWith("/")) {
            path = dir;
        } else if (currDir.length() > 1) {
            path = currDir + "/" + dir;
        } else {
            path = "/" + dir;
        }
        HdfsFtpFile file = new HdfsFtpFile(path, user, dfs);
        if (file.isDirectory() && file.isReadable()) {
            currDir = path;
            return true;
        } else {
            return false;
        }
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
