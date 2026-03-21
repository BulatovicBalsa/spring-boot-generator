package myplugin.generator.fmmodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import myplugin.generator.IdStrategy;
import myplugin.generator.TypeUtil;
import myplugin.generator.options.GeneratorOptions;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class FMClass {
	private String name;
	private List<FMProperty> properties = new ArrayList<>();
	private boolean embeddable = false;

	public FMClass(String name) {
		this.name = name;
	}

	public boolean hasId() {
		for (FMProperty prop : properties) {
			if (prop.isId()) return true;
		}
		return false;
	}

	public String resolveIdType(GeneratorOptions generatorOptions) {
		FMProperty idProp = findIdProperty();
		if (idProp != null && idProp.getType() != null) {
			String javaType = new TypeUtil().toJava(idProp.getType());
			if ("java.util.UUID".equals(javaType)) return "UUID";
			if (javaType.indexOf('.') >= 0) return javaType.substring(javaType.lastIndexOf('.') + 1);
			return javaType;
		}
		if (generatorOptions.getIdStrategy() == IdStrategy.UUID) return "UUID";
		return "Long";
	}

	private FMProperty findIdProperty() {
		if (this.getProperties() == null) return null;
		for (FMProperty p : this.getProperties()) {
			if (p != null && p.isId()) return p;
		}
		return null;
	}
}