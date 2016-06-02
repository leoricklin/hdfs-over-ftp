package org.apache.hadoop.contrib.ftp;

import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Start-up class of FTP server
 */
public class HdfsOverFtpServer {

    private static Logger log = Logger.getLogger(HdfsOverFtpServer.class);

    private static final String USERS_CONF = "users.conf";
    private static final String CONF_FILE = "hdfs-over-ftp.conf";

    private static int port = 2222;
    private static int sslPort = 2226;
    private static String passivePorts = null;
    private static String sslPassivePorts = null;
    private static String hdfsUri = null;
    private static String sslPassword = null;

    public static void main(String[] args) throws Exception {
        loadConfig();

        startServer();

        startSSLServer();
    }

    /**
     * Load configuration
     *
     * @throws IOException
     */
    private static void loadConfig() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(CONF_FILE));

        try {
            port = Integer.parseInt(props.getProperty("port"));
            log.info("port is set. ftp server will be started");
        } catch (Exception e) {
            log.info("port is not set. so ftp server will not be started");
        }

        try {
            sslPort = Integer.parseInt(props.getProperty("ssl-port"));
            log.info("ssl-port is set. ssl server will be started");
        } catch (Exception e) {
            log.info("ssl-port is not set. so ssl server will not be started");
        }

        passivePorts = props.getProperty("data-ports");
        if (passivePorts == null) {
             log.fatal("data-ports is not set");
             System.exit(1);
        }

        sslPassivePorts = props.getProperty("ssl-data-ports");
        if (sslPassivePorts == null) {
            log.fatal("ssl-data-ports is not set");
            System.exit(1);
        }
        sslPassword = props.getProperty("keystore-password");
            if (sslPassword == null) {
                log.fatal("keystore-password is not set");
                System.exit(1);
        }

        hdfsUri = props.getProperty("hdfs-uri");
        if (hdfsUri == null) {
            log.fatal("hdfs-uri is not set");
            System.exit(1);
        }

        String superuser = props.getProperty("superuser");
        if (superuser == null) {
            log.fatal("superuser is not set");
            System.exit(1);
        }
        HdfsOverFtpSystem.setSuperuser(superuser);
    }

    /**
     * Starts FTP server
     *
     * @throws Exception
     */
    public static void startServer() throws Exception {

        log.info("Starting Hdfs-Over-Ftp server. port: " + port + " data-ports: " + passivePorts + " hdfs-uri: " + hdfsUri);

        HdfsOverFtpSystem.setHDFS_URI(hdfsUri);

        FtpServerFactory ftpServerFactory = new FtpServerFactory();

        DataConnectionConfigurationFactory dataConFactory = new DataConnectionConfigurationFactory();
        dataConFactory.setPassivePorts(passivePorts);

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setDataConnectionConfiguration(dataConFactory.createDataConnectionConfiguration());
        listenerFactory.setPort(port);

        Map<String, Listener> listenerMap = new HashMap<String, Listener>();
        listenerMap.put("default", listenerFactory.createListener());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(new File(USERS_CONF));
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());//SaltedPasswordEncryptor());
        UserManager um = userManagerFactory.createUserManager();


        ftpServerFactory.setListeners(listenerMap);
        ftpServerFactory.setUserManager(um);
        ftpServerFactory.setFileSystem(new HdfsFileSystemFactory());

        FtpServer ftpServer = ftpServerFactory.createServer();
        ftpServer.start();
    }

    /**
     * Starts SSL FTP server
     *
     * @throws Exception
     */
    public static void startSSLServer() throws Exception {

        log.info("Starting Hdfs-Over-Ftp SSL server. ssl-port: " + sslPort + " ssl-data-ports: " + sslPassivePorts + " hdfs-uri: " + hdfsUri);


        HdfsOverFtpSystem.setHDFS_URI(hdfsUri);

        FtpServerFactory ftpServerFactory = new FtpServerFactory();

        DataConnectionConfigurationFactory dataConFactory = new DataConnectionConfigurationFactory();
        dataConFactory.setPassivePorts(sslPassivePorts);

        SslConfigurationFactory sslConfigurationFactory = new SslConfigurationFactory();
        sslConfigurationFactory.setKeystoreFile(new File("ftp.jks"));
        sslConfigurationFactory.setKeystoreType("JKS");
        sslConfigurationFactory.setKeystorePassword(sslPassword);

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setDataConnectionConfiguration(dataConFactory.createDataConnectionConfiguration());
        listenerFactory.setPort(sslPort);
        listenerFactory.setSslConfiguration(sslConfigurationFactory.createSslConfiguration());
        listenerFactory.setImplicitSsl(true);

        Map<String, Listener> listenerMap = new HashMap<String, Listener>();
        listenerMap.put("default", listenerFactory.createListener());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(new File(USERS_CONF));
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());//SaltedPasswordEncryptor());
        UserManager um = userManagerFactory.createUserManager();

        ftpServerFactory.setListeners(listenerMap);
        ftpServerFactory.setUserManager(um);
        ftpServerFactory.setFileSystem(new HdfsFileSystemFactory());

        FtpServer ftpServer = ftpServerFactory.createServer();
        ftpServer.start();
    }
}
