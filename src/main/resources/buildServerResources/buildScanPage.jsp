<%@ page import="nu.studer.teamcity.buildscan.BuildScanReferences" %>
<%@ page import="nu.studer.teamcity.buildscan.BuildScanReference" %>
<%@ include file="/include.jsp" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div>
<c:choose>
    <c:when test="${buildScans.size()>1}">
        <div>
            Build Scans <a href="${buildScans.first().url}" target="_blank">${buildScans.first().id}</a><c:forEach items="${buildScans.all()}" var="buildScan" begin="1">, <a href="${buildScan.url}" target="_blank">${buildScan.id}</a></c:forEach> have been published.
        </div>
    </c:when>
    <c:when test="${!buildScans.isEmpty()}">
        <div>Build Scan <a href="${buildScans.first().url}" target="_blank">${buildScans.first().id}</a> has been published.</div>
    </c:when>
    <c:otherwise>
        <div>
            No Build Scan has been published.
            Learn more about how to enable Gradle Build Scans <a href="https://scans.gradle.com/get-started" target="_blank">here</a>.
        </div>
    </c:otherwise>
</c:choose>
</div>
