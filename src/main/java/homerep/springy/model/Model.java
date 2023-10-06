package homerep.springy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Model {
    @JsonIgnore
    boolean isValid();
}
