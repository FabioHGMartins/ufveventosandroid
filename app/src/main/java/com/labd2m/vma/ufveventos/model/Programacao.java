package com.labd2m.vma.ufveventos.model;

import java.io.Serializable;

/**
 * Created by vma on 27/04/2018.
 */

public class Programacao implements Serializable {
    private int idprog;
    private String horainicioprog,horafimprog,datainicioprog,datafimprog,descricaoprog;

    public Programacao(int idprog, String horainicioprog, String horafimprog, String datainicioprog, String datafimprog, String descricaoprog) {
        this.idprog = idprog;
        this.horainicioprog = horainicioprog;
        this.horafimprog = horafimprog;
        this.datainicioprog = datainicioprog;
        this.datafimprog = datafimprog;
        this.descricaoprog = descricaoprog;
    }

    public int getIdprog() {
        return idprog;
    }

    public void setIdprog(int idprog) {
        this.idprog = idprog;
    }

    public String getHorainicioprog() {
        return horainicioprog;
    }

    public void setHorainicioprog(String horainicioprog) {
        this.horainicioprog = horainicioprog;
    }

    public String getHorafimprog() {
        return horafimprog;
    }

    public void setHorafimprog(String horafimprog) {
        this.horafimprog = horafimprog;
    }

    public String getDatainicioprog() {
        return datainicioprog;
    }

    public void setDatainicioprog(String datainicioprog) {
        this.datainicioprog = datainicioprog;
    }

    public String getDatafimprog() {
        return datafimprog;
    }

    public void setDatafimprog(String datafimprog) {
        this.datafimprog = datafimprog;
    }

    public String getDescricaoprog() {
        return descricaoprog;
    }

    public void setDescricaoprog(String descricaoprog) {
        this.descricaoprog = descricaoprog;
    }
}
