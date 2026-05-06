package myplugin;

import java.awt.event.ActionEvent;
import java.util.List;

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
			List<String> repoWarnings = repoGenerator.getGenerationWarnings();

			String entityPackage = MyPlugin.ENTITY_OPTIONS.getFilePackage();
			String repoPackage = MyPlugin.REPO_OPTIONS.getFilePackage();
			String serviceCrudPackage = MyPlugin.SERVICE_CRUD_OPTIONS.getFilePackage();

			ServiceCrudGenerator crudInterfaceGenerator =
					new ServiceCrudGenerator(MyPlugin.SERVICE_CRUD_OPTIONS, entityPackage, repoPackage);
			crudInterfaceGenerator.generate();

			ServiceCrudGenerator crudImplGenerator =
					new ServiceCrudGenerator(MyPlugin.SERVICE_CRUD_IMPL_OPTIONS, entityPackage, repoPackage, serviceCrudPackage);
			crudImplGenerator.generate();

			ControllerGenerator ctrlGen =
					new ControllerGenerator(MyPlugin.CONTROLLER_OPTIONS, entityPackage, MyPlugin.DTO_OPTIONS.getFilePackage(), serviceCrudPackage);
			ctrlGen.generate();

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

			if (!repoWarnings.isEmpty()) {
				StringBuilder warningMessage = new StringBuilder();
				warningMessage.append("Repository generation warnings:");
				for (String warning : repoWarnings) {
					warningMessage.append("\n- ").append(warning);
				}
				warningMessage.append("\n\nExpected protected markers format:\n");
				warningMessage.append("// <protected:imports>\n");
				warningMessage.append("// your custom imports (e.g. Query, Param)\n");
				warningMessage.append("// </protected:imports>\n\n");
				warningMessage.append("public interface XRepository extends JpaRepository<X, IdType> {\n");
				warningMessage.append("    // <protected:methods>\n");
				warningMessage.append("    // your custom methods\n");
				warningMessage.append("    // </protected:methods>\n");
				warningMessage.append("}\n\n");
				warningMessage.append("Fix protected markers and regenerate.");
				JOptionPane.showMessageDialog(null, warningMessage.toString(), "Generation warnings", JOptionPane.WARNING_MESSAGE);
			}
		} catch (AnalyzeException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Generation failed: " + e.getMessage());
		}
	}
}
