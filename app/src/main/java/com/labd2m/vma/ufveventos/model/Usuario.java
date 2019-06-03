package com.labd2m.vma.ufveventos.model;

/**
 * Created by vma on 21/07/2017.
 */

public class Usuario {
    private String id="", nome="", email="", senha="", nascimento="", sexo="", matricula="", foto="",
            googleId="";

    public Usuario(){
        id="";
        nome="";
        email="";
        senha="";
        nascimento="";
        sexo="";
        matricula="";
        foto="";
        googleId="";
    }

    public Usuario(String id, String nome, String email, String senha, String nascimento, String sexo, String matricula,
                   String googleId) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.nascimento = nascimento;
        this.sexo = sexo;
        this.matricula = matricula;
        this.googleId = googleId;
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
}
