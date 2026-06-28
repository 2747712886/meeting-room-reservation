package com.example.reservation.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.reservation.domain.enums.AppointmentStatus;

@TableName("appointment_log")
public class AppointmentLog extends BaseEntity {

    private Long appointmentId;
    private AppointmentStatus oldStatus;
    private AppointmentStatus newStatus;
    private Long operatorId;
    private String remark;

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public AppointmentStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(AppointmentStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public AppointmentStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(AppointmentStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
