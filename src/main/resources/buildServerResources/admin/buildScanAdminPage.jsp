<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@include file="/include-internal.jsp" %>
    <jsp:useBean id="itemsToRemove" type="java.lang.Integer" scope="request"/>
    <h2 class="cleanUp">Cleanup storage of previous plugin versions</h2>
    <p>This text will explain the admin user the cleanup procedure in detail.</p>
    <c:choose>
        <c:when test = "${itemsToRemove > 0}">
            <authz:authorize allPermissions="CONFIGURE_SERVER_DATA_CLEANUP">
                <table class="runnerFormTable">
                  <tbody>
                    <tr>
                     <th>Warning: This will remove all build scans from previous versions of this plugin</th>
                     <td>
                         <div id="cleanupResult" style="visibility:hidden;">Not running</div>
                         <input class="btn btn_mini" type="button" id="startBuildScanCleanupButton" value="Start clean-up now" onclick="return BS.BuildScan.startCleanup();">
                         <forms:progressRing id="progressRing" className="progressRingInline" style="visibility:hidden;"/>
                     </td>
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
