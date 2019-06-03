package com.labd2m.vma.ufveventos.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.labd2m.vma.ufveventos.controller.Api;
import com.labd2m.vma.ufveventos.model.Evento;

import org.json.JSONObject;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vma on 15/01/2018.
 */

public class Agenda {
    public void deleteEventNotification(Evento evento,Context context,ContentResolver cr){
        //Recupera id do evento
        SharedPreferences sharedPref = context.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0agenda",
                Context.MODE_PRIVATE);
        long eventID = sharedPref.getLong(""+evento.getId(),-1);
        if (eventID != -1) {
            Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
            int rows = cr.delete(deleteUri, null, null);

            //Envia ao servidor
            sharedPref = context.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0", Context.MODE_PRIVATE);
            String idUsuario = sharedPref.getString("id", "falso");
            sharedPref = context.getSharedPreferences("UFVEVENTOS" + idUsuario, Context.MODE_PRIVATE);
            String token = sharedPref.getString("firebasetoken", "falso");
            RetrofitAPI retrofit = new RetrofitAPI();
            final Api api = retrofit.retrofit().create(Api.class);

            //Cria json object
            JSONObject json = new JSONObject();
            try {
                json.put("token",token);
                json.put("idUsuario",idUsuario);
                json.put("agenda","" + eventID);
            }catch(Exception e){Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();};

            Observable<Void> observable = api.deleteAgenda(json);
            observable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Void>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("ErroDeletaAgendaServ:", e.getMessage());
                        }

