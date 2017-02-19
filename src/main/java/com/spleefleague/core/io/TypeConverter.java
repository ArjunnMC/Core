/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.core.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.player.Rank;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author Jonas
 * @param <T>
 * @param <V>
 */
public abstract class TypeConverter<T, V> {

    public abstract V convertLoad(T t);

    public abstract T convertSave(V v);

    //Some common TypeConverters
    public static class UUIDStringConverter extends TypeConverter<String, UUID> {

        @Override
        public String convertSave(UUID t) {
            return t.toString();
        }

        @Override
        public UUID convertLoad(String v) {
            return UUID.fromString(v);
        }
    }

    public static class RankStringConverter extends TypeConverter<String, Rank> {

        @Override
        public String convertSave(Rank t) {
            return t.getName();
        }

        @Override
        public Rank convertLoad(String v) {
            return Rank.valueOf(v);
        }
    }

    public static class DateConverter extends TypeConverter<Date, Date> {

        @Override
        public Date convertSave(Date t) {
            return t;
        }

        @Override
        public Date convertLoad(Date v) {
            return v;
        }
    }

    public static class MapConverter extends TypeConverter<List, Map<String, Object>> {

        @Override
        public Map<String, Object> convertLoad(List t) {
            try {
                Map<String, Object> map = new HashMap<>();
                for (Document doc : (List<Document>) t) {
                    String key = doc.get("value", String.class);
                    Class c = Class.forName(doc.get("class", String.class));
                    map.put(key, EntityBuilder.load(doc, c));
                }
                return map;
            } catch (ClassNotFoundException ex) {
                //Error handling
                Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        @Override
        public List convertSave(Map<String, Object> v) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public static class LocationConverter extends TypeConverter<List, Location> {

        @Override
        public Location convertLoad(List l) {
            List t = new ArrayList<>();
            for(Object o : l) {
                if(o instanceof Long) {
                    t.add(((Long)o).intValue());//Why is this even necessary?
                }
                else {
                    t.add(o);
                }
            }
            double x, y, z;
            float pitch = 0, yaw = 0;
            World world;
            if (t.get(0) instanceof Integer) {
                x = (Integer) t.get(0);
            } else {
                x = (double) t.get(0);
            }
            if (t.get(1) instanceof Integer) {
                y = (Integer) t.get(1);
            } else {
                y = (double) t.get(1);
            }
            if (t.get(2) instanceof Integer) {
                z = (Integer) t.get(2);
            } else {
                z = (double) t.get(2);
            }
            if (t.size() >= 5) {
                if (t.get(3) instanceof Integer) {
                    pitch = ((Integer) t.get(3)).floatValue();
                } else {
                    pitch = ((Double) t.get(3)).floatValue();
                }
                if (t.get(4) instanceof Integer) {
                    yaw = ((Integer) t.get(4)).floatValue();
                } else {
                    yaw = ((Double) t.get(4)).floatValue();
                }
            }
            world = (t.size() % 2 == 0) ? Bukkit.getWorld((String) t.get(t.size() - 1)) : SpleefLeague.DEFAULT_WORLD;
            return t.size() < 5 ? new Location(world, x, y, z) : new Location(world, x, y, z, pitch, yaw);
        }

        @Override
        public List convertSave(Location v) {
            List bdbl = new ArrayList();
            bdbl.add(v.getX());
            bdbl.add(v.getY());
            bdbl.add(v.getZ());
            if (v.getWorld() != SpleefLeague.DEFAULT_WORLD) {
                bdbl.add(v.getWorld().getName());
            }
            return bdbl;
        }
    }
}
