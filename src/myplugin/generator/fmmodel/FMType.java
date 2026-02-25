package myplugin.generator.fmmodel;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FMType extends FMElement {

    public FMType(String name, String typePackage) {
		super(name);
		this.typePackage = typePackage;
	}

    //Qualified package name, used for import declaration
	//Empty string for standard library types
	private String typePackage;

}
