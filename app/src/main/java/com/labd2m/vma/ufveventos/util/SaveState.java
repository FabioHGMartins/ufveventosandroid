package com.labd2m.vma.ufveventos.util;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.labd2m.vma.ufveventos.model.Evento;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaveState implements Serializable {
    static SaveState instance=null;
    private static File mFolder;
    public static final String SAVESTATE_EVENTOS_PATH = "eventosTranslate.data";
    public static final String SAVESTATE_CATEGORIAS_PATH = "categoriasTranslate.data";

    public static SaveState getInstance(){
        if( instance == null )
            instance = new SaveState();
        return instance;
    }

    public static void saveData(Activity pContext, Object objetos, String nomeArquivo){
        if(mFolder == null){
            mFolder = pContext.getExternalFilesDir(null);
        }

        ObjectOutput out;
        try {
            File outFile = new File(mFolder,nomeArquivo);
            out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(objetos);
            out.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Object loadData(Activity pContext, String nomeArquivo){
        if(mFolder == null){
            mFolder = pContext.getExternalFilesDir(null);
        }
        ObjectInput in;
        Object objetos  = null;
        try {
            FileInputStream fileIn = new FileInputStream(mFolder.getPath() + File.separator + nomeArquivo);
            in = new ObjectInputStream(fileIn);

            objetos= in.readObject();
            in.close();
        }  catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.i("TRANSLATE", "Failed Save");
        }
        if(objetos == null) objetos = new ArrayList<>();
//        Log.i("TRANSLATE", objetos.size() + "");
        return objetos;
    }

    public static void clearData(Activity pContext, String nomeArquivo){
        if(mFolder == null){
            mFolder = pContext.getExternalFilesDir(null);
        }

        File file = new File(mFolder, nomeArquivo);
        file.delete();
    }

    public static List<Evento> attListaEventos(List<Evento> eventos){
        Log.i("SAVESTATE", "num salvos: " + eventos.size());
        Collator collator = Collator.getInstance();
        String hoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        Log.i("TRANSLATE", "hoje " + hoje);
        List<Evento> novoEventos = new ArrayList<>();
        for(int i = 0; i < eventos.size(); i++) {
            if(collator.compare(eventos.get(i).getDataInicio(),hoje) >= 0) {
                novoEventos.add(eventos.get(i));
            }
        }
        return novoEventos;
    }
}