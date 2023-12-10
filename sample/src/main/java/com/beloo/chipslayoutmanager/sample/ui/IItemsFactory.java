package com.beloo.chipslayoutmanager.sample.ui;

import androidx.appcompat.widget.RecyclerView;

import java.util.List;

public interface IItemsFactory<Item> {

    List<Item> getFewItems();

    List<Item> getItems();

    List<Item> getDoubleItems();

    List<Item> getALotOfItems();

    List<Item> getALotOfRandomItems();

    Item createOneItemForPosition(int position);

    RecyclerView.Adapter<? extends RecyclerView.ViewHolder> createAdapter(List<Item> items, OnRemoveListener onRemoveListener);
}
