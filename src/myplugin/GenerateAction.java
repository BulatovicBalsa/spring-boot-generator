package myplugin;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import myplugin.analyzer.AnalyzeException;
import myplugin.analyzer.ModelAnalyzer;
import myplugin.generator.*;

class GenerateAction extends MDAction {

	public GenerateAction(String name) {
		super("", name, null, null);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (Application.getInstance().getProject() == null) return;
		Package root = Application.getInstance().getProject().getModel();
		if (root == null) return;

		try {
			ModelAnalyzer analyzer = new ModelAnalyzer(root);
			analyzer.prepareModel();

			EntityGenerator generator = new EntityGenerator(MyPlugin.ENTITY_OPTIONS);
			generator.generate();

			EnumGenerator enumGenerator = new EnumGenerator(MyPlugin.ENUM_OPTIONS);
			enumGenerator.generate();

			RepositoryGenerator repoGenerator = new RepositoryGenerator(MyPlugin.REPO_OPTIONS);
			repoGenerator.generate();

			String entityPackage = MyPlugin.ENTITY_OPTIONS.getFilePackage();
			String repoPackage = MyPlugin.REPO_OPTIONS.getFilePackage();
			String serviceCrudPackage = MyPlugin.SERVICE_CRUD_OPTIONS.getFilePackage();

			ServiceCrudGenerator crudInterfaceGenerator =
					new ServiceCrudGenerator(MyPlugin.SERVICE_CRUD_OPTIONS, entityPackage, repoPackage);
			crudInterfaceGenerator.generate();

			ServiceCrudGenerator crudImplGenerator =
					new ServiceCrudGenerator(MyPlugin.SERVICE_CRUD_IMPL_OPTIONS, entityPackage, repoPackage);
			crudImplGenerator.generate();

			ControllerGenerator ctrlGen =
					new ControllerGenerator(MyPlugin.CONTROLLER_OPTIONS, entityPackage, serviceCrudPackage);
			ctrlGen.generate();

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