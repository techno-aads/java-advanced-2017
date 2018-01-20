package ru.ifmo.ctddev.solutions.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Person interface to create Stub class from it
 */

public interface RemotePerson extends Remote {
    String getName() throws RemoteException;

    long changeAmount(String accountId, long difference) throws RemoteException;

    LocalPerson createLocal() throws RemoteException;

    boolean verify(LocalPerson person) throws RemoteException;
}
