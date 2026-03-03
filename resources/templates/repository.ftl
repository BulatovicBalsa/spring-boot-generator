package ${packageName};

import org.springframework.data.jpa.repository.JpaRepository;
<#if idType == "UUID">
import java.util.UUID;
</#if>

public interface ${clazz.name}Repository extends JpaRepository<${clazz.name}, ${idType}> {
}