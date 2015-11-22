package com.letbyte.callblock.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.letbyte.callblock.BR;
import com.letbyte.callblock.R;
import com.letbyte.callblock.data.model.Block;
import com.letbyte.callblock.data.model.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuc on 9/29/2015.
 */

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final class BindingHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        public BindingHolder(View rowView) {
            super(rowView);
            binding = DataBindingUtil.bind(rowView);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    private List<Model> models;

    public Adapter(List<Model> models) {
        this.models = models;
    }

    public Model getItem(int position) {
        return models.get(position);
    }

    public void addItems(List<Model<?>> models) {
        this.models = new ArrayList<Model>(models);
        notifyDataSetChanged();
    }

    public Model<?> removeItem(int position) {
        Model<?> model = this.models.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int viewResourceId = R.layout.block;

        View rowView = LayoutInflater.from(parent.getContext()).inflate(viewResourceId, parent, false);
        return new BindingHolder(rowView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Model model = getItem(position);
        int viewType = getItemViewType(position);

        if (viewType == Model.Type.BLOCK.ordinal()) {
            Block block = (Block) model.t;

            ((BindingHolder) holder).getBinding().setVariable(BR.block, block);
            ((BindingHolder) holder).getBinding().executePendingBindings();
        }
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type.ordinal();
    }

    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }
}
