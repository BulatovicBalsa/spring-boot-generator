package myplugin.generator.fmmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FMClass extends FMType {	
	
	private String visibility;
	
	//Class properties
	private final List<FMProperty> properties = new ArrayList<>();
	
	//list of packages (for import declarations) 
	private final List<String> importedPackages = new ArrayList<>();
	
	public FMClass(String name, String classPackage, String visibility) {
		super(name, classPackage);		
		this.visibility = visibility;
	}

	public void addProperty(FMProperty property){
		properties.add(property);
	}
}
