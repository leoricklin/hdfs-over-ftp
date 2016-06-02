package org.apache.hadoop.contrib.ftp;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * This class implements all actions to HDFS
 */
public class HdfsFtpFile implements FtpFile {

    private final Logger log = Logger.getLogger(HdfsFtpFile.class);

    private final Path path;
    private final User user;
    private final FileSystem dfs;
    public static final String SUPERGROUP = "supergroup";

    /**
     * Constructs HdfsFileObject from path
     *
     * @param path path to represent object
     * @param user accessor of the object
     * v0.20.2, 2011/07/18, leo, print out debug log
     */
    public HdfsFtpFile(String path, User user, FileSystem dfs) {
       log.debug("path[" + path + "] user[" + user.getName() + "]");
       this.path = new Path(path);
       this.user = user;
       this.dfs = dfs;
    }

    /**
     * 
     */
    public String getAbsolutePath() {
       log.debug("getAbsolutePath[" + path.toString() + "]");
       return path.toString();
    }
    /**
     * v0.20.2, 2011/07/18, leo, using Path.getName() to returns the final component of this path.
     */
    public String getName() {
       log.debug("path[" + this.path.getName() + "]");
       return this.path.getName();
       /*
       String full = getAbsolutePath();
       int pos = full.lastIndexOf("/");
       if (full.length() == 1) {
           return "/";
       }
       return full.substring(pos + 1);
       */
    }

    /**
     * HDFS has no hidden objects
     * @return always false
     */
    public boolean isHidden() {
       return false;
    }

    /**
     * Checks if the object is a directory
     * @return true if the object is a directory
     */
    public boolean isDirectory() {
        try {
/* v2.0.0, 2013/07/08, leo, replace with hadoop 2.0.0 API
            FileStatus staus = dfs.getFileStatus(path);
            log.debug("path[" + this.getName() + " isDirectory[" + staus.isDir() + "]");
            return staus.isDir();
*/
           log.debug("path[" + this.getName() + " isDirectory[" + dfs.isDirectory(path) + "]");
           return dfs.isDirectory(path);
        } catch (IOException e) {
            log.warn(path + " is not dir", e);
            return false;
        }
    }

    /**
     * Get HDFS permissions
     * @return HDFS permissions as a FsPermission instance
     * @throws IOException if path doesn't exist so we get permissions of parent object in that case
     */
    private FsPermission getPermissions() throws IOException {
       return dfs.getFileStatus(path).getPermission();
    }
    /**
     * Checks if the object is a file
     *
     * @return true if the object is a file
     */
    public boolean isFile() {
       try {
          log.debug("path[" + this.getName() + " isFile[" + dfs.isFile(path) + "]");
          return dfs.isFile(path);
       } catch (IOException e) {
          log.warn(path + " is not file", e);
          return false;
       }
    }

    /**
     * Checks if the object does exist
     *
     * @return true if the object does exist
     * v0.20.2, 2011/07/18, leo, using exists() to check if exists. 
     */
   public boolean doesExist() {
      try {
         log.debug("path[" + this.getName() + " doesExist[" + dfs.exists(path) + "]");
         return dfs.exists(path);
         // dfs.getFileStatus(path);
         // return true;
      } catch (FileNotFoundException e){
          return false;
      } catch (IOException e) {
          log.debug("IOException has been thrown while trying read from " + path);
          return false;
      }
   }

