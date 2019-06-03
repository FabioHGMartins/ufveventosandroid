package com.labd2m.vma.ufveventos.util;


import android.os.AsyncTask;
import android.util.Log;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.TranslationsListResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GoogleTranslate extends AsyncTask< Object, Void, List<String> > {

    /*
     * Your Google API Key here
     */

    private final String API_KEY = "AIzaSyBhFlS2IC4yugpI67ggJAcFQ8dn99-dkmI"; // talvez seja necessário mudar depois

    /*
     * Performing the translation in background process
     */

    @Override
    protected List<String> doInBackground(Object... params) {
        final List<String> textToTranslate = (List<String>) params[0]; //Texto

        final String SOURCE_LANGUAGE = (String) params[1]; //Idioma original

        final String TARGET_LANGUAGE = (String) params[2]; // Idioma desejado

        try {
            NetHttpTransport netHttpTransport 	= new NetHttpTransport();

            JacksonFactory jacksonFactory 		= new JacksonFactory();

            Translate translate = new Translate.Builder(netHttpTransport, jacksonFactory, null).build();

            Translate.Translations.List listToTranslate = translate.new Translations().list(
                    textToTranslate, TARGET_LANGUAGE).setKey(API_KEY);


            listToTranslate.setSource(SOURCE_LANGUAGE); // Definir idioma original

            /*
             * Executing the translation and saving the response in the response object
             */

            TranslationsListResponse response = listToTranslate.execute(); // Executar tradução

            /*
             * A resposta está no formato: {"translatedText":"blabla"}
             * Necessitamos apenas da informação contida no segundo par de aspas
             * para isso, utiliza-se a função getTranslatedText
             */

            List<String> retorno = new ArrayList<>();
            int contadorDeCaracteresATraduzir = 0;
            int contadorDeCaracteresTraduzidos = 0;
            for(int i = 0; i < response.getTranslations().size(); i++) {
                contadorDeCaracteresATraduzir += textToTranslate.get(i).length();
                contadorDeCaracteresTraduzidos += response.getTranslations().get(i).getTranslatedText().length();
                retorno.add(response.getTranslations().get(i).getTranslatedText());
            }
            Log.i("TRANSLATE", "Total de " + contadorDeCaracteresATraduzir + " caracteres foram traduzidos");
            Log.i("TRANSLATE", "Total de " + contadorDeCaracteresTraduzidos + " caracteres são tradução");
            return retorno;
        } catch (Exception e){

            Log.e("Google Response ", e.getMessage());

            /*
             * I would return empty string if there is an error
             * to let the method which invoked the translating method know that there is an error
             * and subsequently it deals with it
             */

            return null;
        }
    }

}
