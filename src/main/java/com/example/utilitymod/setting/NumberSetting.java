package com.example.utilitymod.setting;

public class NumberSetting extends Setting {
    private double value;
    public final double min, max, inc;
    public NumberSetting(String name, double def, double min, double max, double inc) {
        super(name); this.value = def; this.min = min; this.max = max; this.inc = inc;
    }
    public double get() { return value; }
    public float getF() { return (float) value; }
    public void set(double v) {
        v = Math.max(min, Math.min(max, v));
        v = Math.round(v / inc) * inc;
        value = Math.max(min, Math.min(max, v));
    }
}
