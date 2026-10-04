package com.example.utilitymod.setting;

public class BooleanSetting extends Setting {
    private boolean value;
    public BooleanSetting(String name, boolean def) { super(name); value = def; }
    public boolean get() { return value; }
    public void set(boolean v) { value = v; }
    public void toggle() { value = !value; }
}
