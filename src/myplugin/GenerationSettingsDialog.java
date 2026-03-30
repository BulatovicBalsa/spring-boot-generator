package myplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import myplugin.generator.IdStrategy;
import myplugin.generator.options.GeneratorOptions;

public final class GenerationSettingsDialog {

    private GenerationSettingsDialog() {
    }

    public static boolean configure(Component parent) {
        JTextField projectPathField = new JTextField(MyPlugin.PROJECT_OPTIONS.getOutputPath(), 40);

        String javaDefault = MyPlugin.ENTITY_OPTIONS.getOutputPath();
        if (isBlank(javaDefault)) {
            javaDefault = projectPathField.getText().trim() + "/src/main/java";
        }
        JTextField javaOutputField = new JTextField(javaDefault, 40);

        String basePackageDefault = MyPlugin.APPLICATION_OPTIONS.getFilePackage();
        if (isBlank(basePackageDefault)) {
            basePackageDefault = "com.example.generated";
        }
        JTextField basePackageField = new JTextField(basePackageDefault, 40);
        JTextField dtoPackageField = new JTextField(MyPlugin.DTO_OPTIONS.getFilePackage(), 40);

        IdStrategy idDefault = MyPlugin.ENTITY_OPTIONS.getIdStrategy();
        if (idDefault == null) {
            idDefault = IdStrategy.UUID;
        }
        JComboBox<IdStrategy> idStrategyBox = new JComboBox<IdStrategy>(IdStrategy.values());
        idStrategyBox.setSelectedItem(idDefault);

        boolean overwriteDefault = isOverwriteEnabled(MyPlugin.ENTITY_OPTIONS);
        JCheckBox overwriteBox = new JCheckBox("Overwrite generated files", overwriteDefault);

        while (true) {
            String basePackagePreview = basePackageField.getText().trim();
            if (isBlank(basePackagePreview)) {
                basePackagePreview = "com.example.generated";
            }

            JPanel activePanel = new JPanel(new GridLayout(0, 1, 6, 6));
            activePanel.setBorder(BorderFactory.createTitledBorder("Active Options"));
            activePanel.add(new JLabel("Project output folder (root):"));
            activePanel.add(projectPathField);
            activePanel.add(new JLabel("Java output folder (for generated .java files):"));
            activePanel.add(javaOutputField);
            activePanel.add(new JLabel("Base package namespace (for generated code):"));
            activePanel.add(basePackageField);
            activePanel.add(new JLabel("DTO package namespace:"));
            activePanel.add(dtoPackageField);
            activePanel.add(new JLabel("Default ID strategy:"));
            activePanel.add(idStrategyBox);
            activePanel.add(overwriteBox);

            JPanel legacyPanel = new JPanel(new GridLayout(0, 1, 6, 6));
            legacyPanel.setBorder(BorderFactory.createTitledBorder("TODO Features Options (disabled for now)"));
            legacyPanel.add(new JLabel("Main class package"));
            legacyPanel.add(disabledField(basePackagePreview));
            legacyPanel.add(new JLabel("Domain/entity package"));
            legacyPanel.add(disabledField(basePackagePreview + ".domain"));
            legacyPanel.add(new JLabel("Enum package"));
            legacyPanel.add(disabledField(basePackagePreview + ".enumeration"));
            legacyPanel.add(new JLabel("Repository package"));
            legacyPanel.add(disabledField(basePackagePreview + ".repository"));
            legacyPanel.add(new JLabel("Service package"));
            legacyPanel.add(disabledField(basePackagePreview + ".service"));
            legacyPanel.add(new JLabel("Service implementation package"));
            legacyPanel.add(disabledField(basePackagePreview + ".service.impl"));
            legacyPanel.add(new JLabel("Controller package"));
            legacyPanel.add(disabledField(basePackagePreview + ".controller"));
            legacyPanel.add(new JLabel("View templates output root"));
            legacyPanel.add(disabledField(projectPathField.getText().trim() + "/src/main/resources/templates"));
            legacyPanel.add(new JLabel("POM output file"));
            legacyPanel.add(disabledField(projectPathField.getText().trim() + "/pom.xml"));
            legacyPanel.add(new JLabel("application.properties output file"));
            legacyPanel.add(disabledField(projectPathField.getText().trim() + "/src/main/resources/application.properties"));
            legacyPanel.add(disabledCheck("Generate HTML views (index/edit/new)", false));
            legacyPanel.add(disabledCheck("Generate pom.xml from template", false));
            legacyPanel.add(disabledCheck("Generate application.properties from template", false));

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.add(activePanel);
            content.add(legacyPanel);

            JScrollPane scrollPane = new JScrollPane(content);
            scrollPane.setPreferredSize(new Dimension(760, 620));

            int result = JOptionPane.showConfirmDialog(
                    parent,
                    scrollPane,
                    "Spring Boot Generator Settings",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return false;
            }

            String projectPath = projectPathField.getText().trim();
            String javaOutputPath = javaOutputField.getText().trim();
            String basePackage = basePackageField.getText().trim();
            String dtoPackage = dtoPackageField.getText().trim();
            IdStrategy idStrategy = (IdStrategy) idStrategyBox.getSelectedItem();
            boolean overwrite = overwriteBox.isSelected();

            if (isBlank(projectPath)) {
                showValidation(parent, "Project output folder is required.");
                continue;
            }
            if (isBlank(javaOutputPath)) {
                showValidation(parent, "Java output folder is required.");
                continue;
            }
            if (!isValidPackage(basePackage)) {
                showValidation(parent, "Base package is not valid (example: com.example.generated).");
                continue;
            }
            if (!isValidPackage(dtoPackage)) {
                showValidation(parent, "DTO package is not valid (example: com.example.generated.dto).");
                continue;
            }

            applySettings(projectPath, javaOutputPath, basePackage, dtoPackage, idStrategy, overwrite);
            return true;
        }
    }

