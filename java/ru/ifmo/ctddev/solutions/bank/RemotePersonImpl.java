package ru.ifmo.ctddev.solutions.bank;


import java.rmi.Remote;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RemotePerson implementation
 */
public class RemotePersonImpl implements Remote, RemotePerson {

    protected final ConcurrentMap<String, AtomicLong> amounts;
    protected final String passportId;
    protected final String name;
    protected final String surname;

    /**
     * Constructor from another person
     *
     * @param person
     */
    public RemotePersonImpl(LocalPerson person) {
        amounts = new ConcurrentHashMap<>();
        passportId = person.getPassportId();
        name = person.getName();
        surname = person.getSurname();
    }

    /***
     * change accounts amount
     * @param accountId
     * @param difference
     * @return
     */
    @Override
    public long changeAmount(String accountId, long difference) {
        amounts.putIfAbsent(accountId, new AtomicLong());
        long res = amounts.get(accountId).addAndGet(difference);
        logChangeAmount(accountId, difference, res);
        return res;
    }

    /***
     * print change log to console
     * @param accountId
     * @param difference
     * @param newBalance
     */
    private void logChangeAmount(String accountId, long difference, long newBalance) {
        System.err.printf("%s %s (passportId: %s) accountId=%s was changed by %d. New balance: %d\n", name, surname, passportId, accountId, difference, newBalance);
    }

    /***
     * create local person
     * @return
     */
    @Override
    public LocalPerson createLocal() {
        return new LocalPersonImpl(passportId, name, surname);
    }

    /***
     * check if persons data match to existed account
     * @param person
     * @return
     */
    @Override
    public boolean verify(LocalPerson person) {
        return createLocal().verify(person);
    }

    /***
     * get Second Name
     * @return
     */
    public String getSurname() {
        return surname;
    }

    /***
     * get Passport Id
     * @return
     */
    public String getPassportId() {
        return passportId;
    }

    /**
     * get first name
     *
     * @return
     */
    public String getName() {
        return name;
    }
}
