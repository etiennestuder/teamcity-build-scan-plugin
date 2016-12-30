<%@ include file="/include.jsp" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<tr>
    <td class="st"><c:choose><c:when test="${buildScans.size()>1}">Build scans:</c:when><c:otherwise>Build scan:</c:otherwise></c:choose></td>
          <td class="st">
               <c:choose>
                   <c:when test="${buildScans.size()>1}">
                       <a href="${buildScans.first().url}" target="_blank">${buildScans.first().urlWithoutProtocol}</a><c:forEach items="${buildScans.all()}" var="buildScan" begin="1"><br><a href="${buildScan.url}" target="_blank">${buildScan.urlWithoutProtocol}</a></c:forEach>
                   </c:when>
                   <c:when test="${!buildScans.isEmpty()}">
                       <a href="${buildScans.first().url}" target="_blank">${buildScans.first().urlWithoutProtocol}</a>
                   </c:when>
                   <c:otherwise>
                     ---
                   </c:otherwise>
               </c:choose>
          </td>
          <td class="st labels"/>
          <td class="st fixed"/>
</tr>
