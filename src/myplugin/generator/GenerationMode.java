package myplugin.generator;

public enum GenerationMode {
    REST_ONLY("REST only"),
    THYMELEAF_ONLY("Thymeleaf only"),
    REST_AND_THYMELEAF("REST + Thymeleaf");

    private final String label;

    GenerationMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
