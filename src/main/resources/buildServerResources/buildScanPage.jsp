<%@ include file="/include.jsp" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div>
<c:choose>
    <c:when test="${buildScans.size()>1}">
        <div>
            <h2>Build scans</h2>
            <ul>
                <c:forEach items="${buildScans.all()}" var="buildScan">,
                    <li><a href="${buildScan.url}" target="_blank"><img src="${buildScan.buildScanBadge}" alt="${buildScan.urlWithoutProtocol}"></a></li>
                </c:forEach>
            </ul>
        </div>
    </c:when>
    <c:when test="${!buildScans.isEmpty()}">
        <div>Build scan <a href="${buildScans.first().url}" target="_blank"><img src="${buildScans.first().buildScanBadge}" alt="${buildScan.urlWithoutProtocol}"></a> has been published.</div>
    </c:when>
    <c:otherwise>
        <div>
            No build scans have been published.
            Learn more about how to enable Gradle build scans <a href="https://scans.gradle.com" target="_blank">here</a>.
        </div>
    </c:otherwise>
</c:choose>
</div>
