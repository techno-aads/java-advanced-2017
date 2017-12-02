package ru.ifmo.ctddev.solutions.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class BankImpl implements Bank {
    private final Map<String, Account> accounts = new HashMap<String, Account>();
    private final int port;

    public BankImpl(final int port) {
        this.port = port;
    }

    public Account createAccount(String id) throws RemoteException {
        Account account = new AccountImpl(id);
        accounts.put(id, account);
        UnicastRemoteObject.exportObject(account, port);
        return account;
    }

    public Account getAccount(String id) {
        return accounts.get(id);
    }
}
