package com.example.utilitymod.setting;

public class ModeSetting extends Setting {
    private int index;
    public final String[] modes;
    public ModeSetting(String name, String def, String... modes) {
        super(name); this.modes = modes;
        for (int i = 0; i < modes.length; i++) if (modes[i].equals(def)) index = i;
    }
    public String get() { return modes[index]; }
    public boolean is(String m) { return modes[index].equalsIgnoreCase(m); }
    public void cycle() { index = (index + 1) % modes.length; }
}
