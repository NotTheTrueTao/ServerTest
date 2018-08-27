package testServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;


public class SocketServerMultiThread {

    static {
        BasicConfigurator.configure();
    }

    private static final Log LOGGER = LogFactory.getLog(SocketServerMultiThread.class);
    
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(8000, 10 , InetAddress.getByName("127.0.0.1"));

        try {
            while(true) {
                //这里JAVA通过JNI请求操作系统，并一直等待操作系统返回结果（或者出错）
                Socket socket = serverSocket.accept();
                SocketServerThread sockerServerThread = new SocketServerThread(socket);
                new Thread(sockerServerThread).start();
            }
        } catch(Exception e) {
            SocketServerMultiThread.LOGGER.error(e.getMessage(), e);
        } finally {
            if(serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}


class SocketServerThread implements Runnable{
    private static final Log LOGGER = LogFactory.getLog(SocketServerThread.class);
    private Socket socket;
    public SocketServerThread (Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            Integer sourcePort = socket.getPort();
            int maxLen = 1024;
            byte[] contextBytes = new byte[maxLen];
            int realLen = in.read(contextBytes, 0, maxLen);
            String message = new String(contextBytes, 0, realLen);
            LOGGER.info("服务器收到来自于端口：" + sourcePort + "的信息：" + message);
            out.write("回发响应信息！".getBytes());
        } catch(Exception e) {
            SocketServerThread.LOGGER.error(e.getMessage(), e);
        } finally {
            socket.close();
        }
    }
}
