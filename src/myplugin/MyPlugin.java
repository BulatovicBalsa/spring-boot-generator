package myplugin;

import java.io.File;
import javax.swing.JOptionPane;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;

import myplugin.generator.IdStrategy;
import myplugin.generator.options.GeneratorOptions;

public class MyPlugin extends com.nomagic.magicdraw.plugins.Plugin {

	public static GeneratorOptions ENTITY_OPTIONS;

    @Override
	public void init() {
		JOptionPane.showMessageDialog(null, "My Plugin init");

        String pluginDir = getDescriptor().getPluginDirectory().getPath();

		ActionsConfiguratorsManager manager = ActionsConfiguratorsManager.getInstance();
		manager.addMainMenuConfigurator(new MainMenuConfigurator(getSubmenuActions()));

		ENTITY_OPTIONS = new GeneratorOptions(
				"c:/temp/generated-src",
				"entity",
				"templates",
				"{0}.java",
				true,
				"com.example.generated",
				IdStrategy.UUID
		);

		ENTITY_OPTIONS.setTemplateDir(pluginDir + File.separator + ENTITY_OPTIONS.getTemplateDir());
	}

	private NMAction[] getSubmenuActions() {
		return new NMAction[] { new GenerateAction("Generate") };
	}

	@Override public boolean close() { return true; }
	@Override public boolean isSupported() { return true; }
}