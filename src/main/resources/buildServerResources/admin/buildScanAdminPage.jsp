<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@include file="/include-internal.jsp" %>
    <jsp:useBean id="count" type="java.lang.Integer" scope="request"/>
    <h2 class="cleanUp">Cleanup storage of previous plugin versions</h2>
    <c:choose>
        <c:when test = "${count > 0}">
            <authz:authorize allPermissions="CONFIGURE_SERVER_DATA_CLEANUP">
                <table class="runnerFormTable">
                  <tbody>
                    <tr>
                     <th>Warning: This will remove all build scans from previous versions of this plugin</th>
                     <td>
                         <div id="cleanupResult" style="visibility:hidden;">Not running</div>
                         <form id="cleanupCustomDataStorage" action="<c:url value='/admin/buildScanCleanup.html'/>" onsubmit="return false">
                           <input class="btn btn_mini" type="button" id="startBuildScanCleanupButton" value="Start clean-up now" onclick="return BS.BuildScan.startCleanup();">
                         </form>
                     </td>
                     <td>
                       <forms:progressRing id="progressRing" className="progressRingInline" style="visibility:hidden;"/></td>
                    </tr>
                  </tbody>
                </table>
                <br/>

            </authz:authorize>
        </c:when>
        <c:otherwise>
            No cleanup required
        </c:otherwise>
    </c:choose>
