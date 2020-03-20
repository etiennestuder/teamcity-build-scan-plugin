<%@ include file="/include.jsp" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div>
<c:choose>
    <c:when test="${buildScans.size()>1}">
        <div>
            Build scans
            <a href="${buildScans.first().url}" target="_blank">${buildScans.first().urlWithoutProtocol}</a><c:forEach items="${buildScans.all()}" var="buildScan" begin="1">,
            <a href="${buildScan.url}" target="_blank">${buildScan.urlWithoutProtocol}</a></c:forEach> have been published.
        </div>
    </c:when>
    <c:when test="${!buildScans.isEmpty()}">
        <div>Build scan <a href="${buildScans.first().url}" target="_blank">${buildScans.first().urlWithoutProtocol}</a> has been published.</div>
    </c:when>
    <c:otherwise>
        <div>
            No build scans have been published.
            Learn more about how to enable Gradle build scans <a href="https://scans.gradle.com" target="_blank">here</a>.
        </div>
    </c:otherwise>
</c:choose>
</div>
