package com.zjjxgobang.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class GobangServer {

    private Socket player1;
    private Socket player2;

    public void createGame() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(3300));
            serverSocket.setSoTimeout(300000);
            while (true) {
                player1 = serverSocket.accept();
                player2 = serverSocket.accept();

                if (player1.isConnected() && player2.isConnected()){
                    String color1;
                    String color2;
                    if (Math.random() < 0.5) {
                        color1 = "BL";
                        color2 = "BU";
                    }else{
                        color1 = "BU";
                        color2 = "BL";
                    }
                    PlayConnectTask playConnectTask1 = new PlayConnectTask(player1,color1);
                    PlayConnectTask playConnectTask2 = new PlayConnectTask(player2,color2);
                    Thread connectThread1 = new Thread(playConnectTask1);
                    connectThread1.start();
                    Thread connectThread2 = new Thread(playConnectTask2);
                    connectThread2.start();

                    PlayGameTask playGameTask1 = new PlayGameTask(player1, player2);
                    PlayGameTask playGameTask2 = new PlayGameTask(player2, player1);
                    Thread playGameThread1 = new Thread(playGameTask1);
                    Thread playGameThread2 = new Thread(playGameTask2);
                    playGameThread1.start();
                    playGameThread2.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PlayConnectTask implements Runnable{
        private Socket socket;
        private String color;

        public PlayConnectTask(Socket socket, String color) {
            this.socket = socket;
            this.color = color;
        }

        @Override
        public void run() {
            try {
                OutputStreamWriter write = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                write.write("ok\tcolor:"+color+"\r\n");
                write.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class PlayGameTask implements Runnable{

        private Socket thisPlayerSocket;
        private Socket otPlayerSocket;

        public PlayGameTask(Socket thisPlayerSocket, Socket otPlayerSocket) {
            this.thisPlayerSocket = thisPlayerSocket;
            this.otPlayerSocket = otPlayerSocket;
        }

        @Override
        public void run() {
            try {
                InputStreamReader reader = new InputStreamReader(thisPlayerSocket.getInputStream(),"UTF-8");
                OutputStreamWriter otherWriter = new OutputStreamWriter(otPlayerSocket.getOutputStream(), "UTF-8");

                int len;
                char[] line = new char[96];
                while((len=reader.read(line))!=-1){
                    String msg = String.valueOf(line, 0, len);
                    otherWriter.write(msg);
                    otherWriter.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}