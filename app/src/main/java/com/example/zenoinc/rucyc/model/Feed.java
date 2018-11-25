package com.example.zenoinc.rucyc.model;

import java.util.ArrayList;
import java.util.List;

public class Feed {
    private List<String> history;

    public Feed(List<String> data){
        history = new ArrayList<>();
        history.addAll(data);
    }

    public List<String> getHistory(){
        return history;
    }

    public void setHistory(List<String> history){
        this.history = history;
    }
}
