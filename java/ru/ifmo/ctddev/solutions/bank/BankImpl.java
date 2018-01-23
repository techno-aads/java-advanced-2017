package ru.ifmo.ctddev.solutions.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/***
 * Bank demon implementation
 */
public class BankImpl implements Bank {
    private final ConcurrentMap<String, RemotePerson> clients;

    /**
     * default client
     */
    public BankImpl() {
        clients = new ConcurrentHashMap<>();
    }

    /***
     * return a LocalPerson if it has an account or null instead
     * @param person
     * @return
     * @throws RemoteException
     */
    @Override
    public LocalPerson getVerifyLocalPerson(LocalPerson person) throws RemoteException {
        LocalPerson realPerson = getLocalPerson(person.getPassportId());
        if (realPerson != null && realPerson.verify(person)) {
            return realPerson;
        }
        return null;
    }

    /***
     * return a RemotePerson if it has an account or null instead
     * @param person
     * @return
     * @throws RemoteException
     */
    @Override
    public RemotePerson getVerifyRemotePerson(LocalPerson person) throws RemoteException {
        RemotePerson realPerson = getRemotePerson(person.getPassportId());
        if (realPerson != null && realPerson.verify(person)) {
            return realPerson;
        }
        return null;
    }

    /**
     * return a LocalPerson if ti exists or null instead
     *
     * @param passportId
     * @return
     * @throws RemoteException
     */
    @Override
    public LocalPerson getLocalPerson(String passportId) throws RemoteException {
        RemotePerson remotePerson = getRemotePerson(passportId);
        return remotePerson == null ? null : remotePerson.createLocal();
    }

    /**
     * return a RemotePerson
     *
     * @param passportId
     * @return
     * @throws RemoteException
     */
    @Override
    public RemotePerson getRemotePerson(String passportId) {
        return clients.get(passportId);
    }

    /***
     * create persons account
     * @param person
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean createPerson(LocalPerson person) throws RemoteException {
        RemotePersonImpl remotePerson = new RemotePersonImpl(person);
        RemotePerson prevPerson = clients.putIfAbsent(person.getPassportId(), remotePerson);
        if (prevPerson == null) {
            UnicastRemoteObject.exportObject(remotePerson, Server.PORT);
        }
        return prevPerson == null;
    }

    /***
     * return LocalPerson if exist or create new instead
     * @param person
     * @return
     * @throws RemoteException
     */
    @Override
    public LocalPerson getVerifyOrCreateLocalPerson(LocalPerson person) throws RemoteException {
        createPerson(person);
        return getVerifyLocalPerson(person);
    }

    /***
     * return RemotePerson if exist or create new instead
     * @param person
     * @return
     * @throws RemoteException
     */
    @Override
    public RemotePerson getVerifyOrCreateRemotePerson(LocalPerson person) throws RemoteException {
        createPerson(person);
        return getVerifyRemotePerson(person);
    }

    /***
     * return LocalPerson if exist or create new instead
     * @param person
     * @return
     * @throws RemoteException
     */
    @Override
    public LocalPerson getOrCreateLocalPerson(LocalPerson person) throws RemoteException {
        createPerson(person);
        return getLocalPerson(person.getPassportId());
    }

    /***
     * return Remote person if exist or create new instead
     * @param person
     * @return
     * @throws RemoteException
     */
    @Override
    public RemotePerson getOrCreateRemotePerson(LocalPerson person) throws RemoteException {
        createPerson(person);
        return getRemotePerson(person.getPassportId());
    }
}