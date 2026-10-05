package com.example.utilitymod;

import com.example.utilitymod.module.Module;
import com.example.utilitymod.setting.*;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import java.io.*;

public class Config {
    private static File file() {
        File f = new File(Minecraft.getMinecraft().mcDataDir, "config/utilitymod.json");
        f.getParentFile().mkdirs();
        return f;
    }

    public static void save() {
        if (UtilityMod.modules == null) return;
        JsonObject root = new JsonObject();
        for (Module m : UtilityMod.modules.all()) {
            JsonObject mo = new JsonObject();
            mo.addProperty("enabled", m.isEnabled());
            mo.addProperty("key", m.getKey());
            JsonObject so = new JsonObject();
            for (Setting s : m.getSettings()) {
                if (s instanceof BooleanSetting) so.addProperty(s.name, ((BooleanSetting) s).get());
                else if (s instanceof NumberSetting) so.addProperty(s.name, ((NumberSetting) s).get());
                else if (s instanceof ModeSetting) so.addProperty(s.name, ((ModeSetting) s).get());
            }
            mo.add("settings", so);
            root.add(m.getName(), mo);
        }
        try {
            Writer w = new OutputStreamWriter(new FileOutputStream(file()), "UTF-8");
            new GsonBuilder().setPrettyPrinting().create().toJson(root, w);
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        File f = file();
        if (!f.exists()) return;
        try {
            Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            JsonObject root = new JsonParser().parse(r).getAsJsonObject();
            r.close();
            for (Module m : UtilityMod.modules.all()) {
                if (!root.has(m.getName())) continue;
                JsonObject mo = root.getAsJsonObject(m.getName());
                if (mo.has("key")) m.setKey(mo.get("key").getAsInt());
                if (mo.has("settings")) {
                    JsonObject so = mo.getAsJsonObject("settings");
                    for (Setting s : m.getSettings()) {
                        if (!so.has(s.name)) continue;
                        JsonElement el = so.get(s.name);
                        if (s instanceof BooleanSetting) ((BooleanSetting) s).set(el.getAsBoolean());
                        else if (s instanceof NumberSetting) ((NumberSetting) s).set(el.getAsDouble());
                        else if (s instanceof ModeSetting) ((ModeSetting) s).set(el.getAsString());
                    }
                }
                if (mo.has("enabled")) m.setEnabled(mo.get("enabled").getAsBoolean());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
