package ru.ifmo.ctddev.solutions.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {

    Account createAccount(String id) throws RemoteException;

    Account getAccount(String id) throws RemoteException;
}
