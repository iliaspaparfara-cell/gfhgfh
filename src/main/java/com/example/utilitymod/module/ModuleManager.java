package com.example.utilitymod.module;

import com.example.utilitymod.module.impl.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<Module>();

    public ModuleManager() {
        modules.add(new AimAssist());
        modules.add(new AutoClicker());
        modules.add(new Reach());
        modules.add(new Velocity());
        modules.add(new Sprint());
        modules.add(new Timer());
        modules.add(new FastPlace());
        modules.add(new Eagle());
        modules.add(new Fullbright());
        modules.add(new ESP());
        modules.add(new Tracers());
        modules.add(new ArrayListHUD());
        // enable HUD by default
        get(ArrayListHUD.class).setEnabled(true);
    }

    public List<Module> all() { return modules; }

    public List<Module> byCategory(Category c) {
        List<Module> out = new ArrayList<Module>();
        for (Module m : modules) if (m.getCategory() == c) out.add(m);
        return out;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> cls) {
        for (Module m : modules) if (m.getClass() == cls) return (T) m;
        return null;
    }
}
