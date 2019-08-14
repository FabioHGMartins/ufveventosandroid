package com.labd2m.vma.ufveventos.controller;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
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
import com.labd2m.vma.ufveventos.model.EventosSingleton;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.labd2m.vma.ufveventos.util.SharedPref;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class detalhes_evento_com_descricao extends AppCompatActivity implements OnMapReadyCallback, LocationListener,
        View.OnClickListener {
    GoogleMap mGoogleMap;
    private LocationManager mLocationManager = null;
    private String provider = null;
    private Marker mCurrentPosition = null;
    private ArrayList<LatLng> traceOfMe = null;
    private Polyline mPolyline = null;
    private LatLng mSourceLatLng = null;
    private LatLng mDestinationLatLng;
    public Evento evento;
    public float yAnterior;
    public float y;

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
        setContentView(R.layout.activity_detalhes_evento_com_descricao);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        LinearLayout thirdPart = (LinearLayout) findViewById(R.id.thirdPartDetalhesEvento);
        thirdPart.setOnTouchListener(new ScrollFunction());

        //Google Analytics
        MyApplication application = (MyApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("detalhes_evento_com_descricao");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        findViewById(R.id.addAgenda).setOnClickListener(this);

        //Traça rota
        List<Local> locaisAux = evento.getLocais();
        double latDest = Double.parseDouble(locaisAux.get(0).getLatitude());
        double lngDest = Double.parseDouble(locaisAux.get(0).getLongitude());
        mDestinationLatLng = new LatLng(latDest, lngDest);
        mSourceLatLng = new LatLng(latDest, lngDest);

        SharedPreferences sharedPref = this.getSharedPreferences("UFVEVENTOS45dfd94be4b30d5844d2bcca2d997db0",
                Context.MODE_PRIVATE);

        //Requisita permissão para mapas
        Permission permission = new Permission();
        permission.requestPermissionMaps(detalhes_evento_com_descricao.this,this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (googleServicesAvailable())
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
            String locale = this.getResources().getConfiguration().locale.getCountry();
            String dataInicio;
            String dataFim;
            if(locale.equals("US")) {
                dataInicio = aux.substring(5, 7) + "/" + aux.substring(8, 10) + "/" + aux.substring(0, 4);
                dataFim = aux.substring(5, 7) + "/" + aux.substring(8, 10) + "/" + aux.substring(0, 4);
            } else {
                dataInicio = aux.substring(8, 10) + "/" + aux.substring(5, 7) + "/" + aux.substring(0, 4);
                dataFim = aux.substring(8, 10) + "/" + aux.substring(5, 7) + "/" + aux.substring(0, 4);
            }
            aux = evento.getDataFim();
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

                if(i == 0)
                    localEventoMarcador = local;

                if (i != locais.size() - 1)
                    local = local + ", ";
            }
            findViewById(R.id.localLabelEvento).setVisibility(View.VISIBLE);

            if(SharedPref.deveTraduzir(sharedPref))
                local = translate(local);

            ((TextView) findViewById(R.id.localEvento)).
                    setText(local);
        }

        //Seta número de participantes do evento
        if (evento.getMostrarparticipantes() == 1) { //Deseja divulgar o número de participantes
            if (evento.getNumeroParticipantes() > 0) {
                findViewById(R.id.participantesLabelEvento).setVisibility(View.VISIBLE);
                Log.i("Texto", evento.getNumeroParticipantes()+"");
                ((TextView) findViewById(R.id.participantesEvento)).
                        setText(evento.getNumeroParticipantes()+"");
            }
        }else{
            findViewById(R.id.participantesLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.participantesEvento)).setText(R.string.detalhes_ilimitado);

        }

        //Seta valor da inscrição
        String text;
        if (evento.getTeminscricao() == 1)
            if (evento.getValorinscricao() == 0){
                ((TextView) findViewById(R.id.taxaIngresso)).
                        setText(R.string.detalhes_gratuito);
            }else{
                Locale locale = new Locale("pt", "BR");
                NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
                Log.i("Texto", evento.getValorinscricao()+"");
                String valor = currencyFormatter.format(evento.getValorinscricao());
                text = valor;
                ((TextView) findViewById(R.id.taxaIngresso)).
                        setText(valor);
            }
        else {
            findViewById(R.id.taxaIngressoLabel).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.taxaIngresso)).
                    setText(R.string.detalhes_sem_inscricao);
        }
        findViewById(R.id.taxaIngressoLabel).setVisibility(View.VISIBLE);

        //Seta local ou link da inscricao
        if (evento.getTeminscricao() == 1){
            findViewById(R.id.localInscricaoLabel).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.localInscricao)).
                    setText(evento.getLinklocalinscricao());
        }

        //Seta público alvo do evento
        if (evento.getPublicoAlvo() != null) {
            text = evento.getPublicoAlvo();
            findViewById(R.id.publicoAlvoLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.publicoAlvoEvento)).
                    setText(text);
        }

        //Seta categorias do evento
        if (evento.getCategorias().size() > 0) {
            List<Categoria> categorias = evento.getCategorias();
            String categoria = "";
            for (int i = 0; i < categorias.size(); i++) {
                int idCat = evento.getCategorias().get(i).getId();
                categoria = categoria + EventosSingleton.getInstance().getCategorias().get(idCat-1).getNome();
                if (i != categorias.size() - 1)
                    categoria = categoria + ", ";
            }
            findViewById(R.id.categoriaLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.categoriaEvento)).
                    setText(categoria);
        }

        //Seta descrição do evento
        if (evento.getDescricao_evento() != ""){
            text = evento.getDescricao_evento();
            findViewById(R.id.descricaoTitulo).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.descricaoEvento)).
                    setText(text);
        }

        //Seta a programação do evento
        if (evento.getProgramacoes().size() > 0){
            List <Programacao> programacoes = new ArrayList<>();
            for (Programacao prog: evento.getProgramacoes()) {
                programacoes.add(prog);
            }

            if(SharedPref.deveTraduzir(sharedPref))
                programacoes = traduzirProg(programacoes);

            RecyclerView myRecyclerView = (RecyclerView) findViewById(R.id.programacaoEvento);
            myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            RecyclerViewProgramacaoAdapter adapter = new RecyclerViewProgramacaoAdapter(getBaseContext(),programacoes);
            myRecyclerView.setAdapter(adapter);
            findViewById(R.id.programacaoTitulo).setVisibility(View.VISIBLE);
        }
    }

    private void initMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View view){
        int i = view.getId();
        //Clicou no botão de adicionar à agenda
        if (i == R.id.addAgenda) {
            //Requisita permissão para escrita
            Permission permission = new Permission();
            permission.requestPermissionCalendar(detalhes_evento_com_descricao.this,this);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                Agenda calendar = new Agenda();
                calendar.addEvent(evento, getBaseContext(), getContentResolver(), getParent());
                Toast.makeText(getBaseContext(), R.string.detalhes_add_agenda, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (isProviderAvailable() && (provider != null)) {
            locateCurrentPosition();
        }

        traceMe(mSourceLatLng,mDestinationLatLng);
    }
    private void locateCurrentPosition() {
        int status = getPackageManager().checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                getPackageName());

        if (status == PackageManager.PERMISSION_GRANTED) {
            locationStart = mLocationManager.getLastKnownLocation(provider);
            updateWithNewLocation(locationStart);
            long minTime = 5000;// ms
            float minDist = 5.0f;// meter
            mLocationManager.requestLocationUpdates(provider, minTime, minDist, this);
        }
    }
    private boolean isProviderAvailable() {
        mLocationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
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

        if (provider != null) {
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

            //TODO: teste para corrigir rota quando obtem atualização da posição
            //traceMe(mSourceLatLng,mDestinationLatLng);

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
    private Marker addMarker(double lat, double lng, String text) {
        return mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat,lng))
                .title(text)
                .flat(false));
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
        updateWithNewLocation(location);
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

                                myMarker = addMarker(locationStart.getLatitude(),
                                        locationStart.getLongitude(),
                                        getResources().getString(R.string.detalhes_minha_localizacao),
                                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

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


    public void getDirection(View view) {
        if (mSourceLatLng != null && mDestinationLatLng != null) {
            traceMe(mSourceLatLng, mDestinationLatLng);
        }
    }

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
        /*Verifica se a terceira parte está aberta*/
        View v = findViewById(R.id.thirdPartDetalhesEvento);
        RelativeLayout.LayoutParams vParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
        boolean terceiraParteEstaAberta = false;
        if (convertPixelsToDp(vParams.height,getBaseContext()) > 60) //Está aberto
            terceiraParteEstaAberta = true;

        //Aumenta ou reduz quadro com informações gerais
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (view.getHeight() < convertDpToPixel((float) 270, getBaseContext())){ // Verifica se está recolhido
            params.height = Math.round(convertDpToPixel((float)270, getBaseContext())); //Abre
            if (terceiraParteEstaAberta)
                vParams.height = Math.round(convertDpToPixel((float) 60, getBaseContext()));
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

    private final class ScrollFunction implements View.OnTouchListener{
        public boolean onTouch(View view, final MotionEvent event){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();

            boolean alterou = false;
            y = event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    yAnterior = y;
                    break;
                case MotionEvent.ACTION_UP:
                    yAnterior = y;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

                    if (lParams.height+(yAnterior-y) < convertDpToPixel((float) 60, getBaseContext())){ //Atingiu a base
                        lParams.height = (int) convertDpToPixel((float) 60, getBaseContext());
                        alterou = true;
                    } else
                        if(lParams.height+(yAnterior-y) > convertDpToPixel((float) 270, getBaseContext())) { //Atingiu o topo
                            lParams.height = (int) convertDpToPixel((float) 270, getBaseContext());
                            alterou = true;
                        } else {
                            if ((lParams.height+(yAnterior-y)) >= convertDpToPixel(60,getBaseContext())
                                    && (lParams.height+(yAnterior-y)) <= convertDpToPixel(270,getBaseContext())) {
                                lParams.height = (int) (lParams.height + (yAnterior - y));
                                alterou = true;
                            }
                        }
                    if (alterou)
                        view.setLayoutParams(lParams);

                    //Controla a primeira parte, no caso, fecha
                    if(lParams.height+(yAnterior-y) > convertDpToPixel((float) 80, getBaseContext())){
                        LinearLayout flayout = (LinearLayout) findViewById(R.id.firstPartDetalhesEvento);
                        FrameLayout.LayoutParams fparams = (FrameLayout.LayoutParams) flayout.getLayoutParams();
                        // Verifica se está recolhido
                        if (view.getHeight() < convertDpToPixel((float) 270, getBaseContext())) {
                            ((ImageView) findViewById(R.id.abreFechaFirstPart)).setImageResource(R.drawable.abrir); //Seta imagem "abrir"
                            fparams.height = Math.round(convertDpToPixel((float) 67.5, getBaseContext())); //Fecha
                            flayout.setLayoutParams(fparams);
                        }

                        LinearLayout rlayout = (LinearLayout) findViewById(R.id.retanguloDetalhesEvento);
                        RelativeLayout.LayoutParams rparams = (RelativeLayout.LayoutParams) rlayout.getLayoutParams();
                        // Verifica se está recolhido
                        if (rparams.height >= convertDpToPixel((float)250, getBaseContext())){
                            rparams.height = Math.round(convertDpToPixel((float)62.5, getBaseContext()));
                            rlayout.setLayoutParams(rparams);
                        }
                    }

                    yAnterior = y;
                    break;
            }
            return true;
        }
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static float getScreenWidth(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return dpWidth;
    }

    public static float getDp(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dp = 1/displayMetrics.density;
        return dp;
    }

    public static float getScreenHeight(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        return dpHeight;
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
                            == PackageManager.PERMISSION_GRANTED){
                        if (googleServicesAvailable())
                            initMap();
                    }
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
                        Agenda calendar = new Agenda();
                        calendar.addEvent(evento, getBaseContext(), getContentResolver(), getParent());
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

    private List<Programacao> traduzirProg(List<Programacao> programacoes) {
        List<String> textosDescricao = new ArrayList<>();
        for(int i = 0; i < programacoes.size(); i++) {
            textosDescricao.add(programacoes.get(i).getDescricaoprog());
        }
        try {
            textosDescricao = new GoogleTranslate().execute(textosDescricao,"pt", Locale.getDefault().getLanguage()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < programacoes.size(); i++) {
            programacoes.get(i).setDescricaoprog(textosDescricao.get(i));
        }

        return programacoes;
    }
}
