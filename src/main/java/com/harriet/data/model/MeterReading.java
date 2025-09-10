package com.harriet.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name="meter_readings")
public class MeterReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nmi;
    private LocalDateTime timestamp;
    private BigDecimal consumption;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNmi() {
        return nmi;
    }

    public void setNmi(String nmi) {
        this.nmi = nmi;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getConsumption() {
        return consumption;
    }

    public void setConsumption(BigDecimal consumption) {
        this.consumption = consumption;
    }

    @Override
    public String toString() {
        return "Reading{" +
                "id=" + id +
                ", nmi='" + nmi + '\'' +
                ", timestamp=" + timestamp +
                ", consumption=" + consumption +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeterReading)) return false;
        MeterReading reading = (MeterReading) o;
        return Objects.equals(id, reading.id) &&
                Objects.equals(nmi, reading.nmi) &&
                Objects.equals(timestamp, reading.timestamp) &&
                Objects.equals(consumption, reading.consumption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nmi, timestamp, consumption);
    }

}

