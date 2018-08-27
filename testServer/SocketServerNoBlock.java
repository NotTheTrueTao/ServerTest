package testServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;


public class SocketServerNoBlock {
    private static Object xWait = new Object();

    static {
        BasicConfigurator.configure();
    }

    private static final Log LOGGER = LogFactory.getLog(SocketServerNoBlock.class);
    
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(8000, 10 , InetAddress.getByName("127.0.0.1"));
        serverSocket.setSoTimeout(100);

        try {
            while(true) {
                Socket socket = null;
                // 这里不一直阻塞，使用同步非阻塞模式
                try {
                    socket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    synchronized (SocketServerNoBlock.xWait) {
                        LOGGER.info("没有接收到TCP连接，等待10ms，模拟时间x的处理时间");
                        SocketServerNoBlock.xWait.wait(10);
                    }
                    continue;
                }

                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                Integer sourcePort = socket.getPort();
                int maxLen = 2048;
                byte[] contextBytes = new byte[maxLen];
                int realLen;
                StringBuffer message = new StringBuffer();
                //下面我们收取信息（非阻塞方式，read()方式的等待超时时间）
                socket.setSoTimeout(10);

                BIORead:while(true) {
                    try {
                        while((realLen = in.read(contextBytes, 0, maxLen)) != -1) {
                            message.append(new String(contextBytes , 0 , realLen));
                            /*
                            * 我们假设读取到“over”关键字，
                            * 表示客户端的所有信息在经过若干次传送后，完成
                            * */
                            if(message.indexOf("over") != -1) {
                                break;
                            }
                        }
                    } catch(SocketTimeoutException e2) {
                        LOGGER.info("没有接收到数据报文，等待10ms，模拟事件Y的处理时间");
                        continue;
                    }
                //下面打印信息
                SocketServerNoBlock.LOGGER.info("服务器收到来自于端口：" + sourcePort + "的信息：" + message);

                //下面开始发送信息
                out.write("回发响应信息！".getBytes());

                //关闭
                out.close();
                in.close();
                socket.close();
        } catch(Exception e) {
            SocketServerNoBlock.LOGGER.error(e.getMessage(), e);
        } finally {
            if(serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}

