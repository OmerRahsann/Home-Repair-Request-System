package homerep.springy.model.error;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ApiErrorModel {
    @JsonProperty
    private final long timestamp;

    @JsonProperty
    private final String type;

    @JsonProperty
    private final String message;

    public ApiErrorModel(String type, String message) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.message = message;
    }

    public long timestamp() {
        return timestamp;
    }

    public String type() {
        return type;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiErrorModel apiError = (ApiErrorModel) o;
        return timestamp == apiError.timestamp && Objects.equals(type, apiError.type) && Objects.equals(message, apiError.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, type, message);
    }
}
