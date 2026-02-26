package myplugin.generator.fmmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class FMEnumeration extends FMType {
	private final ArrayList <String> values = new ArrayList<>();
	
	public FMEnumeration(String name, String typePackage) {
		super(name, typePackage);
	}
}
