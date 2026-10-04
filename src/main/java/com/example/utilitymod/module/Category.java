package com.example.utilitymod.module;

public enum Category {
    COMBAT("Combat"), MOVEMENT("Movement"), PLAYER("Player"), RENDER("Render"), WORLD("World");
    public final String label;
    Category(String l) { label = l; }
}
