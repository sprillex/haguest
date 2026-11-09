package org.haguest;

import java.io.Serializable;
import java.util.Objects;

public class Device implements Serializable {
    public enum Type {
        LIGHT("l"),
        CLIMATE("c");

        private final String code;

        Type(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static Type fromCode(String code) {
            if ("c".equalsIgnoreCase(code)) {
                return CLIMATE;
            }
            return LIGHT; // default to light
        }
    }

    private final Type type;
    private final String id;
    private final String label;

    public Device(Type type, String id, String label) {
        this.type = type != null ? type : Type.LIGHT;
        this.id = id != null ? id : "";
        this.label = label != null ? label : "";
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return type == device.type && Objects.equals(id, device.id) && Objects.equals(label, device.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, label);
    }

    @Override
    public String toString() {
        return "Device{" +
                "type=" + type +
                ", id='" + id + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
