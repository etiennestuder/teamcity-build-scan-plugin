BS.BuildScan = {
    startCleanup: function () {
        $('cleanupResult').style.visibility = 'visible'
        $('startBuildScanCleanupButton').disabled = true
        $('progressRing').style.visibility = 'visible'
        $('cleanupResult').textContent = 'Cleanup is running...';
        BS.ajaxRequest('/admin/buildScanCleanup.html', {
            method: 'POST',
            onComplete: function (res) {
                $('progressRing').style.visibility = 'hidden'
                $('startBuildScanCleanupButton').disabled = false
                if (res.status === 200) {
                    $('cleanupResult').textContent = 'Cleanup was successful';
                } else {
                    $('cleanupResult').textContent = 'Failed to execute cleanup - check the server logs'
                }
            }
        });
        return false;
    }
}