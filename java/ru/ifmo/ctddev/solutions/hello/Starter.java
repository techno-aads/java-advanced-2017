package ru.ifmo.ctddev.solutions.udp;

public class Starter {

    public static void main(String... args){

        HelloUDPServer server = new HelloUDPServer();

        HelloUDPClient client = new HelloUDPClient();

        server.start(5000, 4);

        client.run("localhost", 5000, "kekes", 10, 4);

        server.close();

    }

}
