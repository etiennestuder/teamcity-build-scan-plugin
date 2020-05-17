<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@include file="/include-internal.jsp" %>
    <jsp:useBean id="count" type="java.lang.Integer" scope="request"/>
    <h3>Cleanup storage of previous plugin versions</h3>
    <c:choose>
        <c:when test = "${count > 0}">
            <h4>Warning: This will remove all build scans from previous versions of this plugin</h4>
            <authz:authorize allPermissions="CONFIGURE_SERVER_DATA_CLEANUP">

                <table>
                  <tr><td><div id="cleanupResult" style="visibility:hidden;">Not running</div></td><td></td></tr>
                  <tr><td><form id="cleanupCustomDataStorage" method="POST" action="<c:url value='/admin/buildScanCleanup.html'/>" onsubmit="">
                <input class="btn btn_mini" type="button" id="startBuildScanCleanupButton" value="Start clean-up now" onclick="return BS.BuildScan.startCleanup();">
                </form></td><td><forms:progressRing id="progressRing" className="progressRingInline" style="visibility:hidden;"/></td></tr>
                </table>
                <br/>

            </authz:authorize>
        </c:when>
        <c:otherwise>
            No cleanup required
        </c:otherwise>
    </c:choose>
