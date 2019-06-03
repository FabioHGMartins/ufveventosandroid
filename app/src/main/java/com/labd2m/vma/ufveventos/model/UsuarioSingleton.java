package com.labd2m.vma.ufveventos.model;

/**
 * Created by vma on 07/09/2017.
 */

public class UsuarioSingleton {
    private static final UsuarioSingleton ourInstance = new UsuarioSingleton();

    private String id="", nome="", email="", senha="", nascimento="", sexo="", matricula="", token="", foto="",
            googleId="", agenda="", notificacoes="";

    private int numCategorias = 0;

    public static UsuarioSingleton getInstance() {
        return ourInstance;
    }

    private UsuarioSingleton() {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getNascimento() {
        return nascimento;
    }

    public void setNascimento(String nascimento) {
        this.nascimento = nascimento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public int getNumCategorias() {
        return numCategorias;
    }

    public void setNumCategorias(int numCategorias) {
        this.numCategorias = numCategorias;
    }
}
