<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@include file="/include-internal.jsp" %>
<jsp:useBean id="itemsToRemove" type="java.lang.Integer" scope="request"/>
<h2 class="cleanUp">Legacy persistence clean-up</h2>
<p>Prior to v0.12 of the teamcity-build-scan-plugin, the build scan links contained in the executed TeamCity builds were persisted
in the TeamCity database. In scenarios of many hundreds of thousands of builds, this caused performance issues. As of v0.12 of the
teamcity-build-scan-plugin, the build scan links for a given TeamCity build are persisted more efficiently as hidden build artifacts.
Running the clean-up action below will irrevocably remove all build scan links stored in the legacy persistence. No TeamCity builds
will be deleted and no build scans will be deleted.</p>
<c:choose>
    <c:when test = "${itemsToRemove > 0}">
        <authz:authorize allPermissions="CONFIGURE_SERVER_DATA_CLEANUP">
            <table class="runnerFormTable">
              <tbody>
                <tr>
                 <th>Warning: This action cannot be undone</th>
                 <td>
                     <div id="cleanupResult" style="visibility:hidden;">Not running</div>
                     <input class="btn btn_mini" type="button" id="startBuildScanCleanupButton" value="Start clean-up now" onclick="return BS.BuildScan.startCleanup();">
                     <forms:progressRing id="progressRing" className="progressRingInline" style="visibility:hidden;"/>
                 </td>
                </tr>
              </tbody>
            </table>
        </authz:authorize>
    </c:when>
    <c:otherwise>
        There are no build scan links stored in the legacy persistence. No clean-up is required.
    </c:otherwise>
</c:choose>
