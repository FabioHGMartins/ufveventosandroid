package com.labd2m.vma.ufveventos.util;

public class FormatStrings {
    public String cortaTituloEvento(String titulo){
        if (titulo.length() >= 48) {
            titulo = titulo.substring(0, 44);
            titulo = titulo.concat("...");
        }

        return titulo;
    }


}
