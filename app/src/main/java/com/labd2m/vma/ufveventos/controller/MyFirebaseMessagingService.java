package com.labd2m.vma.ufveventos.controller;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.labd2m.vma.ufveventos.model.Evento;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.labd2m.vma.ufveventos.model.Local;
import com.labd2m.vma.ufveventos.model.Programacao;
import com.labd2m.vma.ufveventos.util.Agenda;
import com.labd2m.vma.ufveventos.util.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by vma on 07/12/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    SharedPref sharedPrefUtil = new SharedPref();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        //Verifica se a mensagem contém notificação
        if (remoteMessage.getNotification() != null || remoteMessage.getData().size() > 0){
            //Recupera dados da notificação
            String acao="",id="",denominacao="",descricao_evento="",
                    horainicio="",horafim="",datainicio="",datafim="",participantes="",
                    publico="",teminscricao="",valorinscricao="",linklocalinscricao="",mostrarparticipantes="";
            Map<String,String> dados = remoteMessage.getData();
            JSONObject dadosJson = null;
            List<Local> locais = new ArrayList<>();
            List<Programacao> programacoes = new ArrayList<>();
            try {
                dadosJson = new JSONObject(dados.get("body"));
                Log.i("DADOS NOT",""+dadosJson);
                acao = dadosJson.getString("acao");
                id = dadosJson.getString("id");
                denominacao = dadosJson.getString("denominacao");
                descricao_evento = dadosJson.getString("descricao_evento");
                horainicio = dadosJson.getString("horainicio").substring(0, 5);
                horafim = dadosJson.getString("horafim").substring(0, 5);
                datainicio = dadosJson.getString("datainicio");
                datainicio = datainicio.substring(8, 10) + "/" + datainicio.substring(5, 7) + "/" + datainicio.substring(0, 4);
                datafim = dadosJson.getString("datafim");
                datafim = datafim.substring(8, 10) + "/" + datafim.substring(5, 7) + "/" + datafim.substring(0, 4);
                participantes = dadosJson.getString("participantes");
                publico = dadosJson.getString("publico");
                teminscricao = dadosJson.getString("teminscricao");
                valorinscricao = dadosJson.getString("valorinscricao");
                linklocalinscricao = dadosJson.getString("linklocalinscricao");
                mostrarparticipantes = dadosJson.getString("mostrarparticipantes");
                JSONArray jsonAux = new JSONArray(dadosJson.getString("locais"));
                int numLocais = jsonAux.length();
                Log.i("LOCAIS RASTREIO",""+jsonAux);
                for (int i = 0; i < numLocais; i++) {
                    Local local = new Local(Integer.parseInt(jsonAux.getJSONObject(i).getString("id")),
                            jsonAux.getJSONObject(i).getString("descricao"),
                            jsonAux.getJSONObject(i).getString("lat"),
                            jsonAux.getJSONObject(i).getString("lng"));
                    locais.add(local);
                }

                jsonAux = new JSONArray(dadosJson.getString("programacoes"));
                int num = jsonAux.length();
                for (int i = 0; i < num; i++) {
                    Programacao prog = new Programacao(Integer.parseInt(jsonAux.getJSONObject(i).getString("idprog"))  ,
                            jsonAux.getJSONObject(i).getString("horainicioprog"),
                            jsonAux.getJSONObject(i).getString("horafimprog"),
                            jsonAux.getJSONObject(i).getString("datainicioprog"),
                            jsonAux.getJSONObject(i).getString("datafimprog"),
                            jsonAux.getJSONObject(i).getString("descricaoprog"));
                    programacoes.add(prog);
                }
            }catch(JSONException e){Log.e("JSON ERRO",e.getMessage());}

            Evento evento = new Evento(Integer.parseInt(id),denominacao,horainicio,horafim,datainicio,datafim,descricao_evento,
                    programacoes,Integer.parseInt(participantes),publico,null,locais,null,Integer.parseInt(teminscricao),
                    Float.parseFloat(valorinscricao),linklocalinscricao,Integer.parseInt(mostrarparticipantes));

            Agenda calendar = new Agenda();
            //Verifica se o usuário deseja que a notificação seja adicionada à agenda
            SharedPreferences sharedPref = this.getSharedPreferences(sharedPrefUtil.getKey(), Context.MODE_PRIVATE);
            String idUsuario = sharedPref.getString("id","default");

            //Se o usuário está logado
            if (!idUsuario.equals("default")) {
                Log.i("ADD EVENTO", "LOGADO");
                String agenda = sharedPref.getString("agenda", "default");
                /*Verifica se o usuário deseja gravar a notificação na agenda,
                e se ele acabou de ser adicionado ao sistema*/
                if (agenda.equals("1") && acao.equals("novo")) {
                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_CALENDAR)
                            == PackageManager.PERMISSION_GRANTED)
                        calendar.addEventNotification(evento, getBaseContext(), getContentResolver());
                }
            }

            //Verifica se o usuário deseja receber notificacoes
            String notificacoes = sharedPref.getString("notificacoes","1");
            //Chegou novo evento
            if (notificacoes.equals("1") && acao.equals("novo")) {
                /*Cancela notificação já agendada, a fim de impedir que o app
                inunde o celular de notificações de novos eventos*/
                Context ctx = getApplicationContext();
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent cancelServiceIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
                PendingIntent cancelServicePendingIntent = PendingIntent.getBroadcast(
                        ctx,
                        0, // integer constant used to identify the service
                        cancelServiceIntent,
                        0 //no FLAG needed for a service cancel
                );
                alarmManager.cancel(cancelServicePendingIntent);

                //Agenda nova notificação
                Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
                notificationIntent.addCategory("android.intent.category.DEFAULT");
                notificationIntent.putExtra("acao",acao);
                PendingIntent broadcast = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, 7200); //Envia a notificação num horário agendado
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
                //Evento cancelado
            }else if (notificacoes.equals("1") && acao.equals("cancelado")) {
                    //Agenda nova notificação
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");

                    Gson gson = new Gson();
                    String json = gson.toJson(evento);
                    notificationIntent.putExtra("evento", json);
                    notificationIntent.putExtra("acao", acao);

                    notificationIntent.addCategory("android.intent.category.DEFAULT");
                    PendingIntent broadcast = PendingIntent.getBroadcast(this, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND, 7200);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);

                    //Recupera id do evento
                    sharedPref = getApplicationContext().getSharedPreferences(sharedPrefUtil.getAgendaKey(),
                            Context.MODE_PRIVATE);
                    long eventID = sharedPref.getLong(""+evento.getId(),-1);
                    if (eventID != -1)
                        calendar.deleteEventNotification(evento,getBaseContext(),getContentResolver());
                    }else if (notificacoes.equals("1") && acao.equals("atualizado")) {
                        Log.i("NOTI","Evento atualizado");
                        //Evento atualizado
                        /*Cancela notificação já agendada, a fim de impedir que o app
                        inunde o celular de notificações de novos eventos*/

                        Context ctx = getApplicationContext();
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                        Intent cancelServiceIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
                        PendingIntent cancelServicePendingIntent = PendingIntent.getBroadcast(
                                ctx,
                                2, // integer constant used to identify the service
                                cancelServiceIntent,
                                0 //no FLAG needed for a service cancel
                        );
                        alarmManager.cancel(cancelServicePendingIntent);

                        //Agenda nova notificação
                        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
                        Gson gson = new Gson();
                        String json = gson.toJson(evento);
                        Log.i("log json",json);
                        notificationIntent.putExtra("evento", json);
                        notificationIntent.putExtra("acao", acao);

                        notificationIntent.addCategory("android.intent.category.DEFAULT");
                        PendingIntent broadcast = PendingIntent.getBroadcast(this, 2, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.SECOND, 7200); //Envia notificação imediatamente
                        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
                        //Recupera id do evento
                        sharedPref = ctx.getSharedPreferences(sharedPrefUtil.getAgendaKey(),
                                Context.MODE_PRIVATE);
                        long eventID = sharedPref.getLong(""+evento.getId(),-1);
                        if (eventID != -1)
                            calendar.updateEventNotification(evento,getBaseContext(),getContentResolver());
                    }
        }
    }

    @Override
    public void onDeletedMessages(){Log.i("NOTIFICATION DELETED","CHEGOU");}
}