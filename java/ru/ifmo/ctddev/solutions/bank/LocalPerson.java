package ru.ifmo.ctddev.solutions.bank;

import java.io.Serializable;

/**
 * Serializable person, which Bank can return
 */

public interface LocalPerson extends Serializable {
    String getPassportId();

    String getName();

    String getSurname();

    boolean verify(LocalPerson person);
}

