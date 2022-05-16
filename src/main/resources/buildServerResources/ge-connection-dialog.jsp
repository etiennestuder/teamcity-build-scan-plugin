<%--suppress XmlPathReference --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="keys" class="nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants"/>
<jsp:useBean id="project" type="jetbrains.buildServer.serverSide.SProject" scope="request"/>

<tr>
    <td><label for="displayName">Display name:</label><l:star/></td>
    <td>
        <props:textProperty name="displayName" className="longField"/>
        <span class="smallNote">Provide some name to distinguish this connection from others.</span>
        <span class="error" id="error_displayName"></span>
    </td>
</tr>

<tr>
    <td><label for="${keys.gradleEnterpriseUrl}">Gradle Enterprise Server URL:</label></td>
    <td>
        <props:textProperty name="${keys.gradleEnterpriseUrl}" className="longField"/>
        <span class="smallNote">The URL of the Gradle Enterprise instance to which Build Scans will be published.</span>
        <span class="error" id="error_displayName"></span>
    </td>
</tr>

<tr>
    <td><label for="${keys.gradleEnterpriseAccessKey}">Gradle Enterprise Access Key:</label></td>
    <td>
        <props:passwordProperty name="${keys.gradleEnterpriseAccessKey}" className="longField"/>
        <span class="smallNote">The access key used to authenticate with the Gradle Enterprise instance.</span>
        <span class="error" id="error_displayName"></span>
    </td>
</tr>

<tr>
    <td><label for="${keys.gradleEnterprisePluginVersion}">Gradle Enterprise Plugin Version:</label></td>
    <td>
        <props:textProperty name="${keys.gradleEnterprisePluginVersion}" className="longField"/>
        <span class="smallNote">The version of the Gradle Enterprise Plugin to apply to Gradle builds.</span>
        <span class="error" id="error_displayName"></span>
    </td>
</tr>

<tr>
    <td><label for="${keys.commonCustomUserDataPluginVersion}">Common Custom User Data Plugin Version:</label></td>
    <td>
        <props:textProperty name="${keys.commonCustomUserDataPluginVersion}" className="longField"/>
        <span class="smallNote">The version of the Common Custom User Data Plugin to apply to Gradle builds.</span>
        <span class="error" id="error_displayName"></span>
    </td>
</tr>

<tr>
    <td><label for="${keys.gradleEnterpriseExtensionVersion}">Gradle Enterprise Extension Version:</label></td>
    <td>
        <props:textProperty name="${keys.gradleEnterpriseExtensionVersion}" className="longField"/>
        <span class="smallNote">The version of the Gradle Enterprise Extension to apply to Maven builds.</span>
        <span class="error" id="error_displayName"></span>
    </td>
</tr>

<tr>
    <td><label for="${keys.commonCustomUserDataExtensionVersion}">Common Custom User Data Extension Version:</label></td>
    <td>
        <props:textProperty name="${keys.commonCustomUserDataExtensionVersion}" className="longField"/>
        <span class="smallNote">The version of the Common Custom User Data Extension to apply to Maven builds.</span>
        <span class="error" id="error_displayName"></span>
    </td>
</tr>
