package com.labd2m.vma.ufveventos.controller;

import com.google.gson.JsonObject;
import com.labd2m.vma.ufveventos.model.Categoria;
import com.labd2m.vma.ufveventos.model.Dispositivo;
import com.labd2m.vma.ufveventos.model.Evento;
import com.labd2m.vma.ufveventos.model.Usuario;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface Api{
    String auth = "Authorization:Basic 45dfd94be4b30d5844d2bcca2d997db0";

    @Headers("Authorization:Basic 45dfd94be4b30d5844d2bcca2d997db0")
    @GET("evento/categoria/personalizado/idusuario/{idUsuario}/indexinicial/{offset}/indexfinal/{limit}")
    Observable<List<Evento>> getEventosPorUsuario(@Path("idUsuario") String idUsuario,
                                                  @Path("offset") int offset, @Path("limit") int limit);

    //Retorna agenda do dispositivo
    @Headers(auth)
    @FormUrlEncoded
    @POST("getcalendar")
    Observable<Object> getCalendar(@Field("data") JSONObject data);

    @Headers(auth)
    @GET("evento/indexinicial/{offset}/indexfinal/{limit}")
    Observable<List<Evento>> getEventos(@Path("offset") int offset, @Path("limit") int limit);

    //Retorna categorias de evento que o usuário deseja receber notificacoes
    @Headers(auth)
    @GET("preferencias_notificacoes/{idUsuario}")
    Observable<List<Categoria>> getPreferenciasDeNotificacoes(@Path("idUsuario") String idUsuario);

    //Retorna categorias de evento preferidas do usuário
    @Headers(auth)
    @GET("preferencias_categorias/{idUsuario}")
    Observable<List<Categoria>> getPreferenciasDeCategorias(@Path("idUsuario") String idUsuario);

    //Retorna categorias de evento
    @Headers(auth)
    @GET("categoria")
    Observable<List<Categoria>> getCategorias();

    //Recupera senha
    @FormUrlEncoded
    @Headers(auth)
    @POST("email")
    Observable<Void> recuperaSenha(@Field("data") JSONObject data);

    //Autentica usuário
    @FormUrlEncoded
    @Headers(auth)
    @POST("usuario_auth")
    Observable<Usuario> authUsuario(@Field("data") JSONObject data);

    //Cria novo usuário
    @FormUrlEncoded
    @Headers(auth)
    @POST("usuario")
    Observable<Integer> setUsuario(@Field("data") JSONObject data);

    //Cria novo usuário google
    @FormUrlEncoded
    @Headers(auth)
    @POST("usuario_google")
    Observable<Integer> setUsuarioGoogle(@Field("data") JSONObject data);

    //Cadastra novo dispositivo do usuário
    @FormUrlEncoded
    @Headers(auth)
    @POST("dispositivos")
    Observable<Void> setDispositivo(@Field("data") JSONObject data);

    @FormUrlEncoded
    @Headers(auth)
    @POST("agenda_notificacoes")
    Observable<Dispositivo> getAgendaNotificacoes(@Field("data") JSONObject data);

    //Atualiza dados do cadastro do usuário
    @FormUrlEncoded
    @Headers(auth)
    @PUT("usuario/{idUsuario}")
    Observable<Void> updateUsuario(@Field("data") JSONObject data,@Path("idUsuario") String idUsuario);

    //Atualiza a opção do usuário receber notificções
    @FormUrlEncoded
    @Headers(auth)
    @PUT("notificacoes")
    Observable<Void> updateNotificacoes(@Field("data") JSONObject data);

    //Atualiza a opção do usuário adicionar eventos que chegam via notificação automaticamente à agenda
    @FormUrlEncoded
    @Headers(auth)
    @PUT("agenda")
    Observable<Void> updateAgenda(@Field("data") JSONObject data);

    //Atualiza preferências de categoria do usuário
    @FormUrlEncoded
    @Headers(auth)
    @PUT("preferencias_categorias/{idUsuario}")
    Observable<Void> updatePreferenciasCategorias(@Field("data") String data,@Path("idUsuario") String idUsuario);

    //Atualiza preferências de categoria do usuário
    @FormUrlEncoded
    @Headers(auth)
    @PUT("preferencias_notificacoes/{idUsuario}")
    Observable<Void> updatePreferenciasNotificacoes(@Field("data") String data,@Path("idUsuario") String idUsuario);

    //Adiciona a agenda do usuário
    @FormUrlEncoded
    @Headers(auth)
    @POST("calendar")
    Observable<Void> addAgenda(@Field("data") JSONObject data);

    //Adiciona a agenda do usuário
    @Headers(auth)
    @FormUrlEncoded
    @POST("deletecalendar")
    Observable<Void> deleteAgenda(@Field("data") JSONObject data);
}