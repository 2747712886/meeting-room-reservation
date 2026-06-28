package com.example.reservation.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("meeting_room")
public class MeetingRoom extends BaseEntity {

    private String name;
    private String floor;
    private Integer capacity;
    private Boolean hasProjector;
    private Boolean hasWhiteboard;
    private Boolean enabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getHasProjector() {
        return hasProjector;
    }

    public void setHasProjector(Boolean hasProjector) {
        this.hasProjector = hasProjector;
    }

    public Boolean getHasWhiteboard() {
        return hasWhiteboard;
    }

    public void setHasWhiteboard(Boolean hasWhiteboard) {
        this.hasWhiteboard = hasWhiteboard;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
