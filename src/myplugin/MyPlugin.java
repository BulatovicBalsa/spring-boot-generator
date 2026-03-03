package myplugin;

import java.io.File;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;

import myplugin.generator.IdStrategy;
import myplugin.generator.options.GeneratorOptions;

public class MyPlugin extends com.nomagic.magicdraw.plugins.Plugin {

	public static GeneratorOptions ENTITY_OPTIONS;
	public static GeneratorOptions ENUM_OPTIONS;
	public static GeneratorOptions REPO_OPTIONS;
	public static GeneratorOptions SERVICE_CRUD_OPTIONS;
	public static GeneratorOptions SERVICE_CRUD_IMPL_OPTIONS;
	public static GeneratorOptions CONTROLLER_OPTIONS;

    @Override
	public void init() {
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

		ENUM_OPTIONS = new GeneratorOptions(
			"c:/temp/generated-src",
			"enum",
			"templates",
			"{0}.java",
			true,
			"com.example.generated",
			IdStrategy.UUID
		);

		ENUM_OPTIONS.setTemplateDir(pluginDir + File.separator + ENUM_OPTIONS.getTemplateDir());

		REPO_OPTIONS = new GeneratorOptions(
			"c:/temp/generated-src",
			"repository",     // repository.ftl
			"templates",
			"{0}.java",
			true,
			"com.example.generated.repository",
			IdStrategy.UUID
		);

		REPO_OPTIONS.setTemplateDir(pluginDir + File.separator + REPO_OPTIONS.getTemplateDir());

		SERVICE_CRUD_OPTIONS = new GeneratorOptions(
			"c:/temp/generated-src",
			"service-crud",
			"templates",
			"{0}.java",
			true,
			"com.example.generated.service.crud",
			IdStrategy.UUID
		);

		SERVICE_CRUD_OPTIONS.setTemplateDir(pluginDir + File.separator + SERVICE_CRUD_OPTIONS.getTemplateDir());

		SERVICE_CRUD_IMPL_OPTIONS = new GeneratorOptions(
			"c:/temp/generated-src",
			"service-crud-impl",
			"templates",
			"{0}.java",
			true,
			"com.example.generated.service.crud.impl",
			IdStrategy.UUID
		);

		SERVICE_CRUD_IMPL_OPTIONS.setTemplateDir(pluginDir + File.separator + SERVICE_CRUD_IMPL_OPTIONS.getTemplateDir());

		CONTROLLER_OPTIONS = new GeneratorOptions(
				"c:/temp/generated-src",
				"controller",
				"templates",
				"{0}.java",
				true,
				"com.example.generated.controller",
				IdStrategy.UUID
		);

		CONTROLLER_OPTIONS.setTemplateDir(pluginDir + File.separator + CONTROLLER_OPTIONS.getTemplateDir());
	}

	private NMAction[] getSubmenuActions() {
		return new NMAction[] { new GenerateAction("Generate") };
	}

	@Override public boolean close() { return true; }
	@Override public boolean isSupported() { return true; }
}