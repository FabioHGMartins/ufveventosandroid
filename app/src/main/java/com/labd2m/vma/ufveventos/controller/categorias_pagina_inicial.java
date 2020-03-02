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
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.labd2m.vma.ufveventos.R;
import com.labd2m.vma.ufveventos.model.Categoria;
import com.labd2m.vma.ufveventos.model.EventosSingleton;
import com.labd2m.vma.ufveventos.model.Usuario;
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

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class categorias_pagina_inicial extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private RecyclerView myRecyclerView;
    private RecyclerViewCategoriasAdapter adapter;
    UsuarioSingleton usuario = UsuarioSingleton.getInstance();
    GoogleSignInOptions gso;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private List<String> categorias_preferencias;
    SharedPref sharedPrefUtil = new SharedPref();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias_pagina_inicial);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        //MODIFICA O TEXTO DA OPÇÃO DE LOGOUT DO NAVIGATION DRAWER QUANDO USUÁRIO FOR ANÔNIMO
        try {
            //Verifica se usuário entrou como anônimo
            if(UsuarioSingleton.getInstance().getEmail().contains("@anonimo.com")){
                navigationView.getMenu().getItem(4).setTitle("Login");
            }
        }catch (Exception e){
            Log.e("SairMsg", e.getMessage());
        }

        navigationView.setNavigationItemSelectedListener(this);

        categorias_preferencias = new ArrayList();

        //Google Analytics
        MyApplication application = (MyApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("categorias_pagina_inicial");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

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

        //Seta dados do usuário no navigation drawer
        UsuarioNavigationDrawer und = new UsuarioNavigationDrawer();
        und.setNomeUsuario(navigationView,usuario.getNome());
        und.setUsuarioImagem(navigationView, usuario.getFoto());

        myRecyclerView = (RecyclerView) findViewById(R.id.lista_categorias);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewCategoriasAdapter(getBaseContext(), EventosSingleton.getInstance().getCategorias(),categorias_preferencias);
        myRecyclerView.setAdapter(adapter);
        adapter.setCategoriaClickListener(new OnCategoriaClickListener() {
            @Override
            public void onItemClick(Categoria item) {
            }
        });

        //Inicia barra de carregamento
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarCategorias);
        progressBar.setProgress(View.VISIBLE);

        //Cria objeto para acessar a API de dados Siseventos
        RetrofitAPI retrofit = new RetrofitAPI();
        final Api api = retrofit.retrofit().create(Api.class);

        Observable<List<Categoria>> observable = api.getPreferenciasDeCategorias(usuario.getId());
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Categoria>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Encerra barra de carregamento
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(final List<Categoria> response) {
                        categorias_preferencias.clear();
                        for (int j = 0; j < response.size(); j++)
                            categorias_preferencias.add(""+response.get(j).getId());
                        //Atualiza RecyclerView
                        adapter.notifyDataSetChanged();
                        //Encerra barra de carregamento
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public void escolher_categorias(View view){
        JSONArray json = new JSONArray(categorias_preferencias);
        String aux = json.toString();
        final String data = "{\"categorias\":"+aux+"}";

        //Inicia barra de carregamento
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarCategorias);
        progressBar.setVisibility(View.VISIBLE);

        //Cria objeto para acessar a API de dados Siseventos
        RetrofitAPI retrofit = new RetrofitAPI();
        final Api api = retrofit.retrofit().create(Api.class);

        Observable<Void> observable = api.updatePreferenciasCategorias(data,usuario.getId());
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Encerra barra de carregamento
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getBaseContext(), R.string.categorias_toast_updateerror, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Void response) {
                        Toast.makeText(getBaseContext(), R.string.categorias_toast_success, Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        UsuarioSingleton.getInstance().setNumCategorias(categorias_preferencias.size());
                        resultIntent.putExtra("numCategorias",categorias_preferencias.size());
                        setResult(Activity.RESULT_OK,resultIntent);
                        finish();
                    }
                });
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
        getMenuInflater().inflate(R.menu.categorias_pagina_inicial, menu);
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
        } else if (id == R.id.nav_editar_perfil) {
            Intent it;
            //Se não é um usuário logado com a conta Google pode editar o perfil
            if (usuario.getGoogleId().equals("default") || usuario.getGoogleId().equals("") ){
                //Verifica se usuário entrou como anônimo
                if(UsuarioSingleton.getInstance().getEmail().contains("@anonimo.com")){
                    Toast.makeText(getBaseContext(),R.string.inicial_toast_anonimo,Toast.LENGTH_LONG)
                            .show();
                }else{
                    it = new Intent(getBaseContext(), editar_perfil.class);
                    startActivity(it);
                }
            }
            else{
                Toast.makeText(getBaseContext(),R.string.categorias_toast_funcindisponivel,Toast.LENGTH_LONG)
                        .show();
            }
        } else if (id == R.id.nav_notificacoes) {
            //Verifica se usuário entrou como anônimo
            if(UsuarioSingleton.getInstance().getEmail().contains("@anonimo.com")){
                Toast.makeText(getBaseContext(),R.string.inicial_toast_anonimo,Toast.LENGTH_LONG)
                        .show();
            }else{
                Intent it = new Intent(getBaseContext(),notificacoes.class);
                startActivity(it);
            }
        } else if (id == R.id.nav_sair) {
            //Registra que o usuário saiu
            SharedPreferences sharedPref = this.getSharedPreferences(sharedPrefUtil.getKey(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
            editor.putBoolean("logado",false);
            editor.commit();

            // Firebase sign out
            mAuth.signOut();

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

}