    private static void applySettings(
            String projectPath,
            String javaOutputPath,
            String basePackage,
            String dtoPackage,
            IdStrategy idStrategy,
            boolean overwrite
    ) {
        configureCodeOptions(MyPlugin.ENTITY_OPTIONS, javaOutputPath, basePackage + ".domain", idStrategy, overwrite);
        configureCodeOptions(MyPlugin.ENUM_OPTIONS, javaOutputPath, basePackage + ".enumeration", idStrategy, overwrite);
        configureCodeOptions(MyPlugin.DTO_OPTIONS, javaOutputPath, dtoPackage, idStrategy, overwrite);
        configureCodeOptions(MyPlugin.REPO_OPTIONS, javaOutputPath, basePackage + ".repository", idStrategy, overwrite);
        configureCodeOptions(MyPlugin.SERVICE_CRUD_OPTIONS, javaOutputPath, basePackage + ".service.crud", idStrategy, overwrite);
        configureCodeOptions(MyPlugin.SERVICE_CRUD_IMPL_OPTIONS, javaOutputPath, basePackage + ".service.crud.impl", idStrategy, overwrite);
        configureCodeOptions(MyPlugin.CONTROLLER_OPTIONS, javaOutputPath, basePackage + ".controller", idStrategy, overwrite);
        configureCodeOptions(MyPlugin.APPLICATION_OPTIONS, javaOutputPath, basePackage, idStrategy, overwrite);

        MyPlugin.PROJECT_OPTIONS.setOutputPath(projectPath);
    }

    private static void configureCodeOptions(
            GeneratorOptions options,
            String outputPath,
            String filePackage,
            IdStrategy idStrategy,
            boolean overwrite
    ) {
        options.setOutputPath(outputPath);
        options.setFilePackage(filePackage);
        options.setIdStrategy(idStrategy);
        options.setOverwrite(overwrite);
    }

    private static boolean isOverwriteEnabled(GeneratorOptions options) {
        return options.getOverwrite() != null && options.getOverwrite();
    }

    private static JTextField disabledField(String value) {
        JTextField field = new JTextField(value, 40);
        field.setEnabled(false);
        return field;
    }

    private static JPanel disabledCheck(String text, boolean selected) {
        JPanel panel = new JPanel(new BorderLayout());
        JCheckBox checkBox = new JCheckBox(text, selected);
        checkBox.setEnabled(false);
        panel.add(checkBox, BorderLayout.WEST);
        return panel;
    }

    private static boolean isValidPackage(String value) {
        if (isBlank(value)) {
            return false;
        }
        String[] parts = value.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!part.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void showValidation(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Invalid settings", JOptionPane.WARNING_MESSAGE);
    }
}
