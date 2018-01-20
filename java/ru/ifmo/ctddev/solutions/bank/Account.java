package ru.ifmo.ctddev.solutions.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Account extends Remote {

    String getId() throws RemoteException;
}