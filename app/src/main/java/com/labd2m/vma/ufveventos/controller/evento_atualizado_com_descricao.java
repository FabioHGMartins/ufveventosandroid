package com.labd2m.vma.ufveventos.controller;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.labd2m.vma.ufveventos.R;
import com.labd2m.vma.ufveventos.model.Evento;
import com.labd2m.vma.ufveventos.model.Local;
import com.labd2m.vma.ufveventos.model.Programacao;
import com.labd2m.vma.ufveventos.util.Permission;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class evento_atualizado_com_descricao extends AppCompatActivity implements OnMapReadyCallback, LocationListener{
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Captura evento solicitado
        String eventoJson = getIntent().getStringExtra("evento");
        Gson gson = new Gson();
        evento = gson.fromJson(eventoJson, Evento.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evento_atualizado_com_descricao);

        LinearLayout thirdPart = (LinearLayout) findViewById(R.id.thirdPartDetalhesEvento);
        thirdPart.setOnTouchListener(new ScrollFunction());

        //Google Analytics
        MyApplication application = (MyApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("evento_atualizado_com_descricao");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        //Traça rota
        List<Local> locaisAux = evento.getLocais();
        double latDest = Double.parseDouble(locaisAux.get(0).getLatitude());
        double lngDest = Double.parseDouble(locaisAux.get(0).getLongitude());
        mDestinationLatLng = new LatLng(latDest, lngDest);
        mSourceLatLng = new LatLng(latDest, lngDest);

        //Requisita permissão para mapas
        Permission permission = new Permission();
        permission.requestPermissionMaps(evento_atualizado_com_descricao.this,this);

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
            String dataInicio = evento.getDataInicio();
            String dataFim = evento.getDataFim();
            findViewById(R.id.dataLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.dataEvento)).
                    setText(dataInicio + " à " + dataFim);
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
            findViewById(R.id.localLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.localEvento)).
                    setText(local);
        }

        //Seta número de participantes do evento
        if (evento.getMostrarparticipantes() == 1) { //Deseja divulgar o número de participantes
            if (evento.getNumeroParticipantes() > 0) {
                findViewById(R.id.participantesLabelEvento).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.participantesEvento)).
                        setText(String.valueOf(evento.getNumeroParticipantes()));
            }
        }else{
            findViewById(R.id.participantesLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.participantesEvento)).
                    setText("Ilimitado");
        }
        //Seta valor da inscrição
        if (evento.getTeminscricao() == 1)
            if (evento.getValorinscricao() == 0){
                findViewById(R.id.taxaIngressoLabel).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.taxaIngresso)).
                        setText("Gratuito");
            }else{
                String valor = String.format( "%.2f",evento.getValorinscricao());
                findViewById(R.id.taxaIngressoLabel).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.taxaIngresso)).
                        setText("R$"+valor);
            }
        else {
            findViewById(R.id.taxaIngressoLabel).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.taxaIngresso)).
                    setText("Não é necessário realizar inscrição.");
        }

        //Seta local ou link da inscricao
        if (evento.getTeminscricao() == 1){
            findViewById(R.id.localInscricaoLabel).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.localInscricao)).
                    setText(evento.getLinklocalinscricao());
        }
        //Seta público alvo do evento
        if (evento.getPublicoAlvo() != null) {
            findViewById(R.id.publicoAlvoLabelEvento).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.publicoAlvoEvento)).
                    setText(evento.getPublicoAlvo());
        }
        //Seta descrição do evento
        if (evento.getDescricao_evento() != ""){
            findViewById(R.id.descricaoTitulo).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.descricaoEvento)).
                    setText(evento.getDescricao_evento());

        }
        //Seta a programação do evento
        if (evento.getProgramacoes().size() > 0){
            List <Programacao> programacoes = new ArrayList<>();
            programacoes = evento.getProgramacoes();
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
            Location location = mLocationManager.getLastKnownLocation(provider);
            updateWithNewLocation(location);
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

            CameraPosition camPosition = new CameraPosition.Builder()
                    .target(new LatLng(lat, lng)).zoom(14f).build();

            if (mGoogleMap != null) {
                mGoogleMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(camPosition));
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)
                    mGoogleMap.setMyLocationEnabled(true);
            }
        } else {
            Log.d("Location error", "Something went wrong");
        }
    }
    private void addBoundaryToCurrentPosition(double lat, double lang) {
        MarkerOptions mMarkerOptions = new MarkerOptions();
        mMarkerOptions.position(new LatLng(lat, lang));
        mMarkerOptions.anchor(0.5f, 0.5f);
        mCurrentPosition = mGoogleMap.addMarker(mMarkerOptions);
    }
    private void addMarker(double lat, double lng, String text) {
        mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat,lng))
                .title(text)
                .flat(true));
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

                            //Adiciona marcador à posição final
                            HashMap<String, String> pointAux = path.get(path.size()-1);
                            Double latAux = Double.parseDouble(pointAux.get("lat"));
                            Double lngAux = Double.parseDouble(pointAux.get("lng"));
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
            Toast.makeText(getBaseContext(),"Não foi possível conectar.",Toast.LENGTH_SHORT).show();
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
        }
    }
}
