package ru.ifmo.ctddev.solutions.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * interface for create Stub class from it
 */
public interface Bank extends Remote {
    LocalPerson getVerifyLocalPerson(LocalPerson person) throws RemoteException;

    Object getVerifyRemotePerson(LocalPerson person) throws RemoteException;

    LocalPerson getLocalPerson(String passportId) throws RemoteException;

    Object getRemotePerson(String passportId) throws RemoteException;

    boolean createPerson(LocalPerson person) throws RemoteException;

    LocalPerson getVerifyOrCreateLocalPerson(LocalPerson person) throws RemoteException;

    Object getVerifyOrCreateRemotePerson(LocalPerson person) throws RemoteException;

    LocalPerson getOrCreateLocalPerson(LocalPerson person) throws RemoteException;

    Object getOrCreateRemotePerson(LocalPerson person) throws RemoteException;
}
