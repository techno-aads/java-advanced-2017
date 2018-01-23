package ru.ifmo.ctddev.solutions.bank;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Server class, which store data, received from clients
 */
public class Server extends UnicastRemoteObject {
    public final static int PORT = 3456;
    public final static String BANK_URL = "rmi://localhost/bank";

    public Server() throws RemoteException {

    }

    /**
     * start Bank demon
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
        } catch (IOException e) {
            System.out.println(e);
        }
        Bank bank = new BankImpl();
        try {
            UnicastRemoteObject.exportObject(bank, PORT);
            Naming.rebind(BANK_URL, bank);
        } catch (RemoteException e) {
            System.err.println("Can't export Bank object");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.err.println("Malformed url " + BANK_URL);
        }
    }
}