package ru.ifmo.ctddev.solutions.bank;

import java.util.Objects;

/***
 * Serializible person implementation
 */
public class LocalPersonImpl implements LocalPerson {
    protected final String passportId;
    protected final String name;
    protected final String surname;

    /**
     * create local person
     *
     * @param passportId
     * @param name
     * @param surname
     */
    public LocalPersonImpl(String passportId, String name, String surname) {
        this.passportId = passportId;
        this.name = name;
        this.surname = surname;
    }

    /***
     * get second name
     * @return
     */
    public String getSurname() {
        return surname;
    }

    @Override
    public boolean verify(LocalPerson person) {
        return person.equals(this);
    }

    /***
     * get passport id
     * @return
     */
    public String getPassportId() {
        return passportId;
    }

    /***
     * get first name
     * @return
     */
    public String getName() {
        return name;
    }

    /***
     * check if persons are equal
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LocalPersonImpl)) {
            return false;
        }
        LocalPersonImpl that = (LocalPersonImpl) o;
        if (!Objects.equals(passportId, that.passportId)) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return !(surname != null ? !surname.equals(that.surname) : that.surname != null);

    }
}
