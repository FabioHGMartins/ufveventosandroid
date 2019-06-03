package com.labd2m.vma.ufveventos.util;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by vma on 24/01/2018.
 */

public class Seguranca {
    public String duploMd5(String senha){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            BigInteger hash = new BigInteger(1, md.digest(senha.getBytes()));
            senha = hash.toString(16);
            hash = new BigInteger(1, md.digest(senha.getBytes()));
            senha = hash.toString(16);
        }catch(Exception e){}

        return senha;
    }
}