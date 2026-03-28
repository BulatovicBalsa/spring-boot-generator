package myplugin;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import myplugin.analyzer.AnalyzeException;
import myplugin.analyzer.ModelAnalyzer;
import myplugin.generator.*;
import myplugin.generator.options.GeneratorOptions;

class GenerateAction extends MDAction {

	public GenerateAction(String name) {
		super("", name, null, null);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (Application.getInstance().getProject() == null) return;
		Package root = Application.getInstance().getProject().getModel();
		if (root == null) return;
		if (!GenerationSettingsDialog.configure(null)) return;

		try {
			ModelAnalyzer analyzer = new ModelAnalyzer(root);
			analyzer.prepareModel();

			EntityGenerator generator = new EntityGenerator(MyPlugin.ENTITY_OPTIONS);
			generator.generate();

			EnumGenerator enumGenerator = new EnumGenerator(MyPlugin.ENUM_OPTIONS);
			enumGenerator.generate();

			DTOGenerator dtoGenerator = new DTOGenerator(MyPlugin.DTO_OPTIONS);
			dtoGenerator.generate();

			RepositoryGenerator repoGenerator = new RepositoryGenerator(MyPlugin.REPO_OPTIONS);
			repoGenerator.generate();

			String entityPackage = MyPlugin.ENTITY_OPTIONS.getFilePackage();
			String repoPackage = MyPlugin.REPO_OPTIONS.getFilePackage();
			String serviceCrudPackage = MyPlugin.SERVICE_CRUD_OPTIONS.getFilePackage();

			ServiceCrudGenerator crudInterfaceGenerator =
					new ServiceCrudGenerator(MyPlugin.SERVICE_CRUD_OPTIONS, entityPackage, repoPackage);
			crudInterfaceGenerator.generate();

			ServiceCrudGenerator crudImplGenerator =
					new ServiceCrudGenerator(MyPlugin.SERVICE_CRUD_IMPL_OPTIONS, entityPackage, repoPackage, serviceCrudPackage);
			crudImplGenerator.generate();

			GenerationMode mode = MyPlugin.CONTROLLER_OPTIONS.getGenerationMode();
			if (mode == null) {
				mode = GenerationMode.REST_ONLY;
			}

			if (mode == GenerationMode.REST_ONLY || mode == GenerationMode.REST_AND_THYMELEAF) {
				ControllerGenerator ctrlGen =
						new ControllerGenerator(MyPlugin.CONTROLLER_OPTIONS, entityPackage, MyPlugin.DTO_OPTIONS.getFilePackage(), serviceCrudPackage);
				ctrlGen.generate();
			}

			if (mode == GenerationMode.THYMELEAF_ONLY || mode == GenerationMode.REST_AND_THYMELEAF) {
				GeneratorOptions viewControllerOptions = new GeneratorOptions(
						MyPlugin.CONTROLLER_OPTIONS.getOutputPath(),
						"controller-view",
						MyPlugin.CONTROLLER_OPTIONS.getTemplateDir(),
						"{0}PageController.java",
						MyPlugin.CONTROLLER_OPTIONS.getOverwrite(),
						MyPlugin.CONTROLLER_OPTIONS.getFilePackage(),
						MyPlugin.CONTROLLER_OPTIONS.getIdStrategy(),
						mode
				);

				ViewControllerGenerator viewCtrlGen =
						new ViewControllerGenerator(viewControllerOptions, entityPackage, MyPlugin.DTO_OPTIONS.getFilePackage(), serviceCrudPackage);
				viewCtrlGen.generate();
			}

			ProjectGenerator projectGenerator =
					new ProjectGenerator(MyPlugin.PROJECT_OPTIONS.getTemplateDir(), MyPlugin.PROJECT_OPTIONS.getOutputPath());

			projectGenerator.generate();

			ApplicationGenerator appGenerator = new ApplicationGenerator(MyPlugin.APPLICATION_OPTIONS);
			appGenerator.generate();

			JOptionPane.showMessageDialog(
					null,
					"Entities generated to: " + MyPlugin.ENTITY_OPTIONS.getOutputPath() +
							"\npackage: " + MyPlugin.ENTITY_OPTIONS.getFilePackage()
			);
		} catch (AnalyzeException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Generation failed: " + e.getMessage());
		}
	}
}
