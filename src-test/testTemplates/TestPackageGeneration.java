package testTemplates;

	import org.junit.Before;
	import org.junit.Test;
	import java.util.List;

	import myplugin.generator.EJBGenerator;
	import myplugin.generator.fmmodel.FMClass;
	import myplugin.generator.fmmodel.FMModel;
	import myplugin.generator.fmmodel.FMProperty;
	import myplugin.generator.options.GeneratorOptions;
	import myplugin.generator.options.ProjectOptions;

	/**
	 * TestPackageGeneration: Unit tests for package generation
	 */
	public class TestPackageGeneration {

		@Before
		public void setUp() {
			initModel();
			GeneratorOptions ejbOptions = new GeneratorOptions("c:/temp", "ejbclass", "./resources/templates/", "{0}.java", true, "ejb");
			ProjectOptions.getProjectOptions().getGeneratorOptions().put("EJBGenerator", ejbOptions);
		}

		private void initModel() {
			List<FMClass> classes = FMModel.getInstance().getClasses();
			classes.clear();

			FMClass cl = new FMClass("Preduzece", "ejb.orgsema", "public");
			cl.addProperty(new FMProperty("sifraPreduzeca", "String", "private", 1, 1));
			cl.addProperty(new FMProperty("nazivPreduzeca", "String", "private", 1, 1));
			classes.add(cl);

			cl = new FMClass("Materijal", "ejb.magacin", "public");
			cl.addProperty(new FMProperty("sifraMaterijala", "String", "private", 1, 1));
			cl.addProperty(new FMProperty("nazivMaterijala", "String", "private", 1, 1));
			cl.addProperty(new FMProperty("slozen", "Boolean", "private", 1, 1));
			classes.add(cl);

			cl = new FMClass("Odeljenje", "ejb.orgsema", "public");
			cl.addProperty(new FMProperty("sifra", "String", "private", 1, 1));
			cl.addProperty(new FMProperty("naziv", "String", "private", 1, 1));
			classes.add(cl);

			cl = new FMClass("Osoba", "ejb", "public");
			cl.addProperty(new FMProperty("prezime", "String", "private", 1, 1));
			cl.addProperty(new FMProperty("ime", "String", "private", 1, 1));
			cl.addProperty(new FMProperty("datumRodjenja", "Date", "private", 0, 1));
			cl.addProperty(new FMProperty("clanoviPorodice", "Osoba", "private", 0, -1));
			cl.addProperty(new FMProperty("vestina", "String", "private", 1, 3));
			classes.add(cl);

			cl = new FMClass("Kartica", "ejb.magacin.kartica", "public");
			cl.addProperty(new FMProperty("sifraKartice", "String", "private", 1, 1));
			cl.addProperty(new FMProperty("nazivKartice", "String", "private", 1, 1));
			classes.add(cl);
		}

		@Test
		public void testEJBGenerator() {
			GeneratorOptions go = ProjectOptions.getProjectOptions().getGeneratorOptions().get("EJBGenerator");
			EJBGenerator g = new EJBGenerator(go);
			g.generate();
		}
	}