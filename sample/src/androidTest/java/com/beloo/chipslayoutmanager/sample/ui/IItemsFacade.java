package com.beloo.chipslayoutmanager.sample.ui;

import androidx.appcompat.widget.RecyclerView;

import java.util.List;

public interface IItemsFacade<Item> {
    List<Item> getItems();
    RecyclerView.Adapter<? extends RecyclerView.ViewHolder> createAdapter(List<Item> chipsEntities, OnRemoveListener onRemoveListener);
    Item createOneItemForPosition(int position);
}