                        @Override
                        public void onNext(Void response) {
                            Log.i("DeletaAgendaServer", "Delete");
                        }
                    });

            //Remove chave do shared preferences
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(""+evento.getId());
            editor.commit();
        }
    }

    public void updateEventNotification(Evento evento,Context context,ContentResolver cr){
        String[] projection =
                new String[]{
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.NAME,
                        CalendarContract.Calendars.ACCOUNT_NAME,
                        CalendarContract.Calendars.ACCOUNT_TYPE};
        String calendar_id = "";
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED){
            Cursor calCursor =
                    context.getContentResolver().
                            query(CalendarContract.Calendars.CONTENT_URI,
                                    projection,
                                    CalendarContract.Calendars.VISIBLE + " = 1",
                                    null,
                                    CalendarContract.Calendars._ID + " ASC");
            calCursor.moveToFirst();
            calendar_id = calCursor.getString(0);
            Pattern pattern = Pattern.compile(".+@.+");
            try {
                do {
                    Matcher matcher = pattern.matcher(calCursor.getString(1));
                    if (matcher.matches())
                        calendar_id = calCursor.getString(0);
                } while (calCursor.moveToNext());
            }catch(Exception e){Log.i("Erro de match agenda",e.getMessage());}
        }

        int diaInicio = Integer.parseInt(evento.getDataInicio().substring(0,2));
        int mesInicio = Integer.parseInt(evento.getDataInicio().substring(3,5));
        int anoInicio = Integer.parseInt(evento.getDataInicio().substring(6,10));
        int horaInicio = Integer.parseInt(evento.getHoraInicio().substring(0,2));
        int minutoInicio = Integer.parseInt(evento.getHoraInicio().substring(3,5));
        java.util.Calendar beginTime = java.util.Calendar.getInstance();
        beginTime.set(anoInicio,mesInicio-1,diaInicio,horaInicio,minutoInicio);
        int diaFim = Integer.parseInt(evento.getDataFim().substring(0,2));
        int mesFim = Integer.parseInt(evento.getDataFim().substring(3,5));
        int anoFim = Integer.parseInt(evento.getDataFim().substring(6,10));
        int horaFim = Integer.parseInt(evento.getHoraFim().substring(0,2));
        int minutoFim = Integer.parseInt(evento.getHoraFim().substring(3,5));
        java.util.Calendar endTime = java.util.Calendar.getInstance();
        endTime.set(anoFim,mesFim-1,diaFim,horaFim,minutoFim);
        String local = evento.getLocais().get(0).getDescricao()+","+
                evento.getLocais().get(0).getLatitude()+","+evento.getLocais().get(0).getLongitude();
        //Recupera id do evento
        SharedPreferences sharedPref = context.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0agenda",
                Context.MODE_PRIVATE);
        long eventID = sharedPref.getLong(""+evento.getId(),-1);
        if (eventID != -1) {
            ContentValues values = new ContentValues();
            TimeZone timeZone = TimeZone.getDefault();
            Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);

            values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
            values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
            values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
            values.put(CalendarContract.Events.TITLE, evento.getDenominacao());
            values.put(CalendarContract.Events.DESCRIPTION, evento.getDescricao_evento());
            values.put(CalendarContract.Events.CALENDAR_ID, calendar_id);
            values.put(CalendarContract.Events.EVENT_LOCATION, local);
            values.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, "1");
            values.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, "1");

            int rows = cr.update(updateUri, values, null, null);

            //Remove chave do shared preferences
            /*
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(""+evento.getId());
            editor.commit();*/
        }
    }

    public void addEvent(Evento evento, Context context, ContentResolver cr, Activity activity) {
        //Requisita permissão para escrita no calendar
        Permission permission = new Permission();
        permission.requestPermissionCalendar(activity, context);

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection =
                new String[]{
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.NAME,
                        CalendarContract.Calendars.ACCOUNT_NAME,
                        CalendarContract.Calendars.ACCOUNT_TYPE};
        String calendar_id = "";
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED){
            Cursor calCursor =
                    context.getContentResolver().
                            query(CalendarContract.Calendars.CONTENT_URI,
                                    projection,
                                    CalendarContract.Calendars.VISIBLE + " = 1",
                                    null,
                                    CalendarContract.Calendars._ID + " ASC");
            try {
                calCursor.moveToFirst();
                int index = calCursor.getColumnIndex(CalendarContract.Calendars._ID);
                calendar_id = calCursor.getString(index);
                Pattern pattern = Pattern.compile(".+@.+");
            do {
                Matcher matcher = pattern.matcher(calCursor.getString(1));
                if (matcher.matches())
                    calendar_id = calCursor.getString(0);
            }while(calCursor.moveToNext());
            }catch (Exception e){Log.i("AGENDA", e.getMessage());}
        }
        int diaInicio = Integer.parseInt(evento.getDataInicio().substring(8,10));
        int mesInicio = Integer.parseInt(evento.getDataInicio().substring(5,7));
        int anoInicio = Integer.parseInt(evento.getDataInicio().substring(0,4));

        int horaInicio = Integer.parseInt(evento.getHoraInicio().substring(0,2));
        int minutoInicio = Integer.parseInt(evento.getHoraInicio().substring(3,5));

        java.util.Calendar beginTime = java.util.Calendar.getInstance();
        beginTime.set(anoInicio,mesInicio-1,diaInicio,horaInicio,minutoInicio);

        int diaFim = Integer.parseInt(evento.getDataFim().substring(8,10));
        int mesFim = Integer.parseInt(evento.getDataFim().substring(5,7));
        int anoFim = Integer.parseInt(evento.getDataFim().substring(0,4));

        int horaFim = Integer.parseInt(evento.getHoraFim().substring(0,2));
        int minutoFim = Integer.parseInt(evento.getHoraFim().substring(3,5));

        java.util.Calendar endTime = java.util.Calendar.getInstance();
        endTime.set(anoFim,mesFim-1,diaFim,horaFim,minutoFim);
        ContentValues values = new ContentValues();
        TimeZone timeZone = TimeZone.getDefault();
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.CALENDAR_ID, calendar_id);
        values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE, evento.getDenominacao());
        values.put(CalendarContract.Events.DESCRIPTION, evento.getDescricao_evento());
        String local = evento.getLocais().get(0).getDescricao()+","+
                evento.getLocais().get(0).getLatitude()+","+evento.getLocais().get(0).getLongitude();
        values.put(CalendarContract.Events.EVENT_LOCATION, local);
        values.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, "1");
        values.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, "1");
        Uri uri = null;
        try {
            uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        }catch (SecurityException e){Log.i("ERRO ALARM",e.getMessage());}

        long eventID = Long.parseLong(uri.getLastPathSegment());
        try{
            values = new ContentValues();
            values.put(CalendarContract.Reminders.MINUTES, TimeUnit.MINUTES.convert(1, TimeUnit.HOURS));
            values.put(CalendarContract.Reminders.EVENT_ID, eventID);
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
        }catch (SecurityException e){Log.i("ERRO ALARM",e.getMessage());}

        //Grava id do evento
        SharedPreferences sharedPref = context.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0agenda",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(""+evento.getId(),eventID);
        editor.commit();

        sharedPref = context.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0", Context.MODE_PRIVATE);
        String idUsuario = sharedPref.getString("id", "falso");

        sharedPref = context.getSharedPreferences("UFVEVENTOS"+idUsuario, Context.MODE_PRIVATE);
        String token = sharedPref.getString("firebasetoken", "falso");
        //Cria json object
        JSONObject json = new JSONObject();
        try {
            json.put("token",""+token);
            json.put("agenda",""+eventID);
            json.put("evento",""+evento.getId());
            json.put("usuario",idUsuario);
        }catch(Exception e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();}

        RetrofitAPI retrofit = new RetrofitAPI();
        final Api api = retrofit.retrofit().create(Api.class);
        Observable<Void> observable = api.addAgenda(json);
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ErroAddAgendaServer:",e.getMessage());
                    }

                    @Override
                    public void onNext(Void response) {
                        Log.i("AddAgendaServer","Add");
                    }
                });
    }
    public void addEventNotification(Evento evento,Context context, ContentResolver cr) {
        String[] projection =
                new String[]{
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.NAME,
                        CalendarContract.Calendars.ACCOUNT_NAME,
                        CalendarContract.Calendars.ACCOUNT_TYPE};
        String calendar_id = "";
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED){
            Cursor calCursor =
                    context.getContentResolver().
                            query(CalendarContract.Calendars.CONTENT_URI,
                                    projection,
                                    CalendarContract.Calendars.VISIBLE + " = 1",
                                    null,
                                    CalendarContract.Calendars._ID + " ASC");
            calCursor.moveToFirst();
            calendar_id = calCursor.getString(0);
            Pattern pattern = Pattern.compile(".+@.+");
            do{
                Matcher matcher = pattern.matcher(calCursor.getString(1));
                if (matcher.matches())
                    calendar_id = calCursor.getString(0);
            }while(calCursor.moveToNext());
        }

        int diaInicio = Integer.parseInt(evento.getDataInicio().substring(0,2));
        int mesInicio = Integer.parseInt(evento.getDataInicio().substring(3,5));
        int anoInicio = Integer.parseInt(evento.getDataInicio().substring(6,10));

        int horaInicio = Integer.parseInt(evento.getHoraInicio().substring(0,2));
        int minutoInicio = Integer.parseInt(evento.getHoraInicio().substring(3,5));

        java.util.Calendar beginTime = java.util.Calendar.getInstance();
        beginTime.set(anoInicio,mesInicio-1,diaInicio,horaInicio,minutoInicio);

        int diaFim = Integer.parseInt(evento.getDataFim().substring(0,2));
        int mesFim = Integer.parseInt(evento.getDataFim().substring(3,5));
        int anoFim = Integer.parseInt(evento.getDataFim().substring(6,10));

        int horaFim = Integer.parseInt(evento.getHoraFim().substring(0,2));
        int minutoFim = Integer.parseInt(evento.getHoraFim().substring(3,5));

        java.util.Calendar endTime = java.util.Calendar.getInstance();
        endTime.set(anoFim,mesFim-1,diaFim,horaFim,minutoFim);

        String local = evento.getLocais().get(0).getDescricao()+","+
                evento.getLocais().get(0).getLatitude()+","+evento.getLocais().get(0).getLongitude();

        ContentValues values = new ContentValues();
        TimeZone timeZone = TimeZone.getDefault();
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE, evento.getDenominacao());
        values.put(CalendarContract.Events.DESCRIPTION, evento.getDescricao_evento());
        values.put(CalendarContract.Events.CALENDAR_ID, calendar_id);
        values.put(CalendarContract.Events.EVENT_LOCATION, local);
        values.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, "1");
        values.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, "1");
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        Uri uri = null;
        try {
            uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        }catch (SecurityException e){}

        long eventID = Long.parseLong(uri.getLastPathSegment());

        values = new ContentValues();
        values.put(CalendarContract.Reminders.MINUTES, TimeUnit.MINUTES.convert(1, TimeUnit.HOURS));
        values.put(CalendarContract.Reminders.EVENT_ID, eventID);
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        try{
            uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
        }catch (SecurityException e){}

        //Grava id do evento
        SharedPreferences sharedPref = context.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0agenda",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(""+evento.getId(),eventID);
        editor.commit();

        sharedPref = context.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0", Context.MODE_PRIVATE);
        String idUsuario = sharedPref.getString("id", "falso");

        sharedPref = context.getSharedPreferences("UFVEVENTOS"+idUsuario, Context.MODE_PRIVATE);
        String token = sharedPref.getString("firebasetoken", "falso");

        //Cria json object
        JSONObject json = new JSONObject();
        try {
            json.put("token",""+token);
            json.put("agenda",""+eventID);
            json.put("evento",""+evento.getId());
            json.put("usuario",idUsuario);
        }catch(Exception e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();}

        RetrofitAPI retrofit = new RetrofitAPI();
        final Api api = retrofit.retrofit().create(Api.class);
        Observable<Void> observable = api.addAgenda(json);
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ErroAddAgendaServer:",e.getMessage());
                    }

                    @Override
                    public void onNext(Void response) {
                        Log.i("AddAgendaServer","Add");
                    }
                });
    }
}