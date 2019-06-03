package com.labd2m.vma.ufveventos.controller;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.labd2m.vma.ufveventos.R;
import com.labd2m.vma.ufveventos.model.Evento;
import com.labd2m.vma.ufveventos.util.FormatStrings;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by vma on 08/09/2017.
 */

public class RecyclerViewEventosTelaInicialAdapter extends RecyclerView.Adapter<RecyclerViewEventosTelaInicialAdapter.CustomViewHolder> {
    private List<Evento> eventos;
    private Context mContext;
    private Resources resources;

    public RecyclerViewEventosTelaInicialAdapter(Context context, List<Evento> eventos,Resources resources) {
        this.eventos = eventos;
        this.mContext = context;
        this.resources = resources;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.evento_row, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final Evento evento = eventos.get(i);
        //Seta imagem do evento
        char primeiraLetra = evento.getDenominacao().toLowerCase().charAt(0);
        switch (primeiraLetra){
            case 'a':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.a));
                customViewHolder.letra.setText("A");
                break;
            case 'b':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.b));
                customViewHolder.letra.setText("B");
                break;
            case 'c':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.c));
                customViewHolder.letra.setText("C");
                break;
            case 'd':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.d));
                customViewHolder.letra.setText("D");
                break;
            case 'e':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.e));
                customViewHolder.letra.setText("E");
                break;
            case 'f':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.f));
                customViewHolder.letra.setText("F");
                break;
            case 'g':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.g));
                customViewHolder.letra.setText("G");
                break;
            case 'h':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.h));
                customViewHolder.letra.setText("H");
                break;
            case 'i':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.i));
                customViewHolder.letra.setText("I");
                break;
            case 'j':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.j));
                customViewHolder.letra.setText("J");
                break;
            case 'k':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.k));
                customViewHolder.letra.setText("K");
                break;
            case 'l':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.l));
                customViewHolder.letra.setText("L");
                break;
            case 'm':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.m));
                customViewHolder.letra.setText("M");
                break;
            case 'n':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.n));
                customViewHolder.letra.setText("N");
                break;
            case 'o':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.o));
                customViewHolder.letra.setText("O");
                break;
            case 'p':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.p));
                customViewHolder.letra.setText("P");
                break;
            case 'q':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.q));
                customViewHolder.letra.setText("Q");
                break;
            case 'r':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.r));
                customViewHolder.letra.setText("R");
                break;
            case 's':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.s));
                customViewHolder.letra.setText("S");
                break;
            case 't':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.t));
                customViewHolder.letra.setText("T");
                break;
            case 'u':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.u));
                customViewHolder.letra.setText("U");
                break;
            case 'v':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.v));
                customViewHolder.letra.setText("V");
                break;
            case 'w':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.w));
                customViewHolder.letra.setText("W");
                break;
            case 'x':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.x));
                customViewHolder.letra.setText("X");
                break;
            case 'y':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.y));
                customViewHolder.letra.setText("Y");
                break;
            case 'z':
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.z));
                customViewHolder.letra.setText("Z");
                break;
            default:
                ((GradientDrawable) customViewHolder.circulo.getBackground()).setColor(resources.getColor(R.color.outros));
                customViewHolder.letra.setText("");
                break;
        }


        //Seta denominação do evento
        FormatStrings fs = new FormatStrings();
        customViewHolder.denominacao.setText(fs.cortaTituloEvento(evento.getDenominacao()));
        //Seta data do evento
        String data;
        String locale = mContext.getResources().getConfiguration().locale.getCountry();
        if(locale.equals("US")) {
            data = evento.getDataInicio().substring(5,7)+"/"+evento.getDataInicio().substring(8,10);
        } else {
            data = evento.getDataInicio().substring(8,10)+"/"+evento.getDataInicio().substring(5,7);
        }
        customViewHolder.data.setText(data);
        //Seta horário de início e fim do evento
        customViewHolder.horario.setText(mContext.getString(R.string.evento_row_lb_horario)  + evento.getHoraInicio().substring(0,5)+" - "+evento.getHoraFim().substring(0,5));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEventoTelaInicialClickListener.onItemClick(evento);
            }
        };

        customViewHolder.itemView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return (null != eventos ? eventos.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected RelativeLayout circulo;
        protected TextView denominacao,horario,data,letra;

        public CustomViewHolder(View view) {
            super(view);
            this.circulo = (RelativeLayout) view.findViewById(R.id.circuloEventoRow);
            this.letra = (TextView) view.findViewById(R.id.letraEventoRow);
            this.denominacao = (TextView) view.findViewById(R.id.denominacaoEventoRow);
            this.horario = (TextView) view.findViewById(R.id.horarioEventoRow);
            this.data = (TextView) view.findViewById(R.id.dataEventoRow);
        }
    }

    private OnEventoTelaInicialClickListener onEventoTelaInicialClickListener;

    public OnEventoTelaInicialClickListener getOnEventoTelaInicialClickListener() {
        return onEventoTelaInicialClickListener;
    }

    public void setOnEventoTelaInicialClickListener(OnEventoTelaInicialClickListener onEventoTelaInicialClickListener) {
        this.onEventoTelaInicialClickListener = onEventoTelaInicialClickListener;
    }
}