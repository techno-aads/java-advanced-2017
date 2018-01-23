package ru.ifmo.ctddev.solutions.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Client class, which read console and send data to server
 */

public class Client {
    private final Bank bank;

    /**
     * Constructor
     *
     * @param bank - reference to Bank, send to
     */
    public Client(Bank bank) {
        this.bank = bank;
    }

    /**
     * Main method - read data from args and send data to bank
     *
     * @param args
     */
    public static void main(String[] args) {
        Transaction transaction;
        try {
            transaction = Transaction.parseFromArgs(args);
        } catch (Exception ex) {
            System.err.println("Error parsing args");
            return;
        }
        try {
            Bank bank;
            try {
                bank = (Bank) Naming.lookup(Server.BANK_URL);
            } catch (NotBoundException e) {
                System.err.println("Bank is not bound");
                return;
            } catch (MalformedURLException e) {
                System.err.println("Malformed url " + Server.BANK_URL);
                return;
            }
            try {
                new Client(bank).runTransaction(transaction);
            } catch (TransactionException e) {
                System.err.println(e.getMessage());
            }
        } catch (RemoteException e) {
            System.err.println("Remote exception occurred: " + e.getMessage());
        }
    }

    private void runTransaction(Transaction transaction) throws RemoteException, TransactionException {
        RemotePerson person = (RemotePerson) bank.getVerifyOrCreateRemotePerson(new LocalPersonImpl(transaction.passportId, transaction.name, transaction.surname));
        if (person == null) {
            throw new TransactionException("Authentication error occurred");
        }
        long newBalance = person.changeAmount(transaction.accountId, transaction.difference);
        System.out.println(transaction + " result: new balance " + newBalance);
    }

    private static class TransactionException extends Exception {
        public TransactionException(String message) {
            super(message);
        }
    }

    private static class Transaction {
        public final String name, surname;
        public final String passportId;
        public final String accountId;
        public final long difference;

        public Transaction(String name, String surname, String passportId, String accountId, long difference) {
            this.name = name;
            this.surname = surname;
            this.passportId = passportId;
            this.accountId = accountId;
            this.difference = difference;
        }

        public static Transaction parseFromArgs(String[] args) {
            String name = args[0];
            String surname = args[1];
            String passportId = args[2];
            String accountId = args[3];
            int difference = Integer.parseInt(args[4]);
            return new Transaction(name, surname, passportId, accountId, difference);
        }

        @Override
        public String toString() {
            return "Transaction{" +
                    "name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    ", passportId=" + passportId +
                    ", accountId=" + accountId +
                    ", difference=" + difference +
                    '}';
        }
    }
}