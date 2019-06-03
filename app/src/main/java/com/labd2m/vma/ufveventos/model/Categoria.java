package com.labd2m.vma.ufveventos.model;

import java.io.Serializable;

/**
 * Created by vma on 21/07/2017.
 */

public class Categoria implements Serializable {
    private int id;
    private String nome;

    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
