package com.example.frauddetectionsystem.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
@Embeddable
public class TransactionDetails {
    
    @Column
    @Convert(converter = MapConverter.class)
    private Map<String, String> details = new HashMap<>();

    @JsonAnyGetter
    public Map<String, String> getDetails() {
        return details;
    }

    @JsonAnySetter
    public void addDetail(String key, String value) {
        details.put(key, value);
    }
    
    // This ensures the map is properly serialized by Jackson
    @JsonIgnore
    public void setDetails(Map<String, String> details) {
        this.details = details != null ? details : new HashMap<>();
    }
}
