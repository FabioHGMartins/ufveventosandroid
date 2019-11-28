package com.labd2m.vma.ufveventos.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.labd2m.vma.ufveventos.R;
import com.labd2m.vma.ufveventos.model.Categoria;
import com.labd2m.vma.ufveventos.model.Evento;
import com.labd2m.vma.ufveventos.model.Local;
import com.labd2m.vma.ufveventos.model.Programacao;
import com.labd2m.vma.ufveventos.util.Agenda;
import com.labd2m.vma.ufveventos.util.GoogleTranslate;
import com.labd2m.vma.ufveventos.util.Permission;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.labd2m.vma.ufveventos.util.SharedPref;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class detalhes_evento_sem_descricao extends AppCompatActivity implements OnMapReadyCallback, LocationListener,
    View.OnClickListener{
    GoogleMap mGoogleMap;
    private LocationManager mLocationManager = null;
    private String provider = null;
    private Marker mCurrentPosition = null;
    private ArrayList<LatLng> traceOfMe = null;
    private Polyline mPolyline = null;
    private LatLng mSourceLatLng = new LatLng(-20.7661678,-20.7661678);
    private LatLng mDestinationLatLng;
    public Evento evento;

    public boolean isTraced = false;
    public boolean isLocalized = false;
    public long minTime = 5000;// ms
    public float minDist = 0;// meter

    public String localEventoMarcador = "";
    public Marker myMarker = null;
    public Location locationStart = null;
    public int contLocationUpdates = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Captura evento solicitado
        String eventoJson = getIntent().getStringExtra("evento");
        Gson gson = new Gson();
        evento = gson.fromJson(eventoJson, Evento.class);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento_sem_descricao);

        mLocationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Google Analytics
        MyApplication application = (MyApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("detalhes_evento_sem_descricao");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        findViewById(R.id.addAgenda).setOnClickListener(this);



        SharedPreferences sharedPref = this.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0",
                Context.MODE_PRIVATE);

        //Traça rota
        List<Local> locaisAux = evento.getLocais();
        double latDest = Double.parseDouble(locaisAux.get(0).getLatitude());
        double lngDest = Double.parseDouble(locaisAux.get(0).getLongitude());
        mDestinationLatLng = new LatLng(latDest, lngDest);

        //Requisita permissão para mapas
        Permission permission = new Permission();
        permission.requestPermissionMaps(detalhes_evento_sem_descricao.this,this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            if (googleServicesAvailable()){
                initMap();
            }

        //Seta denominação do evento
        if (evento.getDenominacao() != null){
            ((TextView) findViewById(R.id.tituloEvento)).
                    setText(evento.getDenominacao());
        }

        //Seta hora de início e fim do evento
        if (evento.getHoraInicio() != null && evento.getHoraFim() != null) {
            String horaInicio = evento.getHoraInicio().substring(0, 5);
            String horaFim = evento.getHoraFim().substring(0, 5);
            findViewById(R.id.horarioLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.horarioEvento)).
                    setText(horaInicio+" - "+horaFim);
        }

        //Seta data do evento
        if (evento.getDataInicio() != null && evento.getDataFim() != null) {
            String aux = evento.getDataInicio();
            String dataInicio = aux.substring(8, 10) + "/" + aux.substring(5, 7) + "/" + aux.substring(0, 4);
            aux = evento.getDataFim();
            String dataFim = aux.substring(8, 10) + "/" + aux.substring(5, 7) + "/" + aux.substring(0, 4);
            findViewById(R.id.dataLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.dataEvento)).
                    setText(dataInicio + " - " + dataFim);
        }

        //Seta local do evento
        if (evento.getLocais().size() > 0) {
            List<Local> locais = evento.getLocais();
            String local = "";
            for (int i = 0; i < locais.size(); i++) {
                local = local + locais.get(i).getDescricao();
                if (i != locais.size() - 1)
                    local = local + ", ";
            }


            if(SharedPref.deveTraduzir(sharedPref))
                local = translate(local);

            findViewById(R.id.localLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.localEvento)).
                    setText(local);
        }

        //Seta número de participantes do evento
        if (evento.getMostrarparticipantes() == 1) { //Deseja divulgar o número de participantes
            if (evento.getNumeroParticipantes() > 0) {
                findViewById(R.id.participantesLabelEvento).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.participantesEvento)).
                        setText(evento.getNumeroParticipantes());
            }
        }else{
            findViewById(R.id.participantesLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.participantesEvento)).
                    setText(R.string.detalhes_ilimitado);
        }

        //Seta valor da inscrição
        if (evento.getTeminscricao() == 1)
            if (evento.getValorinscricao() == 0){
                findViewById(R.id.taxaIngressoLabel).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.taxaIngresso)).
                        setText(R.string.detalhes_gratuito);
            }else{
                String valor = String.format( "%.2f",evento.getValorinscricao());
                findViewById(R.id.taxaIngressoLabel).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.taxaIngresso)).
                        setText("R$"+valor);
            }
        else {
            findViewById(R.id.taxaIngressoLabel).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.taxaIngresso)).
                    setText(R.string.detalhes_sem_inscricao);
        }


        //Seta local ou link da inscricao
        if (evento.getTeminscricao() == 1){
            findViewById(R.id.localInscricaoLabel).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.localInscricao)).
                    setText(evento.getLinklocalinscricao());
        }

        if (evento.getPublicoAlvo() != null) {
            //Seta público alvo do evento
            findViewById(R.id.publicoAlvoLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.publicoAlvoEvento)).
                    setText(evento.getPublicoAlvo());
        }

        //Seta categorias do evento
        if (evento.getCategorias().size() > 0) {
            List<Categoria> categorias = evento.getCategorias();
            String categoria = "";
            for (int i = 0; i < categorias.size(); i++) {
                categoria = categoria + categorias.get(i).getNome();
                if (i != categorias.size() - 1)
                    categoria = categoria + ", ";
            }
            findViewById(R.id.categoriaLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.categoriaEvento)).
                    setText(categoria);
        }
    }

    protected void onDestroy() {
        try{
            mLocationManager.removeUpdates(this);
            Log.d("Location", "Parou serviço de localização");
        }catch (Exception e){
            Log.e("Location", "Erro ao parar serviço de localização");
        }

        super.onDestroy();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onRestart() {
        super.onRestart();

        try{
            if(!isLocalized){
                if (isProviderAvailable() && provider != null) {
                    locateCurrentPosition();                    //inicia localização
                    Log.d("Location", "Reiniciou localização " + provider);
                }
            }
        }catch (Exception e){
            Log.d("Location", "Erro ao reiniciar localização");
        }

    }

    private void initMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View view){
        try {
            int i = view.getId();
            //Clicou no botão de adicionar à agenda
            if (i == R.id.addAgenda) {
                //Requisita permissão para escrita
                Permission permission = new Permission();
                permission.requestPermissionCalendar(detalhes_evento_sem_descricao.this, this);

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                        == PackageManager.PERMISSION_GRANTED) {
                    Agenda agenda = new Agenda();
                    agenda.addEvent(evento, getBaseContext(), getContentResolver(), getParent());
                    Toast.makeText(getBaseContext(), R.string.detalhes_add_agenda, Toast.LENGTH_LONG).show();
                }
            }
        }catch (Exception e){
            Toast.makeText(getBaseContext(), R.string.detalhes_add_agenda_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //mGoogleMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);

        if (isProviderAvailable() && provider != null) {
            locateCurrentPosition();                    //inicia localização
            traceMe(mSourceLatLng,mDestinationLatLng);  //traça rota
        }else{
            //serviço de localização desativado
            locationServiceEnable();
        }

    }

    //ativa serviço de localização
    private void locationServiceEnable() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);

        alerta.setMessage(R.string.gps_desativado_title);
        alerta.setPositiveButton(R.string.gps_ativar_msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        alerta.setNegativeButton(R.string.gps_cancelar_ativacao, null).show();
    }

    private void locateCurrentPosition() {
        int status = getPackageManager().checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                getPackageName());

        if (status == PackageManager.PERMISSION_GRANTED) {
            locationStart = mLocationManager.getLastKnownLocation(provider);
            updateWithNewLocation(locationStart);
            mLocationManager.requestLocationUpdates(provider, minTime, minDist, this);
        }
    }

    private boolean isProviderAvailable() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        provider = mLocationManager.getBestProvider(criteria, true);

        if (mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;

            return true;
        }

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            return true;
        }

        return false;
    }

    private void updateWithNewLocation(Location location) {
        if (location != null && provider != null) {
            double lng = location.getLongitude();
            double lat = location.getLatitude();

            mSourceLatLng = new LatLng(lat, lng);

            //Atualiza marcador do usuário
            if(myMarker != null)
                myMarker.remove();

            myMarker = addMarker(lat,
                    lng,
                    getResources().getString(R.string.detalhes_minha_localizacao),
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));


            CameraPosition camPosition = new CameraPosition.Builder()
                    .target(new LatLng(lat, lng)).zoom(14f).build();

            if (mGoogleMap != null) {
                //só atualiza a câmera na primeira vez que pega a posição do usuário
                if(contLocationUpdates == 0)
                    mGoogleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(camPosition));
                else
                    Log.d("Marcador", "Contador de atualizações: " + contLocationUpdates);

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)
                    mGoogleMap.setMyLocationEnabled(true);
            }

            contLocationUpdates += 1;
        } else {
            Log.d("Location error", "Something went wrong");
        }

    }

    private void addBoundaryToCurrentPosition(double lat, double lang) {
        MarkerOptions mMarkerOptions = new MarkerOptions();
        mMarkerOptions.position(new LatLng(lat, lang));
        mMarkerOptions.anchor(0.5f, 0.5f);

        if(mCurrentPosition != null) {
            mCurrentPosition.remove();
            Log.d("Marcador", "Removi posição evento");
        }

        //TODO: posição temporária do evento. Eliminar futuramente
        mCurrentPosition = mGoogleMap.addMarker(mMarkerOptions);
    }

    private void addMarker(double lat, double lng, String text) {
        mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat,lng))
                .title(text)
                .flat(true));
    }

    private Marker addMarker(double lat, double lng, String text, BitmapDescriptor cor ) {
        return mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat,lng))
                .title(text)
                .icon(cor)
                .flat(false));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Location", "Localização atualizada");

        updateWithNewLocation(location);
        isLocalized = true;

        if(location != null)
            locationStart = location;

        if(!isTraced)
            traceMe(mSourceLatLng,mDestinationLatLng);  //traça rota quando usuário ativa GPS e volta para app
    }

    @Override
    public void onProviderDisabled(String provider) {

        updateWithNewLocation(null);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                break;
            case LocationProvider.AVAILABLE:
                break;
        }
    }

    private void traceMe(LatLng srcLatLng, LatLng destLatLng) {
        /*String srcParam = srcLatLng.latitude + "," + srcLatLng.longitude;
        String destParam = destLatLng.latitude + "," + destLatLng.longitude;
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+srcParam+"&destination="
                + destParam + "&sensor=false&units=metric&mode=driving&key=AIzaSyCYMR04JVUMSJMs0BtLxl6rsAVY-xwTLqk";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        MapDirectionsParser parser = new MapDirectionsParser();
                        List<List<HashMap<String, String>>> routes = parser.parse(response);
                        ArrayList<LatLng> points = null;

                        for (int i = 0; i < routes.size(); i++) {
                            points = new ArrayList<LatLng>();

                            // Fetching i-th route
                            List<HashMap<String, String>> path = routes.get(i);

                            //Limpa mapa
                            mGoogleMap.clear();

                            //Adiciona marcador à posição inicial
                            HashMap<String, String> pointAux = path.get(0);
                            double latAux = Double.parseDouble(pointAux.get("lat"));
                            double lngAux = Double.parseDouble(pointAux.get("lng"));

                            CircleOptions mOptions = new CircleOptions()
                                    .center(new LatLng(latAux, lngAux)).radius(200)
                                    .strokeColor(0x110000FF).strokeWidth(5).fillColor(0x110000FF);
                            mGoogleMap.addCircle(mOptions);

                            CircleOptions circleOptions = new CircleOptions()
                                    .center(new LatLng(latAux, lngAux))
                                    .strokeWidth(1)
                                    .fillColor(Color.BLUE)
                                    .radius(60); // In meters
                            mGoogleMap.addCircle(circleOptions);

                            //Adiciona marcador à posição final
                            pointAux = path.get(path.size()-1);
                            latAux = Double.parseDouble(pointAux.get("lat"));
                            lngAux = Double.parseDouble(pointAux.get("lng"));
                            addMarker(latAux,lngAux,"Destino");

                            // Fetching all the points in i-th route
                            for (int j = 0; j < path.size(); j++) {
                                HashMap<String, String> point = path.get(j);

                                double lat = Double.parseDouble(point.get("lat"));
                                double lng = Double.parseDouble(point.get("lng"));
                                LatLng position = new LatLng(lat, lng);

                                points.add(position);
                            }
                        }
                        drawPoints(points, mGoogleMap);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        MyApplication.getInstance().addToReqQueue(jsonObjectRequest);
        addBoundaryToCurrentPosition(destLatLng.latitude,destLatLng.longitude);*/

        String srcParam = srcLatLng.latitude + "," + srcLatLng.longitude;
        String destParam = destLatLng.latitude + "," + destLatLng.longitude;
        /*String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+srcParam+"&destination="
                + destParam + "&sensor=false&units=metric&mode=driving&key=AIzaSyCYMR04JVUMSJMs0BtLxl6rsAVY-xwTLqk";*/

        //Mesmo padrão de requisição do iOS
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+srcParam+"&destination="
                + destParam + "&sensor=false&mode=driving&key=AIzaSyC2vzuwOgPqc-bKKZZ_OykqsTYx6qRTTe8";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Rota", "Obtive rota");
                        MapDirectionsParser parser = new MapDirectionsParser();
                        List<List<HashMap<String, String>>> routes = parser.parse(response);
                        ArrayList<LatLng> points = null;

                        Log.d("Rota", "Tamanho rota: " + routes.size());

                        if(routes.size() > 0) {
                            for (int i = 0; i < routes.size(); i++) {
                                points = new ArrayList<LatLng>();

                                // Fetching i-th route
                                List<HashMap<String, String>> path = routes.get(i);

                                //Limpa mapa
                                mGoogleMap.clear();

                                //Adiciona marcador à posição final
                                HashMap<String, String> pointAux = path.get(path.size() - 1);
                                Double latAux = Double.parseDouble(pointAux.get("lat"));
                                Double lngAux = Double.parseDouble(pointAux.get("lng"));

                                //Marcador do local do evento
                                addMarker(latAux, lngAux, getResources().getString(R.string.detalhes_destino));

                                //Local do evento não traduzido no marcador
                                //addMarker(latAux,lngAux,localEventoMarcador);

                                //TODO: posição inicial da pessoa. Eliminar futuramente
                                if (myMarker != null)
                                    myMarker.remove();

                                try{
                                    myMarker = addMarker(locationStart.getLatitude(),
                                            locationStart.getLongitude(),
                                            getResources().getString(R.string.detalhes_minha_localizacao),
                                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                                    isTraced = true; //traçou rota
                                }catch (Exception e){
                                    Log.e("Location", "Localização inicial desconhecida");
                                }


                                Log.d("Rota", "Pontos na rota: " + path.size());

                                // Fetching all the points in i-th route
                                for (int j = 0; j < path.size(); j++) {
                                    HashMap<String, String> point = path.get(j);

                                    double lat = Double.parseDouble(point.get("lat"));
                                    double lng = Double.parseDouble(point.get("lng"));
                                    LatLng position = new LatLng(lat, lng);

                                    points.add(position);
                                }
                            }
                            drawPoints(points, mGoogleMap);
                        }else
                            Log.d("Rota", "Não foi possível obter rota");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Rota", "Erro na rota");
                    }
                });

        MyApplication.getInstance().addToReqQueue(jsonObjectRequest);
        addBoundaryToCurrentPosition(destLatLng.latitude,destLatLng.longitude);
    }


    private void drawPoints(ArrayList<LatLng> points, GoogleMap mMaps) {
        if (points == null) {
            return;
        }
        traceOfMe = points;
        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : traceOfMe) {
            polylineOpt.add(latlng);
        }
        polylineOpt.color(Color.BLUE);
        if (mPolyline != null) {
            mPolyline.remove();
            mPolyline = null;
        }
        if (mGoogleMap != null) {
            mPolyline = mGoogleMap.addPolyline(polylineOpt);

        } else {

        }
        if (mPolyline != null)
            mPolyline.setWidth(10);
    }

    /*public void getDirection(View view) {
        if (mSourceLatLng != null && mDestinationLatLng != null) {
            traceMe(mSourceLatLng, mDestinationLatLng);
        }
    }*/

    public boolean googleServicesAvailable(){
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS){
            return true;
        }else if (api.isUserResolvableError(isAvailable)){
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        }else{
            Toast.makeText(getBaseContext(),R.string.detalhes_toast_error,Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void showHideFirstPart(View view){
        //Aumenta ou reduz quadro com informações gerais
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (view.getHeight() < convertDpToPixel((float) 270, getBaseContext())){ // Verifica se está recolhido
            params.height = Math.round(convertDpToPixel((float)270, getBaseContext())); //Abre
            ((ImageView) findViewById(R.id.abreFechaFirstPart)).setImageResource(R.drawable.fechar); //Seta imagem "fechar"
        }
        else {
            ((ImageView) findViewById(R.id.abreFechaFirstPart)).setImageResource(R.drawable.abrir); //Seta imagem "abrir"
            params.height = Math.round(convertDpToPixel((float) 67.5, getBaseContext())); //Fecha
        }
        view.setLayoutParams(params);

        //Recolhe retangulo vermelho
        LinearLayout layout = (LinearLayout) findViewById(R.id.retanguloDetalhesEvento);
        params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
        if (params.height < convertDpToPixel((float)250, getBaseContext())) // Verifica se está recolhido
            params.height = Math.round(convertDpToPixel((float)250, getBaseContext()));
        else
            params.height = Math.round(convertDpToPixel((float)62.5, getBaseContext()));
        layout.setLayoutParams(params);
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)
                        if (googleServicesAvailable())
                            initMap();
                } else {
                }
                return;
            }
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                            == PackageManager.PERMISSION_GRANTED) {
                        Agenda agenda = new Agenda();
                        agenda.addEvent(evento, getBaseContext(), getContentResolver(), getParent());
                        Toast.makeText(getBaseContext(), R.string.detalhes_add_agenda, Toast.LENGTH_LONG).show();
                    }
                } else {
                }
                return;
            }
        }
    }

    private String translate(String text) {
        if(!Locale.getDefault().getLanguage().equals("pt")) {
            try {
                List<String> aux = new ArrayList<>();
                aux.add(text);
                text = (new GoogleTranslate().execute(aux, "pt", Locale.getDefault().getLanguage()).get()).get(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return text;
    }
}
