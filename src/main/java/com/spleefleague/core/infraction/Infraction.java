/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.core.infraction;

import java.util.UUID;
import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBLoadable;
import com.spleefleague.entitybuilder.DBSave;
import com.spleefleague.entitybuilder.DBSaveable;
import com.spleefleague.entitybuilder.TypeConverter;

/**
 *
 * @author Manuel
 */
public class Infraction extends DBEntity implements DBLoadable, DBSaveable {

    private UUID uuid, punisher;
    private InfractionType type;
    private long time, duration;
    private String message;

    public Infraction(UUID uuid, UUID punisher, InfractionType type, long time, long duration, String message) {
        this.uuid = uuid;
        this.punisher = punisher;
        this.type = type;
        this.time = time;
        this.duration = duration;
        this.message = message;
    }

    @DBLoad(fieldName = "uuid", typeConverter = TypeConverter.UUIDStringConverter.class)
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @DBLoad(fieldName = "punisher", typeConverter = TypeConverter.UUIDStringConverter.class)
    public void setPunisher(UUID punisher) {
        this.punisher = punisher;
    }

    @DBLoad(fieldName = "type")
    public void setType(InfractionType type) {
        this.type = type;
    }

    @DBLoad(fieldName = "time")
    public void setTime(long time) {
        this.time = time;
    }

    @DBLoad(fieldName = "duration")
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @DBLoad(fieldName = "message")
    public void setMessage(String message) {
        this.message = message;
    }

    @DBSave(fieldName = "uuid", typeConverter = TypeConverter.UUIDStringConverter.class)
    public UUID getUUID() {
        return uuid;
    }

    @DBSave(fieldName = "punisher", typeConverter = TypeConverter.UUIDStringConverter.class)
    public UUID getPunisher() {
        return punisher;
    }

    @DBSave(fieldName = "type")
    public InfractionType getType() {
        return type;
    }

    @DBSave(fieldName = "time")
    public long getTime() {
        return time;
    }

    @DBSave(fieldName = "duration")
    public long getDuration() {
        return duration;
    }

    @DBSave(fieldName = "message")
    public String getMessage() {
        return message;
    }
}
