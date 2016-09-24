<%@ page import="nu.studer.teamcity.buildscan.BuildScanReferences" %>
<%@ page import="nu.studer.teamcity.buildscan.BuildScanReference" %>
<%@ include file="/include.jsp" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<tr>
          <td class="st">Build Scan:</td>
          <td class="st">
               <c:choose>
                   <c:when test="${buildScans.size()>1}">
                     <a href="${buildScans.first().url}" target="_blank">${buildScans.first().id}</a><c:forEach items="${buildScans.all()}" var="buildScan" begin="1">, <a href="${buildScan.url}" target="_blank">${buildScan.id}</a></c:forEach>
                   </c:when>
                   <c:when test="${!buildScans.isEmpty()}">
                     <a href="${buildScans.first().url}" target="_blank">${buildScans.first().id}</a>
                   </c:when>
                   <c:otherwise>
                     ---
                   </c:otherwise>
               </c:choose>
          </td>
          <td class="st labels"/>
          <td class="st fixed"/>
</tr>
