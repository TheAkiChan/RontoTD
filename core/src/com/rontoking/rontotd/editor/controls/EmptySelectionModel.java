package com.rontoking.rontotd.editor.controls;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;

public class EmptySelectionModel<T> extends MultipleSelectionModel<T> {

    // since this list is empty and unmodifiable, it's safe to use it as
    // both index and item list
    private ObservableList emptyList = FXCollections.emptyObservableList();

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return emptyList;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return emptyList;
    }

    // just do nothing instead of selecting/unselecting anything

    @Override
    public void selectIndices(int index, int... indices) {
    }

    @Override
    public void selectAll() {
    }

    @Override
    public void selectFirst() {
    }

    @Override
    public void selectLast() {
    }

    @Override
    public void clearAndSelect(int index) {
    }

    @Override
    public void select(int index) {
    }

    @Override
    public void select(T obj) {
    }

    @Override
    public void clearSelection(int index) {
    }

    @Override
    public void clearSelection() {
    }

    @Override
    public void selectPrevious() {
    }

    @Override
    public void selectNext() {
    }

    @Override
    public boolean isSelected(int index) {
        return false; // no items are selected
    }

    @Override
    public boolean isEmpty() {
        return true; // selection is always empty
    }

}