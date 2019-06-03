package com.labd2m.vma.ufveventos.controller;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.labd2m.vma.ufveventos.R;
import com.labd2m.vma.ufveventos.model.Categoria;
import com.labd2m.vma.ufveventos.model.Evento;
import com.labd2m.vma.ufveventos.model.EventosSingleton;
import com.labd2m.vma.ufveventos.model.UsuarioSingleton;
import com.labd2m.vma.ufveventos.util.GoogleTranslate;
import com.labd2m.vma.ufveventos.util.RetrofitAPI;
import com.labd2m.vma.ufveventos.util.SaveState;
import com.labd2m.vma.ufveventos.util.SharedPref;
import com.labd2m.vma.ufveventos.util.UsuarioNavigationDrawer;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class inicial extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView myRecyclerView;
    private RecyclerViewEventosTelaInicialAdapter adapter;
    private List<Evento> eventos;
    private int offset,limit;
    UsuarioSingleton usuario = UsuarioSingleton.getInstance();
    private RetrofitAPI retrofit;
    EventosSingleton eventosSing = EventosSingleton.getInstance();
    GoogleSignInOptions gso;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences sharedPref;
    SharedPref sharedPrefUtil = new SharedPref();

    @Override
    protected void onResume(){
        super.onResume();
        //Seta dados do usuário no navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        UsuarioNavigationDrawer und = new UsuarioNavigationDrawer();
        und.setNomeUsuario(navigationView,usuario.getNome());
        und.setUsuarioImagem(navigationView, usuario.getFoto());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Inicia barra de carregamento
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarTelaInicial);
        progressBar.setProgress(View.VISIBLE);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Cria objeto para acessar a API de dados Siseventos
        retrofit = new RetrofitAPI();
        final Api api = retrofit.retrofit().create(Api.class);

        //Google Analytics
        try {
            MyApplication application = (MyApplication) getApplication();
            Tracker mTracker = application.getDefaultTracker();
            mTracker.setScreenName("inicial");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }catch (Exception e){}

        /*O usuário acontece de estar vazio na situação em que o usuário
        clica na notificação de novos eventos, neste caso, recupera os dados
        do shared preferences*/
        if (usuario.getNome().equals("")) {
            SharedPreferences sharedPref = this.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0",
                    Context.MODE_PRIVATE);
                UsuarioSingleton usuario = UsuarioSingleton.getInstance();
                usuario.setId(sharedPref.getString("id","default"));
                usuario.setGoogleId(sharedPref.getString("googleId","default"));
                usuario.setEmail(sharedPref.getString("email","default"));
                usuario.setMatricula(sharedPref.getString("matricula","default"));
                usuario.setNascimento(sharedPref.getString("nascimento","default"));
                usuario.setNome(sharedPref.getString("nome","default"));
                usuario.setSenha(sharedPref.getString("senha","default"));
                usuario.setSexo(sharedPref.getString("sexo","default"));
                usuario.setFoto(sharedPref.getString("foto","default"));
                usuario.setAgenda(sharedPref.getString("agenda","0"));
                usuario.setNotificacoes(sharedPref.getString("notificacoes","1"));
                SharedPreferences sharedPref2 = getBaseContext().
                        getSharedPreferences("UFVEVENTOS"+usuario.getId(), Context.MODE_PRIVATE);
                usuario.setToken(sharedPref2.getString("firebasetoken","default"));
        }

        //Cria json object
        JSONObject json = new JSONObject();
        try {
            json.put("token",usuario.getToken());
        }catch(Exception e){Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_SHORT).show();};

        //Busca dados armazenados na agenda do usuário no servidor
        Observable<Object> observableCalendar = api.getCalendar(json);
        observableCalendar.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Object response) {
                        JSONArray jsonArray = null;
                        boolean agendaVazia = false;
                        try{
                            jsonArray = new JSONArray(response.toString());
                            if (jsonArray.getJSONObject(0).has("erro"))
                                agendaVazia = true;
                        }catch(JSONException e){}

                        //Se a agenda não está vazia
                        if (!agendaVazia) {
                            sharedPref = getBaseContext()
                                    .getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0agenda",
                                            Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            int numObjetos = jsonArray.length();
                            for (int i = 0; i < numObjetos; i++) {
                                try {
                                    JSONObject json = new JSONObject();
                                    json = jsonArray.getJSONObject(i);
                                    editor.putLong(json.getString("idEvento"), Long.parseLong(json.getString("idAgenda")));
                                    editor.commit();
                                } catch (JSONException e) {e.printStackTrace();}
                            }
                        }
                    }
                });

        //Seta dados do usuário no navigation drawer
        UsuarioNavigationDrawer und = new UsuarioNavigationDrawer();
        und.setNomeUsuario(navigationView,usuario.getNome());
        und.setUsuarioImagem(navigationView, usuario.getFoto());

        eventos = new ArrayList<>();
        myRecyclerView = (RecyclerView) findViewById(R.id.lista_eventos);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewEventosTelaInicialAdapter(getBaseContext(),eventos,getResources());
        myRecyclerView.setAdapter(adapter);
        adapter.setOnEventoTelaInicialClickListener(new OnEventoTelaInicialClickListener() {
            @Override
            public void onItemClick(Evento item) {
                Intent it;
                //Verifica se o evento possui descrição ou programação
                if (item.getDescricao_evento() == "" && item.getProgramacoes().size() == 0)
                    it = new Intent(getBaseContext(),detalhes_evento_sem_descricao.class);
                else
                    it = new Intent(getBaseContext(),detalhes_evento_com_descricao.class);

                Gson gson = new Gson();
                String json = gson.toJson(item);
                it.putExtra("evento", json);
                startActivity(it);
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("224128381554-g15qnhlokg544p5746fv9q5tg1b0c1aa.apps.googleusercontent.com")
                .requestProfile()
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Start initialize_auth]
        mAuth = FirebaseAuth.getInstance();



        if(!Locale.getDefault().getLanguage().equals("pt")) {
            eventosSing.setCategorias((List<Categoria>) SaveState.loadData(this,SaveState.SAVESTATE_CATEGORIAS_PATH));

        } else {
            eventosSing.setCategorias(new ArrayList<Categoria>());

        }

        Observable<List<Categoria>> observableCategorias = api.getCategorias();
        observableCategorias.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Categoria>>() {
                               @Override
                               public void onCompleted() {
                               }

                               @Override
                               public void onError(Throwable e) {
                                   //Encerra barra de carregamento
                                   progressBar.setVisibility(View.GONE);
                                   Toast.makeText(getBaseContext(), R.string.categorias_toast_loaderror, Toast.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onNext(List<Categoria> response) {
                                   if (SharedPref.deveTraduzir()) {
                                       if (!attListaCategorias(response)) {
                                           eventosSing.setCategorias(traduzirCategorias(response));
                                           SaveState.saveData(getParent(),eventosSing.getCategorias(),SaveState.SAVESTATE_CATEGORIAS_PATH);
                                       }
                                   }
                               }
                           });

        //Recupera eventos salvos internamente e armazena a lista no singleton
        if(SharedPref.deveTraduzir()) {
            eventosSing.setEventos(SaveState.attListaEventos((List<Evento>) SaveState.loadData(this,SaveState.SAVESTATE_EVENTOS_PATH)));

            if(eventosSing.tamanho() > 0) {
                //Copia singleton para a lista de eventos
                for (int i = 0; i < eventosSing.tamanho(); i++) {
                    eventos.add(eventosSing.getEvento(i));
//                    Log.i("TRANSLATE", eventos.get(i).getDataFim());
                }
            }
            Log.i("TRANSLATE", eventosSing.tamanho() + "");

        }

        //Verifica se a lista possui algum evento
//        if (eventosSing.tamanho() < 20) {
            offset = eventosSing.tamanho();
            limit = 110;
            Observable<List<Evento>> observable;
            observable= api.getEventos(offset, limit);

            //Salva todos os limit - offset eventos no singleton
            observable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Evento>>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            //Encerra barra de carregamento
                            progressBar.setVisibility(View.GONE);
                            e.printStackTrace();
                            Toast.makeText(getBaseContext(), R.string.inicial_toast_carregarevento, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(List<Evento> response) {
                            //Copia resultados para a lista de eventos
                            if(SharedPref.deveTraduzir()) {
                                response = traduzirInicial(response);
                            }
                            eventos.addAll(response);
                            eventosSing.setEventos(eventos);
                            SaveState.saveData(getParent(),eventos,SaveState.SAVESTATE_EVENTOS_PATH);

                            //Atualiza RecyclerView
                            if(usuario.getNumCategorias() == 0) {
                                Log.i("TRANSLATE", "Ficou no numCategorias");
                                adapter.notifyDataSetChanged();
                                //Encerra barra de carregamento
                                progressBar.setVisibility(View.GONE);
                            } else {
                                //Se o usuario tiver alguma preferencia de categoria, este trecho executa uma filtragem nos eventos salvos no singleton
                                if(usuario.getNumCategorias() != 0) {
                                    Observable<List<Evento>> observableCat = api.getEventosPorUsuario(usuario.getId(),offset,limit);
                                    observableCat.subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Observer<List<Evento>>() {
                                                @Override
                                                public void onCompleted() {
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    //Encerra barra de carregamento
                                                    progressBar.setVisibility(View.GONE);
                                                    e.printStackTrace();
                                                    Toast.makeText(getBaseContext(), R.string.inicial_toast_carregarevento, Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onNext(List<Evento> response) {
                                                    //Copia resultados para a lista de eventos
                                                    if(SharedPref.deveTraduzir()) {
                                                        eventos = selectEventos(response);
                                                    } else {
                                                        eventos = response;
                                                    }

                                                    //Atualiza RecyclerView
                                                    Log.i("TRANSLATE", "NUMSELECTED ONCREATE: " + eventos.size());
                                                    adapter.notifyDataSetChanged();
                                                    //Encerra barra de carregamento
                                                    progressBar.setVisibility(View.GONE);

                                                }
                                            });
                                }
                            }

                        }
                    });
//        } else {
//            //Atualiza RecyclerView
//            adapter.notifyDataSetChanged();
//            //Encerra barra de carregamento
//            progressBar.setVisibility(View.GONE);
//        }



        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) myRecyclerView
                .getLayoutManager();

        myRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView,
                                   int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0){ //Verifica se o scroll foi pra baixo
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (progressBar.getVisibility() != View.VISIBLE &&
                            (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        //Carrega mais 5 itens
                        //Mostra barra de carregamento
                        progressBar.setVisibility(View.VISIBLE);
                        //Atualiza offset e limit, ou seja, busca mais 10 eventos
                        offset = eventosSing.tamanho();
                        limit = offset+10;
                        Log.i("TRANSLATE", offset + " " + limit);
                        Observable<List<Evento>> observable;
                        if(usuario.getNumCategorias() == 0) observable= api.getEventos(offset, limit);
                        else observable = api.getEventosPorUsuario(usuario.getId(),offset,limit);
                        observable.subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<List<Evento>>() {
                                    @Override
                                    public void onCompleted() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        //Encerra barra de carregamento
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getBaseContext(), R.string.inicial_toast_carregarevento, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onNext(List<Evento> response) {
                                        //Copia resultados para a lista de eventos
                                        if(SharedPref.deveTraduzir()) {
                                            response = traduzirInicial(response);
                                        }
                                        eventos.addAll(response);
                                        SaveState.saveData(getParent(),eventos,SaveState.SAVESTATE_EVENTOS_PATH);
                                        //Atualiza RecyclerView
                                        adapter.notifyDataSetChanged();
                                        //Encerra barra de carregamento
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                        }
                }
            }
        });

    }

    public void escolher_categorias(View view){
        //Dispara intent para a tela de categorias
        Intent it = new Intent(getBaseContext(),categorias_pagina_inicial.class);
        startActivityForResult(it,200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Log.i("TRADUCAO", "IF DO ACTIVITY RESULT");
            //Limpa lista de eventos
            eventos.clear();

            //Cria objeto para acessar a API de dados Siseventos
            RetrofitAPI retrofit = new RetrofitAPI();
            final Api api = retrofit.retrofit().create(Api.class);

            myRecyclerView = (RecyclerView) findViewById(R.id.lista_eventos);
            myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RecyclerViewEventosTelaInicialAdapter(getBaseContext(),eventos,getResources());
            myRecyclerView.setAdapter(adapter);
            adapter.setOnEventoTelaInicialClickListener(new OnEventoTelaInicialClickListener() {
                @Override
                public void onItemClick(Evento item) {
                    Intent it;
                    //Verifica se o evento possui descrição ou programação
                    if (item.getDescricao_evento() == "" && item.getProgramacoes().size() == 0)
                        it = new Intent(getBaseContext(),detalhes_evento_sem_descricao.class);
                    else
                        it = new Intent(getBaseContext(),detalhes_evento_com_descricao.class);

                    Gson gson = new Gson();
                    String json = gson.toJson(item);
                    it.putExtra("evento", json);
                    startActivity(it);
                }
            });

            //Inicia barra de carregamento
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarTelaInicial);
            progressBar.setVisibility(View.VISIBLE);

            //Verifica se a lista possui algum evento
            Log.i("TRANSLATE", "PASSOU NO IF");

            offset = 0;
            limit = 110;
            Observable<List<Evento>> observable = api.getEventosPorUsuario(usuario.getId(),offset,limit);


            if(usuario.getNumCategorias() == 0) {
                eventos = eventosSing.getEventos();
                Log.i("TRANSLATE", "NUMEVENTOS: " + eventos.size());
                adapter.notifyDataSetChanged();
                //Encerra barra de carregamento
                progressBar.setVisibility(View.GONE);
            } else {
            //Se o usuario tiver alguma preferencia de categoria, este trecho executa uma filtragem nos eventos salvos no singleton
                observable.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<Evento>>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                //Encerra barra de carregamento
                                progressBar.setVisibility(View.GONE);
                                e.printStackTrace();
                                Toast.makeText(getBaseContext(), R.string.inicial_toast_carregarevento, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNext(List<Evento> response) {
                                //Copia resultados para a lista de eventos
                                if(SharedPref.deveTraduzir()) {
                                    eventos = selectEventos(response);
                                } else {
                                    eventos = response;
                                }

                                //Atualiza RecyclerView
                                Log.i("TRANSLATE", "NUMSELECTED: " + eventos.size());
                                adapter.notifyDataSetChanged();
                                //Encerra barra de carregamento
                                progressBar.setVisibility(View.GONE);

                            }
                        });
            }

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) myRecyclerView
                    .getLayoutManager();

            myRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (dy > 0){ //Verifica se o scroll foi pra baixo
                        int visibleItemCount = linearLayoutManager.getChildCount();
                        int totalItemCount = linearLayoutManager.getItemCount();
                        int pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                        if (progressBar.getVisibility() != View.VISIBLE &&
                                (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            //Carrega mais 5 itens
                            //Mostra barra de carregamento
                            progressBar.setVisibility(View.VISIBLE);
                            //Atualiza offset e limit, ou seja, busca mais 10 eventos
                            offset = eventosSing.tamanho();
                            limit = offset+10;
                            Log.i("TRANSLATE", offset + " " + limit);
                            Observable<List<Evento>> observable = api.getEventosPorUsuario(usuario.getId(),offset,limit);
                            observable.subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<List<Evento>>() {
                                        @Override
                                        public void onCompleted() {
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            //Encerra barra de carregamento
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(getBaseContext(), "Não foi possível carregar os eventos.", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onNext(List<Evento> response) {
                                            //Copia resultados para a lista de eventos
                                            if(!Locale.getDefault().getLanguage().equals("pt")) {
                                                response = traduzirInicial(response);
                                            }
                                            eventos.addAll(response);
                                            SaveState.saveData(getParent(),eventos,SaveState.SAVESTATE_EVENTOS_PATH);
                                            //Atualiza RecyclerView
                                            adapter.notifyDataSetChanged();
                                            //Encerra barra de carregamento
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    }
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inicial, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_inicio) {
            Intent it = new Intent(getBaseContext(),inicial.class);
            startActivity(it);
        }
        else if(id == R.id.nav_sobre){
            Intent it = new Intent(getBaseContext(), sobre.class);
            startActivity(it);
        }
        else if (id == R.id.nav_editar_perfil) {
            Intent it;
            //Se não é um usuário logado com a conta Google pode editar o perfil
            if (usuario.getGoogleId().equals("default") || usuario.getGoogleId().equals("") ){
                it = new Intent(getBaseContext(), editar_perfil.class);
                startActivity(it);
            }
            else{
                Toast.makeText(getBaseContext(),R.string.inicial_toast_funcindisponivel,Toast.LENGTH_LONG)
                        .show();
            }
        } else if (id == R.id.nav_notificacoes) {
            Intent it = new Intent(getBaseContext(),notificacoes.class);
            startActivity(it);
        } else if (id == R.id.nav_sair) {
            //Registra que o usuário saiu
            SharedPreferences sharedPref = this.getSharedPreferences(sharedPrefUtil.getKey() ,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
            editor.putBoolean("logado",false);
            editor.commit();

            usuario.setGoogleId("");

            // Firebase sign out
            mAuth.signOut();

            // Deletar Arquivos internos
            SaveState.clearData(this);

            // Google sign out
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });

            Intent it = new Intent(getBaseContext(),login.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(it);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private List<Evento> traduzirInicial(List<Evento> eventos) {
        List<String> textosDenominacao = new ArrayList<>();
        List<String> textosDescricao = new ArrayList<>();
        List<String> textosPublicoAlvo = new ArrayList<>();
        for(int i = 0; i < eventos.size(); i++) {
            textosDenominacao.add(eventos.get(i).getDenominacao());
            textosDescricao.add(eventos.get(i).getDescricao_evento());
            textosPublicoAlvo.add(eventos.get(i).getPublicoAlvo());
        }
        try {
            textosDenominacao = new GoogleTranslate().execute(textosDenominacao,"pt", Locale.getDefault().getLanguage()).get();
            textosDescricao = new GoogleTranslate().execute(textosDescricao,"pt", Locale.getDefault().getLanguage()).get();
            textosPublicoAlvo = new GoogleTranslate().execute(textosPublicoAlvo,"pt", Locale.getDefault().getLanguage()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < eventos.size(); i++) {
            eventos.get(i).setDenominacao(textosDenominacao.get(i));
            eventos.get(i).setDescricao_evento(textosDescricao.get(i));
            eventos.get(i).setPublicoAlvo(textosPublicoAlvo.get(i));
        }
        Log.i("TRANSLATE", "Passou pelo save");
        return eventos;
    }



    private List<Categoria> traduzirCategorias(List<Categoria> categorias) {
        List<String> titCategorias = new ArrayList<>();
        for(int i = 0; i < categorias.size(); i++) {
            titCategorias.add(categorias.get(i).getNome());
        }
        try {
            titCategorias = new GoogleTranslate().execute(titCategorias,"pt", Locale.getDefault().getLanguage()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < categorias.size(); i++) {
            categorias.get(i).setNome(titCategorias.get(i));
        }

        return categorias;
    }

    private boolean attListaCategorias(List<Categoria> nCategorias){
        if(eventosSing.getCategorias().size() == nCategorias.size()) {
            for(int i = 0; i < EventosSingleton.getInstance().getCategorias().size(); i++) {
                if(eventosSing.getCategorias().get(i).getId() != nCategorias.get(i).getId()) {
                    return false;
                }
            }
        } else return false;
        return true;
    }

    private List<Evento> selectEventos(List<Evento> response){
        List<Evento> eventos = new ArrayList<>();
        for(int i = 0, j = 0; i < eventosSing.tamanho() && j < response.size(); i++) {
            if(response.get(j).getId() == eventosSing.getEvento(i).getId()) {
                eventos.add(eventosSing.getEvento(i));
                j++;
            }
        }
        return eventos;
    }
}
