package com.labd2m.vma.ufveventos.controller;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.labd2m.vma.ufveventos.R;
import com.labd2m.vma.ufveventos.model.UsuarioSingleton;
import com.labd2m.vma.ufveventos.util.RetrofitAPI;
import com.labd2m.vma.ufveventos.util.SaveState;
import com.labd2m.vma.ufveventos.util.Seguranca;
import com.labd2m.vma.ufveventos.util.UsuarioNavigationDrawer;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class editar_perfil extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    UsuarioSingleton usuario = UsuarioSingleton.getInstance();
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Google Analytics
        MyApplication application = (MyApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("editar_perfil");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        //Seta dados do usuário no navigation drawer
        UsuarioNavigationDrawer und = new UsuarioNavigationDrawer();
        und.setNomeUsuario(navigationView,usuario.getNome());
        und.setUsuarioImagem(navigationView, usuario.getFoto());

        //Inicia barra de carregamento
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarEditarPerfil);
        progressBar.setVisibility(View.GONE);

        //Seta campos com os dados do usuário logado
        UsuarioSingleton usuario = UsuarioSingleton.getInstance();
        ((EditText) findViewById(R.id.nomeEditarPerfil)).setText(usuario.getNome());
        ((EditText) findViewById(R.id.emailEditarPerfil)).setText(usuario.getEmail());
        if (!usuario.getNascimento().isEmpty()) {
            String data = usuario.getNascimento().substring(8, 10) + "/" + usuario.getNascimento().substring(5,7)
                    +"/"+usuario.getNascimento().substring(0,4);
            ((EditText) findViewById(R.id.nascimentoEditarPefil)).setText(data);
        }

        //Recupera sexo
        Spinner spinner = (Spinner) findViewById(R.id.sexo_editar_perfil);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sexo_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //Seta o sexo do usuário cadastrado
        if (usuario.getSexo().equals("m"))
            spinner.setSelection(2);
        else
            if (usuario.getSexo().equals("f"))
                spinner.setSelection(1);
            else
                if (usuario.getSexo().equals("m"))
                    spinner.setSelection(3);
    }

    public void alterar_cadastro(View view){
        //Valida campos
        boolean valido1 = validaEditText("nomeErroEditarPerfil","nomeEditarPerfil","O campo não pode estar vazio.");
        boolean valido2 = validaEditText("emailErroEditarPerfil","emailEditarPerfil","O campo não pode estar vazio.");
        boolean valido3 = validaEditText("senhaErroEditarPerfil","senhaEditarPerfil","O campo não pode estar vazio.");
        boolean valido4 = validaSenha("confirmaSenhaErroEditarPerfil","confirmaSenhaEditarPerfil",
                "senhaErroEditarPerfil","senhaEditarPerfil","Este campo precisa ser igual à senha.");

        //Se os dados digitados estão corretos envia ao servidor
        if (valido1 && valido2 && valido3 && valido4){
            //Mostra barra de carregamento
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarEditarPerfil);
            progressBar.setVisibility(View.VISIBLE);

            //Recupera dados do formulário
            String nome = ((EditText) findViewById(R.id.nomeEditarPerfil)).getText().toString();
            final String email = ((EditText) findViewById(R.id.emailEditarPerfil)).getText().toString();
            String senhaTemp = ((EditText) findViewById(R.id.senhaEditarPerfil)).getText().toString();
            Seguranca s = new Seguranca();
            final String senha = s.duploMd5(senhaTemp);
            String nascimento = ((EditText) findViewById(R.id.nascimentoEditarPefil)).getText().toString();
            if (!nascimento.isEmpty())
                nascimento = nascimento.substring(6,10)+"-"+nascimento.substring(3,5)+"-"+nascimento.substring(0,2);

            Spinner spinner = (Spinner) findViewById(R.id.sexo_editar_perfil);
            String sexo = spinner.getSelectedItem().toString();
            if (sexo.equals(String.valueOf(R.string.sexo_array_item1)))
                sexo = "p";
            else
            if(sexo.equals(String.valueOf(R.string.sexo_array_item2)))
                sexo = "f";
            else
            if (sexo.equals(String.valueOf(R.string.sexo_array_item3)))
                sexo = "m";
            else
                sexo = "o";

            //Atualiza singleton usuario
            usuario.setSexo(sexo);
            usuario.setSenha(senha);
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setNascimento(nascimento);

            //Atualiza shared preferences
            sharedPref = this.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("email");
            editor.putString("email", email);
            editor.remove("nascimento");
            editor.putString("nascimento", nascimento);
            editor.remove("nome");
            editor.putString("nome", nome);
            editor.remove("senha");
            editor.putString("senha", senha);
            editor.remove("sexo");
            editor.putString("sexo", sexo);
            editor.commit();

            //Cria json object
            JSONObject json = new JSONObject();
            try {
                json.put("nome", nome);
                json.put("email", email);
                json.put("senha", senha);
                json.put("nascimento",nascimento);
                json.put("sexo", sexo);
            }catch(Exception e){Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_SHORT).show();};

            //Cria objeto para acessar a API de dados Siseventos
            RetrofitAPI retrofit = new RetrofitAPI();
            Api api = retrofit.retrofit().create(Api.class);

            //Faz requisição ao servidor
            Observable<Void> observable =  api.updateUsuario(json,usuario.getId());

            //Intercepta a resposta da requisição
            observable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Void>(){
                        @Override
                        public void onCompleted(){}

                        @Override
                        public void onError(Throwable e){
                            //Esconde barra de carregamento
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getBaseContext(),R.string.atualizar_toast_cadastrarerror1, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onNext(Void response){
                            //Esconde barra de carregamento
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getBaseContext(),R.string.atualizar_toast_attsucesso,Toast.LENGTH_SHORT).show();
                            finish();
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
        //getMenuInflater().inflate(R.menu.editar_perfil, menu);
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
                it = new Intent(getBaseContext(), editar_perfil.class);
                startActivity(it);
            }
            else{
                Toast.makeText(getBaseContext(),R.string.atualizar_toast_cadastrarerror2,Toast.LENGTH_LONG)
                        .show();
            }
        } else if (id == R.id.nav_notificacoes) {
            Intent it = new Intent(getBaseContext(),notificacoes.class);
            startActivity(it);
        } else if (id == R.id.nav_sair) {
            //Registra que o usuário saiu
            SharedPreferences sharedPref = this.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
            editor.putBoolean("logado",false);
            editor.commit();

            // Deletar Arquivos internos
            SaveState.clearData(this,SaveState.SAVESTATE_CATEGORIAS_PATH);
            SaveState.clearData(this,SaveState.SAVESTATE_EVENTOS_PATH);

            Intent it = new Intent(getBaseContext(),login.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(it);
            finish();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public boolean validaSenha(String idErroConfirma, String idCampoConfirma, String idErro, String idCampo, String msg){
        //Busca referência do campo
        int texto1 = getResources().getIdentifier(idCampo, "id",
                this.getBaseContext().getPackageName());

        int texto2 = getResources().getIdentifier(idCampoConfirma, "id",
                this.getBaseContext().getPackageName());

        //Escrever texto embaixo do campo
        String aux1 = ((EditText) findViewById(texto1)).getText().toString();
        String aux2 = ((EditText) findViewById(texto2)).getText().toString();
        if (!aux1.equals(aux2)) {
            //Muda a cor do campo para vermelho
            ((EditText) findViewById(texto2))
                    .getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);

            //Busca referência do campo
            texto2 = getResources().getIdentifier(idErroConfirma, "id",
                    this.getBaseContext().getPackageName());
            //Escrever texto embaixo do campo
            TextView senhaErro = ((TextView) findViewById(texto2));
            senhaErro.setText(msg);

            return false;
        }else{
            //Volta ao normal a cor do campo
            ((EditText) findViewById(texto2))
                    .getBackground().mutate().setColorFilter(getResources().getColor(R.color.EditText), PorterDuff.Mode.SRC_ATOP);
            //Busca referência do campo
            texto2 = getResources().getIdentifier(idErroConfirma, "id",
                    this.getBaseContext().getPackageName());
            //Remove texto de erro
            TextView emailmatriculaErro = ((TextView) findViewById(texto2));
            emailmatriculaErro.setText("");
            return true;
        }
    }
    public boolean validaEditText(String idErro, String idCampo, String msg){
        //Busca referência do campo
        int texto = getResources().getIdentifier(idCampo, "id",
                this.getBaseContext().getPackageName());

        //Escrever texto embaixo do campo
        String aux = ((EditText) findViewById(texto)).getText().toString();
        if (aux.isEmpty()) {
            //Muda a cor do campo para vermelho
            ((EditText) findViewById(texto))
                    .getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);

            //Busca referência do campo
            texto = getResources().getIdentifier(idErro, "id",
                    this.getBaseContext().getPackageName());
            //Escrever texto embaixo do campo
            TextView senhaErro = ((TextView) findViewById(texto));
            senhaErro.setText(msg);

            return false;
        }else{
            //Volta ao normal a cor do campo
            ((EditText) findViewById(texto))
                    .getBackground().mutate().setColorFilter(getResources().getColor(R.color.EditText), PorterDuff.Mode.SRC_ATOP);
            //Busca referência do campo
            texto = getResources().getIdentifier(idErro, "id",
                    this.getBaseContext().getPackageName());
            //Remove texto de erro
            TextView emailmatriculaErro = ((TextView) findViewById(texto));
            emailmatriculaErro.setText("");
            return true;
        }
    }
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            //Seta data no input de data de nascimento
            EditText aux = (EditText) getActivity().findViewById(R.id.nascimentoEditarPefil);
            //Formata a data
            String dia = ""+day;
            if (day < 10)
                dia = "0"+day;
            month += 1;
            String mes = ""+month;
            if (month < 10)
                mes = "0"+month;

            String locale = this.getResources().getConfiguration().locale.getCountry();
            if(locale.equals("US")) {
                aux.setText(mes+"/"+dia+"/"+year);
            } else {
                aux.setText(dia+"/"+mes+"/"+year);
            }
        }
    }
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
}
