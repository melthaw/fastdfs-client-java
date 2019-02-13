/**
 * Copyright (C) 2008 Happy Fish / YuQing
 * <p>
 * FastDFS Java Client may be copied only under the terms of the GNU Lesser
 * General Public License (LGPL).
 * Please visit the FastDFS Home Page http://www.csource.org/ for more detail.
 **/

package org.csource.fastdfs;

import org.csource.common.NameValuePair;

import java.io.File;
import java.net.InetSocketAddress;

/**
 * client test
 *
 * @author Happy Fish / YuQing
 * @version Version 1.20
 */
public class TestAppender1 {
  private TestAppender1() {
  }

  /**
   * entry point
   *
   * @param args comand arguments
   *             <ul><li>args[0]: config filename</li></ul>
   *             <ul><li>args[1]: local filename to upload</li></ul>
   */
  public static void main(String args[]) {
    if (args.length < 2) {
      System.out.println("Error: Must have 2 parameters, one is config filename, "
        + "the other is the local filename to upload");
      return;
    }

    System.out.println("java.version=" + System.getProperty("java.version"));

    String conf_filename = args[0];
    String local_filename = args[1];

    try {
      ClientGlobal.init(conf_filename);
      System.out.println("network_timeout=" + ClientGlobal.networkTimeout + "ms");
      System.out.println("charset=" + ClientGlobal.charset);

      long startTime;
      ServerInfo[] servers;
      TrackerClient tracker = new TrackerClient();
      TrackerServer trackerServer = tracker.getConnection();

      StorageServer storageServer = null;

  		/*
      storageServer = tracker.getStoreStorage(trackerServer);
  		if (storageServer == null)
  		{
  			System.out.println("getStoreStorage fail, error code: " + tracker.getErrorCode());
  			return;
  		}
  		*/

      StorageClient1 client = new StorageClient1(trackerServer, storageServer);
      byte[] file_buff;
      NameValuePair[] meta_list;
      String group_name;
      String appender_file_id;
      String file_ext_name;
      int errno;

      meta_list = new NameValuePair[4];
      meta_list[0] = new NameValuePair("width", "800");
      meta_list[1] = new NameValuePair("heigth", "600");
      meta_list[2] = new NameValuePair("bgcolor", "#FFFFFF");
      meta_list[3] = new NameValuePair("author", "Mike");

      file_buff = "this is a test".getBytes(ClientGlobal.charset);
      System.out.println("file length: " + file_buff.length);

      group_name = null;
      StorageServer[] storageServers = tracker.getStoreStorages(trackerServer, group_name);
      if (storageServers == null) {
        System.err.println("get store storage servers fail, error code: " + tracker.getErrorCode());
      } else {
        System.err.println("store storage servers count: " + storageServers.length);
        for (int k = 0; k < storageServers.length; k++) {
          System.err.println((k + 1) + ". " + storageServers[k].getInetSocketAddress().getAddress().getHostAddress() + ":" + storageServers[k].getInetSocketAddress().getPort());
        }
        System.err.println("");
      }

      startTime = System.currentTimeMillis();
      appender_file_id = client.uploadAppenderFile1(file_buff, "txt", meta_list);
      System.out.println("uploadAppenderFile1 time used: " + (System.currentTimeMillis() - startTime) + " ms");

  		/*
  		group_name = "";
  		appender_file_id = client.uploadAppenderFile1(group_name, file_buff, "txt", meta_list);
  		*/
      if (appender_file_id == null) {
        System.err.println("upload file fail, error code: " + client.getErrorCode());
        return;
      } else {
        System.err.println(client.getFileInfo1(appender_file_id));

        servers = tracker.getFetchStorages1(trackerServer, appender_file_id);
        if (servers == null) {
          System.err.println("get storage servers fail, error code: " + tracker.getErrorCode());
        } else {
          System.err.println("storage servers count: " + servers.length);
          for (int k = 0; k < servers.length; k++) {
            System.err.println((k + 1) + ". " + servers[k].getIpAddr() + ":" + servers[k].getPort());
          }
          System.err.println("");
        }

        meta_list = new NameValuePair[4];
        meta_list[0] = new NameValuePair("width", "1024");
        meta_list[1] = new NameValuePair("heigth", "768");
        meta_list[2] = new NameValuePair("bgcolor", "#000000");
        meta_list[3] = new NameValuePair("title", "Untitle");

        startTime = System.currentTimeMillis();
        errno = client.setMetadata1(appender_file_id, meta_list, ProtoCommon.STORAGE_SET_METADATA_FLAG_MERGE);
        System.out.println("setMetadata time used: " + (System.currentTimeMillis() - startTime) + " ms");
        if (errno == 0) {
          System.err.println("setMetadata success");
        } else {
          System.err.println("setMetadata fail, error no: " + errno);
        }

        meta_list = client.getMetadata1(appender_file_id);
        if (meta_list != null) {
          for (int i = 0; i < meta_list.length; i++) {
            System.out.println(meta_list[i].getName() + " " + meta_list[i].getValue());
          }
        }

        startTime = System.currentTimeMillis();
        file_buff = client.downloadFile1(appender_file_id);
        System.out.println("downloadFile time used: " + (System.currentTimeMillis() - startTime) + " ms");

        if (file_buff != null) {
          System.out.println("file length:" + file_buff.length);
          System.out.println((new String(file_buff)));
        }

        file_buff = "this is a slave buff".getBytes(ClientGlobal.charset);
        file_ext_name = "txt";
        startTime = System.currentTimeMillis();
        errno = client.appendFile1(appender_file_id, file_buff);
        System.out.println("appendFile time used: " + (System.currentTimeMillis() - startTime) + " ms");
        if (errno == 0) {
          System.err.println(client.getFileInfo1(appender_file_id));
        } else {
          System.err.println("append file fail, error no: " + errno);
        }

        startTime = System.currentTimeMillis();
        errno = client.deleteFile1(appender_file_id);
        System.out.println("deleteFile time used: " + (System.currentTimeMillis() - startTime) + " ms");
        if (errno == 0) {
          System.err.println("Delete file success");
        } else {
          System.err.println("Delete file fail, error no: " + errno);
        }
      }

      appender_file_id = client.uploadAppenderFile1(local_filename, null, meta_list);
      if (appender_file_id != null) {
        int ts;
        String token;
        String file_url;
        InetSocketAddress inetSockAddr;

        inetSockAddr = trackerServer.getInetSocketAddress();
        file_url = "http://" + inetSockAddr.getAddress().getHostAddress();
        if (ClientGlobal.trackerHttpPort != 80) {
          file_url += ":" + ClientGlobal.trackerHttpPort;
        }
        file_url += "/" + appender_file_id;
        if (ClientGlobal.antiStealToken) {
          ts = (int) (System.currentTimeMillis() / 1000);
          token = ProtoCommon.getToken(appender_file_id, ts, ClientGlobal.secretKey);
          file_url += "?token=" + token + "&ts=" + ts;
        }

        System.err.println(client.getFileInfo1(appender_file_id));
        System.err.println("file url: " + file_url);

        errno = client.downloadFile1(appender_file_id, 0, 0, "c:\\" + appender_file_id.replaceAll("/", "_"));
        if (errno == 0) {
          System.err.println("Download file success");
        } else {
          System.err.println("Download file fail, error no: " + errno);
        }

        errno = client.downloadFile1(appender_file_id, 0, 0, new DownloadFileWriter("c:\\" + appender_file_id.replaceAll("/", "-")));
        if (errno == 0) {
          System.err.println("Download file success");
        } else {
          System.err.println("Download file fail, error no: " + errno);
        }

        file_ext_name = null;
        startTime = System.currentTimeMillis();
        errno = client.appendFile1(appender_file_id, local_filename);
        System.out.println("appendFile time used: " + (System.currentTimeMillis() - startTime) + " ms");
        if (errno == 0) {
          System.err.println(client.getFileInfo1(appender_file_id));
        } else {
          System.err.println("append file fail, error no: " + errno);
        }
      }

      File f;
      f = new File(local_filename);
      int nPos = local_filename.lastIndexOf('.');
      if (nPos > 0 && local_filename.length() - nPos <= ProtoCommon.FDFS_FILE_EXT_NAME_MAX_LEN + 1) {
        file_ext_name = local_filename.substring(nPos + 1);
      } else {
        file_ext_name = null;
      }

      appender_file_id = client.uploadAppenderFile1(null, f.length(),
                                                    new UploadLocalFileSender(local_filename), file_ext_name, meta_list);
      if (appender_file_id != null) {
        System.out.println(client.getFileInfo1(appender_file_id));

        startTime = System.currentTimeMillis();
        errno = client.appendFile1(appender_file_id, f.length(), new UploadLocalFileSender(local_filename));
        System.out.println("appendFile time used: " + (System.currentTimeMillis() - startTime) + " ms");
        if (errno == 0) {
          System.err.println(client.getFileInfo1(appender_file_id));
        } else {
          System.err.println("append file fail, error no: " + errno);
        }

        startTime = System.currentTimeMillis();
        errno = client.modifyFile1(appender_file_id, 0, f.length(), new UploadLocalFileSender(local_filename));
        System.out.println("modifyFile time used: " + (System.currentTimeMillis() - startTime) + " ms");
        if (errno == 0) {
          System.err.println(client.getFileInfo1(appender_file_id));
        } else {
          System.err.println("modify file fail, error no: " + errno);
        }

        startTime = System.currentTimeMillis();
        errno = client.truncateFile1(appender_file_id, 0);
        System.out.println("truncateFile time used: " + (System.currentTimeMillis() - startTime) + " ms");
        if (errno == 0) {
          System.err.println(client.getFileInfo1(appender_file_id));
        } else {
          System.err.println("truncate file fail, error no: " + errno);
        }
      } else {
        System.err.println("Upload file fail, error no: " + errno);
      }

      storageServer = tracker.getFetchStorage1(trackerServer, appender_file_id);
      if (storageServer == null) {
        System.out.println("getFetchStorage fail, errno code: " + tracker.getErrorCode());
        return;
      }
  		/* for test only */
      System.out.println("active test to storage server: " + ProtoCommon.activeTest(storageServer.getSocket()));

      storageServer.close();

  		/* for test only */
      System.out.println("active test to tracker server: " + ProtoCommon.activeTest(trackerServer.getSocket()));

      trackerServer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