    /**
     * Checks if the user has a read permission on the object
     *
     * @return true if the user can read the object
     * v0.20.2, 2011/07/18, leo, using org.apache.hadoop.fs.permission.FsAction
     * 
     */
   public boolean isReadable() {
      try {
         FsPermission permissions = getPermissions();
         FsAction action = null; 
         if (user.getName().equals(getOwnerName())) {
            // check owner's permission
            action = permissions.getUserAction(); 
            log.debug("path[" + this.getName() + " owner action[" + action.toString() + "]");
            /*
             if (permissions.toString().substring(0, 1).equals("r")) {
                 log.debug("PERMISSIONS: " + path + " - " + " read allowed for user");
                 return true;
             }
            */
//         } else if (user.isGroupMember(getGroupName())) {
//             if (permissions.toString().substring(3, 4).equals("r")) {
//                 log.debug("PERMISSIONS: " + path + " - " + " read allowed for group");
//                 return true;
//             }
         } else {
            // check other's permission
            action = permissions.getOtherAction();
            log.debug("path[" + this.getName() + " other action[" + action.toString() + "]");
            /*
             if (permissions.toString().substring(6, 7).equals("r")) {
                 log.debug("PERMISSIONS: " + path + " - " + " read allowed for others");
                 return true;
             }
            */
         }
         /*
         if (action.equals(FsAction.READ) || action.equals(FsAction.READ_WRITE) 
               || action.equals(FsAction.READ_EXECUTE) || action.equals(FsAction.ALL)) {
            log.debug("PERMISSIONS: " + path + " - " + " read allowed for user");
            return true;
         }            
         */
         if (action.implies(FsAction.READ)) {
            log.debug("PERMISSIONS: " + path + " - " + " read allowed for user");
            return true;
         } else {
            log.warn("PERMISSIONS: " + path + " - " + " read denied");
            return false;
         }
      } catch (IOException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        return false;
      }
   }
    /**
     * @return
     * v0.20.2, 2011/07/19, leo, using org.apache.hadoop.fs.Path.getParent() 
     */
   private HdfsFtpFile getParent() {
      String parentS = "/";
      /*
      String pathS = path.toString();
      int pos = pathS.lastIndexOf("/");
      if (pos > 0) {
          parentS = pathS.substring(0, pos);
      }
      */
      Path ppath = this.path.getParent();
      if (ppath != null) {
         parentS = ppath.toString();
      }
      log.debug("path[" + this.getName() + " getParent[" + parentS + "]");
      return new HdfsFtpFile(parentS, user, dfs);
   }
    /**
     * Checks if the user has a write permission on the object
     * @return true if the user has write permission on the object
     * v0.20.2, 2011/07/18, leo, using org.apache.hadoop.fs.permission.FsAction
     */
   public boolean isWritable() {
      try {
         FsPermission permissions = getPermissions();
         FsAction action = null; 
         if (user.getName().equals(getOwnerName())) {
            // check owner's permission
            action = permissions.getUserAction(); 
            log.debug("path[" + this.getName() + " owner action[" + action.toString() + "]");
            /*
            if (permissions.toString().substring(1, 2).equals("w")) {
               log.debug("PERMISSIONS: " + path + " - " + " write allowed for user");
               return true;
            }
            */
//       } else if (user.isGroupMember(getGroupName())) {
//          if (permissions.toString().substring(4, 5).equals("w")) {
//             log.debug("PERMISSIONS: " + path + " - " + " write allowed for group");
//             return true;
//          }
         } else {
            // check other's permission
            action = permissions.getOtherAction();
            log.debug("path[" + this.getName() + " other action[" + action.toString() + "]");
            /*
            if (permissions.toString().substring(7, 8).equals("w")) {
               log.debug("PERMISSIONS: " + path + " - " + " write allowed for others");
               return true;
            }
            */
         }
         if (action.implies(FsAction.WRITE)) {
            log.debug("PERMISSIONS: " + path + " - " + " write allowed for " + user.getName());
            return true;
         } else {
            log.warn("PERMISSIONS: " + path + " - " + " write denied for " + user.getName());
            return false;
         }
         /*
         log.debug("PERMISSIONS: " + path + " - " + " write denied");
         return false;
         */
      } catch (IOException e) {
         return getParent().isWritable();
      }
   }
   /**
    * Checks if the user has a delete permission on the object
    * @return true if the user has delete permission on the object
    */
   public boolean isRemovable() {
      log.debug("path[" + this.getName() + " isRemovable[" + isWritable() + "]"); 
      return isWritable();
   }
   /**
    * Get owner of the object
    * @return owner of the object
    * v0.20.2, 2011/07/18, leo, print out debug log
    */
   public String getOwnerName() {
      try {
         FileStatus fs = dfs.getFileStatus(path);
         log.debug("path[" + this.getName() + " getOwnerName[" + fs.getOwner() + "]");
         return fs.getOwner();
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }
    /**
     * Get group of the object
     * @return group of the object
     * v0.20.2, 2011/07/18, leo, print out debug log
     */
    public String getGroupName() {
       try {
          FileStatus fs = dfs.getFileStatus(path);
          log.debug("path[" + this.getName() + " getGroupName[" + fs.getGroup() + "]]");
          return fs.getGroup();
       } catch (IOException e) {
          e.printStackTrace();
          return null;
       }
    }
   /**
     * Get link count
     * @return 3 is for a directory and 1 is for a file
     */
   public int getLinkCount() {
       return isDirectory() ? 3 : 1;
   }
   /**
    * Get last modification date
    *
    * @return last modification date as a long
    */
   public long getLastModified() {
      try {
         FileStatus fs = dfs.getFileStatus(path);
         log.debug("path[" + this.getName() + " getLastModified[" + fs.getModificationTime() + "]]");
         return fs.getModificationTime();
      } catch (IOException e) {
         e.printStackTrace();
         return 0;
      }
   }
   public boolean setLastModified(long l) {
      return false;
   }
   /**
    * Get a size of the object
    * @return size of the object in bytes
    */
   public long getSize() {
      try {
         FileStatus fs = dfs.getFileStatus(path);
         log.debug("path[" + this.getName() + " getSize[" + fs.getLen() + "]");
         return fs.getLen();
      } catch (IOException e) {
         e.printStackTrace();
         return 0;
      }
   }
   /**
    * Create a new dir from the object
    * @return true if dir is created
    * v0.20.2, 2011/07/18, Leo: disable dfs.setOwner() due to only HDFS super user could change owner.
    */
   public boolean mkdir() {
      if (!isWritable()) {
         log.warn("user " + user.getName() + " has No write permission : " + path);
         return false;
      }
      try {
         dfs.mkdirs(path);
         /* <- 20131113, leo
          * dfs.setOwner(path, user.getName(), SUPERGROUP); // user.getMainGroup()
          --> */         
         return true;
      } catch (IOException e) {
         log.error("mkdir error", e);
         return false;
      }
   }
   /**
    * Delete object from the HDFS filesystem
    * @return true if the object is deleted
    * v0.20.2, 2011/07/19, Leo: check write permission before delete
    */
   public boolean delete() {
      if (!isWritable()) {
         log.warn("user " + user.getName() + " has No write permission : " + path);
         return false;
      }
      try {
         dfs.delete(path, true);
         return true;
      } catch (IOException e) {
         log.error("delete error", e);
         return false;
      }
   }
   /**
    * Move the object to another location
    * @param fileObject location to move the object
    * @return true if the object is moved successfully
    * v0.20.2, 2011/07/19, Leo: check write permission before delete
    */
   public boolean move(FtpFile fileObject) {
      if (!isWritable()) {
         log.warn("user " + user.getName() + " has No write permission : " + path);
         return false;
      }
      try {
         dfs.rename(path, new Path(fileObject.getAbsolutePath()));
         return true;
      } catch (IOException e) {
         log.error("move error", e);
         return false;
      }
   }
   /**
    * List files of the directory
    * @return List of files in the directory
    */
    public List<FtpFile> listFiles() {
       if (!isReadable()) {
          log.warn("user " + user.getName() + " has No read permission : " + path);
          return null;
       }
       try {
          FileStatus fileStats[] = dfs.listStatus(path);
          FtpFile fileObjects[] = new FtpFile[fileStats.length];
          for (int i = 0; i < fileStats.length; i++) {
             fileObjects[i] = new HdfsFtpFile(fileStats[i].getPath().toString(), user, dfs);
          }
          return Arrays.asList(fileObjects);
       } catch (IOException e) {
          log.debug("", e);
          return null;
       }
    }
   /**
    * Creates output stream to write to the object
    * @param l is not used here
    * @return OutputStream
    * @throws IOException
    * v0.20.2, 2011/07/18, leo: disable dfs.setOwner() due to only HDFS super user could change owner.
    * v0.20.2, 2011/07/21, leo: remove try-catch block
    */
   public OutputStream createOutputStream(long l) throws IOException {
      log.debug("path[" + this.getName() + "createOutputStream [" + l + "]");
      if (!isWritable()) {
         // check permission
         throw new IOException("user " + user.getName() + " has No write permission : " + path);
      }
      FSDataOutputStream out = null;
      if (this.doesExist()) {
         // resuming
         if (this.getSize() == l) {
            // starting address is same as the EOF
            out = dfs.append(path);
         } else {
            throw new IOException("resuming offset is wrong");
         }
      } else {
         // new file
         out = dfs.create(path);
      }
      /* <- 20131113, leo
       * dfs.setOwner(path, user.getName(), SUPERGROUP); // user.getMainGroup()
      --> */
      dfs.setPermission(this.path, new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL));
      return out;
   }
   /**
    * Creates input stream to read from the object
    *
    * @param l is not used here
    * @return OutputStream
    * @throws IOException
    * v0.20.2, 2011/07/20, leo: check offset against the size of file
    * v0.20.2, 2011/07/21, leo: remove try-catch block
    * v0.20.2, 2011/07/22, leo: add in.seek(l)
    */
   public InputStream createInputStream(long l) throws IOException {
      log.debug("path[" + this.getName() + "createInputStream [" + l + "]");
      // permission check
      if (!isReadable()) {
         throw new IOException("user " + user.getName() + " has No read permission : " + path);
      }
      if (l >= this.getSize() ) {
         throw new IOException("offset " + l + " over the size of " + path);
      }
      FSDataInputStream in = dfs.open(path);
      in.seek(l);
      return in;
   }
}
