package org.example.rest;

import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class SandwichDAO
{
    public SandwichDAO() {

    }

    public List<String> getIngredients() {
        List<String> data = new ArrayList<>();
        data.add("bacon");
        data.add("lettuce");
        data.add("tomato");
        return data;
    }
}
