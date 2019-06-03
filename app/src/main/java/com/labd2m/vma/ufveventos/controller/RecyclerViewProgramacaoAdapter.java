package com.labd2m.vma.ufveventos.controller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.labd2m.vma.ufveventos.R;
import com.labd2m.vma.ufveventos.model.Categoria;
import com.labd2m.vma.ufveventos.model.Programacao;

import java.util.List;

/**
 * Created by vma on 08/09/2017.
 */

public class RecyclerViewProgramacaoAdapter extends RecyclerView.Adapter<RecyclerViewProgramacaoAdapter.CustomViewHolder> {
    private List<Programacao> programacoes;
    private Context mContext;

    public RecyclerViewProgramacaoAdapter(Context context, List<Programacao> programacoes) {
        this.programacoes = programacoes;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.programacao_row, viewGroup,false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final Programacao programacao = programacoes.get(i);

        //Seta nome da categoria
        String data = programacao.getDatainicioprog().substring(8,10)+"/"+programacao.getDatainicioprog().substring(5,7)+
                " a "+programacao.getDatafimprog().substring(8,10)+"/"+programacao.getDatafimprog().substring(5,7);
        String hora = programacao.getHorainicioprog().substring(0, 5)+" às "+programacao.getHorafimprog().substring(0, 5);
        customViewHolder.data.setText(data);
        customViewHolder.hora.setText(hora);
        customViewHolder.descricao.setText(programacao.getDescricaoprog());
    }

    @Override
    public int getItemCount() {
        return (null != programacoes ? programacoes.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView data,hora,descricao;

        public CustomViewHolder(View view) {
            super(view);
            this.hora = (TextView) view.findViewById(R.id.horaProgramacaoRow);
            this.data = (TextView) view.findViewById(R.id.dataProgramacaoRow);
            this.descricao = (TextView) view.findViewById(R.id.descricaoProgramacaoRow);
        }
    }
}