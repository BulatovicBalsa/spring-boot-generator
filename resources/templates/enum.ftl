package ${packageName};

public enum ${enum.name} {
<#list enum.values as v>
    ${v}<#if v_has_next>,</#if>
</#list>
}