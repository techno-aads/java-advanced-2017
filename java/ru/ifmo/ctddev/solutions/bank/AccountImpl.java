package ru.ifmo.ctddev.solutions.bank;

public class AccountImpl implements Account {
    private final String id;
    private int amount;

    public AccountImpl(String id) {
        this.id = id;
        amount = 0;
    }

    public String getId() {
        return id;
    }

    public void setAmount(int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }
}
