package ${packageName};

<#if idType == "UUID">
import java.util.UUID;
</#if>

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

// <protected:imports>
// </protected:imports>

import ${entityPackage}.${clazz.name};

@Repository
public interface ${clazz.name}Repository extends JpaRepository<${clazz.name}, ${idType}> {
	// <protected:methods>
	// </protected:methods>
}