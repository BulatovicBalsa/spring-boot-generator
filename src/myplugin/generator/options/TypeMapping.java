package myplugin.generator.options;

import lombok.Getter;
import lombok.Setter;

/** TypeMapping: UML 2.0 to java (or any other destination language) type mapping */

@Getter
@Setter
public class TypeMapping {
	private String umlType;
	private String destType;
	
	//libraryName: name used for import declaration  
	private String libraryName;
	
	public TypeMapping(String uMLType, String destType, String libraryName) {
		super();
		this.umlType = uMLType;
		this.destType = destType;
		this.libraryName = libraryName;
	}
}
