package com.labd2m.vma.ufveventos.controller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.labd2m.vma.ufveventos.R;
import com.labd2m.vma.ufveventos.model.Categoria;
import com.labd2m.vma.ufveventos.util.GoogleTranslate;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static com.labd2m.vma.ufveventos.R.drawable.v;

/**
 * Created by vma on 08/09/2017.
 */

public class RecyclerViewCategoriasAdapter extends RecyclerView.Adapter<RecyclerViewCategoriasAdapter.CustomViewHolder> {
    private List<Categoria> categorias;
    private List<String> cat_pref;
    private Context mContext;

    public RecyclerViewCategoriasAdapter(Context context, List<Categoria> categorias, List<String> cat_pref) {
        this.categorias = categorias;
        this.cat_pref = cat_pref;
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.categoria_row, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder customViewHolder, int i) {
        final Categoria categoria = categorias.get(i);

        //Seta nome da categoria
        customViewHolder.nome.setText(categoria.getNome());
        customViewHolder.idCategoria.setText(String.valueOf(categoria.getId()));
        if (cat_pref.contains(""+categoria.getId()))
            customViewHolder.checkBox.setChecked(true);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCategoriaClickListener.onItemClick(categoria);
            }
        };

        customViewHolder.itemView.setOnClickListener(listener);

        customViewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked())
                    cat_pref.add(""+categoria.getId());
                else
                    cat_pref.remove(""+categoria.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != categorias ? categorias.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView nome;
        protected TextView idCategoria;
        protected CheckBox checkBox;

        public CustomViewHolder(View view) {
            super(view);
            this.nome = (TextView) view.findViewById(R.id.nomeCategoriaRow);
            this.idCategoria = (TextView) view.findViewById(R.id.idCategoriaRow);
            this.checkBox = (CheckBox) view.findViewById(R.id.checkBoxCategoriaRow);
        }
    }
    private OnCategoriaClickListener onCategoriaClickListener;

    public OnCategoriaClickListener getOnCategoriaClickListener() {
        return onCategoriaClickListener;
    }

    public void setCategoriaClickListener(OnCategoriaClickListener onCategoriaClickListener) {
        this.onCategoriaClickListener = onCategoriaClickListener;
    }
}