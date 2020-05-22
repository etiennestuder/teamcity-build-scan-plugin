BS.BuildScan = {
    startCleanup: function () {
        $('startBuildScanCleanupButton').disabled = true
        $('cleanupResult').textContent = 'Cleanup is running...';
        $('cleanupResult').style.visibility = 'visible'
        $('progressRing').style.visibility = 'visible'
        BS.ajaxRequest('/admin/buildScanCleanup.html', {
            method: 'POST',
            onComplete: function (res) {
                $('progressRing').style.visibility = 'hidden'
                if (res.status === 200) {
                    $('cleanupResult').textContent = 'Cleanup successful';
                } else {
                    $('cleanupResult').textContent = 'Cleanup failed - check the server logs for details'
                }
                $('startBuildScanCleanupButton').disabled = false
            }
        });
        return false;
    }
}
