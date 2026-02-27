package ${packageName};

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "${nameUtil.toSnakeCase(clazz.name)}")
public class ${clazz.name} {
<#if !hasId>
  <#if idStrategy?string == "LONG_IDENTITY">
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  <#elseif idStrategy?string == "UUID">
    @Id
    @GeneratedValue
    private java.util.UUID id;
  </#if>
</#if>
<#list props as p>
  <#if p.id>
    <#if idStrategy?string == "LONG_IDENTITY">
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    <#elseif idStrategy?string == "UUID">
    @Id
    @GeneratedValue
    </#if>
  </#if>
    @Column(name = "${nameUtil.toSnakeCase(p.name)}")
    private ${typeUtil.toJava(p.type)} ${p.name};
</#list>
}