/*
package com.letbyte.contact.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * Created by nuc on 9/29/2015.
 *//*


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
        int viewResourceId = 0;

        View rowView = LayoutInflater.from(parent.getContext()).inflate(viewResourceId, parent, false);
        return new BindingHolder(rowView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Model model = getItem(position);
        int viewType = getItemViewType(position);

        if (viewType == Model.Type.WORD.ordinal()) {
            Word block = (Word) model.t;

           // ((BindingHolder) holder).getBinding().setVariable(BR.block, block);
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

        setSelection(position, selectedItems.get(position, false));
    }

    private void setSelection(int position, boolean selected) {
        if (!isResolved(position)) return;

        final Model model = getItem(position);
        int viewType = getItemViewType(position);

        if (viewType == Model.Type.WORD.ordinal()) {
            Word block = (Word) model.t;
            //block.setSelected(selected);
        }
    }

    private boolean isResolved(int position) {
        return  models.size() > position;
    }

    public void clearSelections() {

        List<Integer> selectedItemPositions = getSelectedItems();

        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            int currentPosition = selectedItemPositions.get(i);

            setSelection(currentPosition, false);
        }

        selectedItems.clear();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public boolean isAnyItemSelected() {
        return getSelectedItemCount() > 0;
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

*/
/*    private boolean isItemChecked(int position) {
        return selectedItems.get(position, false);
    }*//*


    */
/*private boolean mIsSelectable = false;

    private void setItemChecked(int position, boolean isChecked) {
        selectedItems.put(position, isChecked);
    }

    private boolean isItemChecked(int position) {
        return selectedItems.get(position);
    }

    private void setSelectable(boolean selectable) {
        mIsSelectable = selectable;
    }

    private boolean isSelectable() {
        return mIsSelectable;
    }*//*

}
*/
