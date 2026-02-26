package ${packageName};

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "${tableName}")
public class ${clazz.name} {

<#list props as p>
<#if p.name?lower_case == "id">
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
</#if>
    private ${typeUtil.toJava(p.type)} ${p.name};
</#list>
}