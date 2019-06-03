package com.labd2m.vma.ufveventos.model;

/**
 * Created by vma on 23/01/2018.
 */

public class Dispositivo {
    private String id,token,agenda,notificacoes;

    public Dispositivo(String id, String token, String agenda, String notificacoes) {
        this.id = id;
        this.token = token;
        this.agenda = agenda;
        this.notificacoes = notificacoes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAgenda() {
        return agenda;
    }

    public void setAgenda(String agenda) {
        this.agenda = agenda;
    }

    public String getNotificacoes() {
        return notificacoes;
    }

    public void setNotificacoes(String notificacoes) {
        this.notificacoes = notificacoes;
    }
}
